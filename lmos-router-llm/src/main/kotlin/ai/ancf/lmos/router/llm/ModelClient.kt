// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.ai.openai.models.*
import com.azure.core.credential.AzureKeyCredential
import org.eclipse.lmos.router.core.*

/**
 * The [ModelClient] interface represents a client that can call a model.
 *
 * The [call] method calls the model with the given messages.
 *
 * @see ChatMessage
 * @see AgentRoutingSpecResolverException
 * @see Result
 */
interface ModelClient {
    fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>
}

/**
 * The [DefaultModelClient] class is a default implementation of the [ModelClient] interface.
 *
 * The [call] method calls the OpenAI model with the given messages.
 *
 * @param defaultModelClientProperties The properties for the default model client.
 */
class DefaultModelClient(private val defaultModelClientProperties: DefaultModelClientProperties) : ModelClient {
    private var client =
        OpenAIClientBuilder()
            .credential(AzureKeyCredential(defaultModelClientProperties.openAiApiKey))
            .endpoint(defaultModelClientProperties.openAiUrl)
            .buildClient()

    override fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException> {
        try {
            val chatCompletionsOptions =
                ChatCompletionsOptions(
                    messages.map {
                        when (it) {
                            is UserMessage -> ChatRequestUserMessage(it.content)
                            is AssistantMessage -> ChatRequestAssistantMessage(it.content)
                            is SystemMessage -> ChatRequestSystemMessage(it.content)
                            else -> throw AgentRoutingSpecResolverException("Unknown message type")
                        }
                    },
                ).setTemperature(defaultModelClientProperties.temperature)
                    .setModel(defaultModelClientProperties.model)
                    .setMaxTokens(defaultModelClientProperties.maxTokens)
                    .apply {
                        defaultModelClientProperties.format.let {
                            responseFormat = ChatCompletionsJsonResponseFormat()
                        }
                    }

            return Success(
                AssistantMessage(
                    client.getChatCompletions(
                        defaultModelClientProperties.model,
                        chatCompletionsOptions,
                    ).choices.first().message.content,
                ),
            )
        } catch (e: Exception) {
            return Failure(AgentRoutingSpecResolverException("Failed to call model", e))
        }
    }
}

/**
 * The [DefaultModelClientProperties] data class represents the properties for the default model client.
 *
 * @param openAiUrl The OpenAI URL.
 * @param openAiApiKey The OpenAI API key.
 * @param model The model.
 * @param maxTokens The maximum number of tokens.
 * @param temperature The temperature.
 * @param format The format.
 */
data class DefaultModelClientProperties(
    val openAiUrl: String = "https://api.openai.com/v1/chat/completions",
    val openAiApiKey: String,
    val model: String = "gpt-4o-mini",
    val maxTokens: Int = 200,
    val temperature: Double = 0.0,
    val format: String = "json_object",
)

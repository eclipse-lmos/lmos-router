// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

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
class DefaultModelClient(
    private val defaultModelClientProperties: DefaultModelClientProperties,
    private val delegate: LangChainModelClient =
        LangChainModelClient(LangChainChatModelFactory.createClient(defaultModelClientProperties)),
) : ModelClient {
    override fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException> = delegate.call(messages)
}

open class ModelClientProperties(
    open val provider: String,
    open val apiKey: String? = null,
    open val baseUrl: String? = null,
    open val model: String,
    open val maxTokens: Int = 2000,
    open val temperature: Double = 0.0,
    open val format: String? = null,
    open val topK: Int? = null,
    open val topP: Double? = null,
)

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
    val openAiUrl: String = "https://api.openai.com/v1",
    val openAiApiKey: String,
    override val model: String = "gpt-4o-mini",
    override val maxTokens: Int = 200,
    override val temperature: Double = 0.0,
    override val format: String = "json_object",
    override val apiKey: String? = openAiApiKey,
    override val baseUrl: String = openAiUrl,
    override val provider: String = "openai",
) : ModelClientProperties(
        provider,
        apiKey,
        baseUrl,
        model,
        maxTokens,
        temperature,
        format,
    )

/**
 * This interface represents a model response processor.
 *
 * The objective is to process the response from the model and return agentSpec compliant json.
 */
interface ModelClientResponseProcessor {
    fun process(modelResponse: String): String
}

/**
 * This class is a default implementation of the ModelResponseProcessor interface.
 *
 * The processResponse method processes the response from the model.
 *
 * By default, it cleans the response and remove ```json and <answer> tags. Refer default prompt for more information.
 */
class DefaultModelClientResponseProcessor : ModelClientResponseProcessor {
    override fun process(modelResponse: String): String {
        var response = modelResponse.trim()

        if (response.contains("```json")) {
            response = response.substringAfter("```json").substringBefore("```").trim()
        }

        if (response.contains("<answer>")) {
            response = response.substringAfter("<answer>").substringBefore("</answer>").trim()
        }
        return response
    }
}

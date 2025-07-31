// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.azure.AzureOpenAiChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder
import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.llm.LangChainClientProvider.AZURE_OPENAI

/**
 * A model client that uses Langchain4j(https://docs.langchain4j.dev/) to call a language model.
 *
 * @param chatModel The language model.
 */
class LangChainModelClient(
    private val chatModel: ChatModel,
) : ModelClient {
    override fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException> {
        try {
            val response =
                chatModel.chat(
                    messages.map {
                        when (it) {
                            is UserMessage ->
                                dev.langchain4j.data.message
                                    .UserMessage(it.content)
                            is AssistantMessage -> AiMessage(it.content)
                            is SystemMessage ->
                                dev.langchain4j.data.message
                                    .SystemMessage(it.content)
                            else -> throw AgentRoutingSpecResolverException("Unknown message type")
                        }
                    },
                )
            return Success(AssistantMessage(response.aiMessage().text()))
        } catch (e: Exception) {
            return Failure(AgentRoutingSpecResolverException("Failed to call language model", e))
        }
    }
}

/**
 * A factory class to create a Langchain4j language model client. The factory creates a client based on the given properties.
 *
 * The factory supports the following providers:
 * - OPENAI
 * - ANTHROPIC
 * - GEMINI
 * - OLLAMA
 * - OTHER (for other providers which have OpenAI compatible API)
 */
class LangChainChatModelFactory private constructor() {
    companion object {
        fun createClient(properties: ModelClientProperties): ChatModel =
            when (properties.provider) {
                LangChainClientProvider.OPENAI.name.lowercase(),
                -> {
                    val model =
                        OpenAiChatModelBuilder()
                            .apiKey(properties.apiKey)
                            .modelName(properties.model)
                            .maxTokens(properties.maxTokens)
                            .temperature(properties.temperature)

                    properties.topP?.let { model.topP(it) }
                    properties.format.takeIf { it != null }?.let { model.responseFormat(it) }

                    model.build()
                }

                LangChainClientProvider.ANTHROPIC.name.lowercase() -> {
                    val model =
                        AnthropicChatModel
                            .builder()
                            .apiKey(properties.apiKey)
                            .modelName(properties.model)
                            .maxTokens(properties.maxTokens)
                            .temperature(properties.temperature)

                    properties.topP?.let { model.topP(it) }
                    properties.topK?.let { model.topK(it) }

                    model.build()
                }

                AZURE_OPENAI.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }

                    val model =
                        AzureOpenAiChatModel
                            .builder()
                            .endpoint(properties.baseUrl)
                            .apiKey(properties.apiKey)
                            .deploymentName(properties.model)
                            .maxTokens(properties.maxTokens)
                            .temperature(properties.temperature)

                    properties.topP?.let { model.topP(it) }

                    model.build()
                }

                LangChainClientProvider.GEMINI.name.lowercase() -> {
                    val model =
                        GoogleAiGeminiChatModel
                            .builder()
                            .modelName(properties.model)
                            .maxOutputTokens(properties.maxTokens)
                            .temperature(properties.temperature)
                            .topK(properties.topK)
                            .topP(properties.topP)

                    properties.format.takeIf { it != null }?.let {
                        model.responseFormat(
                            ResponseFormat
                                .builder()
                                .type(ResponseFormatType.valueOf(it))
                                .build(),
                        )
                    }
                    properties.apiKey?.let { model.apiKey(it) }
                    properties.topK?.let { model.topK(it) }
                    properties.topP?.let { model.topP(it) }

                    model.build()
                }

                LangChainClientProvider.OLLAMA.name.lowercase() -> {
                    OllamaChatModel
                        .builder()
                        .baseUrl(properties.baseUrl)
                        .modelName(properties.model)
                        .temperature(properties.temperature)
                        .build()
                }

                LangChainClientProvider.OTHER.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for OTHER provider" }

                    val model =
                        OpenAiChatModelBuilder()
                            .baseUrl(properties.baseUrl)
                            .apiKey(properties.apiKey)
                            .modelName(properties.model)
                            .maxTokens(properties.maxTokens)
                            .temperature(properties.temperature)
                    properties.topP?.let { model.topP(it) }
                    properties.format.takeIf { it != null }?.let { model.responseFormat(it) }

                    model.build()
                }

                else -> {
                    throw IllegalArgumentException("Unknown model client properties: $properties")
                }
            }
    }
}

enum class LangChainClientProvider {
    OPENAI,
    ANTHROPIC,
    AZURE_OPENAI,
    GEMINI,
    OLLAMA,
    OTHER,
}

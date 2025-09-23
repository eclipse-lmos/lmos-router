// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import com.azure.identity.DefaultAzureCredentialBuilder
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.azure.AzureOpenAiChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder
import org.eclipse.lmos.classifier.llm.LangChainClientProvider.*
import org.slf4j.LoggerFactory

/**
 * A factory class to create a Langchain4j chat model client.
 */
class LangChainChatModelFactory private constructor() {
    companion object {
        fun createClient(properties: ChatModelClientProperties): ChatModel =
            when (properties.provider) {
                OPENAI.name.lowercase() -> {
                    OpenAiChatModelBuilder()
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .strictJsonSchema(true)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                ANTHROPIC.name.lowercase() -> {
                    require(properties.apiKey != null) { "apiKey is required for '${ANTHROPIC.name.lowercase()}' provider" }
                    AnthropicChatModel
                        .builder()
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                AZURE_OPENAI.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    require(properties.apiKey != null) { "apiKey is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    AzureOpenAiChatModel
                        .builder()
                        .endpoint(properties.baseUrl)
                        .apiKey(properties.apiKey)
                        .deploymentName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequestsAndResponses(properties.logRequestsAndResponses)
                        .strictJsonSchema(true)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                AZURE_OPENAI_IDENTITY.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    AzureOpenAiChatModel
                        .builder()
                        .endpoint(properties.baseUrl)
                        .tokenCredential(DefaultAzureCredentialBuilder().build())
                        .deploymentName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequestsAndResponses(properties.logRequestsAndResponses)
                        .strictJsonSchema(true)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                GEMINI.name.lowercase() -> {
                    require(properties.apiKey != null) { "apiKey is required for '${GEMINI.name.lowercase()}' provider" }
                    GoogleAiGeminiChatModel
                        .builder()
                        .modelName(properties.model)
                        .apiKey(properties.apiKey)
                        .maxOutputTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequestsAndResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                OLLAMA.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for '${OLLAMA.name.lowercase()}' provider" }
                    OllamaChatModel
                        .builder()
                        .baseUrl(properties.baseUrl)
                        .modelName(properties.model)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
                }

                OTHER.name.lowercase() -> {
                    require(properties.baseUrl != null) { "baseUrl is required for OTHER provider" }
                    OpenAiChatModelBuilder()
                        .baseUrl(properties.baseUrl)
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .logRequests(properties.logRequestsAndResponses)
                        .logResponses(properties.logRequestsAndResponses)
                        .listeners(listOf(ModelConversationLogger()))
                        .build()
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
    AZURE_OPENAI_IDENTITY,
    GEMINI,
    OLLAMA,
    OTHER,
}

open class ChatModelClientProperties(
    open val provider: String,
    open val apiKey: String? = null,
    open val baseUrl: String? = null,
    open val model: String,
    open val maxTokens: Int = 2000,
    open val temperature: Double = 0.0,
    open val logRequestsAndResponses: Boolean = false,
)

class ModelConversationLogger : ChatModelListener {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onRequest(requestContext: ChatModelRequestContext) {
        logger.debug("onRequest(): {}", requestContext.chatRequest())
    }

    override fun onResponse(responseContext: ChatModelResponseContext) {
        logger.debug("onResponse(): {}", responseContext.chatResponse())
    }

    override fun onError(errorContext: ChatModelErrorContext) {
        logger.error("onError(): {}", errorContext.error().message)
    }
}

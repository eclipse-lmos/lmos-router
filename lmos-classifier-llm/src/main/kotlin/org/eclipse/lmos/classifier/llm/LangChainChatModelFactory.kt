// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import com.azure.identity.DefaultAzureCredentialBuilder
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.azure.AzureOpenAiChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder
import org.eclipse.lmos.classifier.llm.LangChainClientProvider.*

/**
 * A factory class to create a Langchain4j chat model client.
 */
class LangChainChatModelFactory private constructor() {
    companion object {
        fun createClient(
            chatModelProperties: ChatModelClientProperties,
            chatModelListeners: List<ClassifierChatModelListener> = emptyList(),
        ): ChatModel =
            when (chatModelProperties.provider) {
                OPENAI.name.lowercase() -> {
                    OpenAiChatModelBuilder()
                        .apiKey(chatModelProperties.apiKey)
                        .modelName(chatModelProperties.model)
                        .maxTokens(chatModelProperties.maxTokens)
                        .temperature(chatModelProperties.temperature)
                        .logRequests(chatModelProperties.logRequestsAndResponses)
                        .logResponses(chatModelProperties.logRequestsAndResponses)
                        .strictJsonSchema(true)
                        .listeners(chatModelListeners)
                        .build()
                }

                ANTHROPIC.name.lowercase() -> {
                    require(chatModelProperties.apiKey != null) { "apiKey is required for '${ANTHROPIC.name.lowercase()}' provider" }
                    AnthropicChatModel
                        .builder()
                        .apiKey(chatModelProperties.apiKey)
                        .modelName(chatModelProperties.model)
                        .maxTokens(chatModelProperties.maxTokens)
                        .temperature(chatModelProperties.temperature)
                        .logRequests(chatModelProperties.logRequestsAndResponses)
                        .logResponses(chatModelProperties.logRequestsAndResponses)
                        .listeners(chatModelListeners)
                        .build()
                }

                AZURE_OPENAI.name.lowercase() -> {
                    require(chatModelProperties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    require(chatModelProperties.apiKey != null) { "apiKey is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    AzureOpenAiChatModel
                        .builder()
                        .endpoint(chatModelProperties.baseUrl)
                        .apiKey(chatModelProperties.apiKey)
                        .deploymentName(chatModelProperties.model)
                        .maxTokens(chatModelProperties.maxTokens)
                        .temperature(chatModelProperties.temperature)
                        .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses)
                        .strictJsonSchema(true)
                        .listeners(chatModelListeners)
                        .build()
                }

                AZURE_OPENAI_IDENTITY.name.lowercase() -> {
                    require(chatModelProperties.baseUrl != null) { "baseUrl is required for '${AZURE_OPENAI.name.lowercase()}' provider" }
                    AzureOpenAiChatModel
                        .builder()
                        .endpoint(chatModelProperties.baseUrl)
                        .tokenCredential(DefaultAzureCredentialBuilder().build())
                        .deploymentName(chatModelProperties.model)
                        .maxTokens(chatModelProperties.maxTokens)
                        .temperature(chatModelProperties.temperature)
                        .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses)
                        .strictJsonSchema(true)
                        .listeners(chatModelListeners)
                        .build()
                }

                GEMINI.name.lowercase() -> {
                    require(chatModelProperties.apiKey != null) { "apiKey is required for '${GEMINI.name.lowercase()}' provider" }
                    GoogleAiGeminiChatModel
                        .builder()
                        .modelName(chatModelProperties.model)
                        .apiKey(chatModelProperties.apiKey)
                        .maxOutputTokens(chatModelProperties.maxTokens)
                        .temperature(chatModelProperties.temperature)
                        .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses)
                        .listeners(chatModelListeners)
                        .build()
                }

                OLLAMA.name.lowercase() -> {
                    require(chatModelProperties.baseUrl != null) { "baseUrl is required for '${OLLAMA.name.lowercase()}' provider" }
                    OllamaChatModel
                        .builder()
                        .baseUrl(chatModelProperties.baseUrl)
                        .modelName(chatModelProperties.model)
                        .temperature(chatModelProperties.temperature)
                        .logRequests(chatModelProperties.logRequestsAndResponses)
                        .logResponses(chatModelProperties.logRequestsAndResponses)
                        .listeners(chatModelListeners)
                        .build()
                }

                OTHER.name.lowercase() -> {
                    require(chatModelProperties.baseUrl != null) { "baseUrl is required for OTHER provider" }
                    OpenAiChatModelBuilder()
                        .baseUrl(chatModelProperties.baseUrl)
                        .apiKey(chatModelProperties.apiKey)
                        .modelName(chatModelProperties.model)
                        .maxTokens(chatModelProperties.maxTokens)
                        .temperature(chatModelProperties.temperature)
                        .logRequests(chatModelProperties.logRequestsAndResponses)
                        .logResponses(chatModelProperties.logRequestsAndResponses)
                        .listeners(chatModelListeners)
                        .build()
                }

                else -> {
                    throw IllegalArgumentException("Unknown model client properties: $chatModelProperties")
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

interface ClassifierChatModelListener : ChatModelListener

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.azure.AzureOpenAiChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class LangChainChatModelFactoryTest {
    @Test
    fun `createClient should return OpenAiChatModel for OPENAI provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.OPENAI.name.lowercase(),
                baseUrl = "https://api.openai.com",
                apiKey = "openai-api-key",
                model = "gpt-4o-mini",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(OpenAiChatModel::class.java)
    }

    @Test
    fun `createClient should return AnthropicChatModel for ANTHROPIC provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.ANTHROPIC.name.lowercase(),
                apiKey = "anthropic-api-key",
                model = "claude-v1",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(AnthropicChatModel::class.java)
    }

    @Test
    fun `createClient should return AzureOpenAiChatModel for AZURE_OPENAI provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.AZURE_OPENAI.name.lowercase(),
                apiKey = "azure-openai-api-key",
                baseUrl = "https://azure-openai-endpoint.com",
                model = "gpt-4o-mini",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(AzureOpenAiChatModel::class.java)
    }

    @Test
    fun `createClient should return AzureOpenAiChatModel for AZURE_OPENAI_IDENTITY provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.AZURE_OPENAI_IDENTITY.name.lowercase(),
                baseUrl = "https://azure-openai-endpoint.com",
                model = "gpt-4o-mini",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(AzureOpenAiChatModel::class.java)
    }

    @Test
    fun `createClient should return GoogleAiGeminiChatModel for GEMINI provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.GEMINI.name.lowercase(),
                apiKey = "gemini-api-key",
                model = "gemini-1",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(GoogleAiGeminiChatModel::class.java)
    }

    @Test
    fun `createClient should return OllamaChatModel for OLLAMA provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.OLLAMA.name.lowercase(),
                baseUrl = "http://localhost:11434",
                model = "ollama-model",
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(OllamaChatModel::class.java)
    }

    @Test
    fun `createClient should return OpenAiChatModel for OTHER provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = LangChainClientProvider.OTHER.name.lowercase(),
                baseUrl = "https://api.other.com",
                apiKey = "other-api-key",
                model = "other-model",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when
        val client = LangChainChatModelFactory.createClient(properties)

        // then
        assertThat(client).isInstanceOf(OpenAiChatModel::class.java)
    }

    @Test
    fun `createClient should throw IllegalArgumentException for unknown provider`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = "unknown_provider",
                baseUrl = "https://api.unknown.com",
                apiKey = "unknown-api-key",
                model = "unknown-model",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when & then
        assertThatThrownBy { LangChainChatModelFactory.createClient(properties) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unknown model client properties: $properties")
    }

    @Test
    fun `createClient should throw IllegalArgumentException when provider is empty`() {
        // given
        val properties =
            ChatModelClientProperties(
                provider = "",
                baseUrl = "https://api.null.com",
                apiKey = "null-api-key",
                model = "null-model",
                maxTokens = 1000,
                temperature = 0.7,
            )

        // when & then
        assertThatThrownBy { LangChainChatModelFactory.createClient(properties) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unknown model client properties: $properties")
    }
}

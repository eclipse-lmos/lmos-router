// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.ai.openai.models.ChatChoice
import com.azure.ai.openai.models.ChatCompletions
import com.azure.ai.openai.models.ChatResponseMessage
import com.azure.core.credential.AzureKeyCredential
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.eclipse.lmos.router.core.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultModelClientTest {
    private lateinit var defaultModelClientProperties: DefaultModelClientProperties
    private lateinit var client: OpenAIClient
    private lateinit var defaultModelClient: DefaultModelClient

    @BeforeEach
    fun setUp() {
        defaultModelClientProperties =
            DefaultModelClientProperties(
                openAiApiKey = "fake-api-key",
                openAiUrl = "https://api.openai.com/v1/chat/completions",
                model = "gpt-4o-mini",
                maxTokens = 200,
                temperature = 0.0,
                format = "json_object",
            )

        client = mockk()
        mockkConstructor(OpenAIClientBuilder::class)
        every {
            anyConstructed<OpenAIClientBuilder>().credential(any<AzureKeyCredential>()).endpoint(any<String>())
                .buildClient()
        } returns client

        defaultModelClient = DefaultModelClient(defaultModelClientProperties)
    }

    @Test
    fun `call should return AssistantMessage when valid messages are passed`() {
        val messages =
            listOf(
                UserMessage("Hello, how can I assist you today?"),
                AssistantMessage("I am here to help you."),
            )

        val mockResponse = mockk<ChatCompletions>()
        val mockChoice = mockk<ChatChoice>()
        val mockChatMessage = mockk<ChatResponseMessage>()
        every { mockResponse.choices } returns listOf(mockChoice)
        every { mockChoice.message } returns mockChatMessage
        every { mockChatMessage.content } returns "This is a response from the assistant."

        every { client.getChatCompletions(any(), any()) } returns mockResponse

        val result = defaultModelClient.call(messages)

        assertEquals(result, Success(AssistantMessage("This is a response from the assistant.")))

        verify { client.getChatCompletions(any(), any()) }
    }

    @Test
    fun `call should throw AgentRoutingSpecResolverException for API call failure`() {
        val messages =
            listOf(
                UserMessage("Hello, how can I assist you today?"),
            )

        every { client.getChatCompletions(any(), any()) } throws RuntimeException("API failure")

        val result = defaultModelClient.call(messages)

        assert(result is Failure)
        assert((result as Failure).exceptionOrNull()?.message == "Failed to call model")
    }
}

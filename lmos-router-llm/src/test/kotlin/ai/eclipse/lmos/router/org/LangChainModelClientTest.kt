// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm.org.eclipse.lmos.router.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.llm.LangChainModelClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import dev.langchain4j.data.message.SystemMessage as LangchainSystemMessage
import dev.langchain4j.data.message.UserMessage as LangchainUserMessage

/**
 * Tests for [LangChainModelClient].
 */
class LangChainModelClientTest {
    private val mockChatLanguageModel = mockk<ChatModel>()
    private val modelClient = LangChainModelClient(mockChatLanguageModel)

    @Test
    fun `call should return Success with AssistantMessage when model generates response successfully`() {
        // Arrange
        val userMessage = UserMessage("Hello")
        val assistantResponse = "Hi there!"
        val mockResponse = mockk<ChatResponse>()

        every { mockResponse.aiMessage().text() } returns assistantResponse
        every {
            mockChatLanguageModel.chat(
                listOf(LangchainUserMessage("Hello")),
            )
        } returns mockResponse

        // Act
        val result = modelClient.call(listOf(userMessage))

        // Assert
        assertTrue(result is Success)
        val success = result as Success
        assertEquals(AssistantMessage(assistantResponse), success.value)
        verify(exactly = 1) {
            mockChatLanguageModel.chat(listOf(LangchainUserMessage("Hello")))
        }
    }

    @Test
    fun `call should return Failure when model generate throws exception`() {
        // Arrange
        val userMessage = UserMessage("Hello")
        val exception = RuntimeException("Model error")

        every {
            mockChatLanguageModel.chat(
                listOf(LangchainUserMessage("Hello")),
            )
        } throws exception

        // Act
        val result = modelClient.call(listOf(userMessage))

        // Assert
        assertTrue(result is Failure)
        val failure = result as Failure
        assertEquals("Failed to call language model", failure.reason.message)
        assertEquals(exception, failure.reason.cause)
        verify(exactly = 1) {
            mockChatLanguageModel.chat(listOf(LangchainUserMessage("Hello")))
        }
    }

    @Test
    fun `call should handle AssistantMessage and SystemMessage correctly`() {
        // Arrange
        val assistantMsg = AssistantMessage("I can help you with that.")
        val systemMsg = SystemMessage("System initialized.")
        val mockResponse = mockk<ChatResponse>()

        every { mockResponse.aiMessage().text() } returns "Sure, let's proceed."
        every {
            mockChatLanguageModel.chat(
                listOf(
                    LangchainUserMessage("Hello"),
                    LangchainSystemMessage("System initialized."),
                    dev.langchain4j.data.message
                        .AiMessage("I can help you with that."),
                ),
            )
        } returns mockResponse

        val messages =
            listOf(
                UserMessage("Hello"),
                systemMsg,
                assistantMsg,
            )

        // Act
        val result = modelClient.call(messages)

        // Assert
        assertTrue(result is Success)
        val success = result as Success
        assertEquals(AssistantMessage("Sure, let's proceed."), success.value)
        verify(exactly = 1) {
            mockChatLanguageModel.chat(
                listOf(
                    LangchainUserMessage("Hello"),
                    LangchainSystemMessage("System initialized."),
                    dev.langchain4j.data.message
                        .AiMessage("I can help you with that."),
                ),
            )
        }
    }

    @Test
    fun `call should handle empty message list`() {
        // Arrange
        val messages = emptyList<ChatMessage>()

        // Assuming that the language model can handle empty input and returns a valid response
        val assistantResponse = "Hello! How can I assist you today?"
        val mockResponse = mockk<ChatResponse>()

        every { mockResponse.aiMessage().text() } returns assistantResponse
        every { mockChatLanguageModel.chat(emptyList()) } returns mockResponse

        // Act
        val result = modelClient.call(messages)

        // Assert
        assertTrue(result is Success)
        val success = result as Success
        assertEquals(AssistantMessage(assistantResponse), success.value)
        verify(exactly = 1) {
            mockChatLanguageModel.chat(emptyList())
        }
    }

    @Test
    fun `call should propagate AgentRoutingSpecResolverException when model generate throws it`() {
        // Arrange
        val userMessage = UserMessage("Hello")
        val exception = AgentRoutingSpecResolverException("Custom exception")

        every {
            mockChatLanguageModel.chat(
                listOf(LangchainUserMessage("Hello")),
            )
        } throws exception

        // Act
        val result = modelClient.call(listOf(userMessage))

        // Assert
        assertTrue(result is Failure)
        val failure = result as Failure
        assertEquals("Failed to call language model", failure.reason.message)
        assertEquals(exception, failure.reason.cause)
        verify(exactly = 1) {
            mockChatLanguageModel.chat(listOf(LangchainUserMessage("Hello")))
        }
    }
}

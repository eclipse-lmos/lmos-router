// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// Test class for Context
class ContextTest {
    private lateinit var chatMessage1: ChatMessage
    private lateinit var chatMessage2: ChatMessage
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        // Creating mock objects for ChatMessage
        chatMessage1 = mockk<ChatMessage>()
        chatMessage2 = mockk<ChatMessage>()

        // Setting up mock responses
        every { chatMessage1.content } returns "Hello"
        every { chatMessage2.content } returns "World"

        // Initializing the context with mocked chat messages
        context = Context(previousMessages = listOf(chatMessage1, chatMessage2))
    }

    @Test
    fun `test context initialization with previous messages`() {
        // Verifying the size of previous messages
        assertEquals(2, context.previousMessages.size)

        // Verifying the contents of previous messages
        assertEquals("Hello", context.previousMessages[0].content)
        assertEquals("World", context.previousMessages[1].content)
    }

    @Test
    fun `test context with empty previous messages`() {
        // Creating a new context with no previous messages
        val emptyContext = Context(previousMessages = emptyList())

        // Verifying the size of previous messages
        assertEquals(0, emptyContext.previousMessages.size)
    }

    @Test
    fun `test context with a single previous message`() {
        // Creating a new context with one previous message
        val singleMessageContext = Context(previousMessages = listOf(chatMessage1))

        // Verifying the size of previous messages
        assertEquals(1, singleMessageContext.previousMessages.size)

        // Verifying the content of the single previous message
        assertEquals("Hello", singleMessageContext.previousMessages[0].content)
    }
}

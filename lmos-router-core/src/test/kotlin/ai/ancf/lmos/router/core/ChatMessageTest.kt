// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChatMessageFactoryTest {
    private val factory = ChatMessageFactory()

    @Test
    fun `getChatMessage should return UserMessage when role is user`() {
        val content = "Hello, user!"
        val role = "user"
        val result = factory.getChatMessage(content, role)
        assertTrue(result is UserMessage)
        assertEquals(content, result.content)
    }

    @Test
    fun `getChatMessage should return SystemMessage when role is system`() {
        val content = "System update available."
        val role = "system"
        val result = factory.getChatMessage(content, role)
        assertTrue(result is SystemMessage)
        assertEquals(content, result.content)
    }

    @Test
    fun `getChatMessage should return AssistantMessage when role is assistant`() {
        val content = "How can I assist you?"
        val role = "assistant"
        val result = factory.getChatMessage(content, role)
        assertTrue(result is AssistantMessage)
        assertEquals(content, result.content)
    }

    @Test
    fun `getChatMessage should throw IllegalArgumentException when role is unknown`() {
        val content = "Unknown role test"
        val role = "unknown"
        val exception =
            assertThrows<IllegalArgumentException> {
                factory.getChatMessage(content, role)
            }
        assertEquals("Unknown message type", exception.message)
    }
}

class ChatMessageBuilderTest {
    @Test
    fun `build should return UserMessage when role is user`() {
        val content = "Hello, user!"
        val role = "user"

        val builder =
            ChatMessageBuilder()
                .content(content)
                .role(role)

        val result = builder.build()

        assertTrue(result is UserMessage)
        assertEquals(content, result.content)
    }

    @Test
    fun `build should return SystemMessage when role is system`() {
        val content = "System update available."
        val role = "system"

        val builder =
            ChatMessageBuilder()
                .content(content)
                .role(role)

        val result = builder.build()

        assertTrue(result is SystemMessage)
        assertEquals(content, result.content)
    }

    @Test
    fun `build should return AssistantMessage when role is assistant`() {
        val content = "How can I assist you?"
        val role = "assistant"

        val builder =
            ChatMessageBuilder()
                .content(content)
                .role(role)

        val result = builder.build()

        assertTrue(result is AssistantMessage)
        assertEquals(content, result.content)
    }

    @Test
    fun `build should throw IllegalArgumentException when role is unknown`() {
        val content = "Unknown role test"
        val role = "unknown"

        val builder =
            ChatMessageBuilder()
                .content(content)
                .role(role)

        val exception =
            assertThrows<IllegalArgumentException> {
                builder.build()
            }
        assertEquals("Unknown message type", exception.message)
    }
}

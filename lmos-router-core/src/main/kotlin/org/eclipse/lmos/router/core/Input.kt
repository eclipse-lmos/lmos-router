// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

/**
 * Represents a chat message.
 *
 * The [content] field contains the content of the message.
 */
sealed class ChatMessage {
    abstract val content: String
}

/**
 * Represents a user message.
 *
 * The [content] field contains the content of the message.
 */
data class UserMessage(
    override val content: String,
) : ChatMessage()

/**
 * Represents a system message.
 *
 * The [content] field contains the content of the message.
 */
data class SystemMessage(
    override val content: String,
) : ChatMessage()

/**
 * Represents an assistant message.
 *
 * The [content] field contains the content of the message.
 */
data class AssistantMessage(
    override val content: String,
) : ChatMessage()

/**
 * Factory for creating chat messages.
 */
class ChatMessageFactory {
    fun getChatMessage(
        content: String,
        role: String,
    ): ChatMessage =
        when (role) {
            "user" -> UserMessage(content)
            "system" -> SystemMessage(content)
            "assistant" -> AssistantMessage(content)
            else -> throw IllegalArgumentException("Unknown message type")
        }
}

/**
 * Represents a builder for creating chat messages.
 */
class ChatMessageBuilder {
    private var content: String = ""
    private var role: String = ""

    fun content(content: String) = apply { this.content = content }

    fun role(role: String) = apply { this.role = role }

    fun build(): ChatMessage = ChatMessageFactory().getChatMessage(content, role)
}

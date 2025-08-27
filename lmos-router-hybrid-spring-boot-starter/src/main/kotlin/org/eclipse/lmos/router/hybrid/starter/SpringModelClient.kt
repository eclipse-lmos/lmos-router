// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.llm.ModelClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt

/**
 * This is a model client that uses the Spring chat model to resolve agent routing specifications.
 *
 * @param chatModel The chat model.
 */
class SpringModelClient(
    private val chatModel: ChatModel,
) : ModelClient {
    /**
     * Calls the chat model with the given messages.
     *
     * @param messages The messages.
     * @return The result of the call.
     */
    override fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException> =
        try {
            val response =
                chatModel
                    .call(
                        Prompt(
                            messages.map {
                                when (it) {
                                    is UserMessage ->
                                        org.springframework.ai.chat.messages
                                            .UserMessage(it.content)
                                    is AssistantMessage ->
                                        org.springframework.ai.chat.messages
                                            .AssistantMessage(it.content)
                                    is SystemMessage ->
                                        org.springframework.ai.chat.messages
                                            .SystemMessage(it.content)
                                    else -> throw IllegalArgumentException("Unsupported message type: ${it::class.simpleName}")
                                }
                            },
                        ),
                    ).result.output.text
            Success(AssistantMessage(response))
        } catch (e: Exception) {
            Failure(AgentRoutingSpecResolverException(e.message ?: "An error occurred", e))
        }
}

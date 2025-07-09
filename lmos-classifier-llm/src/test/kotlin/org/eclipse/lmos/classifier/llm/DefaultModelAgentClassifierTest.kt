// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.HistoryMessage
import org.eclipse.lmos.classifier.core.HistoryMessageRole.*
import org.eclipse.lmos.classifier.core.InputContext
import org.eclipse.lmos.classifier.core.SystemContext
import org.junit.jupiter.api.Test

internal class DefaultModelAgentClassifierTest {
    private val chatModelMock = mockk<ChatModel>()
    private val systemPrompt = "This is the system prompt, listing some agents:\n{{agents}}"
    private val systemPromptAgents =
        """
        Agent 'weather-bot':
         - Provides weather forecasts

        Agent 'news-bot':
         - Provides latest news
        """.trimIndent()

    private val underTest = DefaultModelAgentClassifier(chatModelMock, systemPrompt)

    @Test
    fun `classify should return classification result with expected agentId`() {
        // given
        val request = modelClassificationRequest()

        val expectedAgentId = "weather-bot"
        val chatResponse = ChatResponse.builder().aiMessage(AiMessage((expectedAgentId))).build()
        val messagesSlot = slot<List<ChatMessage>>()
        every { chatModelMock.chat(capture(messagesSlot)) } returns chatResponse

        // when
        val classification = underTest.classify(request)

        // then
        assertThat(classification.agents).isEqualTo(listOf(expectedAgentId))
        assertThat(messagesSlot.captured).hasSize(4)

        assertThat(messagesSlot.captured[0]).isInstanceOf(SystemMessage::class.java)
        assertThat((messagesSlot.captured[0] as SystemMessage).text()).isEqualTo(
            systemPrompt.replace("{{agents}}", systemPromptAgents),
        )

        assertThat(messagesSlot.captured[1]).isInstanceOf(UserMessage::class.java)
        assertThat((messagesSlot.captured[1] as UserMessage).singleText()).isEqualTo("Hi")

        assertThat(messagesSlot.captured[2]).isInstanceOf(AiMessage::class.java)
        assertThat((messagesSlot.captured[2] as AiMessage).text()).isEqualTo("Hello, how can I help?")

        assertThat(messagesSlot.captured[3]).isInstanceOf(UserMessage::class.java)
        assertThat((messagesSlot.captured[3] as UserMessage).singleText()).isEqualTo("What's the weather today?")
    }

    @Test
    fun `classify should return empty list if no agent is found`() {
        // given
        val request = modelClassificationRequest()

        val expectedAgentId = "null"
        val chatResponse = ChatResponse.builder().aiMessage(AiMessage((expectedAgentId))).build()
        val messagesSlot = slot<List<ChatMessage>>()
        every { chatModelMock.chat(capture(messagesSlot)) } returns chatResponse

        // when
        val classification = underTest.classify(request)

        // then
        assertThat(classification.agents).isEmpty()
    }

    private fun modelClassificationRequest() =
        ClassificationRequest(
            inputContext =
                InputContext(
                    userMessage = "What's the weather today?",
                    historyMessages =
                        listOf(
                            HistoryMessage(role = USER, content = "Hi"),
                            HistoryMessage(role = ASSISTANT, content = "Hello, how can I help?"),
                        ),
                    agents =
                        listOf(
                            Agent(
                                id = "weather-bot",
                                capabilities = listOf(Capability(id = "weather", description = "Provides weather forecasts")),
                            ),
                            Agent(
                                id = "news-bot",
                                capabilities = listOf(Capability(id = "news", description = "Provides latest news")),
                            ),
                        ),
                ),
            systemContext =
                SystemContext(
                    tenantId = "tenant1",
                    channelId = "channel1",
                ),
        )
}

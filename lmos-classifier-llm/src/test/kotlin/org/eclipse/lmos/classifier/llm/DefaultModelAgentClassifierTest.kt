// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.*
import org.eclipse.lmos.classifier.core.HistoryMessageRole.*
import org.junit.jupiter.api.Test

internal class DefaultModelAgentClassifierTest {
    private val chatModelMock = mockk<ChatModel>()
    private val systemPrompt = "This is the system prompt, listing some agents:\n{{agents}}"
    private val systemPromptAgents =
        """
        [{"agentId":"weather-bot","descriptions":["Provides weather forecasts"]},{"agentId":"news-bot","descriptions":["Provides latest news"]}]
        """.trimIndent()

    private val underTest = DefaultModelAgentClassifier(chatModelMock, systemPrompt)

    @Test
    fun `classify should return classification result with expected agent`() {
        // given
        val request = modelClassificationRequest()

        val expectedAgentId = jacksonObjectMapper().writeValueAsString(ClassifiedAgentResult("weather-bot", "customer wants weather info"))
        val expectedAgent = ClassifiedAgent("weather-bot", "weather-bot-name", "weather-bot-address")
        val chatResponse = ChatResponse.builder().aiMessage(AiMessage((expectedAgentId))).build()
        val messagesSlot = slot<ChatRequest>()
        every { chatModelMock.chat(capture(messagesSlot)) } returns chatResponse

        // when
        val classification = underTest.classify(request)

        // then
        assertThat(classification.agents).isEqualTo(listOf(expectedAgent))
        assertThat(messagesSlot.captured.messages()).hasSize(4)

        assertThat(messagesSlot.captured.messages()[0]).isInstanceOf(SystemMessage::class.java)
        assertThat((messagesSlot.captured.messages()[0] as SystemMessage).text()).isEqualTo(
            systemPrompt.replace("{{agents}}", systemPromptAgents),
        )

        assertThat(messagesSlot.captured.messages()[1]).isInstanceOf(UserMessage::class.java)
        assertThat((messagesSlot.captured.messages()[1] as UserMessage).singleText()).isEqualTo("Hi")

        assertThat(messagesSlot.captured.messages()[2]).isInstanceOf(AiMessage::class.java)
        assertThat((messagesSlot.captured.messages()[2] as AiMessage).text()).isEqualTo("Hello, how can I help?")

        assertThat(messagesSlot.captured.messages()[3]).isInstanceOf(UserMessage::class.java)
        assertThat((messagesSlot.captured.messages()[3] as UserMessage).singleText()).isEqualTo("What's the weather today?")
    }

    @Test
    fun `classify should return empty list if no agent is found`() {
        // given
        val request = modelClassificationRequest()

        val expectedAgentId = jacksonObjectMapper().writeValueAsString(ClassifiedAgentResult(null, "no suitable agent found"))
        val chatResponse = ChatResponse.builder().aiMessage(AiMessage((expectedAgentId))).build()
        val messagesSlot = slot<ChatRequest>()
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
                                name = "weather-bot-name",
                                address = "weather-bot-address",
                                capabilities = listOf(Capability(id = "weather", description = "Provides weather forecasts")),
                            ),
                            Agent(
                                id = "news-bot",
                                name = "weather-bot-name",
                                address = "weather-bot-address",
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

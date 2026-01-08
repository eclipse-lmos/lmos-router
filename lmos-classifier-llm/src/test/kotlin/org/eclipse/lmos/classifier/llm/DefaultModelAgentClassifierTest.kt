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
import dev.langchain4j.model.output.TokenUsage
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.*
import org.eclipse.lmos.classifier.core.HistoryMessageRole.*
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.eclipse.lmos.classifier.core.tracing.NoopClassifierTracer
import org.junit.jupiter.api.Test

internal class DefaultModelAgentClassifierTest {
    private val chatModelMock = mockk<ChatModel>()
    private val systemPromptTemplate =
        "This is the system prompt, listing some agents: @foreach{agent : agents}@{agent.id}@end{', '}"
    private val expectedSystemPrompt =
        "This is the system prompt, listing some agents: weather-bot, news-bot"
    private val agentProvider =
        object : AgentProvider {
            override suspend fun provide(request: ClassificationRequest) =
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
                )
        }

    private val underTest =
        DefaultModelAgentClassifier(
            chatModelMock,
            systemPromptTemplate,
            MvelSystemPromptRenderer(),
            DefaultAgentAggregator(listOf(agentProvider)),
            NoopClassifierTracer(),
        )

    @Test
    fun `classify should return classification result with expected agent`(): Unit =
        runBlocking {
            // given
            val request = modelClassificationRequest()

            val expectedAgentId =
                jacksonObjectMapper().writeValueAsString(
                    ClassifiedAgentResult("customer wants weather info", "weather-bot"),
                )
            val expectedClassifiedAgent = ClassifiedAgent("weather-bot", "weather-bot-name", "weather-bot-address")
            val expectedCandidateAgents = agentProvider.provide(request)
            val chatResponse =
                ChatResponse
                    .builder()
                    .modelName("MyModel")
                    .tokenUsage(TokenUsage(1, 2, 3))
                    .aiMessage(AiMessage((expectedAgentId)))
                    .build()
            val messagesSlot = slot<ChatRequest>()
            every { chatModelMock.chat(capture(messagesSlot)) } returns chatResponse

            // when
            val classification = underTest.classify(request)

            // then
            assertThat(classification.classifiedAgents).isEqualTo(listOf(expectedClassifiedAgent))
            assertThat(classification.candidateAgents).isEqualTo(expectedCandidateAgents)
            assertThat(classification.reason).isEqualTo("customer wants weather info")
            assertThat(messagesSlot.captured.messages()).hasSize(4)

            assertThat(messagesSlot.captured.messages()[0]).isInstanceOf(SystemMessage::class.java)
            assertThat((messagesSlot.captured.messages()[0] as SystemMessage).text()).isEqualTo(expectedSystemPrompt)

            assertThat(messagesSlot.captured.messages()[1]).isInstanceOf(UserMessage::class.java)
            assertThat((messagesSlot.captured.messages()[1] as UserMessage).singleText()).isEqualTo("Hi")

            assertThat(messagesSlot.captured.messages()[2]).isInstanceOf(AiMessage::class.java)
            assertThat((messagesSlot.captured.messages()[2] as AiMessage).text()).isEqualTo("Hello, how can I help?")

            assertThat(messagesSlot.captured.messages()[3]).isInstanceOf(UserMessage::class.java)
            assertThat((messagesSlot.captured.messages()[3] as UserMessage).singleText()).isEqualTo("What's the weather today?")
        }

    @Test
    fun `classify should return empty list if no agent is found`(): Unit =
        runBlocking {
            // given
            val request = modelClassificationRequest()

            val expectedAgentId = jacksonObjectMapper().writeValueAsString(ClassifiedAgentResult("no suitable agent found", null))
            val expectedCandidateAgents = agentProvider.provide(request)
            val chatResponse =
                ChatResponse
                    .builder()
                    .modelName("MyModel")
                    .tokenUsage(TokenUsage(1, 2, 3))
                    .aiMessage(AiMessage((expectedAgentId)))
                    .build()
            val messagesSlot = slot<ChatRequest>()
            every { chatModelMock.chat(capture(messagesSlot)) } returns chatResponse

            // when
            val classification = underTest.classify(request)

            // then
            assertThat(classification.classifiedAgents).isEmpty()
            assertThat(classification.candidateAgents).isEqualTo(expectedCandidateAgents)
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
                ),
            systemContext =
                SystemContext(
                    tenantId = "tenant1",
                    channelId = "channel1",
                ),
        )
}

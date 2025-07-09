// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.hybrid

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.*
import org.eclipse.lmos.classifier.core.HistoryMessageRole.*
import org.eclipse.lmos.classifier.core.llm.*
import org.eclipse.lmos.classifier.core.semantic.EmbeddingAgentClassifier
import org.junit.jupiter.api.Test

class FastTrackAgentClassifierTest {
    private val embeddingClassifierMock = mockk<EmbeddingAgentClassifier>()
    private val modelClassifierMock = mockk<ModelAgentClassifier>()
    private val underTest: FastTrackAgentClassifier = FastTrackAgentClassifier(embeddingClassifierMock, modelClassifierMock)

    private val topScoredEmbeddings =
        listOf(
            Agent(
                id = "agent-1",
                capabilities =
                    listOf(
                        Capability(
                            id = "cap-1",
                            description = "Handles subscription cancellation",
                            examples = listOf("cancel my plan", "end membership"),
                        ),
                    ),
            ),
        )

    private val request =
        ClassificationRequest(
            inputContext =
                InputContext(
                    userMessage = "How do I cancel my subscription?",
                    historyMessages =
                        listOf(
                            HistoryMessage(role = USER, content = "I want to change my plan."),
                        ),
                ),
            systemContext =
                SystemContext(
                    tenantId = "my-tenant",
                    channelId = "my-channel",
                ),
        )

    @Test
    fun `classify should return classification result from vector-search`() {
        // given
        every { embeddingClassifierMock.classify(request) } returns
            ClassificationResult(
                agents = listOf("agent-1"),
                topRankedEmbeddings = topScoredEmbeddings,
            )

        // when
        val result = underTest.classify(request)

        // then
        assertThat(result.agents).isEqualTo(listOf("agent-1"))
        assertThat(result.topRankedEmbeddings).isEqualTo(topScoredEmbeddings)
    }

    @Test
    fun `classify should return classification result from model-search if vector-search returns no agent`() {
        // given
        every { embeddingClassifierMock.classify(request) } returns
            ClassificationResult(
                agents = emptyList(),
                topRankedEmbeddings = topScoredEmbeddings,
            )

        every { modelClassifierMock.classify(any()) } answers {
            val req = it.invocation.args[0] as ClassificationRequest
            assertThat(req.inputContext.userMessage).isEqualTo(request.inputContext.userMessage)
            assertThat(req.inputContext.historyMessages).isEqualTo(request.inputContext.historyMessages)
            assertThat(req.inputContext.agents).isEqualTo(topScoredEmbeddings)
            ClassificationResult(agents = listOf("agent-1"), topScoredEmbeddings)
        }

        // when
        val result = underTest.classify(request)

        // then
        assertThat(result.agents).isEqualTo(listOf("agent-1"))
    }
}

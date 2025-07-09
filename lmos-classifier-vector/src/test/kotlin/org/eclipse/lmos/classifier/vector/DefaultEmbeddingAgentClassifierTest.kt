// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.InputContext
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
import org.junit.jupiter.api.Test

class DefaultEmbeddingAgentClassifierTest {
    private val embeddingRetrieverMock = mockk<EmbeddingRetriever>()
    private val embeddingRankerMock = mockk<EmbeddingRanker>()

    private val underTest = DefaultEmbeddingAgentClassifier(embeddingRetrieverMock, embeddingRankerMock, SimpleConcatenationRephraser(10))

    private val request =
        ClassificationRequest(
            inputContext =
                InputContext(
                    userMessage = "How do I reset my password?",
                ),
            systemContext =
                SystemContext(
                    tenantId = "my-tenant",
                    channelId = "my-channel",
                ),
        )

    private val embedding =
        Embedding(
            example = "Reset password",
            score = 1.0,
            agentId = "agent-1",
            capabilityId = "cap-1",
            capabilityDescription = "Resetting passwords",
        )

    @Test
    fun `classify should return classification result with expected agentId`() {
        // given
        every { embeddingRetrieverMock.retrieve(request.systemContext, request.inputContext.userMessage) } returns
            listOf(embedding)
        every { embeddingRankerMock.findMostQualifiedAgents(listOf(embedding)) } returns listOf("agent-1")

        // when
        val result = underTest.classify(request)

        // then
        assertThat(result.agents).isEqualTo(listOf("agent-1"))
        assertThat(result.topRankedEmbeddings).hasSize(1)
        assertThat(result.topRankedEmbeddings[0].id).isEqualTo("agent-1")
    }

    @Test
    fun `classify should return empty list if no agent is found`() {
        // given
        every { embeddingRetrieverMock.retrieve(request.systemContext, request.inputContext.userMessage) } returns
            listOf(embedding)
        every { embeddingRankerMock.findMostQualifiedAgents(listOf(embedding)) } returns emptyList()

        // when
        val result = underTest.classify(request)

        // then
        assertThat(result.agents).isEmpty()
        assertThat(result.topRankedEmbeddings).hasSize(1)
        assertThat(result.topRankedEmbeddings[0].id).isEqualTo("agent-1")
    }

    @Test
    fun `classify can handle empty list of embeddings`() {
        // given
        every { embeddingRetrieverMock.retrieve(request.systemContext, request.inputContext.userMessage) } returns emptyList()
        every { embeddingRankerMock.findMostQualifiedAgents(emptyList()) } returns emptyList()

        // when
        val result = underTest.classify(request)

        // then
        assertThat(result.agents).isEmpty()
        assertThat(result.topRankedEmbeddings).isEmpty()
    }
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.ranker

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.semantic.Embedding
import org.junit.jupiter.api.Test

internal class SingleAgentEmbeddingRankerTest {
    private val thresholds =
        EmbeddingRankingThreshold(
            minScore = 5.0,
            minDistance = 4.0,
            minMeanScore = 0.8,
            minRelDistance = 0.3,
        )

    private val underTest = SingleAgentEmbeddingRanker(thresholds)

    @Test
    fun `findQualifiedAgent returns no agent for empty embeddings`() {
        // given
        val emptyEmbeddings = emptyList<Embedding>()
        // when
        val result = underTest.findMostQualifiedAgents(emptyEmbeddings)
        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `findQualifiedAgent returns agent directly when only one embedding is given`() {
        // given
        val embedding =
            Embedding(
                example = "sample",
                score = 1.23,
                agentId = "agent-1",
                agentName = "agent-1-name",
                agentAddress = "agent-1-address",
                capabilityId = "cap-1",
                capabilityDescription = "desc",
            )
        // when
        val result = underTest.findMostQualifiedAgents(listOf(embedding))
        // then
        assertThat(result).isEqualTo(listOf("agent-1"))
    }

    @Test
    fun `findQualifiedAgent returns most qualified agent when thresholds met`() {
        // given
        val embeddings =
            listOf(
                Embedding("a1", 3.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("a2", 3.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("b1", 1.0, "B", "B-Agent-Name", "B-Agent-Address", "c2", "desc"),
            )
        // when
        val result = underTest.findMostQualifiedAgents(embeddings)
        // then; A, because weight=6.0, distance=5.0, mean=3.0, rel=2.5
        assertThat(result).isEqualTo(listOf("A"))
    }

    @Test
    fun `findQualifiedAgent returns no agent when minimum score weight is below threshold`() {
        // given
        val thresholds = EmbeddingRankingThreshold(minScore = 10.0)
        val ranker = SingleAgentEmbeddingRanker(thresholds)
        val embeddings =
            listOf(
                Embedding("a1", 3.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("a2", 3.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("b1", 1.0, "B", "B-Agent-Name", "B-Agent-Address", "c2", "desc"),
            )

        // when
        val result = ranker.findMostQualifiedAgents(embeddings)
        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `findQualifiedAgent returns no agent when minimum distance to neighbour agent is below threshold`() {
        // given
        val thresholds = EmbeddingRankingThreshold(minDistance = 5.0)
        val ranker = SingleAgentEmbeddingRanker(thresholds)
        val embeddings =
            listOf(
                Embedding("a1", 6.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("b1", 2.5, "B", "B-Agent-Name", "B-Agent-Address", "c2", "desc"),
            )
        // when
        val result = ranker.findMostQualifiedAgents(embeddings)
        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `findQualifiedAgent returns no agent when mean score is below threshold`() {
        // given
        val thresholds = EmbeddingRankingThreshold(minMeanScore = 3.0)
        val ranker = SingleAgentEmbeddingRanker(thresholds)
        val embeddings =
            listOf(
                Embedding("a1", 2.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("a2", 0.5, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("b1", 1.0, "B", "B-Agent-Name", "B-Agent-Address", "c2", "desc"),
            )
        // when
        val result = ranker.findMostQualifiedAgents(embeddings)
        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `findQualifiedAgent returns no agent when relative distance is below threshold`() {
        // given
        val thresholds = EmbeddingRankingThreshold(minRelDistance = 1.0)
        val ranker = SingleAgentEmbeddingRanker(thresholds)
        val embeddings =
            listOf(
                Embedding("a1", 4.0, "A", "A-Agent-Name", "A-Agent-Address", "c1", "desc"),
                Embedding("b1", 3.2, "B", "B-Agent-Name", "B-Agent-Address", "c2", "desc"),
            )
        // when
        val result = ranker.findMostQualifiedAgents(embeddings)
        // then
        assertThat(result).isEmpty()
    }
}

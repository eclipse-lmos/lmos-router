// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.ranker

import org.eclipse.lmos.classifier.core.semantic.Embedding
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRanker
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Ranks a set of embeddings to determine the most qualified single agent
 * based on aggregated similarity scores and configurable threshold criteria.
 *
 * For each agent, all embedding scores are accumulated and averaged.
 * The top-ranked agent is only selected if:
 * - the total and mean scores exceed the configured threshold minimums, and
 * - in case a second candidate exists, the absolute and relative score differences
 *   compared to that candidate also exceed the threshold minimums.
 *
 * If any threshold check fails, no agent is returned.
 *
 * @param thresholds Configuration object defining the minimum score and distance criteria.
 */
class SingleAgentEmbeddingRanker(
    private val thresholds: EmbeddingRankingThreshold,
) : EmbeddingRanker {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun findMostQualifiedAgents(
        embeddings: List<Embedding>,
        maxResults: Int,
    ): List<String> {
        if (embeddings.isEmpty()) return emptyList()
        if (embeddings.size == 1) return listOf(embeddings[0].agentId)

        // Aggregate metrics per agent: accumulate total score and number of embeddings, sorted by total score
        val agentMetricsSortedByScore =
            embeddings
                .groupBy { it.agentId }
                .mapValues { (_, items) ->
                    AgentMetric(
                        agentId = items[0].agentId,
                        totalScore = items.sumOf { it.score },
                        hitCount = items.size,
                    )
                }.values
                .sortedByDescending { it.totalScore }

        // Calculate score, mean score, distance and relative distance
        val firstAgent = agentMetricsSortedByScore[0]
        val score = firstAgent.totalScore
        val meanScore = score / firstAgent.hitCount

        val secondAgentExists = agentMetricsSortedByScore.size > 1
        val distance = if (secondAgentExists) score - agentMetricsSortedByScore[1].totalScore else -1.0
        val relDistance = if (secondAgentExists) distance / firstAgent.hitCount else -1.0

        // Check thresholds
        val passesThresholds =
            if (secondAgentExists) {
                distance >= thresholds.minDistance &&
                    relDistance >= thresholds.minRelDistance &&
                    score >= thresholds.minScore &&
                    meanScore >= thresholds.minMeanScore
            } else {
                score >= thresholds.minScore &&
                    meanScore >= thresholds.minMeanScore
            }

        logger.logRankingEvaluation(
            EmbeddingRankingEvaluation(score, meanScore, distance, relDistance),
            firstAgent.agentId,
            passesThresholds,
        )

        return if (passesThresholds) listOf(firstAgent.agentId) else emptyList()
    }

    private fun Logger.logRankingEvaluation(
        embeddingRankingEvaluation: EmbeddingRankingEvaluation,
        topAgent: String,
        rankingMatch: Boolean,
    ) {
        this
            .atInfo()
            .addKeyValue("classifier-embedding-ranking-thresholds", thresholds)
            .addKeyValue("classifier-embedding-ranking-evaluation", embeddingRankingEvaluation)
            .addKeyValue("classifier-embedding-ranking-top-agent", topAgent)
            .addKeyValue("classifier-embedding-ranking-thresholds-matched", rankingMatch)
            .addKeyValue("event", "CLASSIFICATION_VECTOR_METRICS")
            .log("Executed embedding ranker. Thresholds matched: {}", rankingMatch)
    }
}

data class AgentMetric(
    val agentId: String,
    var totalScore: Double = 0.0,
    var hitCount: Int = 0,
)

data class EmbeddingRankingThreshold(
    val minScore: Double = 5.0,
    val minMeanScore: Double = 0.8,
    val minDistance: Double = 4.0,
    val minRelDistance: Double = 0.3,
)

data class EmbeddingRankingEvaluation(
    val score: Double,
    val meanScore: Double,
    val distance: Double? = null,
    val relDistance: Double? = null,
)

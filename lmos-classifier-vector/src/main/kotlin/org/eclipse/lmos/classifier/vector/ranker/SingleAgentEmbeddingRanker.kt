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

        // Aggregate metrics per agent: accumulate total score and number of embeddings
        val agentMetrics = mutableMapOf<String, AgentMetric>()
        embeddings.forEach {
            val metric = agentMetrics.getOrPut(it.agentId) { AgentMetric() }
            metric.totalScore += it.score
            metric.hitCount++
        }

        // Rank agents by their total score
        val agentMetricsSortedByScore = agentMetrics.entries.sortedByDescending { it.value.totalScore }

        // First threshold check: overall score and mean score must exceed minimums
        val firstAgent = agentMetricsSortedByScore[0]
        val score = firstAgent.value.totalScore
        val meanScore = score / firstAgent.value.hitCount
        if (score < thresholds.minScore || meanScore < thresholds.minMeanScore) {
            logger.logRankingEvaluation(EmbeddingRankingEvaluation(score, meanScore), false)
            return emptyList()
        }

        // Second threshold check: if a neighboring exists, distance and relative distance must exceed minimums
        var distance: Double? = null
        var relDistance: Double? = null
        if (agentMetricsSortedByScore.size > 1) {
            val secondAgent = agentMetricsSortedByScore[1]
            distance = score - secondAgent.value.totalScore
            relDistance = distance / firstAgent.value.hitCount
            if (distance < thresholds.minDistance || relDistance < thresholds.minRelDistance) {
                logger.logRankingEvaluation(EmbeddingRankingEvaluation(score, meanScore, distance, relDistance), false)
                return emptyList()
            }
        }

        // All thresholds passed, return the best-ranked agent
        logger.logRankingEvaluation(EmbeddingRankingEvaluation(score, meanScore, distance, relDistance), true)
        return listOf(firstAgent.key)
    }

    private fun Logger.logRankingEvaluation(
        embeddingRankingEvaluation: EmbeddingRankingEvaluation,
        rankingMatch: Boolean,
    ) {
        this
            .atInfo()
            .addKeyValue("classifier-embedding-ranking-thresholds", thresholds)
            .addKeyValue("classifier-embedding-ranking-evaluation", embeddingRankingEvaluation)
            .addKeyValue("classifier-embedding-thresholds-match", rankingMatch)
            .addKeyValue("event", "CLASSIFICATION_VECTOR_METRICS")
            .log("Executed embedding ranker. Thresholds matched: {}", rankingMatch)
    }
}

data class AgentMetric(
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

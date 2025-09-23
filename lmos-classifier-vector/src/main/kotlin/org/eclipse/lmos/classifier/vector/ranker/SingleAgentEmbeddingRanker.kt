// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.ranker

import org.eclipse.lmos.classifier.core.semantic.Embedding
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRanker
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Ranks a set of embeddings to identify the most qualified single agent
 * based on similarity scoring and configurable ranking thresholds.
 *
 * The agent with the highest cumulative score is only selected if:
 * - the score difference to the second-best agent exceeds a minimum distance,
 * - the total and mean scores exceed predefined thresholds,
 * - and the relative score difference is sufficiently large.
 *
 * @param thresholds Configuration defining the score-based selection criteria.
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

        // Create ranking
        val ranking = mutableMapOf<String, Pair<Double, Int>>() // <agent, (scoreSum, hitCount)>
        embeddings.forEach {
            val (scoreSum, hitCount) = ranking.getOrDefault(it.agentId, Pair(0.0, 0))
            ranking[it.agentId] = Pair(scoreSum + it.score, hitCount + 1)
        }

        // Sort ranking descending based on scoreSum
        val sortedRanking =
            ranking.entries
                .sortedByDescending { it.value.first }
        if (sortedRanking.size == 1) return listOf(sortedRanking[0].key)

        val firstRankedAgent = sortedRanking[0]
        val secondRankedAgent = sortedRanking[1]

        val score = firstRankedAgent.value.first
        val distance = score - secondRankedAgent.value.first
        val meanScore = score / firstRankedAgent.value.second
        val relDistance = distance / firstRankedAgent.value.second

        val rankingEvaluation = EmbeddingRankingEvaluation(score, distance, meanScore, relDistance)
        return if (
            rankingEvaluation.score >= thresholds.minScore &&
            rankingEvaluation.distance >= thresholds.minDistance &&
            rankingEvaluation.meanScore >= thresholds.minMeanScore &&
            rankingEvaluation.relDistance >= thresholds.minRelDistance
        ) {
            logger.logRankingEvaluation(rankingEvaluation, true)
            listOf(firstRankedAgent.key)
        } else {
            logger.logRankingEvaluation(rankingEvaluation, false)
            emptyList()
        }
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

data class EmbeddingRankingThreshold(
    val minScore: Double = 5.0,
    val minDistance: Double = 4.0,
    val minMeanScore: Double = 0.8,
    val minRelDistance: Double = 0.3,
)

data class EmbeddingRankingEvaluation(
    val score: Double,
    val distance: Double,
    val meanScore: Double,
    val relDistance: Double,
)

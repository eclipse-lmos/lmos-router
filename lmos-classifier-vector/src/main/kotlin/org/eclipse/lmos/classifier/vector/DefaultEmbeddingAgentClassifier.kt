// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector

import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.classifier.core.rephrase.Rephraser
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.classifier.vector.ranker.SingleAgentEmbeddingRanker
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
import org.eclipse.lmos.classifier.vector.utils.convertEmbeddingsToAgents
import org.slf4j.LoggerFactory

class DefaultEmbeddingAgentClassifier(
    private val embeddingRetriever: EmbeddingRetriever,
    private val embeddingRanker: EmbeddingRanker,
    private var rephraser: Rephraser,
) : EmbeddingAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val rephrasedMessage = rephraser.rephrase(request.inputContext)
        val embeddings = embeddingRetriever.retrieve(request.systemContext, rephrasedMessage)
        val qualifiedAgents = embeddingRanker.findMostQualifiedAgents(embeddings)
        logger.info(
            "[${javaClass.simpleName}] Classified agent '${qualifiedAgents.firstOrNull()}' " +
                "for rephrased message '$rephrasedMessage', based on embeddings $embeddings.",
        )
        return ClassificationResult(
            qualifiedAgents.firstOrNull()?.let { listOf(it) } ?: emptyList(),
            embeddings.convertEmbeddingsToAgents(),
        )
    }

    companion object {
        fun builder(): EmbeddingAgentClassifierBuilder = EmbeddingAgentClassifierBuilder()
    }
}

class EmbeddingAgentClassifierBuilder {
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var embeddingRankingThreshold: EmbeddingRankingThreshold = EmbeddingRankingThreshold()
    private var embeddingRanker: EmbeddingRanker = SingleAgentEmbeddingRanker(embeddingRankingThreshold)
    private var rephraser: Rephraser = SimpleConcatenationRephraser(15)

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) =
        apply {
            this.embeddingRetriever = embeddingRetriever
        }

    fun withEmbeddingRanker(embeddingRanker: EmbeddingRanker) =
        apply {
            this.embeddingRanker = embeddingRanker
        }

    fun withRephraser(rephraser: Rephraser) =
        apply {
            this.rephraser = rephraser
        }

    fun build(): DefaultEmbeddingAgentClassifier {
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")
        return DefaultEmbeddingAgentClassifier(embeddingRetriever!!, embeddingRanker, rephraser)
    }
}

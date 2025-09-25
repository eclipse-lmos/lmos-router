// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector

import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.classifier.core.ClassifiedAgent
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.classifier.vector.ranker.SingleAgentEmbeddingRanker
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
import org.eclipse.lmos.classifier.vector.utils.convertEmbeddingsToAgents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultEmbeddingAgentClassifier(
    private val embeddingRetriever: EmbeddingRetriever,
    private val embeddingRanker: EmbeddingRanker,
    private var queryRephraser: QueryRephraser,
) : EmbeddingAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val rephrasedMessage = queryRephraser.rephrase(request.inputContext)
        val embeddings = embeddingRetriever.retrieve(request.systemContext, rephrasedMessage)
        val qualifiedAgentsIds = embeddingRanker.findMostQualifiedAgents(embeddings)
        val agent =
            qualifiedAgentsIds
                .firstOrNull()
                ?.let { agentId ->
                    val embedding = embeddings.find { it.agentId == agentId }
                    embedding?.let { ClassifiedAgent(it.agentId, it.agentName, it.agentAddress) }
                }
        val classificationResult = ClassificationResult(listOfNotNull(agent), embeddings.convertEmbeddingsToAgents())
        logger.logClassificationResult(request, classificationResult, rephrasedMessage, embeddings)
        return classificationResult
    }

    private fun Logger.logClassificationResult(
        request: ClassificationRequest,
        result: ClassificationResult,
        searchQuery: String,
        searchResult: List<Embedding>,
    ) {
        val classifiedAgentId = result.agents.firstOrNull()?.id ?: "none"
        this
            .atInfo()
            .addKeyValue("classifier-type", "Vector")
            .addKeyValue("classifier-user-message", request.inputContext.userMessage)
            .addKeyValue("classifier-embedding-search-query", searchQuery)
            .addKeyValue("classifier-embedding-search-result", searchResult)
            .addKeyValue("classifier-selected-agent", classifiedAgentId)
            .addKeyValue("event", "CLASSIFICATION_VECTOR_DONE")
            .log(
                "Executed classification using the vector search. Query: '{}', classified agent: '{}'.",
                request.inputContext.userMessage,
                classifiedAgentId,
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
    private var queryRephraser: QueryRephraser = SimpleConcatenationRephraser(10)

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) =
        apply {
            this.embeddingRetriever = embeddingRetriever
        }

    fun withEmbeddingRanker(embeddingRanker: EmbeddingRanker) =
        apply {
            this.embeddingRanker = embeddingRanker
        }

    fun withQueryRephraser(queryRephraser: QueryRephraser) =
        apply {
            this.queryRephraser = queryRephraser
        }

    fun build(): DefaultEmbeddingAgentClassifier {
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")
        return DefaultEmbeddingAgentClassifier(embeddingRetriever!!, embeddingRanker, queryRephraser)
    }
}

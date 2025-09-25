// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.hybrid

import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.classifier.core.InputContext
import org.eclipse.lmos.classifier.core.hybrid.HybridAgentClassifier
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.llm.DefaultModelAgentClassifier
import org.eclipse.lmos.classifier.llm.defaultSystemPrompt
import org.eclipse.lmos.classifier.vector.DefaultEmbeddingAgentClassifier
import org.eclipse.lmos.classifier.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.classifier.vector.ranker.SingleAgentEmbeddingRanker
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A hybrid agent classifier that combines [EmbeddingAgentClassifier] and [ModelAgentClassifier] for classification.
 *
 * It first attempts to identify the most appropriate agent using an [EmbeddingAgentClassifier].
 * If no suitable match is found, the classification falls back to a [ModelAgentClassifier], which selects
 * an agent from the set of candidates returned by the semantic search.
 *
 * This approach ensures fast matching when a confident semantic hit exists, while preserving flexibility
 * through the generative capabilities of a language model when needed.
 */
class FastTrackAgentClassifier(
    private val embeddingAgentClassifier: EmbeddingAgentClassifier,
    private val modelAgentClassifier: ModelAgentClassifier,
) : HybridAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val embeddingClassification = embeddingAgentClassifier.classify(request)
        if (embeddingClassification.agents.isEmpty()) {
            val modelClassification =
                modelAgentClassifier.classify(
                    ClassificationRequest(
                        InputContext(
                            request.inputContext.userMessage,
                            request.inputContext.historyMessages,
                            embeddingClassification.topRankedEmbeddings,
                        ),
                        request.systemContext,
                    ),
                )
            logger.logClassificationResult(request, modelClassification, false)
            return ClassificationResult(modelClassification.agents, embeddingClassification.topRankedEmbeddings)
        }

        logger.logClassificationResult(request, embeddingClassification, true)
        return ClassificationResult(embeddingClassification.agents, embeddingClassification.topRankedEmbeddings)
    }

    private fun Logger.logClassificationResult(
        request: ClassificationRequest,
        result: ClassificationResult,
        isFastTrack: Boolean,
    ) {
        val classifiedAgentId = result.agents.firstOrNull()?.id ?: "none"
        this
            .atInfo()
            .addKeyValue("classifier-type", "Hybrid-Fasttrack")
            .addKeyValue("classifier-user-message", request.inputContext.userMessage)
            .addKeyValue("classifier-is-fasttrack", isFastTrack)
            .addKeyValue("classifier-selected-agent", classifiedAgentId)
            .addKeyValue("event", "CLASSIFICATION_FASTTRACK_DONE")
            .log(
                "Executed classification using the hybrid fast-track approach. Query: '{}', classified agent: '{}'.",
                request.inputContext.userMessage,
                classifiedAgentId,
            )
    }

    companion object {
        fun builder(): FastTrackAgentClassifierBuilder = FastTrackAgentClassifierBuilder()
    }
}

class FastTrackAgentClassifierBuilder {
    private var model: ChatModel? = null
    private var systemPrompt: String? = null
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var embeddingRanker: EmbeddingRanker = SingleAgentEmbeddingRanker(EmbeddingRankingThreshold())
    private var queryRephraser: QueryRephraser = SimpleConcatenationRephraser(10)

    fun withChatModel(model: ChatModel) =
        apply {
            this.model = model
        }

    fun withSystemPrompt(systemPrompt: String) =
        apply {
            this.systemPrompt = systemPrompt
        }

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

    fun build(): FastTrackAgentClassifier {
        if (model == null) throw IllegalStateException("ChatModel must be set")
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")
        if (systemPrompt.isNullOrEmpty()) systemPrompt = defaultSystemPrompt()

        val embeddingClassifier =
            DefaultEmbeddingAgentClassifier
                .builder()
                .withEmbeddingRetriever(embeddingRetriever!!)
                .withEmbeddingRanker(embeddingRanker)
                .withQueryRephraser(queryRephraser)
                .build()

        val modelClassifier =
            DefaultModelAgentClassifier
                .builder()
                .withChatModel(model!!)
                .withSystemPrompt(systemPrompt!!)
                .build()

        return FastTrackAgentClassifier(embeddingClassifier, modelClassifier)
    }
}

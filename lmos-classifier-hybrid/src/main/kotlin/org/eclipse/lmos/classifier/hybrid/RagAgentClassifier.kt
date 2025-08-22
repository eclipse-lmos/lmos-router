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
import org.eclipse.lmos.classifier.core.rephrase.Rephraser
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.llm.DefaultModelAgentClassifier
import org.eclipse.lmos.classifier.llm.defaultSystemPrompt
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
import org.eclipse.lmos.classifier.vector.utils.convertEmbeddingsToAgents
import org.slf4j.LoggerFactory

/**
 * A hybrid agent classifier based on the Retrieval-Augmented Generation (RAG).
 *
 * It first performs semantic retrieval to collect a set of candidate agents based on the user input.
 * These retrieved agents are then passed to a language model, which selects the most appropriate agent
 * from this filtered set.
 */
class RagAgentClassifier(
    private val embeddingRetriever: EmbeddingRetriever,
    private val modelAgentClassifier: ModelAgentClassifier,
    private var rephraser: Rephraser,
) : HybridAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val rephrasedMessage = rephraser.rephrase(request.inputContext)
        val topScoredEmbeddings = embeddingRetriever.retrieve(request.systemContext, rephrasedMessage)
        val topScoredAgents = topScoredEmbeddings.convertEmbeddingsToAgents()
        val modelClassification =
            modelAgentClassifier.classify(
                ClassificationRequest(
                    InputContext(
                        request.inputContext.userMessage,
                        request.inputContext.historyMessages,
                        topScoredAgents,
                    ),
                    request.systemContext,
                ),
            )
        logger.info(
            "[${javaClass.simpleName}] Classified agent '${modelClassification.agents}' " +
                "for query '${request.inputContext.userMessage}'. Top scored embeddings: $topScoredAgents.",
        )
        return ClassificationResult(modelClassification.agents, topScoredAgents)
    }

    companion object {
        fun builder(): RagAgentClassifierBuilder = RagAgentClassifierBuilder()
    }
}

class RagAgentClassifierBuilder {
    private var model: ChatModel? = null
    private var systemPrompt: String? = null
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var rephraser: Rephraser = SimpleConcatenationRephraser(15)

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

    fun withRephraser(rephraser: Rephraser) =
        apply {
            this.rephraser = rephraser
        }

    fun build(): RagAgentClassifier {
        if (model == null) throw IllegalStateException("ChatModel must be set")
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")
        if (systemPrompt.isNullOrEmpty()) systemPrompt = defaultSystemPrompt()

        val modelClassifier =
            DefaultModelAgentClassifier
                .builder()
                .withChatModel(model!!)
                .withSystemPrompt(systemPrompt!!)
                .build()

        return RagAgentClassifier(embeddingRetriever!!, modelClassifier, rephraser)
    }
}

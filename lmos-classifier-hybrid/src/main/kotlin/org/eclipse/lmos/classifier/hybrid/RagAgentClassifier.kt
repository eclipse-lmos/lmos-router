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
import org.eclipse.lmos.classifier.core.llm.SystemPromptContentProvider
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
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
    private var queryRephraser: QueryRephraser,
) : HybridAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val rephrasedMessage = queryRephraser.rephrase(request.inputContext)
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
    private var systemPromptTemplate = defaultSystemPrompt()
    private var systemPromptContentProviders: List<SystemPromptContentProvider> = emptyList()
    private var embeddingRetriever: EmbeddingRetriever? = null
    private var queryRephraser: QueryRephraser = SimpleConcatenationRephraser(10)

    fun withChatModel(model: ChatModel) =
        apply {
            this.model = model
        }

    fun withSystemPromptTemplate(systemPromptTemplate: String) =
        apply {
            if (systemPromptTemplate.isNotEmpty()) this.systemPromptTemplate = systemPromptTemplate
        }

    fun withSystemPromptContentProviders(providers: List<SystemPromptContentProvider>) =
        apply {
            systemPromptContentProviders = providers
        }

    fun withEmbeddingRetriever(embeddingRetriever: EmbeddingRetriever) =
        apply {
            this.embeddingRetriever = embeddingRetriever
        }

    fun withQueryRephraser(queryRephraser: QueryRephraser) =
        apply {
            this.queryRephraser = queryRephraser
        }

    fun build(): RagAgentClassifier {
        if (model == null) throw IllegalStateException("ChatModel must be set")
        if (embeddingRetriever == null) throw IllegalStateException("EmbeddingRetriever must be set")

        val modelClassifier =
            DefaultModelAgentClassifier
                .builder()
                .withChatModel(model!!)
                .withSystemPromptTemplate(systemPromptTemplate)
                .withSystemPromptContentProviders(systemPromptContentProviders)
                .build()

        return RagAgentClassifier(embeddingRetriever!!, modelClassifier, queryRephraser)
    }
}

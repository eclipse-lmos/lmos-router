// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.semantic

import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.SystemContext

/**
 * The [EmbeddingHandler] manages agent capabilities in an embedding store.
 */
interface EmbeddingHandler {
    /**
     * Ingests the capabilities of the provided [agents] into the embedding store for the specified [context].
     *
     * This process typically includes transforming capability examples into embeddings
     * and storing them for use in semantic search or classification.
     *
     * @param context The context under which the embeddings are stored
     * @param agents The list of agents whose capabilities should be ingested.
     */
    fun ingest(
        context: SystemContext,
        agents: List<Agent>,
    )

    /**
     * Ingests the capabilities of the provided [agent] into the embedding store for the specified [context].
     *
     * This process typically includes transforming capability examples into embeddings
     * and storing them for use in semantic search or classification.
     *
     * @param context The context under which the embeddings are stored
     * @param agent The agent whose capabilities should be ingested.
     */
    fun ingest(
        context: SystemContext,
        agent: Agent,
    )

    /**
     * Removes all stored embeddings associated with the given [context].
     *
     * @param context The context under which the embeddings are stored.
     */
    fun remove(context: SystemContext)

    /**
     * Removes all stored embeddings associated with the given [context] and [agent].
     *
     * @param context The context under which the embeddings are stored.
     * @param agent The agent whose embeddings should be removed.
     */
    fun remove(
        context: SystemContext,
        agent: Agent,
    )
}

/**
 * [EmbeddingRetriever] retrieves embeddings from an embedding store.
 */
interface EmbeddingRetriever {
    /**
     * Retrieves embeddings for the given context and user message sorted by relevance.
     *
     * @param context The context under which the embeddings are stored.
     * @param userMessage The user message.
     * @return A list of [Embedding]s sorted by relevance.
     */
    fun retrieve(
        context: SystemContext,
        userMessage: String,
    ): List<Embedding>

    /**
     * Retrieves embeddings for the given context and multiple user messages sorted by relevance.
     *
     * @param context The context under which the embeddings are stored.
     * @param userMessages The list of user messages.
     * @return A list of [Embedding]s sorted by relevance.
     */
    fun retrieve(
        context: SystemContext,
        userMessages: List<String>,
    ): List<Embedding>
}

/**
 * A single vector embedding and its metadata for agent ranking.
 *
 * @property example A representative input example.
 * @property score Similarity score for the current query.
 * @property agentId ID of the agent this embedding belongs to.
 * @property capabilityId ID of the capability this embedding belongs to.
 * @property capabilityDescription Natural language description of the capability.
 */
data class Embedding(
    val example: String,
    val score: Double,
    val agentId: String,
    val agentName: String,
    val agentAddress: String,
    val capabilityId: String,
    val capabilityDescription: String,
)

/**
 * Exception indicating that the retrieval of embeddings has failed.
 */
class RetrievalFailedException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

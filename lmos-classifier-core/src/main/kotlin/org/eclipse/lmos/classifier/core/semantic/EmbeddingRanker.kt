// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.semantic

/**
 * The [EmbeddingRanker] is used by the [EmbeddingAgentClassifier] to evaluate a list of [Embedding]s
 * to determine the most qualified agents based on scores, thresholds, or other heuristics.
 *
 * The specific ranking algorithm depends on the concrete implementation
 * and may involve techniques such as maximum score selection, threshold filtering,
 * or composite scoring strategies.
 */
interface EmbeddingRanker {
    /**
     * Find the most qualified agents from a given set of [Embedding]s.
     *
     * The selection is typically based on the highest semantic similarity score
     * or a ranking strategy implemented by the classifier.
     *
     * @param embeddings A list of embeddings.
     * @param maxResults Maximum number of agents to return.
     * @return A list of agent IDs ordered by relevance, or empty list if no agents satisfy the ranking conditions.
     */
    fun findMostQualifiedAgents(
        embeddings: List<Embedding>,
        maxResults: Int = 1,
    ): List<String>
}

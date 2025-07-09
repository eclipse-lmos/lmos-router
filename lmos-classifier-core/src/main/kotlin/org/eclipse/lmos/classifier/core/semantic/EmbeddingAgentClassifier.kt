// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.semantic

import org.eclipse.lmos.classifier.core.AgentClassifier
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult

/**
 * The [EmbeddingAgentClassifier] classifies the given request to the most appropriate
 * agents using a vector search and a ranking algorithm.
 *
 * It performs a two-step process:
 * - Performs a semantic search using vector embeddings to retrieve candidate agents based on similarity.
 * - Ranks the agents candidates using an [EmbeddingRanker] to determine the most qualified agents.
 */
interface EmbeddingAgentClassifier : AgentClassifier {
    /**
     * Classifies the given request using semantic search and a ranking algorithm.
     *
     * @param request the classification request
     * @return The classification result with the most appropriate agents
     */
    override fun classify(request: ClassificationRequest): ClassificationResult
}

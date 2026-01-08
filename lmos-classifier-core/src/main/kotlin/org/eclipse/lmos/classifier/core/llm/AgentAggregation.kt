// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.llm

import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.ClassificationRequest

/**
 * Aggregates [Agent]s from multiple [AgentProvider]s as input for
 * the LLM-based classification by the [ModelAgentClassifier].
 */
interface AgentAggregator {
    /**
     * Collects and aggregates [Agent]s from all registered [AgentProvider]s for the given [ClassificationRequest].
     *
     * @param request The classification request containing input and system context information.
     * @return A consolidated list of candidate agents aggregated from all providers.
     */
    suspend fun aggregate(request: ClassificationRequest): List<Agent>
}

/**
 * Provides [Agent]s used by the [ModelAgentClassifier] during LLM-based classification.
 *
 * An [AgentProvider] is responsible for supplying potential agent candidates that can be
 * considered during classification. The data sources for these agents can vary and may
 * include vector databases, external systems, etc.
 */
interface AgentProvider {
    /**
     * Returns a list of candidate [Agent]s relevant for the given [ClassificationRequest].
     *
     * @param request The classification request containing input and system context for agent selection.
     * @return A list of agent candidates to be considered for classification.
     */
    suspend fun provide(request: ClassificationRequest): List<Agent>
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.llm

import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.AgentClassifier
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult

/**
 * The [ModelAgentClassifier] classifies the given request to the most appropriate agents
 * using a large language model (LLM).
 */
interface ModelAgentClassifier : AgentClassifier {
    /**
     * Classifies the given request using agents provided by the configured [AgentProvider]s.
     *
     * @param request The classification request containing input and system context information.
     * @return A [ClassificationResult] representing the agents identified as most relevant.
     */
    override fun classify(request: ClassificationRequest): ClassificationResult

    /**
     * Classifies the given request using both the provided [agents], and the agents
     * from the configured [AgentProvider]s.
     *
     * @param request The classification request containing input and system context information.
     * @param agents Additional candidate agents to consider during classification.
     * @return A [ClassificationResult] representing the agents identified as most relevant.
     */
    fun classify(
        request: ClassificationRequest,
        agents: List<Agent>,
    ): ClassificationResult
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.llm

import org.eclipse.lmos.classifier.core.AgentClassifier
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult

/**
 * The [ModelAgentClassifier] classifies the given request to the most appropriate agents
 * using a large language model (LLM).
 */
interface ModelAgentClassifier : AgentClassifier {
    /**
     * Classifies the given request using an LLM.
     *
     * @param request the classification request containing user message, history messages, and agents that should be considered
     * @return The classification result with the most appropriate agents
     */
    override fun classify(request: ClassificationRequest): ClassificationResult
}

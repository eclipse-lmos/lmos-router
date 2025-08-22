// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.hybrid

import org.eclipse.lmos.classifier.core.AgentClassifier
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult

/**
 * A classifier that combines semantic search with model-based reasoning to identify the most appropriate agents.
 *
 * A hybrid strategy is applied where semantic search narrows down relevant agents and a language model can be
 * used to enhance the classification.
 *
 * The specific decision logic is defined by the implementing class.
 */
interface HybridAgentClassifier : AgentClassifier {
    /**
     * Classifies the given request using a hybrid approach (semantic search + LLM).
     *
     * @param request the classification request
     * @return The classification result with the chosen agents
     */
    override fun classify(request: ClassificationRequest): ClassificationResult
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.rephrase

import org.eclipse.lmos.classifier.core.ClassificationRequest

/**
 * Rephrases a conversation to generate alternative formulations of the user's intent. The rephrased
 * output can be used to enhance retrieval or classification accuracy.
 */
interface QueryRephraser {
    /**
     * Rephrases the given classification request.
     *
     * @param request The classification request containing the conversation and system context.
     * @return A list of messages rephrasing the user's intent
     */
    fun rephrase(request: ClassificationRequest): List<String>
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.rephrase

import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser

/**
 * A simple implementation of [QueryRephraser] that concatenates message contents instead of performing true rephrasing.
 */
class SimpleConcatenationRephraser(
    private val maxHistoryMessages: Int,
) : QueryRephraser {
    override fun rephrase(request: ClassificationRequest) =
        listOf(
            (
                request.inputContext.historyMessages
                    .takeLast(maxHistoryMessages)
                    .map { it.content } + request.inputContext.userMessage
            ).joinToString("\n"),
        )
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.rephrase

import org.eclipse.lmos.classifier.core.InputContext

/**
 * Interface for rephrasing a message in the context of a conversation.
 */
interface Rephraser {
    /**
     * Rephrases the given user message considering the conversation history.
     *
     * @param context The context with the user message and the conversation history.
     * @return A rephrased version of the given messages.
     */
    fun rephrase(context: InputContext): String
}

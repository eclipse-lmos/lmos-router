// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.llm

import org.eclipse.lmos.classifier.core.ClassificationRequest

/**
 * A [SystemPromptContentProvider] supplies content for a system prompt used in agent classification.
 * Each piece of content is identified by a unique key that corresponds to a placeholder
 * in the system prompt template.
 *
 * During rendering, the [SystemPromptRenderer] replaces each placeholder with the content
 * provided by the corresponding [SystemPromptContentProvider]. Content may be defined statically
 * or retrieved dynamically from external sources.
 */
interface SystemPromptContentProvider {
    /**
     * Returns the key used as a placeholder for the content in the prompt template
     *
     * @return the placeholder key
     */
    fun key(): String

    /**
     * Returns the content associated with this key.
     *
     * @param request the classification request context
     * @return the resolved content
     */
    fun content(request: ClassificationRequest): Any
}

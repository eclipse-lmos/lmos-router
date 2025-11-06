// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.llm

import org.eclipse.lmos.classifier.core.ClassificationRequest

/**
 * Renders a system prompt template by resolving and replacing all placeholders
 * with their corresponding content.
 */
interface SystemPromptRenderer {
    /**
     * Renders the given system prompt template by resolving all keys with the content provided
     * by the given [SystemPromptContentProvider] instances.
     *
     * @param systemPromptTemplate the raw template string that may contain placeholders
     * @param systemPromptContentProviders the providers supplying content for the placeholders
     * @param classificationRequest the classification request used to resolve provider content
     * @return the fully rendered system prompt with all placeholders replaced
     */
    fun render(
        systemPromptTemplate: String,
        systemPromptContentProviders: List<SystemPromptContentProvider>,
        classificationRequest: ClassificationRequest,
    ): String
}

/**
 * Exception thrown when rendering a prompt fails.
 *
 * @param message a human-readable description of the error
 * @param cause the underlying exception that caused the failure
 */
class SystemPromptRendererException(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause)

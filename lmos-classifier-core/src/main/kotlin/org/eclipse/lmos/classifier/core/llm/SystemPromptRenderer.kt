// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.llm

/**
 * Renders a system prompt template by resolving and replacing all placeholders
 * with their corresponding content.
 */
interface SystemPromptRenderer {
    /**
     * Renders the given [systemPromptTemplate] by replacing all placeholders with
     * the matching values from [systemPromptVariables].
     *
     * @param systemPromptTemplate the raw template string that may contain placeholders
     * @param systemPromptVariables a map where each key represents a placeholder name in the template,
     * and the corresponding value provides the content used to replace it
     * @return the fully rendered system prompt with all placeholders replaced
     */
    fun render(
        systemPromptTemplate: String,
        systemPromptVariables: Map<String, Any>,
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

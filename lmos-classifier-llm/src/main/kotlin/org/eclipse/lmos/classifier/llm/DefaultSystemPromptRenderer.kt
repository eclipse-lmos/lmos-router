// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.llm.SystemPromptContentProvider
import org.eclipse.lmos.classifier.core.llm.SystemPromptRenderer
import org.eclipse.lmos.classifier.core.llm.SystemPromptRendererException
import org.mvel2.templates.TemplateRuntime

/**
 * Default implementation of [SystemPromptRenderer] using MVEL 2.0 as templating engine.
 *
 * MVEL is used to make provider content explicitly visible within the prompt template
 * and to allow changes to displayed provider content without requiring any code modifications.
 * For details on MVEL syntax and templating, see the official documentation:
 * [MVEL 2.0 Templating Guide](http://mvel.documentnode.com/#mvel-2.0-templating-guide)
 */
class DefaultSystemPromptRenderer : SystemPromptRenderer {
    override fun render(
        systemPromptTemplate: String,
        systemPromptContentProviders: List<SystemPromptContentProvider>,
        classificationRequest: ClassificationRequest,
    ) = renderPrompt(systemPromptTemplate, resolveVariables(systemPromptContentProviders, classificationRequest))

    private fun resolveVariables(
        systemPromptContentProviders: List<SystemPromptContentProvider>,
        request: ClassificationRequest,
    ): Map<String, Any> {
        val variables = mutableMapOf<String, Any>()
        for (provider in systemPromptContentProviders) {
            try {
                variables[provider.key()] = provider.content(request)
            } catch (ex: Exception) {
                throw SystemPromptRendererException(
                    "Failed to resolve variable '${provider.key()}' from provider '${provider::class.qualifiedName}': ${ex.message}",
                    ex,
                )
            }
        }
        return variables
    }

    private fun renderPrompt(
        systemPromptTemplate: String,
        variables: Map<String, Any>,
    ): String {
        try {
            return TemplateRuntime.eval(systemPromptTemplate, variables).toString()
        } catch (ex: Exception) {
            throw SystemPromptRendererException(
                "Failed to render prompt template: ${ex.message}\nTemplate:\n$systemPromptTemplate",
                ex,
            )
        }
    }
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.llm.AgentAggregator
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.slf4j.LoggerFactory

class DefaultAgentAggregator(
    private val agentProviders: List<AgentProvider>,
) : AgentAggregator {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun aggregate(request: ClassificationRequest): List<Agent> =
        agentProviders
            .flatMap { provider ->
                runCatching { provider.provide(request) }
                    .onFailure { e ->
                        logger.error(
                            "Failed to retrieve agents from provider '${provider::class.simpleName}'. " +
                                "Agents from this provider will be ignored for this classification call.",
                            e,
                        )
                    }.getOrDefault(emptyList())
            }.distinctBy { it.id }
}

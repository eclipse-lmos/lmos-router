// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.provider

import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.vector.utils.convertEmbeddingsToAgents
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "lmos.router.agent-provider.embedding",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class EmbeddingAgentProvider(
    private val embeddingRetriever: EmbeddingRetriever,
    private var queryRephraser: QueryRephraser,
) : AgentProvider {
    override fun provide(request: ClassificationRequest): List<Agent> {
        val rephrasedMessage = queryRephraser.rephrase(request.inputContext)
        val embeddings = embeddingRetriever.retrieve(request.systemContext, rephrasedMessage)
        return embeddings.convertEmbeddingsToAgents()
    }
}

@Component
@ConditionalOnProperty(
    prefix = "lmos.router.agent-provider.weather",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class WeatherAgentProvider : AgentProvider {
    override fun provide(request: ClassificationRequest): List<Agent> =
        listOf(
            Agent(
                id = "weather-bot",
                name = "web-weather-bot",
                address = "https://weather-bot.com",
                capabilities =
                    listOf(
                        Capability(id = "weather", description = "Provides weather forecasts"),
                        Capability(id = "climate", description = "Gives climate statistics and long-term trends"),
                        Capability(id = "alerts", description = "Sends severe weather warnings and alerts"),
                    ),
            ),
        )
}

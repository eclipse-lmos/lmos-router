// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector.starter

import org.eclipse.lmos.router.core.AgentRoutingSpecsProvider
import org.eclipse.lmos.router.core.JsonAgentRoutingSpecsProvider
import org.eclipse.lmos.router.vector.VectorAgentRoutingSpecsResolver
import org.eclipse.lmos.router.vector.VectorSearchClient
import org.eclipse.lmos.router.vector.VectorSeedClient
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(
    SpringVectorSearchClientProperties::class,
    VectorAgentRoutingSpecsResolverProperties::class,
)
open class VectorAgentRoutingSpecsResolverAutoConfiguration(
    private val springVectorSearchClientProperties: SpringVectorSearchClientProperties,
    private val properties: VectorAgentRoutingSpecsResolverProperties,
) {
    /**
     * Provides a [VectorSearchClient] that uses the vector store to search for similar vectors.
     *
     * @param vectorStore The vector store to search in.
     * @return The vector search client.
     */
    @Bean
    @ConditionalOnMissingBean(VectorSearchClient::class)
    open fun vectorSearchClient(vectorStore: VectorStore): VectorSearchClient =
        SpringVectorSearchClient(vectorStore, springVectorSearchClientProperties)

    /**
     * Provides a [VectorSeedClient] that uses the vector store to seed vectors.
     *
     * @param vectorStore The vector store to seed.
     * @return The vector seed client.
     */
    @Bean
    @ConditionalOnMissingBean(VectorSeedClient::class)
    open fun vectorSeedClient(vectorStore: VectorStore): VectorSeedClient = SpringVectorSeedClient(vectorStore)

    /**
     * Provides an [AgentRoutingSpecsProvider] that reads agent routing specifications from a JSON file.
     *
     * @return The agent specs provider.
     */
    @Bean
    @ConditionalOnMissingBean(AgentRoutingSpecsProvider::class)
    open fun agentRoutingSpecsProvider(): AgentRoutingSpecsProvider = JsonAgentRoutingSpecsProvider(properties.specFilePath)

    /**
     * Provides a [VectorAgentRoutingSpecsResolver] that resolves agent routing specifications using the vector search client.
     *
     * @param agentRoutingSpecsProvider The agent specs provider.
     * @param vectorSearchClient The vector search client.
     * @return The vector agent spec resolver.
     */
    @Bean
    @ConditionalOnMissingBean(VectorAgentRoutingSpecsResolver::class)
    open fun vectorAgentRoutingSpecsResolver(
        agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
        vectorSearchClient: VectorSearchClient,
    ): VectorAgentRoutingSpecsResolver = VectorAgentRoutingSpecsResolver(agentRoutingSpecsProvider, vectorSearchClient)
}

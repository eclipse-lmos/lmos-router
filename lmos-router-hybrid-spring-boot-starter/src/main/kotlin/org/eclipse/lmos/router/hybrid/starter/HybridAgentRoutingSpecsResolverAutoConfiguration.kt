// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import org.eclipse.lmos.router.core.AgentRoutingSpecsProvider
import org.eclipse.lmos.router.core.JsonAgentRoutingSpecsProvider
import org.eclipse.lmos.router.hybrid.HybridAgentRoutingSpecsResolver
import org.eclipse.lmos.router.hybrid.ModelToVectorQueryConverter
import org.eclipse.lmos.router.hybrid.NoOpModelToVectorQueryConverter
import org.eclipse.lmos.router.llm.ExternalModelPromptProvider
import org.eclipse.lmos.router.llm.ModelClient
import org.eclipse.lmos.router.llm.ModelPromptProvider
import org.eclipse.lmos.router.vector.VectorSearchClient
import org.eclipse.lmos.router.vector.VectorSeedClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(
    SpringVectorSearchClientProperties::class,
    HybridAgentRoutingSpecsResolverProperties::class,
)
open class HybridAgentRoutingSpecsResolverAutoConfiguration(
    private val springVectorSearchClientProperties: SpringVectorSearchClientProperties,
    private val properties: HybridAgentRoutingSpecsResolverProperties,
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
     * Provides a [ModelClient] that uses the [ChatModel] to resolve agent routing specifications.
     *
     * @param chatModel The chat model.
     * @return The model client.
     */
    @Bean
    @ConditionalOnMissingBean(ModelClient::class)
    open fun springAgentResolverCompletionProvider(chatModel: ChatModel): ModelClient = SpringModelClient(chatModel)

    /**
     * Provides an [AgentRoutingSpecsProvider] that reads agent routing specifications from a JSON file.
     *
     * @return The agent specs provider.
     */
    @Bean
    @ConditionalOnMissingBean(AgentRoutingSpecsProvider::class)
    open fun agentRoutingSpecsProvider(): AgentRoutingSpecsProvider = JsonAgentRoutingSpecsProvider(properties.specFilePath)

    /**
     * Provides a [ModelPromptProvider] that uses the default model prompt provider.
     *
     * @return The model prompt provider.
     */
    @Bean
    @ConditionalOnMissingBean(ModelPromptProvider::class)
    open fun agentResolverPromptProvider(): ModelPromptProvider = ExternalModelPromptProvider(properties.resolverPromptFilePath)

    /**
     * Provides a [ModelToVectorQueryConverter] that uses the no-op model to vector query converter.
     *
     * @return The model to vector query converter.
     */
    @Bean
    @ConditionalOnMissingBean(ModelToVectorQueryConverter::class)
    open fun modelToVectorQueryConverter(): ModelToVectorQueryConverter = NoOpModelToVectorQueryConverter()

    /**
     * Provides a [HybridAgentRoutingSpecsResolver] that resolves agent routing specifications using the vector search client.
     *
     * @param agentRoutingSpecsProvider The agent specs provider.
     * @param vectorSearchClient The vector search client.
     * @param modelClient The model client.
     * @param promptProvider The model prompt provider.
     * @return The vector agent spec resolver.
     */
    @Bean
    @ConditionalOnMissingBean(HybridAgentRoutingSpecsResolver::class)
    open fun hybridAgentRoutingSpecsResolver(
        agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
        vectorSearchClient: VectorSearchClient,
        modelClient: ModelClient,
        promptProvider: ModelPromptProvider,
        modelToVectorQueryConverter: ModelToVectorQueryConverter,
    ): HybridAgentRoutingSpecsResolver =
        HybridAgentRoutingSpecsResolver(
            agentRoutingSpecsProvider,
            modelClient,
            promptProvider,
            vectorSearchClient,
            modelToVectorQueryConverter,
        )
}

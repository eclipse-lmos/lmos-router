// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm.starter

import org.eclipse.lmos.router.core.AgentRoutingSpecsProvider
import org.eclipse.lmos.router.core.JsonAgentRoutingSpecsProvider
import org.eclipse.lmos.router.llm.DefaultModelPromptProvider
import org.eclipse.lmos.router.llm.LLMAgentRoutingSpecsResolver
import org.eclipse.lmos.router.llm.ModelClient
import org.eclipse.lmos.router.llm.ModelPromptProvider
import org.springframework.ai.chat.model.ChatModel
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Autoconfiguration for the LLM agent resolver.
 *
 * The [springAgentResolverCompletionProvider] bean provides a [ModelClient] that uses the [ChatModel] to resolve agent routing specifications.
 * The [agentRoutingSpecsProvider] bean provides an [AgentRoutingSpecsProvider] that reads agent routing specifications from a JSON file.
 * The [agentResolverPromptProvider] bean provides a [ModelPromptProvider] that uses the default model prompt provider.
 * The [llmAgentRoutingSpecsResolver] bean provides an [LLMAgentRoutingSpecsResolver] that uses the [AgentRoutingSpecsProvider], [ModelPromptProvider], and [ModelClient] to resolve agent routing specifications.
 *
 * @param properties The properties for the LLM agent resolver.
 */
@AutoConfiguration
@EnableConfigurationProperties(LLMAgentRoutingSpecsResolverProperties::class)
open class LLMAgentRoutingSpecsResolverAutoConfiguration(
    private val properties: LLMAgentRoutingSpecsResolverProperties,
) {
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
    open fun agentResolverPromptProvider(): ModelPromptProvider = DefaultModelPromptProvider()

    /**
     * Provides an [LLMAgentRoutingSpecsResolver] that uses the [AgentRoutingSpecsProvider], [ModelPromptProvider], and [ModelClient] to resolve agent routing specifications.
     *
     * @param agentRoutingSpecsProvider The provider of agent routing specifications.
     * @param modelPromptProvider The provider of model prompts.
     * @param modelClient The client for the language model.
     * @return The LLM agent spec resolver.
     */
    @Bean
    @ConditionalOnMissingBean(LLMAgentRoutingSpecsResolver::class)
    open fun llmAgentRoutingSpecsResolver(
        agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
        modelPromptProvider: ModelPromptProvider,
        modelClient: ModelClient,
    ): LLMAgentRoutingSpecsResolver = LLMAgentRoutingSpecsResolver(agentRoutingSpecsProvider, modelPromptProvider, modelClient)
}

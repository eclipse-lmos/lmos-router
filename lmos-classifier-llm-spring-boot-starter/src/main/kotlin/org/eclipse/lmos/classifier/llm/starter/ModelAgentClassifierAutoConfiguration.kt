// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm.starter

import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.eclipse.lmos.classifier.core.starter.ChatModelProperties
import org.eclipse.lmos.classifier.llm.ChatModelClientProperties
import org.eclipse.lmos.classifier.llm.DefaultModelAgentClassifier
import org.eclipse.lmos.classifier.llm.LangChainChatModelFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(
    ChatModelProperties::class,
)
@ConditionalOnProperty(
    prefix = "lmos.router.classifier.llm",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
open class ModelAgentClassifierAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ChatModel::class)
    open fun chatModel(chatModelProperties: ChatModelProperties): ChatModel =
        LangChainChatModelFactory.createClient(
            ChatModelClientProperties(
                provider = chatModelProperties.provider,
                apiKey = chatModelProperties.apiKey,
                baseUrl = chatModelProperties.baseUrl,
                model = chatModelProperties.model,
                maxTokens = chatModelProperties.maxTokens,
                temperature = chatModelProperties.temperature,
                logRequestsAndResponses = chatModelProperties.logRequestsAndResponses,
            ),
        )

    @Bean
    @ConditionalOnMissingBean(ModelAgentClassifier::class)
    open fun modelAgentClassifier(
        chatModel: ChatModel,
        chatModelProperties: ChatModelProperties,
        agentProviders: List<AgentProvider>,
    ): ModelAgentClassifier =
        DefaultModelAgentClassifier
            .builder()
            .withChatModel(chatModel)
            .withSystemPromptTemplate(chatModelProperties.systemPrompt)
            .withAgentProviders(agentProviders)
            .build()
}

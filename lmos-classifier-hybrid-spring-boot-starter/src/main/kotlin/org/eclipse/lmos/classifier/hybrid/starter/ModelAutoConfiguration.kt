// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.hybrid.starter

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.core.starter.ChatModelProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingModelProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.classifier.llm.ChatModelClientProperties
import org.eclipse.lmos.classifier.llm.LangChainChatModelFactory
import org.eclipse.lmos.classifier.vector.retriever.QdrantEmbeddingRetriever
import org.eclipse.lmos.classifier.vector.utils.EmbeddingModelClientProperties
import org.eclipse.lmos.classifier.vector.utils.LangChainEmbeddingModelFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ChatModelProperties::class,
    EmbeddingStoreProperties::class,
    EmbeddingModelProperties::class,
    EmbeddingRankingProperties::class,
)
@ConditionalOnProperty(
    prefix = "lmos.router.classifier.hybrid-fast-track",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
open class ModelAutoConfiguration {
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
    @ConditionalOnMissingBean(EmbeddingModel::class)
    open fun embeddingModel(embeddingModelProperties: EmbeddingModelProperties): EmbeddingModel =
        LangChainEmbeddingModelFactory.createClient(
            EmbeddingModelClientProperties(
                provider = embeddingModelProperties.provider,
                modelName = embeddingModelProperties.modelName,
                apiKey = embeddingModelProperties.apiKey,
                baseUrl = embeddingModelProperties.baseUrl,
                modelPath = embeddingModelProperties.modelPath,
                tokenizerPath = embeddingModelProperties.tokenizerPath,
            ),
        )

    @Bean
    @ConditionalOnMissingBean(EmbeddingRetriever::class)
    open fun embeddingRetriever(
        embeddingModel: EmbeddingModel,
        embeddingStoreProperties: EmbeddingStoreProperties,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): EmbeddingRetriever =
        QdrantEmbeddingRetriever
            .builder()
            .withQdrantHost(embeddingStoreProperties.host)
            .withQdrantPort(embeddingStoreProperties.port)
            .withQdrantTlsEnabled(embeddingStoreProperties.tlsEnabled)
            .withQdrantApiKey(embeddingStoreProperties.apiKey)
            .withEmbeddingModel(embeddingModel)
            .withEmbeddingMaxResults(embeddingRankingProperties.maxEmbeddings)
            .build()
}

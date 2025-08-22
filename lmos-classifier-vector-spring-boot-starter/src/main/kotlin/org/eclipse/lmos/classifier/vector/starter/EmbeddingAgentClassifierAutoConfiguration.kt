// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.starter

import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.classifier.core.rephrase.Rephraser
import org.eclipse.lmos.classifier.core.semantic.EmbeddingAgentClassifier
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.core.starter.EmbeddingModelProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingRephraserProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingStoreProperties
import org.eclipse.lmos.classifier.vector.DefaultEmbeddingAgentClassifier
import org.eclipse.lmos.classifier.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.classifier.vector.ranker.SingleAgentEmbeddingRanker
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
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
    EmbeddingStoreProperties::class,
    EmbeddingRankingProperties::class,
    EmbeddingRephraserProperties::class,
)
@ConditionalOnProperty(
    prefix = "lmos.router.classifier.vector",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
open class EmbeddingAgentClassifierAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(EmbeddingModel::class)
    open fun embeddingModel(chatModelProperties: EmbeddingModelProperties): EmbeddingModel =
        LangChainEmbeddingModelFactory.createClient(
            EmbeddingModelClientProperties(
                provider = chatModelProperties.provider,
                modelName = chatModelProperties.modelName,
                apiKey = chatModelProperties.apiKey,
                baseUrl = chatModelProperties.baseUrl,
                modelPath = chatModelProperties.modelPath,
                tokenizerPath = chatModelProperties.tokenizerPath,
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

    @Bean
    @ConditionalOnMissingBean(Rephraser::class)
    open fun rephraser(embeddingRephraserProperties: EmbeddingRephraserProperties): Rephraser =
        SimpleConcatenationRephraser(embeddingRephraserProperties.maxHistoryMessages)

    @Bean
    @ConditionalOnMissingBean(EmbeddingAgentClassifier::class)
    open fun embeddingAgentClassifier(
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
        rephraser: Rephraser,
    ): EmbeddingAgentClassifier =
        DefaultEmbeddingAgentClassifier
            .builder()
            .withEmbeddingRetriever(embeddingRetriever)
            .withEmbeddingRanker(
                SingleAgentEmbeddingRanker(
                    EmbeddingRankingThreshold(
                        minWeight = embeddingRankingProperties.minWeight,
                        minDistance = embeddingRankingProperties.minDistance,
                        minMeanScore = embeddingRankingProperties.minMeanScore,
                        minRealDistance = embeddingRankingProperties.minRealDistance,
                    ),
                ),
            ).withRephraser(rephraser)
            .build()
}

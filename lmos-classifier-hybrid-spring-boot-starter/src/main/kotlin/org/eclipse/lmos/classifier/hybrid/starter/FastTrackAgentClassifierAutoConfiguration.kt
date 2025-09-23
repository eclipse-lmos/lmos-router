// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.hybrid.starter

import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.core.starter.ChatModelProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingRankingProperties
import org.eclipse.lmos.classifier.hybrid.FastTrackAgentClassifier
import org.eclipse.lmos.classifier.vector.ranker.EmbeddingRankingThreshold
import org.eclipse.lmos.classifier.vector.ranker.SingleAgentEmbeddingRanker
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ChatModelProperties::class,
    EmbeddingRankingProperties::class,
)
@ConditionalOnProperty(
    prefix = "lmos.router.classifier.hybrid-fast-track",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
open class FastTrackAgentClassifierAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(FastTrackAgentClassifier::class)
    open fun fastTrackAgentClassifier(
        chatModel: ChatModel,
        chatModelProperties: ChatModelProperties,
        embeddingRetriever: EmbeddingRetriever,
        embeddingRankingProperties: EmbeddingRankingProperties,
    ): FastTrackAgentClassifier =
        FastTrackAgentClassifier
            .builder()
            .withChatModel(chatModel)
            .withSystemPrompt(chatModelProperties.systemPrompt)
            .withEmbeddingRetriever(embeddingRetriever)
            .withEmbeddingRanker(
                SingleAgentEmbeddingRanker(
                    EmbeddingRankingThreshold(
                        minScore = embeddingRankingProperties.minScore,
                        minDistance = embeddingRankingProperties.minDistance,
                        minMeanScore = embeddingRankingProperties.minMeanScore,
                        minRelDistance = embeddingRankingProperties.minRelDistance,
                    ),
                ),
            ).build()
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.hybrid.starter

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.core.starter.ChatModelProperties
import org.eclipse.lmos.classifier.core.starter.EmbeddingRephraserProperties
import org.eclipse.lmos.classifier.hybrid.RagAgentClassifier
import org.eclipse.lmos.classifier.vector.rephrase.SimpleConcatenationRephraser
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(
    ChatModelProperties::class,
    EmbeddingRephraserProperties::class,
)
@ConditionalOnProperty(
    prefix = "lmos.router.classifier.hybrid-rag",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class RagAgentClassifierAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(RagAgentClassifier::class)
    fun ragAgentClassifier(
        chatModel: ChatModel,
        chatModelProperties: ChatModelProperties,
        embeddingModel: EmbeddingModel,
        embeddingRetriever: EmbeddingRetriever,
        queryRephraser: QueryRephraser,
    ): RagAgentClassifier =
        RagAgentClassifier
            .builder()
            .withChatModel(chatModel)
            .withSystemPromptTemplate(chatModelProperties.systemPrompt)
            .withEmbeddingRetriever(embeddingRetriever)
            .withQueryRephraser(queryRephraser)
            .build()

    @Bean
    @ConditionalOnMissingBean(QueryRephraser::class)
    fun queryRephraser(embeddingRephraserProperties: EmbeddingRephraserProperties): QueryRephraser =
        SimpleConcatenationRephraser(embeddingRephraserProperties.maxHistoryMessages)
}

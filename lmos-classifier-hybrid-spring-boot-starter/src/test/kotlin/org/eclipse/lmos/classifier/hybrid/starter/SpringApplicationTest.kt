// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.hybrid.starter

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.hybrid.FastTrackAgentClassifier
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringApplicationTest {
    @Autowired
    private lateinit var embeddingModel: EmbeddingModel

    @Autowired
    private lateinit var embeddingRetriever: EmbeddingRetriever

    @Autowired
    private lateinit var chatModel: ChatModel

    @Autowired
    private lateinit var queryRephraser: QueryRephraser

    @Autowired
    private lateinit var fastTrackAgentClassifier: FastTrackAgentClassifier

    @Test
    fun `bean EmbeddingModel ist loaded if classifier is enabled by property`() {
        assertThat(embeddingModel).isNotNull()
    }

    @Test
    fun `bean EmbeddingStore ist loaded if classifier is enabled by property`() {
        assertThat(embeddingRetriever).isNotNull()
    }

    @Test
    fun `bean ChatModel ist loaded if classifier is enabled by property`() {
        assertThat(chatModel).isNotNull()
    }

    @Test
    fun `bean QueryRephraser ist loaded if classifier is enabled by property`() {
        assertThat(queryRephraser).isNotNull()
    }

    @Test
    fun `bean FastTrackAgentClassifier ist loaded if classifier is enabled by property`() {
        assertThat(fastTrackAgentClassifier).isNotNull()
    }
}

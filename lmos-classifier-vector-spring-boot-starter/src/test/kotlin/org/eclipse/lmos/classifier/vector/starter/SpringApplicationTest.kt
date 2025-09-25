// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.starter

import dev.langchain4j.model.embedding.EmbeddingModel
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.rephrase.QueryRephraser
import org.eclipse.lmos.classifier.core.semantic.EmbeddingAgentClassifier
import org.eclipse.lmos.classifier.core.semantic.EmbeddingHandler
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
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
    private lateinit var embeddingAgentClassifier: EmbeddingAgentClassifier

    @Autowired
    private lateinit var embeddingHandler: EmbeddingHandler

    @Autowired
    private lateinit var queryRephraser: QueryRephraser

    @Test
    fun `bean EmbeddingModel ist loaded if classifier is enabled by property`() {
        assertThat(embeddingModel).isNotNull()
    }

    @Test
    fun `bean EmbeddingStore ist loaded if classifier is enabled by property`() {
        assertThat(embeddingRetriever).isNotNull()
    }

    @Test
    fun `bean EmbeddingAgentClassifier ist loaded if classifier is enabled by property`() {
        assertThat(embeddingAgentClassifier).isNotNull()
    }

    @Test
    fun `bean EmbeddingHandler ist loaded if classifier is enabled by property`() {
        assertThat(embeddingHandler).isNotNull()
    }

    @Test
    fun `bean QueryRephraser ist loaded if classifier is enabled by property`() {
        assertThat(queryRephraser).isNotNull()
    }
}

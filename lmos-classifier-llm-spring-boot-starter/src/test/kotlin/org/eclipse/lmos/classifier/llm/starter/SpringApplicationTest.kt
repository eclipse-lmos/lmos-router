// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm.starter

import dev.langchain4j.model.chat.ChatModel
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringApplicationTest {
    @Autowired
    private lateinit var chatModel: ChatModel

    @Autowired
    private lateinit var modelAgentClassifier: ModelAgentClassifier

    @Test
    fun `bean ChatModel ist loaded if classifier is enabled by property`() {
        assertThat(chatModel).isNotNull()
    }

    @Test
    fun `bean ModelAgentClassifier ist loaded if classifier is enabled by property`() {
        assertThat(modelAgentClassifier).isNotNull()
    }
}

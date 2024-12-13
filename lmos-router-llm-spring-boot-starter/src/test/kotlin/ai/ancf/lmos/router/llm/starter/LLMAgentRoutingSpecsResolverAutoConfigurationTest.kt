// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm.starter

import io.mockk.mockk
import org.eclipse.lmos.router.core.AgentRoutingSpecsProvider
import org.eclipse.lmos.router.core.JsonAgentRoutingSpecsProvider
import org.eclipse.lmos.router.llm.DefaultModelPromptProvider
import org.eclipse.lmos.router.llm.LLMAgentRoutingSpecsResolver
import org.eclipse.lmos.router.llm.ModelClient
import org.eclipse.lmos.router.llm.ModelPromptProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.model.ChatModel

class LLMAgentRoutingSpecsResolverAutoConfigurationTest {
    private lateinit var properties: LLMAgentRoutingSpecsResolverProperties
    private lateinit var autoConfiguration: LLMAgentRoutingSpecsResolverAutoConfiguration

    @BeforeEach
    fun setup() {
        properties = LLMAgentRoutingSpecsResolverProperties(specFilePath = "src/test/resources/agentRoutingSpecs.json")
        autoConfiguration = LLMAgentRoutingSpecsResolverAutoConfiguration(properties)
    }

    @Test
    fun `test springAgentResolverCompletionProvider creates ModelClient`() {
        // Arrange
        val chatModel = mockk<ChatModel>()

        // Act
        val modelClient = autoConfiguration.springAgentResolverCompletionProvider(chatModel)

        // Assert
        assertEquals(modelClient::class, SpringModelClient::class)
    }

    @Test
    fun `test agentRoutingSpecsProvider creates JsonAgentRoutingSpecsProvider`() {
        // Act
        val agentSpecsProvider = autoConfiguration.agentRoutingSpecsProvider()

        // Assert
        assertEquals(agentSpecsProvider::class, JsonAgentRoutingSpecsProvider::class)
    }

    @Test
    fun `test agentResolverPromptProvider creates DefaultModelPromptProvider`() {
        // Act
        val modelPromptProvider = autoConfiguration.agentResolverPromptProvider()

        // Assert
        assertEquals(modelPromptProvider::class, DefaultModelPromptProvider::class)
    }

    @Test
    fun `test llmAgentRoutingSpecResolver creates LLMAgentRoutingSpecResolver with correct dependencies`() {
        // Arrange
        val agentRoutingSpecsProvider = mockk<AgentRoutingSpecsProvider>()
        val modelPromptProvider = mockk<ModelPromptProvider>()
        val modelClient = mockk<ModelClient>()

        // Act
        val llmAgentRoutingSpecResolver =
            autoConfiguration.llmAgentRoutingSpecsResolver(agentRoutingSpecsProvider, modelPromptProvider, modelClient)

        // Assert
        assertEquals(llmAgentRoutingSpecResolver::class, LLMAgentRoutingSpecsResolver::class)
    }
}

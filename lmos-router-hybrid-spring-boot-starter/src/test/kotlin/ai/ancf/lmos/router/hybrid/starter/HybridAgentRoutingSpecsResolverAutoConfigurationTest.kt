// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import io.mockk.every
import io.mockk.mockk
import org.eclipse.lmos.router.core.AgentRoutingSpecsProvider
import org.eclipse.lmos.router.hybrid.ModelToVectorQueryConverter
import org.eclipse.lmos.router.llm.ModelClient
import org.eclipse.lmos.router.llm.ModelPromptProvider
import org.eclipse.lmos.router.vector.VectorSearchClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.vectorstore.VectorStore

class HybridAgentRoutingSpecsResolverAutoConfigurationTest {
    private lateinit var springVectorSearchClientProperties: SpringVectorSearchClientProperties
    private lateinit var properties: HybridAgentRoutingSpecsResolverProperties
    private lateinit var configuration: HybridAgentRoutingSpecsResolverAutoConfiguration

    @BeforeEach
    fun setUp() {
        springVectorSearchClientProperties = mockk()
        properties = mockk()
        configuration =
            HybridAgentRoutingSpecsResolverAutoConfiguration(springVectorSearchClientProperties, properties)
    }

    @Test
    fun `test vectorSearchClient`() {
        val vectorStore = mockk<VectorStore>()
        val result = configuration.vectorSearchClient(vectorStore)
        assertNotNull(result)
    }

    @Test
    fun `test vectorSeedClient`() {
        val vectorStore = mockk<VectorStore>()
        val result = configuration.vectorSeedClient(vectorStore)
        assertNotNull(result)
    }

    @Test
    fun `test springAgentResolverCompletionProvider`() {
        val chatModel = mockk<ChatModel>()
        val result = configuration.springAgentResolverCompletionProvider(chatModel)
        assertNotNull(result)
    }

    @Test
    fun `test agentRoutingSpecsProvider`() {
        val specFilePath = "src/test/resources/agentRoutingSpecs.json"
        every { properties.specFilePath } returns specFilePath
        val result = configuration.agentRoutingSpecsProvider()
        assertNotNull(result)
    }

    @Test
    fun `test agentResolverPromptProvider`() {
        val resolverPromptFilePath = "src/test/resources/resolverPrompt.txt"
        every { properties.resolverPromptFilePath } returns resolverPromptFilePath
        val result = configuration.agentResolverPromptProvider()
        assertNotNull(result)
    }

    @Test
    fun `test modelToVectorQueryConverter`() {
        val result = configuration.modelToVectorQueryConverter()
        assertNotNull(result)
    }

    @Test
    fun `test vectorAgentRoutingSpecsResolver`() {
        val agentRoutingSpecsProvider = mockk<AgentRoutingSpecsProvider>()
        val vectorSearchClient = mockk<VectorSearchClient>()
        val modelClient = mockk<ModelClient>()
        val promptProvider = mockk<ModelPromptProvider>()
        val modelToVectorQueryConverter = mockk<ModelToVectorQueryConverter>()
        val result =
            configuration.hybridAgentRoutingSpecsResolver(
                agentRoutingSpecsProvider,
                vectorSearchClient,
                modelClient,
                promptProvider,
                modelToVectorQueryConverter,
            )
        assertNotNull(result)
    }
}

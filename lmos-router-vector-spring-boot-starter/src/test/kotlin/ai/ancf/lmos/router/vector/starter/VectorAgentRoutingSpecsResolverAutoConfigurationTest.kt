// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector.starter

import io.mockk.every
import io.mockk.mockk
import org.eclipse.lmos.router.core.AgentRoutingSpecsProvider
import org.eclipse.lmos.router.vector.VectorSearchClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.vectorstore.VectorStore

class VectorAgentRoutingSpecsResolverAutoConfigurationTest {
    private lateinit var springVectorSearchClientProperties: SpringVectorSearchClientProperties
    private lateinit var properties: VectorAgentRoutingSpecsResolverProperties
    private lateinit var vectorAgentRoutingSpecsResolverAutoConfiguration: VectorAgentRoutingSpecsResolverAutoConfiguration

    @BeforeEach
    fun setUp() {
        springVectorSearchClientProperties = mockk()
        properties = mockk()
        vectorAgentRoutingSpecsResolverAutoConfiguration =
            VectorAgentRoutingSpecsResolverAutoConfiguration(springVectorSearchClientProperties, properties)
    }

    @Test
    fun `test vectorSearchClient`() {
        val vectorStore = mockk<VectorStore>()
        val result = vectorAgentRoutingSpecsResolverAutoConfiguration.vectorSearchClient(vectorStore)

        assertNotNull(result)
    }

    @Test
    fun `test vectorSeedClient`() {
        val vectorStore = mockk<VectorStore>()
        val result = vectorAgentRoutingSpecsResolverAutoConfiguration.vectorSeedClient(vectorStore)

        assertNotNull(result)
    }

    @Test
    fun `test agentRoutingSpecsProvider`() {
        val specFilePath = "src/test/resources/agentRoutingSpecs.json"
        every { properties.specFilePath } returns specFilePath
        val result = vectorAgentRoutingSpecsResolverAutoConfiguration.agentRoutingSpecsProvider()

        assertNotNull(result)
    }

    @Test
    fun `test vectorAgentRoutingSpecsResolver`() {
        val agentRoutingSpecsProvider = mockk<AgentRoutingSpecsProvider>()
        val vectorSearchClient = mockk<VectorSearchClient>()
        val result =
            vectorAgentRoutingSpecsResolverAutoConfiguration.vectorAgentRoutingSpecsResolver(
                agentRoutingSpecsProvider,
                vectorSearchClient,
            )

        assertNotNull(result)
    }
}

// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.lmos.router.core.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VectorAgentRoutingSpecsResolverTest {
    private lateinit var agentRoutingSpecsProvider: AgentRoutingSpecsProvider
    private lateinit var vectorSearchClient: VectorSearchClient
    private lateinit var vectorAgentRoutingSpecsResolver: VectorAgentRoutingSpecsResolver

    @BeforeEach
    fun setUp() {
        agentRoutingSpecsProvider = mockk()
        vectorSearchClient = mockk()
        vectorAgentRoutingSpecsResolver = VectorAgentRoutingSpecsResolver(agentRoutingSpecsProvider, vectorSearchClient)
    }

    @Test
    fun `resolve without filters returns agent spec`() {
        // Arrange
        val context = mockk<Context>()
        val input = mockk<UserMessage>()
        val agentRoutingSpec1 =
            mockk<AgentRoutingSpec> {
                every { name } returns "Agent1"
            }
        val agentSpecs = setOf(agentRoutingSpec1)
        val vectorSearchClientResponse =
            mockk<VectorSearchClientResponse> {
                every { agentName } returns "Agent1"
            }
        every { input.content } returns "Sample content"

        every { agentRoutingSpecsProvider.provide(setOf()) } returns Success(agentSpecs)
        every { vectorSearchClient.find(any(), any()) } returns Success(vectorSearchClientResponse)

        // Act
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)

        // Assert
        assertTrue(result is Success)
        assertEquals(agentRoutingSpec1, result.getOrThrow())
        verify { agentRoutingSpecsProvider.provide(setOf()) }
        verify { vectorSearchClient.find(any(), agentSpecs) }
    }

    @Test
    fun `resolve with filters returns agent spec`() {
        // Arrange
        val filters = setOf<SpecFilter>(mockk())
        val context = mockk<Context>()
        val input = mockk<UserMessage>()
        val agentRoutingSpec1 =
            mockk<AgentRoutingSpec> {
                every { name } returns "Agent1"
            }
        val agentRoutingSpecs = setOf(agentRoutingSpec1)
        val vectorSearchClientResponse =
            mockk<VectorSearchClientResponse> {
                every { agentName } returns "Agent1"
            }
        every { input.content } returns "Sample content"

        every { agentRoutingSpecsProvider.provide(filters) } returns Success(agentRoutingSpecs)
        every { vectorSearchClient.find(any(), any()) } returns Success(vectorSearchClientResponse)

        // Act
        val result = vectorAgentRoutingSpecsResolver.resolve(filters, context, input)

        // Assert
        assertTrue(result is Success)
        assertEquals(agentRoutingSpec1, result.getOrThrow())
        verify { agentRoutingSpecsProvider.provide(filters) }
        verify { vectorSearchClient.find(any(), agentRoutingSpecs) }
    }

    @Test
    fun `resolve returns failure when agentSpecProvider fails`() {
        // Arrange
        val context = mockk<Context>()
        val input = mockk<UserMessage>()

        every { agentRoutingSpecsProvider.provide(setOf()) } returns Failure(AgentRoutingSpecsProviderException("Provider failed"))

        // Act
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)

        // Assert
        assertTrue(result is Failure)
        assertTrue(result.exceptionOrNull() is AgentRoutingSpecResolverException)
        verify { agentRoutingSpecsProvider.provide(setOf()) }
    }

    @Test
    fun `resolve returns failure when vectorSearchClient fails`() {
        // Arrange
        val context = mockk<Context>()
        val input = mockk<UserMessage>()
        val agentRoutingSpecs = setOf(mockk<AgentRoutingSpec>())

        every { agentRoutingSpecsProvider.provide(any()) } returns Success(agentRoutingSpecs)
        every { vectorSearchClient.find(any(), any()) } returns Failure(VectorClientException("Search failed"))
        every { input.content } returns "Sample content"

        // Act
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)

        // Assert
        assertTrue(result is Failure)
        assertTrue(result.exceptionOrNull() is AgentRoutingSpecResolverException)
        verify { vectorSearchClient.find(any(), any()) }
    }

    @Test
    fun `resolve returns null if no matching agent found`() {
        // Arrange
        val agentRoutingSpec =
            mockk<AgentRoutingSpec> {
                every { name } returns "Agent1"
            }
        val context = mockk<Context>()
        val input = mockk<UserMessage>()
        val agentSpecs = setOf(agentRoutingSpec)
        val vectorSearchClientResponse =
            mockk<VectorSearchClientResponse> {
                every { agentName } returns "NonExistentAgent"
            }

        every { agentRoutingSpecsProvider.provide(setOf()) } returns Success(agentSpecs)
        every { vectorSearchClient.find(any(), any()) } returns Success(vectorSearchClientResponse)
        every { input.content } returns "Sample content"

        // Act
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)

        // Assert
        assertTrue(result is Success)
        assertNull(result.getOrThrow())
        verify { vectorSearchClient.find(any(), agentSpecs) }
    }
}

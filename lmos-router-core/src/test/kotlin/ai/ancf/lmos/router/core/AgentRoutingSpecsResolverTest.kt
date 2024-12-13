// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AgentRoutingSpecsResolverTest {
    private lateinit var agentRoutingSpecsProvider: AgentRoutingSpecsProvider
    private lateinit var agentRoutingSpecsResolver: AgentRoutingSpecsResolver
    private lateinit var context: Context
    private lateinit var input: UserMessage
    private lateinit var filters: Set<SpecFilter>

    @BeforeEach
    fun setUp() {
        agentRoutingSpecsProvider = mockk()
        agentRoutingSpecsResolver = mockk()
        context = mockk()
        input = mockk()
        filters = setOf(mockk(), mockk())
    }

    @Test
    fun `resolve should return AgentSpec`() {
        val expectedAgentRoutingSpec = mockk<AgentRoutingSpec>()
        every { agentRoutingSpecsResolver.resolve(any(), any()) } returns Success(expectedAgentRoutingSpec)

        val result = agentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Success)
        assertEquals(expectedAgentRoutingSpec, result.getOrThrow())
        verify { agentRoutingSpecsResolver.resolve(context, input) }
    }

    @Test
    fun `resolve should return null AgentSpec`() {
        every { agentRoutingSpecsResolver.resolve(any(), any()) } returns Success(null)

        val result = agentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Success)
        assertNull(result.getOrNull())
        verify { agentRoutingSpecsResolver.resolve(context, input) }
    }

    @Test
    fun `resolve should throw AgentSpecResolverException`() {
        val exception = AgentRoutingSpecResolverException("Error")
        every { agentRoutingSpecsResolver.resolve(any(), any()) } returns Failure(exception)

        val result = agentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Failure)
        assertThrows<AgentRoutingSpecResolverException> { result.getOrThrow() }
        verify { agentRoutingSpecsResolver.resolve(context, input) }
    }

    @Test
    fun `resolve with filters should return AgentSpec`() {
        val expectedAgentRoutingSpec = mockk<AgentRoutingSpec>()
        every { agentRoutingSpecsResolver.resolve(any(), any(), any()) } returns Success(expectedAgentRoutingSpec)

        val result = agentRoutingSpecsResolver.resolve(filters, context, input)

        assertTrue(result is Success)
        assertEquals(expectedAgentRoutingSpec, result.getOrThrow())
        verify { agentRoutingSpecsResolver.resolve(filters, context, input) }
    }

    @Test
    fun `resolve with filters should return null AgentSpec`() {
        every { agentRoutingSpecsResolver.resolve(any(), any(), any()) } returns Success(null)

        val result = agentRoutingSpecsResolver.resolve(filters, context, input)

        assertTrue(result is Success)
        assertNull(result.getOrNull())
        verify { agentRoutingSpecsResolver.resolve(filters, context, input) }
    }

    @Test
    fun `resolve with filters should throw AgentSpecResolverException`() {
        val exception = AgentRoutingSpecResolverException("Error")
        every { agentRoutingSpecsResolver.resolve(any(), any(), any()) } returns Failure(exception)

        val result = agentRoutingSpecsResolver.resolve(filters, context, input)

        assertTrue(result is Failure)
        assertThrows<AgentRoutingSpecResolverException> { result.getOrThrow() }
        verify { agentRoutingSpecsResolver.resolve(filters, context, input) }
    }
}

// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException

class JsonAgentRoutingSpecsProviderTest {
    @Test
    fun `test provide with no filters`() {
        val jsonAgentRoutingSpecsProvider = JsonAgentRoutingSpecsProvider("src/test/resources/agentRoutingSpecs.json")
        val result = jsonAgentRoutingSpecsProvider.provide()
        assertTrue(result is Success)
        val agentRoutingSpecs = result.getOrNull()
        assertEquals(2, agentRoutingSpecs?.size)
    }

    @Test
    fun `test provide with filters`() {
        val jsonAgentRoutingSpecsProvider = JsonAgentRoutingSpecsProvider("src/test/resources/agentRoutingSpecs.json")
        val filter = mockk<SpecFilter>()
        every { filter.filter(any()) } answers {
            firstArg<Set<AgentRoutingSpec>>().filter { it.name == "agent1" }.toMutableSet()
        }

        val result = jsonAgentRoutingSpecsProvider.provide(setOf(filter))
        assertTrue(result is Success)
        val agentRoutingSpecs = result.getOrThrow()
        assertEquals(1, agentRoutingSpecs.size)
        assertEquals("agent1", agentRoutingSpecs.first().name)

        verify { filter.filter(any()) }
    }

    @Test
    fun `test provide when file reading fails`() {
        assertThrows(FileNotFoundException::class.java) {
            JsonAgentRoutingSpecsProvider("src/test/resources/invalid_file.json").provide()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `test provide when JSON parsing fails`() {
        assertThrows(MissingFieldException::class.java) {
            JsonAgentRoutingSpecsProvider("src/test/resources/invalid_agentRoutingSpecs.json").provide()
        }
    }
}

class SimpleAgentRoutingSpecProviderTest {
    private lateinit var simpleAgentSpecProvider: SimpleAgentRoutingSpecProvider

    @BeforeEach
    fun setUp() {
        simpleAgentSpecProvider =
            SimpleAgentRoutingSpecProvider(
                mutableSetOf(
                    AgentRoutingSpec(
                        "Agent1",
                        addresses = setOf(),
                        capabilities = setOf(),
                        description = "",
                        version = "",
                    ),
                    AgentRoutingSpec("Agent2", addresses = setOf(), capabilities = setOf(), description = "", version = ""),
                ),
            )
    }

    @Test
    fun `test provide with no filters`() {
        val result = simpleAgentSpecProvider.provide()
        assertTrue(result is Success)
        val agentSpecs = result.getOrNull()
        assertEquals(2, agentSpecs?.size)
    }

    @Test
    fun `test provide with filters`() {
        val filter = mockk<SpecFilter>()
        every { filter.filter(any()) } answers {
            firstArg<Set<AgentRoutingSpec>>().filter { it.name == "Agent1" }.toMutableSet()
        }

        val result = simpleAgentSpecProvider.provide(setOf(filter))
        assertTrue(result is Success)
        val agentSpecs = result.getOrNull()
        assertEquals(1, agentSpecs?.size)
        assertEquals("Agent1", agentSpecs?.first()?.name)

        verify { filter.filter(any()) }
    }

    @Test
    fun `test provide when filter throws exception`() {
        val filter = mockk<SpecFilter>()
        every { filter.filter(any()) } throws Exception("Filter failed")

        val result = simpleAgentSpecProvider.provide(setOf(filter))
        assertTrue(result is Failure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AgentRoutingSpecsProviderException)
        assertEquals("Failed to provide agent specs", exception?.message)
    }

    @Test
    fun `test add agent spec`() {
        val newAgentRoutingSpec =
            AgentRoutingSpec("Agent3", addresses = setOf(), capabilities = setOf(), description = "", version = "")
        simpleAgentSpecProvider.add(newAgentRoutingSpec)

        val result = simpleAgentSpecProvider.provide()
        assertTrue(result is Success)
        val agentSpecs = result.getOrNull()
        assertEquals(3, agentSpecs?.size)
        assertTrue(agentSpecs?.any { it.name == "Agent3" } == true)
    }
}

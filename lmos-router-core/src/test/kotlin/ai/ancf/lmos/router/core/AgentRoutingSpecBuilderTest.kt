// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AgentRoutingSpecBuilderTest {
    @Test
    fun `test build with valid data`() {
        val capability1 = Capability("capability1", "desc1", "1.0")
        val capability2 = Capability("capability2", "desc2", "1.1")
        val address1 = Address("http", "http://address1")

        val agentSpec =
            AgentRoutingSpecBuilder()
                .name("agent1")
                .description("agent description")
                .version("1.0")
                .address(address1)
                .addCapability(capability1)
                .addCapability(capability2)
                .build()

        assertEquals("agent1", agentSpec.name)
        assertEquals("agent description", agentSpec.description)
        assertEquals("1.0", agentSpec.version)
        assertTrue(agentSpec.capabilities.contains(capability1))
        assertTrue(agentSpec.capabilities.contains(capability2))
        assertTrue(agentSpec.addresses.contains(address1))
    }

    @Test
    fun `test build with missing name should throw exception`() {
        val address1 = Address("http", "http://address1")

        val exception =
            assertThrows<IllegalArgumentException> {
                AgentRoutingSpecBuilder()
                    .description("agent description")
                    .version("1.0")
                    .address(address1)
                    .build()
            }
        assertEquals("name cannot be blank", exception.message)
    }

    @Test
    fun `test build with missing version should throw exception`() {
        val address1 = Address("http", "http://address1")

        val exception =
            assertThrows<IllegalArgumentException> {
                AgentRoutingSpecBuilder()
                    .name("agent1")
                    .description("agent description")
                    .address(address1)
                    .build()
            }
        assertEquals("version cannot be blank", exception.message)
    }

    @Test
    fun `test build with missing address should throw exception`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                AgentRoutingSpecBuilder()
                    .name("agent1")
                    .description("agent description")
                    .version("1.0")
                    .build()
            }
        assertEquals("address cannot be empty", exception.message)
    }

    @Test
    fun `test build with blank fields`() {
        val address1 = Address("http", "http://address1")
        val exception =
            assertThrows<IllegalArgumentException> {
                AgentRoutingSpecBuilder()
                    .name("")
                    .description("agent description")
                    .version("1.0")
                    .address(address1)
                    .build()
            }
        assertEquals("name cannot be blank", exception.message)
    }

    @Test
    fun `test valid Capability creation with builder`() {
        val capability =
            CapabilitiesBuilder()
                .name("capability1")
                .description("description1")
                .version("1.0")
                .build()

        assertEquals("capability1", capability.name)
        assertEquals("description1", capability.description)
        assertEquals("1.0", capability.version)
    }

    @Test
    fun `test valid AgentSpec creation with multiple addresses`() {
        val capability = Capability("capability1", "desc1", "1.0")
        val address1 = Address("http", "http://address1")
        val address2 = Address("https", "https://address2")

        val agentSpec =
            AgentRoutingSpecBuilder()
                .name("agent1")
                .description("agent description")
                .version("1.0")
                .address(address1)
                .address(address2)
                .addCapability(capability)
                .build()

        assertEquals("agent1", agentSpec.name)
        assertEquals("agent description", agentSpec.description)
        assertEquals("1.0", agentSpec.version)
        assertEquals(2, agentSpec.addresses.size)
        assertTrue(agentSpec.addresses.contains(address1))
        assertTrue(agentSpec.addresses.contains(address2))
        assertTrue(agentSpec.capabilities.contains(capability))
    }
}

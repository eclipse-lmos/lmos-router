// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class NameSpecFilterTest {
    @Test
    @DisplayName("Test NameSpecFilter filters by name")
    fun testNameSpecFilterFiltersByName() {
        // Arrange
        val agentRoutingSpec1 = mockk<AgentRoutingSpec>()
        val agentRoutingSpec2 = mockk<AgentRoutingSpec>()
        val agentRoutingSpec3 = mockk<AgentRoutingSpec>()

        every { agentRoutingSpec1.name } returns "agentA"
        every { agentRoutingSpec2.name } returns "agentB"
        every { agentRoutingSpec3.name } returns "agentA"

        val filter = NameSpecFilter("agentA")

        val agentSpecs = setOf(agentRoutingSpec1, agentRoutingSpec2, agentRoutingSpec3)

        // Act
        val result = filter.filter(agentSpecs)

        // Assert
        val expected = setOf(agentRoutingSpec1, agentRoutingSpec3)
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("Test NameSpecFilter filters by name with no matches")
    fun testNameSpecFilterFiltersByNameWithNoMatches() {
        // Arrange
        val agentRoutingSpec1 = mockk<AgentRoutingSpec>()
        val agentRoutingSpec2 = mockk<AgentRoutingSpec>()

        every { agentRoutingSpec1.name } returns "agentA"
        every { agentRoutingSpec2.name } returns "agentB"

        val filter = NameSpecFilter("agentC")

        val agentSpecs = setOf(agentRoutingSpec1, agentRoutingSpec2)

        // Act
        val result = filter.filter(agentSpecs)

        // Assert
        val expected = emptySet<AgentRoutingSpec>()
        assertEquals(expected, result)
    }
}

class VersionSpecFilterTest {
    @Test
    @DisplayName("Test VersionSpecFilter filters by version")
    fun testVersionSpecFilterFiltersByVersion() {
        // Arrange
        val agentRoutingSpec1 = mockk<AgentRoutingSpec>()
        val agentRoutingSpec2 = mockk<AgentRoutingSpec>()
        val agentRoutingSpec3 = mockk<AgentRoutingSpec>()

        every { agentRoutingSpec1.version } returns "1.0.0"
        every { agentRoutingSpec2.version } returns "2.0.0"
        every { agentRoutingSpec3.version } returns "1.0.0"

        val filter = VersionSpecFilter("1.0.0")

        val agentSpecs = setOf(agentRoutingSpec1, agentRoutingSpec2, agentRoutingSpec3)

        // Act
        val result = filter.filter(agentSpecs)

        // Assert
        val expected = setOf(agentRoutingSpec1, agentRoutingSpec3)
        assertEquals(expected, result)
    }

    @Test
    @DisplayName("Test VersionSpecFilter filters by version with no matches")
    fun testVersionSpecFilterFiltersByVersionWithNoMatches() {
        // Arrange
        val agentRoutingSpec1 = mockk<AgentRoutingSpec>()
        val agentRoutingSpec2 = mockk<AgentRoutingSpec>()

        every { agentRoutingSpec1.version } returns "1.0.0"
        every { agentRoutingSpec2.version } returns "2.0.0"

        val filter = VersionSpecFilter("3.0.0")

        val agentSpecs = setOf(agentRoutingSpec1, agentRoutingSpec2)

        // Act
        val result = filter.filter(agentSpecs)

        // Assert
        val expected = emptySet<AgentRoutingSpec>()
        assertEquals(expected, result)
    }
}

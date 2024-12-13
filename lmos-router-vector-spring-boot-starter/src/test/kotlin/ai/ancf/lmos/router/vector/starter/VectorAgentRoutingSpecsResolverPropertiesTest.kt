// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector.starter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VectorAgentRoutingSpecsResolverPropertiesTest {
    @Test
    fun `test default property value`() {
        val properties = VectorAgentRoutingSpecsResolverProperties()
        assertEquals("", properties.specFilePath, "Default specFilePath should be an empty string")
    }

    @Test
    fun `test property value set correctly`() {
        val expectedPath = "/path/to/spec/file"
        val properties = VectorAgentRoutingSpecsResolverProperties(expectedPath)
        assertEquals(expectedPath, properties.specFilePath, "specFilePath should be set correctly")
    }
}

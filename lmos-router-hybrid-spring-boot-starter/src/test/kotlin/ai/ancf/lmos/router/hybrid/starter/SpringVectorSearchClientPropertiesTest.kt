// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SpringVectorSearchClientPropertiesTest {
    @Test
    fun `should initialize with default values`() {
        val properties = SpringVectorSearchClientProperties()

        assertEquals(0.5, properties.threshold)
        assertEquals(1, properties.topK)
    }

    @Test
    fun `should allow setting custom threshold value`() {
        val properties = SpringVectorSearchClientProperties(threshold = 0.75)

        assertEquals(0.75, properties.threshold)
    }

    @Test
    fun `should allow setting custom topK value`() {
        val properties = SpringVectorSearchClientProperties(topK = 5)

        assertEquals(5, properties.topK)
    }

    @Test
    fun `should allow setting both custom values`() {
        val properties = SpringVectorSearchClientProperties(threshold = 0.85, topK = 10)

        assertEquals(0.85, properties.threshold)
        assertEquals(10, properties.topK)
    }

    @Test
    fun `should throw an exception for invalid threshold`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                SpringVectorSearchClientProperties(threshold = -1.0)
            }
        assertEquals("threshold must be between 0.0 and 1.0", exception.message)
    }

    @Test
    fun `should throw an exception for invalid topK`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                SpringVectorSearchClientProperties(topK = -1)
            }
        assertEquals("topK must be a positive integer", exception.message)
    }
}

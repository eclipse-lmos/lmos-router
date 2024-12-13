// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid

import io.mockk.mockk
import org.eclipse.lmos.router.core.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NoOpModelToVectorQueryConverterTest {
    @Test
    fun `test convert method returns correct VectorSearchClientRequest`() {
        // Arrange
        val modelResponse = "test response"
        val context = mockk<Context>()
        val converter = NoOpModelToVectorQueryConverter()

        // Act
        val result = converter.convert(modelResponse, context)

        // Assert
        assertEquals(modelResponse, result.query)
        assertEquals(context, result.context)
    }
}

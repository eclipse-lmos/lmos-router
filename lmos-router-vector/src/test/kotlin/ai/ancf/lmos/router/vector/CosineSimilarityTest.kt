// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.sqrt

class CosineSimilarityTest {
    @Test
    fun `cosine similarity of vectors with different lengths throws exception`() {
        // Given
        val vectorA = listOf(1.0, 2.0, 3.0)
        val vectorB = listOf(4.0, 5.0)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                vectorA.cosineSimilarity(vectorB)
            }
        assertEquals("Vectors must be of the same length", exception.message)
    }

    @Test
    fun `cosine similarity of orthogonal vectors is zero`() {
        // Given
        val vectorA = listOf(1.0, 0.0)
        val vectorB = listOf(0.0, 1.0)

        // When
        val result = vectorA.cosineSimilarity(vectorB)

        // Then
        assertEquals(0.0, result, 1e-9)
    }

    @Test
    fun `cosine similarity of parallel vectors is one`() {
        // Given
        val vectorA = listOf(1.0, 1.0)
        val vectorB = listOf(2.0, 2.0)

        // When
        val result = vectorA.cosineSimilarity(vectorB)

        // Then
        assertEquals(1.0, result, 1e-9)
    }

    @Test
    fun `cosine similarity of zero vector and any vector is zero`() {
        // Given
        val vectorA = listOf(0.0, 0.0)
        val vectorB = listOf(1.0, 1.0)

        // When
        val result = vectorA.cosineSimilarity(vectorB)

        // Then
        assertEquals(0.0, result, 1e-9)
    }

    @Test
    fun `cosine similarity of two non-zero vectors`() {
        // Given
        val vectorA = listOf(1.0, 2.0, 3.0)
        val vectorB = listOf(4.0, -5.0, 6.0)

        // When
        val result = vectorA.cosineSimilarity(vectorB)

        // Then
        val expectedDotProduct = 1 * 4 + 2 * -5 + 3 * 6 // 4 - 10 + 18 = 12.0
        val expectedMagnitudeA = sqrt((1 * 1 + 2 * 2 + 3 * 3).toDouble()) // sqrt(1+4+9) = sqrt(14)
        val expectedMagnitudeB = sqrt((4 * 4 + -5 * -5 + 6 * 6).toDouble()) // sqrt(16+25+36) = sqrt(77)
        val expectedResult = expectedDotProduct / (expectedMagnitudeA * expectedMagnitudeB)

        assertEquals(expectedResult, result, 1e-9)
    }
}

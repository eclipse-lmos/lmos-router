// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import kotlin.math.sqrt

/**
 * Calculates the cosine similarity between two vectors.
 *
 * @param other The other vector.
 * @return The cosine similarity.
 */
fun List<Double>.cosineSimilarity(other: List<Double>): Double {
    require(this.size == other.size) { "Vectors must be of the same length" }

    val dotProduct = this.zip(other).sumOf { (a, b) -> a * b }
    val magnitudeA = sqrt(this.sumOf { it * it })
    val magnitudeB = sqrt(other.sumOf { it * it })

    return if (magnitudeA != 0.0 && magnitudeB != 0.0) {
        dotProduct / (magnitudeA * magnitudeB)
    } else {
        0.0 // If either vector is zero, the similarity is undefined; return 0.0
    }
}

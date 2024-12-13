// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for the SpringVectorSearchClient.
 *
 * @param threshold The similarity threshold.
 * @param topK The number of similar vectors to return.
 */
@ConfigurationProperties("route.llm.vector.search")
data class SpringVectorSearchClientProperties(
    var threshold: Double = 0.5,
    var topK: Int = 1,
) {
    init {
        require(threshold in 0.0..1.0) { "threshold must be between 0.0 and 1.0" }
        require(topK > 0) { "topK must be a positive integer" }
    }
}

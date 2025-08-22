// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.router.embedding")
data class EmbeddingDocumentProperties(
    val documentPath: String,
)

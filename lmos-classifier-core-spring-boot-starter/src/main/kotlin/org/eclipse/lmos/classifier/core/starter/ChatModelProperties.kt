// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.starter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lmos.router.llm")
data class ChatModelProperties(
    val provider: String,
    val apiKey: String? = null,
    val baseUrl: String? = null,
    val model: String,
    val maxTokens: Int = 2000,
    val temperature: Double = 0.0,
    val logRequestsAndResponses: Boolean = false,
    val maxChatHistory: Int = 10,
    val systemPrompt: String = "",
)

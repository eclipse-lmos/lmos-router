// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm.starter

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for the LLM agent resolver.
 *
 * The [specFilePath] field contains the path to the JSON file that contains the agent routing specifications.
 *
 * @param specFilePath The path to the JSON file that contains the agent routing specifications.
 */
@ConfigurationProperties(prefix = "route.agent.llm")
data class LLMAgentRoutingSpecsResolverProperties(
    var specFilePath: String = "",
)

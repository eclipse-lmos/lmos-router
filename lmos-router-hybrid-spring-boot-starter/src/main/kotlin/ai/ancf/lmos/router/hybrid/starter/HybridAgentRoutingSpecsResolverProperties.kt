// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for the VectorAgentRoutingSpecsResolver.
 *
 * @param specFilePath The path to the file containing the agent specs.
 */
@ConfigurationProperties(prefix = "route.agent.hybrid")
class HybridAgentRoutingSpecsResolverProperties(
    var specFilePath: String = "",
    var resolverPromptFilePath: String = "",
)

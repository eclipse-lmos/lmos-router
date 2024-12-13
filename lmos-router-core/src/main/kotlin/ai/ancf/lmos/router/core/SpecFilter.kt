// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

/**
 * This is a marker interface for filtering agent specs.
 */
interface SpecFilter {
    fun filter(agentRoutingSpecs: Set<AgentRoutingSpec>): Set<AgentRoutingSpec>
}

/**
 * This is a filter that filters agent specs by name.
 */
class NameSpecFilter(private val value: String) : SpecFilter {
    override fun filter(agentRoutingSpecs: Set<AgentRoutingSpec>): Set<AgentRoutingSpec> {
        return agentRoutingSpecs.filter { it.name == value }.toSet()
    }
}

/**
 * This is a filter that filters agent specs by version.
 */
class VersionSpecFilter(private val value: String) : SpecFilter {
    override fun filter(agentRoutingSpecs: Set<AgentRoutingSpec>): Set<AgentRoutingSpec> {
        return agentRoutingSpecs.filter { it.version == value }.toSet()
    }
}

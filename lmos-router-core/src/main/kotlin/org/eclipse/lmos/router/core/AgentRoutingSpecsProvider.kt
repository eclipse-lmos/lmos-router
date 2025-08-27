// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import kotlinx.serialization.json.Json
import java.io.File

/**
 * Base interface for providing agent routing specifications.
 *
 * The [provide] method returns a set of agent routing specifications.
 * The [provide] method with filters returns a set of agent routing specifications that match the filters.
 *
 * @see AgentRoutingSpec
 * @see SpecFilter
 */
interface AgentRoutingSpecsProvider {
    fun provide(): Result<Set<AgentRoutingSpec>, AgentRoutingSpecsProviderException> = provide(emptySet())

    fun provide(filters: Set<SpecFilter>): Result<Set<AgentRoutingSpec>, AgentRoutingSpecsProviderException>
}

/**
 * Exception thrown when an error occurs while providing agent routing specifications.
 *
 * @param msg The error message.
 * @param cause The cause of the exception.
 */
class AgentRoutingSpecsProviderException(
    msg: String,
    cause: Exception? = null,
) : Exception(msg, cause)

/**
 * A provider that reads agent routing specifications from a JSON file.
 *
 * @param jsonFilePath The path to the JSON file containing the agent routing specifications.
 */
class JsonAgentRoutingSpecsProvider(
    jsonFilePath: String,
) : AgentRoutingSpecsProvider {
    private val agentRoutingSpecs = mutableSetOf<AgentRoutingSpec>()

    init {
        val jsonFile = File(jsonFilePath)
        val json = jsonFile.readText()
        val agentRoutingSpecs = Json.decodeFromString<Set<AgentRoutingSpec>>(json)
        this.agentRoutingSpecs.addAll(agentRoutingSpecs)
    }

    override fun provide(filters: Set<SpecFilter>): Result<Set<AgentRoutingSpec>, AgentRoutingSpecsProviderException> =
        try {
            Success(filters.fold(agentRoutingSpecs) { acc, specFilter -> specFilter.filter(acc).toMutableSet() })
        } catch (e: Exception) {
            Failure(AgentRoutingSpecsProviderException("Failed to provide agent specs", e))
        }
}

class SimpleAgentRoutingSpecProvider() : AgentRoutingSpecsProvider {
    private var agentRoutingSpecs: MutableSet<AgentRoutingSpec> = mutableSetOf()

    constructor(agentRoutingSpecs: MutableSet<AgentRoutingSpec>) : this() {
        this.agentRoutingSpecs = agentRoutingSpecs
    }

    override fun provide(filters: Set<SpecFilter>): Result<Set<AgentRoutingSpec>, AgentRoutingSpecsProviderException> =
        try {
            Success(filters.fold(agentRoutingSpecs) { acc, specFilter -> specFilter.filter(acc).toMutableSet() })
        } catch (e: Exception) {
            Failure(AgentRoutingSpecsProviderException("Failed to provide agent specs", e))
        }

    fun add(agentRoutingSpec: AgentRoutingSpec): SimpleAgentRoutingSpecProvider {
        agentRoutingSpecs.add(agentRoutingSpec)
        return this
    }
}

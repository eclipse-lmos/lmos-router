// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import kotlinx.serialization.Serializable

/**
 * Represents the routing specification of an agent.
 * The name field specifies the name of the agent.
 * The description field specifies a description of the agent.
 * The version field specifies the version of the agent.
 * The capabilities field specifies the capabilities of the agent.
 * The addresses field specifies the addresses of the agent.
 */
@Serializable
open class AgentRoutingSpec(
    val name: String,
    val description: String,
    val version: String,
    val capabilities: Set<Capability>,
    val addresses: Set<Address>,
)

/**
 * Represents the capabilities of an agent.
 * The name field specifies the name of the capability.
 * The description field specifies a description of the capability.
 * The version field specifies the version of the capability.
 */
@Serializable
open class Capability(
    val name: String,
    val description: String,
    val version: String,
)

/**
 * Represents the address of an agent.
 * The address is a URI that can be used to communicate with the agent.
 * The protocol field specifies the protocol to use when communicating with the agent.
 */
@Serializable
open class Address(
    val protocol: String = "http",
    val uri: String,
)

/**
 * Represents a builder for creating an agent's capabilities.
 */
class CapabilitiesBuilder {
    private var name: String = ""
    private var description: String = ""
    private var version: String = ""

    fun name(name: String) = apply { this.name = name }

    fun description(description: String) = apply { this.description = description }

    fun version(version: String) = apply { this.version = version }

    fun build(): Capability = Capability(name, description, version)
}

/**
 * Represents a builder for creating an agent specification.
 */
class AgentRoutingSpecBuilder {
    private var name: String = ""
    private var description: String = ""
    private var version: String = ""
    private var capabilities: MutableSet<Capability> = mutableSetOf()
    private var address: MutableSet<Address> = mutableSetOf()

    fun name(name: String) = apply { this.name = name }

    fun description(description: String) = apply { this.description = description }

    fun version(version: String) = apply { this.version = version }

    fun address(address: Address) = apply { this.address.add(address) }

    fun addCapability(capability: Capability) = apply { this.capabilities.add(capability) }

    fun build(): AgentRoutingSpec {
        require(name.isNotBlank()) { "name cannot be blank" }
        require(version.isNotBlank()) { "version cannot be blank" }
        require(address.isNotEmpty()) { "address cannot be empty" }

        return AgentRoutingSpec(name, description, version, capabilities, address)
    }
}

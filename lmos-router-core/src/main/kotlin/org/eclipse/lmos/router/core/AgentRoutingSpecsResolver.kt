// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

/**
 * Interface for resolving agent routing specifications.
 *
 * The [resolve] method returns an agent specification based on the context and input.
 * The [resolve] method with filters returns an agent specification after applying the filters and resolving based on the context and input.
 *
 * @see AgentRoutingSpec
 * @see SpecFilter
 */
interface AgentRoutingSpecsResolver {
    val agentRoutingSpecsProvider: AgentRoutingSpecsProvider

    fun resolve(
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException>

    fun resolve(
        filters: Set<SpecFilter>,
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException>
}

/**
 * Exception thrown when an agent spec resolver fail.
 *
 * @param msg The error message.
 * @param cause The cause of the exception.
 */
open class AgentRoutingSpecResolverException(
    msg: String,
    cause: Exception? = null,
) : Exception(msg, cause)

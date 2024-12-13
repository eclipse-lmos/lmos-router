// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import org.eclipse.lmos.router.core.*

/**
 * An implementation of AgentSpecResolver that resolves agent specs using a vector similarity search.
 *
 * @param agentRoutingSpecsProvider The agent specs provider.
 * @param vectorSearchClient The vector search client.
 */
open class VectorAgentRoutingSpecsResolver(
    override val agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
    private val vectorSearchClient: VectorSearchClient =
        DefaultVectorClient(
            DefaultVectorClientProperties(
                seedJsonFilePath = System.getenv("VECTOR_SEED_JSON_FILE_PATH"),
            ),
        ),
) : AgentRoutingSpecsResolver {
    override fun resolve(
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> {
        return resolve(setOf(), context, input)
    }

    override fun resolve(
        filters: Set<SpecFilter>,
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> {
        return try {
            val agentSpecs = agentRoutingSpecsProvider.provide(filters).getOrThrow()
            val result =
                vectorSearchClient.find(
                    VectorSearchClientRequest(input.content, context),
                    agentSpecs,
                ).getOrThrow()

            Success(agentSpecs.firstOrNull { it.name == result?.agentName })
        } catch (e: Exception) {
            Failure(AgentRoutingSpecResolverException("Failed to resolve agent spec", e))
        }
    }
}

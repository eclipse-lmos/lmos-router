// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import org.junit.jupiter.api.Test

class SampleFlow {
    @Test
    fun sampleFlow() {
        // Define the specs collection builder
        val jsonAgentRoutingSpecsBuilder = JsonAgentRoutingSpecsProvider("src/test/resources/agentRoutingSpecs.json")

        // Define the agent resolver
        val simpleAgentResolver =
            object : AgentRoutingSpecsResolver {
                override val agentRoutingSpecsProvider: AgentRoutingSpecsProvider = jsonAgentRoutingSpecsBuilder

                override fun resolve(
                    context: Context,
                    input: UserMessage,
                ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> =
                    when (val specs = agentRoutingSpecsProvider.provide()) {
                        is Success -> {
                            val agentRoutingSpec = specs.value.firstOrNull()
                            Success(agentRoutingSpec)
                        }

                        is Failure -> Failure(AgentRoutingSpecResolverException("No agent spec found", specs.reason))
                    }

                override fun resolve(
                    filters: Set<SpecFilter>,
                    context: Context,
                    input: UserMessage,
                ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> =
                    when (val specs = agentRoutingSpecsProvider.provide(filters)) {
                        is Success -> {
                            val agentRoutingSpec = specs.value.firstOrNull()
                            Success(agentRoutingSpec)
                        }

                        is Failure -> Failure(AgentRoutingSpecResolverException("No agent spec found", specs.reason))
                    }
            }

        // Assert the agentSpecs collection is loaded and the agent resolver works
        assert(jsonAgentRoutingSpecsBuilder.provide().getOrNull()?.size == 2)
        assert(
            simpleAgentResolver
                .resolve(Context(listOf()), UserMessage("Hello"))
                .getOrNull()
                ?.name
                ?.isNotBlank() == true,
        )
    }

    @Test
    fun `filter agent specs by name`() {
        // Define the specs collection builder
        val jsonAgentSpecsBuilder = JsonAgentRoutingSpecsProvider("src/test/resources/agentRoutingSpecs.json")

        // Define the agent resolver
        val simpleAgentResolver =
            object : AgentRoutingSpecsResolver {
                override val agentRoutingSpecsProvider: AgentRoutingSpecsProvider = jsonAgentSpecsBuilder

                override fun resolve(
                    context: Context,
                    input: UserMessage,
                ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> =
                    when (val specs = agentRoutingSpecsProvider.provide(setOf(NameSpecFilter("agent1")))) {
                        is Success -> {
                            val agentRoutingSpec = specs.value.firstOrNull()
                            Success(agentRoutingSpec)
                        }

                        is Failure -> Failure(AgentRoutingSpecResolverException("No agent spec found", specs.reason))
                    }

                override fun resolve(
                    filters: Set<SpecFilter>,
                    context: Context,
                    input: UserMessage,
                ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> =
                    when (val specs = agentRoutingSpecsProvider.provide(filters)) {
                        is Success -> {
                            val agentSpec = specs.value.firstOrNull()
                            Success(agentSpec)
                        }

                        is Failure -> Failure(AgentRoutingSpecResolverException("No agent spec found", specs.reason))
                    }
            }

        // Assert the agentSpecs collection is loaded and the agent resolver works
        assert(jsonAgentSpecsBuilder.provide().getOrNull()?.size == 2)
        assert(simpleAgentResolver.resolve(Context(listOf()), UserMessage("Hello")).getOrNull()?.name == "agent1")
    }

    @Test
    fun `simple agent spec provider test`() {
        val agentSpecsProvider =
            SimpleAgentRoutingSpecProvider()
                .add(
                    AgentRoutingSpecBuilder()
                        .name("agent1")
                        .description("agent1 description")
                        .version("1.0.0")
                        .address(Address(uri = "http://localhost:8080"))
                        .addCapability(
                            CapabilitiesBuilder()
                                .name("capability1")
                                .description("capability1 description")
                                .version("1.0.0")
                                .build(),
                        ).addCapability(
                            CapabilitiesBuilder()
                                .name("capability2")
                                .description("capability2 description")
                                .version("1.0.0")
                                .build(),
                        ).build(),
                ).add(
                    AgentRoutingSpecBuilder()
                        .name("agent2")
                        .description("agent2 description")
                        .version("1.1.0")
                        .address(Address(uri = "http://localhost:8080"))
                        .build(),
                )

        val agentSpec = agentSpecsProvider.provide().getOrNull()?.find { agentSpec -> agentSpec.name == "agent1" }
        assert(agentSpecsProvider.provide().getOrNull()?.size == 2)
        assert(agentSpec?.name == "agent1")
        assert(agentSpec?.capabilities?.last()?.name == "capability2")
    }
}

// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.eclipse.lmos.router.core.*
import org.slf4j.LoggerFactory

/**
 * An agent spec resolver that uses a language model to resolve agent specs.
 *
 * @param agentRoutingSpecsProvider The provider of agent routing specifications.
 * @param modelPromptProvider The provider of model prompts.
 * @param modelClient The client for the language model.
 * @param serializer The JSON serializer.
 */
class LLMAgentRoutingSpecsResolver(
    override val agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
    private val modelPromptProvider: ModelPromptProvider = DefaultModelPromptProvider(),
    private val modelClient: ModelClient =
        DefaultModelClient(
            DefaultModelClientProperties(
                openAiApiKey =
                    System.getenv(
                        "OPENAI_API_KEY",
                    ),
            ),
        ),
    private val serializer: Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        },
) : AgentRoutingSpecsResolver {
    private val log = LoggerFactory.getLogger(LLMAgentRoutingSpecsResolver::class.java)

    override fun resolve(
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> {
        return resolve(emptySet(), context, input)
    }

    override fun resolve(
        filters: Set<SpecFilter>,
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> {
        try {
            log.trace("Resolving agent spec")
            val agentRoutingSpecs = agentRoutingSpecsProvider.provide(filters).getOrThrow()

            log.trace("Fetching agent spec prompt")
            val prompt = modelPromptProvider.providePrompt(context, agentRoutingSpecs, input)

            val messages = mutableListOf<ChatMessage>()
            messages.add(SystemMessage(prompt.getOrThrow()))
            messages.addAll(context.previousMessages)
            messages.add(input)

            log.trace("Fetching agent spec completion")
            val response: String = modelClient.call(messages).getOrThrow().content
            val agent: ModelClientResponse = serializer.decodeFromString(serializer(), response)

            log.trace("Agent resolved: ${agent.agentName}")
            return Success(agentRoutingSpecs.firstOrNull { it.name == agent.agentName })
        } catch (e: Exception) {
            log.error("Failed to resolve agent spec", e)
            return Failure(AgentRoutingSpecResolverException("Failed to resolve agent spec", e))
        }
    }
}

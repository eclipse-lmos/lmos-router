// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.lmos.router.core.*

/**
 * Represents a model prompt provider.
 */
interface ModelPromptProvider {
    fun providePrompt(
        context: Context,
        agentRoutingSpecs: Set<AgentRoutingSpec>,
        input: UserMessage,
    ): Result<String, AgentRoutingSpecResolverException>
}

/**
 * Default implementation of [ModelPromptProvider] that provides a generic prompt for agent resolution.
 * The prompt includes instructions for selecting the most suitable agent based on the agents' capabilities.
 * This prompt is intended to be used as a fallback when a custom prompt is not provided.
 * This prompt requires json mode to be enabled.
 */
class DefaultModelPromptProvider : ModelPromptProvider {
    override fun providePrompt(
        context: Context,
        agentRoutingSpecs: Set<AgentRoutingSpec>,
        input: UserMessage,
    ): Result<String, AgentRoutingSpecResolverException> =
        Success(
            """
            You are an AI tasked with selecting the most suitable agent to address a user query based on the agents' capabilities. 
            You will be provided with a list of agents and their capabilities, followed by a user query. 
            Your goal is to analyze the query and match it with the most appropriate agent.

            First, here is the list of agents and their capabilities:

            ${generateAgentRoutingSpecsXml(agentRoutingSpecs)}

            To select the most suitable agent, follow these steps:

            1. Carefully read and understand the user query.
            2. Review the list of agents and their capabilities.
            3. Analyze how well each agent's capabilities match the requirements of the user query.
            4. Consider factors such as relevance, expertise, and specificity of the agent's capabilities in relation to the query.
            5. Select the agent whose capabilities best align with the user's needs.

            Once you have determined the most suitable agent, provide your answer in the following JSON format:

            <answer>
            ```json
            {"agentName": "name-of-agent"}
            ```
            </answer>

            Ensure that the agent name you provide exactly matches the name given in the agents list. 
            Do not include any additional explanation or justification in your response; only provide the JSON object as specified.
            """.trimIndent(),
        )
}

class ExternalModelPromptProvider(
    private val promptFilePath: String,
    private val agentRoutingSpecsListType: AgentRoutingSpecListType = AgentRoutingSpecListType.XML,
) : ModelPromptProvider {
    override fun providePrompt(
        context: Context,
        agentRoutingSpecs: Set<AgentRoutingSpec>,
        input: UserMessage,
    ): Result<String, AgentRoutingSpecResolverException> =
        try {
            var prompt = java.io.File(promptFilePath).readText()
            val agentRoutingSpecsString =
                when (agentRoutingSpecsListType) {
                    AgentRoutingSpecListType.XML -> generateAgentRoutingSpecsXml(agentRoutingSpecs)
                    AgentRoutingSpecListType.JSON -> generateAgentRoutingSpecsJson(agentRoutingSpecs)
                }
            prompt = prompt.replace("\${" + agentRoutingSpecsListType.keyword + "}", agentRoutingSpecsString)
            Success(prompt)
        } catch (e: Exception) {
            Failure(AgentRoutingSpecResolverException("Failed to read prompt file: $promptFilePath", e))
        }
}

enum class AgentRoutingSpecListType(
    val keyword: String,
) {
    XML("agents_list_xml"),
    JSON("agents_list_json"),
}

fun generateAgentRoutingSpecsXml(agentRoutingSpecs: Set<AgentRoutingSpec>): String =
    buildString {
        appendLine("<agents_list>")
        agentRoutingSpecs.forEach { agentRoutingSpec ->
            appendLine("<agent>")
            appendLine("<name>${agentRoutingSpec.name}</name>")
            appendLine("<description>${agentRoutingSpec.description}</description>")
            appendLine("<capabilities>")
            agentRoutingSpec.capabilities.forEach { capability ->
                appendLine("<capability>")
                appendLine("<name>${capability.name}</name>")
                appendLine("<description>${capability.description}</description>")
                appendLine("</capability>")
            }
            appendLine("</capabilities>")
            appendLine("</agent>")
        }
        appendLine("</agents_list>")
    }

fun generateAgentRoutingSpecsJson(agentRoutingSpecs: Set<AgentRoutingSpec>) = Json.encodeToString(agentRoutingSpecs)

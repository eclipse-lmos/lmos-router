// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core

/**
 * Marker interface for the classification of the most appropriate agent based on a given request.
 */
interface AgentClassifier {
    /**
     * Classifies the given request and returns a result containing the selected agent.
     *
     * @param request The classification request.
     * @return The classification result.
     */
    fun classify(request: ClassificationRequest): ClassificationResult
}

/**
 * The classification request containing contextual information of a conversation
 * to select the most appropriate agent.
 *
 * @property inputContext Input context used for classification.
 * @property systemContext System context used for classification.
 */
data class ClassificationRequest(
    val inputContext: InputContext,
    val systemContext: SystemContext,
)

/**
 * Provides the context of a conversation used for classification.
 *
 * @property userMessage The last user message of the conversation.
 * @property historyMessages An optional list of messages representing the conversation history,
 * including user and assistant messages, providing additional context for classification.
 * @property metadata Optional metadata with additional contextual information related to the input context.
 */
data class InputContext(
    val userMessage: String,
    val historyMessages: List<HistoryMessage> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
)

/**
 * Provides the context of the system's tenant and channel used for classification.
 *
 * @property tenantId The identifier for the tenant.
 * @property channelId The identifier for the channel.
 * @property channelId The identifier for the subset, default is 'stable'.
 */
data class SystemContext(
    val tenantId: String,
    val channelId: String,
    val subset: String = "stable",
)

/**
 * Represents the result of an agent classification process.
 *
 * @property classifiedAgents The agents that were classified as the best matches for a given [ClassificationRequest].
 * @property candidateAgents The agents considered during classification, depending on the classification strategy:
 * - **Embedding-based classifier**; candidates originate from semantic search.
 * - **LLM-based classifier**; candidates derived from [AgentProvider]s.
 */
open class ClassificationResult(
    var classifiedAgents: List<ClassifiedAgent>,
    val candidateAgents: List<Agent> = emptyList(),
)

/**
 * Represents a classified agent that has been selected as a result of the classification process.
 *
 * @property id ID of the agent.
 * @property name Name of the agent.
 * @property address Address of the agent, typically used for routing or invocation.
 */
data class ClassifiedAgent(
    val id: String,
    val name: String,
    val address: String,
)

/**
 * Represents a single message in the conversation history.
 *
 * @property role The role of the message.
 * @property content The content of the message.
 */
data class HistoryMessage(
    val role: HistoryMessageRole,
    val content: String,
)

/**
 * Defines the possible roles of a message in the conversation history.
 */
enum class HistoryMessageRole {
    /** Represents a message sent by the user. */
    USER,

    /** Represents a message sent by the assistant. */
    ASSISTANT,
}

/**
 * Represents an agent with a unique identifier and a set of capabilities.
 *
 * @property id The unique identifier of the agent.
 * @property name The name of the agent.
 * @property address The address of the agent.
 * @property capabilities The list of capabilities.
 */
data class Agent(
    val id: String,
    val name: String,
    val address: String,
    val capabilities: List<Capability>,
)

/**
 * Represents a specific capability of an agent.
 *
 * @property id The unique identifier of the capability.
 * @property description A textual description of what the capability entails.
 * @property examples Examples illustrating the capability.
 * */
data class Capability(
    val id: String,
    val description: String,
    val examples: List<String> = emptyList(),
)

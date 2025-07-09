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
 * The classification request containing context information for selecting the most appropriate agent.
 *
 * @property inputContext Input context used for classification.
 * @property systemContext System context used for classification.
 */
data class ClassificationRequest(
    val inputContext: InputContext,
    val systemContext: SystemContext,
)

/**
 * Input context used for classification.
 *
 * @property userMessage The last user message to be classified.
 * @property historyMessages An optional list of history messages, providing additional context for classification.
 * @property agents An optional list of agents, providing additional context when purely LLM classification is used.
 */
data class InputContext(
    val userMessage: String,
    val historyMessages: List<HistoryMessage> = emptyList(),
    val agents: List<Agent> = emptyList(),
)

/**
 * System context used for classification.
 *
 * @property tenantId The identifier for the tenant.
 * @property channelId The identifier for the channel.
 */
data class SystemContext(
    val tenantId: String,
    val channelId: String,
)

/**
 *  Classification result containing the classified agents.
 *
 * @property agents The classified agents.
 * @property topRankedEmbeddings Optional list of top ranked agents returned from a semantic (vector-based) search.
 * This field is only populated when the classification includes embedding-based retrieval.
 */
open class ClassificationResult(
    var agents: List<String>,
    val topRankedEmbeddings: List<Agent> = emptyList(),
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
 * @property capabilities The list of capabilities.
 */
data class Agent(
    val id: String,
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

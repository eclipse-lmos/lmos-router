// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.classifier.core.ClassifiedAgent
import org.eclipse.lmos.classifier.core.HistoryMessageRole.*
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.slf4j.LoggerFactory

class DefaultModelAgentClassifier(
    private val chatModel: ChatModel,
    private val systemPrompt: String,
) : ModelAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val messages = mutableListOf<ChatMessage>()
        messages.add(prepareSystemMessage(request))
        messages.addAll(prepareHistoryMessages(request))
        messages.add(prepareUserMessage(request))

        val response = chatModel.chat(messages)
        val classificationResult = prepareResponse(request, response)
        logger.info(
            "[${javaClass.simpleName}] Classified agent '${classificationResult.agents}' for query '${request.inputContext.userMessage}'.",
        )
        return classificationResult
    }

    private fun prepareSystemMessage(query: ClassificationRequest) =
        SystemMessage(
            systemPrompt.replace(
                "{{agents}}",
                query.inputContext.agents.joinToString("\n\n") { agent ->
                    String.format(
                        "Agent '%s':\n - %s",
                        agent.id,
                        agent.capabilities.joinToString("\n - ") { capability -> capability.description },
                    )
                },
            ),
        )

    private fun prepareHistoryMessages(query: ClassificationRequest) =
        query.inputContext.historyMessages.map {
            when (it.role) {
                USER -> UserMessage(it.content)
                ASSISTANT -> AiMessage(it.content)
            }
        }

    private fun prepareUserMessage(request: ClassificationRequest) = UserMessage(request.inputContext.userMessage)

    private fun prepareResponse(
        request: ClassificationRequest,
        chatResponse: ChatResponse,
    ): ClassificationResult {
        val responseMessage = chatResponse.aiMessage()?.text()
        logger.info("[${javaClass.simpleName}] LLM response: $responseMessage")
        val agentId = extractAgentId(responseMessage)
        val agent = request.inputContext.agents.find { it.id == agentId }
        return if (agent == null) {
            ClassificationResult(emptyList())
        } else {
            ClassificationResult(listOf(ClassifiedAgent(agent.id, agent.name, agent.address)))
        }
    }

    companion object {
        fun builder(): ModelAgentClassifierBuilder = ModelAgentClassifierBuilder()
    }
}

/**
 * Extracts the agent ID from a ChatResponse.
 *
 * This function takes the text of the AI message, trims leading and trailing whitespace,
 * removes all characters that are not letters, digits, underscores, or hyphens,
 * and returns the cleaned ID if it is not empty or "null".
 *
 * The regex "[^\\w-]" means:
 *   - [^ ... ] : Negation, i.e., matches any character NOT in the following group
 *   - \\w     : Word character (letters, digits, underscore)
 *   - -       : Hyphen
 * So the regex removes all characters except letters, digits, underscore, and hyphen.
 *
 * @param chatResponse The response from the chat model
 * @return The extracted agent ID or null if no valid ID is found
 */
fun extractAgentId(chatResponse: String?): String? {
    val agentId =
        chatResponse
            ?.trim()
            ?.replace(Regex("[^\\w-]"), "")
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    return agentId
}

class ModelAgentClassifierBuilder {
    private var model: ChatModel? = null
    private var systemPrompt: String? = null

    fun withChatModel(model: ChatModel) =
        apply {
            this.model = model
        }

    fun withSystemPrompt(systemPrompt: String) =
        apply {
            this.systemPrompt = systemPrompt
        }

    fun build(): DefaultModelAgentClassifier {
        if (model == null) throw IllegalStateException("ChatModel must be set")
        if (systemPrompt.isNullOrEmpty()) systemPrompt = defaultSystemPrompt()
        return DefaultModelAgentClassifier(model!!, systemPrompt!!)
    }
}

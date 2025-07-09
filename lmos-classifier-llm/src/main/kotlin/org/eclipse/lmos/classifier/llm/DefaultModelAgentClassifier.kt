// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
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
        val classificationResult = prepareResponse(response.aiMessage().text())
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

    private fun prepareResponse(agentId: String?) =
        ClassificationResult(if (agentId == null || agentId == "null") emptyList() else listOf(agentId), emptyList())

    companion object {
        fun builder(): ModelAgentClassifierBuilder = ModelAgentClassifierBuilder()
    }
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

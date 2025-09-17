// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.service.output.JsonSchemas
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.classifier.core.ClassifiedAgent
import org.eclipse.lmos.classifier.core.HistoryMessageRole.*
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.slf4j.LoggerFactory

class DefaultModelAgentClassifier(
    private val chatModel: ChatModel,
    private val systemPrompt: String,
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) : ModelAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun classify(request: ClassificationRequest): ClassificationResult {
        val chatRequest = prepareChatRequest(request)
        val chatResponse = chatModel.chat(chatRequest)
        val classificationResult = prepareClassificationResult(chatResponse, request.inputContext.agents)
        logger.info(
            "[${javaClass.simpleName}] Classified agent '${classificationResult.agents}' for query '${request.inputContext.userMessage}'.",
        )
        return classificationResult
    }

    private fun prepareChatRequest(request: ClassificationRequest): ChatRequest {
        val responseFormat =
            ResponseFormat
                .builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchemas.jsonSchemaFrom(ClassifiedAgentResult::class.java).get())
                .build()

        val messages = mutableListOf<ChatMessage>()
        messages.add(prepareSystemMessage(request))
        messages.addAll(prepareHistoryMessages(request))
        messages.add(prepareUserMessage(request))

        val chatRequest =
            ChatRequest
                .builder()
                .responseFormat(responseFormat)
                .messages(messages)
                .build()
        return chatRequest
    }

    private fun prepareSystemMessage(query: ClassificationRequest): SystemMessage {
        val agents =
            query.inputContext.agents.map { agent ->
                AgentDescription(
                    agentId = agent.id,
                    descriptions = agent.capabilities.map { it.description },
                )
            }
        val json = objectMapper.writeValueAsString(agents)
        return SystemMessage(
            systemPrompt.replace("{{agents}}", json),
        )
    }

    private fun prepareHistoryMessages(query: ClassificationRequest) =
        query.inputContext.historyMessages.map {
            when (it.role) {
                USER -> UserMessage(it.content)
                ASSISTANT -> AiMessage(it.content)
            }
        }

    private fun prepareUserMessage(request: ClassificationRequest) = UserMessage(request.inputContext.userMessage)

    private fun prepareClassificationResult(
        chatResponse: ChatResponse,
        agents: List<Agent>,
    ): ClassificationResult {
        val rawResponse = chatResponse.aiMessage().text()
        val response = objectMapper.readValue<ClassifiedAgentResult>(rawResponse)
        val agent = agents.find { it.id == response.agentId }
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

data class ClassifiedAgentResult(
    val agentId: String?,
    val reason: String,
)

data class AgentDescription(
    val agentId: String,
    val descriptions: List<String>,
)

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

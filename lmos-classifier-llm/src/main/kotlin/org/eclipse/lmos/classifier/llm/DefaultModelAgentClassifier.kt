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
import org.eclipse.lmos.classifier.core.llm.AgentAggregator
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.eclipse.lmos.classifier.core.llm.SystemPromptRenderer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultModelAgentClassifier(
    private val chatModel: ChatModel,
    private val systemPromptTemplate: String,
    private val systemPromptRenderer: SystemPromptRenderer,
    private val agentAggregator: AgentAggregator,
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) : ModelAgentClassifier {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val responseFormat =
        ResponseFormat
            .builder()
            .type(ResponseFormatType.JSON)
            .jsonSchema(JsonSchemas.jsonSchemaFrom(ClassifiedAgentResult::class.java).get())
            .build()

    override fun classify(request: ClassificationRequest) = doClassify(request, emptyList())

    override fun classify(
        request: ClassificationRequest,
        agents: List<Agent>,
    ) = doClassify(request, agents)

    private fun doClassify(
        request: ClassificationRequest,
        agents: List<Agent>,
    ): ClassificationResult {
        val candidateAgents = agentAggregator.aggregate(request) + agents
        val chatRequest = prepareChatRequest(request, candidateAgents)
        val chatResponse = chatModel.chat(chatRequest)
        val classificationResult = prepareClassificationResult(chatResponse, candidateAgents)
        logger.logClassificationResult(request, classificationResult)
        return classificationResult
    }

    private fun prepareChatRequest(
        request: ClassificationRequest,
        agents: List<Agent>,
    ): ChatRequest {
        val messages = mutableListOf<ChatMessage>()
        messages.add(prepareSystemMessage(agents))
        messages.addAll(prepareHistoryMessages(request))
        messages.add(prepareUserMessage(request))

        return ChatRequest
            .builder()
            .responseFormat(responseFormat)
            .messages(messages)
            .build()
    }

    private fun prepareSystemMessage(agents: List<Agent>): SystemMessage {
        val prompt = systemPromptRenderer.render(systemPromptTemplate, mapOf("agents" to agents))
        return SystemMessage(prompt)
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
            ClassificationResult(emptyList(), agents)
        } else {
            ClassificationResult(listOf(ClassifiedAgent(agent.id, agent.name, agent.address)), agents)
        }
    }

    private fun Logger.logClassificationResult(
        request: ClassificationRequest,
        result: ClassificationResult,
    ) {
        val candidateAgents =
            result.candidateAgents.map { agent ->
                LlmCandidateAgent(agent.id, agent.capabilities.map { cap -> LlmCandidateCapability(cap.id, cap.description) })
            }
        val classifiedAgentId = result.candidateAgents.firstOrNull()?.id ?: "none"
        this
            .atInfo()
            .addKeyValue("classifier-type", "LLM")
            .addKeyValue("classifier-user-message", request.inputContext.userMessage)
            .addKeyValue("classifier-candidate-agents", candidateAgents)
            .addKeyValue("classifier-selected-agent", classifiedAgentId)
            .addKeyValue("event", "CLASSIFICATION_LLM_DONE")
            .log(
                "Executed classification using the LLM. Query: '{}', classified agent: '{}'.",
                request.inputContext.userMessage,
                classifiedAgentId,
            )
    }

    companion object {
        fun builder(): ModelAgentClassifierBuilder = ModelAgentClassifierBuilder()
    }
}

data class ClassifiedAgentResult(
    val agentId: String?,
    val reason: String,
)

data class LlmCandidateAgent(
    val id: String,
    val capabilities: List<LlmCandidateCapability>,
)

data class LlmCandidateCapability(
    val id: String,
    val description: String,
)

class ModelAgentClassifierBuilder {
    private var model: ChatModel? = null
    private var systemPromptTemplate = defaultSystemPrompt()
    private var agentProviders: List<AgentProvider> = emptyList()

    fun withChatModel(model: ChatModel) =
        apply {
            this.model = model
        }

    fun withSystemPromptTemplate(systemPromptTemplate: String) =
        apply {
            if (systemPromptTemplate.isNotEmpty()) this.systemPromptTemplate = systemPromptTemplate
        }

    fun withAgentProviders(providers: List<AgentProvider>) =
        apply {
            agentProviders = providers
        }

    fun build(): DefaultModelAgentClassifier {
        if (model == null) throw IllegalStateException("ChatModel must be set")
        return DefaultModelAgentClassifier(
            model!!,
            systemPromptTemplate,
            MvelSystemPromptRenderer(),
            DefaultAgentAggregator(agentProviders),
        )
    }
}

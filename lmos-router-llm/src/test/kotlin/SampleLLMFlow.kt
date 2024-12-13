// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import io.mockk.clearAllMocks
import org.eclipse.lmos.router.core.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SampleLLMFlow {
    @Test
    fun `test sample llm resolver flow`() {
        val agentRoutingSpecsProvider =
            JsonAgentRoutingSpecsProvider(jsonFilePath = "src/test/resources/agentRoutingSpecs.json")
        val agentRoutingSpecResolver =
            LLMAgentRoutingSpecsResolver(agentRoutingSpecsProvider = agentRoutingSpecsProvider)
        val context = Context(listOf(AssistantMessage("Hello")))

        // Test the resolver with a message that should trigger the offer-agent
        val input = UserMessage("Can you help me find a new phone?")
        val result = agentRoutingSpecResolver.resolve(context, input)
        assert(result is Success)
        assert((result as Success).getOrNull()?.name == "offer-agent")

        // Test the resolver with a message that should trigger the offer-agent
        val input2 = UserMessage("I want to buy a new phone")
        val result2 = agentRoutingSpecResolver.resolve(context, input2)
        assert(result2 is Success)
        assert((result2 as Success).getOrNull()?.name == "offer-agent")

        // Test the resolver with a message that should trigger the order-agent
        val input3 = UserMessage("What is the status of my order number 345435?")
        val result3 = agentRoutingSpecResolver.resolve(context, input3)
        assert(result3 is Success)
        assert((result3 as Success).getOrNull()?.name == "order-agent")
    }

    @Test
    fun `sample test with external prompt agent spec in xml format`() {
        val agentRoutingSpecsResolver =
            LLMAgentRoutingSpecsResolver(
                JsonAgentRoutingSpecsProvider(jsonFilePath = "src/test/resources/agentRoutingSpecs.json"),
                ExternalModelPromptProvider("src/test/resources/prompt.txt"),
            )
        val context = Context(listOf(AssistantMessage("Hello")))
        val input = UserMessage("Can you help me find a new phone?")
        val result = agentRoutingSpecsResolver.resolve(context, input)
        assert(result is Success)
        assert((result as Success).getOrNull()?.name == "offer-agent")
    }

    @Test
    fun `sample test with external prompt agent spec in json format`() {
        val routingSpecsResolver =
            LLMAgentRoutingSpecsResolver(
                JsonAgentRoutingSpecsProvider(jsonFilePath = "src/test/resources/agentRoutingSpecs.json"),
                ExternalModelPromptProvider(
                    "src/test/resources/prompt_agentRoutingSpec_json.txt",
                    AgentRoutingSpecListType.JSON,
                ),
            )
        val context = Context(listOf(AssistantMessage("Hello")))
        val input = UserMessage("Can you help me find a new phone?")
        val result = routingSpecsResolver.resolve(context, input)
        assert(result is Success)
        assert((result as Success).getOrNull()?.name == "offer-agent")
    }

    @BeforeEach
    fun clearAll() {
        // Clear all mock
        clearAllMocks()
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            require(System.getenv("OPENAI_API_KEY") != null) {
                "Please set the OPENAI_API_KEY environment variable to run the tests"
            }
        }
    }
}

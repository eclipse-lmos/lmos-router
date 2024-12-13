// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.eclipse.lmos.router.core.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LLMAgentRoutingSpecsResolverTest {
    private lateinit var agentRoutingSpecsProvider: AgentRoutingSpecsProvider
    private lateinit var modelPromptProvider: ModelPromptProvider
    private lateinit var modelClient: ModelClient
    private lateinit var serializer: Json
    private lateinit var llmAgentRoutingSpecsResolver: LLMAgentRoutingSpecsResolver

    @BeforeEach
    fun setUp() {
        agentRoutingSpecsProvider = mockk()
        modelPromptProvider = mockk()
        modelClient = mockk()
        serializer = mockk()
        llmAgentRoutingSpecsResolver =
            LLMAgentRoutingSpecsResolver(agentRoutingSpecsProvider, modelPromptProvider, modelClient, serializer)
    }

    @Test
    fun `resolve should return an AgentSpec when modelClient responds with valid agent name`() {
        val context = mockk<Context>()
        val input = UserMessage("Test input")
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    "agent-1",
                    "description-1",
                    "",
                    setOf(Capability("cap-1", "desc-1", version = "")),
                    setOf(),
                ),
            )
        val prompt = "Test prompt"
        val response = """{"agentName": "agent-1"}"""
        val expectedAgentSpec = agentRoutingSpecs.first { it.name == "agent-1" }

        every { agentRoutingSpecsProvider.provide(emptySet()) } returns Success(agentRoutingSpecs)
        every { modelPromptProvider.providePrompt(context, agentRoutingSpecs, input) } returns Success(prompt)
        every { context.previousMessages } returns emptyList()
        every { modelClient.call(any()) } returns Success(AssistantMessage(response))
        every {
            serializer.decodeFromString<ModelClientResponse>(
                any(),
                response,
            )
        } returns ModelClientResponse("agent-1")

        val result = llmAgentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Success)
        assertEquals(expectedAgentSpec, result.getOrThrow())
    }

    @Test
    fun `resolve should return AgentSpecResolverException when agentSpecsProvider fails`() {
        val context = mockk<Context>()
        val input = UserMessage("Test input")

        val expectedException = AgentRoutingSpecsProviderException("Failed to provide agent specs")

        every { agentRoutingSpecsProvider.provide(emptySet()) } returns Failure(expectedException)

        val result = llmAgentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Failure)
        assertTrue(result.exceptionOrNull() is AgentRoutingSpecResolverException)
    }

    @Test
    fun `resolve should return AgentSpecResolverException when modelPromptProvider fails`() {
        val context = mockk<Context>()
        val input = UserMessage("Test input")
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    "agent-1",
                    "description-1",
                    "",
                    setOf(Capability("cap-1", "desc-1", version = "")),
                    setOf(),
                ),
            )

        val expectedException = AgentRoutingSpecResolverException("Failed to provide prompt")

        every { agentRoutingSpecsProvider.provide(emptySet()) } returns Success(agentRoutingSpecs)
        every {
            modelPromptProvider.providePrompt(
                context,
                agentRoutingSpecs,
                input,
            )
        } returns Failure(expectedException)

        val result = llmAgentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Failure)
        assertTrue(result.exceptionOrNull() is AgentRoutingSpecResolverException)
    }

    @Test
    fun `resolve should return AgentSpecResolverException when modelClient fails`() {
        val context = mockk<Context>()
        val input = UserMessage("Test input")
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    "agent-1",
                    "description-1",
                    "",
                    setOf(Capability("cap-1", "desc-1", version = "")),
                    setOf(),
                ),
            )
        val prompt = "Test prompt"

        val expectedException = AgentRoutingSpecResolverException("Failed to fetch completion from model client")

        every { agentRoutingSpecsProvider.provide(emptySet()) } returns Success(agentRoutingSpecs)
        every { modelPromptProvider.providePrompt(context, agentRoutingSpecs, input) } returns Success(prompt)
        every { context.previousMessages } returns emptyList()
        every { modelClient.call(any()) } returns Failure(expectedException)

        val result = llmAgentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Failure)
        assertTrue(result.exceptionOrNull() is AgentRoutingSpecResolverException)
    }

    @Test
    fun `resolve should return AgentSpecResolverException when serializer fails`() {
        val context = mockk<Context>()
        val input = UserMessage("Test input")
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    "agent-1",
                    "description-1",
                    "",
                    setOf(Capability("cap-1", "desc-1", version = "")),
                    setOf(),
                ),
            )
        val prompt = "Test prompt"
        val response = """{"agentName": "agent-1"}"""

        every { agentRoutingSpecsProvider.provide(emptySet()) } returns Success(agentRoutingSpecs)
        every { modelPromptProvider.providePrompt(context, agentRoutingSpecs, input) } returns Success(prompt)
        every { context.previousMessages } returns emptyList()
        every { modelClient.call(any()) } returns Success(AssistantMessage(response))
        every {
            serializer.decodeFromString<ModelClientResponse>(
                any(),
                response,
            )
        } throws Exception("JSON decode error")

        val result = llmAgentRoutingSpecsResolver.resolve(context, input)

        assertTrue(result is Failure)
        val actualException = result.exceptionOrNull()
        assertTrue(actualException is AgentRoutingSpecResolverException)
        assertEquals("Failed to resolve agent spec", actualException?.message)
    }
}

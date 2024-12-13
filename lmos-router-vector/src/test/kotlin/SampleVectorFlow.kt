// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import io.ktor.client.*
import org.eclipse.lmos.router.core.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.ollama.OllamaContainer

class SampleVectorFlow {
    @Test
    fun `test sample flow`() {
        val agentRoutingSpecsProvider =
            JsonAgentRoutingSpecsProvider(jsonFilePath = "src/test/resources/agentRoutingSpecs.json")
        val vectorAgentRoutingSpecsResolver =
            VectorAgentRoutingSpecsResolver(
                agentRoutingSpecsProvider,
                DefaultVectorClient(
                    DefaultVectorClientProperties(seedJsonFilePath = "src/test/resources/seed.json"),
                    embeddingClient,
                ),
            )
        val context = Context(listOf(AssistantMessage("Hello")))

        // The input to test whether offer-agent is resolved
        val input = UserMessage("Can you help me find a new phone?")
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)
        assert(result is Success)
        assert((result as Success).getOrNull()?.name == "offer-agent")

        // The input to test whether order-agent is resolved
        val input2 = UserMessage("I would like to cancel my pizza order")
        val result2 = vectorAgentRoutingSpecsResolver.resolve(context, input2)
        assert(result2 is Success)
        assert((result2 as Success).getOrNull()?.name == "order-agent")
    }

    companion object {
        private lateinit var container: OllamaContainer
        private lateinit var embeddingClient: EmbeddingClient

        @JvmStatic
        @BeforeAll
        fun setup() {
            container = OllamaContainer("ollama/ollama:0.1.26")
            container.start()
            container.execInContainer(
                "ollama",
                "pull",
                DefaultEmbeddingClientProperties().model,
            )
            embeddingClient =
                DefaultEmbeddingClient(
                    HttpClient(),
                    DefaultEmbeddingClientProperties(container.endpoint + "/api/embeddings"),
                )
            val ping = embeddingClient.embed("Hello").getOrNull()
            require(ping != null) {
                "Ollama is not running. Please start Ollama to run this test. " +
                    "Also, make sure you have 'all-minilm' installed in your Ollama."
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDownAll() {
            container.stop()
        }
    }
}

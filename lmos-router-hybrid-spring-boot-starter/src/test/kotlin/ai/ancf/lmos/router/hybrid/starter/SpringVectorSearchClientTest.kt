// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.vector.VectorClientException
import org.eclipse.lmos.router.vector.VectorRouteConstants.Companion.AGENT_FIELD_NAME
import org.eclipse.lmos.router.vector.VectorSearchClientRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore

class SpringVectorSearchClientTest {
    private lateinit var vectorStore: VectorStore
    private lateinit var springVectorSearchClientProperties: SpringVectorSearchClientProperties
    private lateinit var springVectorSearchClient: SpringVectorSearchClient

    @BeforeEach
    fun setUp() {
        vectorStore = mockk()
        springVectorSearchClientProperties = mockk()
        springVectorSearchClient = SpringVectorSearchClient(vectorStore, springVectorSearchClientProperties)
    }

    @Test
    fun `find should return Success with null when no similar vectors are found`() {
        val request = VectorSearchClientRequest("test_query", context = Context(listOf()))
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    name = "test_agent",
                    addresses = setOf(),
                    capabilities = setOf(),
                    description = "",
                    version = "",
                ),
            )

        every { springVectorSearchClientProperties.threshold } returns 0.5
        every { springVectorSearchClientProperties.topK } returns 10
        every {
            vectorStore.similaritySearch(any<SearchRequest>())
        } returns emptyList()

        val result = springVectorSearchClient.find(request, agentRoutingSpecs)

        assertTrue(result is Success)
        assertEquals(null, (result as Success).value)
        verify { vectorStore.similaritySearch(any<SearchRequest>()) }
    }

    @Test
    fun `find should return Success with VectorSearchClientResponse when similar vectors are found`() {
        val request = VectorSearchClientRequest("test_query", context = Context(listOf()))
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    name = "test_agent",
                    addresses = setOf(),
                    capabilities = setOf(),
                    description = "",
                    version = "",
                ),
            )
        val mockDocument = mockk<Document>()

        every { springVectorSearchClientProperties.threshold } returns 0.5
        every { springVectorSearchClientProperties.topK } returns 10
        every {
            vectorStore.similaritySearch(any<SearchRequest>())
        } returns listOf(mockDocument)
        every { mockDocument.content } returns "document_content"
        every { mockDocument.metadata[AGENT_FIELD_NAME] } returns "test_agent"

        val result = springVectorSearchClient.find(request, agentRoutingSpecs)

        assertTrue(result is Success)
        with(result as Success) {
            val response = result.value ?: throw AssertionError("Expected non-null response")
            assertEquals("document_content", response.text)
            assertEquals("test_agent", response.agentName)
        }
        verify { vectorStore.similaritySearch(any<SearchRequest>()) }
    }

    @Test
    fun `find should throw VectorClientException when exception occurs`() {
        val request = VectorSearchClientRequest("test_query", context = Context(listOf()))
        val agentRoutingSpecs =
            setOf(
                AgentRoutingSpec(
                    name = "test_agent",
                    addresses = setOf(),
                    capabilities = setOf(),
                    description = "",
                    version = "",
                ),
            )

        every { springVectorSearchClientProperties.threshold } returns 0.5
        every { springVectorSearchClientProperties.topK } returns 10
        every {
            vectorStore.similaritySearch(any<SearchRequest>())
        } throws VectorClientException("Vector store error")

        val result = springVectorSearchClient.find(request, agentRoutingSpecs)

        assertTrue(result is Failure)
        assertTrue((result as Failure).exceptionOrNull() is VectorClientException)
        verify { vectorStore.similaritySearch(any<SearchRequest>()) }
    }
}

// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid.starter

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.eclipse.lmos.router.core.Failure
import org.eclipse.lmos.router.core.Success
import org.eclipse.lmos.router.core.exceptionOrNull
import org.eclipse.lmos.router.vector.VectorRouteConstants.Companion.AGENT_FIELD_NAME
import org.eclipse.lmos.router.vector.VectorSeedRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore

class SpringVectorSeedClientTest {
    private val vectorStore = mockk<VectorStore>()
    private val springVectorSeedClient = SpringVectorSeedClient(vectorStore)

    @Test
    fun `should seed vector store successfully`() {
        // Arrange
        val documents =
            listOf(
                VectorSeedRequest("agent1", "text1"),
                VectorSeedRequest("agent2", "text2"),
            )

        // Mock
        val vectorDocumentsSlot = slot<List<Document>>()
        every { vectorStore.add(capture(vectorDocumentsSlot)) } returns Unit

        // Act
        val result = springVectorSeedClient.seed(documents)

        // Assert
        assertTrue(result is Success)
        assertEquals(2, vectorDocumentsSlot.captured.size)
        assertEquals("text1", vectorDocumentsSlot.captured[0].content)
        assertEquals("agent1", vectorDocumentsSlot.captured[0].metadata[AGENT_FIELD_NAME])
        assertEquals("text2", vectorDocumentsSlot.captured[1].content)
        assertEquals("agent2", vectorDocumentsSlot.captured[1].metadata[AGENT_FIELD_NAME])
        verify { vectorStore.add(any()) }
    }

    @Test
    fun `should return failure when exception is thrown`() {
        // Arrange
        val documents =
            listOf(
                VectorSeedRequest("agent1", "text1"),
                VectorSeedRequest("agent2", "text2"),
            )

        // Mock
        every { vectorStore.add(any()) } throws RuntimeException("Mock exception")

        // Act
        val result = springVectorSeedClient.seed(documents)

        // Assert
        assertTrue(result is Failure)
        val failure = result as Failure
        assertEquals("Mock exception", failure.exceptionOrNull()?.message)
        verify { vectorStore.add(any()) }
    }
}

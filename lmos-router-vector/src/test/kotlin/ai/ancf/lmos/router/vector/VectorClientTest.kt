// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.eclipse.lmos.router.core.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VectorSearchClientRequestTest {
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        context = mockk()
    }

    @Test
    fun `test initialization`() {
        val query = "sample query"
        val request = VectorSearchClientRequest(query, context)

        assertEquals(query, request.query)
        assertEquals(context, request.context)
    }
}

class VectorSearchClientResponseTest {
    @Test
    fun `test initialization`() {
        val text = "sample text"
        val agentName = "agent007"
        val response = VectorSearchClientResponse(text, agentName)

        assertEquals(text, response.text)
        assertEquals(agentName, response.agentName)
    }
}

class VectorSeedRequestTest {
    @Test
    fun `test initialization and serialization`() {
        val text = "sample text"
        val agentName = "agent007"
        val request = VectorSeedRequest(agentName, text)

        assertEquals(agentName, request.agentName)
        assertEquals(text, request.text)

        val jsonString = Json.encodeToString(VectorSeedRequest.serializer(), request)
        val decodedRequest = Json.decodeFromString<VectorSeedRequest>(jsonString)

        assertEquals(request.agentName, decodedRequest.agentName)
        assertEquals(request.text, decodedRequest.text)
    }
}

class VectorClientExceptionTest {
    @Test
    fun `test exception message`() {
        val message = "An error occurred"
        val exception = VectorClientException(message)

        assertEquals(message, exception.message)
    }

    @Test
    fun `test exception throwing`() {
        val message = "An error occurred"

        val exception =
            assertThrows<VectorClientException> {
                throw VectorClientException(message)
            }

        assertEquals(message, exception.message)
    }
}

class VectorRouteConstantsTest {
    @Test
    fun `test constants`() {
        assertEquals("agentName", VectorRouteConstants.AGENT_FIELD_NAME)
    }
}

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.lmos.router.core.*

/**
 * An interface for a client that can embed text.
 */
interface EmbeddingClient {
    /**
     * Embeds the given text.
     *
     * @param text The text to embed.
     * @return A result containing the embedding or an exception.
     */
    fun embed(text: String): Result<List<Double>, EmbeddingClientException>

    /**
     * Embeds the given texts.
     *
     * @param texts The texts to embed.
     * @return A result containing the embeddings or an exception.
     */
    fun batchEmbed(texts: List<String>): Result<List<List<Double>>, EmbeddingClientException>
}

/**
 * An exception thrown by the EmbeddingClient.
 *
 * @param message The exception message.
 */
class EmbeddingClientException(
    message: String,
    reason: Exception? = null,
) : Exception(message, reason)

/**
 * A default implementation of the EmbeddingClient.
 *
 * @param embeddingClientProperties The properties for the client.
 * @property client The HTTP client.
 */
class DefaultEmbeddingClient(
    private val client: HttpClient,
    private val embeddingClientProperties: DefaultEmbeddingClientProperties = DefaultEmbeddingClientProperties(),
) : EmbeddingClient {
    override fun embed(text: String): Result<List<Double>, EmbeddingClientException> =
        when (batchEmbed(listOf(text))) {
            is Failure -> Failure(EmbeddingClientException("Failed to embed text"))
            is Success -> Success(batchEmbed(listOf(text)).getOrThrow().first())
        }

    override fun batchEmbed(texts: List<String>): Result<List<List<Double>>, EmbeddingClientException> {
        val embeddings = mutableListOf<List<Double>>()
        return try {
            texts.forEach {
                val responseContent: String =
                    runBlocking {
                        val response =
                            client.post(embeddingClientProperties.url) {
                                contentType(ContentType.Application.Json)
                                setBody("""{"model": "${embeddingClientProperties.model}","prompt": "$it"}""")
                            }
                        response.bodyAsText()
                    }
                val embedding = Json.decodeFromString<DefaultEmbeddingResponse>(responseContent).embedding
                embeddings.add(embedding)
            }
            Success(embeddings)
        } catch (e: Exception) {
            Failure(EmbeddingClientException("Failed to batch embed texts", e))
        }
    }
}

/**
 * An embedding client which uses the OpenAI API to embed text.
 *
 * @param openAIEmbeddingClientProperties The properties for the client.
 * @property client The HTTP client.
 *
 * Functionality:
 * - Embeds text using the OpenAI API.
 * - Embeds text in batches(batch size is configurable).
 */
class OpenAIEmbeddingClient(
    private val openAIEmbeddingClientProperties: OpenAIEmbeddingClientProperties,
) : EmbeddingClient {
    private val client: HttpClient = HttpClient()

    override fun embed(text: String): Result<List<Double>, EmbeddingClientException> = batchEmbed(listOf(text)).map { it.first() }

    override fun batchEmbed(texts: List<String>): Result<List<List<Double>>, EmbeddingClientException> {
        // Call openai api to get embeddings
        val embeddings = mutableListOf<List<Double>>()
        return try {
            texts.chunked(openAIEmbeddingClientProperties.batchSize).forEach { chunk ->
                val responseContent: String =
                    runBlocking {
                        val response =
                            client.post(openAIEmbeddingClientProperties.url) {
                                contentType(ContentType.Application.Json)
                                bearerAuth(openAIEmbeddingClientProperties.apiKey)
                                setBody(Json.encodeToString(OpenAIEmbeddingRequest(openAIEmbeddingClientProperties.model, chunk)))
                            }
                        response.bodyAsText()
                    }
                val openAIEmbeddingResponse = Json.decodeFromString<OpenAIEmbeddingResponse>(responseContent)
                openAIEmbeddingResponse.data.forEach {
                    embeddings.add(it.embedding)
                }
            }
            Success(embeddings)
        } catch (e: Exception) {
            Failure(EmbeddingClientException("Failed to batch embed texts", e))
        }
    }
}

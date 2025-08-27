// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.eclipse.lmos.router.core.Context

/**
 * A class representing a request to the VectorSearchClient.
 */
open class VectorSearchClientRequest(
    val query: String,
    val context: Context,
)

/**
 * A class representing a response from the VectorSearchClient.
 */
open class VectorSearchClientResponse(
    val text: String,
    val agentName: String,
)

/**
 * A class representing a request to the VectorSeedClient.
 */
@Serializable
open class VectorSeedRequest(
    val agentName: String,
    val text: String,
)

/**
 * An exception thrown by the VectorClient.
 *
 * @param message The exception message.
 */
class VectorClientException(
    message: String,
    reason: Exception? = null,
) : Exception(message, reason)

/**
 * Constants for the VectorRoute.
 */
class VectorRouteConstants {
    companion object {
        const val AGENT_FIELD_NAME = "agentName"
    }
}

/**
 * The default properties for the EmbeddingClient.
 *
 * @property url The URL of the embedding service. It defaults to "http://localhost:11434/api/embeddings" of Ollama.
 * @property model The model to use for embedding. It defaults to "all-minilm" of Ollama.
 */
data class DefaultEmbeddingClientProperties(
    val url: String = "http://localhost:11434/api/embeddings",
    val model: String = "all-minilm",
)

/**
 * An implementation of the EmbeddingClient that uses OpenAI embeddings.
 *
 * @property apiKey The API key for OpenAI. It defaults to the OPENAI_API_KEY environment variable.
 * @property model The model to use for embedding. It defaults to "text-embedding-3-large".
 * @property batchSize The batch size for embedding. It defaults to 300.
 */
class OpenAIEmbeddingClientProperties(
    val url: String = "https://api.openai.com/v1/embeddings",
    val model: String = "text-embedding-3-large",
    val batchSize: Int = 300,
    val apiKey: String = System.getenv("OPENAI_API_KEY"),
)

/**
 * Request to embed text using the OpenAI API.
 *
 * @property model The model to use for embedding.
 * @property input The list of strings to embed.
 */
@Serializable
class OpenAIEmbeddingRequest(
    val model: String,
    val input: List<String>,
)

/**
 * Response from the OpenAI API for embedding text.
 *
 * @property object The object type. It is "list" for a list of embeddings.
 * @property data The list of embeddings.
 * @property model The model used for embedding.
 * @property usage The usage statistics.
 */
@Serializable
class OpenAIEmbeddingResponse(
    val `object`: String,
    val data: List<OpenAIEmbeddingData>,
    val model: String,
    val usage: OpenAIEmbeddingUsage,
)

/**
 * Data class for the embedding response from the OpenAI API.
 *
 * @property embedding The embedding.
 * @property index The index of the embedding.
 * @property object The object type. It is "embedding" for an embedding.
 */
@Serializable
class OpenAIEmbeddingData(
    val embedding: List<Double>,
    val index: Int,
    val `object`: String,
)

/**
 * Usage class for the embedding response from the OpenAI API.
 *
 * @property promptTokens The number of tokens in the prompt.
 * @property totalTokens The total number of tokens.
 */
@Serializable
class OpenAIEmbeddingUsage
    @OptIn(ExperimentalSerializationApi::class)
    constructor(
        @OptIn(ExperimentalSerializationApi::class)@JsonNames("prompt_tokens")val promptTokens: Int,
        @OptIn(ExperimentalSerializationApi::class)@JsonNames("total_tokens") val totalTokens: Int,
    )

// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.vector

import io.ktor.client.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.eclipse.lmos.router.core.*
import java.io.File

/**
 * A client for searching similar vectors.
 *
 * The [find] method takes a [VectorSearchClientRequest] and a set of [AgentRoutingSpec]s and returns the most similar document based on the cosine similarity of the embeddings.
 */
interface VectorSearchClient {
    fun find(
        request: VectorSearchClientRequest,
        agentRoutingSpecs: Set<AgentRoutingSpec>,
    ): Result<VectorSearchClientResponse?, VectorClientException>
}

/**
 * A client for seeding vectors.
 *
 * The [seed] method takes a list of [VectorSeedRequest]s and seeds the documents.
 */
interface VectorSeedClient {
    fun seed(documents: List<VectorSeedRequest>): Result<Unit, VectorClientException>
}

/**
 * The default implementation of the VectorSearchClient and VectorSeedClient.
 *
 * This implementation keeps a list of documents and searches for the most similar document based on the cosine similarity of the embeddings.
 * The documents are seeded using the seed method and kept in memory.
 * This implementation is not meant for production use and is only for demonstration purposes.
 *
 * @param vectorClientProperties The properties for the client.
 * @param embeddingClient The embedding client.
 *
 * @see VectorSearchClient
 * @see VectorSeedClient
 * @see EmbeddingClient
 */
class DefaultVectorClient(
    private val vectorClientProperties: DefaultVectorClientProperties,
    private val embeddingClient: EmbeddingClient =
        DefaultEmbeddingClient(
            HttpClient(),
            DefaultEmbeddingClientProperties(),
        ),
) : VectorSearchClient,
    VectorSeedClient {
    private val documents = mutableListOf<DefaultVectorDocument>()

    init {
        if (vectorClientProperties.seedJsonFilePath.isNotEmpty()) {
            val jsonFile = File(vectorClientProperties.seedJsonFilePath)
            val json = jsonFile.readText()
            val vectorSeedRequests = Json.decodeFromString<List<VectorSeedRequest>>(json)
            seed(vectorSeedRequests).getOrThrow()
        }
    }

    override fun find(
        request: VectorSearchClientRequest,
        agentRoutingSpecs: Set<AgentRoutingSpec>,
    ): Result<VectorSearchClientResponse?, VectorClientException> =
        try {
            val embeddings = embeddingClient.embed(request.query).getOrThrow()
            val result =
                documents
                    .filter { agentRoutingSpecs.any { agentRoutingSpec -> agentRoutingSpec.name == it.agentName } }
                    .sortedByDescending { embeddings.cosineSimilarity(it.vector) }
                    .take(vectorClientProperties.limit)
                    .map { VectorSearchClientResponse(it.text, it.agentName) }
            Success(
                result
                    .groupBy { it.agentName }
                    .maxByOrNull { it.value.size }
                    ?.value
                    ?.first(),
            )
        } catch (e: Exception) {
            Failure(VectorClientException("Failed to find documents", e))
        }

    override fun seed(documents: List<VectorSeedRequest>): Result<Unit, VectorClientException> =
        try {
            val batchEmbeddings = embeddingClient.batchEmbed(documents.map { it.text }).getOrThrow()
            Success(
                batchEmbeddings.forEachIndexed { index, embeddings ->
                    this.documents.add(
                        DefaultVectorDocument(
                            documents[index].text,
                            embeddings,
                            documents[index].agentName,
                        ),
                    )
                },
            )
        } catch (e: Exception) {
            Failure(VectorClientException("Failed to seed documents", e))
        }
}

/**
 * The default properties for the VectorClient.
 */
data class DefaultVectorClientProperties(
    val seedJsonFilePath: String,
    val limit: Int = 5,
)

/**
 * The default vector document.
 */
data class DefaultVectorDocument(
    val text: String,
    val vector: List<Double>,
    val agentName: String,
)

/**
 * The default embedding response.
 */
@Serializable
data class DefaultEmbeddingResponse(
    val embedding: List<Double>,
)

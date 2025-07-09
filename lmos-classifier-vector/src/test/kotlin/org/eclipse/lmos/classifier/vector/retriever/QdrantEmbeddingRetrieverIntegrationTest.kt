// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.retriever

import dev.langchain4j.model.embedding.EmbeddingModel
import io.qdrant.client.PointIdFactory
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import io.qdrant.client.grpc.Points.PointStruct
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.vector.EmbeddingModelMock
import org.eclipse.lmos.classifier.vector.utils.getQdrantCollectionName
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.qdrant.QdrantContainer
import java.util.*

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QdrantEmbeddingRetrieverIntegrationTest {
    private lateinit var underTest: QdrantEmbeddingRetriever

    private lateinit var qdrantClient: QdrantClient
    private lateinit var embeddingModel: EmbeddingModel

    private val context = SystemContext("test-tenant", "test-channel")

    @BeforeAll
    fun setup() {
        val qdrantContainer = QdrantContainer("qdrant/qdrant")
        qdrantContainer.start()

        qdrantClient =
            QdrantClient(
                QdrantGrpcClient
                    .newBuilder(
                        qdrantContainer.host,
                        qdrantContainer.getMappedPort(6334),
                        false,
                    ).build(),
            )

        embeddingModel = EmbeddingModelMock()

        underTest =
            QdrantEmbeddingRetriever(
                qdrantClient,
                embeddingModel = embeddingModel,
                embeddingMaxResults = 5,
            )
    }

    @BeforeEach
    fun createCollection() {
        qdrantClient
            .createCollectionAsync(
                getQdrantCollectionName(context),
                VectorParams
                    .newBuilder()
                    .setDistance(Distance.Cosine)
                    .setSize(1024)
                    .build(),
            ).get()
    }

    @AfterEach
    fun deleteCollection() {
        qdrantClient.deleteCollectionAsync(getQdrantCollectionName(context)).get()
    }

    @Test
    fun `retrieve returns empty list if no embeddings exists`() {
        // when
        val results = underTest.retrieve(context, "anything")
        // then
        assertThat(results).isEmpty()
    }

    @Test
    fun `retrieve returns embeddings sorted by relevance`() {
        // given
        val fistPoint = createPoint("agent-id-1", "capability-id-1", "capability-desc-1", "my great example")
        val secondPoint = createPoint("agent-id-2", "capability-id-2", "capability-desc-2", "something else")

        qdrantClient.upsertAsync(getQdrantCollectionName(context), listOf(fistPoint, secondPoint)).get()

        // when
        val results = underTest.retrieve(context, "my great example")

        // then
        assertThat(results).hasSize(2)
        assertThat(results[0].agentId).isEqualTo("agent-id-1")
        assertThat(results[0].capabilityId).isEqualTo("capability-id-1")
        assertThat(results[0].capabilityDescription).isEqualTo("capability-desc-1")
        assertThat(results[0].example).isEqualTo("my great example")

        assertThat(results[1].agentId).isEqualTo("agent-id-2")
        assertThat(results[1].capabilityId).isEqualTo("capability-id-2")
        assertThat(results[1].capabilityDescription).isEqualTo("capability-desc-2")
        assertThat(results[1].example).isEqualTo("something else")
    }

    @Test
    fun `retrieve throws exception for unknown tenant`() {
        val context = SystemContext("tenant-does-not-exist", "test-channel")
        assertThatThrownBy { underTest.retrieve(context, "some query") }
            .isInstanceOf(TenantNotSupportedException::class.java)
            .hasMessage("No collection found for tenant '${context.tenantId}' and channel '${context.channelId}'.")
    }

    private fun createPoint(
        agentId: String,
        capabilityId: String,
        capabilityDescription: String,
        capabilityExample: String,
    ): PointStruct {
        val embedding = embeddingModel.embed(capabilityExample).content().vectorAsList()
        return PointStruct
            .newBuilder()
            .setId(PointIdFactory.id(UUID.randomUUID()))
            .setVectors(vectors(embedding))
            .putAllPayload(
                mapOf(
                    EMBEDDING_METADATA_AGENT_ID to value(agentId),
                    EMBEDDING_METADATA_CAPABILITY_ID to value(capabilityId),
                    EMBEDDING_METADATA_CAPABILITY_DESCRIPTION to value(capabilityDescription),
                    EMBEDDING_METADATA_CAPABILITY_EXAMPLE to value(capabilityExample),
                ),
            ).build()
    }
}

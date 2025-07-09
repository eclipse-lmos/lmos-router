// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.handler

import io.qdrant.client.ConditionFactory
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Points.*
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.EMBEDDING_METADATA_AGENT_ID
import org.eclipse.lmos.classifier.core.semantic.EMBEDDING_METADATA_CAPABILITY_DESCRIPTION
import org.eclipse.lmos.classifier.core.semantic.EMBEDDING_METADATA_CAPABILITY_EXAMPLE
import org.eclipse.lmos.classifier.core.semantic.EMBEDDING_METADATA_CAPABILITY_ID
import org.eclipse.lmos.classifier.vector.EmbeddingModelMock
import org.eclipse.lmos.classifier.vector.utils.getQdrantCollectionName
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.qdrant.QdrantContainer
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
internal class QdrantEmbeddingHandlerIntegrationTest {
    private lateinit var underTest: QdrantEmbeddingHandler

    private lateinit var qdrantClient: QdrantClient

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

        val embeddingModel = EmbeddingModelMock()

        underTest = QdrantEmbeddingHandler(qdrantClient, embeddingModel)
    }

    @AfterEach
    fun cleanup() {
        qdrantClient.deleteCollectionAsync(getQdrantCollectionName(context)).get()
    }

    @Test
    fun `ingest adds capability examples to the embedding store`() {
        // given
        val agent =
            Agent(
                "sales-agent",
                listOf(
                    Capability("view-offers", "View offers", listOf("This is example 1", "This is example 2")),
                    Capability("place-order", "Place my order", listOf("This is example 3")),
                ),
            )

        // when
        underTest.ingest(context, agent)

        // then
        val pointsPayload = readPointsByAgent(getQdrantCollectionName(context), "sales-agent", 3)
        assertThat(pointsPayload).containsExactlyInAnyOrder(
            listOf("This is example 1", "sales-agent", "view-offers", "View offers"),
            listOf("This is example 2", "sales-agent", "view-offers", "View offers"),
            listOf("This is example 3", "sales-agent", "place-order", "Place my order"),
        )
    }

    @Test
    fun `ingest removes capability examples from the embedding store`() {
        // given
        val initialAgent =
            Agent(
                "sales-agent",
                listOf(
                    Capability("view-offers", "View offers", listOf("This is example 1", "This is example 2")),
                ),
            )

        underTest.ingest(context, initialAgent)

        val pointsPayload = readPointsByAgent(getQdrantCollectionName(context), "sales-agent", 2)
        assertThat(pointsPayload).containsExactlyInAnyOrder(
            listOf("This is example 1", "sales-agent", "view-offers", "View offers"),
            listOf("This is example 2", "sales-agent", "view-offers", "View offers"),
        )

        // when
        val updatedAgent =
            Agent(
                "sales-agent",
                listOf(
                    Capability("view-offers", "View offers", listOf("This is example 1")),
                ),
            )

        underTest.ingest(context, updatedAgent)

        // then
        val updatedPointsPayload = readPointsByAgent(getQdrantCollectionName(context), "sales-agent", 1)
        assertThat(updatedPointsPayload).containsExactlyInAnyOrder(
            listOf("This is example 1", "sales-agent", "view-offers", "View offers"),
        )
    }

    @Test
    fun `ingest updates capability examples in embedding store`() {
        // given
        val originalAgent =
            Agent(
                "sales-agent",
                listOf(
                    Capability("view-offers", "View offers", listOf("This is example 1", "This is example 2")),
                    Capability("place-order", "Place my order", listOf("This is example 3")),
                ),
            )

        underTest.ingest(context, originalAgent)

        val pointsPayload = readPointsByAgent(getQdrantCollectionName(context), "sales-agent", 3)
        assertThat(pointsPayload).containsExactlyInAnyOrder(
            listOf("This is example 1", "sales-agent", "view-offers", "View offers"),
            listOf("This is example 2", "sales-agent", "view-offers", "View offers"),
            listOf("This is example 3", "sales-agent", "place-order", "Place my order"),
        )

        // when
        val updatedAgent =
            Agent(
                "sales-agent",
                listOf(
                    Capability("view-offers", "View offers", listOf("This is example 1", "This is my updated example 2")),
                    Capability("place-order", "Place my order", listOf("This is example 3")),
                ),
            )

        underTest.ingest(context, updatedAgent)

        // then
        val updatedPointsPayload = readPointsByAgent(getQdrantCollectionName(context), "sales-agent", 3)
        assertThat(updatedPointsPayload).containsExactlyInAnyOrder(
            listOf("This is example 1", "sales-agent", "view-offers", "View offers"),
            listOf("This is my updated example 2", "sales-agent", "view-offers", "View offers"),
            listOf("This is example 3", "sales-agent", "place-order", "Place my order"),
        )
    }

    @Test
    fun `ingest adds no capability examples when agent has no capabilities`() {
        // given
        val agent =
            Agent(
                "sales-agent",
                listOf(Capability("view-offers", "View offers", emptyList())),
            )

        // when
        underTest.ingest(context, agent)

        // then
        val results = readPointsByAgent(getQdrantCollectionName(context), "sales-agent", 0)
        assertThat(results).isEmpty()
    }

    private fun readPointsByAgent(
        collection: String,
        agentId: String,
        expectedSize: Int,
        timeoutSeconds: Long = 5,
    ): List<List<String?>> {
        var result: List<List<String?>> = emptyList()
        Awaitility
            .await()
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .atMost(timeoutSeconds, TimeUnit.SECONDS)
            .until {
                result = getPointsPayload(collection, agentId)
                result.size == expectedSize
            }
        return result
    }

    private fun getPointsPayload(
        collection: String,
        agentId: String,
    ): List<List<String?>> {
        val agentFilter =
            Filter
                .newBuilder()
                .addMust(ConditionFactory.matchKeyword(EMBEDDING_METADATA_AGENT_ID, agentId))
                .build()

        val scrollPoints =
            ScrollPoints
                .newBuilder()
                .setCollectionName(collection)
                .setFilter(agentFilter)
                .setLimit(100)
                .build()

        val results = qdrantClient.scrollAsync(scrollPoints).get().resultList

        return results
            .map {
                listOf(
                    it.payloadMap[EMBEDDING_METADATA_CAPABILITY_EXAMPLE]?.stringValue,
                    it.payloadMap[EMBEDDING_METADATA_AGENT_ID]?.stringValue,
                    it.payloadMap[EMBEDDING_METADATA_CAPABILITY_ID]?.stringValue,
                    it.payloadMap[EMBEDDING_METADATA_CAPABILITY_DESCRIPTION]?.stringValue,
                )
            }
    }
}

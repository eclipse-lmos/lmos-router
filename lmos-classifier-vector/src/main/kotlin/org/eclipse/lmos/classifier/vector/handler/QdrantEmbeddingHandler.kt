// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.handler

import dev.langchain4j.model.embedding.EmbeddingModel
import io.qdrant.client.ConditionFactory
import io.qdrant.client.PointIdFactory
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import io.qdrant.client.grpc.Common.*
import io.qdrant.client.grpc.Points.*
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.vector.utils.getQdrantCollectionName
import org.slf4j.LoggerFactory
import java.util.UUID

class QdrantEmbeddingHandler(
    private val qdrantClient: QdrantClient,
    private val embeddingModel: EmbeddingModel,
) : EmbeddingHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun ingest(
        context: SystemContext,
        agents: List<Agent>,
    ) {
        val collection = getQdrantCollectionName(context)
        logger.info("Start ingesting embeddings into the '$collection' collection for the following agents: ${agents.map { it.id }}.")
        agents.forEach {
            try {
                ingest(context, it)
            } catch (e: Exception) {
                logger.error("Failed to ingest embeddings for agent '${it.id}' to collection '$collection'.", e)
            }
        }
        logger.info(
            "Completed ingestion of embeddings into the '$collection' collection for the following agents: ${agents.map { it.id }}.",
        )
    }

    override fun ingest(
        context: SystemContext,
        agent: Agent,
    ) {
        val collection = getQdrantCollectionName(context)
        createCollectionIfNotExist(collection)
        val existingPoints = getExistingPoints(collection, agent.id)
        val desiredPoints = getDesiredPoints(agent)
        val pointsToAdd = desiredPoints.keys - existingPoints.keys
        val pointsToDelete = existingPoints.keys - desiredPoints.keys

        if (pointsToAdd.isNotEmpty()) {
            val points = pointsToAdd.map { createPointStruct(desiredPoints[it]!!) }
            qdrantClient.upsertAsync(collection, points)
            logger.info("Added ${pointsToAdd.size} embeddings to collection '$collection' for agent '${agent.id}'.")
        }
        if (pointsToDelete.isNotEmpty()) {
            val points = pointsToDelete.map { existingPoints[it]!! }
            qdrantClient.deleteAsync(collection, points.map { it.id })
            logger.info("Deleted ${pointsToDelete.size} embeddings from collection '$collection' for agent '${agent.id}'.")
        }
        if (pointsToAdd.isEmpty() && pointsToDelete.isEmpty()) {
            logger.info("No embedding changes for agent '${agent.id}' and collection '$collection'.")
        }
    }

    private fun getExistingPoints(
        collection: String,
        agentId: String,
    ): Map<UUID, RetrievedPoint> {
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
                .setLimit(5000)
                .build()
        return qdrantClient
            .scrollAsync(scrollPoints)
            .get()
            .resultList
            .associateBy { UUID.fromString(it.id.uuid) }
    }

    private fun getDesiredPoints(agent: Agent): Map<UUID, PointInfo> =
        agent.capabilities
            .flatMap { capability ->
                capability.examples.map { example ->
                    val rawId = "${agent.id}::${capability.id}::${example.hashCode()}"
                    val uuid = UUID.nameUUIDFromBytes(rawId.toByteArray())
                    uuid to PointInfo(uuid, agent.id, agent.name, agent.address, capability, example)
                }
            }.toMap()

    private fun createPointStruct(pointInfo: PointInfo): PointStruct {
        val embedding = embedText(pointInfo.example)
        val payload =
            mapOf(
                EMBEDDING_METADATA_AGENT_ID to value(pointInfo.agentId),
                EMBEDDING_METADATA_AGENT_NAME to value(pointInfo.agentName),
                EMBEDDING_METADATA_AGENT_ADDRESS to value(pointInfo.agentAddress),
                EMBEDDING_METADATA_CAPABILITY_ID to value(pointInfo.capability.id),
                EMBEDDING_METADATA_CAPABILITY_DESCRIPTION to value(pointInfo.capability.description),
                EMBEDDING_METADATA_CAPABILITY_EXAMPLE to value(pointInfo.example),
            )
        return PointStruct
            .newBuilder()
            .setId(PointIdFactory.id(pointInfo.uuid))
            .setVectors(vectors(embedding))
            .putAllPayload(payload)
            .build()
    }

    private fun embedText(text: String): List<Float> = embeddingModel.embed(text).content().vectorAsList()

    private fun createCollectionIfNotExist(collection: String) {
        val collectionExists = qdrantClient.collectionExistsAsync(collection).get()
        if (!collectionExists) {
            qdrantClient
                .createCollectionAsync(
                    collection,
                    VectorParams
                        .newBuilder()
                        .setDistance(Distance.Cosine)
                        .setSize(1024)
                        .build(),
                ).get()
        }
    }

    override fun remove(context: SystemContext) {
        val collection = getQdrantCollectionName(context)
        qdrantClient.deleteCollectionAsync(collection).get()
        logger.info("Removed '$collection' collection and all related embeddings.")
    }

    override fun remove(
        context: SystemContext,
        agent: Agent,
    ) {
        val collection = getQdrantCollectionName(context)
        val pointsToDelete =
            getDesiredPoints(agent)
                .keys
                .map {
                    PointIdFactory.id(it)
                }
        qdrantClient.deleteAsync(collection, pointsToDelete).get()
        logger.info("Removed ${pointsToDelete.size} embeddings from collection '$collection' for agent '${agent.id}'.")
    }

    companion object {
        fun builder(): QdrantEmbeddingHandlerBuilder = QdrantEmbeddingHandlerBuilder()
    }
}

data class PointInfo(
    val uuid: UUID,
    val agentId: String,
    val agentName: String,
    val agentAddress: String,
    val capability: Capability,
    val example: String,
)

class QdrantEmbeddingHandlerBuilder {
    private var qdrantHost: String = "localhost"
    private var qdrantPort: Int = 6334
    private var qdrantTlsEnabled: Boolean = false
    private var qdrantApiKey: String = ""
    private var embeddingModel: EmbeddingModel? = null

    fun withQdrantHost(qdrantHost: String) =
        apply {
            this.qdrantHost = qdrantHost
        }

    fun withQdrantPort(qdrantPort: Int) =
        apply {
            this.qdrantPort = qdrantPort
        }

    fun withQdrantTlsEnabled(qdrantTlsEnabled: Boolean) =
        apply {
            this.qdrantTlsEnabled = qdrantTlsEnabled
        }

    fun withQdrantApiKey(qdrantApiKey: String) =
        apply {
            this.qdrantApiKey = qdrantApiKey
        }

    fun withEmbeddingModel(embeddingModel: EmbeddingModel) =
        apply {
            this.embeddingModel = embeddingModel
        }

    fun build(): QdrantEmbeddingHandler {
        if (embeddingModel == null) throw IllegalStateException("EmbeddingModel must be set")

        val qdrantClient =
            QdrantClient(
                QdrantGrpcClient
                    .newBuilder(
                        qdrantHost,
                        qdrantPort,
                        qdrantTlsEnabled,
                    ).withApiKey(qdrantApiKey)
                    .build(),
            )
        return QdrantEmbeddingHandler(qdrantClient, embeddingModel!!)
    }
}

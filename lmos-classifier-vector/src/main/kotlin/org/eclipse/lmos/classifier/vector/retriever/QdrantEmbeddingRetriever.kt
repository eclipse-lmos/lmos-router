// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.retriever

import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.EMBEDDING_METADATA_CAPABILITY_EXAMPLE
import org.eclipse.lmos.classifier.core.semantic.Embedding
import org.eclipse.lmos.classifier.core.semantic.EmbeddingRetriever
import org.eclipse.lmos.classifier.core.semantic.RetrievalFailedException
import org.eclipse.lmos.classifier.vector.utils.convert
import org.eclipse.lmos.classifier.vector.utils.getQdrantCollectionName

class QdrantEmbeddingRetriever(
    private val qdrantClient: QdrantClient,
    private val embeddingModel: EmbeddingModel,
    private var embeddingMaxResults: Int = 15,
) : EmbeddingRetriever {
    private val retrieverByCollection = mutableMapOf<String, ContentRetriever>()

    override fun retrieve(
        context: SystemContext,
        userMessage: String,
    ): List<Embedding> {
        val collection = getQdrantCollectionName(context)
        try {
            val retriever = retrieverByCollection.getOrPut(collection) { createRetriever(collection) }
            val contents = retriever.retrieve(Query(userMessage))
            val embeddings = contents.mapNotNull { it.convert() }
            return embeddings
        } catch (e: RuntimeException) {
            throw RetrievalFailedException("Failed to retrieve embeddings. Reason: ${e.message}.", e)
        }
    }

    private fun createRetriever(tenant: String): EmbeddingStoreContentRetriever {
        val embeddingStore =
            QdrantEmbeddingStore
                .builder()
                .client(qdrantClient)
                .collectionName(tenant)
                .payloadTextKey(EMBEDDING_METADATA_CAPABILITY_EXAMPLE)
                .build()

        val contentRetriever =
            EmbeddingStoreContentRetriever
                .builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(embeddingMaxResults)
                .build()

        return contentRetriever
    }

    companion object {
        fun builder(): QdrantEmbeddingRetrieverBuilder = QdrantEmbeddingRetrieverBuilder()
    }
}

class QdrantEmbeddingRetrieverBuilder {
    private var qdrantHost: String = "localhost"
    private var qdrantPort: Int = 6334
    private var qdrantTlsEnabled: Boolean = false
    private var qdrantApiKey: String = ""
    private var embeddingModel: EmbeddingModel? = null
    private var embeddingMaxResults: Int = 15

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

    fun withEmbeddingMaxResults(embeddingMaxResults: Int) =
        apply {
            this.embeddingMaxResults = embeddingMaxResults
        }

    fun build(): QdrantEmbeddingRetriever {
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
        return QdrantEmbeddingRetriever(qdrantClient, embeddingModel!!, embeddingMaxResults)
    }
}

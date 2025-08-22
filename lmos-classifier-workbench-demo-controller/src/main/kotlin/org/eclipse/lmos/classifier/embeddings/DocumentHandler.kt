// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.embeddings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.lmos.classifier.config.EmbeddingDocumentProperties
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.EmbeddingHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.io.File

@Component
@Configuration
@EnableConfigurationProperties(EmbeddingDocumentProperties::class)
open class DocumentHandler(
    private val embeddingHandler: EmbeddingHandler,
    private val embeddingDocumentProperties: EmbeddingDocumentProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun ingestDocuments(context: SystemContext) {
        val channelRoutings = loadJsonFilesAsChannelRoutings(embeddingDocumentProperties.documentPath)
        val groups = channelRoutings.flatMap { it.capabilityGroups }
        embeddingHandler.ingest(context, groups)
    }

    private fun loadJsonFilesAsChannelRoutings(directoryPath: String): List<ChannelRouting> {
        val objectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return File(directoryPath)
            .listFiles { file -> file.isFile && file.extension == "json" }
            ?.mapNotNull { file ->
                runCatching {
                    objectMapper.readValue(file, ChannelRouting::class.java)
                }.onFailure { ex ->
                    logger.error("Failed to parse file: ${file.name}", ex)
                }.onSuccess {
                    logger.info("Successfully parsed file: ${file.name}")
                }.getOrNull()
            } ?: emptyList()
    }

    fun removeDocuments(context: SystemContext) {
        embeddingHandler.remove(context)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ChannelRouting(
        val id: String,
        val capabilityGroups: List<Agent>,
    )
}

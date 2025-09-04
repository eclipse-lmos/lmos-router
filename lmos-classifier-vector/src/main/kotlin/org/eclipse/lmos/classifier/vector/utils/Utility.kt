// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector.utils

import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.ContentMetadata
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.semantic.*

fun Content.convert(): Embedding? {
    val example = this.textSegment().text()
    val score = this.metadata()[ContentMetadata.SCORE]
    val agentId = this.textSegment().metadata().getString(EMBEDDING_METADATA_AGENT_ID)
    val agentName = this.textSegment().metadata().getString(EMBEDDING_METADATA_AGENT_NAME)
    val agentAddress = this.textSegment().metadata().getString(EMBEDDING_METADATA_AGENT_ADDRESS)
    val capabilityId = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_ID)
    val capabilityDescription = this.textSegment().metadata().getString(EMBEDDING_METADATA_CAPABILITY_DESCRIPTION)

    return if (score != null &&
        agentId != null &&
        agentName != null &&
        agentAddress != null &&
        capabilityId != null &&
        capabilityDescription != null
    ) {
        Embedding(
            example = example,
            score = score as Double,
            agentId = agentId,
            agentName = agentName,
            agentAddress = agentAddress,
            capabilityId = capabilityId,
            capabilityDescription = capabilityDescription,
        )
    } else {
        null
    }
}

fun List<Embedding>.convertEmbeddingsToAgents(): List<Agent> =
    this
        .groupBy { it.agentId }
        .map { (agentId, embeddings) ->
            val examplesByCapability =
                embeddings
                    .groupBy(
                        keySelector = { it.capabilityId },
                        valueTransform = { it.example },
                    )
            val capabilities =
                embeddings
                    .distinctBy { it.capabilityId }
                    .map {
                        Capability(
                            id = it.capabilityId,
                            description = it.capabilityDescription,
                            examples = examplesByCapability[it.capabilityId] ?: emptyList(),
                        )
                    }
            Agent(
                id = agentId,
                name = embeddings.first().agentName,
                address = embeddings.first().agentAddress,
                capabilities = capabilities,
            )
        }

fun getQdrantCollectionName(context: SystemContext) = "${context.tenantId}-${context.channelId}-${context.subset}"

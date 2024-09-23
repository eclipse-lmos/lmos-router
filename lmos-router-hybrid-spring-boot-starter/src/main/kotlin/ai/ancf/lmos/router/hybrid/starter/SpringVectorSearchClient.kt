// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid.starter

import ai.ancf.lmos.router.core.AgentRoutingSpec
import ai.ancf.lmos.router.core.Failure
import ai.ancf.lmos.router.core.Result
import ai.ancf.lmos.router.core.Success
import ai.ancf.lmos.router.vector.VectorClientException
import ai.ancf.lmos.router.vector.VectorRouteConstants.Companion.AGENT_FIELD_NAME
import ai.ancf.lmos.router.vector.VectorSearchClient
import ai.ancf.lmos.router.vector.VectorSearchClientRequest
import ai.ancf.lmos.router.vector.VectorSearchClientResponse
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder

/**
 * A Spring implementation of the VectorSearchClient.
 *
 * @param vectorStore The vector store to search in.
 */
class SpringVectorSearchClient(
    private val vectorStore: VectorStore,
    private val springVectorSearchClientProperties: SpringVectorSearchClientProperties,
) : VectorSearchClient {
    /**
     * Finds the most similar vector to the given query.
     *
     * @param request The search request.
     * @param agentRoutingSpecs The agent specs to filter by.
     * @return A result containing the most similar vector or null if no similar vectors were found.
     */
    override fun find(
        request: VectorSearchClientRequest,
        agentRoutingSpecs: Set<AgentRoutingSpec>,
    ): Result<VectorSearchClientResponse?, VectorClientException> {
        return try {
            val documents =
                vectorStore.similaritySearch(
                    SearchRequest.query(request.query)
                        .withSimilarityThreshold(springVectorSearchClientProperties.threshold)
                        .withTopK(springVectorSearchClientProperties.topK)
                        .withFilterExpression(
                            FilterExpressionBuilder().`in`(
                                AGENT_FIELD_NAME,
                                *agentRoutingSpecs.map { it.name }.toTypedArray(),
                            ).build(),
                        ),
                )
            if (documents.isEmpty()) {
                Success(null)
            } else {
                val grouped = documents.groupBy { it.metadata[AGENT_FIELD_NAME] as String }
                val agentName = grouped.maxByOrNull { it.value.size }?.key
                if (agentName != null) {
                    Success(
                        VectorSearchClientResponse(
                            grouped.getValue(agentName).joinToString("\n") { it.content },
                            agentName,
                        ),
                    )
                } else {
                    Success(null)
                }
            }
        } catch (e: Exception) {
            Failure(VectorClientException("Failed to find similar vectors", e))
        }
    }
}

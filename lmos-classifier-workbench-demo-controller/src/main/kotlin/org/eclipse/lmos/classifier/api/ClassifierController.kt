// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.api

import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.ClassificationResult
import org.eclipse.lmos.classifier.core.llm.ModelAgentClassifier
import org.eclipse.lmos.classifier.core.semantic.*
import org.eclipse.lmos.classifier.hybrid.FastTrackAgentClassifier
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/routings", produces = [MediaType.APPLICATION_JSON_VALUE])
class ClassifierController(
    private val embeddingAgentClassifier: EmbeddingAgentClassifier,
    private val modelAgentClassifier: ModelAgentClassifier,
    private val fastTrackAgentClassifier: FastTrackAgentClassifier,
    private val embeddingRetriever: EmbeddingRetriever,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/llm")
    suspend fun llm(
        @RequestBody request: ClassificationRequest,
    ): ClassificationResult {
        logger.info("Route user query '$request' by ModelAgentClassifier.")
        return modelAgentClassifier.classify(request)
    }

    @PostMapping("/hybrid-fast-track")
    suspend fun hybridFastTrack(
        @RequestBody request: ClassificationRequest,
    ): ClassificationResult {
        logger.info("Route user query '$request' by FastTrackAgentClassifier.")
        return fastTrackAgentClassifier.classify(request)
    }

    @PostMapping("/vector")
    suspend fun vector(
        @RequestBody request: ClassificationRequest,
    ): ClassificationResult {
        logger.info("Route user query '$request' by EmbeddingAgentClassifier.")
        return embeddingAgentClassifier.classify(request)
    }

    @PostMapping("/vector/plain")
    suspend fun vectorPlain(
        @RequestBody request: ClassificationRequest,
    ): List<Embedding> {
        logger.info(
            "Retrieve plain embeddings for system context ${request.systemContext} and user message '${request.inputContext.userMessage}'.",
        )
        return embeddingRetriever.retrieve(request.systemContext, request.inputContext.userMessage)
    }

    @ExceptionHandler(RetrievalFailedException::class)
    fun handleTenantNotSupported(ex: RetrievalFailedException): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                error = "RETRIEVAL_FAILED",
                message = ex.message ?: "Failed to retrieve relevant data.",
            )
        return ResponseEntity.badRequest().body(response)
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
)

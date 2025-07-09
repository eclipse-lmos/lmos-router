// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.api

import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.embeddings.DocumentHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/embeddings", produces = [MediaType.APPLICATION_JSON_VALUE])
class DocumentController(
    private val documentHandler: DocumentHandler,
) {
    @PostMapping
    fun ingestDocuments(
        @RequestParam tenant: String,
        @RequestParam channel: String,
    ) {
        documentHandler.ingestDocuments(SystemContext(tenant, channel))
    }

    @DeleteMapping
    fun removeDocuments(
        @RequestParam tenant: String,
        @RequestParam channel: String,
    ) {
        documentHandler.removeDocuments(SystemContext(tenant, channel))
    }
}

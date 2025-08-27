// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.hybrid

import org.eclipse.lmos.router.core.Context
import org.eclipse.lmos.router.vector.VectorSearchClientRequest

/**
 * A model to vector query converter. Converts a model response to a vector search client request.
 */
abstract class ModelToVectorQueryConverter {
    abstract fun convert(
        modelResponse: String,
        context: Context,
    ): VectorSearchClientRequest
}

/**
 * A no-op model to vector query converter.
 */
class NoOpModelToVectorQueryConverter : ModelToVectorQueryConverter() {
    override fun convert(
        modelResponse: String,
        context: Context,
    ): VectorSearchClientRequest = VectorSearchClientRequest(query = modelResponse, context = context)
}

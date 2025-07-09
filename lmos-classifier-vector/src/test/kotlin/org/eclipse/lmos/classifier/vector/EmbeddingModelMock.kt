// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.vector

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.output.Response

class EmbeddingModelMock : EmbeddingModel {
    private val dimension = 1024

    override fun embed(input: String): Response<Embedding> {
        val vector = FloatArray(dimension) { i -> input.hashCode() % (i + 1) * 0.01f }
        return Response(Embedding(vector))
    }

    override fun embedAll(textSegments: List<TextSegment>): Response<List<Embedding>> {
        val embeddings = textSegments.map { embed(it.text()).content() }
        return Response(embeddings)
    }
}

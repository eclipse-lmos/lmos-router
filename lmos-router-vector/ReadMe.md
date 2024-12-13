<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# Vector Module

## Overview

The Vector module is a submodule of the Intelligent Agent Routing System that handles agent routing specifications using vector embeddings. It provides functionality to embed text, search for similar vectors, and resolve agent routing specifications based on vector similarity.

## Table of Contents

1. [Introduction](#introduction)
2. [Components](#components)
3. [Usage](#usage)
4. [Configuration](#configuration)
5. [Classes and Interfaces](#classes-and-interfaces)
6. [Exceptions](#exceptions)

## Introduction

The Vector module uses vector embeddings to represent queries and agent capabilities, comparing them using cosine similarity. This approach is efficient for large-scale data and is independent of external APIs.

## Components

### Core Components

- **EmbeddingClient**: Interface for embedding text.
- **VectorSearchClient**: Interface for searching similar vectors.
- **VectorSeedClient**: Interface for seeding vectors.
- **VectorAgentRoutingSpecsResolver**: Resolves agent routing specifications using vector similarity search.

### Implementations

- **DefaultEmbeddingClient**: Default implementation of the EmbeddingClient using a local service.
- **OpenAIEmbeddingClient**: Implementation of the EmbeddingClient using the OpenAI API.
- **DefaultVectorClient**: Default implementation of the VectorSearchClient and VectorSeedClient.

## Usage

### Step 1: Initialize the Embedding Client

You can use either the `DefaultEmbeddingClient` or the `OpenAIEmbeddingClient` to embed text.

```kotlin
val embeddingClient = DefaultEmbeddingClient(HttpClient(), DefaultEmbeddingClientProperties())
```

Or

```kotlin
val openAIEmbeddingClient = OpenAIEmbeddingClient(OpenAIEmbeddingClientProperties())
```

### Step 2: Seed the Vector Client

Seed the vector client with initial documents.

```kotlin
val vectorClientProperties = DefaultVectorClientProperties(seedJsonFilePath = "path/to/seed.json")
val vectorClient = DefaultVectorClient(vectorClientProperties, embeddingClient)
```

### Step 3: Resolve agent routing specifications

Use the `VectorAgentRoutingSpecsResolver` to resolve agent routing specifications based on the input query.

```kotlin
val agentRoutingSpecsProvider = SimpleAgentRoutingSpecProvider() // Add your agent specs here
val vectorAgentRoutingSpecsResolver = VectorAgentRoutingSpecsResolver(agentRoutingSpecsProvider, vectorClient)

val context = Context(listOf(AssistantMessage("Hello")))
val input = UserMessage("Can you help me find a new phone?")
val result = vectorAgentRoutingSpecsResolver.resolve(context, input)
```

## Configuration

### Environment Variables

- `VECTOR_SEED_JSON_FILE_PATH`: Path to the JSON file containing seed vectors.
- `OPENAI_API_KEY`: Your OpenAI API key (if using `OpenAIEmbeddingClient`).

### Properties

#### DefaultEmbeddingClientProperties

- `url`: The URL of the embedding service (default: "http://localhost:11434/api/embeddings").
- `model`: The model to use for embedding (default: "all-minilm").

#### OpenAIEmbeddingClientProperties

- `url`: The URL of the OpenAI API (default: "https://api.openai.com/v1/embeddings").
- `model`: The model to use for embedding (default: "text-embedding-3-large").
- `batchSize`: The batch size for embedding (default: 300).
- `apiKey`: The API key for OpenAI (default: `System.getenv("OPENAI_API_KEY")`).

#### DefaultVectorClientProperties

- `seedJsonFilePath`: Path to the JSON file containing seed vectors.
- `limit`: The limit for the number of similar documents to return (default: 5).

## Classes and Interfaces

### EmbeddingClient

```kotlin
interface EmbeddingClient {
    fun embed(text: String): Result<List<Double>, EmbeddingClientException>
    fun batchEmbed(texts: List<String>): Result<List<List<Double>>, EmbeddingClientException>
}
```

### VectorSearchClient

```kotlin
interface VectorSearchClient {
    fun find(request: VectorSearchClientRequest, agentRoutingSpecs: Set<AgentRoutingSpec>): Result<VectorSearchClientResponse?, VectorClientException>
}
```

### VectorSeedClient

```kotlin
interface VectorSeedClient {
    fun seed(documents: List<VectorSeedRequest>): Result<Unit, VectorClientException>
}
```

### VectorAgentRoutingSpecsResolver

```kotlin
class VectorAgentRoutingSpecsResolver(
    override val agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
    private val vectorSearchClient: VectorSearchClient
) : AgentRoutingSpecsResolver {
    // Implementation
}
```

## Exceptions

### EmbeddingClientException

```kotlin
class EmbeddingClientException(message: String) : Exception(message)
```

### VectorClientException

```kotlin
class VectorClientException(message: String, reason: Exception? = null) : Exception(message)
```

## Utility Functions

### Cosine Similarity

```kotlin
fun List<Double>.cosineSimilarity(other: List<Double>): Double {
    require(this.size == other.size) { "Vectors must be of the same length" }
    val dotProduct = this.zip(other).sumOf { (a, b) -> a * b }
    val magnitudeA = sqrt(this.sumOf { it * it })
    val magnitudeB = sqrt(other.sumOf { it * it })
    return if (magnitudeA != 0.0 && magnitudeB != 0.0) {
        dotProduct / (magnitudeA * magnitudeB)
    } else {
        0.0 // If either vector is zero, the similarity is undefined; return 0.0
    }
}
```

This readme provides a comprehensive guide to the Vector module, detailing its components, usage, configuration, and key classes and interfaces.
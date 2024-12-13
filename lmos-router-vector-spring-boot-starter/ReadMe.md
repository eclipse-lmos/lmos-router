<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# Vector Agent Routing Module

This module is part of the Intelligent Agent Routing System and focuses on resolving agent routing specifications using vector embeddings. It provides auto-configuration for Spring Boot applications to easily integrate vector-based agent routing.

## Overview

The Vector Agent Routing Module uses vector embeddings to represent queries and agent capabilities, comparing them using cosine similarity. This approach is efficient for large-scale data and is independent of external APIs.

## Features

- **Vector Search Client**: Searches for similar vectors in a vector store.
- **Vector Seed Client**: Seeds the vector store with agent routing specifications.
- **Agent Routing Specs Provider**: Reads agent routing specifications from a JSON file.
- **Vector Agent Routing Specs Resolver**: Resolves agent routing specifications using the vector search client.

## Quickstart Guide

### Step 1: Add Dependencies

Ensure you have the necessary dependencies in your `build.gradle` or `pom.xml` file.

```kotlin
dependencies {
    implementation("org.eclipse.lmos:lmos-router-vector-spring-boot-starter:1.0.0")
}
```

### Step 2: Configure Properties

Set the required properties in your `application.yml` or `application.properties` file.

```yaml
route:
  agent:
    vector:
      specFilePath: "path/to/your/agent-specs.json"
  llm:
    vector:
      search:
        threshold: 0.5
        topK: 1
```

### Step 3: Initialize the Vector Agent Routing Specs Resolver

The module provides auto-configuration, so you only need to inject the `VectorAgentRoutingSpecsResolver` where needed.

```kotlin
import org.eclipse.lmos.router.vector.VectorAgentRoutingSpecsResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AgentRoutingService {

    @Autowired
    private lateinit var vectorAgentRoutingSpecsResolver: VectorAgentRoutingSpecsResolver

    fun resolveAgent(query: String): String? {
        val context = Context(listOf(AssistantMessage("Hello")))
        val input = UserMessage(query)
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)
        return result.agentName
    }
}
```

## Configuration

### Properties

- **route.agent.vector.specFilePath**: Path to the JSON file containing the agent routing specifications.
- **route.llm.vector.search.threshold**: Similarity threshold for vector search (default: 0.5).
- **route.llm.vector.search.topK**: Number of similar vectors to return (default: 1).

### Beans

The module provides the following beans:

- **VectorSearchClient**: Uses the vector store to search for similar vectors.
- **VectorSeedClient**: Uses the vector store to seed vectors.
- **AgentRoutingSpecsProvider**: Reads agent routing specifications from a JSON file.
- **VectorAgentRoutingSpecsResolver**: Resolves agent routing specifications using the vector search client.

## Usage

### Seeding the Vector Store

To seed the vector store with agent routing specifications, use the `VectorSeedClient`.

```kotlin
import org.eclipse.lmos.router.vector.VectorSeedClient
import org.eclipse.lmos.router.vector.VectorSeedRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class VectorSeedService {

    @Autowired
    private lateinit var vectorSeedClient: VectorSeedClient

    fun seedVectors() {
        val seedRequests = listOf(
            VectorSeedRequest("Agent 1", "This is the description for agent 1."),
            VectorSeedRequest("Agent 2", "This is the description for agent 2.")
        )
        vectorSeedClient.seed(seedRequests)
    }
}
```

### Resolving Agents

To resolve agents based on user queries, use the `VectorAgentRoutingSpecsResolver`.

```kotlin
import org.eclipse.lmos.router.vector.VectorAgentRoutingSpecsResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AgentRoutingService {

    @Autowired
    private lateinit var vectorAgentRoutingSpecsResolver: VectorAgentRoutingSpecsResolver

    fun resolveAgent(query: String): String? {
        val context = Context(listOf(AssistantMessage("Hello")))
        val input = UserMessage(query)
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)
        return result.agentName
    }
}
```
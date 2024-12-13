<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->

# Hybrid Agent Routing Module

This module is part of the Intelligent Agent Routing System and focuses on resolving agent routing specifications using a hybrid approach that combines vector embeddings and large language models (LLMs). It provides auto-configuration for Spring Boot applications to easily integrate hybrid-based agent routing.

## Overview

The Hybrid Agent Routing Module leverages both vector embeddings and LLMs to represent queries and agent capabilities, comparing them using cosine similarity and model-based prompts. This approach is efficient for large-scale data and provides flexibility by combining the strengths of both methods.

## Features

- **Vector Search Client**: Searches for similar vectors in a vector store.
- **Vector Seed Client**: Seeds the vector store with agent routing specifications.
- **Agent Routing Specs Provider**: Reads agent routing specifications from a JSON file.
- **Hybrid Agent Routing Specs Resolver**: Resolves agent routing specifications using both vector search and LLMs.
- **Model Client**: Interacts with the LLM to resolve agent routing specifications.
- **Model Prompt Provider**: Provides prompts for the LLM.

## Quickstart Guide

### Step 1: Add Dependencies

Ensure you have the necessary dependencies in your `build.gradle` or `pom.xml` file.

```kotlin
dependencies {
    implementation("org.eclipse.lmos:lmos-router-hybrid-spring-boot-starter:1.0.0")
}
```

### Step 2: Configure Properties

Set the required properties in your `application.yml` or `application.properties` file.

```yaml
route:
  agent:
    hybrid:
      specFilePath: "path/to/your/agent-specs.json"
      resolverPromptFilePath: "path/to/your/resolver-prompt.txt"
  llm:
    vector:
      search:
        threshold: 0.5
        topK: 1
```

### Step 3: Initialize the Hybrid Agent Routing Specs Resolver

The module provides auto-configuration, so you only need to inject the `HybridAgentRoutingSpecsResolver` where needed.

```kotlin
import org.eclipse.lmos.router.hybrid.HybridAgentRoutingSpecsResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AgentRoutingService {
    @Autowired
    private lateinit var hybridAgentRoutingSpecsResolver: HybridAgentRoutingSpecsResolver

    fun resolveAgent(query: String): String? {
        val context = Context(listOf(AssistantMessage("Hello")))
        val input = UserMessage(query)
        val result = hybridAgentRoutingSpecsResolver.resolve(context, input)
        return result.agentName
    }
}
```

## Configuration

### Properties

- **route.agent.hybrid.specFilePath**: Path to the JSON file containing the agent routing specifications.
- **route.agent.hybrid.resolverPromptFilePath**: Path to the file containing the resolver prompts.
- **route.llm.vector.search.threshold**: Similarity threshold for vector search (default: 0.5).
- **route.llm.vector.search.topK**: Number of similar vectors to return (default: 1).

### Beans

The module provides the following beans:

- **VectorSearchClient**: Uses the vector store to search for similar vectors.
- **VectorSeedClient**: Uses the vector store to seed vectors.
- **AgentRoutingSpecsProvider**: Reads agent routing specifications from a JSON file.
- **HybridAgentRoutingSpecsResolver**: Resolves agent routing specifications using both vector search and LLMs.
- **ModelClient**: Interacts with the LLM to interpret queries.
- **ModelPromptProvider**: Provides prompts for the LLM to interpret queries.
- **ModelToVectorQueryConverter**: Converts model response to vector queries.

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

To resolve agents based on user queries, use the `HybridAgentRoutingSpecsResolver`.

```kotlin
import org.eclipse.lmos.router.hybrid.HybridAgentRoutingSpecsResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AgentRoutingService {
    @Autowired
    private lateinit var hybridAgentRoutingSpecsResolver: HybridAgentRoutingSpecsResolver

    fun resolveAgent(query: String): String? {
        val context = Context(listOf(AssistantMessage("Hello")))
        val input = UserMessage(query)
        val result = hybridAgentRoutingSpecsResolver.resolve(context, input)
        return result.agentName
    }
}
```

## License

This project is licensed under the Apache-2.0 License. See the LICENSE file for details.
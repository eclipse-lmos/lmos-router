<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# LLM Submodule

## Overview

The LLM submodule is responsible for resolving agent routing specifications using a language model. It includes classes and interfaces for interacting with the OpenAI API by default, providing prompts to the model, and resolving agent routing specifications based on the model's responses.

## Table of Contents

1. [Introduction](#introduction)
2. [Classes and Interfaces](#classes-and-interfaces)
3. [Usage](#usage)
4. [Configuration](#configuration)
5. [Error Handling](#error-handling)

## Introduction

The LLM submodule leverages advanced language models to understand and match user queries with agent capabilities. It includes a default implementation for calling the OpenAI model and resolving agent routing specifications using the model's responses.

## Classes and Interfaces

### ModelClient

The `ModelClient` interface represents a client that can call a model.

- **Method:**
    - `call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>`

### DefaultModelClient

The `DefaultModelClient` class is a default implementation of the `ModelClient` interface. It calls the OpenAI model with the given messages.

- **Constructor:**
    - `DefaultModelClient(defaultModelClientProperties: DefaultModelClientProperties)`

- **Method:**
    - `call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>`

### DefaultModelClientProperties

The `DefaultModelClientProperties` data class represents the properties for the default model client.

- **Fields:**
    - `openAiUrl: String`
    - `openAiApiKey: String`
    - `model: String`
    - `maxTokens: Int`
    - `temperature: Double`
    - `format: String`

### LLMAgentRoutingSpecsResolver

The `LLMAgentRoutingSpecsResolver` class resolves agent routing specifications using a language model.

- **Constructor:**
    - `LLMAgentRoutingSpecsResolver(agentRoutingSpecsProvider: AgentRoutingSpecsProvider, modelPromptProvider: ModelPromptProvider, modelClient: ModelClient, serializer: Json)`

- **Methods:**
    - `resolve(context: Context, input: UserMessage): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException>`
    - `resolve(filters: Set<SpecFilter>, context: Context, input: UserMessage): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException>`

### ModelPromptProvider

The `ModelPromptProvider` interface represents a provider of model prompts.

- **Method:**
    - `providePrompt(context: Context, agentRoutingSpecs: Set<AgentRoutingSpec>, input: UserMessage): Result<String, AgentRoutingSpecResolverException>`

### DefaultModelPromptProvider

The `DefaultModelPromptProvider` class provides a generic prompt for agent resolution.

- **Method:**
    - `providePrompt(context: Context, agentRoutingSpecs: Set<AgentRoutingSpec>, input: UserMessage): Result<String, AgentRoutingSpecResolverException>`

### ExternalModelPromptProvider

The `ExternalModelPromptProvider` class provides a prompt from an external file. The agent routing specifications for the prompt can be in XML or JSON format.

- **Constructor:**
    - `ExternalModelPromptProvider(promptFilePath: String, agentRoutingSpecsListType: AgentRoutingSpecListType)`

- **Method:**
    - `providePrompt(context: Context, agentRoutingSpecs: Set<AgentRoutingSpec>, input: UserMessage): Result<String, AgentRoutingSpecResolverException>`

### AgentRoutingSpecListType

The `AgentRoutingSpecListType` enum represents the format of the agent routing specs list.

- **Values:**
    - `XML`
    - `JSON`

### ModelClientResponse

The `ModelClientResponse` class represents a model client response.

- **Field:**
    - `agentName: String`

## Usage

### Step 1: Initialize the DefaultModelClient

```kotlin
val defaultModelClientProperties = DefaultModelClientProperties(
    openAiApiKey = "your-openai-api-key"
)
val modelClient = DefaultModelClient(defaultModelClientProperties)
```

### Step 2: Initialize the LLMAgentRoutingSpecsResolver

```kotlin
val agentRoutingSpecsProvider = SimpleAgentRoutingSpecProvider()
val modelPromptProvider = DefaultModelPromptProvider()
val llmAgentRoutingSpecsResolver = LLMAgentRoutingSpecsResolver(
    agentRoutingSpecsProvider,
    modelPromptProvider,
    modelClient
)
```

### Step 3: Resolve the Agent

```kotlin
val context = Context(listOf(AssistantMessage("Hello")))
val input = UserMessage("Can you help me find a new phone?")
val result = llmAgentRoutingSpecsResolver.resolve(context, input)
```
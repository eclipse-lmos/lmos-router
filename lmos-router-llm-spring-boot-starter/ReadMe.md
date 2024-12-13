<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# LLM Agent Routing Starter Module

## Overview

The `LLM Agent Routing Starter` module provides auto-configuration for the LLM-based agent resolver in a Spring Boot application. It integrates with the core module to resolve agent routing specifications using a language model.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Setup and Installation](#setup-and-installation)
4. [Configuration](#configuration)
5. [Usage](#usage)

## Introduction

The `LLM Agent Routing Starter` module simplifies the integration of the LLM-based agent resolver into a Spring Boot application. It provides auto-configuration for essential beans and properties required to resolve agent routing specifications using a language model.

## Features

- **Auto-Configuration**: Automatically configures the necessary beans for the LLM-based agent resolver.
- **Customizable Properties**: Allows customization of the agent routing specifications file path through application properties.
- **Spring Integration**: Seamlessly integrates with Spring Boot's auto-configuration mechanism.

## Setup and Installation

### Installation

1. **Clone the repository**:

    ```bash
    git clone https://github.com/eclipse-lmos/lmos-router.git
    cd lmos-router
    ```

2. **Include the module in your project**:

   Add the following dependency to your `build.gradle` file:

    ```groovy
    implementation 'ai.ancf:lmos-router-llm-spring-boot-starter:x.x.x'
    ```

   Or, if using Maven, add the following dependency to your `pom.xml`:

    ```xml
    <dependency>
        <groupId>ai.ancf</groupId>
        <artifactId>lmos-router-llm-spring-boot-starter</artifactId>
        <version>x.x.x</version>
    </dependency>
    ```

## Configuration

### Application Properties

Configure the path to the JSON file containing agent routing specifications in your `application.properties` or `application.yml` file:

```properties
route.agent.llm.specFilePath=path/to/your/agent-specs.json
```

### Beans Provided

- **ModelClient**: Uses the `ChatModel` to resolve agent routing specifications.
- **AgentRoutingSpecsProvider**: Reads agent routing specifications from a JSON file by default. You can provide a custom implementation by extending the `AgentRoutingSpecsProvider` interface.
- **ModelPromptProvider**: Uses the default model prompt provider.
- **LLMAgentRoutingSpecsResolver**: Uses the `AgentRoutingSpecsProvider`, `ModelPromptProvider`, and `ModelClient` to resolve agent routing specifications.

## Usage

### Example

1. **Define the Chat Model**:

    ```kotlin
    @Bean
    fun chatModel(): ChatModel {
        // Define and return your ChatModel implementation or use Spring's default implementation and supported models
    }
    ```

2. **Use the LLM Agent Routing Specs Resolver**:

    ```kotlin
    @Autowired
    lateinit var llmAgentRoutingSpecsResolver: LLMAgentRoutingSpecsResolver

    fun resolveAgent(context: Context, input: UserMessage): Result<AgentRoutingSpec, AgentRoutingSpecResolverException> {
        return llmAgentRoutingSpecsResolver.resolve(context, input)
    }
    ```
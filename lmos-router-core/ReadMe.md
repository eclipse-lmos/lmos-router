<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# Core Module

## Overview

The Core module contains foundational classes and interfaces essential for the Intelligent Agent Routing System. It provides the basic building blocks for representing chat messages, agent routing specifications, context, and result handling.

## Table of Contents

1. [Introduction](#introduction)
2. [Classes and Interfaces](#classes-and-interfaces)
3. [Usage](#usage)

## Introduction

The Core module is the backbone of the Intelligent Agent Routing System. It defines the essential components required for creating, managing, and resolving agent routing specifications. This module is designed to be flexible and extensible, allowing for easy integration with other modules such as LLM and Vector.

## Classes and Interfaces

### ChatMessage

Represents a chat message. It is a sealed class with the following subclasses:
- `UserMessage`: Represents a message from the user.
- `SystemMessage`: Represents a system-generated message.
- `AssistantMessage`: Represents a message from the assistant.

### ChatMessageFactory

A factory class for creating chat messages based on the role (user, system, assistant).

### ChatMessageBuilder

A builder class for creating chat messages with specified content and role.

### AgentRoutingSpecsResolver

An interface for resolving agent routing specifications based on the context and input messages. It provides methods to resolve specifications with or without filters.

### AgentRoutingSpecResolverException

An exception class thrown when an agent spec resolver fails.

### Result

A sealed class representing the result of an operation. It can be either:
- `Success`: Contains the successful result.
- `Failure`: Contains the exception that caused the failure.

### Context

Represents the context of a conversation, containing the previous messages exchanged.

### AgentRoutingSpecsProvider

An interface for providing agent routing specifications. It includes methods to provide specifications with or without filters.

### AgentRoutingSpecsProviderException

An exception class thrown when an error occurs while providing agent routing specifications.

### JsonAgentRoutingSpecsProvider

A provider class that reads agent routing specifications from a JSON file.

### SimpleAgentRoutingSpecProvider

A simple provider class for managing agent routing specifications in memory.

### SpecFilter

A marker interface for filtering agent routing specifications.

### NameSpecFilter

A filter class that filters agent routing specifications by name.

### VersionSpecFilter

A filter class that filters agent routing specifications by version.

### AgentRoutingSpec

Represents the routing specification of an agent, including its name, description, version, capabilities, and addresses.

### Capability

Represents the capabilities of an agent.

### Address

Represents the address of an agent, including the protocol and URI.

### CapabilitiesBuilder

A builder class for creating an agent's capabilities.

### AgentRoutingSpecBuilder

A builder class for creating an agent specification.

## Usage

### Creating Chat Messages

```kotlin
val userMessage = ChatMessageBuilder()
    .content("Hello, I need help with my order.")
    .role("user")
    .build()

val systemMessage = ChatMessageBuilder()
    .content("System maintenance scheduled at midnight.")
    .role("system")
    .build()

val assistantMessage = ChatMessageBuilder()
    .content("Sure, I can help you with that.")
    .role("assistant")
    .build()
```


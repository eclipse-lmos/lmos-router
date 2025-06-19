// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    implementation("dev.langchain4j:langchain4j-open-ai:1.1.0")
    implementation("dev.langchain4j:langchain4j-anthropic:1.1.0")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:1.1.0")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:1.1.0")
    implementation("dev.langchain4j:langchain4j-ollama:1.1.0")
}

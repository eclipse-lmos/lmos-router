// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val langChain4jVersion: String by project

dependencies {
    api(project(":lmos-router-core"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.9.0")
    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jVersion")
}

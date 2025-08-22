// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val langChain4jModulesVersion: String by project
val langChain4jOpenAiVersion: String by project

dependencies {
    api(project(":lmos-router-core"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
}

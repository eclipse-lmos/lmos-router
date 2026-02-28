// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-classifier-core"))

    implementation("dev.langchain4j:langchain4j")

    compileOnly("dev.langchain4j:langchain4j-open-ai")
    compileOnly("dev.langchain4j:langchain4j-azure-open-ai")
    compileOnly("dev.langchain4j:langchain4j-anthropic")
    compileOnly("dev.langchain4j:langchain4j-google-ai-gemini")
    compileOnly("dev.langchain4j:langchain4j-ollama")
    compileOnly("com.azure:azure-identity")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.mvel:mvel2")

    testImplementation("org.assertj:assertj-core")
    testImplementation("dev.langchain4j:langchain4j-open-ai")
    testImplementation("dev.langchain4j:langchain4j-azure-open-ai")
    testImplementation("dev.langchain4j:langchain4j-anthropic")
    testImplementation("dev.langchain4j:langchain4j-google-ai-gemini")
    testImplementation("dev.langchain4j:langchain4j-ollama")
    testImplementation("com.azure:azure-identity")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

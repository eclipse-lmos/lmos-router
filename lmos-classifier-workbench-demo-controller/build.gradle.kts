// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(project(":lmos-classifier-llm-spring-boot-starter"))
    implementation(project(":lmos-classifier-vector-spring-boot-starter"))
    implementation(project(":lmos-classifier-hybrid-spring-boot-starter"))

    implementation("dev.langchain4j:langchain4j-open-ai")
    implementation("dev.langchain4j:langchain4j-azure-open-ai")
    implementation("dev.langchain4j:langchain4j-anthropic")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini")
    implementation("dev.langchain4j:langchain4j-ollama")
    implementation("com.azure:azure-identity")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("net.logstash.logback:logstash-logback-encoder")
    implementation("tools.jackson.core:jackson-core")
    implementation("tools.jackson.core:jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}

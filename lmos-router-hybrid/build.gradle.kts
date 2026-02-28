// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    api(project(":lmos-router-llm"))
    api(project(":lmos-router-vector"))
    implementation("org.slf4j:slf4j-api")
    implementation("com.azure:azure-ai-openai")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    testImplementation("org.testcontainers:testcontainers-ollama")
}

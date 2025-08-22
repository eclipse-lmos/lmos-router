// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    api(project(":lmos-router-llm"))
    api(project(":lmos-router-vector"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("com.azure:azure-ai-openai:1.0.0-beta.16")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.9.0")
    implementation("io.ktor:ktor-client-cio-jvm:3.2.0")
    testImplementation("org.testcontainers:ollama:1.21.3")
}

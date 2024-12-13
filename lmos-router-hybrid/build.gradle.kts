// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    api(project(":lmos-router-llm"))
    api(project(":lmos-router-vector"))
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("com.azure:azure-ai-openai:1.0.0-beta.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    testImplementation("org.testcontainers:ollama:1.20.3")
}

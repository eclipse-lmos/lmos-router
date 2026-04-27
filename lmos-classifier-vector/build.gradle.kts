// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-classifier-core"))

    api("dev.langchain4j:langchain4j-embeddings")
    api("dev.langchain4j:langchain4j-hugging-face")
    implementation("dev.langchain4j:langchain4j-qdrant")

    implementation("tools.jackson.core:jackson-databind")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    implementation("dev.langchain4j:langchain4j-open-ai")

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-qdrant")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

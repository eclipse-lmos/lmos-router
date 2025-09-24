// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val langChain4jEmbeddingVersion: String by project
val langChain4jVersion: String by project
val jacksonVersion: String by project

dependencies {
    api(project(":lmos-classifier-core"))

    api("dev.langchain4j:langchain4j-embeddings:$langChain4jEmbeddingVersion")
    api("dev.langchain4j:langchain4j-hugging-face:$langChain4jEmbeddingVersion")
    implementation("dev.langchain4j:langchain4j-qdrant:$langChain4jEmbeddingVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")

    testImplementation("org.testcontainers:testcontainers:1.21.3")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("org.testcontainers:qdrant:1.21.3")
    testImplementation("org.awaitility:awaitility:4.3.0")
}

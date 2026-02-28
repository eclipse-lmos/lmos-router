// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    implementation("org.slf4j:slf4j-api")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm")
    testImplementation("org.testcontainers:testcontainers-ollama")
}

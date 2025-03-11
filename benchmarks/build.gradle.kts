// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation("org.apache.commons:commons-csv:1.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    implementation(project(":lmos-router-core"))
    implementation(project(":lmos-router-llm"))
    implementation(project(":lmos-router-vector"))
}

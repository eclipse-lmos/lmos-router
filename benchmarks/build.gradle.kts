// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation("org.apache.commons:commons-csv:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    implementation(project(":lmos-router-core"))
    implementation(project(":lmos-router-llm"))
    implementation(project(":lmos-router-vector"))
}

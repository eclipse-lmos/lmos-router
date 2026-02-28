// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation("org.apache.commons:commons-csv")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation(project(":lmos-router-core"))
    implementation(project(":lmos-router-llm"))
    implementation(project(":lmos-router-vector"))
}

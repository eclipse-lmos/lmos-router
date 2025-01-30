// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

val springBootVersion: String by rootProject.extra

dependencies {
    api(project(":lmos-router-core"))
    api(project(":lmos-router-llm"))
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    implementation("org.springframework.ai:spring-ai-core:1.0.0-M5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    testImplementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
}

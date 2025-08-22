// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val springBootVersion: String by project
val langChain4jModulesVersion: String by project
val langChain4jOpenAiVersion: String by project

dependencies {
    api(project(":lmos-classifier-core"))
    api(project(":lmos-classifier-llm"))
    api(project(":lmos-classifier-core-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
}

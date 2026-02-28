// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-classifier-core"))
    api(project(":lmos-classifier-llm"))
    api(project(":lmos-classifier-core-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("dev.langchain4j:langchain4j-open-ai")
}

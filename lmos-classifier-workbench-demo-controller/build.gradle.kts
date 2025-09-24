// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val springBootVersion: String by project
val langChain4jVersion: String by project
val jacksonVersion: String by project

dependencies {
    implementation(project(":lmos-classifier-llm-spring-boot-starter"))
    implementation(project(":lmos-classifier-vector-spring-boot-starter"))
    implementation(project(":lmos-classifier-hybrid-spring-boot-starter"))

    implementation("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-anthropic:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langChain4jVersion")
    implementation("com.azure:azure-identity:1.18.0")

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
}

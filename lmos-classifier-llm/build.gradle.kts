// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val langChain4jVersion: String by project
val jacksonVersion: String by project
val assertjVersion: String by project
val mvelVersion: String by project

dependencies {
    api(project(":lmos-classifier-core"))

    implementation("dev.langchain4j:langchain4j:$langChain4jVersion")

    compileOnly("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")
    compileOnly("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jVersion")
    compileOnly("dev.langchain4j:langchain4j-anthropic:$langChain4jVersion")
    compileOnly("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jVersion")
    compileOnly("dev.langchain4j:langchain4j-ollama:$langChain4jVersion")
    compileOnly("com.azure:azure-identity:1.18.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.mvel:mvel2:$mvelVersion")

    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")
    testImplementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jVersion")
    testImplementation("dev.langchain4j:langchain4j-anthropic:$langChain4jVersion")
    testImplementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jVersion")
    testImplementation("dev.langchain4j:langchain4j-ollama:$langChain4jVersion")
    testImplementation("com.azure:azure-identity:1.18.0")
}

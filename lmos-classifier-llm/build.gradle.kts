// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val langChain4jCoreVersion: String by project
val langChain4jModulesVersion: String by project
val langChain4jOpenAiVersion: String by project
val jacksonVersion: String by project
val assertjVersion: String by project

dependencies {
    api(project(":lmos-classifier-core"))

    implementation("dev.langchain4j:langchain4j:$langChain4jCoreVersion")

    compileOnly("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
    compileOnly("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
    compileOnly("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
    compileOnly("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    compileOnly("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
    compileOnly("com.azure:azure-identity:1.17.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("dev.langchain4j:langchain4j-open-ai:$langChain4jOpenAiVersion")
    testImplementation("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jModulesVersion")
    testImplementation("dev.langchain4j:langchain4j-anthropic:$langChain4jModulesVersion")
    testImplementation("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jModulesVersion")
    testImplementation("dev.langchain4j:langchain4j-ollama:$langChain4jModulesVersion")
    testImplementation("com.azure:azure-identity:1.17.0")
}

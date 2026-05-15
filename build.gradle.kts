// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

import com.vanniktech.maven.publish.DeploymentValidation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.System.getenv
import java.net.URI

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    kotlin("plugin.spring") apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.cyclonedx.bom") version "3.2.4" apply false
    id("net.researchgate.release") version "3.1.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

subprojects {
    group = "org.eclipse.lmos"

    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "org.cyclonedx.bom")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "io.spring.dependency-management")

    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://repo.spring.io/snapshot") }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-parameters")
            jvmTarget = JvmTarget.JVM_25
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            val langChain4jVersion = "1.9.1"
            val langChain4jEmbeddingVersion = "1.14.1-beta24"
            val jacksonVersion = "3.1.3"
            val springCloudVersion = "1.0.0-M6"

            dependency("tools.jackson.core:jackson-core:$jacksonVersion")
            dependency("tools.jackson.core:jackson-databind:$jacksonVersion")
            dependency("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
            dependency("dev.langchain4j:langchain4j:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-open:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-anthropic:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-ollama:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-open-ai:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-azure-open-ai:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-anthropic:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-google-ai-gemini:$langChain4jVersion")
            dependency("dev.langchain4j:langchain4j-ollama:$langChain4jVersion")

            dependency("dev.langchain4j:langchain4j-embeddings:$langChain4jEmbeddingVersion")
            dependency("dev.langchain4j:langchain4j-qdrant:$langChain4jEmbeddingVersion")
            dependency("dev.langchain4j:langchain4j-hugging-face:$langChain4jEmbeddingVersion")

            dependency("org.mvel:mvel2:2.5.2.Final")
            dependency("io.ktor:ktor-client-cio-jvm:3.5.0")
            dependency("org.apache.commons:commons-csv:1.14.1")
            dependency("com.azure:azure-ai-openai:1.0.0-beta.16")
            dependency("com.azure:azure-identity:1.18.3")
            dependency("io.mockk:mockk:1.14.9")
            dependency("net.logstash.logback:logstash-logback-encoder:9.0")
            dependency("org.springframework.ai:spring-ai-core:$springCloudVersion")
            dependency("org.springframework.ai:spring-ai-openai-spring-boot-starter:$springCloudVersion")
            dependency("org.springframework.ai:spring-ai-qdrant-store-spring-boot-starter:$springCloudVersion")
            dependency("org.springframework.cloud:spring-cloud-starter-gateway:4.3.4")
        }
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.junit.platform:junit-platform-launcher")
        "testImplementation"("org.assertj:assertj-core")
        "testImplementation"("io.mockk:mockk")
    }

    ktlint {
        version.set("1.5.0")
    }

    tasks.named("dokkaJavadoc") {
        mustRunAfter("checksum")
    }

    tasks.withType<Test> {
        val runFlowTests = project.findProperty("runFlowTests")?.toString()?.toBoolean() ?: false

        if (!runFlowTests) {
            exclude("**/*Flow*")
        }

        useJUnitPlatform()
    }

    mavenPublishing {
        publishToMavenCentral(automaticRelease = true, DeploymentValidation.PUBLISHED)
        signAllPublications()

        pom {
            name = "LMOS Router"
            description = "Efficient Agent Routing with SOTA Language and Embedding Models."
            url = "https://github.com/eclipse-lmos/lmos-router"
            licenses {
                license {
                    name = "Apache-2.0"
                    distribution = "repo"
                    url = "https://github.com/eclipse-lmos/lmos-router/blob/main/LICENSES/Apache-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "xmxnt"
                    name = "Amant Kumar"
                    email = "opensource@telekom.de"
                }
                developer {
                    id = "jas34"
                    name = "Jasbir Singh"
                    email = "opensource@telekom.de"
                }
                developer {
                    id = "merrenfx"
                    name = "Max Erren"
                    email = "opensource@telekom.de"
                }
            }
            scm {
                url = "https://github.com/eclipse-lmos/lmos-router.git"
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url = URI("https://maven.pkg.github.com/eclipse-lmos/lmos-router")
                credentials {
                    username = findProperty("GITHUB_USER")?.toString() ?: getenv("GITHUB_USER")
                    password = findProperty("GITHUB_TOKEN")?.toString() ?: getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

fun Project.java(configure: Action<JavaPluginExtension>): Unit = (this as ExtensionAware).extensions.configure("java", configure)

fun String.execWithCode(workingDir: File? = null): Pair<CommandResult, Sequence<String>> {
    ProcessBuilder().apply {
        workingDir?.let { directory(it) }
        command(split(" "))
        redirectErrorStream(true)
        val process = start()
        val result = process.readStream()
        val code = process.waitFor()
        return CommandResult(code) to result
    }
}

class CommandResult(
    val code: Int,
) {
    val isFailed = code != 0
    val isSuccess = !isFailed

    fun ifFailed(block: () -> Unit) {
        if (isFailed) block()
    }
}

fun Project.isBOM() = name.endsWith("-bom")

private fun Process.readStream() =
    sequence<String> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) {
                    break
                }
                yield(line)
            }
        } finally {
            reader.close()
        }
    }

release {
    newVersionCommitMessage = "New Snapshot-Version:"
    preTagCommitMessage = "Release:"
}

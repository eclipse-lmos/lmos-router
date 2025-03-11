// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.System.getenv
import java.net.URI

plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.cyclonedx.bom") version "1.8.2" apply false
    id("net.researchgate.release") version "3.0.2"
    id("com.vanniktech.maven.publish") version "0.31.0"
}

val springBootVersion by extra { "3.3.5" }

subprojects {
    group = "org.eclipse.lmos"

    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "org.cyclonedx.bom")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://repo.spring.io/snapshot") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            freeCompilerArgs += "-Xcontext-receivers"
            jvmTarget = "21"
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.3")
        "testImplementation"("org.assertj:assertj-core:3.26.3")
        "testImplementation"("io.mockk:mockk:1.13.13")
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
        publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
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

class CommandResult(val code: Int) {
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

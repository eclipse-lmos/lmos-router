// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val springBootVersion: String by project

dependencies {
    api(project(":lmos-classifier-core"))
    api(project(":lmos-classifier-vector"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
}

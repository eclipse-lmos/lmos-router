// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "org.eclipse.lmos.lmos-router"
include("lmos-router-core")
include("lmos-router-llm")
include("lmos-router-llm-spring-boot-starter")
include("lmos-router-vector")
include("lmos-router-vector-spring-boot-starter")
include("lmos-router-llm-in-spring-cloud-gateway-demo")
include("benchmarks")
include("lmos-router-hybrid")
include("lmos-router-hybrid-spring-boot-starter")

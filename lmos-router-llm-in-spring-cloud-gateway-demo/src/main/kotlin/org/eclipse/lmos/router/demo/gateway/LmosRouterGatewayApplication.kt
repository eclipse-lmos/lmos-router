// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.demo.gateway

import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.llm.LLMAgentRoutingSpecsResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
open class LmosRouterGatewayApplication

fun main(args: Array<String>) {
    runApplication<LmosRouterGatewayApplication>(*args)
}

@Configuration
open class GatewayConfiguration {
    @Bean
    open fun routeLocator(
        builder: RouteLocatorBuilder,
        agentRoutingSpecsResolver: AgentRoutingSpecsResolver,
    ): RouteLocator =
        builder
            .routes()
            .route("lmos-router-demo") { r ->
                r
                    .path("/agents")
                    .filters { f ->
                        f.filter { exchange, chain ->
                            val req = exchange.request
                            addOriginalRequestUrl(exchange, req.uri)
                            val agentRoutingSpec =
                                resolveAgentRoutingSpec(
                                    req.queryParams.get("userQuery")?.firstOrNull(),
                                    agentRoutingSpecsResolver,
                                )
                            val newPath =
                                agentRoutingSpec?.addresses?.first()?.uri
                                    ?: throw RuntimeException("Unable to find agent URI.")
                            val request = req.mutate().path(newPath).build()
                            exchange.attributes[GATEWAY_REQUEST_URL_ATTR] = request.uri
                            chain.filter(exchange.mutate().request(request).build())
                        }
                    }.uri("http://localhost:9090")
            }.build()

    private fun resolveAgentRoutingSpec(
        userQuery: String?,
        agentRoutingSpecsResolver: AgentRoutingSpecsResolver,
    ): AgentRoutingSpec? {
        if (userQuery == null) {
            throw IllegalStateException("User query not found. Can't route request to any agent.")
        }

        val result = agentRoutingSpecsResolver.resolve(Context(emptyList()), UserMessage(userQuery))
        return result.getOrThrow()
    }

    @Bean
    open fun agentRoutingSpecsResolver(): AgentRoutingSpecsResolver {
        val provider =
            SimpleAgentRoutingSpecProvider()
                .add(
                    AgentRoutingSpecBuilder()
                        .name("offer-agent")
                        .description("This agent is responsible for offer management")
                        .version("1.0.0")
                        .address(Address(uri = "/agents/offer-agent"))
                        .build(),
                ).add(
                    AgentRoutingSpecBuilder()
                        .name("service-agent")
                        .description("This agent is responsible for service management")
                        .version("1.0.0")
                        .address(Address(uri = "/agents/service-agent"))
                        .build(),
                )
        return LLMAgentRoutingSpecsResolver(provider)
    }
}

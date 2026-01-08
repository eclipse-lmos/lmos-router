// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.InputContext
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.llm.AgentProvider
import org.junit.jupiter.api.Test

internal class DefaultAgentAggregatorTest {
    @Test
    fun `aggregate returns agents from all providers`(): Unit =
        runBlocking {
            // given
            val someProvider =
                object : AgentProvider {
                    override suspend fun provide(request: ClassificationRequest) = listOf(agent("some-agent-1"), agent("some-agent-2"))
                }
            val anotherProvider =
                object : AgentProvider {
                    override suspend fun provide(request: ClassificationRequest) = listOf(agent("another-agent-1"))
                }
            val aggregator = DefaultAgentAggregator(listOf(someProvider, anotherProvider))

            // when
            val result = aggregator.aggregate(classificationRequest())

            // then
            assertThat(result)
                .extracting<String> { it.id }
                .containsExactlyInAnyOrder("some-agent-1", "some-agent-2", "another-agent-1")
                .hasSize(3)
        }

    @Test
    fun `aggregate removes duplicate agents by id`(): Unit =
        runBlocking {
            // given
            val someProvider =
                object : AgentProvider {
                    override suspend fun provide(request: ClassificationRequest) = listOf(agent("duplicate-agent-1"), agent("some-agent-1"))
                }
            val anotherProvider =
                object : AgentProvider {
                    override suspend fun provide(request: ClassificationRequest) =
                        listOf(agent("duplicate-agent-1"), agent("another-agent-1"))
                }
            val aggregator = DefaultAgentAggregator(listOf(someProvider, anotherProvider))

            // when
            val result = aggregator.aggregate(classificationRequest())

            // then
            assertThat(result)
                .extracting<String> { it.id }
                .containsExactlyInAnyOrder("duplicate-agent-1", "some-agent-1", "another-agent-1")
                .hasSize(3)
        }

    @Test
    fun `aggregate ignores provider that throws exception and still collect others`(): Unit =
        runBlocking {
            // given
            val someProvider =
                object : AgentProvider {
                    override suspend fun provide(request: ClassificationRequest) = listOf(agent("some-agent-1"))
                }
            val failingProvider =
                object : AgentProvider {
                    override suspend fun provide(request: ClassificationRequest): List<Agent> = throw RuntimeException("boom")
                }
            val aggregator = DefaultAgentAggregator(listOf(failingProvider, someProvider))

            // when
            val result = aggregator.aggregate(classificationRequest())

            // then
            assertThat(result)
                .extracting<String> { it.id }
                .containsExactly("some-agent-1")
                .hasSize(1)
        }

    private fun agent(id: String) = Agent(id = id, name = id, address = "http://$id", capabilities = emptyList())

    private fun classificationRequest(): ClassificationRequest =
        ClassificationRequest(
            inputContext = InputContext(userMessage = "Hey there!"),
            systemContext = SystemContext(tenantId = "tenantId", channelId = "channelId"),
        )
}

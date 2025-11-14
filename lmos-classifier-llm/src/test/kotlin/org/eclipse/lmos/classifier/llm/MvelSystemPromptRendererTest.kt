// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eclipse.lmos.classifier.core.Agent
import org.eclipse.lmos.classifier.core.Capability
import org.eclipse.lmos.classifier.core.ClassificationRequest
import org.eclipse.lmos.classifier.core.InputContext
import org.eclipse.lmos.classifier.core.SystemContext
import org.eclipse.lmos.classifier.core.llm.SystemPromptRendererException
import org.junit.jupiter.api.Test

class MvelSystemPromptRendererTest {
    private val underTest = MvelSystemPromptRenderer()
    private val request =
        ClassificationRequest(
            inputContext =
                InputContext(
                    userMessage = "What's the weather today?",
                ),
            systemContext =
                SystemContext(
                    tenantId = "tenant1",
                    channelId = "channel1",
                ),
        )

    @Test
    fun `render resolves simple scalar variables`() {
        val template = "Agents: @{agent}"

        val out =
            underTest.render(
                template,
                mapOf("agent" to "sim-agent"),
            )

        assertThat(out).isEqualTo("Agents: sim-agent")
    }

    @Test
    fun `render resolves complex variables`() {
        val template =
            """
            Agents: @foreach{agent : agents}@{agent.id} @foreach{cap : agent.capabilities}(@{cap.description})@end{}@end{', '}
            """.trimIndent()

        val agents =
            listOf(
                Agent(
                    id = "weather-bot",
                    name = "weather-bot-name",
                    address = "weather-bot-address",
                    capabilities = listOf(Capability(id = "weather", description = "Provides weather forecasts")),
                ),
                Agent(
                    id = "news-bot",
                    name = "weather-bot-name",
                    address = "weather-bot-address",
                    capabilities = listOf(Capability(id = "news", description = "Provides latest news")),
                ),
            )

        val out =
            underTest.render(
                template,
                mapOf("agents" to agents),
            )

        assertThat(out).isEqualTo(
            """
            Agents: weather-bot (Provides weather forecasts), news-bot (Provides latest news)
            """.trimIndent(),
        )
    }

    @Test
    fun `render throws SystemPromptRendererException on MVEL syntax error`() {
        val template = "Hello @{user"

        assertThatThrownBy {
            underTest.render(
                template,
                mapOf("user" to "Stefan"),
            )
        }.isInstanceOf(SystemPromptRendererException::class.java)
            .hasMessageContaining("Failed to render prompt template")
    }

    @Test
    fun `render throws SystemPromptRendererException when required variable is missing`() {
        val template = "Hello @{user}!"

        assertThatThrownBy {
            underTest.render(
                template,
                emptyMap(),
            )
        }.isInstanceOf(SystemPromptRendererException::class.java)
            .hasMessageContaining("Failed to render prompt template")
    }
}

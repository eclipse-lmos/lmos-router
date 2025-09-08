// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExtractAgentIdTest {
    @Test
    fun `should return agent ID when valid ID is provided`() {
        // when
        val result = extractAgentId("weather-bot")

        // then
        assertThat(result).isEqualTo("weather-bot")
    }

    @Test
    fun `should return agent ID when invalid ID is provided`() {
        val result = extractAgentId("'weather-bot'")

        // then
        assertThat(result).isEqualTo("weather-bot")
    }

    @Test
    fun `should return agent ID when invalid ID is provided2`() {
        val result = extractAgentId("[weather-bot]")

        // then
        assertThat(result).isEqualTo("weather-bot")
    }

    @Test
    fun `should return agent ID when invalid ID is provided3`() {
        val result = extractAgentId("{weather-bot]")

        // then
        assertThat(result).isEqualTo("weather-bot")
    }

    @Test
    fun `should return null when AI message is NULL`() {
        val result = extractAgentId("NULL")

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `should return null when AI message is null`() {
        val result = extractAgentId("null")

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `should return null when AI message is 'null'`() {
        val result = extractAgentId("'null'")

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `should return null when AI message text is empty`() {
        val result = extractAgentId("")

        // then
        assertThat(result).isNull()
    }
}

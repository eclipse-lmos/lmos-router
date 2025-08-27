// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import kotlinx.serialization.Serializable

/**
 * Represents a model client response.
 *
 * The [agentName] field contains the name of the agent.
 */
@Serializable
open class ModelClientResponse(
    val agentName: String,
)

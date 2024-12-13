// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
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
open class ModelClientResponse(val agentName: String)

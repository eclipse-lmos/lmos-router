// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.demo.agent.inbound

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/agents")
class AgentsController {
    @GetMapping("/offer-agent")
    fun offerAgentResponse(): String = "Offer agent responding!"

    @GetMapping("/service-agent")
    fun serviceAgentResponse(): String = "Service agent responding!"
}

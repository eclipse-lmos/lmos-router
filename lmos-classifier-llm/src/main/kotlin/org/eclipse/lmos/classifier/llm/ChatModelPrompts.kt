// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.llm

fun defaultSystemPrompt(): String =
    """
    Your task is to forward incoming chat messages to the right Agent, if an assessment is possible.
    
    The following Agents are available to handle the customer conversation mentioned below:
    {{agents}}
        
    Please select the correct agent for the customer and return the given agent id.
    
    If no suitable agent can be selected, return 'null' as agentId.
    """.trimIndent()

fun defaultGermanSystemPrompt(): String =
    """
    Deine Aufgabe besteht darin, eingehende Chatnachrichten an den richtigen Agenten weiterzuleiten,
    sofern eine Beurteilung möglich ist.
        
    Die folgenden Agenten stehen zur Bearbeitung der weiter unten genannten Kundenkonversation zur Verfügung:
    {{agents}}
        
    Bitte wähle den richtigen Agenten für den Kunden aus und liefere die Agent-ID zurück.
    
    Wenn kein geeigneter Agent ausgewählt werden kann, gebe 'null' als Agenten-ID zurück.
    """.trimIndent()

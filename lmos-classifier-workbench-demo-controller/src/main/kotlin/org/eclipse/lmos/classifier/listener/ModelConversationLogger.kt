// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.listener

import dev.langchain4j.model.chat.listener.ChatModelErrorContext
import dev.langchain4j.model.chat.listener.ChatModelRequestContext
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import org.eclipse.lmos.classifier.llm.ClassifierChatModelListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ModelConversationLogger : ClassifierChatModelListener {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onRequest(requestContext: ChatModelRequestContext) {
        logger.info("onRequest(): {}", requestContext.chatRequest())
    }

    override fun onResponse(responseContext: ChatModelResponseContext) {
        logger.info("onResponse(): {}", responseContext.chatResponse())
    }

    override fun onError(errorContext: ChatModelErrorContext) {
        logger.info("onError(): {}", errorContext.error().message)
    }
}

// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResultTest {
    @Test
    fun `should return Success result`() {
        val result =
            "test".result<String, Exception> {
                "success"
            }
        assertTrue(result is Success)
        assertEquals("success", (result as Success).value)
    }

    @Test
    fun `should return Failure result on expected exception`() {
        val result =
            "test".result<String, IllegalArgumentException> {
                failWith { IllegalArgumentException("failure") }
            }
        assertTrue(result is Failure)
        assertEquals("failure", (result as Failure).reason.message)
    }

    @Test
    fun `should call finally blocks`() {
        var finallyCalled = false
        "test".result<String, Exception> {
            finally { finallyCalled = true }
            "success"
        }
        assertTrue(finallyCalled)
    }

    @Test
    fun `should throw exception with failWith`() {
        assertThrows(IllegalArgumentException::class.java) {
            val context = BasicResultBlock<String, IllegalArgumentException>()
            context.failWith { IllegalArgumentException("failure") }
        }
    }

    @Test
    fun `should ensure predicate is true`() {
        assertThrows(IllegalArgumentException::class.java) {
            val context = BasicResultBlock<String, IllegalArgumentException>()
            context.ensure(false) { IllegalArgumentException("failure") }
        }
    }

    @Test
    fun `should ensure predicate is not null`() {
        assertThrows(IllegalArgumentException::class.java) {
            val context = BasicResultBlock<String, IllegalArgumentException>()
            context.ensureNotNull(null) { IllegalArgumentException("failure") }
        }
    }

    @Test
    fun `should handle onFailure block`() {
        val result = Failure(IllegalArgumentException("failure")) as Result<String, IllegalArgumentException>
        var onFailureCalled = false
        result.onFailure { onFailureCalled = true }
        assertTrue(onFailureCalled)
    }

    @Test
    fun `should get value or throw exception`() {
        val successResult = Success("success") as Result<String, Exception>
        assertEquals("success", successResult.getOrThrow())

        val failureResult = Failure(IllegalArgumentException("failure")) as Result<String, IllegalArgumentException>
        assertThrows(IllegalArgumentException::class.java) { failureResult.getOrThrow() }
    }

    @Test
    fun `should get value or return null`() {
        val successResult = Success("success") as Result<String, Exception>
        assertEquals("success", successResult.getOrNull())

        val failureResult = Failure(IllegalArgumentException("failure")) as Result<String, IllegalArgumentException>
        assertNull(failureResult.getOrNull())
    }

    @Test
    fun `should mapFailure to a new type`() {
        val failureResult = Failure(IllegalArgumentException("failure")) as Result<String, IllegalArgumentException>
        val mappedResult = failureResult.mapFailure { IllegalStateException(it.message) }
        assertTrue(mappedResult is Failure)
        assertEquals("failure", (mappedResult as Failure).reason.message)
    }

    @Test
    fun `should map success value to a new type`() {
        val successResult = Success("success") as Result<String, Exception>
        val mappedResult = successResult.map { it.length }
        assertTrue(mappedResult is Success)
        assertEquals(7, (mappedResult as Success).value)
    }
}

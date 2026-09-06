package com.pluralsight.kai.flow

import kotlinx.coroutines.delay
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlin.time.Duration.Companion.milliseconds

// Retry helper — the boundary decision: retry BEFORE the first token, never mid-stream.
// second demo
fun <T> Flow<T>.retryBeforeFirstEmission(retries: Long = 2): Flow<T> {
    var emitted = false
    return this
        .onEach { emitted = false }
        .retryWhen { cause, attempt ->
            if (!emitted && attempt < retries && cause is IOException) {
                println("***** RETRY *****")
                delay(1000.milliseconds)
                true
            } else {
                false
            }
        }
}

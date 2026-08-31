package com.pluralsight.kai.ai

import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen

// Retry helper — the boundary decision: retry BEFORE the first token, never mid-stream.
// second demo
fun <T> Flow<T>.retryBeforeFirstEmission(retries: Long = 2): Flow<T> {
    var emitted = false
    return this
        .onEach { emitted = true }
        .retryWhen { cause, attempt ->
            !emitted && attempt < retries && cause is IOException
        }
}

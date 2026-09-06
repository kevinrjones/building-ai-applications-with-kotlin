package com.pluralsight.kai.breakme

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class FailFirstRequestInterceptor : Interceptor {
    private val attempts = AtomicInteger()
    override fun intercept(chain: Interceptor.Chain): Response {
            println("Intercepting")
        if (attempts.getAndIncrement() == 0) {
            println("Throwing exception")
            throw IOException("Demo failure")
        }
        return chain.proceed(chain.request())
    }
}
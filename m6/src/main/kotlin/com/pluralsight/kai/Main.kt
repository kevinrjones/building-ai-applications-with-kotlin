package com.pluralsight.kai

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.pluralsight.kai.ai.OpenAiCompatibleClient
import com.pluralsight.kai.breakme.FailFirstRequestInterceptor
import com.pluralsight.kai.cli.ReviewCommand
import com.pluralsight.kai.cli.AskCommand
import com.pluralsight.kai.cli.ClassifyCommand
import com.pluralsight.kai.cli.RewriteCommand
import com.pluralsight.kai.cli.RootCommand
import com.pluralsight.kai.cli.StreamCommand
import com.pluralsight.kai.cli.SummarizeCommand
import com.pluralsight.kai.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

    val config = AppConfig.fromEnv()

    // add after the initial code to show the log
    val wireLogger = LoggerFactory.getLogger("http.wire")
    val loggingInterceptor = HttpLoggingInterceptor { message ->
        wireLogger.info(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }

    val failureInterceptor = FailFirstRequestInterceptor()

    val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        // add this in to demonstrate the interceptor
//        .addInterceptor(failureInterceptor)
        .build()

    val client = OpenAiCompatibleClient(config, httpClient)

    RootCommand()
        .subcommands(
            AskCommand(client),
            SummarizeCommand(client),
            RewriteCommand(client),
            ClassifyCommand(client),
            ReviewCommand(client),
            StreamCommand(client),
            )
        .main(args)
}

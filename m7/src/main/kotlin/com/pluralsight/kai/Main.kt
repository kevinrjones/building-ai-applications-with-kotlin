package com.pluralsight.kai

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.pluralsight.kai.ai.OpenAiCompatibleClient
import com.pluralsight.kai.breakme.FailFirstRequestInterceptor
import com.pluralsight.kai.cli.*
import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.tools.CalculatorTool
import com.pluralsight.kai.tools.ToolRegistry
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
    val registry = ToolRegistry(tools = listOf(CalculatorTool()), allowed = setOf("calculator"))


    RootCommand()
        .subcommands(
            AskCommand(client),
            SummarizeCommand(client),
            RewriteCommand(client),
            ClassifyCommand(client),
            ReviewCommand(client),
            StreamCommand(client),
            AssistantCommand(client, registry),
            )
        .main(args)
}

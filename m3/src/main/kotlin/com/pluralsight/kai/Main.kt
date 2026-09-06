package com.pluralsight.kai

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.pluralsight.kai.ai.OpenAiCompatibleClient
import com.pluralsight.kai.cli.AskCommand
import com.pluralsight.kai.cli.RootCommand
import com.pluralsight.kai.cli.SummarizeCommand
import com.pluralsight.kai.config.AppConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory

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

    val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val client = OpenAiCompatibleClient(config, httpClient)

    RootCommand()
        .subcommands(
            AskCommand(client),
            SummarizeCommand(client),
            )
        .main(args)
}

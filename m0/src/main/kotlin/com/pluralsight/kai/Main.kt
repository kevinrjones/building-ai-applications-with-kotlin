package com.pluralsight.kai

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.pluralsight.kai.ai.OpenAiCompatibleClient
import com.pluralsight.kai.cli.AskCommand
import com.pluralsight.kai.cli.AssistantCommand
import com.pluralsight.kai.cli.ChatCommand
import com.pluralsight.kai.cli.ClassifyCommand
import com.pluralsight.kai.cli.EvalCommand
import com.pluralsight.kai.cli.QaCommand
import com.pluralsight.kai.cli.RewriteCommand
import com.pluralsight.kai.cli.RootCommand
import com.pluralsight.kai.cli.SummarizeCommand
import com.pluralsight.kai.cli.ReviewCommand
import com.pluralsight.kai.cli.StreamCommand
import com.pluralsight.kai.config.AppConfig
import com.pluralsight.kai.rag.FileDocumentEmbeddingStore
import com.pluralsight.kai.rag.Retriever
import com.pluralsight.kai.tools.CalculatorTool
import com.pluralsight.kai.tools.ToolRegistry
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) {
    // add after the initial code to show the log

    val wireLogger = LoggerFactory.getLogger("http.wire")

    val loggingInterceptor = HttpLoggingInterceptor { message ->
        wireLogger.info(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val config = AppConfig.fromEnv()
    val client = OpenAiCompatibleClient(config, http = httpClient)
    val registry = ToolRegistry(tools = listOf(CalculatorTool()), allowed = setOf("calculator"))
    val retriever =
        Retriever(
            client = client
        )

    RootCommand()
        .subcommands(
            AskCommand(client),
            SummarizeCommand(client),
            RewriteCommand(client),
            ClassifyCommand(client),
            ReviewCommand(client),
            StreamCommand(client),
            AssistantCommand(client, registry),
            QaCommand(client, retriever),
            ChatCommand(client),
            EvalCommand(client, retriever),
        )
        .main(args)
}


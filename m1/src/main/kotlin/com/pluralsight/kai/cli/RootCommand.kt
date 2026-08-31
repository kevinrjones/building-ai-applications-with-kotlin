package com.pluralsight.kai.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.pluralsight.kai.config.AppConfig

class RootCommand : CliktCommand("k-ai-assit") {
    override fun run() {
        val config = AppConfig.fromEnv()
        echo("k-ai-assist ready. Add a subcommand like 'ask'.")
    }
}
package com.pluralsight.kai

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.pluralsight.kai.cli.AskCommand
import com.pluralsight.kai.cli.RootCommand

fun main(args: Array<String>) =
    RootCommand()
        .subcommands(AskCommand())
        .main(args)
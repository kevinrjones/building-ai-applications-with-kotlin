package com.pluralsight.kai.tools

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger(name = "module")

class ToolRegistry(tools: List<Tool>, private val allowed: Set<String>) {
    private val byName: Map<String, Tool> = tools.associateBy { it.name }

    fun execute(name: String, argumentsJson: String): Result<String> {
        println("Try and execute tool $name")
        if (name !in allowed) {
            logger.warn { "tool rejected name=$name reason=not-permitted" }
            println("tool rejected name=$name reason=not-permitted")
            return Result.failure(IllegalAccessException("Tool not permitted: $name"))
        }
        val tool = byName[name] ?: return Result.failure(NoSuchElementException("Unknown tool: $name"))
        return tool.execute(argumentsJson)
    }
}

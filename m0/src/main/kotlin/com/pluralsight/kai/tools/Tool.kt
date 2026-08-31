package com.pluralsight.kai.tools

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface Tool {
    val name: String
    fun execute(argumentsJson: String): Result<String>
}

@Serializable
data class CalculatorArgs(val op: String, val a: Double, val b: Double)

class CalculatorTool(
    override val name: String = "calculator",
    private val json: Json = Json { ignoreUnknownKeys = true }
) : Tool{
    override fun execute(argumentsJson: String): Result<String> = runCatching {
        val args = json.decodeFromString<CalculatorArgs>(argumentsJson)
        val result = when(args.op) {
            "add" -> args.a + args.b
            "substract" -> args.a - args.b
            "multiply" -> args.a * args.b
            "divide" -> {
                require(args.b != 0.0) { "Division by zero" }
                args.a / args.b
            }
            else ->  error("Unsupported operation: ${args.op}")
        }

        """{"result": $result}"""
    }
}
package com.pluralsight.kai.config

data class AppConfig(
    val apiKey: String,
    val baseUrl: String,
    val chatModel: String,
    val embeddingModel: String
) {
    companion object {
        fun fromEnv(): AppConfig {
            val apiKey = requireEnv("OPENAI_API_KEY")
            return AppConfig(
                apiKey = apiKey,
                baseUrl = env("OPENAI_BASE_URL") ?: "https://api.openai.com/v1",
                chatModel = env("OPENAI_CHAT_MODEL") ?: "gpt-4o-mini",
                embeddingModel = env("OPENAI_EMBED_MODEL") ?: "text-embedding-3-small"
            )
        }

        private fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

        private fun requireEnv(name: String): String =
            env(name) ?: throw IllegalStateException("Missing required environment variable: $name")
    }
}

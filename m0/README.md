# k-ai-assist — Module 1 Checkpoint

State of the `k-ai-assist` CLI after **Module 1: Course Introduction & Setup**
of *Building AI Applications with Kotlin* (see `orig/demo.md`).

Contains the project scaffold: version catalog, `.gitignore`, the `AppConfig`
environment loader, and a placeholder `RootCommand`. No LLM calls yet.

## Setup

```bash
export OPENAI_API_KEY=sk-...
# Optional overrides:
# export OPENAI_BASE_URL=https://api.openai.com/v1
# export OPENAI_CHAT_MODEL=gpt-4o-mini
# export OPENAI_EMBED_MODEL=text-embedding-3-small
```

(The API key is loaded by `AppConfig` but not used until Module 2.)

## Commands

```bash
./gradlew run    # prints: k-ai-assist ready. Add a subcommand like 'ask'.
./gradlew test
```

## Architecture

1. CLI layer (`Clikt`) — `cli/RootCommand.kt`
2. Secure config loader — `config/AppConfig.kt`

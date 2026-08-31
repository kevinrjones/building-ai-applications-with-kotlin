package com.pluralsight.kai.memory

import com.pluralsight.kai.model.MessageDto
import com.pluralsight.kai.model.MessageRole

class ConversationMemory(private val maxTurns: Int = 12) {
    private val turns = mutableListOf<MessageDto>()

    fun add(role: MessageRole, content: String) {
        turns += MessageDto(role = role, content = content)
        trim()
    }

    fun size(): Int = messageCount()

    fun messageCount(): Int = turns.size

    fun turnCount(): Int = turns.count { it.role == MessageRole.USER }

    fun snapshot(): List<MessageDto> = turns.toList()

    fun replaceOldest(count: Int, summary: MessageDto) {
        repeat(minOf(count, turns.size)) { turns.removeAt(0) }
        turns.add(0, summary)
    }

    private fun trim() {
        while (turnCount() > maxTurns) {
            trimOldestTurn()
        }
    }

    private fun trimOldestTurn() {
        val oldestUserIndex = turns.indexOfFirst { it.role == MessageRole.USER }
        if (oldestUserIndex == -1) {
            turns.clear()
            return
        }

        val nextUserIndex = (oldestUserIndex + 1 until turns.size)
            .firstOrNull { index -> turns[index].role == MessageRole.USER }
            ?: turns.size

        repeat(nextUserIndex) { turns.removeAt(0) }
    }
}

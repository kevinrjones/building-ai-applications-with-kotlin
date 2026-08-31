package com.pluralsight.kai.model

import kotlinx.serialization.Serializable

/*
#PART 3 - Move to ReviewResult
 */

@Serializable
enum class Severity { LOW, MEDIUM, HIGH }

@Serializable
data class ReviewResult(
    val summary: String,
    val severity: Severity,
    val findings: List<String>
)

package com.pluralsight.kai.rag

import kotlin.math.sqrt

/**
 * Provides functionality to calculate the cosine similarity between two vectors.
 */
object Cosine {
    /**
     * Calculates the cosine similarity between two vectors represented as lists of doubles.
     *
     * @param a The first vector.
     * @param b The second vector. Must have the same size as the first vector.
     * @return The cosine similarity as a double value. Returns 0.0 if either vector has a magnitude of 0.
     */
    fun similarity(a: List<Double>, b: List<Double>): Double {
        require(a.size == b.size)
        val dot = a.indices.sumOf { i -> a[i] * b[i] }
        val magA = sqrt(a.sumOf { it * it })
        val magB = sqrt(b.sumOf { it * it })
        return if (magA == 0.0 || magB == 0.0) 0.0 else dot / (magA * magB)
    }
}

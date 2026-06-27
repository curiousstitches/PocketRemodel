package com.pocketremodel.app.ar

/** Maps spoken color words ("navy blue", "forest green") to normalized RGB. */
object ColorWords {

    data class Rgb(val r: Float, val g: Float, val b: Float)

    private val table = mapOf(
        "white" to Rgb(0.95f, 0.95f, 0.95f),
        "black" to Rgb(0.06f, 0.06f, 0.06f),
        "grey" to Rgb(0.5f, 0.5f, 0.52f),
        "gray" to Rgb(0.5f, 0.5f, 0.52f),
        "charcoal" to Rgb(0.2f, 0.21f, 0.23f),
        "red" to Rgb(0.78f, 0.16f, 0.16f),
        "navy" to Rgb(0.09f, 0.15f, 0.34f),
        "blue" to Rgb(0.16f, 0.34f, 0.78f),
        "teal" to Rgb(0.09f, 0.55f, 0.52f),
        "green" to Rgb(0.18f, 0.5f, 0.25f),
        "forest" to Rgb(0.11f, 0.3f, 0.17f),
        "olive" to Rgb(0.42f, 0.42f, 0.18f),
        "yellow" to Rgb(0.9f, 0.78f, 0.2f),
        "mustard" to Rgb(0.72f, 0.55f, 0.13f),
        "orange" to Rgb(0.86f, 0.45f, 0.13f),
        "terracotta" to Rgb(0.71f, 0.36f, 0.26f),
        "pink" to Rgb(0.9f, 0.55f, 0.65f),
        "purple" to Rgb(0.45f, 0.27f, 0.6f),
        "brown" to Rgb(0.38f, 0.26f, 0.16f),
        "walnut" to Rgb(0.3f, 0.2f, 0.12f),
        "oak" to Rgb(0.65f, 0.5f, 0.32f),
        "beige" to Rgb(0.8f, 0.74f, 0.6f),
        "cream" to Rgb(0.93f, 0.9f, 0.8f),
        "tan" to Rgb(0.74f, 0.6f, 0.42f),
    )

    /** Finds the first known color word inside a phrase. */
    fun toRgb(phrase: String): Rgb? {
        val words = phrase.lowercase().split(" ", "-", ",")
        for (w in words) table[w]?.let { return it }
        return null
    }
}

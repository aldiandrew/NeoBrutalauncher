package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class NeoQuotePalette(
    val background: Color,
    val text: Color
)

object NeoQuotes {
    private val defaultQuotes = listOf(
        "Raw structure is not a flaw. It is the message.",
        "Make the grid visible. Make the interface honest.",
        "Strong borders turn space into architecture.",
        "Function first, decoration second, apology never.",
        "Break the polish. Keep the hierarchy.",
        "Design boldly. Let every element earn its place.",
        "Clarity is powerful. Noise is optional.",
        "Build for people, not for applause.",
    )

    fun builtInQuotes(): List<String> = defaultQuotes

    fun allQuotes(customQuotes: List<String> = emptyList()): List<String> =
        (defaultQuotes + customQuotes)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

    fun pairForRotation(
        rotation: Int,
        customQuotes: List<String> = emptyList()
    ): Pair<String, String> {
        val quotes = allQuotes(customQuotes)
        if (quotes.size == 1) return quotes.first() to quotes.first()
        val offset = Math.floorMod(rotation, quotes.size)
        return quotes[offset] to quotes[(offset + 1) % quotes.size]
    }

    fun paletteForRotation(rotation: Int): NeoQuotePalette {
        val backgrounds = BrutalColors.appPalette(0)
        val background = backgrounds[Math.floorMod(rotation, backgrounds.size)]
        return NeoQuotePalette(background, BrutalColors.Ink)
    }

    fun pairForToday(): Pair<String, String> {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return pairForRotation(day)
    }

    fun forToday(): String = pairForToday().first
}

package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.graphics.Color

data class NeoQuotePalette(
    val background: Color,
    val text: Color
)

object NeoQuotes {
    private val defaultQuotes = listOf(
        "Raw structure is not a flaw. It is the message.",
        "Make the grid visible. Make the interface honest.",
        "Strong borders turn space into architecture.",
        "A loud interface can still have a clear purpose.",
        "Function first, decoration second, apology never.",
        "Break the polish. Keep the hierarchy.",
        "Geometry can be expressive without becoming fragile.",
        "Let contrast do the talking.",
        "Every box is a decision. Make it intentional.",
        "Brutal does not mean chaotic. It means unapologetic.",
        "Expose the structure and the user sees the system.",
        "Simple shapes become bold when hierarchy is fearless.",
        "Build less chrome. Show more function.",
        "A useful interface does not need permission to be loud.",
        "Good spacing is structure you can feel.",
        "Make every pixel earn its place.",
        "Clarity survives even when the surface is rough.",
        "The grid is a tool, not a cage.",
        "Design the path. Then remove the noise.",
        "Small controls can carry big intent.",
        "A strong interface makes the next action obvious.",
        "Keep the edges sharp and the purpose sharper.",
        "Order is not minimalism. Order is control.",
        "Make the system visible, then make it useful."
    )

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

}

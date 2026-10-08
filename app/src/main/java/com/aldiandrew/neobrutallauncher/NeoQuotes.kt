package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class NeoQuotePalette(
    val background: Color,
    val text: Color
)

object NeoQuotes {
    private val quotes = listOf(
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

    fun pairForRotation(rotation: Int): Pair<String, String> {
        val offset = Math.floorMod(rotation, quotes.size)
        return quotes[offset] to quotes[(offset + 1) % quotes.size]
    }

    private val palettes = listOf(
        NeoQuotePalette(BrutalColors.Yellow, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Pink, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Cyan, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Lime, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Orange, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Purple, BrutalColors.White),
        NeoQuotePalette(BrutalColors.Red, BrutalColors.White),
        NeoQuotePalette(BrutalColors.Peach, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Lavender, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Sky, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.Mint, BrutalColors.Ink),
        NeoQuotePalette(BrutalColors.White, BrutalColors.Ink)
    )

    fun paletteForRotation(rotation: Int): NeoQuotePalette {
        return palettes[Math.floorMod(rotation, palettes.size)]
    }

    fun pairForToday(): Pair<String, String> {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return pairForRotation(day)
    }

    fun forToday(): String = pairForToday().first
}

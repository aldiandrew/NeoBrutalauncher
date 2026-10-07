package com.aldiandrew.neobrutallauncher

import java.util.Calendar

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
        "Simple shapes become bold when hierarchy is fearless."
    )

    fun pairForToday(): Pair<String, String> {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val first = quotes[day % quotes.size]
        val second = quotes[(day + 1) % quotes.size]
        return first to second
    }

    fun forToday(): String {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[day % quotes.size]
    }
}

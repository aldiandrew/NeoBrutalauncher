package com.aldiandrew.neobrutallauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NeoQuotesTest {
    @Test
    fun customQuotesAreTrimmedAndBlankEntriesAreRemoved() {
        val quotes = NeoQuotes.allQuotes(listOf("  Custom quote  ", "", "   ", "Another quote"))

        assertEquals("Custom quote", quotes[10])
        assertEquals("Another quote", quotes[11])
        assertFalse(quotes.any { it.isBlank() })
    }

    @Test
    fun duplicateCustomQuotesAreRemovedWithoutRemovingDefaults() {
        val quotes = NeoQuotes.allQuotes(listOf("Custom quote", " Custom quote ", "Custom quote"))

        assertEquals(11, quotes.size)
        assertEquals(1, quotes.count { it == "Custom quote" })
        assertTrue(quotes.first().startsWith("Raw structure"))
    }

    @Test
    fun rotationReturnsAdjacentQuotesAndWrapsAtTheEnd() {
        val quotes = NeoQuotes.allQuotes()
        val pair = NeoQuotes.pairForRotation(quotes.lastIndex)

        assertEquals(quotes.last(), pair.first)
        assertEquals(quotes.first(), pair.second)
    }

    @Test
    fun negativeRotationWrapsSafelyToTheLastQuote() {
        val quotes = NeoQuotes.allQuotes()
        val pair = NeoQuotes.pairForRotation(-1)

        assertEquals(quotes.last(), pair.first)
        assertEquals(quotes.first(), pair.second)
    }
}

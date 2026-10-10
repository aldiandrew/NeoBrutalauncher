package com.aldiandrew.neobrutallauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTilesPackingTest {
    private fun tile(id: String, size: NeoTileSize) =
        NeoTileSpec(id = id, size = size, content = {})

    @Test
    fun emptyInputProducesNoPlacements() {
        assertTrue(packDense(emptyList()).isEmpty())
    }

    @Test
    fun mixedTileSizesStayInsideFourColumnsWithoutOverlaps() {
        val tiles = listOf(
            tile("small-1", NeoTileSize.SMALL),
            tile("wide-1", NeoTileSize.HORIZONTAL),
            tile("large-1", NeoTileSize.FOUR_BY_TWO),
            tile("three-1", NeoTileSize.THREE_BY_ONE),
            tile("full-1", NeoTileSize.FOUR_BY_ONE),
            tile("small-2", NeoTileSize.SMALL),
            tile("wide-2", NeoTileSize.HORIZONTAL)
        )

        assertValidPacking(tiles)
    }

    @Test
    fun everySupportedSizeCanBePackedRepeatedly() {
        val tiles = buildList {
            repeat(12) { index ->
                add(tile("small-$index", NeoTileSize.SMALL))
                add(tile("horizontal-$index", NeoTileSize.HORIZONTAL))
                add(tile("three-$index", NeoTileSize.THREE_BY_ONE))
                add(tile("four-$index", NeoTileSize.FOUR_BY_ONE))
                add(tile("large-$index", NeoTileSize.FOUR_BY_TWO))
            }
        }

        assertValidPacking(tiles)
    }

    @Test
    fun eachTileIsPlacedExactlyOnceAndOriginalOrderIsPreserved() {
        val tiles = listOf(
            tile("a", NeoTileSize.SMALL),
            tile("b", NeoTileSize.HORIZONTAL),
            tile("c", NeoTileSize.THREE_BY_ONE),
            tile("d", NeoTileSize.FOUR_BY_TWO)
        )

        val placements = packDense(tiles)

        assertEquals(tiles.map { it.id }, placements.map { it.first.id })
        assertEquals(tiles.size, placements.map { it.first.id }.toSet().size)
    }

    private fun assertValidPacking(tiles: List<NeoTileSpec>) {
        val placements = packDense(tiles)
        assertEquals("Every input tile must be placed", tiles.size, placements.size)
        assertEquals(
            "Every tile ID must appear once",
            tiles.map { it.id }.toSet(),
            placements.map { it.first.id }.toSet()
        )

        val occupied = mutableSetOf<Pair<Int, Int>>()
        placements.forEach { (tile, cell) ->
            val (column, row) = cell
            assertTrue("Negative column for ${tile.id}", column >= 0)
            assertTrue("Negative row for ${tile.id}", row >= 0)
            assertTrue(
                "Tile ${tile.id} exceeds the four-column grid",
                column + tile.size.columns <= 4
            )

            for (y in row until row + tile.size.rows) {
                for (x in column until column + tile.size.columns) {
                    assertTrue(
                        "Overlapping cell ($x, $y), tile ${tile.id}",
                        occupied.add(x to y)
                    )
                }
            }
        }
    }
}

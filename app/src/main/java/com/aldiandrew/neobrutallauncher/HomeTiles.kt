package com.aldiandrew.neobrutallauncher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

enum class NeoTileSize(
    val columns: Int,
    val rows: Int,
    val label: String
) {
    ONE_BY_ONE(
        columns = 1,
        rows = 1,
        label = "1×1"
    ),
    TWO_BY_TWO(
        columns = 2,
        rows = 2,
        label = "2×2"
    ),
    FOUR_BY_TWO(
        columns = 4,
        rows = 2,
        label = "4×2"
    ),
    FOUR_BY_FOUR(
        columns = 4,
        rows = 4,
        label = "4×4"
    )
}

data class NeoTileSpec(
    val id: String,
    val size: NeoTileSize,
    val content: @Composable () -> Unit
)

private data class NeoTilePlacement(
    val column: Int,
    val row: Int
)

@Composable
fun NeoTileGrid(
    tiles: List<NeoTileSpec>,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    gap: Dp = 10.dp
) {
    require(columns > 0) {
        "Tile grid must have at least one column"
    }

    tiles.forEach { tile ->
        require(tile.size.columns <= columns) {
            "Tile size is wider than the tile grid"
        }
    }

    Layout(
        modifier = modifier,
        content = {
            tiles.forEach { tile ->
                tile.content()
            }
        }
    ) { measurables, constraints ->
        val availableWidth = constraints.maxWidth
        val gapPx = gap.roundToPx()
        val cellWidth = if (columns == 1) {
            availableWidth
        } else {
            max(
                1,
                (availableWidth - gapPx * (columns - 1)) / columns
            )
        }
        val cellHeight = cellWidth

        val occupancy = mutableListOf<BooleanArray>()
        val placements = mutableListOf<NeoTilePlacement>()

        fun ensureRows(requiredRows: Int) {
            while (occupancy.size < requiredRows) {
                occupancy.add(BooleanArray(columns))
            }
        }

        fun canPlace(
            startColumn: Int,
            startRow: Int,
            width: Int,
            height: Int
        ): Boolean {
            ensureRows(startRow + height)

            for (row in startRow until startRow + height) {
                for (column in startColumn until startColumn + width) {
                    if (occupancy[row][column]) {
                        return false
                    }
                }
            }

            return true
        }

        fun occupy(
            startColumn: Int,
            startRow: Int,
            width: Int,
            height: Int
        ) {
            for (row in startRow until startRow + height) {
                for (column in startColumn until startColumn + width) {
                    occupancy[row][column] = true
                }
            }
        }

        tiles.forEach { tile ->
            val width = tile.size.columns
            val height = tile.size.rows

            var found = false
            var row = 0

            while (!found) {
                ensureRows(row + height)

                for (column in 0..(columns - width)) {
                    if (canPlace(column, row, width, height)) {
                        placements.add(
                            NeoTilePlacement(
                                column = column,
                                row = row
                            )
                        )
                        occupy(column, row, width, height)
                        found = true
                        break
                    }
                }

                if (!found) {
                    row++
                }
            }
        }

        val gridRows = occupancy.size

        val placeables = measurables.mapIndexed { index, measurable ->
            val size = tiles[index].size

            val width = cellWidth * size.columns + gapPx * (size.columns - 1)
            val height = cellHeight * size.rows + gapPx * (size.rows - 1)

            measurable.measure(
                Constraints.fixed(
                    width = width,
                    height = height
                )
            )
        }

        val layoutWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            cellWidth * columns + gapPx * (columns - 1)
        }

        val layoutHeight = if (gridRows == 0) {
            0
        } else {
            cellHeight * gridRows + gapPx * (gridRows - 1)
        }

        layout(
            width = layoutWidth,
            height = layoutHeight
        ) {
            placeables.forEachIndexed { index, placeable ->
                val placement = placements[index]
                val x = placement.column * (cellWidth + gapPx)
                val y = placement.row * (cellHeight + gapPx)

                placeable.place(
                    x = x,
                    y = y
                )
            }
        }
    }
}

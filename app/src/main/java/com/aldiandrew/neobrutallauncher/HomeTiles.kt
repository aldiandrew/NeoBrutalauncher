package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class NeoTileSize(
    val columns: Int,
    val rows: Int,
    val label: String
) {
    SMALL(1, 1, "SMALL"),
    HORIZONTAL(2, 1, "2x1"),
    MEDIUM(2, 2, "MEDIUM"),
    WIDE(4, 2, "WIDE"),
    LARGE(4, 4, "LARGE");

    fun next(): NeoTileSize = when (this) {
        SMALL -> HORIZONTAL
        HORIZONTAL -> MEDIUM
        MEDIUM -> WIDE
        WIDE -> LARGE
        LARGE -> SMALL
    }
}

enum class TileContentMode(val label: String) {
    ICON("ICON"),
    ICON_TEXT("ICON + TEXT"),
    TEXT("TEXT")
}

data class NeoTilePosition(
    val column: Int,
    val row: Int
)

data class NeoTileSpec(
    val id: String,
    val size: NeoTileSize,
    val label: String = id,
    val onClick: (() -> Unit)? = null,
    val content: @Composable () -> Unit
)

private const val TILE_COLUMNS = 4

private data class TilePlacement(
    val tile: NeoTileSpec,
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp
)

private fun packRows(tiles: List<NeoTileSpec>): List<List<NeoTileSpec>> {
    val rows = mutableListOf<List<NeoTileSpec>>()
    var row = mutableListOf<NeoTileSpec>()
    var usedColumns = 0

    for (tile in tiles) {
        val span = tile.size.columns

        if (span == TILE_COLUMNS && row.isNotEmpty()) {
            rows += row
            row = mutableListOf()
            usedColumns = 0
        }

        if (usedColumns + span > TILE_COLUMNS && row.isNotEmpty()) {
            rows += row
            row = mutableListOf()
            usedColumns = 0
        }

        row += tile
        usedColumns += span

        if (span == TILE_COLUMNS) {
            rows += row
            row = mutableListOf()
            usedColumns = 0
        }
    }

    if (row.isNotEmpty()) rows += row
    return rows
}

@Composable
fun NeoTileGrid(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>,
    onPositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    onTileLongPress: (NeoTileSpec) -> Unit = {},
    modifier: Modifier = Modifier,
    gap: Dp = 8.dp
) {
    // App tiles are intentionally fixed-order. The old drag/reorder implementation
    // caused the visible layout to mutate after returning from a launched app.
    // positions/onPositionsChange remain in the API for source compatibility.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellWidth =
            ((maxWidth - gap * (TILE_COLUMNS - 1)) / TILE_COLUMNS).coerceAtLeast(1.dp)
        val cellHeight = cellWidth
        val rows = packRows(tiles)

        var rowTop = 0.dp
        val placements = buildList {
            rows.forEach { row ->
                val rowHeightCells = row.maxOf { it.size.rows }
                val rowHeight =
                    cellHeight * rowHeightCells + gap * (rowHeightCells - 1)

                var column = 0
                row.forEach { tile ->
                    val tileWidth =
                        cellWidth * tile.size.columns + gap * (tile.size.columns - 1)
                    val tileHeight =
                        cellHeight * tile.size.rows + gap * (tile.size.rows - 1)

                    add(
                        TilePlacement(
                            tile = tile,
                            x = (cellWidth + gap) * column,
                            y = rowTop + (rowHeight - tileHeight) / 2,
                            width = tileWidth,
                            height = tileHeight
                        )
                    )
                    column += tile.size.columns
                }

                rowTop += rowHeight + gap
            }
        }

        val totalHeight = (rowTop - gap).coerceAtLeast(1.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        ) {
            placements.forEach { placement ->
                Box(
                    modifier = Modifier
                        .offset(x = placement.x, y = placement.y)
                        .width(placement.width)
                        .height(placement.height)
                        .clickable(
                            onClick = { placement.tile.onClick?.invoke() },
                            onLongClick = { onTileLongPress(placement.tile) }
                        )
                ) {
                    placement.tile.content()
                }
            }
        }
    }
}

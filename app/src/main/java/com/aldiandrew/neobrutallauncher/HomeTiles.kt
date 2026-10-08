package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity

enum class NeoTileSize(
    val columns: Int,
    val rows: Int,
    val label: String
) {
    SMALL(1, 1, "1x1"),
    HORIZONTAL(2, 1, "2x1"),
    THREE_BY_ONE(3, 1, "3x1"),
    FOUR_BY_ONE(4, 1, "4x1");

    fun next(): NeoTileSize = when (this) {
        SMALL -> HORIZONTAL
        HORIZONTAL -> THREE_BY_ONE
        THREE_BY_ONE -> FOUR_BY_ONE
        FOUR_BY_ONE -> SMALL
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

private data class GridPlacement(
    val tile: NeoTileSpec,
    val column: Int,
    val row: Int,
    val width: Dp,
    val height: Dp
)

private fun orderedTiles(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>
): List<NeoTileSpec> {
    val sourceIndexes = tiles.withIndex().associate { it.value.id to it.index }
    return tiles.sortedWith(
        compareBy<NeoTileSpec> { positions[it.id]?.row ?: Int.MAX_VALUE }
            .thenBy { positions[it.id]?.column ?: Int.MAX_VALUE }
            .thenBy { sourceIndexes[it.id] ?: Int.MAX_VALUE }
    )
}

/**
 * Dense 2D packing: every tile is placed in the first available rectangle
 * where it fits. This keeps the grid filled after resizing instead of leaving
 * artificial gaps caused by fixed row groups.
 */
private fun packDense(tiles: List<NeoTileSpec>): List<Pair<NeoTileSpec, Pair<Int, Int>>> {
    val occupied = mutableSetOf<Pair<Int, Int>>()
    val placements = mutableListOf<Pair<NeoTileSpec, Pair<Int, Int>>>()

    fun fits(column: Int, row: Int, width: Int, height: Int): Boolean {
        if (column + width > TILE_COLUMNS) return false
        for (y in row until row + height) {
            for (x in column until column + width) {
                if ((x to y) in occupied) return false
            }
        }
        return true
    }

    tiles.forEach { tile ->
        var placed = false
        var row = 0
        while (!placed) {
            for (column in 0..(TILE_COLUMNS - tile.size.columns)) {
                if (fits(column, row, tile.size.columns, tile.size.rows)) {
                    for (y in row until row + tile.size.rows) {
                        for (x in column until column + tile.size.columns) {
                            occupied += x to y
                        }
                    }
                    placements += tile to (column to row)
                    placed = true
                    break
                }
            }
            if (!placed) row++
        }
    }

    return placements
}

@Composable
fun NeoTileGrid(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>,
    onPositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    onTileLongPress: (NeoTileSpec) -> Unit = {},
    onTileEdit: (NeoTileSpec) -> Unit = {},
    onTileMoveFinished: () -> Unit = {},
    editMode: Boolean = false,
    modifier: Modifier = Modifier,
    gap: Dp = 8.dp
) {
    val density = LocalDensity.current
    val ordered = orderedTiles(tiles, positions)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellWidth =
            ((maxWidth - gap * (TILE_COLUMNS - 1)) / TILE_COLUMNS).coerceAtLeast(1.dp)
        val cellHeight = cellWidth

        val packed = packDense(ordered)
        val placements = packed.map { (tile, cell) ->
            GridPlacement(
                tile = tile,
                column = cell.first,
                row = cell.second,
                width = cellWidth * tile.size.columns + gap * (tile.size.columns - 1),
                height = cellHeight * tile.size.rows + gap * (tile.size.rows - 1)
            )
        }

        val totalRows = placements.maxOfOrNull { it.row + it.tile.size.rows } ?: 1
        val totalHeight = cellHeight * totalRows + gap * (totalRows - 1)

        var draggedId by remember { mutableStateOf<String?>(null) }
        var dragDelta by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight.coerceAtLeast(1.dp))
        ) {
            placements.forEach { placement ->
                val isDragging = placement.tile.id == draggedId
                val deltaX = if (isDragging) with(density) { dragDelta.x.toDp() } else 0.dp
                val deltaY = if (isDragging) with(density) { dragDelta.y.toDp() } else 0.dp

                Box(
                    modifier = Modifier
                        .offset(
                            x = cellWidth * placement.column + gap * placement.column + deltaX,
                            y = cellHeight * placement.row + gap * placement.row + deltaY
                        )
                        .width(placement.width)
                        .height(placement.height)
                        .combinedClickable(
                            onClick = {
                                if (editMode) {
                                    onTileEdit(placement.tile)
                                } else {
                                    placement.tile.onClick?.invoke()
                                }
                            },
                            onLongClick = { onTileLongPress(placement.tile) }
                        )
                ) {
                    placement.tile.content()

                    if (editMode) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(if (placement.tile.size == NeoTileSize.SMALL) 18.dp else 26.dp)
                            .background(Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isDragging) MaterialTheme.colorScheme.onBackground else BrutalColors.Ink
                            )
                            .pointerInput(placement.tile.id, placements) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggedId = placement.tile.id
                                        dragDelta = Offset.Zero
                                    },
                                    onDragEnd = {
                                        val draggedPlacement =
                                            placements.firstOrNull { it.tile.id == placement.tile.id }
                                        if (draggedPlacement != null) {
                                            val centerX =
                                                with(density) {
                                                    (cellWidth * draggedPlacement.column +
                                                        gap * draggedPlacement.column +
                                                        draggedPlacement.width / 2f).toPx()
                                                } + dragDelta.x
                                            val centerY =
                                                with(density) {
                                                    (cellHeight * draggedPlacement.row +
                                                        gap * draggedPlacement.row +
                                                        draggedPlacement.height / 2f).toPx()
                                                } + dragDelta.y

                                            val target = placements
                                                .filter { it.tile.id != placement.tile.id }
                                                .minByOrNull {
                                                    val targetX =
                                                        with(density) {
                                                            (cellWidth * it.column +
                                                                gap * it.column +
                                                                it.width / 2f).toPx()
                                                        }
                                                    val targetY =
                                                        with(density) {
                                                            (cellHeight * it.row +
                                                                gap * it.row +
                                                                it.height / 2f).toPx()
                                                        }
                                                    val dx = centerX - targetX
                                                    val dy = centerY - targetY
                                                    dx * dx + dy * dy
                                                }

                                            if (target != null) {
                                                val ids = ordered.map { it.id }.toMutableList()
                                                val fromIndex = ids.indexOf(placement.tile.id)
                                                val targetIndex = ids.indexOf(target.tile.id)

                                                if (fromIndex >= 0 && targetIndex >= 0 && fromIndex != targetIndex) {
                                                    val moved = ids.removeAt(fromIndex)
                                                    ids.add(targetIndex, moved)

                                                    val updatedPositions = ids
                                                        .withIndex()
                                                        .associate { (index, id) ->
                                                            id to NeoTilePosition(
                                                                column = 0,
                                                                row = index
                                                            )
                                                        }
                                                    onPositionsChange(updatedPositions)
                                                }
                                            }
                                        }

                                        draggedId = null
                                        dragDelta = Offset.Zero
                                        onTileMoveFinished()
                                    },
                                    onDragCancel = {
                                        draggedId = null
                                        dragDelta = Offset.Zero
                                        onTileMoveFinished()
                                    }
                                ) { _, amount ->
                                    dragDelta += amount
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (placement.tile.size == NeoTileSize.SMALL) "↕" else "DRAG",
                            fontSize = if (placement.tile.size == NeoTileSize.SMALL) 9.sp else 6.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDragging) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                BrutalColors.Ink
                            }
                        )
                    }
                        }
                    }
                }
            }
        }
    }

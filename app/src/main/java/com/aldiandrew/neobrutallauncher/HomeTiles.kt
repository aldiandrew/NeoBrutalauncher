package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Four Metro-style sizes matching the Vanta model:
 * SMALL 1×1, MEDIUM 2×2, WIDE 4×2, LARGE 4×4.
 *
 * Layout is row-based. There is intentionally no free-form (x/y) placement.
 * Tiles are kept in one order and greedily packed into 4-column rows, so
 * every drag immediately closes gaps instead of creating holes.
 */
enum class NeoTileSize(
    val columns: Int,
    val rows: Int,
    val label: String
) {
    SMALL(1, 1, "SMALL"),
    MEDIUM(2, 2, "MEDIUM"),
    WIDE(4, 2, "WIDE"),
    LARGE(4, 4, "LARGE");

    fun next(): NeoTileSize = when (this) {
        SMALL -> MEDIUM
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

/**
 * Kept as a compatibility format for existing saved preferences.
 * The grid now treats these values only as an ordering hint.
 */
data class NeoTilePosition(
    val column: Int,
    val row: Int
)

data class NeoTileSpec(
    val id: String,
    val size: NeoTileSize,
    val label: String = id,
    val content: @Composable () -> Unit
)

private const val TILE_COLUMNS = 4
private val TileRowHeight = 112.dp
private val LargeTileRowHeight = 234.dp
private const val DRAG_REORDER_THRESHOLD = 56f

/**
 * Convert the old persisted (column,row) map into one stable linear order.
 * Missing positions fall back to the current tile declaration order.
 */
private fun orderedTiles(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>
): List<NeoTileSpec> {
    val declared = tiles.withIndex().associate { it.value.id to it.index }
    return tiles.sortedWith(
        compareBy<NeoTileSpec> {
            positions[it.id]?.row ?: Int.MAX_VALUE
        }.thenBy {
            positions[it.id]?.column ?: Int.MAX_VALUE
        }.thenBy {
            declared[it.id] ?: Int.MAX_VALUE
        }.thenBy { it.id }
    )
}

/**
 * Vanta-style greedy row packing. A full-width tile closes the current row.
 */
private fun packRows(tiles: List<NeoTileSpec>): List<List<NeoTileSpec>> {
    val rows = mutableListOf<List<NeoTileSpec>>()
    var row = mutableListOf<NeoTileSpec>()
    var used = 0

    for (tile in tiles) {
        val span = tile.size.columns

        if (span == TILE_COLUMNS && row.isNotEmpty()) {
            rows += row
            row = mutableListOf()
            used = 0
        }

        if (used + span > TILE_COLUMNS && row.isNotEmpty()) {
            rows += row
            row = mutableListOf()
            used = 0
        }

        row += tile
        used += span

        if (span == TILE_COLUMNS) {
            rows += row
            row = mutableListOf()
            used = 0
        }
    }

    if (row.isNotEmpty()) rows += row
    return rows
}

private fun positionsForOrder(
    ordered: List<NeoTileSpec>
): Map<String, NeoTilePosition> {
    val result = linkedMapOf<String, NeoTilePosition>()
    var rowIndex = 0
    var column = 0

    for (tile in ordered) {
        val span = tile.size.columns

        if (span == TILE_COLUMNS) {
            if (column != 0) rowIndex++
            result[tile.id] = NeoTilePosition(0, rowIndex)
            rowIndex += if (tile.size == NeoTileSize.LARGE) 2 else 1
            column = 0
            continue
        }

        if (column + span > TILE_COLUMNS) {
            rowIndex++
            column = 0
        }

        result[tile.id] = NeoTilePosition(column, rowIndex)
        column += span

        if (column == TILE_COLUMNS) {
            rowIndex++
            column = 0
        }
    }

    return result
}

private fun moveItem(
    list: List<NeoTileSpec>,
    id: String,
    delta: Int
): List<NeoTileSpec> {
    val from = list.indexOfFirst { it.id == id }
    if (from < 0) return list

    val to = (from + delta).coerceIn(0, list.lastIndex)
    if (from == to) return list

    return list.toMutableList().apply {
        val item = removeAt(from)
        add(to, item)
    }
}

@Composable
fun NeoTileGrid(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>,
    onPositionsChange: (Map<String, NeoTilePosition>) -> Unit,
    onTileLongPress: (NeoTileSpec) -> Unit = {},
    modifier: Modifier = Modifier,
    gap: Dp = 10.dp
) {
    val baseOrder = remember(tiles, positions) {
        orderedTiles(tiles, positions)
    }
    var dragOrder by remember(baseOrder) { mutableStateOf(baseOrder) }
    var draggedId by remember { mutableStateOf<String?>(null) }

    val rows = packRows(dragOrder)

    ColumnWithRows(
        rows = rows,
        gap = gap,
        modifier = modifier,
        draggedId = draggedId,
        onDraggedIdChange = { draggedId = it },
        onReorder = { id, delta ->
            val updated = moveItem(dragOrder, id, delta)
            if (updated != dragOrder) {
                dragOrder = updated
            }
        },
        onDrop = {
            onPositionsChange(positionsForOrder(dragOrder))
            draggedId = null
        },
        onLongPress = onTileLongPress
    )
}

@Composable
private fun ColumnWithRows(
    rows: List<List<NeoTileSpec>>,
    gap: Dp,
    modifier: Modifier,
    draggedId: String?,
    onDraggedIdChange: (String?) -> Unit,
    onReorder: (String, Int) -> Unit,
    onDrop: () -> Unit,
    onLongPress: (NeoTileSpec) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        rows.forEach { row ->
            val rowHeight = if (row.any { it.size == NeoTileSize.LARGE }) {
                LargeTileRowHeight
            } else {
                TileRowHeight
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(Modifier)
                    .apply { },
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                row.forEach { tile ->
                    val latestDraggedId by rememberUpdatedState(draggedId)

                    NeoTileDraggable(
                        tile = tile,
                        modifier = Modifier
                            .weight(tile.size.columns.toFloat())
                            .fillMaxHeight(),
                        rowHeight = rowHeight,
                        isDragging = latestDraggedId == tile.id,
                        onDragState = onDraggedIdChange,
                        onReorder = onReorder,
                        onDrop = onDrop,
                        onLongPress = { onLongPress(tile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NeoTileDraggable(
    tile: NeoTileSpec,
    modifier: Modifier,
    rowHeight: Dp,
    isDragging: Boolean,
    onDragState: (String?) -> Unit,
    onReorder: (String, Int) -> Unit,
    onDrop: () -> Unit,
    onLongPress: () -> Unit
) {
    var dragOffsetY by remember(tile.id) { mutableFloatStateOf(0f) }
    var accumulatedY by remember(tile.id) { mutableFloatStateOf(0f) }
    var moved by remember(tile.id) { mutableStateOf(false) }

    val latestOnReorder by rememberUpdatedState(onReorder)
    val latestOnDrop by rememberUpdatedState(onDrop)
    val latestOnLongPress by rememberUpdatedState(onLongPress)
    val threshold = rowHeight.value * 0.5f

    Box(
        modifier = modifier
            .zIndex(if (isDragging) 10f else 0f)
            .graphicsLayer {
                translationY = dragOffsetY
                scaleX = if (isDragging) 1.05f else 1f
                scaleY = if (isDragging) 1.05f else 1f
                alpha = if (isDragging) 0.96f else 1f
                shadowElevation = if (isDragging) 18f else 0f
            }
            .pointerInput(tile.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragOffsetY = 0f
                        accumulatedY = 0f
                        moved = false
                        onDragState(tile.id)
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                        accumulatedY = 0f
                        moved = false
                        onDragState(null)
                    },
                    onDragEnd = {
                        val wasMoved = moved
                        dragOffsetY = 0f
                        accumulatedY = 0f
                        moved = false
                        if (!wasMoved) {
                            latestOnLongPress()
                        }
                        latestOnDrop()
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val dy = amount.y
                        dragOffsetY += dy
                        accumulatedY += dy

                        while (accumulatedY >= threshold) {
                            latestOnReorder(tile.id, 1)
                            accumulatedY -= threshold
                            moved = true
                        }
                        while (accumulatedY <= -threshold) {
                            latestOnReorder(tile.id, -1)
                            accumulatedY += threshold
                            moved = true
                        }
                    }
                )
            }
    ) {
        tile.content()
    }
}

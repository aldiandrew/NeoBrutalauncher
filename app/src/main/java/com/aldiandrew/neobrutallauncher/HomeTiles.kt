package com.aldiandrew.neobrutallauncher

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
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
import kotlin.math.abs

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
private const val REORDER_THRESHOLD_RATIO = 0.55f

private data class TilePlacement(
    val tile: NeoTileSpec,
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp
)

private fun orderedTiles(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>
): List<NeoTileSpec> {
    val declarationOrder = tiles.withIndex().associate { it.value.id to it.index }

    return tiles.sortedWith(
        compareBy<NeoTileSpec> { positions[it.id]?.row ?: Int.MAX_VALUE }
            .thenBy { positions[it.id]?.column ?: Int.MAX_VALUE }
            .thenBy { declarationOrder[it.id] ?: Int.MAX_VALUE }
            .thenBy { it.id }
    )
}

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

private fun positionsForOrder(
    ordered: List<NeoTileSpec>
): Map<String, NeoTilePosition> =
    ordered.mapIndexed { index, tile ->
        tile.id to NeoTilePosition(column = 0, row = index)
    }.toMap()

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
    gap: Dp = 8.dp
) {
    val initialOrder = remember(tiles, positions) {
        orderedTiles(tiles, positions)
    }

    var dragOrder by remember(initialOrder) { mutableStateOf(initialOrder) }
    var draggedId by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellWidth =
            ((maxWidth - gap * (TILE_COLUMNS - 1)) / TILE_COLUMNS).coerceAtLeast(1.dp)
        val cellHeight = cellWidth

        val rows = packRows(dragOrder)
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
                val tile = placement.tile
                val animatedX by animateDpAsState(
                    targetValue = placement.x,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 520f
                    ),
                    label = "tile-x"
                )
                val animatedY by animateDpAsState(
                    targetValue = placement.y,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 520f
                    ),
                    label = "tile-y"
                )
                val animatedWidth by animateDpAsState(
                    targetValue = placement.width,
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = 650f
                    ),
                    label = "tile-width"
                )
                val animatedHeight by animateDpAsState(
                    targetValue = placement.height,
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = 650f
                    ),
                    label = "tile-height"
                )

                NeoTileDraggable(
                    tile = tile,
                    x = animatedX,
                    y = animatedY,
                    width = animatedWidth,
                    height = animatedHeight,
                    isDragging = draggedId == tile.id,
                    rowStepPx = with(androidx.compose.ui.platform.LocalDensity.current) {
                        (cellHeight + gap).toPx()
                    },
                    onDragState = { draggedId = it },
                    onReorder = { id, delta ->
                        dragOrder = moveItem(dragOrder, id, delta)
                    },
                    onDrop = {
                        onPositionsChange(positionsForOrder(dragOrder))
                        draggedId = null
                    },
                    onLongPress = { onTileLongPress(tile) }
                )
            }
        }
    }
}

@Composable
private fun NeoTileDraggable(
    tile: NeoTileSpec,
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    isDragging: Boolean,
    rowStepPx: Float,
    onDragState: (String?) -> Unit,
    onReorder: (String, Int) -> Unit,
    onDrop: () -> Unit,
    onLongPress: () -> Unit
) {
    var dragOffsetY by remember(tile.id) { mutableFloatStateOf(0f) }
    var accumulatedY by remember(tile.id) { mutableFloatStateOf(0f) }
    var moved by remember(tile.id) { mutableStateOf(false) }

    val latestClick by rememberUpdatedState(tile.onClick)
    val latestOnDragState by rememberUpdatedState(onDragState)
    val latestOnReorder by rememberUpdatedState(onReorder)
    val latestOnDrop by rememberUpdatedState(onDrop)
    val latestOnLongPress by rememberUpdatedState(onLongPress)

    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .width(width)
            .height(height)
            .zIndex(if (isDragging) 10f else 0f)
            .graphicsLayer {
                translationY = dragOffsetY
                scaleX = if (isDragging) 1.045f else 1f
                scaleY = if (isDragging) 1.045f else 1f
                alpha = if (isDragging) 0.97f else 1f
                shadowElevation = if (isDragging) 18f else 0f
            }
            .pointerInput(tile.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val longPress = awaitLongPressOrCancellation(down.id)

                    if (longPress == null) {
                        val event = currentEvent
                        val movedBeforeLongPress = event?.changes?.any {
                            it.position != it.previousPosition
                        } == true

                        if (!movedBeforeLongPress) {
                            latestClick?.invoke()
                        }
                        return@awaitEachGesture
                    }

                    latestOnDragState(tile.id)
                    dragOffsetY = 0f
                    accumulatedY = 0f
                    moved = false

                    val threshold =
                        (rowStepPx * REORDER_THRESHOLD_RATIO).coerceAtLeast(24f)

                    val completed = drag(longPress.id) { change ->
                        val dy = (change.position.y - change.previousPosition.y)
                        if (dy == 0f) return@drag

                        change.consume()
                        dragOffsetY += dy
                        accumulatedY += dy

                        if (abs(dragOffsetY) > 6f) {
                            moved = true
                        }

                        while (accumulatedY >= threshold) {
                            latestOnReorder(tile.id, 1)
                            accumulatedY -= threshold
                            dragOffsetY -= threshold
                        }

                        while (accumulatedY <= -threshold) {
                            latestOnReorder(tile.id, -1)
                            accumulatedY += threshold
                            dragOffsetY += threshold
                        }
                    }

                    if (completed) {
                        if (!moved) {
                            latestOnLongPress()
                        }
                        dragOffsetY = 0f
                        accumulatedY = 0f
                        moved = false
                        latestOnDrop()
                    } else {
                        dragOffsetY = 0f
                        accumulatedY = 0f
                        moved = false
                        latestOnDragState(null)
                    }
                }
            }
    ) {
        tile.content()
    }
}

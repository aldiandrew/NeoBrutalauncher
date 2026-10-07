package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.max
import kotlin.math.roundToInt

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

data class NeoTilePosition(
    val column: Int,
    val row: Int
)

data class NeoTileSpec(
    val id: String,
    val size: NeoTileSize,
    val content: @Composable () -> Unit
)

private data class NeoTilePlacement(
    val column: Int,
    val row: Int
)

private fun calculateNeoTilePlacements(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>,
    columns: Int,
    priorityTileId: String? = null
): Map<String, NeoTilePlacement> {
    val occupancy = mutableListOf<BooleanArray>()
    val placements = linkedMapOf<String, NeoTilePlacement>()

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
        if (startColumn < 0 || startColumn + width > columns || startRow < 0) {
            return false
        }

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

    fun placeAt(
        tile: NeoTileSpec,
        position: NeoTilePosition
    ) {
        placements[tile.id] = NeoTilePlacement(
            column = position.column,
            row = position.row
        )
        occupy(
            startColumn = position.column,
            startRow = position.row,
            width = tile.size.columns,
            height = tile.size.rows
        )
    }

    val orderedTiles = if (priorityTileId != null) {
        buildList {
            tiles.firstOrNull { it.id == priorityTileId }?.let(::add)
            tiles.filterNot { it.id == priorityTileId }.forEach(::add)
        }
    } else {
        tiles
    }

    val deferred = mutableListOf<NeoTileSpec>()

    orderedTiles.forEach { tile ->
        val requested = positions[tile.id]

        if (
            requested != null &&
            canPlace(
                startColumn = requested.column,
                startRow = requested.row,
                width = tile.size.columns,
                height = tile.size.rows
            )
        ) {
            placeAt(tile, requested)
        } else {
            deferred += tile
        }
    }

    deferred.forEach { tile ->
        val width = tile.size.columns
        val height = tile.size.rows

        var found = false
        var row = 0

        while (!found) {
            ensureRows(row + height)

            for (column in 0..(columns - width)) {
                if (canPlace(column, row, width, height)) {
                    placeAt(
                        tile = tile,
                        position = NeoTilePosition(
                            column = column,
                            row = row
                        )
                    )
                    found = true
                    break
                }
            }

            if (!found) {
                row++
            }
        }
    }

    return placements
}

@Composable
fun NeoTileGrid(
    tiles: List<NeoTileSpec>,
    positions: Map<String, NeoTilePosition>,
    onPositionsChange: (Map<String, NeoTilePosition>) -> Unit,
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

    var gridWidthPx by remember { mutableStateOf(0) }

    val placements = calculateNeoTilePlacements(
        tiles = tiles,
        positions = positions,
        columns = columns
    )

    Layout(
        modifier = modifier.onGloballyPositioned {
            if (gridWidthPx != it.size.width) {
                gridWidthPx = it.size.width
            }
        },
        content = {
            tiles.forEach { tile ->
                val basePlacement = placements[tile.id] ?: NeoTilePlacement(
                    column = 0,
                    row = 0
                )

                NeoTileDraggable(
                    tile = tile,
                    basePlacement = basePlacement,
                    gridWidthPx = gridWidthPx,
                    columns = columns,
                    gap = gap,
                    onDrop = { targetPosition ->
                        val requestedPositions = positions.toMutableMap().apply {
                            put(tile.id, targetPosition)
                        }

                        val committedPlacements = calculateNeoTilePlacements(
                            tiles = tiles,
                            positions = requestedPositions,
                            columns = columns,
                            priorityTileId = tile.id
                        )

                        val committedPositions = committedPlacements.mapValues { (_, placement) ->
                            NeoTilePosition(
                                column = placement.column,
                                row = placement.row
                            )
                        }

                        onPositionsChange(committedPositions)
                    }
                )
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

        val measurePlacements = calculateNeoTilePlacements(
            tiles = tiles,
            positions = positions,
            columns = columns
        )

        val tileById = tiles.associateBy { it.id }
        val maxRow = measurePlacements.maxOfOrNull { (tileId, placement) ->
            placement.row + (tileById[tileId]?.size?.rows ?: 0)
        } ?: 0

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

        val layoutHeight = if (maxRow == 0) {
            0
        } else {
            cellHeight * maxRow + gapPx * (maxRow - 1)
        }

        layout(
            width = layoutWidth,
            height = layoutHeight
        ) {
            placeables.forEachIndexed { index, placeable ->
                val tile = tiles[index]
                val placement = measurePlacements[tile.id] ?: NeoTilePlacement(
                    column = 0,
                    row = 0
                )
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

@Composable
private fun NeoTileDraggable(
    tile: NeoTileSpec,
    basePlacement: NeoTilePlacement,
    gridWidthPx: Int,
    columns: Int,
    gap: Dp,
    onDrop: (NeoTilePosition) -> Unit
) {
    var dragOffset by remember(tile.id) {
        mutableStateOf(Offset.Zero)
    }
    var dragging by remember(tile.id) {
        mutableStateOf(false)
    }

    val latestBasePlacement by rememberUpdatedState(basePlacement)
    val latestOnDrop by rememberUpdatedState(onDrop)
    val density = LocalDensity.current
    val gapPx = with(density) {
        gap.toPx()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (dragging) 10f else 0f)
            .graphicsLayer {
                translationX = dragOffset.x
                translationY = dragOffset.y
                alpha = if (dragging) 0.94f else 1f
            }
            .pointerInput(tile.id, gridWidthPx, columns, gapPx) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragging = true
                        dragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        dragging = false
                        dragOffset = Offset.Zero
                    },
                    onDragEnd = {
                        if (gridWidthPx > 0) {
                            val base = latestBasePlacement
                            val cellWidth = if (columns == 1) {
                                gridWidthPx.toFloat()
                            } else {
                                max(
                                    1f,
                                    (gridWidthPx - gapPx * (columns - 1)) / columns.toFloat()
                                )
                            }
                            val step = cellWidth + gapPx

                            if (step > 0f) {
                                val targetColumn = (
                                    (base.column * step + dragOffset.x) / step
                                ).roundToInt().coerceIn(
                                    0,
                                    columns - tile.size.columns
                                )
                                val targetRow = (
                                    (base.row * step + dragOffset.y) / step
                                ).roundToInt().coerceAtLeast(0)

                                latestOnDrop(
                                    NeoTilePosition(
                                        column = targetColumn,
                                        row = targetRow
                                    )
                                )
                            }
                        }

                        dragging = false
                        dragOffset = Offset.Zero
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragOffset += amount
                    }
                )
            }
    ) {
        tile.content()
    }
}


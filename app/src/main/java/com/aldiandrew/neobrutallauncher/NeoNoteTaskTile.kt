package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NoteTaskKind(val title: String, val emptyLabel: String) {
    NOTES("NOTES", "TYPE A NOTE…"),
    TASKS("TASKS", "TYPE A TASK…")
}

data class NeoListItem(
    val text: String,
    val checked: Boolean = false
)

@Composable
fun NeoNoteTaskTile(
    kind: NoteTaskKind,
    items: List<NeoListItem>,
    modifier: Modifier = Modifier,
    onAddItem: (String) -> Unit,
    onItemTextChange: (Int, String) -> Unit,
    onToggleItem: (Int) -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    var draft by remember(kind) { mutableStateOf("") }
    val visibleItems = items.takeLast(3)
    val firstVisibleIndex = (items.size - visibleItems.size).coerceAtLeast(0)

    BrutalBlock(
        modifier = modifier,
        background = Color.Transparent,
        borderWidth = 3.dp,
        borderColor = textColor,
        shadowX = 0.dp,
        shadowY = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = kind.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = textColor
                )
                Box(
                    modifier = Modifier
                        .size(27.dp)
                        .border(2.dp, textColor, RoundedCornerShape(0.dp))
                        .clickable {
                            val clean = draft.trim()
                            if (clean.isNotEmpty()) {
                                onAddItem(clean)
                                draft = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Save",
                        tint = textColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                visibleItems.forEachIndexed { visibleIndex, item ->
                    val actualIndex = firstVisibleIndex + visibleIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (kind == NoteTaskKind.TASKS) {
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .border(2.dp, textColor, RoundedCornerShape(0.dp))
                                    .clickable { onToggleItem(actualIndex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.checked) {
                                    Text(
                                        text = "×",
                                        fontSize = 13.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .border(2.dp, BrutalColors.Pink, RoundedCornerShape(0.dp))
                            )
                        }

                        Spacer(Modifier.size(6.dp))
                        BasicTextField(
                            value = item.text,
                            onValueChange = { onItemTextChange(actualIndex, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = if (kind == NoteTaskKind.TASKS && item.checked) {
                                    textColor.copy(alpha = 0.48f)
                                } else textColor,
                                fontSize = 11.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                if (visibleItems.isEmpty()) {
                    Text(
                        text = if (kind == NoteTaskKind.NOTES) "NO SAVED NOTES" else "NO TASKS YET",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor.copy(alpha = 0.55f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 10.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (draft.isBlank()) {
                            Text(
                                text = kind.emptyLabel,
                                fontSize = 9.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )
                Text(
                    text = " + TO SAVE",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor.copy(alpha = 0.55f)
                )
            }
        }
    }
}

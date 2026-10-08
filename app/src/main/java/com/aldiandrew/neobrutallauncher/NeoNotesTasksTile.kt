package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeoNotesTasksTile(
    notes: List<NeoListItem>,
    tasks: List<NeoListItem>,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onAddNote: (String) -> Unit,
    onEditNote: (Int, String) -> Unit,
    onAddTask: (String) -> Unit,
    onEditTask: (Int, String) -> Unit,
    onToggleTask: (Int) -> Unit
) {
    var noteDraft by remember { mutableStateOf("") }
    var taskDraft by remember { mutableStateOf("") }

    BrutalBlock(
        modifier = modifier,
        background = background,
        borderWidth = 4.dp,
        borderColor = if (
            background == BrutalColors.DarkTile ||
            background == BrutalColors.DarkPaper
        ) BrutalColors.DarkWhite else BrutalColors.Ink,
        shadowX = 6.dp,
        shadowY = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NotesTasksColumn(
                title = "NOTES",
                inputHint = "TYPE NOTE…",
                items = notes,
                draft = noteDraft,
                modifier = Modifier.weight(1f),
                isTasks = false,
                onDraftChange = { noteDraft = it },
                onAdd = {
                    noteDraft.trim().takeIf { it.isNotEmpty() }?.let {
                        onAddNote(it)
                        noteDraft = ""
                    }
                },
                onEditItem = onEditNote,
                onToggleItem = {}
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
                    .background(textColor)
            )

            NotesTasksColumn(
                title = "TASKS",
                inputHint = "TYPE TASK…",
                items = tasks,
                draft = taskDraft,
                modifier = Modifier.weight(1f),
                isTasks = true,
                onDraftChange = { taskDraft = it },
                onAdd = {
                    taskDraft.trim().takeIf { it.isNotEmpty() }?.let {
                        onAddTask(it)
                        taskDraft = ""
                    }
                },
                onEditItem = onEditTask,
                onToggleItem = onToggleTask
            )
        }
    }
}

@Composable
private fun NotesTasksColumn(
    title: String,
    inputHint: String,
    items: List<NeoListItem>,
    draft: String,
    modifier: Modifier,
    isTasks: Boolean,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onEditItem: (Int, String) -> Unit,
    onToggleItem: (Int) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val visibleItems = items.takeLast(3)
    val firstIndex = (items.size - visibleItems.size).coerceAtLeast(0)

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = textColor
            )
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .border(2.dp, textColor, RoundedCornerShape(0.dp))
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Save $title",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            visibleItems.forEachIndexed { visibleIndex, item ->
                val actualIndex = firstIndex + visibleIndex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTasks) {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .border(2.dp, textColor, RoundedCornerShape(0.dp))
                                .clickable { onToggleItem(actualIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.checked) {
                                Text(
                                    text = "✓",
                                    fontSize = 11.sp,
                                    lineHeight = 11.sp,
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

                    Spacer(Modifier.size(5.dp))
                    BasicTextField(
                        value = item.text,
                        onValueChange = { onEditItem(actualIndex, it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = if (isTasks && item.checked) {
                                textColor.copy(alpha = 0.45f)
                            } else textColor,
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (visibleItems.isEmpty()) {
                Text(
                    text = if (isTasks) "NO TASKS YET" else "NO SAVED NOTES",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                ),
                decorationBox = { inner ->
                    if (draft.isBlank()) {
                        Text(
                            text = inputHint,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            )
            Text(
                text = " +",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

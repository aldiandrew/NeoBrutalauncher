package com.aldiandrew.neobrutallauncher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NoteTaskKind(val title: String, val emptyLabel: String) {
    NOTES("NOTES", "TAP TO WRITE"),
    TASKS("TASKS", "TAP TO WRITE")
}

@Composable
fun NeoNoteTaskTile(
    kind: NoteTaskKind,
    value: String,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    BrutalBlock(
        modifier = modifier.clickable(onClick = onEdit),
        background = Color.Transparent,
        borderWidth = 3.dp,
        borderColor = textColor,
        shadowX = 6.dp,
        shadowY = 6.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = kind.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = textColor
            )
            Text(
                text = value.ifBlank { kind.emptyLabel },
                fontSize = if (value.isBlank()) 13.sp else 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (value.isBlank()) "TAP TO WRITE" else "TAP TO EDIT",
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                color = textColor.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun NoteTaskEditorDialog(
    kind: NoteTaskKind,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var value by remember(initialValue, kind) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = kind.title, fontWeight = FontWeight.Black) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 12,
                label = { Text("WRITE " + kind.title) }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(value) },
                colors = ButtonDefaults.textButtonColors(contentColor = BrutalColors.Ink)
            ) {
                Text(text = "SAVE", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCEL", fontWeight = FontWeight.Black)
            }
        }
    )
}

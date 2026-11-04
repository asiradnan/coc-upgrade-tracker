package com.asiradnan.cocupgradetracker.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DeleteVillageConfirmDialog(
    villageName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismiss) {
        ClashPanelCard(
            headerTitle = "Delete Village?",
            headerColor = Color(0xFF7D241C)
        ) {
            Text(
                text = "\"$villageName\" and its tracked upgrade timers will be removed from this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClashBevelButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    style = ClashButtonStyle.NEUTRAL
                )
                ClashBevelButton(
                    text = "Delete",
                    onClick = onConfirmDelete,
                    style = ClashButtonStyle.RED
                )
            }
        }
    }
}

@Composable
fun AddVillageDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, rawJson: String) -> String?
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var nameText by rememberSaveable { mutableStateOf("") }
    var jsonText by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

    fun readClipboardText(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return clipboard?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        ClashPanelCard(
            headerTitle = "Add Village",
            headerColor = Color(0xFF575045)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Where to get your village JSON",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = colorScheme.primary
                        )
                        Text(
                            text = "In Clash of Clans, open Settings → More Settings → scroll down to Data Export and tap Copy to Clipboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Village name") },
                    placeholder = {
                        Text(
                            text = defaultName,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = colorScheme.surfaceContainerLow,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant,
                        cursorColor = colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Village export JSON",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    ClashBevelButton(
                        text = "Paste clipboard",
                        onClick = {
                            val clip = readClipboardText()
                            if (!clip.isNullOrBlank()) {
                                jsonText = clip
                                errorMsg = null
                            } else {
                                errorMsg = "Clipboard is empty"
                            }
                        },
                        style = ClashButtonStyle.BLUE,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMsg = null
                    },
                    placeholder = {
                        Text(
                            text = """{"tag": "#...", "timestamp": 1730000000, ...}""",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    isError = errorMsg != null,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = colorScheme.surfaceContainerLow,
                        errorContainerColor = colorScheme.surfaceContainerLow,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                        errorBorderColor = colorScheme.error,
                        cursorColor = colorScheme.primary
                    )
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClashBevelButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        style = ClashButtonStyle.NEUTRAL
                    )
                    ClashBevelButton(
                        text = "Add village",
                        onClick = {
                            val effectiveJson = if (jsonText.isBlank()) {
                                readClipboardText().orEmpty().also {
                                    if (it.isNotBlank()) jsonText = it
                                }
                            } else {
                                jsonText
                            }
                            val err = onSave(nameText.ifBlank { defaultName }, effectiveJson)
                            if (err != null) {
                                errorMsg = err
                            }
                        },
                        style = ClashButtonStyle.GREEN
                    )
                }
            }
        }
    }
}

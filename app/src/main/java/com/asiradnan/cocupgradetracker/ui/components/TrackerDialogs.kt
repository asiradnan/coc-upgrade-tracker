package com.asiradnan.cocupgradetracker.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.asiradnan.cocupgradetracker.data.CocVillageParser
import com.asiradnan.cocupgradetracker.data.ParseResult
import com.asiradnan.cocupgradetracker.ui.theme.PanelHeaderDanger
import com.asiradnan.cocupgradetracker.ui.theme.PanelHeaderStone

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
            headerColor = PanelHeaderDanger
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
    val focusManager = LocalFocusManager.current

    var nameText by rememberSaveable { mutableStateOf("") }
    var jsonText by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
    var showRawJson by rememberSaveable { mutableStateOf(false) }

    fun readClipboardText(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return clipboard?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
    }

    // Auto-detect JSON from clipboard on dialog open if not already set
    LaunchedEffect(Unit) {
        if (jsonText.isBlank()) {
            val clip = readClipboardText()
            if (CocVillageParser.looksLikeCocExport(clip)) {
                jsonText = clip.orEmpty()
            }
        }
    }

    // Validate and parse village export whenever jsonText changes
    val parsedResult = remember(jsonText) {
        if (jsonText.isBlank()) null
        else CocVillageParser.parse(jsonText, System.currentTimeMillis() / 1000L)
    }

    val parsedVillage = (parsedResult as? ParseResult.Success)?.village

    // Auto-fill village name if user hasn't typed one
    LaunchedEffect(parsedVillage?.username) {
        if (parsedVillage != null && nameText.isBlank()) {
            nameText = parsedVillage.username
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ClashPanelCard(
            headerTitle = "Add Village",
            headerColor = PanelHeaderStone
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
                            text = parsedVillage?.username ?: defaultName,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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

                if (parsedVillage != null) {
                    // Clean human-readable preview summary card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF4F3EF),
                        border = BorderStroke(1.5.dp, Color(0xFF4EB31B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4EB31B))
                                    )
                                    Text(
                                        text = "Valid Export Detected",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF2E6B08)
                                    )
                                }
                                Text(
                                    text = if (showRawJson) "Hide raw" else "Change",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2682DE)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            if (showRawJson) {
                                                showRawJson = false
                                            } else {
                                                jsonText = ""
                                                errorMsg = null
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            val builderCount = parsedVillage.totalHomeBuilders
                            val activeCount = parsedVillage.builderUpgrades.size + parsedVillage.labUpgrades.size
                            val builderText = "$builderCount Builder" + if (builderCount == 1) "" else "s"
                            val upgradeText = "$activeCount Active Upgrade" + if (activeCount == 1) "" else "s"
                            val summary = "Account: ${parsedVillage.username} • TH ${parsedVillage.townHallLevel} • $builderText • $upgradeText"

                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF261D15)
                            )
                        }
                    }

                    if (showRawJson) {
                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = {
                                jsonText = it
                                errorMsg = null
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = colorScheme.onSurface
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = colorScheme.surfaceContainerLow,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                                cursorColor = colorScheme.primary
                            )
                        )
                    }
                } else {
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

                    if (jsonText.isBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clip = readClipboardText()
                                    if (!clip.isNullOrBlank()) {
                                        jsonText = clip
                                        errorMsg = null
                                    } else {
                                        errorMsg = "Clipboard is empty"
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp, horizontal = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Tap to paste from clipboard",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = colorScheme.primary
                                )
                                Text(
                                    text = "Export will be verified & summarized automatically",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
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
                            isError = errorMsg != null || parsedResult is ParseResult.Error,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
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
                    }
                }

                val effectiveError = errorMsg ?: (parsedResult as? ParseResult.Error)?.message
                if (effectiveError != null) {
                    Text(
                        text = effectiveError,
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
                            val err = onSave(nameText.ifBlank { parsedVillage?.username ?: defaultName }, effectiveJson)
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

package com.asiradnan.cocupgradetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    villageCount: Int,
    onAddVillageClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.background,
                titleContentColor = colorScheme.onBackground,
                actionIconContentColor = colorScheme.onBackground
            ),
            title = {
                ClashOutlinedText(
                    text = "COC Upgrade Tracker",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        letterSpacing = 0.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actions = {
                ClashBevelButton(
                    text = "Add village",
                    leadingIcon = Icons.Default.Add,
                    onClick = onAddVillageClick,
                    style = ClashButtonStyle.BLUE,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        )
        ClashEtchedDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VillageDetailTopAppBar(
    villageName: String,
    selectedIndex: Int,
    totalVillages: Int,
    onHomeClick: () -> Unit,
    onNextVillageClick: () -> Unit,
    onSelectVillageClick: ((Int) -> Unit)? = null,
    onEditNameClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.background,
                titleContentColor = colorScheme.onBackground,
                navigationIconContentColor = colorScheme.onBackground,
                actionIconContentColor = colorScheme.onBackground
            ),
            navigationIcon = {
                ClashBevelIconButton(
                    icon = Icons.Default.Home,
                    contentDescription = "Home",
                    onClick = onHomeClick,
                    style = ClashButtonStyle.BLUE,
                    modifier = Modifier.padding(start = 10.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                enabled = onEditNameClick != null,
                                onClick = { onEditNameClick?.invoke() }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        ClashOutlinedText(
                            text = villageName,
                            style = MaterialTheme.typography.titleLarge,
                            outlineWidth = 1.6.dp,
                            shadowOffset = 1.0.dp,
                            shadowAlpha = 0.40f,
                            shadowBlurRadius = 1.0.dp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (onEditNameClick != null) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename village",
                                tint = Color(0xFF261D15).copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (totalVillages > 1) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until totalVillages.coerceAtMost(8)) {
                                val isSelected = i == selectedIndex
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable(
                                            enabled = onSelectVillageClick != null,
                                            onClick = { onSelectVillageClick?.invoke(i) }
                                        )
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 8.dp else 5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFF2682DE)
                                                else Color(0xFF261D15).copy(alpha = 0.28f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            actions = {
                ClashBevelIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next village",
                    onClick = onNextVillageClick,
                    style = ClashButtonStyle.BLUE,
                    enabled = totalVillages > 1,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        )
        ClashEtchedDivider()
    }
}

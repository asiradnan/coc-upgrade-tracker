package com.asiradnan.cocupgradetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    villageCount: Int,
    onGlobalPasteClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.background,
            titleContentColor = colorScheme.onBackground,
            actionIconContentColor = colorScheme.onBackground
        ),
        title = {
            ClashShadowedText(
                text = "COC Upgrade Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground
            )
        },
        actions = {
            ClashBevelButton(
                text = "Paste update",
                onClick = onGlobalPasteClick,
                style = ClashButtonStyle.GREEN,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VillageDetailTopAppBar(
    villageName: String,
    selectedIndex: Int,
    totalVillages: Int,
    onHomeClick: () -> Unit,
    onNextVillageClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

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
                ClashShadowedText(
                    text = villageName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (totalVillages > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until totalVillages.coerceAtMost(8)) {
                            val isSelected = i == selectedIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) colorScheme.primary
                                        else colorScheme.onBackground.copy(alpha = 0.35f)
                                    )
                            )
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
}

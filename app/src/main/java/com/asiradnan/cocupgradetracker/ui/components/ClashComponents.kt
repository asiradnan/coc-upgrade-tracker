package com.asiradnan.cocupgradetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ClashButtonStyle {
    GREEN,
    BLUE,
    GOLD,
    RED,
    NEUTRAL
}

private data class BevelPalette(
    val topGradient: Color,
    val bottomGradient: Color,
    val bottomLip: Color,
    val border: Color,
    val contentColor: Color,
    val useDarkShadow: Boolean = true
)

private fun ClashButtonStyle.toPalette(): BevelPalette = when (this) {
    ClashButtonStyle.GREEN -> BevelPalette(
        topGradient = Color(0xFF72CF34),
        bottomGradient = Color(0xFF4B9E15),
        bottomLip = Color(0xFF2E6B08),
        border = Color(0xFF1E3D08),
        contentColor = Color.White
    )
    ClashButtonStyle.BLUE -> BevelPalette(
        topGradient = Color(0xFF57ADFF),
        bottomGradient = Color(0xFF267BD1),
        bottomLip = Color(0xFF154E8A),
        border = Color(0xFF0E2F54),
        contentColor = Color.White
    )
    ClashButtonStyle.GOLD -> BevelPalette(
        topGradient = Color(0xFFFFD24C),
        bottomGradient = Color(0xFFE59612),
        bottomLip = Color(0xFF9C5E04),
        border = Color(0xFF4A2B00),
        contentColor = Color.White
    )
    ClashButtonStyle.RED -> BevelPalette(
        topGradient = Color(0xFFF25C4E),
        bottomGradient = Color(0xFFCC2F21),
        bottomLip = Color(0xFF85170D),
        border = Color(0xFF4A0B05),
        contentColor = Color.White
    )
    ClashButtonStyle.NEUTRAL -> BevelPalette(
        topGradient = Color(0xFFE8DFCE),
        bottomGradient = Color(0xFFCBBFA8),
        bottomLip = Color(0xFF968972),
        border = Color(0xFF574E40),
        contentColor = Color(0xFF2B251E),
        useDarkShadow = false
    )
}

val ClashTextShadow = Shadow(
    color = Color(0xD9000000),
    offset = Offset(0f, 3f),
    blurRadius = 1f
)

@Composable
fun ClashShadowedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.White,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            fontWeight = FontWeight.Black,
            shadow = ClashTextShadow
        ),
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign
    )
}

@Composable
fun ClashBevelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ClashButtonStyle = ClashButtonStyle.GREEN,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
) {
    val palette = style.toPalette()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = MaterialTheme.shapes.medium

    val lipHeight = if (isPressed || !enabled) 1.dp else 3.dp
    val topOffset = if (isPressed && enabled) 2.dp else 0.dp

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .padding(top = topOffset)
            .clip(shape)
            .background(if (enabled) palette.bottomLip else Color(0xFF44474C))
            .border(1.5.dp, if (enabled) palette.border else Color(0xFF2B2E33), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(bottom = lipHeight)
    ) {
        val bodyBrush = if (enabled) {
            Brush.verticalGradient(listOf(palette.topGradient, palette.bottomGradient))
        } else {
            Brush.verticalGradient(listOf(Color(0xFF6B6F75), Color(0xFF55595E)))
        }

        Box(
            modifier = Modifier
                .clip(shape)
                .background(bodyBrush)
                .drawWithContent {
                    drawContent()
                    if (enabled) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.28f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, 3.dp.toPx())
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = palette.contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (palette.useDarkShadow && enabled) {
                    ClashShadowedText(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.contentColor,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (enabled) palette.contentColor else Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ClashBevelIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ClashButtonStyle = ClashButtonStyle.BLUE,
    enabled: Boolean = true,
    buttonSize: Dp = 40.dp
) {
    val palette = style.toPalette()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = MaterialTheme.shapes.medium

    val lipHeight = if (isPressed || !enabled) 1.dp else 3.dp
    val topOffset = if (isPressed && enabled) 2.dp else 0.dp

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(buttonSize)
            .padding(top = topOffset)
            .clip(shape)
            .background(if (enabled) palette.bottomLip else Color(0xFF3A3D42))
            .border(1.5.dp, if (enabled) palette.border else Color(0xFF25282C), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(bottom = lipHeight)
    ) {
        val bodyBrush = if (enabled) {
            Brush.verticalGradient(listOf(palette.topGradient, palette.bottomGradient))
        } else {
            Brush.verticalGradient(listOf(Color(0xFF52565C), Color(0xFF42464B)))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(shape)
                .background(bodyBrush),
            contentAlignment = Alignment.Center
        ) {
            if (enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.28f))
                        .align(Alignment.TopCenter)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) palette.contentColor else Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ClashPanelCard(
    modifier: Modifier = Modifier,
    headerTitle: String? = null,
    headerColor: Color = Color(0xFF575045),
    headerTrailingContent: (@Composable RowScope.() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color(0xFF544B3E),
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (headerTitle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    headerColor,
                                    headerColor.copy(
                                        red = (headerColor.red * 0.85f).coerceIn(0f, 1f),
                                        green = (headerColor.green * 0.85f).coerceIn(0f, 1f),
                                        blue = (headerColor.blue * 0.85f).coerceIn(0f, 1f)
                                    )
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClashShadowedText(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    if (headerTrailingContent != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            content = headerTrailingContent
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(borderColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun ClashProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = Color(0xFF332D25),
    borderColor: Color = Color(0xFF241F19),
    height: Dp = 10.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(height / 2)

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(trackColor)
            .border(1.2.dp, borderColor, shape)
            .padding(1.2.dp)
    ) {
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(fillColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.38f)
                        .background(Color.White.copy(alpha = 0.32f))
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun ClashCrestBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
    shadowText: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        if (shadowText) {
            ClashShadowedText(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ClashTownHallIcon(
    townHallLevel: Int,
    modifier: Modifier = Modifier,
    isUninitialized: Boolean = false
) {
    val roofColor = when {
        isUninitialized -> Color(0xFF7B7366)
        townHallLevel <= 8 -> Color(0xFFE07A1F)   // Classic Orange-Gold roof (TH1-8)
        townHallLevel == 9 -> Color(0xFF3E434C)   // Dark Fortress slate (TH9)
        townHallLevel == 10 -> Color(0xFFC92E23)  // Magma Red (TH10)
        townHallLevel == 11 -> Color(0xFFD99B26)  // Ivory & Royal Gold (TH11)
        townHallLevel in 12..13 -> Color(0xFF287CD4) // Electric / Frost Blue (TH12-13)
        townHallLevel == 14 -> Color(0xFF2E9E49)  // Jungle Emerald (TH14)
        townHallLevel == 15 -> Color(0xFF7E38B8)  // Arcane Purple (TH15)
        townHallLevel == 16 -> Color(0xFFD44328)  // Solar Crimson & Gold (TH16)
        else -> Color(0xFF35457A)                 // Deep Indigo & Gold (TH17+)
    }

    val wallColor = when {
        isUninitialized -> Color(0xFFC9C0B1)
        townHallLevel == 9 -> Color(0xFF6B707B)
        townHallLevel == 11 -> Color(0xFFF2ECE1)
        else -> Color(0xFFDED3BE)
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF3A342C))
            .border(1.5.dp, Color(0xFF594F41), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val w = size.width
            val h = size.height

            // Building stone body
            drawRoundRect(
                color = wallColor,
                topLeft = Offset(w * 0.20f, h * 0.40f),
                size = Size(w * 0.60f, h * 0.46f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Gold trim band under roof
            drawRect(
                color = Color(0xFFF0B22B),
                topLeft = Offset(w * 0.18f, h * 0.39f),
                size = Size(w * 0.64f, h * 0.06f)
            )

            // Pitched Townhall roof
            val roofPath = Path().apply {
                moveTo(w * 0.10f, h * 0.41f)
                lineTo(w * 0.50f, h * 0.10f)
                lineTo(w * 0.90f, h * 0.41f)
                close()
            }
            drawPath(path = roofPath, color = roofColor)

            // Roof ridge highlight
            drawRoundRect(
                color = Color(0xFFF5C542),
                topLeft = Offset(w * 0.44f, h * 0.07f),
                size = Size(w * 0.12f, h * 0.08f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Dark arched entrance doorway
            drawRoundRect(
                color = Color(0xFF2B241D),
                topLeft = Offset(w * 0.39f, h * 0.54f),
                size = Size(w * 0.22f, h * 0.32f),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }

        // Level badge at the bottom right
        if (!isUninitialized) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xD91F1A14))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "TH$townHallLevel",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFFFD24C)
                )
            }
        }
    }
}

@Composable
fun ClashStopwatchIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.54f)
        val radius = w * 0.38f

        // Top crown ring / stem
        drawCircle(
            color = Color(0xFF1A120B),
            radius = w * 0.12f,
            center = Offset(w * 0.5f, h * 0.13f)
        )
        drawCircle(
            color = Color(0xFFF5B82E),
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.13f)
        )

        // Outer dark outline
        drawCircle(
            color = Color(0xFF1A120B),
            radius = radius + 2.dp.toPx(),
            center = center
        )

        // Gold metallic case
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFDF70), Color(0xFFD98A16)),
                startY = center.y - radius,
                endY = center.y + radius
            ),
            radius = radius,
            center = center
        )

        // Inner cream dial face
        val faceRadius = radius * 0.76f
        drawCircle(
            color = Color(0xFFFCF2D9),
            radius = faceRadius,
            center = center
        )

        // Subtle inner rim shadow
        drawCircle(
            color = Color(0xFFB87914),
            radius = faceRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Clock hands
        drawLine(
            color = Color(0xFF2B1E14),
            start = center,
            end = Offset(center.x, center.y - faceRadius * 0.62f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF2B1E14),
            start = center,
            end = Offset(center.x - faceRadius * 0.4f, center.y + faceRadius * 0.25f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFF2B1E14),
            radius = 1.6.dp.toPx(),
            center = center
        )
    }
}

enum class ClashCapsuleFillStyle {
    GREEN,
    ELIXIR
}

@Composable
fun ClashUpgradeCapsuleBar(
    titleText: String,
    rightText: String,
    progress: Float,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    showStopwatch: Boolean = isActive,
    fillStyle: ClashCapsuleFillStyle = ClashCapsuleFillStyle.GREEN,
    titleColor: Color = Color.White,
    rightTextColor: Color = Color.White,
    height: Dp = 36.dp
) {
    val shape = RoundedCornerShape(8.dp)
    val clampedProgress = if (isActive) progress.coerceIn(0.04f, 1f) else 0f

    val topFillColors = when (fillStyle) {
        ClashCapsuleFillStyle.GREEN -> listOf(Color(0xFF84F040), Color(0xFF6CD62B))
        ClashCapsuleFillStyle.ELIXIR -> listOf(Color(0xFFF564E8), Color(0xFFDE42CF))
    }
    val bottomFillColors = when (fillStyle) {
        ClashCapsuleFillStyle.GREEN -> listOf(Color(0xFF4EB31B), Color(0xFF3D9412))
        ClashCapsuleFillStyle.ELIXIR -> listOf(Color(0xFFB825AB), Color(0xFF8F1484))
    }
    val edgeHighlightColor = when (fillStyle) {
        ClashCapsuleFillStyle.GREEN -> Color(0xFFB8FF5C)
        ClashCapsuleFillStyle.ELIXIR -> Color(0xFFFF99F6)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(2.dp, Color(0xFF17110C), shape)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        // Dark espresso-brown recessed two-tone track
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.46f)
                    .background(Color(0xFF3B2B20))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.54f)
                    .background(Color(0xFF4F3B2D))
            )
        }

        // Glossy Clash progress fill (Green or Elixir)
        if (clampedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.46f)
                            .background(Brush.verticalGradient(topFillColors))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.54f)
                            .background(Brush.verticalGradient(bottomFillColors))
                    )
                }

                // Bright right-edge highlight bar
                if (clampedProgress < 0.99f) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight()
                            .background(edgeHighlightColor)
                            .align(Alignment.CenterEnd)
                    )
                }
            }
        }

        // Overlaid text + pocket watch icon
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClashShadowedText(
                text = titleText,
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ClashShadowedText(
                    text = rightText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = rightTextColor,
                    maxLines = 1
                )
                if (showStopwatch) {
                    ClashStopwatchIcon(size = 20.dp)
                }
            }
        }
    }
}




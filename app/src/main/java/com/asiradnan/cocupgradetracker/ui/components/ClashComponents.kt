package com.asiradnan.cocupgradetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.asiradnan.cocupgradetracker.ui.theme.ButtonBlueBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonBlueGlossBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonBlueGlossTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonBlueLip
import com.asiradnan.cocupgradetracker.ui.theme.ButtonBlueTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonDisabledBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonDisabledLip
import com.asiradnan.cocupgradetracker.ui.theme.ButtonDisabledTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonGoldBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonGoldGlossBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonGoldGlossTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonGoldLip
import com.asiradnan.cocupgradetracker.ui.theme.ButtonGoldTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonLimeBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonLimeGlossBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonLimeGlossTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonLimeLip
import com.asiradnan.cocupgradetracker.ui.theme.ButtonLimeTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonNeutralBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonNeutralGlossBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonNeutralGlossTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonNeutralLip
import com.asiradnan.cocupgradetracker.ui.theme.ButtonNeutralTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonRedBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonRedGlossBottom
import com.asiradnan.cocupgradetracker.ui.theme.ButtonRedGlossTop
import com.asiradnan.cocupgradetracker.ui.theme.ButtonRedLip
import com.asiradnan.cocupgradetracker.ui.theme.ButtonRedTop
import com.asiradnan.cocupgradetracker.ui.theme.ClashInkOutline
import com.asiradnan.cocupgradetracker.ui.theme.MenuDivider
import com.asiradnan.cocupgradetracker.ui.theme.MenuDividerHighlight
import com.asiradnan.cocupgradetracker.ui.theme.OnButtonDisabled
import com.asiradnan.cocupgradetracker.ui.theme.OnButtonNeutral
import com.asiradnan.cocupgradetracker.ui.theme.PanelHeaderStone

enum class ClashButtonStyle {
    GREEN,
    BLUE,
    GOLD,
    RED,
    NEUTRAL
}

private data class BevelPalette(
    val glossTop: Color,
    val glossBottom: Color,
    val topGradient: Color,
    val bottomGradient: Color,
    val bottomLip: Color,
    val border: Color = ClashInkOutline,
    val contentColor: Color = Color.White,
    val useDarkShadow: Boolean = true
)

private fun ClashButtonStyle.toPalette(): BevelPalette = when (this) {
    ClashButtonStyle.GREEN -> BevelPalette(
        glossTop = ButtonLimeGlossTop,
        glossBottom = ButtonLimeGlossBottom,
        topGradient = ButtonLimeTop,
        bottomGradient = ButtonLimeBottom,
        bottomLip = ButtonLimeLip
    )
    ClashButtonStyle.BLUE -> BevelPalette(
        glossTop = ButtonBlueGlossTop,
        glossBottom = ButtonBlueGlossBottom,
        topGradient = ButtonBlueTop,
        bottomGradient = ButtonBlueBottom,
        bottomLip = ButtonBlueLip
    )
    ClashButtonStyle.GOLD -> BevelPalette(
        glossTop = ButtonGoldGlossTop,
        glossBottom = ButtonGoldGlossBottom,
        topGradient = ButtonGoldTop,
        bottomGradient = ButtonGoldBottom,
        bottomLip = ButtonGoldLip
    )
    ClashButtonStyle.RED -> BevelPalette(
        glossTop = ButtonRedGlossTop,
        glossBottom = ButtonRedGlossBottom,
        topGradient = ButtonRedTop,
        bottomGradient = ButtonRedBottom,
        bottomLip = ButtonRedLip
    )
    ClashButtonStyle.NEUTRAL -> BevelPalette(
        glossTop = ButtonNeutralGlossTop,
        glossBottom = ButtonNeutralGlossBottom,
        topGradient = ButtonNeutralTop,
        bottomGradient = ButtonNeutralBottom,
        bottomLip = ButtonNeutralLip,
        contentColor = OnButtonNeutral,
        useDarkShadow = false
    )
}

/** Shared CoC-grey palette for any disabled bevel button. */
private val DisabledBevelPalette = BevelPalette(
    glossTop = ButtonDisabledTop,
    glossBottom = ButtonDisabledTop,
    topGradient = ButtonDisabledTop,
    bottomGradient = ButtonDisabledBottom,
    bottomLip = ButtonDisabledLip,
    contentColor = OnButtonDisabled,
    useDarkShadow = false
)

val ClashTextShadow = Shadow(
    color = Color(0xD9000000),
    offset = Offset(0f, 3f),
    blurRadius = 1f
)

/**
 * Clash-style text: colored fill with a thick dark outline and a hard 3D downward drop shadow
 * matching the "Music" / "Sound Effects" / "Language" labels in the CoC Settings screen.
 *
 * Drawn as two stacked layers with identical layout:
 *  1. a stroked copy (outline color + density-aware downward extrusion shadow) underneath,
 *  2. the plain filled copy on top, which hides the inner half of the stroke.
 *
 * @param outlineWidth visible outline thickness; [Dp.Unspecified] scales it with the font size.
 */
@Composable
fun ClashOutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.White,
    outlineColor: Color = ClashInkOutline,
    outlineWidth: Dp = Dp.Unspecified,
    dropShadow: Boolean = true,
    shadowOffset: Dp = 0.6.dp,
    shadowAlpha: Float = 0.28f,
    shadowBlurRadius: Dp = 0.6.dp,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    val density = LocalDensity.current
    val (strokeWidthPx, extrudedShadow) = with(density) {
        val visibleOutlinePx = if (outlineWidth.isSpecified) {
            outlineWidth.toPx()
        } else {
            val fontSize = if (style.fontSize.isSpecified) style.fontSize else 14.sp
            (fontSize.toPx() * 0.09f).coerceIn(1.0.dp.toPx(), 1.8.dp.toPx())
        }
        val shadow = Shadow(
            color = outlineColor.copy(alpha = shadowAlpha),
            offset = Offset(0f, shadowOffset.toPx()),
            blurRadius = shadowBlurRadius.toPx()
        )
        // Stroke is centered on the glyph edge and the fill hides the inner half
        (visibleOutlinePx * 2f) to shadow
    }

    // propagateMinConstraints keeps both layers the same size, so textAlign
    // and ellipsis behave identically when the caller forces a width.
    Box(modifier = modifier, propagateMinConstraints = true) {
        Text(
            text = text,
            style = style.copy(
                drawStyle = Stroke(
                    width = strokeWidthPx,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                ),
                shadow = if (dropShadow) extrudedShadow else null
            ),
            color = outlineColor,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign
        )
        Text(
            text = text,
            style = style.copy(drawStyle = Fill, shadow = null),
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign
        )
    }
}

/**
 * Two-line etched horizontal groove (`1.dp` shadow line on top + `1.dp` light highlight underneath)
 * matching the horizontal divider in the CoC Settings window.
 */
@Composable
fun ClashEtchedDivider(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MenuDivider)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MenuDividerHighlight)
        )
    }
}

private fun Modifier.clashBevelBackground(
    palette: BevelPalette,
    isPressed: Boolean,
    enabled: Boolean,
    cornerRadiusDp: Dp = 10.dp,
    showGlossPill: Boolean = true,
    shadowOffsetDp: Dp = 1.5.dp,
    shadowAlpha: Float = 0.16f
): Modifier = this.drawWithContent {
    val cornerPx = cornerRadiusDp.toPx()
    val strokePx = 2.dp.toPx()
    val halfStroke = strokePx / 2f
    val lipPx = if (isPressed || !enabled) 1.5.dp.toPx() else 3.5.dp.toPx()

    // 1. Soft outer drop shadow underneath button on the light stone panel
    if (!isPressed) {
        drawRoundRect(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(0f, shadowOffsetDp.toPx()),
            size = size,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }

    // 2. Full-button dark 3D bottom lip fill
    drawRoundRect(
        color = palette.bottomLip,
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius(cornerPx, cornerPx)
    )

    // 3. Main body gradient above the bottom bevel lip
    val bodyHeight = (size.height - strokePx - lipPx).coerceAtLeast(0f)
    val bodyCorner = (cornerPx - halfStroke).coerceAtLeast(2f)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(palette.topGradient, palette.bottomGradient),
            startY = halfStroke,
            endY = halfStroke + bodyHeight
        ),
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(size.width - strokePx, bodyHeight),
        cornerRadius = CornerRadius(bodyCorner, bodyCorner)
    )

    // 4. Inset gloss / highlight
    if (enabled && bodyHeight > 4f) {
        if (showGlossPill) {
            val glossInsetX = 3.5.dp.toPx()
            val glossInsetTop = 2.5.dp.toPx()
            val glossWidth = (size.width - strokePx - glossInsetX * 2f).coerceAtLeast(0f)
            val glossHeight = (bodyHeight * 0.46f).coerceAtLeast(0f)
            val glossCorner = (bodyCorner - 2.dp.toPx()).coerceAtLeast(2f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.glossTop, palette.glossBottom),
                    startY = halfStroke + glossInsetTop,
                    endY = halfStroke + glossInsetTop + glossHeight
                ),
                topLeft = Offset(halfStroke + glossInsetX, halfStroke + glossInsetTop),
                size = Size(glossWidth, glossHeight),
                cornerRadius = CornerRadius(glossCorner, glossCorner)
            )
        } else {
            // Sleek, clean top-edge glass shine for icon buttons (clean, high-contrast, no awkward white block)
            val highlightHeight = 3.dp.toPx().coerceAtMost(bodyHeight * 0.25f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = halfStroke,
                    endY = halfStroke + highlightHeight
                ),
                topLeft = Offset(halfStroke, halfStroke),
                size = Size(size.width - strokePx, highlightHeight),
                cornerRadius = CornerRadius(bodyCorner, bodyCorner)
            )
        }
    }

    // 5. Crisp outer black outline
    drawRoundRect(
        color = palette.border,
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(size.width - strokePx, size.height - strokePx),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        style = Stroke(width = strokePx)
    )

    drawContent()
}

@Composable
fun ClashBevelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ClashButtonStyle = ClashButtonStyle.GREEN,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
) {
    val palette = if (enabled) style.toPalette() else DisabledBevelPalette
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(10.dp)

    val topOffset = if (isPressed && enabled) 2.dp else 0.dp
    val bottomLipPadding = if (isPressed || !enabled) 1.dp else 2.5.dp

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .padding(top = topOffset)
            .clip(shape)
            .clashBevelBackground(
                palette = palette,
                isPressed = isPressed,
                enabled = enabled,
                cornerRadiusDp = 10.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(bottom = bottomLipPadding),
        contentAlignment = Alignment.Center
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
            if (palette.useDarkShadow) {
                ClashOutlinedText(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.contentColor,
                    maxLines = 1
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.contentColor,
                    maxLines = 1
                )
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
    val palette = if (enabled) style.toPalette() else DisabledBevelPalette
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(10.dp)

    val topOffset = if (isPressed && enabled) 2.dp else 0.dp
    val bottomLipPadding = if (isPressed || !enabled) 1.dp else 2.5.dp

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(buttonSize)
            .padding(top = topOffset)
            .clip(shape)
            .clashBevelBackground(
                palette = palette,
                isPressed = isPressed,
                enabled = enabled,
                cornerRadiusDp = 10.dp,
                showGlossPill = false,
                shadowOffsetDp = 1.0.dp,
                shadowAlpha = 0.12f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(bottom = bottomLipPadding),
        contentAlignment = Alignment.Center
    ) {
        // Subtle drop shadow behind the white icon for embossed 3D tactile depth
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0x4D000000),
            modifier = Modifier
                .size(20.dp)
                .padding(top = 1.dp)
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.contentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Tactile Clash bevel card designed for interactive list rows (e.g. Village summary cards).
 * Features a warm cream stone face, darker 3D bevel bottom lip, soft drop shadow, and
 * in-game press down animation on tap.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClashBevelCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    backgroundColor: Color = Color(0xFFFAF8F5),
    bottomLipColor: Color = Color(0xFFC7C3B7),
    borderColor: Color = ClashInkOutline,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topOffset = if (isPressed) 2.dp else 0.dp
    val bottomLipPadding = if (isPressed) 1.dp else 3.5.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topOffset)
            .clip(shape)
            .drawWithContent {
                val cornerPx = 12.dp.toPx()
                val strokePx = 2.dp.toPx()
                val halfStroke = strokePx / 2f
                val lipPx = if (isPressed) 1.5.dp.toPx() else 3.5.dp.toPx()

                // Soft outer drop shadow underneath
                if (!isPressed) {
                    drawRoundRect(
                        color = Color(0x22000000),
                        topLeft = Offset(0f, 2.dp.toPx()),
                        size = size,
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                }

                // Full-card dark bevel lip fill
                drawRoundRect(
                    color = bottomLipColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // Main body face
                val bodyHeight = (size.height - strokePx - lipPx).coerceAtLeast(0f)
                val bodyCorner = (cornerPx - halfStroke).coerceAtLeast(2f)
                drawRoundRect(
                    color = backgroundColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokePx, bodyHeight),
                    cornerRadius = CornerRadius(bodyCorner, bodyCorner)
                )

                // Subtle top edge gloss line
                if (bodyHeight > 4f) {
                    drawRoundRect(
                        color = Color(0x45FFFFFF),
                        topLeft = Offset(halfStroke + 2.dp.toPx(), halfStroke + 1.dp.toPx()),
                        size = Size((size.width - strokePx - 4.dp.toPx()).coerceAtLeast(0f), 2.5.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Crisp dark border
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = strokePx)
                )

                drawContent()
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(bottom = bottomLipPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun ClashPanelCard(
    modifier: Modifier = Modifier,
    headerTitle: String? = null,
    headerColor: Color = PanelHeaderStone,
    headerTrailingContent: (@Composable RowScope.() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = ClashInkOutline,
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
                    ClashOutlinedText(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        outlineWidth = 1.2.dp,
                        shadowOffset = 0.6.dp,
                        shadowAlpha = 0.28f,
                        shadowBlurRadius = 0.6.dp
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
            ClashOutlinedText(
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
            ClashOutlinedText(
                text = titleText,
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                outlineWidth = 1.0.dp,
                shadowOffset = 0.5.dp,
                shadowAlpha = 0.22f,
                shadowBlurRadius = 0.5.dp,
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
                ClashOutlinedText(
                    text = rightText,
                    style = MaterialTheme.typography.titleSmall,
                    color = rightTextColor,
                    outlineWidth = 1.0.dp,
                    shadowOffset = 0.5.dp,
                    shadowAlpha = 0.22f,
                    shadowBlurRadius = 0.5.dp,
                    maxLines = 1
                )
                if (showStopwatch) {
                    ClashStopwatchIcon(size = 20.dp)
                }
            }
        }
    }
}




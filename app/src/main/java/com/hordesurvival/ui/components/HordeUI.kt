package com.hordesurvival.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.theme.HordeColors

/**
 * Unified UI component system for Horde Survival.
 * Stylized dark-fantasy arcade aesthetic featuring rich glowing borders, angled cuts,
 * tactile push animations, and high visual personality.
 */

private val CornerCutShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp, topEnd = 2.dp, bottomStart = 2.dp)
private val SmallCutShape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp)

@Composable
fun HordeScreen(
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E1E3F),
                        Color(0xFF0F0F23),
                        Color(0xFF070711)
                    ),
                    center = Offset(0.5f, 0.4f),
                    radius = 1800f
                )
            ),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

@Composable
fun HordeHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: String? = null,
    accentColor: Color = HordeColors.SkyBlue
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = 3.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = accentColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                )
            )
        }
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = HordeColors.TextSecondary,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun HordeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HordeColors.SkyBlue,
    enabled: Boolean = true,
    breathe: Float = 1f,
    icon: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "HordeButtonScale"
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .scale(s * breathe)
            .clip(CornerCutShape)
            .background(
                if (enabled) {
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.85f),
                            color.copy(alpha = 0.45f),
                            Color(0xFF090C15)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF333344),
                            Color(0xFF1A1A26)
                        )
                    )
                }
            )
            .border(
                width = 1.5.dp,
                brush = if (enabled) {
                    Brush.linearGradient(
                        listOf(
                            color,
                            color.copy(alpha = 0.4f),
                            color.copy(alpha = 0.8f)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.DarkGray.copy(alpha = 0.2f)))
                },
                shape = CornerCutShape
            )
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onClick()
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
                letterSpacing = 2.sp,
                style = if (enabled) {
                    TextStyle(shadow = Shadow(color = color.copy(alpha = 0.6f), offset = Offset(0f, 0f), blurRadius = 14f))
                } else {
                    TextStyle.Default
                }
            )
        }
    }
}

@Composable
fun HordeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HordeColors.TextSecondary,
    icon: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "HordeSecondaryButtonScale"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .scale(s)
            .clip(SmallCutShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.22f),
                        Color(0xFF121226)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        color.copy(alpha = 0.6f),
                        color.copy(alpha = 0.2f)
                    )
                ),
                shape = SmallCutShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = 16.sp, color = color)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun HordeSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HordeColors.SkyBlue,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "HordeSmallButtonScale"
    )

    Box(
        modifier = modifier
            .height(34.dp)
            .scale(s)
            .clip(CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
            .background(
                if (enabled) color.copy(alpha = 0.85f) else Color.DarkGray.copy(alpha = 0.4f)
            )
            .border(
                width = 1.dp,
                color = if (enabled) color else Color.Gray.copy(alpha = 0.3f),
                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp)
            )
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() } else Modifier
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.Gray
        )
    }
}

@Composable
fun HordeBackButton(
    text: String = "← Back",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HordeColors.TextSecondary
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun HordeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    HordePanel(
        modifier = modifier,
        onClick = onClick,
        content = content
    )
}

@Composable
fun HordePanel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = HordeColors.CardBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CornerCutShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1D1D3A),
                        Color(0xFF101026)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        borderColor.copy(alpha = 0.7f),
                        borderColor.copy(alpha = 0.2f),
                        borderColor.copy(alpha = 0.5f)
                    )
                ),
                shape = CornerCutShape
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(18.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun HordeItemCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "HordeItemCardScale"
    )

    val bgColor = if (selected) {
        Brush.verticalGradient(listOf(HordeColors.SkyBlue.copy(alpha = 0.25f), Color(0xFF15203D)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF1B1B38), Color(0xFF121226)))
    }

    val bColor = if (selected) HordeColors.SkyBlue else HordeColors.CardBorder.copy(alpha = 0.4f)
    val shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 2.dp, bottomStart = 2.dp)

    Box(
        modifier = modifier
            .scale(s)
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = bColor,
                shape = shape
            )
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onClick()
                } else Modifier
            )
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

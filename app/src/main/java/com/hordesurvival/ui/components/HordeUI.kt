package com.hordesurvival.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import com.hordesurvival.game.audio.SoundManager
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
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
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.theme.HordeTypography
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unified UI component system for Horde Survival.
 * Stylized cheerful light-fantasy arcade aesthetic for children, featuring rich glowing borders, angled cuts,
 * bouncy elastic animations, and high visual personality.
 */

val CornerCutShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp, topEnd = 2.dp, bottomStart = 2.dp)
val SmallCutShape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp)

@Composable
fun HordeBackground(modifier: Modifier = Modifier) {
    // Smooth animations
    val inf = rememberInfiniteTransition(label = "menu")
    val f1 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "f1")
    val f2 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "f2")
    val f3 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "f3")

    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val screenW = config.screenWidthDp.toFloat()
    val screenH = config.screenHeightDp.toFloat()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF120F2D))
            .drawBehind {
                drawRect(Brush.verticalGradient(
                    listOf(Color(0xFF1B1545), Color(0xFF231B58), Color(0xFF140F33))
                ))
            }
    ) {
        // Render background layers in an alignment-agnostic container
        Box(Modifier.fillMaxSize()) {
            // ── Layer 1: Large ambient orbs ──
            val orb1X = screenW * 0.25f + sin(f1 * 6.28f) * screenW * 0.15f
            val orb1Y = screenH * 0.3f + cos(f2 * 6.28f) * screenH * 0.1f
            Box(Modifier.offset(x = orb1X.dp, y = orb1Y.dp).size(350.dp)
                .background(Brush.radialGradient(listOf(HordeColors.Lavender.copy(alpha = 0.08f), Color.Transparent))))

            val orb2X = screenW * 0.65f + cos(f2 * 6.28f) * screenW * 0.12f
            val orb2Y = screenH * 0.55f + sin(f1 * 6.28f) * screenH * 0.08f
            Box(Modifier.offset(x = orb2X.dp, y = orb2Y.dp).size(280.dp)
                .background(Brush.radialGradient(listOf(HordeColors.SkyBlue.copy(alpha = 0.07f), Color.Transparent))))

            val orb3X = screenW * 0.5f + sin(f3 * 6.28f) * screenW * 0.2f
            val orb3Y = screenH * 0.15f + cos(f3 * 6.28f) * screenH * 0.05f
            Box(Modifier.offset(x = orb3X.dp, y = orb3Y.dp).size(200.dp)
                .background(Brush.radialGradient(listOf(HordeColors.WarmPeach.copy(alpha = 0.06f), Color.Transparent))))

            // ── Layer 2: Floating particles ──
            for (i in 0 until 12) {
                val hash = (i * 7919 + 42) % 10000
                val px = (hash % 1000) / 1000f * screenW
                val py = ((hash / 1000) * 3571) % 10000 / 10000f * screenH
                val speed = 0.3f + (hash % 50) / 100f
                val phase = hash.toFloat()
                val animX = px + sin(f1 * 6.28f * speed + phase) * 30f
                val animY = py + cos(f2 * 6.28f * speed + phase) * 20f
                val alpha = 0.15f + 0.1f * sin(f3 * 6.28f + phase)
                val dotSize = 2f + (hash % 30) / 15f
                Box(Modifier.offset(x = animX.dp, y = animY.dp).size(dotSize.dp)
                    .clip(SmallCutShape).background(Color.White.copy(alpha = alpha)))
            }
        }
    }
}

@Composable
fun HordeScreen(
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered && enabled) {
            SoundManager.playHover()
        }
    }

    val targetScale = when {
        isPressed && enabled -> 0.92f
        isHovered && enabled -> 1.06f
        else -> 1f
    }

    val s by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
                    SoundManager.playClick()
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
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) {
            SoundManager.playHover()
        }
    }

    val targetScale = when {
        isPressed -> 0.93f
        isHovered -> 1.05f
        else -> 1f
    }

    val s by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
                SoundManager.playClick()
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
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered && enabled) {
            SoundManager.playHover()
        }
    }

    val targetScale = when {
        isPressed && enabled -> 0.92f
        isHovered && enabled -> 1.06f
        else -> 1f
    }

    val s by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
                ) {
                    SoundManager.playClick()
                    onClick()
                } else Modifier
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) {
            SoundManager.playHover()
        }
    }

    val targetScale = when {
        isPressed -> 0.93f
        isHovered -> 1.05f
        else -> 1f
    }

    val s by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "HordeBackButtonScale"
    )

    TextButton(
        onClick = {
            SoundManager.playClick()
            onClick()
        },
        interactionSource = interactionSource,
        modifier = modifier.scale(s)
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
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered && onClick != null) {
            SoundManager.playHover()
        }
    }

    val targetScale = when {
        isPressed && onClick != null -> 0.95f
        isHovered && onClick != null -> 1.04f
        else -> 1f
    }

    val s by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
                    SoundManager.playClick()
                    onClick()
                } else Modifier
            )
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun HordeSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = HordeColors.SkyBlue,
            checkedTrackColor = HordeColors.SkyBlue.copy(alpha = 0.3f),
            uncheckedThumbColor = HordeColors.TextSecondary,
            uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.4f),
            uncheckedBorderColor = Color.Transparent
        ),
        thumbContent = if (checked) {
            {
                Box(modifier = Modifier.fillMaxSize().clip(SmallCutShape).background(HordeColors.SkyBlue))
            }
        } else {
            {
                Box(modifier = Modifier.fillMaxSize().clip(SmallCutShape).background(HordeColors.TextSecondary))
            }
        }
    )
}

@Composable
fun HordeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = HordeColors.SkyBlue,
            activeTrackColor = HordeColors.SkyBlue,
            inactiveTrackColor = Color.DarkGray.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun HordeDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = CornerCutShape,
        containerColor = HordeColors.CardBg,
        titleContentColor = HordeColors.SkyBlue,
        textContentColor = Color.White,
        title = {
            Text(title, style = HordeTypography.SubHeader)
        },
        text = {
            Text(text, style = HordeTypography.Body)
        },
        confirmButton = {
            HordeSmallButton(
                text = confirmButtonText,
                onClick = onConfirm,
                color = HordeColors.SkyBlue
            )
        },
        dismissButton = if (dismissButtonText != null && onDismiss != null) {
            {
                HordeSmallButton(
                    text = dismissButtonText,
                    onClick = onDismiss,
                    color = HordeColors.TextSecondary
                )
            }
        } else null
    )
}

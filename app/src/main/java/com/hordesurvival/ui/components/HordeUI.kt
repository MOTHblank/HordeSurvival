package com.hordesurvival.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
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
            .background(HordeColors.SurfaceDark)
            .drawBehind {
                drawRect(Brush.verticalGradient(
                    listOf(HordeColors.DarkBg, HordeColors.DarkSurface, HordeColors.DarkBg)
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
    Box(
        modifier = modifier
            .height(54.dp)
            .hordeInteractive(enabled = enabled, breathe = breathe, onClick = onClick)
            .clip(CornerCutShape)
            .background(
                if (enabled) {
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.85f),
                            color.copy(alpha = 0.45f),
                            HordeColors.SurfaceDark
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            HordeColors.SurfaceLight,
                            HordeColors.SurfaceDark
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
    Box(
        modifier = modifier
            .height(48.dp)
            .hordeInteractive(onClick = onClick)
            .clip(SmallCutShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.22f),
                        HordeColors.DarkSurface
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
            ),
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
    Box(
        modifier = modifier
            .height(34.dp)
            .hordeInteractive(enabled = enabled, onClick = onClick)
            .clip(SmallCutShape)
            .background(
                if (enabled) color.copy(alpha = 0.85f) else Color.DarkGray.copy(alpha = 0.4f)
            )
            .border(
                width = 1.dp,
                color = if (enabled) color else Color.Gray.copy(alpha = 0.3f),
                shape = SmallCutShape
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
    Box(
        modifier = modifier
            .height(48.dp)
            .hordeInteractive(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
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
                        HordeColors.PanelBgStart,
                        HordeColors.PanelBgEnd
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
    val bgColor = if (selected) {
        Brush.verticalGradient(listOf(HordeColors.SkyBlue.copy(alpha = 0.25f), HordeColors.DarkCard))
    } else {
        Brush.verticalGradient(listOf(HordeColors.DarkBg, HordeColors.DarkSurface))
    }

    val bColor = if (selected) HordeColors.SkyBlue else HordeColors.CardBorder.copy(alpha = 0.4f)
    val shape = CornerCutShape

    Box(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.hordeInteractive(onClick = onClick)
                else Modifier
            )
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = bColor,
                shape = shape
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
    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 24.dp
    val padding = 2.dp

    val thumbMaxOffset = trackWidth - thumbSize - (padding * 2)

    val thumbOffsetTarget = if (checked) thumbMaxOffset else 0.dp
    val thumbOffset by animateFloatAsState(
        targetValue = thumbOffsetTarget.value,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "HordeSwitchOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) HordeColors.SkyBlue.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.4f),
        label = "HordeSwitchTrackColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) HordeColors.SkyBlue else HordeColors.TextSecondary,
        label = "HordeSwitchThumbColor"
    )

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(SmallCutShape)
            .background(trackColor)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        onValueChange = {
                            SoundManager.playClick()
                            onCheckedChange(it)
                        },
                        role = Role.Switch,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
                } else Modifier
            )
            .padding(padding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.dp.roundToPx(), 0) }
                .size(thumbSize)
                .clip(SmallCutShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun HordeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbSize = 24.dp
    var trackWidthPx by remember { mutableStateOf(0f) }
    val thumbSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { thumbSize.toPx() }

    val safeTrackWidth = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
    val thumbOffsetPx = value * safeTrackWidth

    val safeTrackWidthState by rememberUpdatedState(safeTrackWidth)
    val thumbOffsetPxState by rememberUpdatedState(thumbOffsetPx)
    val onValueChangeState by rememberUpdatedState(onValueChange)

    Box(
        modifier = modifier
            .height(36.dp)
            .fillMaxWidth()
            .onSizeChanged { size ->
                trackWidthPx = size.width.toFloat()
            }
            .pointerInput(Unit) {
                var localDragOffset = 0f
                detectDragGestures(
                    onDragStart = { localDragOffset = thumbOffsetPxState },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val currentSafeTrackWidth = safeTrackWidthState
                        if (currentSafeTrackWidth > 0) {
                            localDragOffset += dragAmount.x
                            val newOffset = localDragOffset.coerceIn(0f, currentSafeTrackWidth)
                            onValueChangeState(newOffset / currentSafeTrackWidth)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val currentSafeTrackWidth = safeTrackWidthState
                    if (currentSafeTrackWidth > 0) {
                        val newOffset = (offset.x - thumbSizePx / 2).coerceIn(0f, currentSafeTrackWidth)
                        onValueChangeState(newOffset / currentSafeTrackWidth)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(SmallCutShape)
                .background(Color.DarkGray.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value)
                    .clip(SmallCutShape)
                    .background(HordeColors.SkyBlue)
            )
        }

        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                .size(thumbSize)
                .clip(SmallCutShape)
                .background(HordeColors.SkyBlue)
        )
    }
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

@Composable
fun HordeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillBrush: Brush = Brush.horizontalGradient(listOf(HordeColors.SkyBlue, HordeColors.Lavender)),
    backgroundColor: Color = Color(0xFF1A1A3F),
    borderColor: Color = Color.Transparent,
    shape: androidx.compose.ui.graphics.Shape = SmallCutShape,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(shape)
                .background(fillBrush)
        )
        content()
    }
}

// ── Shared Settings Widgets ──

@Composable
fun HordeToggleSetting(
    title: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: String? = null
) {
    HordeItemCard(modifier = modifier, onClick = onToggle) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Text(icon, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                }
                Column {
                    Text(title, style = HordeTypography.Body.copy(fontWeight = FontWeight.Bold))
                    if (description != null) {
                        Text(description, style = HordeTypography.Label.copy(fontSize = 11.sp))
                    }
                }
            }
            HordeSwitch(checked = checked, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
fun HordeSliderSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueText: String? = null,
    icon: String? = null
) {
    HordeItemCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Text(icon, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                    Text(title, style = HordeTypography.Body.copy(fontWeight = FontWeight.Bold))
                }
                if (valueText != null) {
                    Text(valueText, style = HordeTypography.Label.copy(fontWeight = FontWeight.Bold, color = HordeColors.SkyBlue))
                }
            }
            Spacer(Modifier.height(4.dp))
            HordeSlider(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun <T> HordeSelectorSetting(
    title: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    columns: Int = 2,
    icon: String? = null
) {
    HordeItemCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Text(icon, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                }
                Text(title, style = HordeTypography.Body.copy(fontWeight = FontWeight.Bold))
            }
            if (description != null) {
                Text(description, style = HordeTypography.Label.copy(fontSize = 11.sp))
            }
            Spacer(Modifier.height(12.dp))

            val chunked = options.chunked(columns)
            for (row in chunked) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { (value, label) ->
                        val sel = selectedOption == value
                        HordeItemCard(
                            modifier = Modifier.weight(1f),
                            onClick = { onOptionSelected(value) },
                            selected = sel
                        ) {
                            Text(label, style = HordeTypography.Label.copy(
                                color = if (sel) HordeColors.SkyBlue else HordeColors.TextSecondary,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ))
                        }
                    }
                    if (row.size < columns) {
                        for (i in 0 until (columns - row.size)) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

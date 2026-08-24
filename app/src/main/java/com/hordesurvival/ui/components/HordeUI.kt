package com.hordesurvival.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.theme.HordeColors

@Composable
fun HordeScreen(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HordeColors.DarkBg, HordeColors.DarkSurface))),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun HordeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = HordeColors.SkyBlue,
    icon: String? = null,
    isSecondary: Boolean = false,
    fontSize: TextUnit = 16.sp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && enabled) 0.95f else 1f, spring(0.7f), label = "button_scale")

    val bgBrush = if (!enabled) {
        Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.1f)))
    } else if (isSecondary) {
        Brush.horizontalGradient(listOf(Color(0xFF1A1A3F), Color(0xFF1A1A3F))) // Dark background for secondary
    } else {
        Brush.horizontalGradient(listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.6f)))
    }

    val borderColor = if (!enabled) Color.Transparent else if (isSecondary) Color.White.copy(alpha = 0.08f) else color.copy(alpha = 0.3f)

    val textColor = if (!enabled) Color.White.copy(alpha = 0.4f) else if (isSecondary) color else Color.White

    Box(
        modifier = modifier
            .scale(scale)
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .then(
                if (enabled) Modifier.clickable(interactionSource = interactionSource, indication = androidx.compose.material.ripple.rememberRipple()) {
                    onClick()
                } else Modifier
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Text(icon, fontSize = fontSize)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                fontSize = fontSize,
                fontWeight = if (isSecondary) FontWeight.Medium else FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun HordeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1E1E3F), Color(0xFF151530))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            content()
        }
    }
}

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.delay

/**
 * Unified UI component system for Horde Survival.
 * Provides consistent styling, animations, and interactions across the game.
 */

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
    color: Color = HordeColors.SkyBlue,
    enabled: Boolean = true,
    breathe: Float = 1f,
    icon: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "HordeButtonScale"
    )


    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(56.dp)
            .scale(s * breathe)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color.copy(alpha = 0.4f)))
                } else {
                    Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.DarkGray.copy(alpha = 0.3f)))
                }
            )
            .border(
                width = 1.dp,
                color = if (enabled) color.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            )
            .then(
                if (enabled) Modifier.clickable(interactionSource = interactionSource, indication = androidx.compose.material.ripple.rememberRipple()) {
                    onClick()
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 19.sp,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                style = if (enabled) {
                    TextStyle(shadow = Shadow(color = color.copy(alpha = 0.5f), offset = Offset(0f, 0f), blurRadius = 12f))
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
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "HordeSecondaryButtonScale"
    )


    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(50.dp)
            .scale(s)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A3F))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = androidx.compose.material.ripple.rememberRipple()) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = 16.sp, color = color)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
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
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = HordeColors.DarkCard
        ),
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.Gray)
    }
}

@Composable
fun HordeBackButton(
    text: String = "← Back",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HordeColors.TextSecondary
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = color, fontSize = 16.sp)
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1E1E3F), Color(0xFF151530))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

package com.hordesurvival.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import com.hordesurvival.game.audio.SoundManager

fun Modifier.hordeInteractive(
    enabled: Boolean = true,
    breathe: Float = 1f,
    onClick: (() -> Unit)? = null
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered && enabled && onClick != null) {
            SoundManager.playHover()
        }
    }

    val targetScale = when {
        isPressed && enabled && onClick != null -> 0.93f
        isHovered && enabled && onClick != null -> 1.05f
        else -> 1f
    }

    val s by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "HordeInteractiveScale"
    )

    this
        .scale(s * breathe)
        .then(
            if (onClick != null && enabled) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        SoundManager.playClick()
                        onClick()
                    }
                )
            } else {
                Modifier
            }
        )
}

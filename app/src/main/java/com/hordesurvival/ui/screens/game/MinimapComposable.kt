package com.hordesurvival.ui.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.hordesurvival.ui.theme.HordeColors
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine

/**
 * Minimap composable — shows enemies, loot boxes, and player position.
 * Draws in a small box in the top-right corner.
 */
@Composable
fun Minimap(
    engine: GameEngine,
    modifier: Modifier = Modifier,
    mapRadius: Float = 800f
) {
    var frameTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) { withFrameMillis { frameTick = it } }
    }

    val entities = remember(frameTick) { engine.getActiveEntities() }

    Canvas(
        modifier = modifier
            .size(100.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val scale = w / (mapRadius * 2f)

        // Background
        drawRect(HordeColors.OverlayMedium, topLeft = Offset.Zero, size = Size(w, h))

        // Border
        drawRect(Color.White.copy(alpha = 0.2f), topLeft = Offset.Zero, size = Size(w, h), style = Stroke(width = 1f))

        // Find player
        val player = engine.playerEntity
        val playerPos = player?.get<TransformComponent>()
        val px = playerPos?.x ?: 0f
        val py = playerPos?.y ?: 0f

        // Draw entities relative to player
        for (e in entities) {
            if (!e.active) continue
            val t = e.get<TransformComponent>() ?: continue
            val dx = (t.x - px) * scale
            val dy = (t.y - py) * scale
            if (dx < -w / 2 || dx > w / 2 || dy < -h / 2 || dy > h / 2) continue

            val sx = cx + dx
            val sy = cy + dy

            when (e.tag) {
                "enemy" -> {
                    val isBoss = e.get<EnemyComponent>()?.isBoss == true
                    val color = if (isBoss) HordeColors.Warning else HordeColors.Danger
                    val radius = if (isBoss) 3f else 1.5f
                    drawCircle(color, radius = radius, center = Offset(sx, sy))
                }
                "loot_box" -> {
                    drawCircle(HordeColors.GoldColor, radius = 2f, center = Offset(sx, sy))
                }
                "health_gem" -> {
                    drawCircle(HordeColors.Success, radius = 1.5f, center = Offset(sx, sy))
                }
                "xp_gem" -> {
                    drawCircle(HordeColors.MintGreen.copy(alpha = 0.4f), radius = 0.8f, center = Offset(sx, sy))
                }
            }
        }

        // Draw player (always center)
        drawCircle(HordeColors.SkyBlue, radius = 3f, center = Offset(cx, cy))
        drawCircle(Color.White.copy(alpha = 0.6f), radius = 3f, center = Offset(cx, cy), style = Stroke(width = 0.5f))

        // Range circle
        drawCircle(Color.White.copy(alpha = 0.08f), radius = w / 2f, center = Offset(cx, cy), style = Stroke(width = 0.5f))
    }
}

package com.hordesurvival.ui.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.hordesurvival.R
import com.hordesurvival.game.component.*
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.systems.PlayerInputSystem
import com.hordesurvival.game.enemy.EnemyType
import com.hordesurvival.game.mode.GameModeType
import com.hordesurvival.game.mode.TowerDefenseMode
import com.hordesurvival.game.weapon.WeaponType
import kotlin.math.*

/**
 * Noto Color Emoji font family for player and enemy visuals.
 * Licensed under SIL Open Font License 1.1 and Apache License 2.0.
 */
val notoEmojiFamily = FontFamily(
    Font(R.font.noto_color_emoji_regular, FontWeight.Normal)
)

/**
 * Game renderer with configurable background styles.
 * backgroundStyle: 0=grid, 1=stars, 2=nebula, 3=checkerboard, 4=solid
 */
@Composable
fun GameRenderer(
    engine: GameEngine,
    inputSystem: PlayerInputSystem,
    backgroundStyle: Int = 0,
    graphicsQuality: Int = 1,      // 0=Low, 1=Medium, 2=High
    showParticles: Boolean = true,
    showDamageNumbers: Boolean = true,
    gameMode: GameModeType = GameModeType.SURVIVAL,
    modifier: Modifier = Modifier
) {
    var frameTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) { withFrameMillis { frameTick = it } }
    }

    // Performance: limit rendered entities based on quality
    val maxRenderedEnemies = when (graphicsQuality) {
        0 -> 80   // Low: render max 80 enemies
        1 -> 150  // Medium: render max 150 enemies
        else -> 300 // High: render all
    }
    val textMeasurer = rememberTextMeasurer()
    val emojiCache = remember { mutableMapOf<Pair<String, Int>, TextLayoutResult>() }

    val entities = remember(frameTick) { engine.getActiveEntities() }

    // Camera zoom: closer when stationary, farther when moving
    var currentZoom by remember { mutableFloatStateOf(1.1f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(inputSystem) {
                detectDragGestures(
                    onDragStart = { offset -> inputSystem.onTouchStart(offset.x, offset.y) },
                    onDrag = { change, _ ->
                        change.consume()
                        inputSystem.onTouchMove(change.position.x, change.position.y)
                    },
                    onDragEnd = { inputSystem.onTouchEnd() },
                    onDragCancel = { inputSystem.onTouchEnd() }
                )
            }
    ) {
        // Calculate zoom every frame based on player velocity
        // In TD mode, zoom is fixed
        val player = entities.find { it.tag == "player" && it.has<PlayerComponent>() }
        val vel = player?.get<VelocityComponent>()
        val playerSpeed = if (gameMode == GameModeType.TOWER_DEFENSE) 0f
            else if (vel != null) kotlin.math.sqrt(vel.vx * vel.vx + vel.vy * vel.vy) else 0f
        val targetZoom = when {
            playerSpeed < 0.1f -> 1.12f  // stationary: slight zoom in
            playerSpeed < 0.5f -> 1.02f  // walking
            else -> 0.95f              // running: gentle zoom out
        }
        currentZoom += (targetZoom - currentZoom) * 0.05f

        // Apply camera zoom
        scale(currentZoom, pivot = Offset(size.width / 2f, size.height / 2f)) {

        val player = player
        val playerPos = player?.get<TransformComponent>()
        // In TD mode, camera is FIXED — no scrolling
        val camX: Float
        val camY: Float
        val offX: Float
        val offY: Float
        if (gameMode == GameModeType.TOWER_DEFENSE) {
            camX = size.width / 2f
            camY = size.height / 2f
            offX = 0f
            offY = 0f
        } else {
            camX = playerPos?.x ?: size.width / 2
            camY = playerPos?.y ?: size.height / 2
            offX = size.width / 2 - camX + engine.shakeOffsetX
            offY = size.height / 2 - camY + engine.shakeOffsetY
        }

        // ── PARALLAX BACKGROUND — always centered on camera, covers all zoom levels ──
        val bgW = size.width * 6f
        val bgH = size.height * 6f
        // Offset background to always be centered on the visible area
        val bgOffsetX = offX - bgW / 2f + size.width / 2f
        val bgOffsetY = offY - bgH / 2f + size.height / 2f
        translate(bgOffsetX, bgOffsetY) {
            drawBackground(bgW, bgH, camX, camY, engine.gameTime, backgroundStyle)
        }

        // ── TOWER DEFENSE BOUNDARY WALLS ─────────────────────────────
        if (gameMode == GameModeType.TOWER_DEFENSE) {
            val wallW = TowerDefenseMode.WALL_THICKNESS
            // Left wall
            drawRect(
                color = Color(0xFF4A148C).copy(alpha = 0.7f),
                topLeft = Offset(offX, offY - size.height),
                size = Size(wallW, size.height * 3f)
            )
            // Left wall glow
            drawRect(
                color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                topLeft = Offset(offX + wallW, offY - size.height),
                size = Size(4f, size.height * 3f)
            )
            // Right wall
            val rightWallX = 1080f - wallW + offX
            drawRect(
                color = Color(0xFF4A148C).copy(alpha = 0.7f),
                topLeft = Offset(rightWallX, offY - size.height),
                size = Size(wallW, size.height * 3f)
            )
            // Right wall glow
            drawRect(
                color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                topLeft = Offset(rightWallX - 4f, offY - size.height),
                size = Size(4f, size.height * 3f)
            )
        }

        // ── POISON CLOUDS ─────────────────────────────────────────
        for (e in entities.filter { it.tag == "poison_cloud" && it.active }) {
            val t = e.get<TransformComponent>() ?: continue
            val c = e.get<PoisonCloudComponent>() ?: continue
            val sx = t.x + offX; val sy = t.y + offY
            if (sx < -200 || sx > size.width + 200 || sy < -200 || sy > size.height + 200) continue
            val alpha = e.get<SpriteComponent>()?.alpha ?: 0.3f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFAAE6BA).copy(alpha = alpha * 0.5f), Color(0xFF7CB68A).copy(alpha = alpha * 0.15f), Color.Transparent),
                    center = Offset(sx, sy), radius = c.radius * 1.3f
                ), radius = c.radius * 1.3f, center = Offset(sx, sy)
            )
            drawCircle(color = Color(0xFFAAE6BA).copy(alpha = alpha * 0.4f), radius = c.radius, center = Offset(sx, sy))
        }

    // ── Single-pass entity categorization (replaces 4+ filter calls) ──
    val _enemies = mutableListOf<com.hordesurvival.game.engine.ecs.Entity>()
    val _nonEnemies = mutableListOf<com.hordesurvival.game.engine.ecs.Entity>()
    var _damageNumbers = 0
    for (e in entities) {
        if (!e.active || !e.has<TransformComponent>() || !e.has<SpriteComponent>()) continue
        when (e.tag) {
            "enemy" -> _enemies.add(e)
            "damage_number" -> _damageNumbers++
            else -> _nonEnemies.add(e)
        }
    }

    // Limit enemies rendered: sort by distance to player, take closest
    val limitedEnemies = if (_enemies.size > maxRenderedEnemies && playerPos != null) {
        // Partial sort: only sort if we need to cull
        _enemies.sortBy { e ->
            val t = e.get<TransformComponent>()
            if (t != null) {
                val dx = t.x - playerPos.x; val dy = t.y - playerPos.y
                dx * dx + dy * dy
            } else Float.MAX_VALUE
        }
        _enemies.subList(0, maxRenderedEnemies)
    } else _enemies

    // Skip particles in Low quality
    val filteredNonEnemies = if (!showParticles) {
        _nonEnemies.filter { it.tag != "particle" }
    } else _nonEnemies

    // Skip damage numbers if disabled
    val finalEntities: List<com.hordesurvival.game.engine.ecs.Entity> = if (!showDamageNumbers) {
        limitedEnemies + filteredNonEnemies.filter { it.tag != "damage_number" }
    } else limitedEnemies + filteredNonEnemies

    // Insertion sort by layer (nearly-sorted input → O(n) average)
    val sorted = finalEntities.toMutableList()
    for (i in 1 until sorted.size) {
        val key = sorted[i]
        val keyLayer = key.get<SpriteComponent>()?.layer ?: 0
        var j = i - 1
        while (j >= 0 && (sorted[j].get<SpriteComponent>()?.layer ?: 0) > keyLayer) {
            sorted[j + 1] = sorted[j]
            j--
        }
        sorted[j + 1] = key
    }

        for (e in sorted) {
            val t = e.get<TransformComponent>() ?: continue
            val s = e.get<SpriteComponent>() ?: continue
            val sx = t.x + offX; val sy = t.y + offY
            if (sx < -200 || sx > size.width + 200 || sy < -200 || sy > size.height + 200) continue

            val color = Color(s.color).copy(alpha = s.alpha)
            val w = s.width * s.scaleX; val h = s.height * s.scaleY

            when (e.tag) {
                "player" -> drawPlayer(sx, sy, w, engine.gameTime, textMeasurer, emojiCache)
                "enemy" -> drawEnemy(e, sx, sy, w, h, color, engine.gameTime, textMeasurer, emojiCache)
                "projectile" -> drawProjectile(e, sx, sy, w, h, color)
                "xp_gem" -> {
                    val isMagnetized = e.get<XpGemComponent>()?.magnetized == true
                    drawXpGem(sx, sy, w, color, engine.gameTime, isMagnetized)
                    // Draw magnet line to player when magnetized
                    if (isMagnetized && playerPos != null) {
                        val px = playerPos.x + offX
                        val py = playerPos.y + offY
                        drawLine(Color(0xFF42A5F5).copy(alpha = 0.3f), Offset(sx, sy), Offset(px, py), strokeWidth = 1.5f)
                    }
                }
                "health_gem" -> drawHealthGem(sx, sy, w, engine.gameTime)
                "orbit_shield" -> drawOrbitShield(sx, sy, w, engine.gameTime)
                "particle" -> drawParticle(sx, sy, w, color, s)
                "loot_box" -> drawLootBox(e, sx, sy, w, h, engine.gameTime)
                "relic" -> drawRelic(sx, sy, w, color, engine.gameTime)
                else -> drawGeneric(sx, sy, w, h, color, s.shape)
            }
        }

        // ── DAMAGE NUMBERS ─────────────────────────────────────────
        if (showDamageNumbers) for (e in entities.filter { it.tag == "damage_number" && it.active }) {
            val t = e.get<TransformComponent>() ?: continue
            val dn = e.get<DamageNumberComponent>() ?: continue
            val sx = t.x + offX; val sy = t.y + offY
            if (sx < -100 || sx > size.width + 100 || sy < -100 || sy > size.height + 100) continue

            val progress = (dn.timer / dn.lifetime).coerceIn(0f, 1f)
            val alpha = 1f - progress
            val scale = if (dn.isCrit) 1.3f else 1f
            val fontSize = (14f * scale).sp

            val color = if (dn.isCrit) Color(0xFFFFD700) else Color.White
            val text = dn.getDisplayText()

            val textResult = textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = if (dn.isCrit) FontWeight.ExtraBold else FontWeight.Bold,
                    color = color.copy(alpha = alpha),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = alpha * 0.7f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )
            drawText(textResult, topLeft = Offset(sx - textResult.size.width / 2f, sy))
        }

        // ── JOYSTICK ──────────────────────────────────────────────
        if (inputSystem.isTouching) {
            drawJoystick(inputSystem.joyBaseX, inputSystem.joyBaseY, inputSystem.joyStickX, inputSystem.joyStickY, inputSystem.joyMagnitude)
        }

        // ── LOW HP WARNING OVERLAY ────────────────────────────────
        val hp = player?.get<HealthComponent>()
        if (hp != null) {
            val hpRatio = (hp.currentHp / hp.maxHp).coerceIn(0f, 1f)
            if (hpRatio < 0.3f) {
                val intensity = ((0.3f - hpRatio) / 0.3f).coerceIn(0f, 1f)
                val pulse = 0.5f + 0.5f * sin(engine.gameTime * 4f)
                val alpha = intensity * 0.15f * pulse
                // Red vignette edges
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xFFFF1744).copy(alpha = alpha)),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width * 0.7f
                    ),
                    topLeft = Offset.Zero, size = size
                )
            }
        }

        // ── BOSS INTRO FLASH ──────────────────────────────────────
        if (engine.bossIntroTimer > 0f) {
            val flashAlpha = (engine.bossIntroTimer / 0.3f).coerceIn(0f, 1f) * 0.4f
            drawRect(Color(0xFFFF6E40).copy(alpha = flashAlpha), topLeft = Offset.Zero, size = size)
        }

        } // end scale
    }
}

// ═══════════════════════════════════════════════════════════════════
// PLAYER — Noto Color Emoji (🧙 Mage with blue glow)
// Uses Noto Color Emoji font by Google (SIL OFL 1.1 + Apache 2.0)
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawPlayer(
    x: Float, y: Float, size: Float, time: Float,
    textMeasurer: TextMeasurer,
    emojiCache: MutableMap<Pair<String, Int>, TextLayoutResult>
) {
    val pulse = 1f + 0.06f * sin(time * 5f)
    // Outer glow ring — larger and brighter
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF6BB6FF).copy(alpha = 0.4f), Color(0xFF4A90D9).copy(alpha = 0.15f), Color.Transparent),
            center = Offset(x, y), radius = size * 2.5f * pulse
        ), radius = size * 2.5f * pulse, center = Offset(x, y)
    )

    // Draw player as Noto Color Emoji (mage character)
    val fontSizeSp = (size * 1.8f * pulse).coerceAtLeast(16f)
    val cacheKey = Pair("🧙", fontSizeSp.toInt())
    val textResult = emojiCache.getOrPut(cacheKey) {
        textMeasurer.measure(
            text = AnnotatedString("🧙"),
            style = TextStyle(
                fontFamily = notoEmojiFamily,
                fontSize = fontSizeSp.sp
            )
        )
    }
    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(x - textResult.size.width / 2f, y - textResult.size.height / 2f)
    )
}

// ═══════════════════════════════════════════════════════════════════
// ENEMY — Noto Color Emoji per type with HP bar
// Uses Noto Color Emoji font by Google (SIL OFL 1.1 + Apache 2.0)
// ═══════════════════════════════════════════════════════════════════
private fun getEnemyEmoji(type: EnemyType?): String = when (type) {
    EnemyType.BASIC_DRONE -> "🤖"
    EnemyType.FLYING_WISP -> "👻"
    EnemyType.TANK_GOLEM -> "🗿"
    EnemyType.SHOOTER_TURRET -> "🎯"
    EnemyType.SWARM_BAT -> "🦇"
    EnemyType.ELITE_KNIGHT -> "⚔️"
    EnemyType.GHOST -> "👻"
    EnemyType.BOSS -> "🐲"
    EnemyType.SPLITTER -> "🦠"
    EnemyType.HEALER -> "💖"
    EnemyType.MAGE -> "🔮"
    null -> "👾"
}

private fun DrawScope.drawEnemy(
    entity: com.hordesurvival.game.engine.ecs.Entity,
    x: Float, y: Float, w: Float, h: Float, color: Color, time: Float,
    textMeasurer: TextMeasurer,
    emojiCache: MutableMap<Pair<String, Int>, TextLayoutResult>
) {
    val enemy = entity.get<EnemyComponent>()
    val type = enemy?.type

    // Shadow
    drawOval(Color.Black.copy(alpha = 0.15f), topLeft = Offset(x - w * 0.35f, y + h * 0.3f), size = Size(w * 0.7f, h * 0.2f))

    // Boss glow
    if (enemy?.isBoss == true) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFAB91).copy(alpha = 0.35f), Color(0xFFFF6E40).copy(alpha = 0.1f), Color.Transparent),
                center = Offset(x, y), radius = w * 2.5f
            ), radius = w * 2.5f, center = Offset(x, y)
        )
    }

    // Noto Color Emoji representation per enemy type
    val emoji = getEnemyEmoji(type)
    val fontSizeSp = (w * 1.3f).coerceAtLeast(14f)
    val cacheKey = Pair(emoji, fontSizeSp.toInt())
    val textResult = emojiCache.getOrPut(cacheKey) {
        textMeasurer.measure(
            text = AnnotatedString(emoji),
            style = TextStyle(
                fontFamily = notoEmojiFamily,
                fontSize = fontSizeSp.sp
            )
        )
    }
    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(x - textResult.size.width / 2f, y - textResult.size.height / 2f)
    )

    // Status effects
    enemy?.let { e ->
        if (e.burnTimer > 0f) drawCircle(Color(0xFFFFCC80).copy(alpha = 0.35f), radius = w / 2f + 4f, center = Offset(x, y))
        if (e.slowTimer > 0f) drawCircle(Color(0xFF80CBC4).copy(alpha = 0.25f), radius = w / 2f + 3f, center = Offset(x, y))
    }

    // HP bar
    val hp = entity.get<HealthComponent>()
    if (hp != null && hp.currentHp < hp.maxHp && hp.maxHp > 15f) {
        val barW = w * 0.8f; val barH = 3f; val barY = y - h / 2f - 8f
        drawRoundRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(x - barW / 2f, barY), size = Size(barW, barH), cornerRadius = CornerRadius(2f))
        val fill = barW * (hp.currentHp / hp.maxHp).coerceIn(0f, 1f)
        val hpCol = if (hp.currentHp / hp.maxHp > 0.5f) Color(0xFFAAE6BA) else if (hp.currentHp / hp.maxHp > 0.25f) Color(0xFFFFDAC1) else Color(0xFFFFB7B2)
        drawRoundRect(hpCol, topLeft = Offset(x - barW / 2f, barY), size = Size(fill, barH), cornerRadius = CornerRadius(2f))
    }
}

// ═══════════════════════════════════════════════════════════════════
// PROJECTILE — distinct per weapon type
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawProjectile(
    entity: com.hordesurvival.game.engine.ecs.Entity,
    x: Float, y: Float, w: Float, h: Float, color: Color
) {
    val proj = entity.get<ProjectileComponent>()
    val vel = entity.get<VelocityComponent>()
    // Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(x, y), radius = w * 2f
        ), radius = w * 2f, center = Offset(x, y)
    )

    // Weapon trail effect — draw fading circles behind the projectile
    if (vel != null && proj != null) {
        val speed = kotlin.math.sqrt(vel.vx * vel.vx + vel.vy * vel.vy)
        if (speed > 10f) {
            val trailLen = when (proj.weaponType) {
                WeaponType.FIREBALL -> 5
                WeaponType.ICE_SHARD -> 3
                WeaponType.MAGIC_MISSILE -> 3
                WeaponType.DIVINE_SPEAR -> 4
                else -> 2
            }
            val trailColor = when (proj.weaponType) {
                WeaponType.FIREBALL -> Color(0xFFFF8A65)
                WeaponType.ICE_SHARD -> Color(0xFF80CBC4)
                WeaponType.MAGIC_MISSILE -> Color(0xFF6BB6FF)
                WeaponType.DIVINE_SPEAR -> Color(0xFFFFF5E1)
                else -> color
            }
            val nx = -vel.vx / speed
            val ny = -vel.vy / speed
            for (i in 1..trailLen) {
                val t = i.toFloat() / trailLen
                val tx = x + nx * i * 6f
                val ty = y + ny * i * 6f
                drawCircle(trailColor.copy(alpha = 0.15f * (1f - t)), radius = w * 0.4f * (1f - t * 0.5f), center = Offset(tx, ty))
            }
        }
    }

    when (proj?.weaponType) {
        WeaponType.MAGIC_MISSILE -> {
            // Small glowing orb
            drawCircle(color, radius = w / 2f, center = Offset(x, y))
            drawCircle(Color.White.copy(alpha = 0.6f), radius = w / 4f, center = Offset(x, y))
        }
        WeaponType.FIREBALL -> {
            // Orange-red circle with flame effect
            drawCircle(Color(0xFFFFCC80), radius = w / 2f, center = Offset(x, y))
            drawCircle(Color(0xFFFF8A65).copy(alpha = 0.6f), radius = w / 3f, center = Offset(x, y))
        }
        WeaponType.ICE_SHARD -> {
            // Triangle shard
            drawTriangle(color, x, y, w, h)
            drawTriangle(Color.White.copy(alpha = 0.4f), x, y, w * 0.4f, h * 0.4f)
        }
        WeaponType.BOOMERANG_DAGGER -> {
            // Spinning diamond
            drawDiamond(color, x, y, w, h)
            drawLine(Color.White.copy(alpha = 0.3f), Offset(x - w / 3f, y), Offset(x + w / 3f, y), strokeWidth = 1f)
        }
        WeaponType.DIVINE_SPEAR -> {
            // Long thin triangle
            drawTriangle(color, x, y, w * 0.6f, h * 1.5f)
        }
        else -> {
            drawCircle(color, radius = w / 2f, center = Offset(x, y))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// XP GEM — glowing diamond with pulse
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawXpGem(x: Float, y: Float, w: Float, color: Color, time: Float, magnetized: Boolean = false) {
    val pulse = 1f + 0.12f * sin(time * 6f + x * 0.01f)
    // Magnetized gems glow blue and pulse faster
    val gemColor = if (magnetized) Color(0xFF42A5F5) else color
    val glowAlpha = if (magnetized) 0.6f else 0.4f
    val glowRadius = if (magnetized) w * 3.5f else w * 2.5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(gemColor.copy(alpha = glowAlpha * pulse), gemColor.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(x, y), radius = glowRadius * pulse
        ), radius = glowRadius * pulse, center = Offset(x, y)
    )
    drawDiamond(gemColor, x, y, w * pulse, w * pulse)
    drawCircle(Color.White.copy(alpha = 0.5f), radius = w / 4f, center = Offset(x, y))
    // Magnetized: sparkle ring
    if (magnetized) {
        val sparkleAngle = time * 8f + x
        for (i in 0 until 3) {
            val a = sparkleAngle + i * 2.094f
            val sr = w * 1.8f
            drawCircle(Color.White.copy(alpha = 0.4f), radius = 1.5f, center = Offset(x + cos(a) * sr, y + sin(a) * sr))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HEALTH GEM — green cross with glow
// ═══════════════════════════════════════════════════════════════════
// ═══════════════════════════════════════════════════════════════════
// HEART SHAPE — reusable for health gems and loot boxes
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawHeart(color: Color, x: Float, y: Float, size: Float) {
    val s = size
    val path = Path().apply {
        // Heart shape using cubic bezier curves
        moveTo(x, y + s * 0.4f)
        // Left bump
        cubicTo(x - s * 0.8f, y - s * 0.4f, x - s * 0.8f, y - s * 1.0f, x, y - s * 0.4f)
        // Right bump
        cubicTo(x + s * 0.8f, y - s * 1.0f, x + s * 0.8f, y - s * 0.4f, x, y + s * 0.4f)
        close()
    }
    drawPath(path, color)
}

// ═══════════════════════════════════════════════════════════════════
// MAGNET SHAPE — U-shape with colored ends
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawMagnet(color: Color, x: Float, y: Float, size: Float) {
    val s = size
    val stroke = Stroke(width = s * 0.35f, cap = StrokeCap.Round)
    // U-shape arc
    val arcSize = Size(s * 1.6f, s * 1.6f)
    val topLeft = Offset(x - s * 0.8f, y - s * 0.4f)
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = stroke
    )
    // Left end (red)
    drawCircle(Color(0xFFEF5350), radius = s * 0.2f, center = Offset(x - s * 0.8f, y + s * 0.4f))
    // Right end (blue)
    drawCircle(Color(0xFF42A5F5), radius = s * 0.2f, center = Offset(x + s * 0.8f, y + s * 0.4f))
}

private fun DrawScope.drawHealthGem(x: Float, y: Float, w: Float, time: Float) {
    val pulse = 1f + 0.1f * kotlin.math.sin(time * 5f + x * 0.01f)
    val red = Color(0xFFEF5350)
    // Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(red.copy(alpha = 0.4f), red.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(x, y), radius = w * 2.5f * pulse
        ), radius = w * 2.5f * pulse, center = Offset(x, y)
    )
    // Heart shape
    drawHeart(red.copy(alpha = 0.95f), x, y, w * 0.6f * pulse)
    // Highlight
    drawCircle(Color.White.copy(alpha = 0.4f), radius = w * 0.12f, center = Offset(x - w * 0.15f, y - w * 0.2f))
}

// ═══════════════════════════════════════════════════════════════════
// ORBIT SHIELD
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawOrbitShield(x: Float, y: Float, w: Float, time: Float) {
    val glow = 0.3f + 0.15f * sin(time * 5f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFB19CD9).copy(alpha = glow), Color.Transparent),
            center = Offset(x, y), radius = w * 2f
        ), radius = w * 2f, center = Offset(x, y)
    )
    drawDiamond(Color(0xFFB19CD9), x, y, w, w)
    drawDiamond(Color(0xFFD1C4E9).copy(alpha = 0.5f), x, y, w * 0.4f, w * 0.4f)
}

// ═══════════════════════════════════════════════════════════════════
// LOOT BOX — glowing box with icon
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawLootBox(
    entity: com.hordesurvival.game.engine.ecs.Entity,
    x: Float, y: Float, w: Float, h: Float, time: Float
) {
    val loot = entity.get<LootBoxComponent>() ?: return
    val bobOffset = kotlin.math.sin(loot.bobPhase) * 3f
    val by = y + bobOffset
    val pulse = 1f + 0.08f * kotlin.math.sin(time * 4f)
    val color = Color(entity.get<SpriteComponent>()?.color ?: 0xFFFFD700.toInt())

    // Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.08f), Color.Transparent),
            center = Offset(x, by), radius = w * 2.5f * pulse
        ), radius = w * 2.5f * pulse, center = Offset(x, by)
    )
    val halfW = w * 0.5f * pulse
    val halfH = h * 0.5f * pulse

    when (loot.lootType) {
        LootType.HEALTH -> {
            // Red heart shape
            drawHeart(color.copy(alpha = 0.95f), x, by, halfW * 1.2f)
            drawHeart(Color.White.copy(alpha = 0.3f), x, by, halfW * 0.5f)
        }
        LootType.MAGNET -> {
            // Magnet shape: U-shape with colored ends
            drawMagnet(color.copy(alpha = 0.95f), x, by, halfW * 1.2f)
        }
        LootType.GOLD -> {
            // Gold coin
            drawCircle(color.copy(alpha = 0.9f), radius = halfW, center = Offset(x, by))
            drawCircle(Color(0xFFFFD700).copy(alpha = 0.5f), radius = halfW * 0.5f, center = Offset(x, by))
            drawCircle(Color.White.copy(alpha = 0.3f), radius = halfW * 0.2f, center = Offset(x - halfW * 0.2f, by - halfW * 0.2f))
        }
        LootType.DAMAGE_BOOST -> {
            // Sword / attack boost icon
            drawRoundRect(
                color = color.copy(alpha = 0.9f),
                topLeft = Offset(x - halfW, by - halfH),
                size = Size(w * pulse, h * pulse),
                cornerRadius = CornerRadius(4f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(x - halfW, by - halfH),
                size = Size(w * pulse, h * pulse),
                cornerRadius = CornerRadius(4f),
                style = Stroke(width = 1.5f)
            )
            // Arrow up
            val arrowSize = halfW * 0.5f
            val path = Path().apply {
                moveTo(x, by - arrowSize)
                lineTo(x + arrowSize * 0.6f, by + arrowSize * 0.3f)
                lineTo(x - arrowSize * 0.6f, by + arrowSize * 0.3f)
                close()
            }
            drawPath(path, Color.White.copy(alpha = 0.7f))
        }
    }
    // Sparkle
    val sparkleAlpha = 0.4f + 0.3f * kotlin.math.sin(time * 6f + x)
    drawCircle(Color.White.copy(alpha = sparkleAlpha), radius = 2f, center = Offset(x + halfW * 0.6f, by - halfH * 0.6f))
}

// ═══════════════════════════════════════════════════════════════════
// RELIC — glowing diamond with rotation
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawRelic(x: Float, y: Float, w: Float, color: Color, time: Float) {
    val pulse = 1f + 0.15f * sin(time * 5f)
    val bobY = y + sin(time * 3f) * 4f
    // Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.1f), Color.Transparent),
            center = Offset(x, bobY), radius = w * 3f * pulse
        ), radius = w * 3f * pulse, center = Offset(x, bobY)
    )
    // Diamond shape
    drawDiamond(color, x, bobY, w * pulse, w * pulse)
    drawDiamond(Color.White.copy(alpha = 0.5f), x, bobY, w * 0.4f * pulse, w * 0.4f * pulse)
    // Sparkle
    val sparkleAlpha = 0.5f + 0.5f * sin(time * 8f + x)
    drawCircle(Color.White.copy(alpha = sparkleAlpha), radius = 2f, center = Offset(x + w * 0.7f, bobY - w * 0.7f))
}

// ═══════════════════════════════════════════════════════════════════
// PARTICLE
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawParticle(x: Float, y: Float, w: Float, color: Color, sprite: SpriteComponent) {
    if (w > 40f) {
        // Large particle (like lightning ring) — draw as outline with lightning lines
        drawCircle(Color.Transparent, radius = w, center = Offset(x, y)) // clear
        drawCircle(color.copy(alpha = sprite.alpha * 0.6f), radius = w, center = Offset(x, y), style = Stroke(width = 3f))
        // Lightning lines on the circle
        val segments = 8
        for (i in 0 until segments) {
            val angle1 = (i.toFloat() / segments) * Math.PI.toFloat() * 2f
            val angle2 = ((i + 1).toFloat() / segments) * Math.PI.toFloat() * 2f
            val midAngle = (angle1 + angle2) / 2f
            val r1 = w * 0.9f
            val r2 = w * 1.1f
            val px1 = x + kotlin.math.cos(angle1) * r1
            val py1 = y + kotlin.math.sin(angle1) * r1
            val px2 = x + kotlin.math.cos(midAngle) * r2
            val py2 = y + kotlin.math.sin(midAngle) * r2
            val px3 = x + kotlin.math.cos(angle2) * r1
            val py3 = y + kotlin.math.sin(angle2) * r1
            drawLine(color.copy(alpha = sprite.alpha * 0.8f), Offset(px1, py1), Offset(px2, py2), strokeWidth = 2f)
            drawLine(color.copy(alpha = sprite.alpha * 0.8f), Offset(px2, py2), Offset(px3, py3), strokeWidth = 2f)
        }
    } else {
        // Small particle — normal filled
        drawCircle(color.copy(alpha = sprite.alpha * 0.3f), radius = w, center = Offset(x, y))
        drawCircle(color, radius = w / 2f, center = Offset(x, y))
    }
}

// ═══════════════════════════════════════════════════════════════════
// JOYSTICK
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawJoystick(baseX: Float, baseY: Float, stickX: Float, stickY: Float, mag: Float) {
    val baseR = PlayerInputSystem.VISUAL_BASE_RADIUS
    val stickR = PlayerInputSystem.VISUAL_STICK_RADIUS
    val sx = stickX * baseR; val sy = stickY * baseR

    drawCircle(Color.White.copy(alpha = 0.08f), radius = baseR, center = Offset(baseX, baseY))
    drawCircle(Color.White.copy(alpha = 0.15f), radius = baseR, center = Offset(baseX, baseY), style = Stroke(2f))
    if (mag > 0.05f) drawLine(Color.White.copy(alpha = 0.1f), Offset(baseX, baseY), Offset(baseX + sx, baseY + sy), strokeWidth = 3f)
    val a = 0.25f + mag * 0.3f
    drawCircle(Color.White.copy(alpha = a), radius = stickR * (1f + mag * 0.2f), center = Offset(baseX + sx, baseY + sy))
    drawCircle(Color.White.copy(alpha = a + 0.1f), radius = stickR * 0.5f, center = Offset(baseX + sx, baseY + sy))
}

// ═══════════════════════════════════════════════════════════════════
// BACKGROUND — multiple styles, smooth movement
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawBackground(w: Float, h: Float, camX: Float, camY: Float, time: Float, style: Int) {
    drawRect(Color(0xFF080814), topLeft = Offset.Zero, size = Size(w, h))

    when (style) {
        0 -> drawGridBg(w, h, camX, camY)
        1 -> drawStarsBg(w, h, camX, camY, time)
        2 -> drawNebulaBg(w, h, camX, camY, time)
        3 -> drawCheckerBg(w, h, camX, camY)
        4 -> { /* solid dark — just the base rect */ }
        5 -> drawPersianBg(w, h, camX, camY, time)
        6 -> drawRomanBg(w, h, camX, camY, time)
        7 -> drawEgyptianBg(w, h, camX, camY, time)
    }
}

/** 0: Smooth grid lines — no intersection dots for performance */
private fun DrawScope.drawGridBg(w: Float, h: Float, camX: Float, camY: Float) {
    val g = 100f
    val p = 0.3f
    val ox = (camX * p) % g
    val oy = (camY * p) % g
    val col = Color(0xFF141430).copy(alpha = 0.35f)
    var x = -ox
    while (x < w) { drawLine(col, Offset(x, 0f), Offset(x, h), strokeWidth = 0.8f); x += g }
    var y = -oy
    while (y < h) { drawLine(col, Offset(0f, y), Offset(w, y), strokeWidth = 0.8f); y += g }
}

/** 1: Stars only — calm, no geometry */
private fun DrawScope.drawStarsBg(w: Float, h: Float, camX: Float, camY: Float, time: Float) {
    for (i in 0 until 80) {
        val hash = (i * 7919 + 42) % 10000
        val sx = ((hash % 1000) / 1000f * w + camX * 0.02f) % w
        val sy = (((hash / 1000) * 3571) % 10000 / 10000f * h + camY * 0.02f) % h
        val tw = 0.6f + 0.4f * sin(time * (1.2f + (hash % 5) / 3f) + hash.toFloat())
        drawCircle(Color.White.copy(alpha = 0.25f * tw), radius = 0.8f + (hash % 30) / 100f, center = Offset(sx, sy))
    }
    for (i in 0 until 20) {
        val hash = (i * 3571 + 99) % 10000
        val sx = ((hash % 1000) / 1000f * w + camX * 0.04f) % w
        val sy = (((hash / 1000) * 7919) % 10000 / 10000f * h + camY * 0.04f) % h
        val tw = 0.5f + 0.5f * sin(time * 1.5f + hash.toFloat())
        drawCircle(Color.White.copy(alpha = 0.4f * tw), radius = 1.2f, center = Offset(sx, sy))
        drawCircle(Color.White.copy(alpha = 0.06f * tw), radius = 4f, center = Offset(sx, sy))
    }
}

/** 2: Nebula — colorful clouds */
private fun DrawScope.drawNebulaBg(w: Float, h: Float, camX: Float, camY: Float, time: Float) {
    drawStarsBg(w, h, camX, camY, time)  // stars underneath
    val nx = w * 0.35f + sin(time * 0.02f) * 50f + camX * 0.01f
    val ny = h * 0.3f + cos(time * 0.015f) * 35f + camY * 0.01f
    drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF4A1A6B).copy(alpha = 0.06f), Color.Transparent), center = Offset(nx, ny), radius = 300f), radius = 300f, center = Offset(nx, ny))
    val nx2 = w * 0.7f + cos(time * 0.025f) * 70f + camX * 0.012f
    val ny2 = h * 0.65f + sin(time * 0.03f) * 45f + camY * 0.012f
    drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF0A3A5B).copy(alpha = 0.05f), Color.Transparent), center = Offset(nx2, ny2), radius = 250f), radius = 250f, center = Offset(nx2, ny2))
    val nx3 = w * 0.55f + sin(time * 0.018f) * 60f + camX * 0.008f
    val ny3 = h * 0.15f + cos(time * 0.022f) * 40f + camY * 0.008f
    drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF2A4A1A).copy(alpha = 0.04f), Color.Transparent), center = Offset(nx3, ny3), radius = 200f), radius = 200f, center = Offset(nx3, ny3))
}

/** 3: Checkerboard — seamless tiling with proper modular offset */
private fun DrawScope.drawCheckerBg(w: Float, h: Float, camX: Float, camY: Float) {
    val tileSize = 80f
    val p = 0.3f
    val cx = camX * p
    val cy = camY * p
    // Use floor-based offset for seamless tiling (no visual tearing)
    val offX = ((cx % tileSize) + tileSize) % tileSize
    val offY = ((cy % tileSize) + tileSize) % tileSize
    val startCol = kotlin.math.floor((cx / tileSize).toDouble()).toInt()
    val startRow = kotlin.math.floor((cy / tileSize).toDouble()).toInt()
    val dark = Color(0xFF0A0A1C)
    val light = Color(0xFF111130)
    val tilesX = (w / tileSize).toInt() + 3
    val tilesY = (h / tileSize).toInt() + 3
    for (iy in -1 until tilesY) {
        for (ix in -1 until tilesX) {
            val isLight = ((startCol + ix) xor (startRow + iy)) and 1 == 0
            drawRect(if (isLight) light else dark, topLeft = Offset(ix * tileSize - offX, iy * tileSize - offY), size = Size(tileSize, tileSize))
        }
    }
    // Subtle grid lines on checkerboard
    val lineCol = Color(0xFF1A1A40).copy(alpha = 0.2f)
    for (iy in -1..tilesY) {
        val y = iy * tileSize - offY
        drawLine(lineCol, Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
    }
    for (ix in -1..tilesX) {
        val x = ix * tileSize - offX
        drawLine(lineCol, Offset(x, 0f), Offset(x, h), strokeWidth = 0.5f)
    }
}

/** 5: Persian/Iranian — geometric arabesque tile patterns */
private fun DrawScope.drawPersianBg(w: Float, h: Float, camX: Float, camY: Float, time: Float) {
    val tileSize = 120f
    val p = 0.25f
    val cx = camX * p
    val cy = camY * p
    val offX = ((cx % tileSize) + tileSize) % tileSize
    val offY = ((cy % tileSize) + tileSize) % tileSize
    val startCol = kotlin.math.floor((cx / tileSize).toDouble()).toInt()
    val startRow = kotlin.math.floor((cy / tileSize).toDouble()).toInt()
    val tilesX = (w / tileSize).toInt() + 3
    val tilesY = (h / tileSize).toInt() + 3
    // Base tile color
    for (iy in -1 until tilesY) {
        for (ix in -1 until tilesX) {
            val tx = ix * tileSize - offX
            val ty = iy * tileSize - offY
            val isAlt = ((startCol + ix) xor (startRow + iy)) and 1 == 0
            drawRect(if (isAlt) Color(0xFF0E0A1A) else Color(0xFF120E22), topLeft = Offset(tx, ty), size = Size(tileSize, tileSize))
        }
    }
    // Geometric star patterns (8-pointed stars)
    val accent = Color(0xFFC8A24E).copy(alpha = 0.08f)
    val accent2 = Color(0xFF8B1A1A).copy(alpha = 0.06f)
    for (iy in -1 until tilesY) {
        for (ix in -1 until tilesX) {
            val tx = ix * tileSize - offX + tileSize / 2f
            val ty = iy * tileSize - offY + tileSize / 2f
            // 8-pointed star
            val r = tileSize * 0.35f
            val innerR = tileSize * 0.15f
            val path = Path()
            for (i in 0 until 16) {
                val a = Math.toRadians((i * 22.5) - 90.0).toFloat()
                val rad = if (i % 2 == 0) r else innerR
                val px = tx + kotlin.math.cos(a) * rad
                val py = ty + kotlin.math.sin(a) * rad
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, accent, style = Fill)
            drawPath(path, accent2, style = Stroke(width = 0.8f))
            // Central diamond
            val dr = tileSize * 0.12f
            val diamond = Path().apply {
                moveTo(tx, ty - dr); lineTo(tx + dr, ty); lineTo(tx, ty + dr); lineTo(tx - dr, ty); close()
            }
            drawPath(diamond, Color(0xFFC8A24E).copy(alpha = 0.12f), style = Fill)
        }
    }
    // Subtle border lines
    val lineCol = Color(0xFFC8A24E).copy(alpha = 0.04f)
    for (iy in -1..tilesY) {
        drawLine(lineCol, Offset(0f, iy * tileSize - offY), Offset(w, iy * tileSize - offY), strokeWidth = 0.5f)
    }
    for (ix in -1..tilesX) {
        drawLine(lineCol, Offset(ix * tileSize - offX, 0f), Offset(ix * tileSize - offX, h), strokeWidth = 0.5f)
    }
}

/** 6: Roman — columns, arches, laurel motifs */
private fun DrawScope.drawRomanBg(w: Float, h: Float, camX: Float, camY: Float, time: Float) {
    // Marble-like base with subtle veins
    drawRect(Color(0xFF0D0D1A), topLeft = Offset.Zero, size = Size(w, h))
    val p = 0.2f
    val cx = camX * p
    val cy = camY * p
    // Marble vein lines
    val veinCol = Color(0xFF1A1A30).copy(alpha = 0.3f)
    for (i in 0 until 12) {
        val hash = (i * 7919 + 13) % 10000
        val x1 = ((hash % 1000) / 1000f * w * 1.5f - cx * 0.1f) % (w * 1.2f)
        val y1 = (((hash / 1000) * 3571) % 10000 / 10000f * h * 1.5f - cy * 0.1f) % (h * 1.2f)
        val x2 = x1 + (hash % 200) - 100f
        val y2 = y1 + ((hash / 3) % 200) - 100f
        drawLine(veinCol, Offset(x1, y1), Offset(x2, y2), strokeWidth = 0.8f)
    }
    // Column patterns at fixed intervals
    val colSpacing = 250f
    val colOffX = ((cx * 0.5f) % colSpacing + colSpacing) % colSpacing
    val colOffY = ((cy * 0.3f) % colSpacing + colSpacing) % colSpacing
    val colCol = Color(0xFF2A2040).copy(alpha = 0.15f)
    var x = -colOffX
    while (x < w + colSpacing) {
        // Column shaft
        drawRect(colCol, topLeft = Offset(x - 8f, 0f), size = Size(16f, h))
        // Column capital (top)
        drawRect(colCol.copy(alpha = 0.2f), topLeft = Offset(x - 16f, 0f), size = Size(32f, 20f))
        // Column base
        drawRect(colCol.copy(alpha = 0.2f), topLeft = Offset(x - 14f, h - 18f), size = Size(28f, 18f))
        x += colSpacing
    }
    // Horizontal entablature lines
    val entCol = Color(0xFFC8A24E).copy(alpha = 0.04f)
    var y = -colOffY
    while (y < h + colSpacing) {
        drawLine(entCol, Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
        drawLine(entCol.copy(alpha = 0.02f), Offset(0f, y + 6f), Offset(w, y + 6f), strokeWidth = 1f)
        y += colSpacing
    }
    // Laurel wreath motifs (small V-shapes)
    val laurelCol = Color(0xFF4A7A4A).copy(alpha = 0.06f)
    for (i in 0 until 30) {
        val hash = (i * 3571 + 77) % 10000
        val lx = ((hash % 1000) / 1000f * w + cx * 0.05f) % w
        val ly = (((hash / 1000) * 7919) % 10000 / 10000f * h + cy * 0.05f) % h
        val s = 6f + (hash % 8)
        val lp = Path().apply {
            moveTo(lx, ly - s); lineTo(lx + s * 0.6f, ly + s * 0.3f); lineTo(lx, ly + s * 0.1f); lineTo(lx - s * 0.6f, ly + s * 0.3f); close()
        }
        drawPath(lp, laurelCol, style = Fill)
    }
}

/** 7: Egyptian — hieroglyph-style symbols, gold accents, pyramids */
private fun DrawScope.drawEgyptianBg(w: Float, h: Float, camX: Float, camY: Float, time: Float) {
    // Sandy dark base
    drawRect(Color(0xFF0E0A06), topLeft = Offset.Zero, size = Size(w, h))
    val p = 0.2f
    val cx = camX * p
    val cy = camY * p
    // Sand gradient overlay
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1408).copy(alpha = 0.3f), Color(0xFF0E0A06).copy(alpha = 0.1f)),
            startY = 0f, endY = h
        ),
        topLeft = Offset.Zero, size = Size(w, h)
    )
    // Pyramid silhouettes at bottom
    val pyramidCol = Color(0xFF2A1E0E).copy(alpha = 0.2f)
    val baseY = h * 0.85f
    for (i in 0 until 3) {
        val hash = (i * 2311 + 55) % 10000
        val px = ((hash % 1000) / 1000f * w * 1.2f - cx * 0.08f) % (w * 1.3f) - w * 0.15f
        val pSize = 120f + (hash % 100)
        val pp = Path().apply {
            moveTo(px, baseY)
            lineTo(px + pSize / 2f, baseY - pSize * 0.7f)
            lineTo(px + pSize, baseY)
            close()
        }
        drawPath(pp, pyramidCol, style = Fill)
        drawPath(pp, Color(0xFFC8A24E).copy(alpha = 0.04f), style = Stroke(width = 0.8f))
    }
    // Hieroglyph-style symbols
    val hierCol = Color(0xFFC8A24E).copy(alpha = 0.07f)
    for (i in 0 until 40) {
        val hash = (i * 4519 + 33) % 10000
        val hx = ((hash % 1000) / 1000f * w + cx * 0.04f) % w
        val hy = (((hash / 1000) * 6271) % 10000 / 10000f * h + cy * 0.04f) % h
        val symbolType = hash % 6
        val s = 8f + (hash % 6)
        when (symbolType) {
            0 -> { // Eye of Horus (simplified)
                drawCircle(hierCol, radius = s, center = Offset(hx, hy), style = Stroke(width = 0.8f))
                drawCircle(hierCol, radius = s * 0.3f, center = Offset(hx, hy))
                drawLine(hierCol, Offset(hx + s, hy), Offset(hx + s * 1.5f, hy + s * 0.5f), strokeWidth = 0.6f)
            }
            1 -> { // Ankh (cross with loop)
                drawLine(hierCol, Offset(hx, hy - s), Offset(hx, hy + s), strokeWidth = 0.8f)
                drawLine(hierCol, Offset(hx - s * 0.6f, hy - s * 0.2f), Offset(hx + s * 0.6f, hy - s * 0.2f), strokeWidth = 0.8f)
                drawCircle(hierCol, radius = s * 0.4f, center = Offset(hx, hy - s * 0.8f), style = Stroke(width = 0.8f))
            }
            2 -> { // Bird (simplified ibis)
                val bp = Path().apply {
                    moveTo(hx, hy - s); lineTo(hx + s * 0.5f, hy); lineTo(hx + s * 0.3f, hy + s * 0.3f)
                    lineTo(hx, hy + s * 0.1f); lineTo(hx - s * 0.3f, hy + s * 0.3f); lineTo(hx - s * 0.5f, hy); close()
                }
                drawPath(bp, hierCol, style = Fill)
            }
            3 -> { // Sun disk
                drawCircle(hierCol, radius = s * 0.6f, center = Offset(hx, hy), style = Stroke(width = 0.8f))
                for (ray in 0 until 8) {
                    val a = Math.toRadians((ray * 45.0)).toFloat()
                    drawLine(hierCol, Offset(hx + kotlin.math.cos(a) * s * 0.7f, hy + kotlin.math.sin(a) * s * 0.7f),
                        Offset(hx + kotlin.math.cos(a) * s * 1.1f, hy + kotlin.math.sin(a) * s * 1.1f), strokeWidth = 0.6f)
                }
            }
            4 -> { // Scarab (simplified)
                drawOval(hierCol, topLeft = Offset(hx - s * 0.4f, hy - s * 0.6f), size = Size(s * 0.8f, s * 1.2f), style = Stroke(width = 0.7f))
                drawLine(hierCol, Offset(hx - s * 0.5f, hy - s * 0.2f), Offset(hx - s, hy - s * 0.5f), strokeWidth = 0.5f)
                drawLine(hierCol, Offset(hx + s * 0.5f, hy - s * 0.2f), Offset(hx + s, hy - s * 0.5f), strokeWidth = 0.5f)
            }
            5 -> { // Wave / water
                val wp = Path().apply {
                    moveTo(hx - s, hy)
                    cubicTo(hx - s * 0.5f, hy - s * 0.4f, hx, hy + s * 0.4f, hx + s, hy)
                }
                drawPath(wp, hierCol, style = Stroke(width = 0.7f))
            }
        }
    }
    // Gold accent lines
    val goldLine = Color(0xFFC8A24E).copy(alpha = 0.03f)
    val bandSpacing = 200f
    val bandOffY = ((cy * 0.15f) % bandSpacing + bandSpacing) % bandSpacing
    var by = -bandOffY
    while (by < h + bandSpacing) {
        drawLine(goldLine, Offset(0f, by), Offset(w, by), strokeWidth = 1.5f)
        by += bandSpacing
    }
}

// ═══════════════════════════════════════════════════════════════════
// SHAPE HELPERS
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawTriangle(color: Color, cx: Float, cy: Float, w: Float, h: Float) {
    drawPath(Path().apply { moveTo(cx, cy - h / 2f); lineTo(cx - w / 2f, cy + h / 2f); lineTo(cx + w / 2f, cy + h / 2f); close() }, color, style = Fill)
}

private fun DrawScope.drawDiamond(color: Color, cx: Float, cy: Float, w: Float, h: Float) {
    drawPath(Path().apply { moveTo(cx, cy - h / 2f); lineTo(cx + w / 2f, cy); lineTo(cx, cy + h / 2f); lineTo(cx - w / 2f, cy); close() }, color, style = Fill)
}

private fun DrawScope.drawStar(color: Color, cx: Float, cy: Float, w: Float, h: Float) {
    val p = Path()
    for (i in 0 until 10) {
        val a = Math.toRadians((i * 36.0) - 90.0).toFloat()
        val r = if (i % 2 == 0) w / 2f else w / 4f
        if (i == 0) p.moveTo(cx + cos(a) * r, cy + sin(a) * r) else p.lineTo(cx + cos(a) * r, cy + sin(a) * r)
    }
    p.close(); drawPath(p, color, style = Fill)
}

private fun DrawScope.drawPolygon(color: Color, cx: Float, cy: Float, radius: Float, sides: Int) {
    val p = Path()
    for (i in 0 until sides) {
        val a = Math.toRadians((i * 360.0 / sides) - 90.0).toFloat()
        val px = cx + cos(a) * radius; val py = cy + sin(a) * radius
        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
    }
    p.close(); drawPath(p, color, style = Fill)
}

private fun DrawScope.drawGeneric(x: Float, y: Float, w: Float, h: Float, color: Color, shape: SpriteShape) {
    when (shape) {
        SpriteShape.CIRCLE -> drawCircle(color, radius = w / 2f, center = Offset(x, y))
        SpriteShape.RECT -> drawRect(color, topLeft = Offset(x - w / 2f, y - h / 2f), size = Size(w, h))
        SpriteShape.TRIANGLE -> drawTriangle(color, x, y, w, h)
        SpriteShape.DIAMOND -> drawDiamond(color, x, y, w, h)
        SpriteShape.STAR -> drawStar(color, x, y, w, h)
    }
}

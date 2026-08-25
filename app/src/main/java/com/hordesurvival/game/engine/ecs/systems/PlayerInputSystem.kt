package com.hordesurvival.game.engine.ecs.systems

import com.hordesurvival.game.component.PlayerComponent
import com.hordesurvival.game.component.VelocityComponent
import com.hordesurvival.game.engine.ecs.Entity
import com.hordesurvival.game.engine.GameEngine
import com.hordesurvival.game.engine.ecs.System
import kotlin.math.sqrt

/**
 * Virtual joystick input system with fixed-base joystick.
 * Base appears where finger first touches, stick follows drag.
 * Includes dead zone and normalized output.
 */
class PlayerInputSystem(private val engine: GameEngine) : System() {

    // ── Touch State (updated from GameRenderer) ────────────────────
    var touchStartX = 0f
    var touchStartY = 0f
    var touchCurrentX = 0f
    var touchCurrentY = 0f
    var isTouching = false

    // ── Joystick Output ────────────────────────────────────────────
    var joyBaseX = 0f       // base center X (where finger first touched)
    var joyBaseY = 0f       // base center Y
    var joyStickX = 0f      // stick offset from base (-1..1)
    var joyStickY = 0f
    var joyMagnitude = 0f   // 0..1 how far stick is pushed

    /** Fixed-base virtual joystick mode */
    var joystickActive = true

    // ── Constants ──────────────────────────────────────────────────
    companion object {
        const val JOYSTICK_RADIUS = 80f   // max stick travel
        const val DEAD_ZONE = 0.12f       // ignore small movements
        const val VISUAL_BASE_RADIUS = 60f
        const val VISUAL_STICK_RADIUS = 28f
    }

    override fun update(dt: Float, entities: List<Entity>) {
        val player = engine.playerEntity ?: return
        val velocity = player.get<VelocityComponent>() ?: return

        if (!isTouching) {
            velocity.vx = 0f
            velocity.vy = 0f
            joyMagnitude = 0f
            joyStickX = 0f
            joyStickY = 0f
            return
        }

        if (joystickActive) {
            // Fixed-base joystick: base at touch start, stick follows finger
            val dx = touchCurrentX - joyBaseX
            val dy = touchCurrentY - joyBaseY
            val dist = sqrt(dx * dx + dy * dy)

            // Normalize to joystick radius
            val clampedDist = dist.coerceAtMost(JOYSTICK_RADIUS)
            joyMagnitude = (clampedDist / JOYSTICK_RADIUS).coerceIn(0f, 1f)

            if (dist > 0f) {
                joyStickX = (dx / dist) * joyMagnitude
                joyStickY = (dy / dist) * joyMagnitude
            } else {
                joyStickX = 0f
                joyStickY = 0f
            }

            // Apply dead zone
            if (joyMagnitude < DEAD_ZONE) {
                velocity.vx = 0f
                velocity.vy = 0f
            } else {
                // Normalize magnitude after dead zone
                val adjustedMag = (joyMagnitude - DEAD_ZONE) / (1f - DEAD_ZONE)
                velocity.vx = joyStickX / joyMagnitude * adjustedMag
                velocity.vy = joyStickY / joyMagnitude * adjustedMag
            }
        } else {
            // Touch-follow mode
            val dx = touchCurrentX - (player.get<com.hordesurvival.game.component.TransformComponent>()?.x ?: 0f)
            val dy = touchCurrentY - (player.get<com.hordesurvival.game.component.TransformComponent>()?.y ?: 0f)
            val dist = sqrt(dx * dx + dy * dy)

            if (dist > 10f) {
                velocity.vx = dx / dist
                velocity.vy = dy / dist
                joyMagnitude = 1f
            } else {
                velocity.vx = 0f
                velocity.vy = 0f
                joyMagnitude = 0f
            }
        }
    }

    /** Called when touch starts */
    fun onTouchStart(x: Float, y: Float) {
        isTouching = true
        touchStartX = x
        touchStartY = y
        touchCurrentX = x
        touchCurrentY = y
        joyBaseX = x
        joyBaseY = y
    }

    /** Called when finger moves */
    fun onTouchMove(x: Float, y: Float) {
        touchCurrentX = x
        touchCurrentY = y
    }

    /** Called when touch ends */
    fun onTouchEnd() {
        isTouching = false
        joyMagnitude = 0f
        joyStickX = 0f
        joyStickY = 0f
    }
}

package com.hordesurvival.utils

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Math utility functions for the game.
 * Wraps LibGDX math and adds custom helpers.
 */
object GameMath {

    /** Distance between two points */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    /** Angle from (x1,y1) to (x2,y2) in radians */
    fun angleTo(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return atan2(y2 - y1, x2 - x1)
    }

    /** Normalize angle to [-PI, PI] */
    fun normalizeAngle(angle: Float): Float {
        var a = angle
        while (a > MathUtils.PI) a -= MathUtils.PI2
        while (a < -MathUtils.PI) a += MathUtils.PI2
        return a
    }

    /** Lerp between two values */
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t.coerceIn(0f, 1f)
    }

    /** Random float in range */
    fun randomRange(min: Float, max: Float): Float {
        return MathUtils.random(min, max)
    }

    /**
     * Random point on circle edge.
     * Takes an optional target [out] Vector2 to populate and return, avoiding new allocations.
     * Crucial for hot paths (e.g. particle spawns, projectile dispersion, death effects) to prevent GC churn.
     */
    fun randomPointOnCircle(radius: Float, out: Vector2 = Vector2()): Vector2 {
        val angle = MathUtils.random(MathUtils.PI2)
        return out.set(
            MathUtils.cos(angle) * radius,
            MathUtils.sin(angle) * radius
        )
    }

    /**
     * Random point inside circle.
     * Takes an optional target [out] Vector2 to populate and return, avoiding new allocations.
     * Crucial for hot paths (e.g. particle spawns, XP gem spreads, collision feedback) to prevent GC churn.
     */
    fun randomPointInCircle(radius: Float, out: Vector2 = Vector2()): Vector2 {
        val angle = MathUtils.random(MathUtils.PI2)
        val r = radius * sqrt(MathUtils.random())  // sqrt for uniform distribution
        return out.set(MathUtils.cos(angle) * r, MathUtils.sin(angle) * r)
    }

    /** Quadratic ease out */
    fun easeOutQuad(t: Float): Float {
        val t2 = t.coerceIn(0f, 1f)
        return t2 * (2f - t2)
    }

    /** Check if point is within rectangle */
    fun pointInRect(px: Float, py: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        return px in rx..(rx + rw) && py in ry..(ry + rh)
    }

    /** Smoothstep interpolation */
    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}

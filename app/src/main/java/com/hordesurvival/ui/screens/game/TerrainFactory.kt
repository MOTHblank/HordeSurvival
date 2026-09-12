package com.hordesurvival.ui.screens.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

object TerrainFactory {

    const val TILE = 32
    const val GRID = 4
    const val SHEET_SIZE = TILE * GRID   // 128

    const val STYLE_GRASS = 0
    const val STYLE_LAVA = 3
    const val STYLE_ICE = 4
    const val STYLE_DARK = 8
    const val STYLE_GRAVEYARD = 9
    const val STYLE_VOID = 10

    // One shared paint — generation is one-time; allocations are irrelevant here.
    private val paint = Paint().apply { isAntiAlias = true }

    fun createTerrainSheet(style: Int): ImageBitmap {
        val sheet = ImageBitmap(SHEET_SIZE, SHEET_SIZE)
        val canvas = Canvas(sheet)
        // Fixed seed per style -> identical sheet every run, on every device
        val rnd = Random(style.toLong() * 7919L + 104729L)
        for (row in 0 until GRID) {
            for (col in 0 until GRID) {
                val index = row * GRID + col
                val x = (col * TILE).toFloat()
                val y = (row * TILE).toFloat()
                when (style) {
                    STYLE_LAVA -> drawLavaTile(canvas, x, y, index, rnd)
                    STYLE_ICE -> drawIceTile(canvas, x, y, index, rnd)
                    STYLE_DARK -> drawDarkTile(canvas, x, y, index, rnd)
                    STYLE_GRAVEYARD -> drawGraveyardTile(canvas, x, y, index, rnd)
                    STYLE_VOID -> drawVoidTile(canvas, x, y, index, rnd)
                    else -> drawGrassTile(canvas, x, y, index, rnd)
                }
            }
        }
        return sheet
    }

    // ── drawing primitives ────────────────────────────────────────────

    private fun fill(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Color) {
        paint.style = PaintingStyle.Fill; paint.color = color
        c.drawRect(Rect(x, y, x + w, y + h), paint)
    }

    private fun dot(c: Canvas, cx: Float, cy: Float, r: Float, color: Color) {
        paint.style = PaintingStyle.Fill; paint.color = color
        c.drawCircle(Offset(cx, cy), r, paint)
    }

    private fun oval(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Color) {
        paint.style = PaintingStyle.Fill; paint.color = color
        c.drawOval(Rect(x, y, x + w, y + h), paint)
    }

    private fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, color: Color, widthPx: Float) {
        paint.style = PaintingStyle.Stroke; paint.strokeWidth = widthPx
        paint.strokeCap = StrokeCap.Round; paint.color = color
        c.drawLine(Offset(x1, y1), Offset(x2, y2), paint)
    }

    private fun rr(rnd: Random, min: Float, max: Float) = min + (max - min) * rnd.nextFloat()

    /** Jagged 2-segment crack from (x,y) in a random direction. */
    private fun crack(c: Canvas, x: Float, y: Float, len: Float, color: Color, w: Float, rnd: Random) {
        val ang = rr(rnd, 0f, (Math.PI * 2).toFloat())
        val ex = x + cos(ang) * len
        val ey = y + sin(ang) * len
        val mx = x + cos(ang) * len * 0.5f + rr(rnd, -3f, 3f)
        val my = y + sin(ang) * len * 0.5f + rr(rnd, -3f, 3f)
        line(c, x, y, mx, my, color, w)
        line(c, mx, my, ex, ey, color, w)
    }

    // ── GRASS — Green Fields ──────────────────────────────────────────
    // Bright mid-greens: the greenlands ambient tint (dark green, 0.30a)
    // darkens these into a pleasant forest floor instead of mud.

    private val grassBases = listOf(Color(0xFF3B8252), Color(0xFF35794B), Color(0xFF418B5A), Color(0xFF317046))
    private val grassMottle = Color(0xFF2C6B40)
    private val tuftDark = Color(0xFF57A26E)
    private val tuftLight = Color(0xFF7BC98D)
    private val stemGreen = Color(0xFF2E5C3C)

    private fun drawGrassTile(c: Canvas, x: Float, y: Float, index: Int, rnd: Random) {
        fill(c, x, y, TILE.toFloat(), TILE.toFloat(), grassBases[index % grassBases.size])
        // every tile gets faint mottling for organic variation
        repeat(2) { oval(c, x + rr(rnd, 2f, 22f), y + rr(rnd, 2f, 22f), rr(rnd, 6f, 12f), rr(rnd, 4f, 8f), grassMottle.copy(alpha = 0.35f)) }
        when (index) {
            0 -> {} // dominant plain tile
            1 -> { tuft(c, x + 10f, y + 20f); tuft(c, x + 22f, y + 18f) }
            2 -> { tuft(c, x + 8f, y + 22f); tuft(c, x + 16f, y + 18f); tuft(c, x + 24f, y + 24f); line(c, x + 16f, y + 20f, x + 17f, y + 13f, tuftLight, 1.5f) }
            3 -> { flower(c, x + 10f, y + 18f, Color(0xFFFFB7B2)); flower(c, x + 22f, y + 14f, Color(0xFFFFDAC1)); tuft(c, x + 16f, y + 24f) }
            4 -> { pebble(c, x + 8f, y + 18f, 3f); pebble(c, x + 20f, y + 22f, 2.5f) }
            5 -> { // small boulder
                oval(c, x + 10f, y + 23f, 12f, 3f, Color(0xFF244D33))   // shadow first
                oval(c, x + 9f, y + 15f, 14f, 9f, Color(0xFF5D6D7E))
                oval(c, x + 11f, y + 16f, 9f, 4f, Color(0xFF8395A7))
            }
            6 -> { // dirt patch
                oval(c, x + 6f, y + 10f, 20f, 12f, Color(0xFF5C4A33))
                repeat(3) { dot(c, x + rr(rnd, 8f, 24f), y + rr(rnd, 12f, 20f), 1f, Color(0xFF7D6144)) }
                tuft(c, x + 24f, y + 8f)
            }
            7 -> { tuft(c, x + 10f, y + 20f); flower(c, x + 21f, y + 16f, Color(0xFFFFF5E1)) }
            8 -> { tuft(c, x + rr(rnd, 8f, 24f), y + rr(rnd, 16f, 24f)) }
            9 -> { pebble(c, x + 12f, y + 20f, 2.5f); pebble(c, x + 20f, y + 15f, 2f); tuft(c, x + 8f, y + 22f) }
            10 -> { flower(c, x + 8f, y + 20f, Color(0xFFFFB7B2)); flower(c, x + 15f, y + 16f, Color(0xFFFFDAC1)); flower(c, x + 23f, y + 21f, Color(0xFFAAE6BA)) }
            11 -> { line(c, x + 4f, y + 18f, x + 28f, y + 17f, grassMottle, 3f); tuft(c, x + 14f, y + 12f) }
            12 -> { tuft(c, x + 9f, y + 18f); tuft(c, x + 22f, y + 22f); pebble(c, x + 16f, y + 13f, 2f) }
            13 -> { flower(c, x + 18f, y + 20f, Color(0xFFFFDAC1)) }
            14 -> { // little mushroom — for the kids this game is for
                line(c, x + 16f, y + 22f, x + 16f, y + 16f, Color(0xFFFFF5E1), 2f)
                dot(c, x + 16f, y + 15f, 3.5f, Color(0xFFFFB7B2))
                dot(c, x + 14.5f, y + 14f, 0.8f, Color.White)
            }
            15 -> { dot(c, x + 14f, y + 18f, 1.5f, Color(0xFFAAE6BA)); dot(c, x + 17f, y + 20f, 1.5f, Color(0xFFAAE6BA)); dot(c, x + 15f, y + 22f, 1.5f, Color(0xFFAAE6BA)) }
        }
    }

    private fun tuft(c: Canvas, x: Float, y: Float) {
        line(c, x, y, x - 2.5f, y - 5f, tuftDark, 1.5f)
        line(c, x, y, x + 0.5f, y - 6.5f, tuftDark, 1.5f)
        line(c, x, y, x + 3f, y - 4.5f, tuftLight, 1.5f)
    }

    private fun flower(c: Canvas, x: Float, y: Float, petal: Color) {
        line(c, x, y + 4f, x, y, stemGreen, 1f)
        dot(c, x, y - 1f, 1.6f, petal)
        dot(c, x, y - 1f, 0.6f, Color(0xFFFFF5E1))
    }

    private fun pebble(c: Canvas, x: Float, y: Float, r: Float) {
        oval(c, x - r, y - r * 0.7f, r * 2f, r * 1.4f, Color(0xFF6B7B8C))
        dot(c, x - r * 0.3f, y - r * 0.5f, r * 0.35f, Color(0xFF93A5B5))
    }

    // ── LAVA — Inferno Pits ───────────────────────────────────────────
    // Scorched rock with glowing veins — pairs with rising ember particles.

    private val lavaBases = listOf(Color(0xFF331713), Color(0xFF2C1310), Color(0xFF3A1B15), Color(0xFF27100D))
    private val lavaCrack = Color(0xFF1A0A08)
    private val lavaGlowCore = Color(0xFFFFB74D)
    private val lavaGlowHot = Color(0xFFFF8A65)
    private val lavaGlowSoft = Color(0xFFE64A19)
    private val emberCol = Color(0xFFFFCC80)
    private val crustCol = Color(0xFF4E342E)

    private fun lavaVein(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        line(c, x1, y1, x2, y2, lavaGlowSoft.copy(alpha = 0.45f), 4f)  // wide soft glow
        line(c, x1, y1, x2, y2, lavaGlowHot, 2f)
        line(c, x1, y1, x2, y2, lavaGlowCore, 1f)                       // bright core
    }

    private fun drawLavaTile(c: Canvas, x: Float, y: Float, index: Int, rnd: Random) {
        fill(c, x, y, TILE.toFloat(), TILE.toFloat(), lavaBases[index % lavaBases.size])
        repeat(2) { oval(c, x + rr(rnd, 2f, 20f), y + rr(rnd, 2f, 20f), rr(rnd, 7f, 12f), rr(rnd, 5f, 9f), lavaCrack.copy(alpha = 0.4f)) }
        when (index) {
            0 -> {}
            1 -> { crack(c, x + 16f, y + 10f, 8f, lavaCrack, 1.5f, rnd); crack(c, x + 10f, y + 22f, 7f, lavaCrack, 1.5f, rnd) }
            2 -> { crack(c, x + 8f, y + 16f, 12f, lavaCrack, 1.5f, rnd) }
            3 -> { lavaVein(c, x + 4f, y + 20f, x + 26f, y + 14f) }
            4 -> { repeat(3) { dot(c, x + rr(rnd, 4f, 28f), y + rr(rnd, 4f, 28f), rr(rnd, 0.8f, 1.4f), emberCol) } }
            5 -> { // small lava pool
                oval(c, x + 8f, y + 12f, 16f, 10f, lavaGlowSoft.copy(alpha = 0.5f))
                oval(c, x + 10f, y + 13.5f, 12f, 7f, lavaGlowHot)
                oval(c, x + 13f, y + 15f, 6f, 3.5f, lavaGlowCore)
            }
            6 -> { crack(c, x + 14f, y + 16f, 9f, lavaCrack, 1.5f, rnd); dot(c, x + 24f, y + 8f, 1.2f, emberCol) }
            7 -> { // cooled crust
                oval(c, x + 6f, y + 8f, 20f, 14f, crustCol.copy(alpha = 0.8f))
                crack(c, x + 16f, y + 15f, 6f, lavaCrack, 1f, rnd)
                dot(c, x + 10f, y + 11f, 1f, Color(0xFF6D4C41))
            }
            8 -> { dot(c, x + 12f, y + 12f, 1.1f, emberCol); oval(c, x + 18f, y + 20f, 8f, 5f, lavaCrack.copy(alpha = 0.35f)) }
            9 -> { lavaVein(c, x + 6f, y + 24f, x + 18f, y + 10f) }
            10 -> { oval(c, x + 20f, y + 6f, 8f, 5f, lavaGlowHot.copy(alpha = 0.7f)); dot(c, x + 8f, y + 24f, 1.2f, emberCol) }
            11 -> { crack(c, x + 20f, y + 8f, 8f, lavaCrack, 1.5f, rnd); crack(c, x + 8f, y + 20f, 8f, lavaCrack, 1.5f, rnd) }
            12 -> { dot(c, x + 16f, y + 16f, 1.3f, emberCol); dot(c, x + 22f, y + 22f, 1f, emberCol) }
            13 -> { oval(c, x + 8f, y + 16f, 14f, 9f, crustCol.copy(alpha = 0.7f)); crack(c, x + 14f, y + 20f, 7f, lavaCrack, 1f, rnd) }
            14 -> { lavaVein(c, x + 4f, y + 8f, x + 28f, y + 24f) }
            15 -> { crack(c, x + 10f, y + 10f, 10f, lavaCrack, 1.5f, rnd); oval(c, x + 16f, y + 20f, 9f, 6f, lavaCrack.copy(alpha = 0.3f)) }
        }
    }

    // ── ICE — Frozen Tundra ───────────────────────────────────────────

    private val iceBases = listOf(Color(0xFF1B4F7A), Color(0xFF16436B), Color(0xFF215784), Color(0xFF123A5C))
    private val iceCrack = Color(0xFFA8D8EA)
    private val snowCol = Color(0xFFE8F4FF)

    private fun sparkle(c: Canvas, x: Float, y: Float) {
        dot(c, x, y, 1.2f, Color.White)
        dot(c, x - 2f, y, 0.5f, Color.White.copy(alpha = 0.6f))
        dot(c, x + 2f, y, 0.5f, Color.White.copy(alpha = 0.6f))
    }

    private fun snowPatch(c: Canvas, x: Float, y: Float, w: Float, h: Float) {
        oval(c, x, y, w, h, snowCol.copy(alpha = 0.55f))
        oval(c, x + 1.5f, y + 1f, w - 3f, h - 2.5f, snowCol.copy(alpha = 0.75f))
        dot(c, x + w * 0.3f, y + h * 0.35f, 0.8f, Color.White)
    }

    private fun drawIceTile(c: Canvas, x: Float, y: Float, index: Int, rnd: Random) {
        fill(c, x, y, TILE.toFloat(), TILE.toFloat(), iceBases[index % iceBases.size])
        // wind streaks on every tile
        line(c, x + 3f, y + rr(rnd, 8f, 12f), x + rr(rnd, 18f, 26f), y + rr(rnd, 8f, 12f), Color(0xFF81D4FA).copy(alpha = 0.18f), 1f)
        line(c, x + rr(rnd, 6f, 14f), y + rr(rnd, 20f, 26f), x + 28f, y + rr(rnd, 20f, 26f), Color(0xFF81D4FA).copy(alpha = 0.12f), 1f)
        when (index) {
            0 -> {}
            1 -> crack(c, x + 16f, y + 16f, 9f, iceCrack.copy(alpha = 0.7f), 1f, rnd)
            2 -> { crack(c, x + 10f, y + 10f, 8f, iceCrack.copy(alpha = 0.6f), 1f, rnd); crack(c, x + 22f, y + 22f, 7f, iceCrack.copy(alpha = 0.6f), 1f, rnd) }
            3 -> { crack(c, x + 14f, y + 18f, 8f, iceCrack.copy(alpha = 0.7f), 1f, rnd); sparkle(c, x + 22f, y + 10f) }
            4 -> snowPatch(c, x + 9f, y + 12f, 14f, 8f)
            5 -> { snowPatch(c, x + 6f, y + 10f, 20f, 11f); sparkle(c, x + 26f, y + 24f) }
            6 -> { repeat(4) { sparkle(c, x + rr(rnd, 5f, 27f), y + rr(rnd, 5f, 27f)) } }
            7 -> { // frost corner
                line(c, x + 1f, y + 10f, x + 1f, y + 30f, snowCol.copy(alpha = 0.15f), 3f)
                line(c, x + 1f, y + 30f, x + 14f, y + 30f, snowCol.copy(alpha = 0.15f), 3f)
            }
            8 -> { sparkle(c, x + 10f, y + 12f); line(c, x + 14f, y + 22f, x + 26f, y + 21f, Color(0xFF81D4FA).copy(alpha = 0.3f), 1.5f) }
            9 -> crack(c, x + 8f, y + 16f, 12f, iceCrack.copy(alpha = 0.7f), 1.2f, rnd)
            10 -> { snowPatch(c, x + 18f, y + 8f, 10f, 6f); crack(c, x + 10f, y + 20f, 6f, iceCrack.copy(alpha = 0.5f), 1f, rnd) }
            11 -> { sparkle(c, x + 8f, y + 8f); sparkle(c, x + 24f, y + 16f) }
            12 -> crack(c, x + 6f, y + 24f, 10f, iceCrack.copy(alpha = 0.6f), 1f, rnd)
            13 -> { line(c, x + 2f, y + 2f, x + 12f, y + 2f, snowCol.copy(alpha = 0.15f), 3f); sparkle(c, x + 20f, y + 20f) }
            14 -> { line(c, x + 6f, y + 12f, x + 24f, y + 10f, iceCrack.copy(alpha = 0.35f), 1f); line(c, x + 8f, y + 20f, x + 26f, y + 18f, iceCrack.copy(alpha = 0.35f), 1f) }
            15 -> { sparkle(c, x + 16f, y + 14f); oval(c, x + 10f, y + 22f, 10f, 5f, Color(0xFF81D4FA).copy(alpha = 0.15f)) }
        }
    }

    // ── DARK — Dark Realm ─────────────────────────────────────────────
    // Deliberately dim & sparse: the map's gimmick is low visibility.

    private val darkBases = listOf(Color(0xFF15151F), Color(0xFF191926), Color(0xFF111118), Color(0xFF1D1D2A))

    private fun drawDarkTile(c: Canvas, x: Float, y: Float, index: Int, rnd: Random) {
        fill(c, x, y, TILE.toFloat(), TILE.toFloat(), darkBases[index % darkBases.size])
        when (index) {
            0 -> {}
            1 -> repeat(3) { dot(c, x + rr(rnd, 4f, 28f), y + rr(rnd, 4f, 28f), rr(rnd, 0.7f, 1.2f), Color(0xFF2E2E3A)) }
            2 -> dot(c, x + 16f, y + 16f, 2f, Color(0xFF555560).copy(alpha = 0.5f))
            3 -> { fill(c, x + 8f, y + 14f, 7f, 5f, Color(0xFF232332)); fill(c, x + 17f, y + 19f, 5f, 4f, Color(0xFF20202C)) }
            4 -> crack(c, x + 14f, y + 14f, 9f, Color(0xFF0C0C12), 1.2f, rnd)
            5 -> { dot(c, x + 10f, y + 10f, 1f, Color(0xFF2E2E3A)); fill(c, x + 18f, y + 18f, 6f, 4f, Color(0xFF232332)) }
            6 -> { dot(c, x + 20f, y + 8f, 1.6f, Color(0xFF555560).copy(alpha = 0.35f)); dot(c, x + 8f, y + 22f, 0.9f, Color(0xFF2E2E3A)) }
            7 -> crack(c, x + 10f, y + 20f, 10f, Color(0xFF0C0C12), 1f, rnd)
            8 -> repeat(2) { dot(c, x + rr(rnd, 4f, 28f), y + rr(rnd, 4f, 28f), 1f, Color(0xFF2E2E3A)) }
            9 -> fill(c, x + 12f, y + 12f, 8f, 6f, Color(0xFF232332))
            10 -> { crack(c, x + 18f, y + 10f, 7f, Color(0xFF0C0C12), 1f, rnd); dot(c, x + 8f, y + 24f, 1f, Color(0xFF2E2E3A)) }
            11 -> dot(c, x + 14f, y + 18f, 1.2f, Color(0xFF3A3A48))
            12 -> fill(c, x + 6f, y + 20f, 6f, 4f, Color(0xFF20202C))
            13 -> dot(c, x + 22f, y + 20f, 1.4f, Color(0xFF555560).copy(alpha = 0.4f))
            14 -> crack(c, x + 16f, y + 22f, 8f, Color(0xFF0C0C12), 1f, rnd)
            15 -> repeat(2) { dot(c, x + rr(rnd, 6f, 26f), y + rr(rnd, 6f, 26f), 0.8f, Color(0xFF2E2E3A)) }
        }
    }

    // ── GRAVEYARD — Cursed Graveyard ──────────────────────────────────

    private val graveBases = listOf(Color(0xFF191527), Color(0xFF1D1830), Color(0xFF151222), Color(0xFF211B33))

    private fun gravestone(c: Canvas, x: Float, y: Float) {
        oval(c, x + 8f, y + 25f, 16f, 3f, Color(0xFF0E0B18))            // ground shadow
        oval(c, x + 10f, y + 10f, 12f, 6f, Color(0xFF546E7A))           // rounded top
        fill(c, x + 10f, y + 13f, 12f, 13f, Color(0xFF546E7A))          // slab
        line(c, x + 16f, y + 16f, x + 16f, y + 22f, Color(0xFF37474F), 1.5f)  // cross
        line(c, x + 14f, y + 18f, x + 18f, y + 18f, Color(0xFF37474F), 1.5f)
    }

    private fun deadGrass(c: Canvas, x: Float, y: Float) {
        line(c, x, y, x - 2f, y - 5f, Color(0xFF3E3352), 1f)
        line(c, x, y, x + 1.5f, y - 6f, Color(0xFF3E3352), 1f)
        line(c, x, y, x + 3f, y - 4f, Color(0xFF463B5E), 1f)
    }

    private fun drawGraveyardTile(c: Canvas, x: Float, y: Float, index: Int, rnd: Random) {
        fill(c, x, y, TILE.toFloat(), TILE.toFloat(), graveBases[index % graveBases.size])
        when (index) {
            0 -> { oval(c, x + 6f, y + 8f, 18f, 8f, Color(0xFF9FAECB).copy(alpha = 0.06f)) }  // fog hint
            1 -> deadGrass(c, x + 10f, y + 20f)
            2 -> crack(c, x + 12f, y + 14f, 9f, Color(0xFF0E0B18), 1.2f, rnd)
            3 -> gravestone(c, x, y)
            4 -> { deadGrass(c, x + 8f, y + 22f); deadGrass(c, x + 20f, y + 18f) }
            5 -> { // bone
                line(c, x + 12f, y + 16f, x + 20f, y + 18f, Color(0xFFCFCFC4), 1.5f)
                dot(c, x + 11f, y + 16f, 1.2f, Color(0xFFCFCFC4))
                dot(c, x + 21f, y + 18f, 1.2f, Color(0xFFCFCFC4))
            }
            6 -> oval(c, x + 4f, y + 16f, 22f, 10f, Color(0xFF9FAECB).copy(alpha = 0.08f))
            7 -> { crack(c, x + 18f, y + 10f, 8f, Color(0xFF0E0B18), 1f, rnd); deadGrass(c, x + 10f, y + 22f) }
            8 -> deadGrass(c, x + 18f, y + 24f)
            9 -> dot(c, x + 14f, y + 12f, 1f, Color(0xFF3E3352))
            10 -> oval(c, x + 8f, y + 18f, 16f, 8f, Color(0xFF9FAECB).copy(alpha = 0.07f))
            11 -> gravestone(c, x, y)
            12 -> { deadGrass(c, x + 14f, y + 18f); crack(c, x + 8f, y + 24f, 7f, Color(0xFF0E0B18), 1f, rnd) }
            13 -> dot(c, x + 20f, y + 14f, 0.9f, Color(0xFFCFCFC4).copy(alpha = 0.7f))
            14 -> oval(c, x + 10f, y + 6f, 14f, 7f, Color(0xFF9FAECB).copy(alpha = 0.06f))
            15 -> deadGrass(c, x + 12f, y + 16f)
        }
    }

    // ── VOID — The Void ───────────────────────────────────────────────

    private val voidBases = listOf(Color(0xFF0B0512), Color(0xFF0E0716), Color(0xFF090410), Color(0xFF110919))

    private fun voidRift(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        line(c, x1, y1, x2, y2, Color(0xFF6A00B0).copy(alpha = 0.4f), 4f)
        line(c, x1, y1, x2, y2, Color(0xFF9900FF).copy(alpha = 0.8f), 1.5f)
        line(c, x1, y1, x2, y2, Color(0xFFE1AFFF), 0.7f)
    }

    private fun drawVoidTile(c: Canvas, x: Float, y: Float, index: Int, rnd: Random) {
        fill(c, x, y, TILE.toFloat(), TILE.toFloat(), voidBases[index % voidBases.size])
        when (index) {
            0 -> {}
            1 -> repeat(3) { dot(c, x + rr(rnd, 4f, 28f), y + rr(rnd, 4f, 28f), rr(rnd, 0.5f, 1f), Color(0xFFB388FF).copy(alpha = 0.5f)) }
            2 -> { dot(c, x + 12f, y + 10f, 1f, Color(0xFFB388FF).copy(alpha = 0.7f)); dot(c, x + 22f, y + 20f, 0.8f, Color(0xFFB388FF).copy(alpha = 0.4f)) }
            3 -> voidRift(c, x + 4f, y + 18f, x + 26f, y + 12f)
            4 -> repeat(4) { dot(c, x + rr(rnd, 3f, 29f), y + rr(rnd, 3f, 29f), 0.6f, Color(0xFFB388FF).copy(alpha = 0.35f)) }
            5 -> { // glow orb
                dot(c, x + 16f, y + 16f, 3.5f, Color(0xFF6A00B0).copy(alpha = 0.35f))
                dot(c, x + 16f, y + 16f, 2f, Color(0xFF9900FF).copy(alpha = 0.5f))
                dot(c, x + 16f, y + 16f, 0.9f, Color(0xFFE1AFFF))
            }
            6 -> { dot(c, x + 8f, y + 22f, 0.9f, Color(0xFFB388FF).copy(alpha = 0.5f)); dot(c, x + 24f, y + 8f, 0.7f, Color(0xFFB388FF).copy(alpha = 0.3f)) }
            7 -> line(c, x + 6f, y + 16f, x + 24f, y + 15f, Color(0xFF6A00B0).copy(alpha = 0.2f), 1f)
            8 -> repeat(2) { dot(c, x + rr(rnd, 4f, 28f), y + rr(rnd, 4f, 28f), 0.8f, Color(0xFFB388FF).copy(alpha = 0.45f)) }
            9 -> dot(c, x + 18f, y + 12f, 1.1f, Color(0xFFB388FF).copy(alpha = 0.6f))
            10 -> { dot(c, x + 10f, y + 8f, 0.7f, Color(0xFFB388FF).copy(alpha = 0.4f)); dot(c, x + 20f, y + 24f, 0.7f, Color(0xFFB388FF).copy(alpha = 0.4f)) }
            11 -> line(c, x + 8f, y + 22f, x + 20f, y + 20f, Color(0xFF6A00B0).copy(alpha = 0.15f), 1f)
            12 -> repeat(3) { dot(c, x + rr(rnd, 3f, 29f), y + rr(rnd, 3f, 29f), 0.5f, Color(0xFFB388FF).copy(alpha = 0.3f)) }
            13 -> { dot(c, x + 14f, y + 20f, 1f, Color(0xFFB388FF).copy(alpha = 0.5f)); dot(c, x + 15.5f, y + 18.5f, 0.4f, Color(0xFFE1AFFF)) }
            14 -> voidRift(c, x + 8f, y + 6f, x + 22f, y + 26f)
            15 -> dot(c, x + 24f, y + 14f, 0.9f, Color(0xFFB388FF).copy(alpha = 0.5f))
        }
    }
}
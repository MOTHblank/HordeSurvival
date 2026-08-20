package com.hordesurvival

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue

/**
 * Splash screen with smooth entrance animations.
 * Shows for 2.5 seconds then launches MainActivity.
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#080814"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Decorative glow circle behind icon
        val glow = android.view.View(this).apply {
            layoutParams = FrameLayout.LayoutParams(400, 400).apply {
                gravity = Gravity.CENTER
                topMargin = -40
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.parseColor("#106BB6FF"))
            }
        }

        // App icon
        val icon = ImageView(this).apply {
            setImageResource(R.drawable.ic_launcher)
            layoutParams = FrameLayout.LayoutParams(180, 180).apply {
                gravity = Gravity.CENTER
                topMargin = -20
            }
        }

        // Title "HORDE"
        val titleHorde = TextView(this).apply {
            text = "HORDE"
            setTextColor(Color.parseColor("#6BB6FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.2f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 160
            }
        }

        // Title "SURVIVAL"
        val titleSurvival = TextView(this).apply {
            text = "SURVIVAL"
            setTextColor(Color.parseColor("#445577"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT
            letterSpacing = 0.3f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 200
            }
        }

        // Version
        val version = TextView(this).apply {
            text = "v1.2.9"
            setTextColor(Color.parseColor("#333350"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 340
            }
        }

        // Loading dots
        val loadingDots = TextView(this).apply {
            text = "● ● ●"
            setTextColor(Color.parseColor("#6BB6FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            alpha = 0f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 400
            }
        }

        root.addView(glow)
        root.addView(icon)
        root.addView(titleHorde)
        root.addView(titleSurvival)
        root.addView(version)
        root.addView(loadingDots)
        setContentView(root)

        // Initial states — all invisible and offset
        icon.alpha = 0f; icon.scaleX = 0.3f; icon.scaleY = 0.3f; icon.translationY = 40f
        glow.alpha = 0f; glow.scaleX = 0.5f; glow.scaleY = 0.5f
        titleHorde.alpha = 0f; titleHorde.translationY = 30f
        titleSurvival.alpha = 0f; titleSurvival.translationY = 20f
        version.alpha = 0f
        loadingDots.alpha = 0f

        // ── Animation Sequence ──

        //0-800ms: Icon pops in with overshoot
        val iconAlpha = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f).setDuration(500)
        val iconScaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0.3f, 1f).setDuration(700)
        val iconScaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.3f, 1f).setDuration(700)
        val iconTransY = ObjectAnimator.ofFloat(icon, "translationY", 40f, 0f).setDuration(600)
        iconScaleX.interpolator = OvershootInterpolator(1.8f)
        iconScaleY.interpolator = OvershootInterpolator(1.8f)
        iconTransY.interpolator = DecelerateInterpolator(2f)

        AnimatorSet().apply {
            playTogether(iconAlpha, iconScaleX, iconScaleY, iconTransY)
            start()
        }

        //300ms: Glow fades in
        Handler(Looper.getMainLooper()).postDelayed({
            val glowAlpha = ObjectAnimator.ofFloat(glow, "alpha", 0f, 1f).setDuration(800)
            val glowScaleX = ObjectAnimator.ofFloat(glow, "scaleX", 0.5f, 1f).setDuration(1000)
            val glowScaleY = ObjectAnimator.ofFloat(glow, "scaleY", 0.5f, 1f).setDuration(1000)
            glowScaleX.interpolator = DecelerateInterpolator(2f)
            glowScaleY.interpolator = DecelerateInterpolator(2f)
            AnimatorSet().apply {
                playTogether(glowAlpha, glowScaleX, glowScaleY)
                start()
            }
        }, 300)

        //600ms: "HORDE" slides up
        Handler(Looper.getMainLooper()).postDelayed({
            val titleAlpha = ObjectAnimator.ofFloat(titleHorde, "alpha", 0f, 1f).setDuration(400)
            val titleTrans = ObjectAnimator.ofFloat(titleHorde, "translationY", 30f, 0f).setDuration(500)
            titleTrans.interpolator = DecelerateInterpolator(2f)
            AnimatorSet().apply {
                playTogether(titleAlpha, titleTrans)
                start()
            }
        }, 600)

        //900ms: "SURVIVAL" slides up
        Handler(Looper.getMainLooper()).postDelayed({
            val subAlpha = ObjectAnimator.ofFloat(titleSurvival, "alpha", 0f, 1f).setDuration(400)
            val subTrans = ObjectAnimator.ofFloat(titleSurvival, "translationY", 20f, 0f).setDuration(500)
            subTrans.interpolator = DecelerateInterpolator(2f)
            AnimatorSet().apply {
                playTogether(subAlpha, subTrans)
                start()
            }
        }, 900)

        //1200ms: Version + loading dots
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(version, "alpha", 0f, 1f).setDuration(400).start()
            ObjectAnimator.ofFloat(loadingDots, "alpha", 0f, 0.6f).setDuration(400).start()
        }, 1200)

        //1500-2200ms: Pulsing dots animation
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(loadingDots, "alpha", 0.6f, 0.2f).apply {
                duration = 400
                repeatCount = 1
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
        }, 1500)

        //2500ms: Launch MainActivity with fade transition
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2500)
    }
}

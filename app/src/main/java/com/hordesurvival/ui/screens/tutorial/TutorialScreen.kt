package com.hordesurvival.ui.screens.tutorial

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.CornerCutShape
import com.hordesurvival.ui.components.SmallCutShape
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.theme.HordeColors

/**
 * Interactive tutorial with step-by-step game mechanics explanation.
 */
@Composable
fun TutorialScreen(
    languageCode: String = "en",
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val L = { k: String -> Locales.getString(k, languageCode) }
    var step by remember { mutableIntStateOf(0) }

    val steps = listOf(
        TutorialStep("🎮", "Joystick Control", "Drag on the left side of the screen to move your character. The joystick appears where you touch."),
        TutorialStep("⚔️", "Auto-Attack", "Weapons fire automatically! Just focus on positioning and dodging enemies."),
        TutorialStep("💎", "Collect XP", "Kill enemies to drop XP gems. Walk near them to collect. Fill the XP bar to level up."),
        TutorialStep("⬆️", "Level Up", "Each level lets you choose an upgrade: new weapons, stronger attacks, or passive bonuses."),
        TutorialStep("❤️", "Health", "Your HP bar is at the top. Collect green health gems or choose healing upgrades to restore HP."),
        TutorialStep("👹", "Boss Fights", "Every 50 levels, a powerful boss appears. Defeat it for massive rewards!"),
        TutorialStep("📦", "Loot Boxes", "Watch for glowing boxes! They contain health, gold, magnets, or damage boosts."),
        TutorialStep("🔗", "Weapon Synergy", "Combine specific weapons to unlock powerful bonuses! Try Fire + Ice for Frostfire, or Poison + Lightning for Storm."),
        TutorialStep("🙏", "Blessings", "Spend gold in the Upgrades menu to unlock Blessings — permanent stat boosts like Might, Vitality, and Luck."),
        TutorialStep("🐉", "Companion Pets", "Unlock pets through achievements! Owl boosts XP, Dragon deals fire damage, Cat increases luck."),
        TutorialStep("⭐", "Prestige", "After reaching high levels, Prestige to reset with permanent multipliers. 5 prestige levels with increasing bonuses."),
        TutorialStep("🏰", "Tower Defense", "Try TD mode! Move left/right to shoot enemies coming from above. Clear stages, defeat bosses, unlock new weapons!"),
        TutorialStep("🏆", "Goal", "Survive as long as possible, collect gold, unlock characters, and conquer all game modes!")
    )

    val currentStep = steps[step]
    val progress = (step + 1).toFloat() / steps.size

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0D2B), Color(0xFF0A0A1F), Color(0xFF050510)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 600.dp).padding(32.dp)
        ) {
            // Progress bar
            Box(
                Modifier.fillMaxWidth().height(4.dp).clip(SmallCutShape)
                    .background(Color(0xFF1A1A3F))
            ) {
                Box(
                    Modifier.fillMaxHeight().fillMaxWidth(progress).clip(SmallCutShape)
                        .background(Brush.horizontalGradient(listOf(HordeColors.SkyBlue, HordeColors.Lavender)))
                )
            }

            Spacer(Modifier.height(40.dp))

            // Step indicator
            Text(
                "${step + 1} / ${steps.size}",
                fontSize = 14.sp,
                color = HordeColors.TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            // Icon
            Text(currentStep.icon, fontSize = 64.sp)

            Spacer(Modifier.height(20.dp))

            // Title
            Text(
                currentStep.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(16.dp))

            // Description
            Text(
                currentStep.description,
                fontSize = 16.sp,
                color = HordeColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(Modifier.weight(1f))

            // Navigation buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    HordeBackButton(onClick = { step-- })
                } else {
                    HordeBackButton(text = "Skip", onClick = onSkip)
                }

                // Dots indicator
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(steps.size) { i ->
                        Box(
                            Modifier.size(8.dp).clip(SmallCutShape)
                                .background(if (i == step) HordeColors.SkyBlue else Color(0xFF1A1A3F))
                        )
                    }
                }

                if (step < steps.size - 1) {
                    HordeButton(
                        text = "Next",
                        icon = "→",
                        onClick = { step++ },
                        modifier = Modifier.width(130.dp)
                    )
                } else {
                    HordeButton(
                        text = "Play!",
                        icon = "🎮",
                        color = Color(0xFF66BB6A),
                        onClick = onFinish,
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
    }
}

private data class TutorialStep(
    val icon: String,
    val title: String,
    val description: String
)

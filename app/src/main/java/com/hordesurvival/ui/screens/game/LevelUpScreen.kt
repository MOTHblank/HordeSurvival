package com.hordesurvival.ui.screens.game

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
import com.hordesurvival.game.upgrade.Rarity
import com.hordesurvival.game.upgrade.UpgradeOption
import com.hordesurvival.game.upgrade.UpgradeType
import com.hordesurvival.ui.Locales
import com.hordesurvival.ui.theme.HordeColors
import com.hordesurvival.ui.components.CornerCutShape
import com.hordesurvival.ui.components.SmallCutShape

/**
 * Level-up screen with animated cards, rarity glow, and satisfying selection.
 */
@Composable
fun LevelUpScreen(
    level: Int,
    options: List<UpgradeOption>,
    onSelect: (UpgradeOption) -> Unit,
    languageCode: String = "en"
) {
    // Entrance animation
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "levelup_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .padding(20.dp)
        ) {
            // Level indicator with glow
            Text(
                text = "⬆ ${Locales.getString("level_up", languageCode)}",
                fontSize = 14.sp,
                color = HordeColors.SkyBlue.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$level",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = HordeColors.SkyBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Locales.getString("choose_upgrade", languageCode),
                fontSize = 16.sp,
                color = HordeColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Upgrade cards with staggered animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                options.forEachIndexed { index, option ->
                    var cardVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(100L * index)
                        cardVisible = true
                    }

                    val cardScale by animateFloatAsState(
                        targetValue = if (cardVisible) 1f else 0.5f,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "card_$index"
                    )

                    UpgradeCard(
                        option = option,
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f, fill = false).widthIn(max = 160.dp).scale(cardScale),
                        languageCode = languageCode
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(option: UpgradeOption, onClick: () -> Unit, modifier: Modifier = Modifier, languageCode: String = "en") {
    val rarityColor = when (option.rarity) {
        Rarity.COMMON -> HordeColors.Common
        Rarity.RARE -> HordeColors.Rare
        Rarity.EPIC -> HordeColors.Epic
        Rarity.LEGENDARY -> HordeColors.Legendary
    }

    // Evolution glow animation
    val isEvolution = option.targetTier == 6
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "press"
    )
    val inf = rememberInfiniteTransition(label = "evo")
    val evoGlow by inf.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glow")

    Box(
        modifier = modifier
            .scale(pressScale)
            .clip(CornerCutShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        rarityColor.copy(alpha = 0.25f),
                        HordeColors.CardBg.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = if (isEvolution) 2.5.dp else 1.5.dp,
                color = if (isEvolution) Color(0xFFFFD700).copy(alpha = evoGlow) else rarityColor.copy(alpha = 0.4f),
                shape = CornerCutShape
            )
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with glow
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CornerCutShape)
                    .background(rarityColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getUpgradeEmoji(option),
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = getLocalizedName(option, languageCode),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = getLocalizedDesc(option, languageCode),
                fontSize = 10.sp,
                color = HordeColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current level indicator
            if (option.currentTier > 0 && option.type != UpgradeType.NEW_WEAPON) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lv ${option.currentTier} → Lv ${option.targetTier}",
                    fontSize = 10.sp,
                    color = HordeColors.SkyBlue.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Rarity badge
            Box(
                modifier = Modifier
                    .clip(SmallCutShape)
                    .background(rarityColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = option.rarity.name,
                    fontSize = 9.sp,
                    color = rarityColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Evolution badge
            if (isEvolution) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✦ EVOLUTION",
                    fontSize = 8.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun getLocalizedName(option: UpgradeOption, lang: String): String {
    return when (option.type) {
        UpgradeType.NEW_WEAPON, UpgradeType.WEAPON_UPGRADE -> {
            option.weaponType?.let { Locales.getWeaponName(it.name, lang) } ?: option.name
        }
        UpgradeType.HEAL -> Locales.getString("heal", lang)
        UpgradeType.PASSIVE -> option.name // passives keep their English names for now
    }
}

private fun getLocalizedDesc(option: UpgradeOption, lang: String): String {
    return when (option.type) {
        UpgradeType.NEW_WEAPON -> {
            option.weaponType?.let { Locales.getWeaponDesc(it.name, lang) } ?: option.description
        }
        UpgradeType.WEAPON_UPGRADE -> {
            option.weaponType?.let { w ->
                val abbrev = Locales.weaponAbbrev(w.name)
                val tier = option.targetTier
                if (tier in 1..5) Locales.getUpgradeDesc(abbrev, tier, lang) else option.description
            } ?: option.description
        }
        else -> option.description
    }
}

private fun getUpgradeEmoji(option: UpgradeOption): String {
    return when {
        option.type == UpgradeType.HEAL -> "💚"
        option.type == UpgradeType.NEW_WEAPON -> "🆕"
        option.icon.contains("missile") -> "🔮"
        option.icon.contains("lightning") -> "⚡"
        option.icon.contains("fire") -> "🔥"
        option.icon.contains("ice") -> "❄️"
        option.icon.contains("poison") -> "☠️"
        option.icon.contains("boomerang") -> "🗡️"
        option.icon.contains("shield") -> "🛡️"
        option.icon.contains("spear") -> "🔱"
        option.icon.contains("spinach") -> "🥬"
        option.icon.contains("tome") -> "📖"
        option.icon.contains("crown") -> "👑"
        option.icon.contains("wings") -> "🪽"
        option.icon.contains("duplicator") -> "✨"
        option.icon.contains("heart") -> "❤️"
        option.icon.contains("clover") -> "🍀"
        option.icon.contains("magnet") -> "🧲"
        option.icon.contains("growth") -> "📈"
        option.icon.contains("speedster") -> "💨"
        option.icon.contains("vampire") -> "🧛"
        else -> "⭐"
    }
}

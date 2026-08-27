package com.hordesurvival.ui.screens.characterselect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hordesurvival.data.model.UnlockedCharacter
import com.hordesurvival.ui.components.HordeBackButton
import com.hordesurvival.ui.components.HordeButton
import com.hordesurvival.ui.components.HordeHeader
import com.hordesurvival.ui.components.HordeItemCard
import com.hordesurvival.ui.components.HordeScreen
import com.hordesurvival.ui.components.SmallCutShape
import com.hordesurvival.ui.theme.HordeColors

/**
 * Character selection screen with heroic tiles and arcade styling.
 */
@Composable
fun CharacterSelectScreen(
    characters: List<UnlockedCharacter>,
    selectedId: Int,
    onSelect: (UnlockedCharacter) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onUnlock: (Int, Int) -> Unit,  // characterId, cost
    gold: Int,
    languageCode: String = "en"
) {
    HordeScreen(contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HordeHeader(
                title = "SELECT HERO",
                subtitle = "Choose your warrior for the horde battle",
                icon = "👤",
                accentColor = HordeColors.Lavender
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(characters) { char ->
                    CharacterCard(
                        character = char,
                        isSelected = char.characterId == selectedId,
                        onClick = {
                            if (char.isUnlocked) onSelect(char)
                        },
                        onUnlock = { id, cost -> onUnlock(id, cost) },
                        gold = gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HordeBackButton(onClick = onBack)

                HordeButton(
                    text = "CONFIRM",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: UnlockedCharacter,
    isSelected: Boolean,
    onClick: () -> Unit,
    onUnlock: (Int, Int) -> Unit,
    gold: Int
) {
    val alpha = if (character.isUnlocked) 1f else 0.5f

    HordeItemCard(
        modifier = Modifier.height(180.dp).alpha(alpha),
        selected = isSelected,
        onClick = if (character.isUnlocked) onClick else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Character icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(SmallCutShape)
                    .background(HordeColors.Lavender.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCharacterEmoji(character.characterId),
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = character.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (character.isUnlocked) Color.White else HordeColors.TextSecondary
            )

            Text(
                text = character.description,
                fontSize = 11.sp,
                color = HordeColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (!character.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onUnlock(character.characterId, 500) },
                    enabled = gold >= 500
                ) {
                    Text(
                        "🔒 Unlock (500💰)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gold >= 500) HordeColors.GoldColor else HordeColors.TextSecondary
                    )
                }
            }
        }
    }
}

private fun getCharacterEmoji(id: Int): String = when (id) {
    0 -> "🧙"; 1 -> "🛡️"; 2 -> "🗡️"; 3 -> "⚗️"; 4 -> "🔮"
    5 -> "🔥"; 6 -> "❄️"; 7 -> "⚡"; 8 -> "🗡️"; 9 -> "💀"
    else -> "👤"
}

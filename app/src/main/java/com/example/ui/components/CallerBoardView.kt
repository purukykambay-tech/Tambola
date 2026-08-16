package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallerPhrases
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SleekPurple
import com.example.ui.theme.SleekPurpleContainer
import com.example.ui.theme.SleekPurpleDark

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CallerBoardView(
    currentCalledNumber: Int?,
    calledNumbers: List<Int>,
    isAutoCalling: Boolean,
    autoCallIntervalSec: Int,
    isSoundEnabled: Boolean,
    autoCallCountdownProgress: Float = 0f,
    autoCallRemainingMillis: Long = 0L,
    onCallNext: () -> Unit,
    onToggleAutoCall: () -> Unit,
    onSetIntervalSec: (Int) -> Unit,
    onToggleSound: () -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calledSet = calledNumbers.toSet()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("caller_board_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header & Call Showcase
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = SleekPurpleContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TAMBOLA NUMBER CALLER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SleekPurpleDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Floating Sphere showcasing current number
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(SleekPurple)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentCalledNumber,
                            transitionSpec = {
                                (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                            }
                        ) { num ->
                            Text(
                                text = num?.toString() ?: "--",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phrase Nickname Display
                    Text(
                        text = currentCalledNumber?.let { CallerPhrases.getPhrase(it) } ?: "Tap Next or Start Auto-Draw",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SleekPurpleDark,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Caller Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Toggle Button
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier.testTag("toggle_sound_btn")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = if (isSoundEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Next Call Primary Button
                Button(
                    onClick = onCallNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("draw_next_num_btn")
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = "Next")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "NEXT NUMBER", fontWeight = FontWeight.Bold)
                }

                // Auto Call Play/Pause
                Button(
                    onClick = onToggleAutoCall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAutoCalling) CoralRed else EmeraldGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("toggle_auto_call_btn")
                ) {
                    Icon(
                        imageVector = if (isAutoCalling) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Auto"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isAutoCalling) "PAUSE" else "AUTO", fontWeight = FontWeight.Bold)
                }

                // Reset Button
                IconButton(
                    onClick = onResetGame,
                    modifier = Modifier.testTag("reset_game_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Game",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Live Countdown Progress Bar when auto calling
            if (isAutoCalling) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auto-Drawing...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = "${String.format("%.1f", autoCallRemainingMillis / 1000f)}s / ${autoCallIntervalSec}s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { autoCallCountdownProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldGreen,
                        trackColor = EmeraldGreen.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Auto Speed Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Setting: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(
                    2 to "2s",
                    3 to "3s",
                    4 to "4s",
                    6 to "6s",
                    8 to "8s"
                ).forEach { (interval, label) ->
                    FilterChip(
                        selected = autoCallIntervalSec == interval,
                        onClick = { onSetIntervalSec(interval) },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Drawn Counter Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Drawn: ${calledNumbers.size} / 90",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Remaining: ${90 - calledNumbers.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1 to 90 Board Matrix Grid (10 Columns x 9 Rows)
            LazyVerticalGrid(
                columns = GridCells.Fixed(10),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items((1..90).toList()) { num ->
                    val isCalled = calledSet.contains(num)
                    val isCurrent = currentCalledNumber == num

                    val bg = when {
                        isCurrent -> AmberGold
                        isCalled -> EmeraldGreen
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val textColor = when {
                        isCurrent -> Color.Black
                        isCalled -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bg)
                            .border(
                                width = if (isCurrent) 2.dp else 0.5.dp,
                                color = if (isCurrent) CoralRed else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCalled || isCurrent) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

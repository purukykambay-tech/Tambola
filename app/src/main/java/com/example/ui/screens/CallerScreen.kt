package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallerPhrases
import com.example.ui.components.AdminGameOverridePanel
import com.example.ui.components.CallerBoardView
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.ui.theme.SleekPurpleContainer
import com.example.ui.theme.SleekPurpleDark
import com.example.viewmodel.TambolaUiState

@Composable
fun CallerScreen(
    state: TambolaUiState,
    onCallNext: () -> Unit,
    onCallSpecificNumber: (Int) -> Boolean = { false },
    onToggleAutoCall: () -> Unit,
    onSetIntervalSec: (Int) -> Unit,
    onToggleSound: () -> Unit,
    onResetGame: () -> Unit,
    onToggleBackgroundService: () -> Unit = {},
    onDrawViaFirestore: () -> Unit = {},
    onResetFirestoreGame: () -> Unit = {},
    onBroadcastMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Real-Time Admin Override & Controlled Progression Remote Panel
        item {
            AdminGameOverridePanel(
                state = state,
                onCallSpecificNumber = onCallSpecificNumber,
                onCallNextNumber = onCallNext,
                onToggleAutoCall = onToggleAutoCall,
                onSetIntervalSec = onSetIntervalSec,
                onResetGame = onResetGame,
                onBroadcastMessage = onBroadcastMessage
            )
        }

        // Firestore 'current_game' Background Engine Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firestore_engine_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning)
                        SleekPurpleContainer
                    else
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning)
                                    Icons.Default.CloudSync
                                else
                                    Icons.Default.CloudDone,
                                contentDescription = "Cloud Engine",
                                tint = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning)
                                    SleekPurple
                                else
                                    EmeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FIRESTORE 'current_game' ENGINE",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekPurpleDark
                                )
                                Text(
                                    text = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning)
                                        "Background Service Active • Pulling Numbers"
                                    else
                                        "Service Ready • Collection: current_game",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning)
                                EmeraldGreen.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning) "LIVE SYNC" else "IDLE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (state.isBackgroundCallerRunning || state.currentGameState.isRunning) EmeraldGreen else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onToggleBackgroundService,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("toggle_background_service_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isBackgroundCallerRunning) CoralRed else SleekPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isBackgroundCallerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isBackgroundCallerRunning) "STOP SERVICE" else "START SERVICE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = onDrawViaFirestore,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("draw_via_firestore_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DRAW CLOUD",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Core Board & Caller Controls
        item {
            CallerBoardView(
                currentCalledNumber = state.currentCalledNumber,
                calledNumbers = state.calledNumbers,
                isAutoCalling = state.isAutoCalling || state.isBackgroundCallerRunning,
                autoCallIntervalSec = state.autoCallIntervalSec,
                isSoundEnabled = state.isSoundEnabled,
                autoCallCountdownProgress = state.autoCallCountdownProgress,
                autoCallRemainingMillis = state.autoCallRemainingMillis,
                onCallNext = onCallNext,
                onToggleAutoCall = onToggleAutoCall,
                onSetIntervalSec = onSetIntervalSec,
                onToggleSound = onToggleSound,
                onResetGame = {
                    onResetFirestoreGame()
                    onResetGame()
                }
            )
        }

        // Recent Called History Reel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RECENT DRAW HISTORY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.calledNumbers.isEmpty()) {
                        Text(
                            text = "No numbers drawn yet. Tap NEXT NUMBER or START SERVICE to begin calling!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.calledNumbers) { num ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (num == state.currentCalledNumber) AmberGold else EmeraldGreen,
                                    shadowElevation = 2.dp
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (num == state.currentCalledNumber) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

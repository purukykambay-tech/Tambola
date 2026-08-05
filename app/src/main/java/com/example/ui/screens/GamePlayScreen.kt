package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallerPhrases
import com.example.model.ClaimType
import com.example.ui.components.BotChatFeed
import com.example.ui.components.TambolaTicketCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.viewmodel.TambolaUiState

@Composable
fun GamePlayScreen(
    state: TambolaUiState,
    onCallNextNumber: () -> Unit,
    onToggleAutoCall: () -> Unit,
    onSetTicketCount: (Int) -> Unit,
    onToggleMark: (String, Int) -> Unit,
    onAutoMark: () -> Unit,
    onClaimPrize: (String, ClaimType) -> Unit,
    onSendMessage: (String) -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Called Ticker Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalPurple)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT NUMBER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                        Text(
                            text = state.currentCalledNumber?.let { CallerPhrases.getPhrase(it) } ?: "Waiting for first call...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AmberGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.currentCalledNumber?.toString() ?: "--",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Quick Controls & Ticket Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ticket count selector chips
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tickets: ",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            listOf(1, 2, 4).forEach { count ->
                                FilterChip(
                                    selected = state.playerTickets.size == count,
                                    onClick = { onSetTicketCount(count) },
                                    label = { Text("$count", fontSize = 12.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }

                        // Auto-Dab Button
                        OutlinedButton(
                            onClick = onAutoMark,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("auto_dab_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Auto", tint = AmberGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Auto-Dab", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Draw & Auto Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCallNextNumber,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("draw_num_game_btn")
                        ) {
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = "Draw")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "DRAW NUMBER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = onToggleAutoCall,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isAutoCalling) CoralRed else EmeraldGreen
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("auto_call_game_btn")
                        ) {
                            Icon(
                                imageVector = if (state.isAutoCalling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Auto"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (state.isAutoCalling) "PAUSE AUTO" else "START AUTO", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        IconButton(onClick = onResetGame) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                        }
                    }
                }
            }
        }

        // Player Score & Active Claims Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Score", tint = AmberGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Your Points: ${state.userScore} pts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "Claims Won: ${state.claimedPrizes.size} / 7",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Player Ticket Cards
        items(state.playerTickets, key = { it.id }) { ticket ->
            TambolaTicketCard(
                ticket = ticket,
                markedNumbers = state.markedNumbersMap[ticket.id] ?: emptySet(),
                calledNumbers = state.calledNumbers.toSet(),
                onToggleMark = { num -> onToggleMark(ticket.id, num) },
                onClaimPrize = { claim -> onClaimPrize(ticket.id, claim) },
                claimedPrizes = state.claimedPrizes
            )
        }

        // Room Chat Feed
        item {
            BotChatFeed(
                messages = state.chatMessages,
                onSendMessage = onSendMessage
            )
        }
    }
}

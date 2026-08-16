package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.AdminGameOverridePanel
import com.example.ui.components.BotChatFeed
import com.example.ui.components.TambolaTicketCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.viewmodel.TambolaUiState

@Composable
fun GamePlayScreen(
    state: TambolaUiState,
    onCallNextNumber: () -> Unit,
    onCallSpecificNumber: (Int) -> Boolean = { false },
    onToggleAutoCall: () -> Unit,
    onSetIntervalSec: (Int) -> Unit = {},
    onSetTicketCount: (Int) -> Unit,
    onToggleMark: (String, Int) -> Unit,
    onAutoMark: () -> Unit,
    onToggleAutoDab: () -> Unit,
    onOpenSettings: () -> Unit,
    onClaimPrize: (String, ClaimType) -> Unit,
    onSendMessage: (String) -> Unit,
    onResetGame: () -> Unit,
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
                onCallNextNumber = onCallNextNumber,
                onToggleAutoCall = onToggleAutoCall,
                onSetIntervalSec = onSetIntervalSec,
                onResetGame = onResetGame,
                onBroadcastMessage = onBroadcastMessage
            )
        }

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

        // Automatic Play Live Countdown Banner (When Active)
        if (state.isAutoCalling) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldGreen,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Timer",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "⚡ AUTOMATIC PLAY ACTIVE",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = EmeraldGreen
                                    )
                                    val remainingSecFloat = (state.autoCallRemainingMillis / 1000f)
                                    Text(
                                        text = "Next call in ${String.format("%.1f", remainingSecFloat)}s (Interval: ${state.autoCallIntervalSec}s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Quick Pause Button
                            OutlinedButton(
                                onClick = onToggleAutoCall,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("auto_pause_banner_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PAUSE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Smooth animated countdown progress bar
                        LinearProgressIndicator(
                            progress = { state.autoCallCountdownProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldGreen,
                            trackColor = EmeraldGreen.copy(alpha = 0.2f)
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
                    // Top row: Ticket Count & Auto-Dab Setting Switch & Settings Button
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

                        // Auto-Dab Quick Toggle Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (state.isAutoDabEnabled) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onToggleAutoDab() }
                                .testTag("auto_dab_toggle_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Auto Dab",
                                    tint = if (state.isAutoDabEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isAutoDabEnabled) "Auto-Dab ON" else "Auto-Dab OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isAutoDabEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = state.isAutoDabEnabled,
                                    onCheckedChange = { onToggleAutoDab() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmeraldGreen
                                    ),
                                    modifier = Modifier.testTag("auto_dab_switch")
                                )
                            }
                        }

                        // Settings dialog button
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("open_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Draw & Auto Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
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
                            Text(
                                text = if (state.isAutoCalling) "PAUSE AUTO" else "START AUTO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Manual Catch-up Dab Button
                        IconButton(
                            onClick = onAutoMark,
                            modifier = Modifier.testTag("auto_dab_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Catch up Dab",
                                tint = AmberGold
                            )
                        }

                        IconButton(onClick = onResetGame) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Setting / Auto-Speed Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = SleekPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Time Setting:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                2 to "2s",
                                3 to "3s",
                                4 to "4s",
                                6 to "6s",
                                8 to "8s"
                            ).forEach { (sec, label) ->
                                val isSelected = state.autoCallIntervalSec == sec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSetIntervalSec(sec) },
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SleekPurple,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
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

        // Auto-Dab Automation Status Pill
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EmeraldGreen.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Full Automation Active: Drawn numbers are automatically marked across all tickets. Winner announcements & claim notifications trigger instantly!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldGreen,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Section Title: My Active Tickets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Tickets (${state.playerTickets.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F1F1F)
                )
                Text(
                    text = "Auto-Marked",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
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
                claimedPrizes = state.claimedPrizes,
                isAutoDabEnabled = state.isAutoDabEnabled,
                onToggleAutoDab = onToggleAutoDab
            )
        }

        // Other Players' Live Room Tickets (Limited Room Slots or Multiplayer Bots)
        val otherRoomSlots = state.currentJoinedRoom?.ticketSlots?.filter { slot ->
            slot.isBooked && state.playerTickets.none { it.id == slot.ticket.id }
        } ?: emptyList()

        if (otherRoomSlots.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Players' Live Tickets (${otherRoomSlots.size} Opponents)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalPurple
                    )
                    Text(
                        text = "Live Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurple
                    )
                }
            }

            items(otherRoomSlots, key = { "slot_${it.slotNumber}_${it.ticket.id}" }) { slot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekPurple
                                ) {
                                    Text(
                                        text = "Slot #${slot.slotNumber}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slot.bookedByName.ifBlank { "Player ${slot.slotNumber}" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2B2B2B)
                                )
                            }

                            val calledSet = state.calledNumbers.toSet()
                            val allSlotNums = slot.ticket.getAllNumbers()
                            val goneCount = allSlotNums.count { calledSet.contains(it) }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (goneCount == 15) AmberGold else Color(0xFFF0EBF8)
                            ) {
                                Text(
                                    text = "$goneCount/15 Gone",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (goneCount == 15) Color.Black else RoyalPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mini Ticket Grid View with automatic gone number marks
                        TambolaTicketCard(
                            ticket = slot.ticket,
                            markedNumbers = slot.ticket.getAllNumbers().filter { state.calledNumbers.contains(it) }.toSet(),
                            calledNumbers = state.calledNumbers.toSet(),
                            onToggleMark = {},
                            onClaimPrize = {},
                            claimedPrizes = state.claimedPrizes,
                            isAutoDabEnabled = true
                        )
                    }
                }
            }
        } else if (state.bots.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Opponents' Live Tickets (${state.bots.size} Players)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalPurple
                    )
                    Text(
                        text = "Live Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPurple
                    )
                }
            }

            items(state.bots, key = { it.name }) { bot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bot.avatarEmoji} ${bot.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF2B2B2B)
                            )
                            val marked = state.botMarkedMap[bot.name] ?: emptySet()
                            Text(
                                text = "${marked.size}/15 Marked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TambolaTicketCard(
                            ticket = bot.ticket,
                            markedNumbers = state.botMarkedMap[bot.name] ?: bot.ticket.getAllNumbers().filter { state.calledNumbers.contains(it) }.toSet(),
                            calledNumbers = state.calledNumbers.toSet(),
                            onToggleMark = {},
                            onClaimPrize = {},
                            claimedPrizes = state.claimedPrizes,
                            isAutoDabEnabled = true
                        )
                    }
                }
            }
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


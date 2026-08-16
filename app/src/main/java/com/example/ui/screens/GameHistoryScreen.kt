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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TambolaWinnerHistory
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleVariant
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.ui.theme.SleekPurpleContainer
import com.example.ui.theme.SleekPurpleDark
import com.example.ui.theme.SleekPurpleLight
import com.example.viewmodel.TambolaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class WinnerFilter(val label: String) {
    ALL("All Winners"),
    FULL_HOUSE("Full House"),
    EARLY_5("Early 5"),
    LINES("Lines & Corners"),
    HIGH_PRIZE("High Stakes (₹10k+)")
}

@Composable
fun GameHistoryScreen(
    state: TambolaUiState,
    onDeleteTicket: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Firestore Winners (Transparency), 1: My Matches & Saved Tickets
    var selectedFilter by remember { mutableStateOf(WinnerFilter.ALL) }

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val filteredWinners = remember(state.pastWinnersHistory, selectedFilter) {
        when (selectedFilter) {
            WinnerFilter.ALL -> state.pastWinnersHistory
            WinnerFilter.FULL_HOUSE -> state.pastWinnersHistory.filter { it.claimPattern.contains("Full House", ignoreCase = true) }
            WinnerFilter.EARLY_5 -> state.pastWinnersHistory.filter { it.claimPattern.contains("Early", ignoreCase = true) }
            WinnerFilter.LINES -> state.pastWinnersHistory.filter {
                it.claimPattern.contains("Line", ignoreCase = true) || it.claimPattern.contains("Corner", ignoreCase = true)
            }
            WinnerFilter.HIGH_PRIZE -> state.pastWinnersHistory.filter { it.prizeAmount >= 10000 }
        }
    }

    val totalPrizeDistributed = remember(state.pastWinnersHistory) {
        state.pastWinnersHistory.sumOf { it.prizeAmount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header: Transparency & Trust Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transparency_hero_banner"),
                colors = CardDefaults.cardColors(containerColor = SleekPurpleDark),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SleekPurpleDark, RoyalPurple, PurpleVariant)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmberGold.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Trophy",
                                            tint = AmberGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "GAME HISTORY & WINNERS",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "Firestore Live",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "100% Real-Time Firestore Verified",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Fair Play",
                                        tint = AmberGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "FAIR PLAY",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Aggregate Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "₹${"%,d".format(totalPrizeDistributed)}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = AmberGold
                                )
                                Text(
                                    text = "Total Paid Out",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${state.pastWinnersHistory.size}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Matches Won",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "100%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "Auto-Audited",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Selector: Past Winners vs Personal Match Logs
        item {
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = RoyalPurple,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedSection == 0) RoyalPurple else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Winners (${state.pastWinnersHistory.size})",
                                fontWeight = if (selectedSection == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedSection == 1) RoyalPurple else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "My Records (${state.gameRecordsList.size})",
                                fontWeight = if (selectedSection == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }
        }

        if (selectedSection == 0) {
            // Filter Chips Bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WinnerFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Winners Feed List
            if (filteredWinners.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberGold.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No past winners recorded under this filter yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredWinners, key = { it.id }) { winner ->
                    WinnerCardItem(winner = winner, dateFormat = dateFormat)
                }
            }
        } else {
            // Local Match Logs & Saved Tickets
            item {
                Text(
                    text = "YOUR LOCAL MATCH HISTORY",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.gameRecordsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No matches played locally yet. Join a match or play against bots to build your record!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(state.gameRecordsList, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = record.gameMode,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Numbers Called: ${record.totalNumbersCalled} | User Points: ${record.userScore} pts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (record.winnerSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Claims Won: ${record.winnerSummary}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WinnerCardItem(
    winner: TambolaWinnerHistory,
    dateFormat: SimpleDateFormat
) {
    val isFullHouse = winner.claimPattern.contains("Full House", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("winner_card_${winner.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullHouse) SleekPurpleContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Room Title + Prize Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isFullHouse) AmberGold else SleekPurpleLight,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFullHouse) Icons.Default.WorkspacePremium else Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = if (isFullHouse) SleekPurpleDark else SleekPurpleDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = winner.roomTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(winner.verifiedTimestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "₹${"%,d".format(winner.prizeAmount)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle: Winner Name & Claim Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = winner.winnerName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (winner.winnerPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${winner.winnerPhone})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Claim: ${winner.claimPattern}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SleekPurple
                    )
                }

                // Winning Ball Info
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = CircleShape,
                        color = AmberGold,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${winner.winningNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = SleekPurpleDark
                            )
                        }
                    }
                    Text(
                        text = "on call #${winner.totalNumbersCalled}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Verification & Audit Trail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "System Verified Claim",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen
                    )
                }

                Text(
                    text = "Ref: ${winner.transactionRef}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

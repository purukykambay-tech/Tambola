package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClaimType
import com.example.model.TambolaTicket
import com.example.ui.components.TambolaTicketCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.viewmodel.TambolaUiState

@Composable
fun VerifierScreen(
    state: TambolaUiState,
    onEvaluateTicket: (TambolaTicket, ClaimType) -> Unit,
    modifier: Modifier = Modifier
) {
    val allAvailableTickets = state.playerTickets + state.bots.map { it.ticket }
    var selectedTicket by remember { mutableStateOf(allAvailableTickets.firstOrNull()) }

    val calledSet = state.calledNumbers.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = "Verifier",
                        tint = AmberGold,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "INSTANT TICKET VERIFIER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Check ticket status against ${state.calledNumbers.size} called numbers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Ticket Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SELECT TICKET TO VERIFY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allAvailableTickets.forEach { t ->
                            FilterChip(
                                selected = selectedTicket?.id == t.id,
                                onClick = { selectedTicket = t },
                                label = { Text(t.id, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Selected Ticket Details & Instant Claims Status Audit
        selectedTicket?.let { ticket ->
            item {
                TambolaTicketCard(
                    ticket = ticket,
                    markedNumbers = state.markedNumbersMap[ticket.id] ?: emptySet(),
                    calledNumbers = calledSet,
                    onToggleMark = { },
                    onClaimPrize = { claim -> onEvaluateTicket(ticket, claim) }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CLAIM VALIDITY AUDIT FOR ${ticket.id}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ClaimType.values().forEach { claim ->
                            val missing = when (claim) {
                                ClaimType.EARLY_FIVE -> {
                                    val calledCount = ticket.getAllNumbers().count { calledSet.contains(it) }
                                    if (calledCount >= 5) emptyList() else listOf(5 - calledCount)
                                }
                                ClaimType.FOUR_CORNERS -> ticket.getCornerNumbers().filter { !calledSet.contains(it) }
                                ClaimType.TOP_LINE -> ticket.getRowNumbers(0).filter { !calledSet.contains(it) }
                                ClaimType.MIDDLE_LINE -> ticket.getRowNumbers(1).filter { !calledSet.contains(it) }
                                ClaimType.BOTTOM_LINE -> ticket.getRowNumbers(2).filter { !calledSet.contains(it) }
                                ClaimType.FULL_HOUSE_1, ClaimType.FULL_HOUSE_2 -> ticket.getAllNumbers().filter { !calledSet.contains(it) }
                            }

                            val isValid = missing.isEmpty()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = "Status",
                                        tint = if (isValid) EmeraldGreen else CoralRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = claim.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isValid) "VALID NOW!" else "Missing ${missing.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isValid) EmeraldGreen else CoralRed
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isValid) EmeraldGreen else CoralRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isValid) "VALID" else "BOGUS",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isValid) Color.White else CoralRed,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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

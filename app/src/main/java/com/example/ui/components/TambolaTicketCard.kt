package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClaimType
import com.example.model.TambolaTicket
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TicketCellBlank

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TambolaTicketCard(
    ticket: TambolaTicket,
    markedNumbers: Set<Int>,
    calledNumbers: Set<Int>,
    onToggleMark: (Int) -> Unit,
    onClaimPrize: (ClaimType) -> Unit,
    claimedPrizes: Map<ClaimType, String> = emptyMap(),
    isAutoDabEnabled: Boolean = false,
    onToggleAutoDab: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val allNumbers = ticket.getAllNumbers()
    val totalCount = allNumbers.size
    val markedCount = allNumbers.count { markedNumbers.contains(it) }
    val calledMarkedCount = allNumbers.count { markedNumbers.contains(it) && calledNumbers.contains(it) }

    var showClaimMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ticket.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Ticket Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Ticket",
                        tint = AmberGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ticket ${ticket.id}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isAutoDabEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen,
                            modifier = Modifier.testTag("ticket_auto_dab_badge_${ticket.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ AUTO-DAB ON",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$markedCount / $totalCount marked",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { markedCount.toFloat() / totalCount.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (markedCount == totalCount) EmeraldGreen else AmberGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3x9 Grid Layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ticket.grid.forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEachIndexed { colIndex, num ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                if (num == null) {
                                    // Empty blank cell
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TicketCellBlank.copy(alpha = 0.5f))
                                    )
                                } else {
                                    val isMarked = markedNumbers.contains(num)
                                    val isCalled = calledNumbers.contains(num)

                                    val cellBg = when {
                                        isMarked && isCalled -> EmeraldGreen
                                        isMarked -> AmberGold
                                        isCalled -> AmberGold.copy(alpha = 0.25f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    val textColor = when {
                                        isMarked && isCalled -> Color.White
                                        isMarked -> Color.Black
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cellBg)
                                            .border(
                                                width = if (isCalled && !isMarked) 1.5.dp else 1.dp,
                                                color = if (isCalled && !isMarked) CoralRed else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { onToggleMark(num) }
                                            .testTag("ticket_cell_${ticket.id}_$num"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isMarked || isCalled) FontWeight.ExtraBold else FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )

                                        if (isMarked) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Marked",
                                                tint = if (isCalled) Color.White.copy(alpha = 0.9f) else RoyalPurple.copy(alpha = 0.9f),
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Claim Prizes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (calledMarkedCount > 0) "$calledMarkedCount verified called" else "Tap numbers to dab",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    Button(
                        onClick = { showClaimMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("claim_prizes_btn_${ticket.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Claim",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Claim Prize", fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showClaimMenu,
                        onDismissRequest = { showClaimMenu = false }
                    ) {
                        ClaimType.values().forEach { claim ->
                            val isClaimed = claimedPrizes.containsKey(claim)
                            val winner = claimedPrizes[claim]

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = claim.displayName,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isClaimed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = claim.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isClaimed) {
                                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text("Claimed ($winner)")
                                            }
                                        } else {
                                            Badge(containerColor = EmeraldGreen) {
                                                Text("+${claim.prizePoints} pts")
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    showClaimMenu = false
                                    if (!isClaimed) {
                                        onClaimPrize(claim)
                                    }
                                },
                                enabled = !isClaimed
                            )
                        }
                    }
                }
            }
        }
    }
}

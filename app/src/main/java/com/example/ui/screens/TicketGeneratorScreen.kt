package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TambolaTicket
import com.example.ui.components.TambolaTicketCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.viewmodel.TambolaUiState

@Composable
fun TicketGeneratorScreen(
    state: TambolaUiState,
    onGenerateSheetOfSix: () -> Unit,
    onGenerateSingleTicket: () -> Unit = {},
    onSaveTicket: (TambolaTicket, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ticketNameInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = "Generator",
                                tint = AmberGold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TICKET GENERATOR",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firestore Sync",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Generate authentic 3x9 grid 90-ball Tambola tickets (15 numbers per ticket, 5 numbers per row). Save directly to your cloud Firestore account or copy to clipboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onGenerateSingleTicket,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_single_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Single")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+1 TICKET", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onGenerateSheetOfSix,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1.6f)
                                .testTag("generate_sheet_btn")
                        ) {
                            Icon(imageVector = Icons.Default.GridOn, contentDescription = "Sheet")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "SHEET OF 6 (1-90)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Generated Tickets List
        if (state.generatedSheet.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap GENERATE FULL SHEET above to produce 6 unique Tambola tickets!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.generatedSheet, key = { it.id }) { ticket ->
                Column {
                    TambolaTicketCard(
                        ticket = ticket,
                        markedNumbers = emptySet(),
                        calledNumbers = emptySet(),
                        onToggleMark = { },
                        onClaimPrize = { }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val textFormat = ticket.grid.joinToString("\n") { row ->
                                    row.joinToString(" | ") { it?.toString() ?: "  " }
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Tambola Ticket ${ticket.id}", textFormat)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Ticket ${ticket.id} copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Grid", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveTicket(ticket, "Custom ${ticket.id}")
                                Toast.makeText(context, "Ticket ${ticket.id} saved to Cloud Firestore & Local DB!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Save to Cloud", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save & Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

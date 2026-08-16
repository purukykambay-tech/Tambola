package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.getValue
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
import com.example.model.RazorpayTransaction
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple

/**
 * Modern Material 3 User Wallet Component
 * Displays live balance synced from Firestore, recent transactions,
 * and quick actions to trigger mock UPI payments with preset amounts.
 */
@Composable
fun UserWalletCard(
    walletBalance: Int,
    userPhone: String,
    isLoggedIn: Boolean,
    recentTransactions: List<RazorpayTransaction> = emptyList(),
    onAddFundsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecentTxs by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_wallet_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            RoyalPurple,
                            Color(0xFF2E0854),
                            Color(0xFF190033)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header: Title & Cloud Sync Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmberGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet Icon",
                                    tint = AmberGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Housie Wallet",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (isLoggedIn) userPhone else "Guest Mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Firestore Live Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Firestore Synced",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firestore Live",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Balance Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹$walletBalance",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberGold
                        )
                    }

                    // Add Funds via UPI Button
                    Button(
                        onClick = onAddFundsClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier.testTag("wallet_add_funds_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = "Add Funds",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ ADD FUNDS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom row: UPI Quick Chips and Security Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAddFundsClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "UPI",
                            tint = AmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPay / PhonePe / Paytm UPI Supported",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (recentTransactions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { showRecentTxs = !showRecentTxs }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showRecentTxs) "Hide" else "Logs (${recentTransactions.size})",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // Expandable Recent Transactions Dropdown
                AnimatedVisibility(visible = showRecentTxs && recentTransactions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Recent Firestore Transaction Logs:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        recentTransactions.take(3).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${tx.paymentMethod} (${tx.paymentId.takeLast(6)})",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "+₹${tx.amount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
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

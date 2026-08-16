package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.WalletTransaction
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.viewmodel.TambolaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTransactionHistoryScreen(
    state: TambolaUiState,
    onOpenDepositModal: () -> Unit,
    onWithdrawFunds: (amount: Int, payoutMethod: String, payoutDetails: String) -> Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(TransactionType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTxForReceipt by remember { mutableStateOf<WalletTransaction?>(null) }
    var showWithdrawModal by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Filter and search transactions
    val filteredTransactions = remember(state.walletTransactions, selectedFilter, searchQuery) {
        state.walletTransactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                TransactionType.ALL -> true
                TransactionType.DEPOSIT -> tx.type == TransactionType.DEPOSIT
                TransactionType.WITHDRAWAL -> tx.type == TransactionType.WITHDRAWAL
                TransactionType.TICKET_PURCHASE -> tx.type == TransactionType.TICKET_PURCHASE
                TransactionType.PRIZE_WIN -> tx.type == TransactionType.PRIZE_WIN
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                tx.id.contains(searchQuery, ignoreCase = true) ||
                tx.referenceId.contains(searchQuery, ignoreCase = true) ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.description.contains(searchQuery, ignoreCase = true) ||
                tx.paymentMethod.contains(searchQuery, ignoreCase = true) ||
                tx.roomTitle.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    // Aggregates for summary
    val totalDeposits = remember(state.walletTransactions) {
        state.walletTransactions.filter { it.type == TransactionType.DEPOSIT && it.status == TransactionStatus.SUCCESS }
            .sumOf { it.amount }
    }
    val totalWithdrawals = remember(state.walletTransactions) {
        state.walletTransactions.filter { it.type == TransactionType.WITHDRAWAL && it.status == TransactionStatus.SUCCESS }
            .sumOf { it.amount }
    }
    val totalTicketSpend = remember(state.walletTransactions) {
        state.walletTransactions.filter { it.type == TransactionType.TICKET_PURCHASE && it.status == TransactionStatus.SUCCESS }
            .sumOf { it.amount }
    }
    val totalPrizeWon = remember(state.walletTransactions) {
        state.walletTransactions.filter { it.type == TransactionType.PRIZE_WIN && it.status == TransactionStatus.SUCCESS }
            .sumOf { it.amount }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F8F5))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- TOP NAVIGATION BAR ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("wallet_history_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F1F1F)
                        )
                    }

                    Text(
                        text = "Transaction Passbook",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1F1F1F)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AmberGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onOpenDepositModal() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Funds",
                                tint = Color(0xFFD48800),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Deposit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD48800)
                            )
                        }
                    }
                }
            }

            // --- HERO BALANCE & FINANCIAL OVERVIEW CARD ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_history_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        RoyalPurple,
                                        Color(0xFF2F0854),
                                        Color(0xFF16002E)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AmberGold.copy(alpha = 0.2f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = "Wallet",
                                                tint = AmberGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "TOTAL WALLET BALANCE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.7f),
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = if (state.isLoggedIn) state.userMobileNumber else "Guest Passbook",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Secured",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Verified 256-bit",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "₹${state.walletBalance}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberGold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick Action Buttons (Deposit & Withdraw)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onOpenDepositModal,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberGold,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("passbook_add_funds_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Deposit",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Cash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showWithdrawModal = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("passbook_withdraw_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowOutward,
                                        contentDescription = "Withdraw",
                                        tint = AmberGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Withdraw", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 Mini Financial Summaries
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryItem(
                                    label = "Deposited",
                                    value = "₹$totalDeposits",
                                    valueColor = EmeraldGreen
                                )
                                SummaryItem(
                                    label = "Withdrawn",
                                    value = "₹$totalWithdrawals",
                                    valueColor = Color(0xFFFF8A80)
                                )
                                SummaryItem(
                                    label = "Tickets",
                                    value = "₹$totalTicketSpend",
                                    valueColor = Color(0xFFCE93D8)
                                )
                                SummaryItem(
                                    label = "Prizes",
                                    value = "₹$totalPrizeWon",
                                    valueColor = AmberGold
                                )
                            }
                        }
                    }
                }
            }

            // --- SEARCH & FILTER BAR ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by TXN ID, Game, UPI Ref...", fontSize = 12.sp, color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color(0xFFE0DDD5)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("transaction_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(TransactionType.values()) { type ->
                                val isSelected = selectedFilter == type
                                val label = when (type) {
                                    TransactionType.ALL -> "All (${state.walletTransactions.size})"
                                    TransactionType.DEPOSIT -> "Deposits 🟢"
                                    TransactionType.WITHDRAWAL -> "Withdrawals 🔴"
                                    TransactionType.TICKET_PURCHASE -> "Tickets 🎫"
                                    TransactionType.PRIZE_WIN -> "Prizes 🏆"
                                }

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = type },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalPurple,
                                        selectedLabelColor = AmberGold,
                                        containerColor = Color(0xFFF7F5F0),
                                        labelColor = Color(0xFF4A4A4A)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) RoyalPurple else Color(0xFFE0DDD5)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("filter_${type.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // --- TRANSACTIONS LIST SECTION HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chronological Statement",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = "${filteredTransactions.size} Records Found",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- TRANSACTION ITEMS / EMPTY STATE ---
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFF7E6),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "No Transactions",
                                        tint = AmberGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Transactions Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No records match '$searchQuery'" else "You haven't made any transactions in this category yet.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenDepositModal,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+ Deposit First Cash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        dateFormat = dateFormat,
                        onCopyRef = { ref ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Reference ID", ref)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied Ref ID: $ref", Toast.LENGTH_SHORT).show()
                        },
                        onViewReceipt = { selectedTxForReceipt = tx }
                    )
                }
            }
        }
    }

    // --- RECEIPT MODAL DIALOG ---
    selectedTxForReceipt?.let { tx ->
        TransactionReceiptDialog(
            transaction = tx,
            dateFormat = dateFormat,
            onDismiss = { selectedTxForReceipt = null },
            onCopyRef = { ref ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Transaction ID", ref)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied ID: $ref", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- WITHDRAW FUNDS MODAL DIALOG ---
    if (showWithdrawModal) {
        WithdrawFundsDialog(
            availableBalance = state.walletBalance,
            onDismiss = { showWithdrawModal = false },
            onConfirmWithdraw = { amt, method, details ->
                val success = onWithdrawFunds(amt, method, details)
                if (success) {
                    showWithdrawModal = false
                    Toast.makeText(context, "Withdrawal request of ₹$amt submitted!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Insufficient balance or invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun TransactionItemCard(
    transaction: WalletTransaction,
    dateFormat: SimpleDateFormat,
    onCopyRef: (String) -> Unit,
    onViewReceipt: () -> Unit
) {
    val isCredit = transaction.type == TransactionType.DEPOSIT || transaction.type == TransactionType.PRIZE_WIN
    val icon = when (transaction.type) {
        TransactionType.DEPOSIT -> Icons.Default.ArrowDownward
        TransactionType.WITHDRAWAL -> Icons.Default.ArrowUpward
        TransactionType.TICKET_PURCHASE -> Icons.Default.ConfirmationNumber
        TransactionType.PRIZE_WIN -> Icons.Default.EmojiEvents
        TransactionType.ALL -> Icons.Default.Payments
    }
    val iconTint = when (transaction.type) {
        TransactionType.DEPOSIT -> EmeraldGreen
        TransactionType.WITHDRAWAL -> Color(0xFFE53935)
        TransactionType.TICKET_PURCHASE -> SleekPurple
        TransactionType.PRIZE_WIN -> Color(0xFFD48800)
        TransactionType.ALL -> Color.Gray
    }
    val iconBg = iconTint.copy(alpha = 0.12f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() }
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEFE8DA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon & Title/Type
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = iconBg,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = transaction.type.name,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = transaction.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateFormat.format(Date(transaction.timestampMs)),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Amount & Sign
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isCredit) "+₹${transaction.amount}" else "-₹${transaction.amount}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCredit) EmeraldGreen else Color(0xFF2E2E2E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (transaction.status) {
                            TransactionStatus.SUCCESS -> EmeraldGreen.copy(alpha = 0.15f)
                            TransactionStatus.PROCESSING -> AmberGold.copy(alpha = 0.2f)
                            TransactionStatus.FAILED -> Color.Red.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = transaction.status.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (transaction.status) {
                                TransactionStatus.SUCCESS -> EmeraldGreen
                                TransactionStatus.PROCESSING -> Color(0xFFD48800)
                                TransactionStatus.FAILED -> Color.Red
                            },
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFFF7F5F0))

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-details footer: Ref ID, Closing Bal, Receipt Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCopyRef(transaction.referenceId) }
                ) {
                    Text(
                        text = "Ref: ${transaction.referenceId}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Ref",
                        tint = Color.Gray,
                        modifier = Modifier.size(11.dp)
                    )
                }

                if (transaction.closingBalance > 0) {
                    Text(
                        text = "Bal: ₹${transaction.closingBalance}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B6B6B)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionReceiptDialog(
    transaction: WalletTransaction,
    dateFormat: SimpleDateFormat,
    onDismiss: () -> Unit,
    onCopyRef: (String) -> Unit
) {
    val isCredit = transaction.type == TransactionType.DEPOSIT || transaction.type == TransactionType.PRIZE_WIN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Receipt",
                    tint = RoyalPurple,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transaction Receipt",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Amount Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                listOf(RoyalPurple.copy(alpha = 0.08f), AmberGold.copy(alpha = 0.1f))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isCredit) "+₹${transaction.amount}" else "-₹${transaction.amount}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCredit) EmeraldGreen else Color(0xFF1F1F1F)
                        )
                        Text(
                            text = transaction.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                ReceiptRow(label = "Transaction ID", value = transaction.id)
                ReceiptRow(label = "Reference ID", value = transaction.referenceId, canCopy = true, onCopy = { onCopyRef(transaction.referenceId) })
                ReceiptRow(label = "Payment Method", value = transaction.paymentMethod)
                ReceiptRow(label = "Status", value = transaction.status.name)
                ReceiptRow(label = "Timestamp", value = dateFormat.format(Date(transaction.timestampMs)))
                if (transaction.closingBalance > 0) {
                    ReceiptRow(label = "Closing Balance", value = "₹${transaction.closingBalance}")
                }
                if (transaction.roomTitle.isNotBlank()) {
                    ReceiptRow(label = "Game Room", value = transaction.roomTitle)
                }
                if (transaction.description.isNotBlank()) {
                    ReceiptRow(label = "Description", value = transaction.description)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    canCopy: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F1F1F)
            )
            if (canCopy) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onCopy, modifier = Modifier.size(18.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = RoyalPurple,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WithdrawFundsDialog(
    availableBalance: Int,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (amount: Int, payoutMethod: String, payoutDetails: String) -> Unit
) {
    var withdrawAmountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("UPI (Instant)") }
    var upiOrAccountInput by remember { mutableStateOf("") }

    val presetWithdrawalAmounts = listOf(100, 200, 500, 1000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowOutward,
                    contentDescription = "Withdraw",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Withdraw to Bank / UPI",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Available Balance: ₹$availableBalance",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                // Amount text field
                OutlinedTextField(
                    value = withdrawAmountText,
                    onValueChange = { withdrawAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Enter Amount (₹)") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input")
                )

                // Quick preset amounts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetWithdrawalAmounts.forEach { amt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (withdrawAmountText == amt.toString()) AmberGold else Color(0xFFF2EFE9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (amt <= availableBalance) {
                                        withdrawAmountText = amt.toString()
                                    }
                                }
                        ) {
                            Text(
                                text = "₹$amt",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Payout Details
                OutlinedTextField(
                    value = upiOrAccountInput,
                    onValueChange = { upiOrAccountInput = it },
                    label = { Text("Enter UPI ID / Bank A/C") },
                    placeholder = { Text("user@okhdfcbank or 1234567890") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_upi_input")
                )

                Text(
                    text = "⚡ Payouts are processed instantly with 0% gateway commission.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = withdrawAmountText.toIntOrNull() ?: 0
                    if (amt > 0 && amt <= availableBalance) {
                        onConfirmWithdraw(amt, selectedMethod, upiOrAccountInput)
                    }
                },
                enabled = (withdrawAmountText.toIntOrNull() ?: 0) in 1..availableBalance,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_withdrawal_btn")
            ) {
                Text("Confirm Payout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

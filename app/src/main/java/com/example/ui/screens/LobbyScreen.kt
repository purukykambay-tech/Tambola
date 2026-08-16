package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameRoom
import com.example.ui.components.UserWalletCard
import com.example.ui.components.formatScheduledDateTime
import com.example.ui.components.getRelativeRemainingTime
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.viewmodel.ActiveTab
import com.example.viewmodel.TambolaUiState

@Composable
fun LobbyScreen(
    state: TambolaUiState,
    onOpenAuthModal: () -> Unit,
    onOpenRazorpayModal: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onJoinRoom: (GameRoom) -> Unit,
    onNavigateToTab: (ActiveTab) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenBuyTicketsModal: (GameRoom?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filterCategories = listOf("All", "Public", "Quick 90", "High Roller")

    var selectedRoomForBooking by remember { mutableStateOf<GameRoom?>(null) }
    var selectedTicketCount by remember { mutableIntStateOf(1) }
    var showRulesDialog by remember { mutableStateOf(false) }

    val displayedRooms = if (state.selectedCategory == "All") {
        state.activeRooms
    } else {
        state.activeRooms.filter { it.category.equals(state.selectedCategory, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF9F6)) // Light warm canvas
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Top Profile Header & Wallet Pill ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Left
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onNavigateToTab(ActiveTab.PROFILE) }
                            .testTag("lobby_profile_header_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF0D4),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        ) {
                            if (!state.userProfile.avatarUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(state.userProfile.avatarUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = state.userProfile.avatarPreset.ifBlank { "👑" },
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (state.isLoggedIn) state.userMobileNumber.ifBlank { "Verified Player" } else "Tap for Profile",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = state.userProfile.nickname.ifBlank { state.userName },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    // Wallet & Settings Row Right
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFFFF6E5),
                            modifier = Modifier
                                .border(1.dp, Color(0xFFF2D399), RoundedCornerShape(24.dp))
                                .clickable { onOpenRazorpayModal() }
                                .testTag("lobby_wallet_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = Color(0xFFD48800),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${state.walletBalance}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2A2A2A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE66700),
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Settings Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onOpenSettings() }
                                .testTag("lobby_settings_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. USER WALLET WITH FIRESTORE SYNC & MOCK UPI PAYMENT TRIGGER ---
            item {
                UserWalletCard(
                    walletBalance = state.walletBalance,
                    userPhone = state.userMobileNumber.ifBlank { "+91 98765 43210" },
                    isLoggedIn = state.isLoggedIn,
                    recentTransactions = state.razorpayTransactions,
                    onAddFundsClick = onOpenRazorpayModal
                )
            }

            // --- 3. ADMIN SUPPORT CONTACT BANNER ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RoyalPurple.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = "Admin Support",
                                        tint = RoyalPurple,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Admin Support Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalPurple)
                                Text(state.adminSupportPhone, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${state.adminSupportPhone.replace(" ", "")}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Admin Contact: ${state.adminSupportPhone}", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Admin", fontSize = 11.sp)
                        }
                    }
                }
            }

            // --- 3. Hero Live Tournament Banner ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_live_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A0A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF2A0808),
                                        Color(0xFF120303),
                                        Color(0xFF3B0000)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFF6D00)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔥", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE TOURNAMENT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Title & Subtitle
                            Text(
                                text = "Mega Amber Jackpot",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Win up to ₹25,000 in Full House claims!",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Button
                            Button(
                                onClick = {
                                    state.activeRooms.firstOrNull()?.let { selectedRoomForBooking = it }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE66700),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("join_live_room_btn")
                            ) {
                                Text(
                                    text = "Book Ticket & Play",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. Category Filter Chips ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterCategories) { category ->
                        val isSelected = state.selectedCategory.equals(category, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFFE66700) else Color.White,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFE5E5E5),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onSelectCategory(category) }
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF4A4A4A),
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // --- 5. Active Game Rooms Header ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Game Rooms",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = "${displayedRooms.size} rooms",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- 6. Active Game Room Cards List ---
            items(displayedRooms, key = { it.id }) { room ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_card_${room.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Top Tags Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF5F3EF)
                                ) {
                                    Text(
                                        text = room.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                if (!room.isUnlimitedPlayers && room.ticketSlots.isNotEmpty()) {
                                    val bookedCount = room.ticketSlots.count { it.isBooked }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SleekPurple.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekPurple.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "🔒 $bookedCount/${room.ticketSlots.size} Slots Booked",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SleekPurple,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                } else if (room.isUnlimitedPlayers) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = RoyalPurple.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = "🌐 Unlimited",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalPurple,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            if (room.scheduledStartTimeMs != null && room.scheduledStartTimeMs > System.currentTimeMillis()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = RoyalPurple.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = RoyalPurple,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = room.scheduledTimeString.ifBlank { formatScheduledDateTime(room.scheduledStartTimeMs) },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = RoyalPurple
                                        )
                                    }
                                }
                            } else if (room.isLive) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("●", fontSize = 10.sp, color = EmeraldGreen)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LIVE NOW",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title & Host
                        Text(
                            text = "${room.iconEmoji} ${room.title}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hosted by ${room.hostName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            if (room.scheduledStartTimeMs != null && room.scheduledStartTimeMs > System.currentTimeMillis()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Starts in ${getRelativeRemainingTime(room.scheduledStartTimeMs)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE66700)
                                )
                            }
                        }

                        if (room.prizeBreakdown.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(room.prizeBreakdown.toList().take(4)) { (pName, pVal) ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AmberGold.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "$pName: ₹$pVal",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C5300),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Footer Row (Prize, Entry Fee, Book Button)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Prize",
                                        tint = Color(0xFFD48800),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Prize: ₹${room.prizeAmount}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2A2A2A)
                                    )
                                }
                                Text(
                                    text = "Entry Fee: ₹${room.entryFee}/ticket",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { onOpenBuyTicketsModal(room) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE66700),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("play_room_${room.id}")
                            ) {
                                Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Book & Play",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- 7. Direct Buy Tickets CTA Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
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
                                text = "TICKET BOOKING & DIRECT PURCHASE",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF5A4500)
                            )
                            Text(
                                text = "Buy directly via Google Pay, Online UPI, or Wallet. Funds route directly to Admin Account.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Button(
                            onClick = { onOpenBuyTicketsModal(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("BUY TICKETS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- 8. Official Club Calling & Support Section ---
            item {
                val org = state.adminOrgInfo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = org.organizationName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = RoyalPurple
                                )
                                Text(
                                    text = "Official Club Support & Player Helpline",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = RoyalPurple)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calling and WhatsApp buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Phone call
                            OutlinedButton(
                                onClick = {
                                    val phone = org.supportPhone.ifBlank { "+919876543210" }
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalPurple)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                            }

                            // WhatsApp
                            OutlinedButton(
                                onClick = {
                                    val waNumber = org.supportWhatsapp.ifBlank { org.supportPhone }.replace(Regex("[^0-9]"), "")
                                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waNumber"))
                                    context.startActivity(waIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Address & Email row
                        if (org.address.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(org.address, fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (org.supportEmail.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(org.supportEmail, fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Rules & Regulations Button
                        Button(
                            onClick = { showRulesDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple.copy(alpha = 0.12f), contentColor = RoyalPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VIEW CLUB RULES & REGULATIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- OFFICIAL RULES & REGULATIONS DIALOG ---
        if (showRulesDialog) {
            Dialog(onDismissRequest = { showRulesDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RULES & REGULATIONS",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = RoyalPurple
                            )
                            IconButton(onClick = { showRulesDialog = false }) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Close", modifier = Modifier.size(18.dp))
                            }
                        }

                        Text(
                            text = state.adminOrgInfo.organizationName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9F7FA),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = state.adminOrgInfo.rulesAndRegulations,
                                    fontSize = 12.sp,
                                    color = Color(0xFF333333),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showRulesDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("I UNDERSTAND & AGREE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- BOOK TICKET & PAYMENT DIALOG ---
        selectedRoomForBooking?.let { room ->
            val totalCost = room.entryFee * selectedTicketCount
            val canPayFromWallet = state.walletBalance >= totalCost

            Dialog(onDismissRequest = { selectedRoomForBooking = null }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF0D4),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = "Book Ticket",
                                    tint = Color(0xFFD48800),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = room.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF1F1F1F)
                        )

                        Text(
                            text = "Prize Pool: ₹${room.prizeAmount} | Entry: ₹${room.entryFee}/ticket",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        if (room.scheduledStartTimeMs != null && room.scheduledStartTimeMs > System.currentTimeMillis()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RoyalPurple.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Scheduled: ${room.scheduledTimeString.ifBlank { formatScheduledDateTime(room.scheduledStartTimeMs) }}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalPurple
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Number of Tickets:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 6).forEach { count ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedTicketCount == count) RoyalPurple else Color(0xFFF3EDF7),
                                    modifier = Modifier
                                        .clickable { selectedTicketCount = count }
                                ) {
                                    Text(
                                        text = "$count ${if (count == 1) "Ticket" else "Tickets"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTicketCount == count) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F6))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Wallet Balance:", fontSize = 12.sp, color = Color.Gray)
                                    Text("₹${state.walletBalance}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Ticket Fee:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("₹$totalCost", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = RoyalPurple)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (canPayFromWallet) {
                            Button(
                                onClick = {
                                    selectedRoomForBooking = null
                                    onJoinRoom(room)
                                    Toast.makeText(context, "Tickets Booked! Entering Room...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PAY ₹$totalCost FROM WALLET & PLAY", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    selectedRoomForBooking = null
                                    onOpenRazorpayModal()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66700)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TOP UP WALLET VIA RAZORPAY", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(onClick = { selectedRoomForBooking = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


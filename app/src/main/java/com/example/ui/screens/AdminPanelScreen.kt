package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameRoom
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.viewmodel.TambolaUiState

@Composable
fun AdminPanelScreen(
    state: TambolaUiState,
    onLoginAdmin: (adminId: String, pass: String) -> Boolean,
    onLogoutAdmin: () -> Unit,
    onCreateRoom: (title: String, host: String, category: String, prize: Int, entryFee: Int, isJackpot: Boolean) -> Unit,
    onUpdateRoom: (roomId: String, title: String, prize: Int, entryFee: Int, isLive: Boolean) -> Unit,
    onDeleteRoom: (roomId: String) -> Unit,
    onCallNextNumber: () -> Unit,
    onBroadcastMessage: (String) -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Admin Login Local State
    var adminIdInput by remember { mutableStateOf("Admin") }
    var passwordInput by remember { mutableStateOf("udoipurtambola@2026") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Room Creation State
    var roomTitleInput by remember { mutableStateOf("Gold Amber 90 Special") }
    var hostNameInput by remember { mutableStateOf("Admin Master") }
    var selectedCategory by remember { mutableStateOf("Public") }
    var prizeAmountInput by remember { mutableStateOf("15000") }
    var entryFeeInput by remember { mutableStateOf("30") }
    var isJackpot by remember { mutableStateOf(true) }

    // Room Editing Dialog/Inline State
    var editingRoomId by remember { mutableStateOf<String?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editPrize by remember { mutableStateOf("") }
    var editFee by remember { mutableStateOf("") }
    var editIsLive by remember { mutableStateOf(true) }

    var broadcastText by remember { mutableStateOf("") }

    // IF NOT AUTHENTICATED: Show Admin Login Gate
    if (!state.isAdminAuthenticated) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF7F5FA))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RoyalPurple,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Security",
                                tint = AmberGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ADMIN PORTAL ACCESS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = RoyalPurple
                    )

                    Text(
                        text = "Authenticate to edit game rooms, ticket rules & payments",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Admin ID Input
                    OutlinedTextField(
                        value = adminIdInput,
                        onValueChange = { adminIdInput = it },
                        label = { Text("Admin ID") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_id_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    state.adminAuthError?.let { err ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val success = onLoginAdmin(adminIdInput, passwordInput)
                            if (success) {
                                Toast.makeText(context, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalPurple,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_btn"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("LOGIN TO ADMIN PANEL", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
        return
    }

    // IF AUTHENTICATED: Show Full Admin Panel
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F6))
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Authenticated Header Banner
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
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = AmberGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ADMIN CONTROL PORTAL",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Logged in as: Admin",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberGold
                            )
                        }
                    }

                    Button(
                        onClick = onLogoutAdmin,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontSize = 12.sp)
                    }
                }
            }
        }

        // Key Financial Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("₹${state.totalRevenueCollected}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Text("Razorpay Total", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${state.activeRooms.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                        Text("Active Rooms", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${state.calledNumbers.size}/90", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        Text("Drawn Count", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Create New Game Room Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CREATE NEW GAME ROOM",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoyalPurple
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomTitleInput,
                        onValueChange = { roomTitleInput = it },
                        label = { Text("Room Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_room_title_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = prizeAmountInput,
                            onValueChange = { prizeAmountInput = it },
                            label = { Text("Prize (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = entryFeeInput,
                            onValueChange = { entryFeeInput = it },
                            label = { Text("Entry Fee (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Category: ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        listOf("Public", "Quick 90", "High Roller").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mark as Jackpot Match 👑", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = isJackpot, onCheckedChange = { isJackpot = it })
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (roomTitleInput.isBlank()) return@Button
                            val prize = prizeAmountInput.toIntOrNull() ?: 10000
                            val fee = entryFeeInput.toIntOrNull() ?: 0
                            onCreateRoom(roomTitleInput, hostNameInput, selectedCategory, prize, fee, isJackpot)
                            Toast.makeText(context, "Room '$roomTitleInput' created!", Toast.LENGTH_SHORT).show()
                            roomTitleInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_create_room_btn"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PUBLISH MATCH TO LOBBY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Room Remote Control & Broadcast
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE MATCH REMOTE CONTROL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoyalPurple
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCallNextNumber,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_force_call_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FORCE DRAW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onResetGame,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_reset_room_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESET BOARD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = broadcastText,
                        onValueChange = { broadcastText = it },
                        label = { Text("Broadcast System Announcement") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (broadcastText.isNotBlank()) {
                                onBroadcastMessage("📢 ADMIN ANNOUNCEMENT: $broadcastText")
                                Toast.makeText(context, "Announcement sent to room chat!", Toast.LENGTH_SHORT).show()
                                broadcastText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BROADCAST TO ALL PLAYERS")
                    }
                }
            }
        }

        // Manage & Edit Existing Rooms List
        item {
            Text(
                text = "MANAGE EXISTING ROOMS (${state.activeRooms.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = RoyalPurple
            )
        }

        items(state.activeRooms, key = { it.id }) { room ->
            val isEditingThis = editingRoomId == room.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_manage_room_${room.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!isEditingThis) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${room.iconEmoji} ${room.title}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Prize: ₹${room.prizeAmount} | Fee: ₹${room.entryFee} | Status: ${if (room.isLive) "LIVE" else "ENDED"}", fontSize = 12.sp, color = Color.Gray)
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        editingRoomId = room.id
                                        editTitle = room.title
                                        editPrize = room.prizeAmount.toString()
                                        editFee = room.entryFee.toString()
                                        editIsLive = room.isLive
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Room", tint = RoyalPurple)
                                }

                                IconButton(
                                    onClick = {
                                        onDeleteRoom(room.id)
                                        Toast.makeText(context, "Deleted room ${room.title}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Room", tint = Color(0xFFB3261E))
                                }
                            }
                        }
                    } else {
                        // Inline Room Edit Form
                        Text("EDIT ROOM: ${room.title}", fontWeight = FontWeight.Bold, color = RoyalPurple)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editPrize,
                                onValueChange = { editPrize = it },
                                label = { Text("Prize (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editFee,
                                onValueChange = { editFee = it },
                                label = { Text("Fee (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Status", fontSize = 13.sp)
                            Switch(checked = editIsLive, onCheckedChange = { editIsLive = it })
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { editingRoomId = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val prize = editPrize.toIntOrNull() ?: room.prizeAmount
                                    val fee = editFee.toIntOrNull() ?: room.entryFee
                                    onUpdateRoom(room.id, editTitle, prize, fee, editIsLive)
                                    editingRoomId = null
                                    Toast.makeText(context, "Saved changes!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Text("SAVE CHANGES")
                            }
                        }
                    }
                }
            }
        }

        // Razorpay Payment Log Audit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RAZORPAY PAYMENT LOGS AUDIT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoyalPurple
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.razorpayTransactions.isEmpty()) {
                        Text("No Razorpay transactions recorded yet.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        state.razorpayTransactions.take(5).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tx.paymentId, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(tx.paymentMethod, fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("+₹${tx.amount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

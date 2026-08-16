package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import com.example.ui.theme.CoralRed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.viewmodel.TambolaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    state: TambolaUiState,
    onUpdateNickname: (String) -> Unit,
    onUpdateAvatarUri: (String?) -> Unit,
    onUpdateAvatarPreset: (String) -> Unit,
    onUpdateBio: (String) -> Unit,
    onOpenAuthModal: () -> Unit,
    onOpenRazorpayModal: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogoutUser: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile = state.userProfile

    // Editing State for Nickname
    var isEditingNickname by remember { mutableStateOf(false) }
    var nicknameInput by remember(profile.nickname) { mutableStateOf(profile.nickname) }

    // Editing State for Bio
    var isEditingBio by remember { mutableStateOf(false) }
    var bioInput by remember(profile.bio) { mutableStateOf(profile.bio) }

    // Dialog/Picker toggle for Avatar Presets
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Photo Picker Launcher for Custom Avatar Photo Upload
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateAvatarUri(uri.toString())
            Toast.makeText(context, "Avatar photo updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    val presetAvatars = listOf(
        "👑", "⚡", "🎯", "💎", "🎲", "🐯", "🦁", "🚀", "🌟", "🔥", "🍀", "🧙‍♂️", "🦹‍♀️", "🦊", "🏆"
    )

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.filled.Logout,
                        contentDescription = null,
                        tint = CoralRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out from Account", fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of ${if (state.isLoggedIn) state.userMobileNumber else "your account"} on this device? Your stats and wallet balance remain securely saved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A4A4A)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutUser()
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    modifier = Modifier.testTag("confirm_logout_btn")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFFAF9F6))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOP NAVIGATION HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F1F1F)
                        )
                    }

                    Text(
                        text = "Player Profile & Stats",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1F1F1F)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Wallet pill shortcut
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AmberGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onOpenRazorpayModal() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = Color(0xFFD48800),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "₹${state.walletBalance}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD48800)
                                )
                            }
                        }

                        if (state.isLoggedIn) {
                            Surface(
                                shape = CircleShape,
                                color = CoralRed.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { showLogoutDialog = true }
                                    .testTag("profile_top_logout_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.filled.Logout,
                                        contentDescription = "Log Out",
                                        tint = CoralRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- HERO CARD: AVATAR, NICKNAME & BADGE ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_hero_card"),
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
                                        Color(0xFF330959),
                                        Color(0xFF18002E)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Frame with Photo Upload Overlay
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.size(108.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmberGold.copy(alpha = 0.2f),
                                    border = BorderStroke(3.dp, AmberGold),
                                    modifier = Modifier
                                        .size(104.dp)
                                        .clip(CircleShape)
                                        .clickable { showAvatarPicker = !showAvatarPicker }
                                        .testTag("profile_avatar_view")
                                ) {
                                    if (!profile.avatarUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(profile.avatarUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Avatar Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(AmberGold.copy(alpha = 0.4f), Color.Transparent)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = profile.avatarPreset,
                                                fontSize = 46.sp
                                            )
                                        }
                                    }
                                }

                                // Upload / Change Camera Badge
                                Surface(
                                    shape = CircleShape,
                                    color = AmberGold,
                                    border = BorderStroke(2.dp, Color(0xFF18002E)),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            photoPickerLauncher.launch("image/*")
                                        }
                                        .testTag("profile_avatar_upload_badge")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Upload Photo",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Nickname View / Edit Field
                            if (isEditingNickname) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    OutlinedTextField(
                                        value = nicknameInput,
                                        onValueChange = { nicknameInput = it },
                                        singleLine = true,
                                        placeholder = { Text("Enter nickname", color = Color.White.copy(alpha = 0.5f)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                            cursorColor = AmberGold
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("profile_nickname_input")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (nicknameInput.isNotBlank()) {
                                                onUpdateNickname(nicknameInput)
                                                isEditingNickname = false
                                                Toast.makeText(context, "Nickname updated!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .background(AmberGold, CircleShape)
                                            .size(40.dp)
                                            .testTag("profile_save_nickname_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save Nickname",
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            nicknameInput = profile.nickname
                                            isEditingNickname = false
                                        },
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = profile.nickname,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { isEditingNickname = true },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("profile_edit_nickname_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Nickname",
                                            tint = AmberGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Phone & Rank Status Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (state.isLoggedIn) EmeraldGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, if (state.isLoggedIn) EmeraldGreen else Color.White.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (state.isLoggedIn) Icons.Default.Verified else Icons.Default.Person,
                                            contentDescription = "Status",
                                            tint = if (state.isLoggedIn) EmeraldGreen else Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (state.isLoggedIn) state.userMobileNumber.ifBlank { "Verified Player" } else "Guest Account",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (state.isLoggedIn) EmeraldGreen else Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AmberGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "VIP Level ${profile.gamesWon / 5 + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Player Bio
                            if (isEditingBio) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                ) {
                                    OutlinedTextField(
                                        value = bioInput,
                                        onValueChange = { bioInput = it },
                                        placeholder = { Text("Enter personal bio", color = Color.White.copy(alpha = 0.5f)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AmberGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            cursorColor = AmberGold
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            onUpdateBio(bioInput)
                                            isEditingBio = false
                                        },
                                        modifier = Modifier
                                            .background(AmberGold, CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save Bio",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.clickable { isEditingBio = true }
                                ) {
                                    Text(
                                        text = profile.bio,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Bio",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- AVATAR PICKER DRAWER / PRESET BAR ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Avatar Customization",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )

                            if (!profile.avatarUri.isNullOrBlank()) {
                                TextButton(
                                    onClick = { onUpdateAvatarUri(null) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Reset to Preset", fontSize = 11.sp, color = Color.Red)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Photo Upload Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_gallery_photo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showAvatarPicker = !showAvatarPicker },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Preset",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFD48800)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (showAvatarPicker) "Hide Styles" else "Pick Icon Style", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Preset Emojis Row
                        AnimatedVisibility(visible = showAvatarPicker) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = "Select an Avatar Style Icon:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(presetAvatars) { emoji ->
                                        val isSelected = profile.avatarUri.isNullOrBlank() && profile.avatarPreset == emoji
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) AmberGold.copy(alpha = 0.25f) else Color(0xFFF7F5F0),
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) AmberGold else Color(0xFFE0DDD5)
                                            ),
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clickable {
                                                    onUpdateAvatarPreset(emoji)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = emoji, fontSize = 24.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 4 SUMMARY METRIC CARDS ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Individual Winning Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Earnings
                        ProfileMetricCard(
                            title = "TOTAL WON",
                            value = "₹${profile.totalEarnings}",
                            subtitle = "${profile.totalPoints} pts earned",
                            icon = Icons.Default.Savings,
                            iconTint = AmberGold,
                            backgroundBrush = Brush.linearGradient(
                                listOf(Color(0xFFFFF9E6), Color(0xFFFFF0C7))
                            ),
                            borderColor = Color(0xFFF0D69A),
                            modifier = Modifier.weight(1f).testTag("stat_total_earnings")
                        )

                        // Matches Won & Win Rate
                        ProfileMetricCard(
                            title = "MATCHES WON",
                            value = "${profile.gamesWon} / ${profile.gamesPlayed}",
                            subtitle = "${profile.winRatePercent}% Win Rate",
                            icon = Icons.Default.EmojiEvents,
                            iconTint = EmeraldGreen,
                            backgroundBrush = Brush.linearGradient(
                                listOf(Color(0xFFEBF9EE), Color(0xFFD4F5DA))
                            ),
                            borderColor = Color(0xFFAFE0B9),
                            modifier = Modifier.weight(1f).testTag("stat_matches_won")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Claims Won
                        ProfileMetricCard(
                            title = "CLAIMS CLAIMED",
                            value = "${profile.totalClaimsWon}",
                            subtitle = "Across all game patterns",
                            icon = Icons.Default.MilitaryTech,
                            iconTint = RoyalPurple,
                            backgroundBrush = Brush.linearGradient(
                                listOf(Color(0xFFF6F0FF), Color(0xFFE9DCFD))
                            ),
                            borderColor = Color(0xFFD3BAFA),
                            modifier = Modifier.weight(1f).testTag("stat_total_claims")
                        )

                        // Full House Count
                        ProfileMetricCard(
                            title = "FULL HOUSES",
                            value = "${profile.fullHouseWins} 🏆",
                            subtitle = "Grand Jackpots",
                            icon = Icons.Default.Star,
                            iconTint = Color(0xFFE65100),
                            backgroundBrush = Brush.linearGradient(
                                listOf(Color(0xFFFFF0EB), Color(0xFFFFDED4))
                            ),
                            borderColor = Color(0xFFF9B8A4),
                            modifier = Modifier.weight(1f).testTag("stat_full_houses")
                        )
                    }
                }
            }

            // --- WIN RATE PROGRESS BAR ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Win Rate",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Win Probability & Performance",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                            Text(
                                text = "${profile.winRatePercent}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (profile.winRatePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldGreen,
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Played: ${profile.gamesPlayed} Matches",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Won: ${profile.gamesWon} Victories",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            }

            // --- CLAIMS BREAKDOWN SECTION ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_claims_breakdown_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Winning Patterns Breakdown",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Text(
                            text = "Detailed statistics per claim type won in live matches",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ClaimStatRow(
                            iconEmoji = "⚡",
                            title = "Jaldi 5 (Early 5)",
                            desc = "First 5 numbers marked",
                            winCount = profile.earlyFiveWins,
                            prizePoints = profile.earlyFiveWins * 50
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0EBE0))

                        ClaimStatRow(
                            iconEmoji = "📐",
                            title = "Four Corners",
                            desc = "All 4 corner numbers verified",
                            winCount = profile.cornersWins,
                            prizePoints = profile.cornersWins * 60
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0EBE0))

                        ClaimStatRow(
                            iconEmoji = "⬆️",
                            title = "Top Line",
                            desc = "1st row 5 numbers marked",
                            winCount = profile.topLineWins,
                            prizePoints = profile.topLineWins * 100
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0EBE0))

                        ClaimStatRow(
                            iconEmoji = "➡️",
                            title = "Middle Line",
                            desc = "2nd row 5 numbers marked",
                            winCount = profile.middleLineWins,
                            prizePoints = profile.middleLineWins * 100
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0EBE0))

                        ClaimStatRow(
                            iconEmoji = "⬇️",
                            title = "Bottom Line",
                            desc = "3rd row 5 numbers marked",
                            winCount = profile.bottomLineWins,
                            prizePoints = profile.bottomLineWins * 100
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0EBE0))

                        ClaimStatRow(
                            iconEmoji = "👑",
                            title = "Full House (1st / 2nd)",
                            desc = "All 15 numbers completely dabbed",
                            winCount = profile.fullHouseWins,
                            prizePoints = profile.fullHouseWins * 300,
                            isGrandPrize = true
                        )
                    }
                }
            }

            // --- ACHIEVEMENTS & TROPHIES ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Milestones & Trophy Badges",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AchievementBadge(
                                title = "First Victory",
                                icon = "🌟",
                                isUnlocked = profile.gamesWon >= 1,
                                description = "Win at least 1 match"
                            )
                            AchievementBadge(
                                title = "Full House King",
                                icon = "👑",
                                isUnlocked = profile.fullHouseWins >= 1,
                                description = "Claim 1+ Full House"
                            )
                            AchievementBadge(
                                title = "Speedster",
                                icon = "⚡",
                                isUnlocked = profile.earlyFiveWins >= 2,
                                description = "2+ Early 5 claims"
                            )
                            AchievementBadge(
                                title = "Century Club",
                                icon = "💯",
                                isUnlocked = profile.totalPoints >= 1000,
                                description = "Earn 1000+ points"
                            )
                            AchievementBadge(
                                title = "High Roller",
                                icon = "💎",
                                isUnlocked = profile.totalEarnings >= 3000,
                                description = "Win ₹3,000+ total"
                            )
                            AchievementBadge(
                                title = "Grand Veteran",
                                icon = "🎯",
                                isUnlocked = profile.gamesPlayed >= 10,
                                description = "Play 10+ games"
                            )
                        }
                    }
                }
            }

            // --- RECENT VERIFIED CLAIMS & MATCH TIMELINE ---
            item {
                val userHistory = state.pastWinnersHistory.filter {
                    it.winnerName.contains("You", ignoreCase = true) ||
                    (state.userMobileNumber.isNotBlank() && it.winnerPhone == state.userMobileNumber)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = SleekPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recent Verified Wins Log",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                            Text(
                                text = "${userHistory.size} Recorded",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (userHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFAF9F6), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎯", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No live claims verified yet in this session.",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Join a live room to claim Early 5, Lines, or Full House!",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            userHistory.take(5).forEachIndexed { idx, win ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${win.claimPattern} (${win.roomTitle})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F1F1F)
                                        )
                                        Text(
                                            text = "On Call #${win.totalNumbersCalled} • ${dateFormat.format(Date(win.verifiedTimestamp))}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldGreen.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "+₹${win.prizeAmount}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (idx < userHistory.take(5).size - 1) {
                                    HorizontalDivider(color = Color(0xFFF0EBE0), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- ACCOUNT ACTIONS & LOGIN / LOGOUT PROMPT ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFE8DA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Account & Session Management",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )

                        if (state.isLoggedIn) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF7F5FA),
                                border = BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldGreen.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Verified Mobile Account",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = state.userMobileNumber,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F1F1F)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldGreen.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = onOpenAuthModal,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalPurple,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_login_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Login",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Login / Register via Mobile OTP", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenRazorpayModal,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Deposit",
                                tint = Color(0xFFD48800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit Funds via UPI (Razorpay)", color = Color(0xFFD48800), fontWeight = FontWeight.Bold)
                        }

                        if (state.isLoggedIn) {
                            Button(
                                onClick = { showLogoutDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralRed.copy(alpha = 0.12f),
                                    contentColor = CoralRed
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_logout_bottom_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.filled.Logout,
                                    contentDescription = "Log Out",
                                    tint = CoralRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Out from Account", fontWeight = FontWeight.Bold, color = CoralRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundBrush: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1F1F1F)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ClaimStatRow(
    iconEmoji: String,
    title: String,
    desc: String,
    winCount: Int,
    prizePoints: Int,
    isGrandPrize: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isGrandPrize) AmberGold.copy(alpha = 0.2f) else Color(0xFFF5F3ED),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = iconEmoji, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isGrandPrize) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isGrandPrize) Color(0xFFB57000) else Color(0xFF1F1F1F)
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$winCount wins",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (winCount > 0) EmeraldGreen else Color.Gray
            )
            Text(
                text = "+$prizePoints pts",
                fontSize = 10.sp,
                color = if (prizePoints > 0) Color(0xFFB57000) else Color.Gray
            )
        }
    }
}

@Composable
private fun AchievementBadge(
    title: String,
    icon: String,
    isUnlocked: Boolean,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) Color(0xFFFFF9E6) else Color(0xFFF7F5F2),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) AmberGold.copy(alpha = 0.6f) else Color(0xFFE2DFD8)
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color(0xFF1F1F1F) else Color.Gray
                    )
                    if (!isUnlocked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = description,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

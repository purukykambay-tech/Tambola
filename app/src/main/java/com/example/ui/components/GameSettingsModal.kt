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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.CoralRed
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple

@Composable
fun GameSettingsModal(
    isVisible: Boolean,
    isAutoDabEnabled: Boolean,
    isAutoCalling: Boolean,
    isSoundEnabled: Boolean,
    autoCallIntervalSec: Int,
    onToggleAutoDab: () -> Unit,
    onToggleAutoCalling: () -> Unit,
    onToggleSound: () -> Unit,
    onSetIntervalSec: (Int) -> Unit,
    onDismiss: () -> Unit,
    isLoggedIn: Boolean = false,
    userPhone: String = "",
    onLogoutUser: () -> Unit = {}
) {
    if (!isVisible) return

    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.filled.Logout,
                        contentDescription = null,
                        tint = CoralRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out from Device", fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
                }
            },
            text = {
                Text(
                    text = "Do you want to log out from this session? Your game data and wallet balance will remain safe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A4A4A)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogoutUser()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    modifier = Modifier.testTag("settings_confirm_logout_btn")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("game_settings_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = RoyalPurple.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = RoyalPurple
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Game & Auto-Play Settings",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customize speed, auto-play & audio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_btn")) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. AUTOMATIC PLAY TOGGLE
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isAutoCalling) RoyalPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isAutoCalling) RoyalPurple.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleAutoCalling() }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAutoCalling) RoyalPurple else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Auto Call",
                                        tint = if (isAutoCalling) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Automatic Game Play",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isAutoCalling) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = RoyalPurple
                                        ) {
                                            Text(
                                                text = "RUNNING (${autoCallIntervalSec}s)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Continuously draws and announces next numbers every $autoCallIntervalSec seconds automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Switch(
                            checked = isAutoCalling,
                            onCheckedChange = { onToggleAutoCalling() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RoyalPurple
                            ),
                            modifier = Modifier.testTag("settings_auto_play_switch")
                        )
                    }
                }

                // 2. TIME SETTING & SPEED SELECTION
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Call Interval (Time Setting)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekPurple.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${autoCallIntervalSec}s / number",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Presets Row 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                2 to "2s Blitz",
                                3 to "3s Fast",
                                4 to "4s Normal"
                            ).forEach { (sec, label) ->
                                val isSelected = autoCallIntervalSec == sec
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
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Presets Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                5 to "5s Balanced",
                                6 to "6s Relaxed",
                                8 to "8s Slow"
                            ).forEach { (sec, label) ->
                                val isSelected = autoCallIntervalSec == sec
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
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 3. AUTO-DAB SETTING
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isAutoDabEnabled) EmeraldGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isAutoDabEnabled) EmeraldGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleAutoDab() }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAutoDabEnabled) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Auto Dab",
                                        tint = if (isAutoDabEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Auto-Dab Matching Numbers",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isAutoDabEnabled) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Automatically strikes out matching numbers on all your tickets in real time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Switch(
                            checked = isAutoDabEnabled,
                            onCheckedChange = { onToggleAutoDab() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldGreen
                            ),
                            modifier = Modifier.testTag("settings_auto_dab_switch")
                        )
                    }
                }

                // 4. SOUND EFFECTS & VOICE
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSound() }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSoundEnabled) AmberGold else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Sound",
                                        tint = if (isSoundEnabled) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sound & Caller Voice",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Synthesized number call announcements, ticket pop sounds, and winning fanfare.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AmberGold
                            ),
                            modifier = Modifier.testTag("settings_sound_switch")
                        )
                    }
                }

                // 5. ACCOUNT & LOGOUT SETTING
                if (isLoggedIn) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CoralRed.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CoralRed.copy(alpha = 0.2f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Account",
                                            tint = CoralRed
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Logged In ($userPhone)",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Session active with cloud wallet & stats sync",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { showLogoutConfirm = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("settings_logout_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.filled.Logout,
                                    contentDescription = "Log Out",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Done Action Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_done_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                ) {
                    Text(
                        text = "SAVE & CLOSE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

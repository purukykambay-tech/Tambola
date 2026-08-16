package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.WinnerNotificationPayload
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.ui.theme.SleekPurpleDark
import kotlinx.coroutines.delay

/**
 * WinnerNotificationToastBanner
 *
 * Real-time FCM Winner alert overlay rendered at the top of the screen whenever
 * a validated win message is dispatched from Firebase Cloud Messaging.
 */
@Composable
fun WinnerNotificationToastBanner(
    payload: WinnerNotificationPayload?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(payload) {
        if (payload != null) {
            delay(5000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = payload != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        payload?.let { data ->
            val isUserWin = data.winnerName.contains("You", ignoreCase = true)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("fcm_winner_banner"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SleekPurpleDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isUserWin) {
                                    listOf(SleekPurpleDark, RoyalPurple, EmeraldGreen.copy(alpha = 0.8f))
                                } else {
                                    listOf(SleekPurpleDark, RoyalPurple, SleekPurple)
                                }
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isUserWin) AmberGold else EmeraldGreen,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isUserWin) Icons.Default.EmojiEvents else Icons.Default.NotificationsActive,
                                        contentDescription = "FCM Notification",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = data.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Claim",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = data.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = data.claimType,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AmberGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Prize: ₹${"%,d".format(data.prizeAmount)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * WinnerNotificationPayload
 * Real-time event payload dispatched when a validated winning claim is processed.
 */
data class WinnerNotificationPayload(
    val title: String,
    val message: String,
    val winnerName: String,
    val claimType: String,
    val prizeAmount: Int,
    val matchId: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * TambolaFirebaseMessagingService
 *
 * FCM service listening to incoming push notifications and real-time game win alerts.
 * Dispatches both high-priority Heads-Up Android system notifications with sound/vibration
 * and a reactive Kotlin Flow to trigger in-app UI celebration popups immediately.
 */
class TambolaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token: $token")
        // Store or sync device token with Firestore user profile if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = data["title"] ?: notification?.title ?: "🎉 Congratulations! You Won!"
        val body = data["body"] ?: notification?.body ?: "Your Tambola ticket claim has been validated!"
        val winnerName = data["winnerName"] ?: "You"
        val claimType = data["claimType"] ?: "Winning Claim"
        val prizeAmount = data["prizeAmount"]?.toIntOrNull() ?: 500
        val matchId = data["matchId"] ?: "MATCH_LIVE"

        val payload = WinnerNotificationPayload(
            title = title,
            message = body,
            winnerName = winnerName,
            claimType = claimType,
            prizeAmount = prizeAmount,
            matchId = matchId
        )

        // 1. Emit to in-app reactive Flow for immediate UI feedback (Confetti, Dialogs, Banner)
        _winnerEvents.tryEmit(payload)

        // 2. Post Android system status bar / heads-up notification
        showSystemNotification(payload)
    }

    private fun showSystemNotification(payload: WinnerNotificationPayload) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = CHANNEL_ID_WINNERS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tambola Win Celebrations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts and payout confirmation when your Tambola ticket wins"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_NAVIGATE_TAB", "GAME_HISTORY")
            putExtra("EXTRA_WINNER_PAYLOAD_TITLE", payload.title)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle("🏆 " + payload.title)
            .setContentText(payload.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${payload.message}\nPattern: ${payload.claimType} • Prize: ₹${payload.prizeAmount}"))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "TambolaFCMService"
        const val CHANNEL_ID_WINNERS = "tambola_winner_notification_channel"
        private const val NOTIFICATION_REQUEST_CODE = 3030

        // Shared Flow to distribute winning events to ViewModels and Composables
        private val _winnerEvents = MutableSharedFlow<WinnerNotificationPayload>(extraBufferCapacity = 10)
        val winnerEvents: SharedFlow<WinnerNotificationPayload> = _winnerEvents.asSharedFlow()

        /**
         * Dispatches a local win event (used when the in-app claim verifier instantly verifies a winning ticket).
         */
        fun notifyLocalWin(context: Context, payload: WinnerNotificationPayload) {
            _winnerEvents.tryEmit(payload)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = CHANNEL_ID_WINNERS

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Tambola Win Celebrations",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time alerts and payout confirmation when your Tambola ticket wins"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_NAVIGATE_TAB", "GAME_HISTORY")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.btn_star_big_on)
                .setContentTitle("🏆 " + payload.title)
                .setContentText(payload.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText("${payload.message}\nPattern: ${payload.claimType} • Prize: ₹${payload.prizeAmount} credited to wallet!"))
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}

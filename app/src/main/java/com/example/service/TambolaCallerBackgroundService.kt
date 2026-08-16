package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.CurrentGameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * TambolaCallerBackgroundService
 *
 * A dedicated background service that periodically pulls/draws random numbers
 * from the Firestore 'current_game' collection to simulate the live Tambola caller engine.
 */
class TambolaCallerBackgroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var callerEngineJob: Job? = null
    private var currentIntervalSec = 5

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "TambolaCallerBackgroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val interval = intent.getIntExtra(EXTRA_INTERVAL_SEC, 5)
                startCallerEngine(interval)
            }
            ACTION_STOP -> {
                stopCallerEngine()
                stopSelf()
            }
            ACTION_SET_INTERVAL -> {
                val interval = intent.getIntExtra(EXTRA_INTERVAL_SEC, 5)
                currentIntervalSec = interval
                if (_isServiceRunning.value) {
                    startCallerEngine(interval)
                }
            }
            ACTION_DRAW_ONCE -> {
                serviceScope.launch {
                    val drawn = FirestoreService.drawNextFirestoreGameNumber()
                    Log.d(TAG, "Manual single draw from Firestore: $drawn")
                }
            }
        }
        return START_STICKY
    }

    private fun startCallerEngine(intervalSec: Int) {
        currentIntervalSec = intervalSec
        _isServiceRunning.value = true
        callerEngineJob?.cancel()

        try {
            val notification = buildForegroundNotification("Tambola Live Caller Engine is Active", "Auto-drawing numbers every ${intervalSec}s from Firestore 'current_game'")
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Foreground notification start exception: ${e.localizedMessage}")
        }

        callerEngineJob = serviceScope.launch {
            Log.d(TAG, "Started Firestore 'current_game' background caller loop with interval: ${intervalSec}s")
            
            // Mark game as running in Firestore
            val existing = FirestoreService.pullCurrentGameState() ?: CurrentGameState()
            FirestoreService.updateCurrentGameState(existing.copy(isRunning = true, drawIntervalSec = intervalSec))

            while (isActive && _isServiceRunning.value) {
                try {
                    // Pull & draw the next random number into Firestore 'current_game'
                    val drawn = FirestoreService.drawNextFirestoreGameNumber()
                    if (drawn != null) {
                        Log.d(TAG, "Drawn number $drawn from Firestore 'current_game'")
                        updateNotificationText("Last Called: #$drawn | Live 90-Ball Match")
                    } else {
                        Log.d(TAG, "All 90 numbers called or game completed!")
                        _isServiceRunning.value = false
                        stopCallerEngine()
                        break
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Error in background caller loop", t)
                }

                delay(currentIntervalSec * 1000L)
            }
        }
    }

    private fun stopCallerEngine() {
        _isServiceRunning.value = false
        callerEngineJob?.cancel()
        callerEngineJob = null
        serviceScope.launch {
            val current = FirestoreService.pullCurrentGameState()
            if (current != null) {
                FirestoreService.updateCurrentGameState(current.copy(isRunning = false))
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "Tambola caller background engine stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tambola Live Caller Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service pulling and syncing numbers from Firestore 'current_game'"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotificationText(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(
            NOTIFICATION_ID,
            buildForegroundNotification("Tambola Live Caller Active", content)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        callerEngineJob?.cancel()
        serviceScope.cancel()
        _isServiceRunning.value = false
        Log.d(TAG, "TambolaCallerBackgroundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "TambolaCallerService"
        private const val CHANNEL_ID = "tambola_caller_engine_channel"
        private const val NOTIFICATION_ID = 2026

        const val ACTION_START = "com.example.action.START_CALLER"
        const val ACTION_STOP = "com.example.action.STOP_CALLER"
        const val ACTION_SET_INTERVAL = "com.example.action.SET_INTERVAL"
        const val ACTION_DRAW_ONCE = "com.example.action.DRAW_ONCE"

        const val EXTRA_INTERVAL_SEC = "extra_interval_sec"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun startService(context: Context, intervalSec: Int = 5) {
            val intent = Intent(context, TambolaCallerBackgroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_INTERVAL_SEC, intervalSec)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Starting standard service fallback: ${e.localizedMessage}")
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TambolaCallerBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setInterval(context: Context, intervalSec: Int) {
            val intent = Intent(context, TambolaCallerBackgroundService::class.java).apply {
                action = ACTION_SET_INTERVAL
                putExtra(EXTRA_INTERVAL_SEC, intervalSec)
            }
            context.startService(intent)
        }

        fun drawSingleNumber(context: Context) {
            val intent = Intent(context, TambolaCallerBackgroundService::class.java).apply {
                action = ACTION_DRAW_ONCE
            }
            context.startService(intent)
        }
    }
}

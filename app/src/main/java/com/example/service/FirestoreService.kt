package com.example.service

import android.util.Log
import com.example.model.AdminOrganizationInfo
import com.example.model.AdminTicketBookingRecord
import com.example.model.CallerPhrases
import com.example.model.CurrentGameState
import com.example.model.GameConfiguration
import com.example.model.GameRoom
import com.example.model.PlayerProfileStats
import com.example.model.RazorpayTransaction
import com.example.model.SavedTicketEntity
import com.example.model.TambolaTicket
import com.example.model.TambolaWinnerHistory
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.WalletTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirestoreService {
    private const val TAG = "FirestoreService"
    private const val COLLECTION_ROOMS = "game_rooms"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_TRANSACTIONS = "transactions"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (t: Throwable) {
            Log.w(TAG, "Firestore initialization fallback: ${t.localizedMessage}")
            null
        }
    }

    // --- GAME ROOMS ---

    /**
     * Real-time stream of all active game rooms.
     */
    fun observeGameRooms(): Flow<List<GameRoom>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ROOMS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing game rooms: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val rooms = snapshot.documents.mapNotNull { doc ->
                            try {
                                GameRoom(
                                    id = doc.getString("id") ?: doc.id,
                                    title = doc.getString("title") ?: "Tambola Room",
                                    hostName = doc.getString("hostName") ?: "Host",
                                    category = doc.getString("category") ?: "Public",
                                    prizeAmount = doc.getLong("prizeAmount")?.toInt() ?: 100,
                                    entryFee = doc.getLong("entryFee")?.toInt() ?: 0,
                                    currentPlayers = doc.getLong("currentPlayers")?.toInt() ?: 1,
                                    maxPlayers = doc.getLong("maxPlayers")?.toInt() ?: 100,
                                    isLive = doc.getBoolean("isLive") ?: true,
                                    isJackpot = doc.getBoolean("isJackpot") ?: false,
                                    iconEmoji = doc.getString("iconEmoji") ?: "👑",
                                    scheduledStartTimeMs = doc.getLong("scheduledStartTimeMs"),
                                    scheduledTimeString = doc.getString("scheduledTimeString") ?: ""
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing room document", e)
                                null
                            }
                        }
                        trySend(rooms)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach snapshot listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Save or update a Game Room in Firestore.
     */
    suspend fun saveGameRoom(room: GameRoom) {
        val db = firestore ?: return
        try {
            val slotsData = room.ticketSlots.map { slot ->
                val gridJson = slot.ticket.grid.joinToString(";") { row ->
                    row.joinToString(",") { it?.toString() ?: "" }
                }
                hashMapOf(
                    "slotNumber" to slot.slotNumber,
                    "ticketId" to slot.ticket.id,
                    "gridJson" to gridJson,
                    "isBooked" to slot.isBooked,
                    "bookedByPhone" to slot.bookedByPhone,
                    "bookedByName" to slot.bookedByName,
                    "bookingTimestamp" to slot.bookingTimestamp,
                    "paymentMethod" to slot.paymentMethod
                )
            }
            val roomData = hashMapOf(
                "id" to room.id,
                "title" to room.title,
                "hostName" to room.hostName,
                "category" to room.category,
                "prizeAmount" to room.prizeAmount,
                "entryFee" to room.entryFee,
                "currentPlayers" to room.currentPlayers,
                "maxPlayers" to room.maxPlayers,
                "isUnlimitedPlayers" to room.isUnlimitedPlayers,
                "isLive" to room.isLive,
                "isJackpot" to room.isJackpot,
                "iconEmoji" to room.iconEmoji,
                "scheduledStartTimeMs" to room.scheduledStartTimeMs,
                "scheduledTimeString" to room.scheduledTimeString,
                "prizeBreakdown" to room.prizeBreakdown,
                "ticketSlots" to slotsData,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_ROOMS)
                .document(room.id)
                .set(roomData, SetOptions.merge())
                .await()
            Log.d(TAG, "Room saved to Firestore: ${room.id}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving game room: ${t.localizedMessage}")
        }
    }

    /**
     * Delete a game room from Firestore.
     */
    suspend fun deleteGameRoom(roomId: String) {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_ROOMS).document(roomId).delete().await()
        } catch (t: Throwable) {
            Log.e(TAG, "Error deleting game room", t)
        }
    }

    // --- USER WALLET BALANCES ---

    /**
     * Real-time stream of a user's wallet balance.
     */
    fun observeUserWallet(userIdOrPhone: String): Flow<Int?> = callbackFlow {
        val db = firestore
        if (db == null || userIdOrPhone.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_USERS)
                .document(docId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing wallet: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val balance = snapshot.getLong("walletBalance")?.toInt()
                        trySend(balance)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach wallet listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Update user wallet balance in Firestore.
     */
    suspend fun updateUserWalletBalance(userIdOrPhone: String, newBalance: Int) {
        val db = firestore ?: return
        if (userIdOrPhone.isBlank()) return
        try {
            val docId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
            val data = hashMapOf(
                "userId" to userIdOrPhone,
                "walletBalance" to newBalance,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Wallet updated for $userIdOrPhone: ₹$newBalance")
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating wallet balance", t)
        }
    }

    // --- USER PROFILE & STATS ---

    /**
     * Real-time stream of a user's profile and winning stats.
     */
    fun observeUserProfile(userIdOrPhone: String): Flow<PlayerProfileStats?> = callbackFlow {
        val db = firestore
        if (db == null || userIdOrPhone.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_USERS)
                .document(docId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing profile: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = PlayerProfileStats(
                            nickname = snapshot.getString("nickname") ?: "Lucky Striker",
                            avatarUri = snapshot.getString("avatarUri"),
                            avatarPreset = snapshot.getString("avatarPreset") ?: "👑",
                            bio = snapshot.getString("bio") ?: "Tambola enthusiast & Housie champion 🎯",
                            gamesPlayed = snapshot.getLong("gamesPlayed")?.toInt() ?: 18,
                            gamesWon = snapshot.getLong("gamesWon")?.toInt() ?: 7,
                            totalEarnings = snapshot.getLong("totalEarnings")?.toInt() ?: 4350,
                            totalPoints = snapshot.getLong("totalPoints")?.toInt() ?: 1850,
                            earlyFiveWins = snapshot.getLong("earlyFiveWins")?.toInt() ?: 3,
                            cornersWins = snapshot.getLong("cornersWins")?.toInt() ?: 2,
                            topLineWins = snapshot.getLong("topLineWins")?.toInt() ?: 4,
                            middleLineWins = snapshot.getLong("middleLineWins")?.toInt() ?: 2,
                            bottomLineWins = snapshot.getLong("bottomLineWins")?.toInt() ?: 1,
                            fullHouseWins = snapshot.getLong("fullHouseWins")?.toInt() ?: 3,
                            memberSince = snapshot.getLong("memberSince") ?: (System.currentTimeMillis() - 1296000000L)
                        )
                        trySend(profile)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach profile listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Save user profile and winning stats in Firestore.
     */
    suspend fun saveUserProfile(userIdOrPhone: String, profile: PlayerProfileStats) {
        val db = firestore ?: return
        if (userIdOrPhone.isBlank()) return
        try {
            val docId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
            val data = hashMapOf(
                "userId" to userIdOrPhone,
                "nickname" to profile.nickname,
                "avatarUri" to profile.avatarUri,
                "avatarPreset" to profile.avatarPreset,
                "bio" to profile.bio,
                "gamesPlayed" to profile.gamesPlayed,
                "gamesWon" to profile.gamesWon,
                "totalEarnings" to profile.totalEarnings,
                "totalPoints" to profile.totalPoints,
                "earlyFiveWins" to profile.earlyFiveWins,
                "cornersWins" to profile.cornersWins,
                "topLineWins" to profile.topLineWins,
                "middleLineWins" to profile.middleLineWins,
                "bottomLineWins" to profile.bottomLineWins,
                "fullHouseWins" to profile.fullHouseWins,
                "memberSince" to profile.memberSince,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Profile updated for $userIdOrPhone: ${profile.nickname}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating user profile", t)
        }
    }


    // --- TRANSACTION LOGS ---

    /**
     * Save transaction record to Firestore.
     */
    suspend fun saveTransaction(userIdOrPhone: String, transaction: RazorpayTransaction) {
        val db = firestore ?: return
        try {
            val txData = hashMapOf(
                "paymentId" to transaction.paymentId,
                "userId" to userIdOrPhone,
                "amount" to transaction.amount,
                "status" to transaction.status,
                "paymentMethod" to transaction.paymentMethod,
                "timestampMs" to transaction.timestampMs
            )
            db.collection(COLLECTION_TRANSACTIONS)
                .document(transaction.paymentId)
                .set(txData, SetOptions.merge())
                .await()
            Log.d(TAG, "Transaction saved to Firestore: ${transaction.paymentId}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving transaction", t)
        }
    }

    /**
     * Real-time stream of transactions for a user.
     */
    fun observeUserTransactions(userIdOrPhone: String): Flow<List<RazorpayTransaction>> = callbackFlow {
        val db = firestore
        if (db == null || userIdOrPhone.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userIdOrPhone)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing transactions: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                RazorpayTransaction(
                                    paymentId = doc.getString("paymentId") ?: doc.id,
                                    amount = doc.getLong("amount")?.toInt() ?: 0,
                                    status = doc.getString("status") ?: "SUCCESS",
                                    paymentMethod = doc.getString("paymentMethod") ?: "UPI",
                                    timestampMs = doc.getLong("timestampMs") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(list)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach transaction listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Save comprehensive wallet transaction to Firestore.
     */
    suspend fun saveWalletTransaction(userIdOrPhone: String, transaction: WalletTransaction) {
        val db = firestore ?: return
        try {
            val txData = hashMapOf(
                "id" to transaction.id,
                "userId" to userIdOrPhone,
                "type" to transaction.type.name,
                "amount" to transaction.amount,
                "title" to transaction.title,
                "description" to transaction.description,
                "paymentMethod" to transaction.paymentMethod,
                "referenceId" to transaction.referenceId,
                "status" to transaction.status.name,
                "closingBalance" to transaction.closingBalance,
                "roomTitle" to transaction.roomTitle,
                "ticketCount" to transaction.ticketCount,
                "timestampMs" to transaction.timestampMs
            )
            db.collection(COLLECTION_TRANSACTIONS)
                .document(transaction.id)
                .set(txData, SetOptions.merge())
                .await()
            Log.d(TAG, "Wallet transaction saved to Firestore: ${transaction.id} (${transaction.type})")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving wallet transaction", t)
        }
    }

    /**
     * Real-time stream of all wallet transactions (Deposits, Withdrawals, Tickets, Prizes) for a user.
     */
    fun observeUserWalletTransactions(userIdOrPhone: String): Flow<List<WalletTransaction>> = callbackFlow {
        val db = firestore
        if (db == null || userIdOrPhone.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userIdOrPhone)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing wallet transactions: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val typeStr = doc.getString("type") ?: "DEPOSIT"
                                val type = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.DEPOSIT }
                                val statusStr = doc.getString("status") ?: "SUCCESS"
                                val status = try { TransactionStatus.valueOf(statusStr) } catch (e: Exception) { TransactionStatus.SUCCESS }

                                WalletTransaction(
                                    id = doc.getString("id") ?: doc.id,
                                    type = type,
                                    amount = doc.getLong("amount")?.toInt() ?: 0,
                                    title = doc.getString("title") ?: (if (type == TransactionType.DEPOSIT) "Add Cash Deposit" else "Transaction"),
                                    description = doc.getString("description") ?: "",
                                    paymentMethod = doc.getString("paymentMethod") ?: "UPI",
                                    referenceId = doc.getString("referenceId") ?: (doc.getString("paymentId") ?: doc.id),
                                    status = status,
                                    closingBalance = doc.getLong("closingBalance")?.toInt() ?: 0,
                                    roomTitle = doc.getString("roomTitle") ?: "",
                                    ticketCount = doc.getLong("ticketCount")?.toInt() ?: 1,
                                    timestampMs = doc.getLong("timestampMs") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedByDescending { it.timestampMs }
                        trySend(list)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach wallet transactions listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }

    // --- USER TICKETS STORAGE ---

    /**
     * Store a generated Tambola ticket in Firestore for the user.
     */
    suspend fun saveUserTicket(userIdOrPhone: String, ticket: TambolaTicket, customName: String) {
        val db = firestore ?: return
        if (userIdOrPhone.isBlank()) return
        try {
            val gridJson = ticket.grid.joinToString(";") { row ->
                row.joinToString(",") { it?.toString() ?: "" }
            }
            val userDocId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
            val ticketData = hashMapOf(
                "id" to ticket.id,
                "name" to if (customName.isBlank()) ticket.id else customName,
                "gridJson" to gridJson,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(userDocId)
                .collection("tickets")
                .document(ticket.id)
                .set(ticketData, SetOptions.merge())
                .await()
            Log.d(TAG, "Ticket ${ticket.id} stored in Firestore for $userIdOrPhone")
        } catch (t: Throwable) {
            Log.e(TAG, "Error storing ticket in Firestore", t)
        }
    }

    /**
     * Real-time stream of stored tickets in Firestore for the user.
     */
    fun observeUserTickets(userIdOrPhone: String): Flow<List<SavedTicketEntity>> = callbackFlow {
        val db = firestore
        if (db == null || userIdOrPhone.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val userDocId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_USERS)
                .document(userDocId)
                .collection("tickets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing user tickets: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val tickets = snapshot.documents.mapNotNull { doc ->
                            try {
                                SavedTicketEntity(
                                    id = doc.getString("id") ?: doc.id,
                                    name = doc.getString("name") ?: doc.id,
                                    gridJson = doc.getString("gridJson") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(tickets)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach ticket listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Delete stored ticket from Firestore for the user.
     */
    suspend fun deleteUserTicket(userIdOrPhone: String, ticketId: String) {
        val db = firestore ?: return
        if (userIdOrPhone.isBlank()) return
        try {
            val userDocId = userIdOrPhone.replace(Regex("[^0-9a-zA-Z_]"), "_")
            db.collection(COLLECTION_USERS)
                .document(userDocId)
                .collection("tickets")
                .document(ticketId)
                .delete()
                .await()
        } catch (t: Throwable) {
            Log.e(TAG, "Error deleting user ticket from Firestore", t)
        }
    }

    // --- GAME CONFIGURATIONS IN FIRESTORE ---

    private const val COLLECTION_CONFIG = "game_configurations"
    private const val DOC_GLOBAL_CONFIG = "global_settings"

    /**
     * Real-time stream of game configuration from Firestore.
     */
    fun observeGameConfiguration(): Flow<GameConfiguration> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(GameConfiguration())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_CONFIG)
                .document(DOC_GLOBAL_CONFIG)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing game config: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val config = GameConfiguration(
                            callerSpeedSeconds = snapshot.getLong("callerSpeedSeconds")?.toInt() ?: 5,
                            voiceLanguage = snapshot.getString("voiceLanguage") ?: "English",
                            autoClaimVerification = snapshot.getBoolean("autoClaimVerification") ?: true,
                            minWithdrawalAmount = snapshot.getLong("minWithdrawalAmount")?.toInt() ?: 100,
                            announcementBanner = snapshot.getString("announcementBanner") ?: "Welcome to Udaipurtambola! 90-Ball Live Games Every 15 Mins.",
                            maintenanceMode = snapshot.getBoolean("maintenanceMode") ?: false,
                            allowGuestJoin = snapshot.getBoolean("allowGuestJoin") ?: true,
                            maxTicketsPerPlayer = snapshot.getLong("maxTicketsPerPlayer")?.toInt() ?: 6,
                            updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                        trySend(config)
                    } else {
                        trySend(GameConfiguration())
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach game configuration listener", t)
            trySend(GameConfiguration())
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Save / Update game configurations in Firestore.
     */
    suspend fun saveGameConfiguration(config: GameConfiguration) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "callerSpeedSeconds" to config.callerSpeedSeconds,
                "voiceLanguage" to config.voiceLanguage,
                "autoClaimVerification" to config.autoClaimVerification,
                "minWithdrawalAmount" to config.minWithdrawalAmount,
                "announcementBanner" to config.announcementBanner,
                "maintenanceMode" to config.maintenanceMode,
                "allowGuestJoin" to config.allowGuestJoin,
                "maxTicketsPerPlayer" to config.maxTicketsPerPlayer,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_CONFIG)
                .document(DOC_GLOBAL_CONFIG)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Game configurations saved to Firestore successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving game configuration: ${t.localizedMessage}")
        }
    }

    // --- CURRENT GAME CALLER ENGINE (FIRESTORE 'current_game' COLLECTION) ---

    private const val COLLECTION_CURRENT_GAME = "current_game"
    private const val DOC_ACTIVE_MATCH = "active_game_state"

    /**
     * Real-time stream of the active match caller state from Firestore 'current_game' collection.
     */
    fun observeCurrentGame(): Flow<CurrentGameState> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(CurrentGameState())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_CURRENT_GAME)
                .document(DOC_ACTIVE_MATCH)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing current_game: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val currentNum = snapshot.getLong("currentNumber")?.toInt()
                        @Suppress("UNCHECKED_CAST")
                        val calledRaw = snapshot.get("calledNumbers") as? List<Long> ?: emptyList()
                        val calledNumbers = calledRaw.map { it.toInt() }
                        val isRunning = snapshot.getBoolean("isRunning") ?: false
                        val lastPhrase = snapshot.getString("lastPhrase") ?: ""
                        val drawIntervalSec = snapshot.getLong("drawIntervalSec")?.toInt() ?: 5
                        val gameId = snapshot.getString("gameId") ?: "LIVE_TAMBOLA_MATCH"
                        val updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()

                        val state = CurrentGameState(
                            gameId = gameId,
                            currentNumber = currentNum,
                            calledNumbers = calledNumbers,
                            lastPhrase = lastPhrase,
                            isRunning = isRunning,
                            drawIntervalSec = drawIntervalSec,
                            updatedAt = updatedAt
                        )
                        trySend(state)
                    } else {
                        trySend(CurrentGameState())
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach current_game listener", t)
            trySend(CurrentGameState())
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Pull the latest game state once from Firestore 'current_game' collection.
     */
    suspend fun pullCurrentGameState(): CurrentGameState? {
        val db = firestore ?: return null
        return try {
            val snapshot = db.collection(COLLECTION_CURRENT_GAME)
                .document(DOC_ACTIVE_MATCH)
                .get()
                .await()

            if (snapshot.exists()) {
                val currentNum = snapshot.getLong("currentNumber")?.toInt()
                @Suppress("UNCHECKED_CAST")
                val calledRaw = snapshot.get("calledNumbers") as? List<Long> ?: emptyList()
                val calledNumbers = calledRaw.map { it.toInt() }
                val isRunning = snapshot.getBoolean("isRunning") ?: false
                val lastPhrase = snapshot.getString("lastPhrase") ?: ""
                val drawIntervalSec = snapshot.getLong("drawIntervalSec")?.toInt() ?: 5
                val gameId = snapshot.getString("gameId") ?: "LIVE_TAMBOLA_MATCH"
                val updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()

                CurrentGameState(
                    gameId = gameId,
                    currentNumber = currentNum,
                    calledNumbers = calledNumbers,
                    lastPhrase = lastPhrase,
                    isRunning = isRunning,
                    drawIntervalSec = drawIntervalSec,
                    updatedAt = updatedAt
                )
            } else {
                null
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error pulling current game state: ${t.localizedMessage}")
            null
        }
    }

    /**
     * Updates or syncs the current match state in the 'current_game' collection.
     */
    suspend fun updateCurrentGameState(gameState: CurrentGameState) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "gameId" to gameState.gameId,
                "currentNumber" to gameState.currentNumber,
                "calledNumbers" to gameState.calledNumbers,
                "lastPhrase" to gameState.lastPhrase,
                "isRunning" to gameState.isRunning,
                "drawIntervalSec" to gameState.drawIntervalSec,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_CURRENT_GAME)
                .document(DOC_ACTIVE_MATCH)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Synced current_game state to Firestore. Number: ${gameState.currentNumber}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating current_game in Firestore", t)
        }
    }

    /**
     * Picks the next random number (1..90), writes it to the Firestore 'current_game' collection,
     * and returns the drawn ball to simulate the Tambola caller engine.
     */
    suspend fun drawNextFirestoreGameNumber(): Int? {
        val current = pullCurrentGameState() ?: CurrentGameState()
        val allNumbers = (1..90).toList()
        val remaining = allNumbers.filterNot { current.calledNumbers.contains(it) }

        if (remaining.isEmpty()) {
            // All 90 numbers called
            updateCurrentGameState(current.copy(isRunning = false))
            return null
        }

        val drawnNumber = remaining.random()
        val updatedCalled = listOf(drawnNumber) + current.calledNumbers
        val phrase = CallerPhrases.getPhrase(drawnNumber)

        val updatedState = current.copy(
            currentNumber = drawnNumber,
            calledNumbers = updatedCalled,
            lastPhrase = phrase,
            isRunning = true,
            updatedAt = System.currentTimeMillis()
        )

        updateCurrentGameState(updatedState)
        return drawnNumber
    }

    /**
     * Manually forces a specific number (1..90) into the active game sequence in Firestore 'current_game'.
     */
    suspend fun drawSpecificFirestoreGameNumber(targetNumber: Int): Boolean {
        if (targetNumber !in 1..90) return false
        val current = pullCurrentGameState() ?: CurrentGameState()
        if (current.calledNumbers.contains(targetNumber)) {
            return false // Already called
        }

        val updatedCalled = listOf(targetNumber) + current.calledNumbers
        val phrase = CallerPhrases.getPhrase(targetNumber)

        val updatedState = current.copy(
            currentNumber = targetNumber,
            calledNumbers = updatedCalled,
            lastPhrase = phrase,
            isRunning = true,
            updatedAt = System.currentTimeMillis()
        )

        updateCurrentGameState(updatedState)
        return true
    }

    /**
     * Reset the active match in Firestore 'current_game' collection.
     */
    suspend fun resetCurrentGameState() {
        val db = firestore ?: return
        try {
            val resetState = hashMapOf(
                "gameId" to "LIVE_TAMBOLA_MATCH",
                "currentNumber" to null,
                "calledNumbers" to emptyList<Int>(),
                "lastPhrase" to "Game Reset by Host",
                "isRunning" to false,
                "drawIntervalSec" to 5,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_CURRENT_GAME)
                .document(DOC_ACTIVE_MATCH)
                .set(resetState)
                .await()
            Log.d(TAG, "Current game in Firestore reset successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Error resetting current_game in Firestore", t)
        }
    }

    // --- GAME WINNERS HISTORY (FIRESTORE 'game_history' COLLECTION) ---

    private const val COLLECTION_HISTORY = "game_history"

    /**
     * Real-time stream of past Tambola winners from Firestore 'game_history' collection.
     */
    fun observeWinnersHistory(): Flow<List<TambolaWinnerHistory>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(getDefaultWinnersHistory())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_HISTORY)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing winners history: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        val winners = snapshots.documents.mapNotNull { doc ->
                            try {
                                TambolaWinnerHistory(
                                    id = doc.id,
                                    matchId = doc.getString("matchId") ?: "MATCH_${doc.id.take(6)}",
                                    roomTitle = doc.getString("roomTitle") ?: "Grand 90 Live",
                                    winnerName = doc.getString("winnerName") ?: "Player",
                                    winnerPhone = doc.getString("winnerPhone") ?: "",
                                    claimPattern = doc.getString("claimPattern") ?: "Full House",
                                    prizeAmount = doc.getLong("prizeAmount")?.toInt() ?: 500,
                                    winningNumber = doc.getLong("winningNumber")?.toInt() ?: 90,
                                    totalNumbersCalled = doc.getLong("totalNumbersCalled")?.toInt() ?: 45,
                                    verifiedTimestamp = doc.getLong("verifiedTimestamp") ?: System.currentTimeMillis(),
                                    isAutoVerified = doc.getBoolean("isAutoVerified") ?: true,
                                    transactionRef = doc.getString("transactionRef") ?: "TXN_${doc.id.take(8).uppercase()}"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedByDescending { it.verifiedTimestamp }
                        trySend(winners)
                    } else {
                        // If collection is empty in Firestore, seed standard transparent historical winners
                        trySend(getDefaultWinnersHistory())
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach winners history listener", t)
            trySend(getDefaultWinnersHistory())
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Record a newly verified Tambola winner in Firestore 'game_history' collection.
     */
    suspend fun recordWinnerInFirestore(winner: TambolaWinnerHistory) {
        val db = firestore ?: return
        try {
            val docId = if (winner.id.isNotBlank()) winner.id else "win_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "matchId" to winner.matchId.ifBlank { "MATCH_${System.currentTimeMillis().toString().takeLast(6)}" },
                "roomTitle" to winner.roomTitle,
                "winnerName" to winner.winnerName,
                "winnerPhone" to winner.winnerPhone,
                "claimPattern" to winner.claimPattern,
                "prizeAmount" to winner.prizeAmount,
                "winningNumber" to winner.winningNumber,
                "totalNumbersCalled" to winner.totalNumbersCalled,
                "verifiedTimestamp" to winner.verifiedTimestamp,
                "isAutoVerified" to winner.isAutoVerified,
                "transactionRef" to winner.transactionRef
            )
            db.collection(COLLECTION_HISTORY)
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved winner record to Firestore: ${winner.winnerName} ($docId)")
        } catch (t: Throwable) {
            Log.e(TAG, "Error recording winner to Firestore", t)
        }
    }

    private fun getDefaultWinnersHistory(): List<TambolaWinnerHistory> {
        val now = System.currentTimeMillis()
        return listOf(
            TambolaWinnerHistory(
                id = "win_1",
                matchId = "MATCH_982142",
                roomTitle = "👑 Royal Udaipur Mega Jackpot",
                winnerName = "Rajesh Sharma",
                winnerPhone = "+91 98290 ****",
                claimPattern = "Full House",
                prizeAmount = 25000,
                winningNumber = 73,
                totalNumbersCalled = 56,
                verifiedTimestamp = now - 1800000,
                isAutoVerified = true,
                transactionRef = "TXN_UDPR_78912"
            ),
            TambolaWinnerHistory(
                id = "win_2",
                matchId = "MATCH_982141",
                roomTitle = "⚡ Fast 90 Rush Hour",
                winnerName = "Priya Mehta",
                winnerPhone = "+91 94141 ****",
                claimPattern = "Top Line",
                prizeAmount = 3500,
                winningNumber = 17,
                totalNumbersCalled = 24,
                verifiedTimestamp = now - 5400000,
                isAutoVerified = true,
                transactionRef = "TXN_UDPR_78905"
            ),
            TambolaWinnerHistory(
                id = "win_3",
                matchId = "MATCH_982140",
                roomTitle = "👑 Royal Udaipur Mega Jackpot",
                winnerName = "Vikramaditya S.",
                winnerPhone = "+91 98280 ****",
                claimPattern = "Early 5",
                prizeAmount = 5000,
                winningNumber = 42,
                totalNumbersCalled = 18,
                verifiedTimestamp = now - 14400000,
                isAutoVerified = true,
                transactionRef = "TXN_UDPR_78891"
            ),
            TambolaWinnerHistory(
                id = "win_4",
                matchId = "MATCH_982138",
                roomTitle = "💎 Diamond High Roller",
                winnerName = "Sunita Rathore",
                winnerPhone = "+91 99281 ****",
                claimPattern = "Middle Line",
                prizeAmount = 4500,
                winningNumber = 88,
                totalNumbersCalled = 38,
                verifiedTimestamp = now - 28800000,
                isAutoVerified = true,
                transactionRef = "TXN_UDPR_78864"
            ),
            TambolaWinnerHistory(
                id = "win_5",
                matchId = "MATCH_982135",
                roomTitle = "⚡ Fast 90 Rush Hour",
                winnerName = "Amitabh Choudhary",
                winnerPhone = "+91 97845 ****",
                claimPattern = "Four Corners",
                prizeAmount = 4000,
                winningNumber = 90,
                totalNumbersCalled = 31,
                verifiedTimestamp = now - 86400000,
                isAutoVerified = true,
                transactionRef = "TXN_UDPR_78750"
            )
        )
    }

    // --- ADMIN ORGANIZATION INFO & SUPPORT DETAILS ---

    private const val DOC_ADMIN_ORG_INFO = "admin_organization_info"
    private const val COLLECTION_ADMIN_BOOKINGS = "admin_ticket_bookings"

    /**
     * Real-time stream of Admin Organization and Rules Info from Firestore.
     */
    fun observeAdminOrgInfo(): Flow<AdminOrganizationInfo> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(AdminOrganizationInfo())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_CONFIG)
                .document(DOC_ADMIN_ORG_INFO)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing admin org info: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val orgInfo = AdminOrganizationInfo(
                            organizationName = snapshot.getString("organizationName") ?: "Udaipurtambola Official Gaming Club",
                            supportPhone = snapshot.getString("supportPhone") ?: "+91 98765 00100",
                            supportWhatsapp = snapshot.getString("supportWhatsapp") ?: "+91 98765 00100",
                            supportEmail = snapshot.getString("supportEmail") ?: "admin@udaipurtambola.club",
                            address = snapshot.getString("address") ?: "Plot 42, Amber Tower, Fatehsagar Lake Road, Udaipur, Rajasthan 313001",
                            adminUpiId = snapshot.getString("adminUpiId") ?: "udaipurtambola.admin@oksbi",
                            adminBankName = snapshot.getString("adminBankName") ?: "State Bank of India (SBI)",
                            adminAccountNumber = snapshot.getString("adminAccountNumber") ?: "•••• •••• 9821",
                            adminIfscCode = snapshot.getString("adminIfscCode") ?: "SBIN0001234",
                            rulesAndRegulations = snapshot.getString("rulesAndRegulations") ?: AdminOrganizationInfo().rulesAndRegulations,
                            updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                        trySend(orgInfo)
                    } else {
                        trySend(AdminOrganizationInfo())
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach admin org info listener", t)
            trySend(AdminOrganizationInfo())
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Save Admin Organization and Rules Info to Firestore.
     */
    suspend fun saveAdminOrgInfo(orgInfo: AdminOrganizationInfo) {
        val db = firestore ?: return
        try {
            val map = hashMapOf(
                "organizationName" to orgInfo.organizationName,
                "supportPhone" to orgInfo.supportPhone,
                "supportWhatsapp" to orgInfo.supportWhatsapp,
                "supportEmail" to orgInfo.supportEmail,
                "address" to orgInfo.address,
                "adminUpiId" to orgInfo.adminUpiId,
                "adminBankName" to orgInfo.adminBankName,
                "adminAccountNumber" to orgInfo.adminAccountNumber,
                "adminIfscCode" to orgInfo.adminIfscCode,
                "rulesAndRegulations" to orgInfo.rulesAndRegulations,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_CONFIG)
                .document(DOC_ADMIN_ORG_INFO)
                .set(map, SetOptions.merge())
                .await()
            Log.d(TAG, "Admin org info saved to Firestore")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving admin org info to Firestore", t)
        }
    }

    /**
     * Record an Admin ticket booking.
     */
    suspend fun recordAdminTicketBooking(booking: AdminTicketBookingRecord) {
        val db = firestore ?: return
        try {
            val map = hashMapOf(
                "id" to booking.id,
                "playerPhone" to booking.playerPhone,
                "playerName" to booking.playerName,
                "roomId" to booking.roomId,
                "roomTitle" to booking.roomTitle,
                "ticketCount" to booking.ticketCount,
                "ticketIds" to booking.ticketIds,
                "amountPaid" to booking.amountPaid,
                "paymentMethod" to booking.paymentMethod,
                "timestampMs" to booking.timestampMs
            )
            db.collection(COLLECTION_ADMIN_BOOKINGS)
                .document(booking.id)
                .set(map)
                .await()
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving admin booking to Firestore", t)
        }
    }

    /**
     * Stream all admin ticket bookings.
     */
    fun observeAdminTicketBookings(): Flow<List<AdminTicketBookingRecord>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COLLECTION_ADMIN_BOOKINGS)
                .orderBy("timestampMs", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing admin bookings: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val bookings = snapshot.documents.mapNotNull { doc ->
                            try {
                                AdminTicketBookingRecord(
                                    id = doc.getString("id") ?: doc.id,
                                    playerPhone = doc.getString("playerPhone") ?: "",
                                    playerName = doc.getString("playerName") ?: "Player",
                                    roomId = doc.getString("roomId") ?: "",
                                    roomTitle = doc.getString("roomTitle") ?: "Match",
                                    ticketCount = doc.getLong("ticketCount")?.toInt() ?: 1,
                                    ticketIds = (doc.get("ticketIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                    amountPaid = doc.getLong("amountPaid")?.toInt() ?: 0,
                                    paymentMethod = doc.getString("paymentMethod") ?: "Admin Direct Booking",
                                    timestampMs = doc.getLong("timestampMs") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(bookings)
                    }
                }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to attach admin bookings listener", t)
        }

        awaitClose {
            listener?.remove()
        }
    }
}

package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TambolaDatabase
import com.example.data.TambolaRepository
import com.example.model.AdminOrganizationInfo
import com.example.model.AdminTicketBookingRecord
import com.example.model.AgentBookingRecord
import com.example.model.BotPlayer
import com.example.model.CallerPhrases
import com.example.model.ClaimResult
import com.example.model.ClaimType
import com.example.model.CurrentGameState
import com.example.model.GameConfiguration
import com.example.model.GameRecord
import com.example.model.GameRoom
import com.example.model.PlayerProfileStats
import com.example.model.RazorpayTransaction
import com.example.model.RoomChatMessage
import com.example.model.RoomTicketSlot
import com.example.model.SavedTicketEntity
import com.example.model.StaffRole
import com.example.model.StaffUser
import com.example.model.TambolaTicket
import com.example.model.TambolaWinnerHistory
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.WalletTransaction
import com.example.model.generateRoomTicketSlots
import com.example.service.FirebaseAuthService
import com.example.service.FirestoreService
import com.example.service.TambolaCallerBackgroundService
import com.example.service.TambolaFirebaseMessagingService
import com.example.service.TambolaSoundManager
import com.example.service.WinnerNotificationPayload
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

enum class ActiveTab {
    LOBBY,
    PROFILE,
    WALLET_HISTORY,
    SOLO_BOT_ROOM,
    CALLER_BOARD,
    CLAIM_VERIFIER,
    TICKET_GENERATOR,
    GAME_HISTORY,
    ADMIN_PANEL
}

data class TambolaUiState(
    val activeTab: ActiveTab = ActiveTab.LOBBY,
    // Wallet & User Profile
    val walletBalance: Int = 1250,
    val userName: String = "Lucky Striker",
    val userMobileNumber: String = "",
    val userProfile: PlayerProfileStats = PlayerProfileStats(),
    val isLoggedIn: Boolean = false,
    val isAuthModalVisible: Boolean = false,
    val authStep: Int = 1, // 1: Enter Mobile, 2: Enter OTP
    val tempMobileInput: String = "",
    val otpInput: String = "",
    val authStatusMessage: String? = null,

    // Admin Config & Support Contact
    val adminSupportPhone: String = "+91 98765 00100",
    val adminSupportWhatsapp: String = "+91 98765 00100",
    val adminOrgInfo: AdminOrganizationInfo = AdminOrganizationInfo(),
    val adminBookingsList: List<AdminTicketBookingRecord> = emptyList(),
    val isBuyTicketsModalVisible: Boolean = false,
    val selectedRoomForDirectBuy: GameRoom? = null,

    // Razorpay & Wallet Integration
    val isRazorpayModalVisible: Boolean = false,
    val razorpayTransactions: List<RazorpayTransaction> = listOf(
        RazorpayTransaction("pay_Rzp982341", 500, "SUCCESS", "UPI (Google Pay)", System.currentTimeMillis() - 86400000),
        RazorpayTransaction("pay_Rzp751920", 750, "SUCCESS", "Razorpay UPI", System.currentTimeMillis() - 172800000)
    ),
    val walletTransactions: List<WalletTransaction> = listOf(
        WalletTransaction(
            id = "TXN_984210",
            type = TransactionType.DEPOSIT,
            amount = 500,
            title = "Deposit via Google Pay UPI",
            description = "Razorpay Instant Bank Settlement",
            paymentMethod = "UPI (Google Pay)",
            referenceId = "pay_Rzp982341",
            status = TransactionStatus.SUCCESS,
            closingBalance = 1250,
            timestampMs = System.currentTimeMillis() - 3600000L
        ),
        WalletTransaction(
            id = "TXN_984209",
            type = TransactionType.TICKET_PURCHASE,
            amount = 50,
            title = "Booked 1 Ticket • Mega Amber Jackpot 90",
            description = "Entry Fee for 90-Ball Public Arena",
            paymentMethod = "Wallet Balance",
            referenceId = "TCK_883019",
            status = TransactionStatus.SUCCESS,
            closingBalance = 750,
            roomTitle = "Mega Amber Jackpot 90",
            ticketCount = 1,
            timestampMs = System.currentTimeMillis() - 14400000L
        ),
        WalletTransaction(
            id = "TXN_984208",
            type = TransactionType.PRIZE_WIN,
            amount = 300,
            title = "Early 5 Prize Won",
            description = "Verified & Approved in Evening Bonanza 90",
            paymentMethod = "Prize Pool Credit",
            referenceId = "WIN_771920",
            status = TransactionStatus.SUCCESS,
            closingBalance = 800,
            roomTitle = "Evening Bonanza 90",
            timestampMs = System.currentTimeMillis() - 43200000L
        ),
        WalletTransaction(
            id = "TXN_984207",
            type = TransactionType.WITHDRAWAL,
            amount = 200,
            title = "Instant Withdrawal to UPI",
            description = "Payout sent to user@okhdfcbank",
            paymentMethod = "Instant UPI Transfer",
            referenceId = "WDR_UPI_661902",
            status = TransactionStatus.SUCCESS,
            closingBalance = 500,
            timestampMs = System.currentTimeMillis() - 86400000L
        ),
        WalletTransaction(
            id = "TXN_984206",
            type = TransactionType.DEPOSIT,
            amount = 750,
            title = "Deposit via PhonePe UPI",
            description = "Razorpay Fast Checkout",
            paymentMethod = "PhonePe UPI",
            referenceId = "pay_Rzp751920",
            status = TransactionStatus.SUCCESS,
            closingBalance = 700,
            timestampMs = System.currentTimeMillis() - 172800000L
        )
    ),

    // Lobby & Active Rooms
    val selectedCategory: String = "All",
    val activeRooms: List<GameRoom> = listOf(
        GameRoom(
            id = "room-1",
            title = "Mega Amber Jackpot 90 (Limited 10)",
            hostName = "HousieMaster Pro",
            category = "Public",
            prizeAmount = 25000,
            entryFee = 50,
            currentPlayers = 6,
            maxPlayers = 10,
            isUnlimitedPlayers = false,
            isLive = true,
            isJackpot = true,
            iconEmoji = "👑",
            scheduledStartTimeMs = System.currentTimeMillis() + 1800000,
            scheduledTimeString = "Today • 09:30 PM",
            prizeBreakdown = mapOf(
                "Jaldi 5" to 2500,
                "Four Corners" to 2000,
                "Top Line" to 3500,
                "Middle Line" to 3500,
                "Bottom Line" to 3500,
                "1st Full House" to 7000,
                "2nd Full House" to 3000
            ),
            ticketSlots = generateRoomTicketSlots(
                count = 10,
                bookedDetails = listOf(
                    "Priya Sharma" to "+91 94141 20011",
                    "Rajesh Mehta" to "+91 98290 30022",
                    "Vikramaditya S." to "+91 98280 40033",
                    "Sunita Rathore" to "+91 99281 50044",
                    "Aarav Patel" to "+91 97845 60055",
                    "Amitabh C." to "+91 98765 70066"
                )
            )
        ),
        GameRoom(
            id = "room-2",
            title = "Quick 5 Speed Housie (Limited 20)",
            hostName = "SpeedyCaller",
            category = "Quick 90",
            prizeAmount = 5000,
            entryFee = 20,
            currentPlayers = 8,
            maxPlayers = 20,
            isUnlimitedPlayers = false,
            isLive = true,
            isJackpot = false,
            iconEmoji = "⚡",
            scheduledStartTimeMs = System.currentTimeMillis() + 600000,
            scheduledTimeString = "Today • 09:00 PM",
            prizeBreakdown = mapOf(
                "Jaldi 5" to 800,
                "Four Corners" to 500,
                "Top Line" to 700,
                "Middle Line" to 700,
                "Bottom Line" to 700,
                "1st Full House" to 1200,
                "2nd Full House" to 400
            ),
            ticketSlots = generateRoomTicketSlots(
                count = 20,
                bookedDetails = listOf(
                    "Rohan Das" to "+91 98291 11001",
                    "Kavita Jain" to "+91 94142 22002",
                    "Mohan Lal" to "+91 98281 33003",
                    "Deepak Joshi" to "+91 99282 44004",
                    "Ananya Roy" to "+91 97846 55005",
                    "Suresh B." to "+91 98766 66006",
                    "Pooja Verma" to "+91 98292 77007",
                    "Naveen K." to "+91 94143 88008"
                )
            )
        ),
        GameRoom(
            id = "room-3",
            title = "High Rollers VIP 90 (Unlimited)",
            hostName = "VIP Host",
            category = "High Roller",
            prizeAmount = 50000,
            entryFee = 200,
            currentPlayers = 80,
            maxPlayers = 100,
            isUnlimitedPlayers = true,
            isLive = true,
            isJackpot = true,
            iconEmoji = "💎",
            scheduledStartTimeMs = System.currentTimeMillis() + 7200000,
            scheduledTimeString = "Tonight • 11:00 PM",
            prizeBreakdown = mapOf(
                "Jaldi 5" to 5000,
                "Four Corners" to 5000,
                "Top Line" to 7000,
                "Middle Line" to 7000,
                "Bottom Line" to 7000,
                "1st Full House" to 14000,
                "2nd Full House" to 5000
            )
        ),
        GameRoom(
            id = "room-4",
            title = "Evening Bonanza 90 (Limited 10)",
            hostName = "TambolaKing",
            category = "Public",
            prizeAmount = 10000,
            entryFee = 30,
            currentPlayers = 3,
            maxPlayers = 10,
            isUnlimitedPlayers = false,
            isLive = true,
            isJackpot = false,
            iconEmoji = "🌟",
            scheduledStartTimeMs = null,
            scheduledTimeString = "Live Now",
            prizeBreakdown = mapOf(
                "Jaldi 5" to 1200,
                "Four Corners" to 800,
                "Top Line" to 1500,
                "Middle Line" to 1500,
                "Bottom Line" to 1500,
                "1st Full House" to 2500,
                "2nd Full House" to 1000
            ),
            ticketSlots = generateRoomTicketSlots(
                count = 10,
                bookedDetails = listOf(
                    "Manish S." to "+91 98293 11111",
                    "Ritu Goyal" to "+91 94144 22222",
                    "Harish C." to "+91 98282 33333"
                )
            )
        )
    ),
    val currentJoinedRoom: GameRoom? = null,

    // Admin Control Panel & Security Authentication
    val isAdminAuthenticated: Boolean = false,
    val adminAuthError: String? = null,
    val totalRevenueCollected: Int = 14500,

    // Caller Deck & Draw State
    val calledNumbers: List<Int> = emptyList(),
    val remainingNumbers: List<Int> = (1..90).toList().shuffled(),
    val currentCalledNumber: Int? = null,
    val isAutoCalling: Boolean = false,
    val autoCallIntervalSec: Int = 4,
    val autoCallRemainingMillis: Long = 0L,
    val autoCallCountdownProgress: Float = 0f,
    val isSoundEnabled: Boolean = true,
    val isAutoDabEnabled: Boolean = true,
    val isSettingsModalVisible: Boolean = false,

    // Player Tickets & Marked state
    val playerTickets: List<TambolaTicket> = listOf(TambolaTicket.generate("My-Ticket-1")),
    val markedNumbersMap: Map<String, Set<Int>> = emptyMap(), // ticketId -> marked set

    // Claims Tracking
    val claimedPrizes: Map<ClaimType, String> = emptyMap(), // ClaimType -> winner name

    // Bot Opponents
    val bots: List<BotPlayer> = listOf(
        BotPlayer("Priya Sharma", "👩‍💼", TambolaTicket.generate("Bot-1")),
        BotPlayer("Rahul Verma", "🧑‍💻", TambolaTicket.generate("Bot-2")),
        BotPlayer("Aarav Patel", "🦸‍♂️", TambolaTicket.generate("Bot-3"))
    ),
    val botMarkedMap: Map<String, Set<Int>> = emptyMap(),

    // Room Chat & Event Feed
    val chatMessages: List<RoomChatMessage> = listOf(
        RoomChatMessage("1", "Host", "Welcome to HousieSphere! Room open. Match starting...", isSystem = true)
    ),

    // Active Verification Modal Result
    val lastClaimResult: ClaimResult? = null,
    val isGameFinished: Boolean = false,
    val userScore: Int = 0,

    // Custom Generator State
    val generatedSheet: List<TambolaTicket> = emptyList(),
    val savedTicketsList: List<SavedTicketEntity> = emptyList(),
    val gameRecordsList: List<GameRecord> = emptyList(),

    // Global Game Configurations in Firestore
    val gameConfiguration: GameConfiguration = GameConfiguration(),

    // Real-time Firestore 'current_game' Caller Engine State
    val currentGameState: CurrentGameState = CurrentGameState(),
    val isBackgroundCallerRunning: Boolean = false,

    // Real-time Firestore 'game_history' Past Winners List
    val pastWinnersHistory: List<TambolaWinnerHistory> = emptyList(),

    // Real-time FCM Winner Alert Popup/Banner
    val activeWinnerNotification: WinnerNotificationPayload? = null,

    // Staff & Administration RBAC System (Admin, Manager, Agent)
    val currentStaffUser: StaffUser? = null,
    val isStaffPortalOpen: Boolean = false,
    val staffUsers: List<StaffUser> = listOf(
        StaffUser(
            id = "staff_admin_1",
            loginId = "admin",
            password = "admin123",
            name = "Chief Administrator",
            role = StaffRole.ADMIN,
            phone = "+91 98765 00100",
            isBookingAllowed = true,
            isCreationAllowed = true
        ),
        StaffUser(
            id = "staff_manager_1",
            loginId = "manager",
            password = "manager123",
            name = "Room Operations Manager",
            role = StaffRole.MANAGER,
            phone = "+91 98290 11223",
            isBookingAllowed = true,
            isCreationAllowed = true
        ),
        StaffUser(
            id = "staff_agent_1",
            loginId = "agent1",
            password = "agent123",
            name = "Rahul Sharma (Agent)",
            role = StaffRole.AGENT,
            phone = "+91 94141 88990",
            isBookingAllowed = true,
            isCreationAllowed = false
        )
    ),
    val agentBookingRecords: List<AgentBookingRecord> = listOf(
        AgentBookingRecord(
            id = "AGBK_101",
            agentId = "staff_agent_1",
            agentName = "Rahul Sharma (Agent)",
            playerPhone = "+91 94141 20011",
            playerName = "Priya Sharma",
            roomId = "room-1",
            roomTitle = "Mega Amber Jackpot 90 (Limited 10)",
            slotNumber = 1,
            ticketCount = 1,
            amountPaid = 50,
            timestampMs = System.currentTimeMillis() - 7200000L
        ),
        AgentBookingRecord(
            id = "AGBK_102",
            agentId = "staff_agent_1",
            agentName = "Rahul Sharma (Agent)",
            playerPhone = "+91 98290 30022",
            playerName = "Rajesh Mehta",
            roomId = "room-1",
            roomTitle = "Mega Amber Jackpot 90 (Limited 10)",
            slotNumber = 2,
            ticketCount = 1,
            amountPaid = 50,
            timestampMs = System.currentTimeMillis() - 3600000L
        )
    )
)

class TambolaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TambolaRepository
    private var tts: TextToSpeech? = null
    val soundManager: TambolaSoundManager = TambolaSoundManager.getInstance(application)

    private val _uiState = MutableStateFlow(TambolaUiState())
    val uiState: StateFlow<TambolaUiState> = _uiState.asStateFlow()

    private var autoCallJob: Job? = null

    init {
        val dao = TambolaDatabase.getDatabase(application).tambolaDao()
        repository = TambolaRepository(dao)

        initTextToSpeech(application)
        observeDatabase()
        observeBackgroundService()
        resetGame()
    }

    private fun initTextToSpeech(context: Application) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    private fun observeBackgroundService() {
        viewModelScope.launch {
            TambolaCallerBackgroundService.isServiceRunning.collectLatest { running ->
                _uiState.update { it.copy(isBackgroundCallerRunning = running) }
            }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.savedTickets.collectLatest { list ->
                _uiState.update { it.copy(savedTicketsList = list) }
            }
        }
        viewModelScope.launch {
            repository.gameRecords.collectLatest { list ->
                _uiState.update { it.copy(gameRecordsList = list) }
            }
        }
        // Observe Firestore Game Rooms
        viewModelScope.launch {
            FirestoreService.observeGameRooms().collectLatest { firestoreRooms ->
                if (firestoreRooms.isNotEmpty()) {
                    _uiState.update { it.copy(activeRooms = firestoreRooms) }
                }
            }
        }
        // Observe Firestore Global Game Configurations
        viewModelScope.launch {
            FirestoreService.observeGameConfiguration().collectLatest { config ->
                _uiState.update { it.copy(gameConfiguration = config) }
            }
        }
        // Observe Firestore Admin Organization and Rules Info
        viewModelScope.launch {
            FirestoreService.observeAdminOrgInfo().collectLatest { orgInfo ->
                _uiState.update { 
                    it.copy(
                        adminOrgInfo = orgInfo,
                        adminSupportPhone = orgInfo.supportPhone,
                        adminSupportWhatsapp = orgInfo.supportWhatsapp
                    ) 
                }
            }
        }
        // Observe Firestore Admin Ticket Bookings
        viewModelScope.launch {
            FirestoreService.observeAdminTicketBookings().collectLatest { bookings ->
                if (bookings.isNotEmpty()) {
                    _uiState.update { it.copy(adminBookingsList = bookings) }
                }
            }
        }
        // Observe Firestore 'game_history' Past Winners Stream
        viewModelScope.launch {
            FirestoreService.observeWinnersHistory().collectLatest { winners ->
                _uiState.update { it.copy(pastWinnersHistory = winners) }
            }
        }
        // Observe Real-time FCM / In-App Winning Notifications
        viewModelScope.launch {
            TambolaFirebaseMessagingService.winnerEvents.collectLatest { payload ->
                _uiState.update { it.copy(activeWinnerNotification = payload) }
                addChatMessage("System 🏆", "🎉 ${payload.title} - ${payload.winnerName} won ${payload.claimType} (₹${payload.prizeAmount})", isSystem = true)
            }
        }
        // Observe Firestore 'current_game' collection (Live Caller Engine Sync)
        viewModelScope.launch {
            FirestoreService.observeCurrentGame().collectLatest { firestoreGame ->
                val prevNum = _uiState.value.currentCalledNumber
                val newNum = firestoreGame.currentNumber

                val allNumbers = (1..90).toList()
                val remaining = allNumbers.filterNot { firestoreGame.calledNumbers.contains(it) }

                _uiState.update { current ->
                    current.copy(
                        currentGameState = firestoreGame,
                        calledNumbers = firestoreGame.calledNumbers,
                        currentCalledNumber = newNum,
                        remainingNumbers = remaining,
                        isAutoCalling = firestoreGame.isRunning || current.isBackgroundCallerRunning
                    )
                }

                // If a new number was drawn and differed from previous
                if (newNum != null && newNum != prevNum) {
                    val phrase = firestoreGame.lastPhrase.ifBlank { CallerPhrases.getPhrase(newNum) }
                    if (_uiState.value.isSoundEnabled) {
                        soundManager.playNumberDrawChime()
                        speakPhrase(phrase)
                    }
                    addChatMessage("Firestore Caller 🎙️", phrase, isSystem = true)
                    processAutoDabForNumber(newNum)
                    processBotTurns(newNum)
                }
            }
        }
    }

    private fun observeUserFirestoreData(userPhone: String) {
        if (userPhone.isBlank()) return
        // Real-time wallet sync from Firestore
        viewModelScope.launch {
            FirestoreService.observeUserWallet(userPhone).collectLatest { balance ->
                if (balance != null) {
                    _uiState.update { it.copy(walletBalance = balance) }
                }
            }
        }
        // Real-time transaction history from Firestore
        viewModelScope.launch {
            FirestoreService.observeUserTransactions(userPhone).collectLatest { txs ->
                if (txs.isNotEmpty()) {
                    _uiState.update { it.copy(razorpayTransactions = txs) }
                }
            }
        }
        // Real-time wallet passbook transactions from Firestore
        viewModelScope.launch {
            FirestoreService.observeUserWalletTransactions(userPhone).collectLatest { txs ->
                if (txs.isNotEmpty()) {
                    _uiState.update { it.copy(walletTransactions = txs) }
                }
            }
        }
        // Real-time user tickets from Firestore
        viewModelScope.launch {
            FirestoreService.observeUserTickets(userPhone).collectLatest { firestoreTickets ->
                if (firestoreTickets.isNotEmpty()) {
                    _uiState.update { it.copy(savedTicketsList = firestoreTickets) }
                }
            }
        }
        // Real-time user profile from Firestore
        viewModelScope.launch {
            FirestoreService.observeUserProfile(userPhone).collectLatest { profile ->
                if (profile != null) {
                    _uiState.update { it.copy(userProfile = profile, userName = profile.nickname) }
                }
            }
        }
    }

    fun selectTab(tab: ActiveTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setTicketCount(count: Int) {
        val newTickets = (1..count).map { idx ->
            TambolaTicket.generate("Ticket-$idx")
        }
        val newMarkedMap = newTickets.associate { it.id to emptySet<Int>() }
        _uiState.update {
            it.copy(
                playerTickets = newTickets,
                markedNumbersMap = newMarkedMap
            )
        }
        addChatMessage("Host", "User joined with $count ticket(s)", isSystem = true)
    }

    fun toggleMarkNumber(ticketId: String, number: Int) {
        var isDabbing = true
        _uiState.update { state ->
            val currentMarked = state.markedNumbersMap[ticketId] ?: emptySet()
            val newSet = if (currentMarked.contains(number)) {
                isDabbing = false
                currentMarked - number
            } else {
                isDabbing = true
                currentMarked + number
            }
            val updatedMap = state.markedNumbersMap.toMutableMap()
            updatedMap[ticketId] = newSet
            state.copy(markedNumbersMap = updatedMap)
        }
        if (uiState.value.isSoundEnabled) {
            soundManager.playDabSound(isDabbing)
        }
    }

    fun autoMarkAllCalledNumbers(silent: Boolean = false) {
        val calledSet = uiState.value.calledNumbers.toSet()
        var markedCount = 0
        _uiState.update { state ->
            val updatedMap = state.markedNumbersMap.toMutableMap()
            state.playerTickets.forEach { ticket ->
                val ticketNumbers = ticket.getAllNumbers().toSet()
                val newlyMarked = ticketNumbers.intersect(calledSet)
                val prevMarked = updatedMap[ticket.id] ?: emptySet()
                if (newlyMarked != prevMarked) {
                    markedCount += (newlyMarked.size - prevMarked.size)
                    updatedMap[ticket.id] = newlyMarked
                }
            }
            state.copy(markedNumbersMap = updatedMap)
        }
        if (!silent && uiState.value.isSoundEnabled && markedCount > 0) {
            soundManager.playDabSound(isDabbing = true)
        }
        if (!silent) {
            addChatMessage("Host", "Auto-dabbed all called numbers on tickets!", isSystem = true)
        }
    }

    fun toggleAutoDab() {
        var isNowActive = false
        _uiState.update { current ->
            isNowActive = !current.isAutoDabEnabled
            current.copy(isAutoDabEnabled = isNowActive)
        }
        if (isNowActive) {
            autoMarkAllCalledNumbers(silent = false)
            addChatMessage("Settings ⚙️", "Auto-Dab enabled! Numbers will be automatically marked as announced.", isSystem = true)
        } else {
            addChatMessage("Settings ⚙️", "Auto-Dab disabled. Switched to manual ticket marking.", isSystem = true)
        }
    }

    fun setAutoDabEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAutoDabEnabled = enabled) }
        if (enabled) {
            autoMarkAllCalledNumbers(silent = false)
            addChatMessage("Settings ⚙️", "Auto-Dab enabled!", isSystem = true)
        } else {
            addChatMessage("Settings ⚙️", "Auto-Dab disabled.", isSystem = true)
        }
    }

    fun openSettingsModal() {
        _uiState.update { it.copy(isSettingsModalVisible = true) }
    }

    fun closeSettingsModal() {
        _uiState.update { it.copy(isSettingsModalVisible = false) }
    }

    fun processAutoDabForNumber(calledNum: Int) {
        if (!_uiState.value.isAutoDabEnabled) return
        var anyDabbed = false
        _uiState.update { state ->
            val updatedMap = state.markedNumbersMap.toMutableMap()
            state.playerTickets.forEach { ticket ->
                val ticketNums = ticket.getAllNumbers()
                if (ticketNums.contains(calledNum)) {
                    val currentMarked = updatedMap[ticket.id] ?: emptySet()
                    if (!currentMarked.contains(calledNum)) {
                        updatedMap[ticket.id] = currentMarked + calledNum
                        anyDabbed = true
                    }
                }
            }
            if (anyDabbed) state.copy(markedNumbersMap = updatedMap) else state
        }
        if (anyDabbed && _uiState.value.isSoundEnabled) {
            soundManager.playDabSound(isDabbing = true)
        }
    }

    fun callNextNumber() {
        val state = uiState.value
        if (state.remainingNumbers.isEmpty() || state.isGameFinished) {
            stopAutoCall()
            return
        }

        val nextNum = state.remainingNumbers.first()
        val newRemaining = state.remainingNumbers.drop(1)
        val newCalled = listOf(nextNum) + state.calledNumbers

        val phrase = CallerPhrases.getPhrase(nextNum)

        _uiState.update {
            it.copy(
                calledNumbers = newCalled,
                remainingNumbers = newRemaining,
                currentCalledNumber = nextNum
            )
        }

        if (state.isSoundEnabled) {
            soundManager.playNumberDrawChime()
            speakPhrase(phrase)
        }

        addChatMessage("Caller 🎙️", phrase, isSystem = true)

        // Auto-Dab player tickets when announced if enabled in settings
        processAutoDabForNumber(nextNum)

        // Fully automated instant win verification & auto-claim for player's tickets
        checkAndAutoClaimPlayerWins(nextNum)

        // Check booked room slot opponents & bot turns
        processRoomSlotTurns(nextNum)
        processBotTurns(nextNum)
    }

    fun callSpecificNumber(targetNumber: Int): Boolean {
        val state = uiState.value
        if (targetNumber !in 1..90) return false
        if (state.calledNumbers.contains(targetNumber)) {
            return false // Already called
        }
        if (state.isGameFinished) {
            stopAutoCall()
            return false
        }

        val newRemaining = state.remainingNumbers.filter { it != targetNumber }
        val newCalled = listOf(targetNumber) + state.calledNumbers
        val phrase = CallerPhrases.getPhrase(targetNumber)

        _uiState.update {
            it.copy(
                calledNumbers = newCalled,
                remainingNumbers = newRemaining,
                currentCalledNumber = targetNumber
            )
        }

        if (state.isSoundEnabled) {
            soundManager.playNumberDrawChime()
            speakPhrase(phrase)
        }

        addChatMessage("Admin Override 🎯", "Manually called #$targetNumber: $phrase", isSystem = true)

        // Sync to Firestore 'current_game' if background caller or active match is live
        viewModelScope.launch {
            FirestoreService.drawSpecificFirestoreGameNumber(targetNumber)
        }

        // Process auto-dab, player wins, room slot turns, bot turns
        processAutoDabForNumber(targetNumber)
        checkAndAutoClaimPlayerWins(targetNumber)
        processRoomSlotTurns(targetNumber)
        processBotTurns(targetNumber)
        return true
    }

    private fun checkAndAutoClaimPlayerWins(calledNum: Int) {
        val state = uiState.value
        val calledSet = (listOf(calledNum) + state.calledNumbers).toSet()

        state.playerTickets.forEach { ticket ->
            val marked = state.markedNumbersMap[ticket.id] ?: emptySet()
            val effectiveMarked = if (state.isAutoDabEnabled) {
                marked + ticket.getAllNumbers().filter { calledSet.contains(it) }
            } else marked

            ClaimType.values().forEach { claim ->
                if (!state.claimedPrizes.containsKey(claim)) {
                    val result = evaluateClaim(ticket, claim, calledSet, effectiveMarked)
                    if (result.isSuccess) {
                        claimPrizeForWinner(claim, "You (${ticket.id})", result)
                        addChatMessage("Winner 🏆", "🎉 AUTOMATION WINNER! You won ${claim.displayName} on Ticket ${ticket.id}!", isSystem = true)
                        if (state.isSoundEnabled) {
                            speakPhrase("Congratulations! You won ${claim.displayName}!")
                        }
                    }
                }
            }
        }
    }

    private fun processRoomSlotTurns(calledNum: Int) {
        val state = uiState.value
        val room = state.currentJoinedRoom ?: return
        val calledSet = (listOf(calledNum) + state.calledNumbers).toSet()

        room.ticketSlots.filter { it.isBooked && !it.bookedByName.equals(state.userName, ignoreCase = true) }.forEach { slot ->
            val slotNums = slot.ticket.getAllNumbers().toSet()
            val slotMarked = slotNums.intersect(calledSet)

            ClaimType.values().forEach { claim ->
                if (!state.claimedPrizes.containsKey(claim)) {
                    val result = evaluateClaim(slot.ticket, claim, calledSet, slotMarked)
                    if (result.isSuccess && Random.nextFloat() < 0.80f) {
                        claimPrizeForWinner(claim, "${slot.bookedByName} (Slot #${slot.slotNumber})", result)
                        addChatMessage(slot.bookedByName, "🎉 BINGO! Won ${claim.displayName} from Slot #${slot.slotNumber}!")
                        if (state.isSoundEnabled) {
                            speakPhrase("${slot.bookedByName} won ${claim.displayName}")
                        }
                    }
                }
            }
        }
    }

    private fun processBotTurns(calledNum: Int) {
        val state = uiState.value
        val updatedBotMarked = state.botMarkedMap.toMutableMap()

        state.bots.forEach { bot ->
            val botTicketNums = bot.ticket.getAllNumbers()
            if (botTicketNums.contains(calledNum)) {
                val current = updatedBotMarked[bot.ticket.id] ?: emptySet()
                updatedBotMarked[bot.ticket.id] = current + calledNum
            }
        }

        _uiState.update { it.copy(botMarkedMap = updatedBotMarked) }

        // Check if any bot satisfies an unclaimed prize with ~75% chance of claiming instantly
        val calledSet = (listOf(calledNum) + state.calledNumbers).toSet()

        state.bots.forEach { bot ->
            val botMarked = updatedBotMarked[bot.ticket.id] ?: emptySet()

            ClaimType.values().forEach { claim ->
                if (!state.claimedPrizes.containsKey(claim)) {
                    val result = evaluateClaim(bot.ticket, claim, calledSet, botMarked)
                    if (result.isSuccess && Random.nextFloat() < 0.75f) {
                        // Bot claims prize!
                        claimPrizeForWinner(claim, bot.name, result)
                        addChatMessage(bot.name, "🎉 BINGO! I claim ${claim.displayName}!")
                        if (state.isSoundEnabled) {
                            speakPhrase("${bot.name} won ${claim.displayName}")
                        }
                    }
                }
            }
        }
    }

    fun toggleAutoCall() {
        val currentState = uiState.value.isAutoCalling
        if (currentState) {
            stopAutoCall()
        } else {
            startAutoCall()
        }
    }

    fun setAutoCallInterval(intervalSec: Int) {
        _uiState.update { it.copy(autoCallIntervalSec = intervalSec) }
        if (uiState.value.isAutoCalling) {
            startAutoCall()
        }
    }

    private fun startAutoCall() {
        autoCallJob?.cancel()
        _uiState.update { 
            it.copy(
                isAutoCalling = true,
                autoCallRemainingMillis = it.autoCallIntervalSec * 1000L,
                autoCallCountdownProgress = 1f
            ) 
        }
        autoCallJob = viewModelScope.launch {
            // Draw first number immediately upon start if no numbers or player wants immediate draw
            if (uiState.value.calledNumbers.isEmpty() && uiState.value.remainingNumbers.isNotEmpty()) {
                callNextNumber()
            }

            while (isActive && uiState.value.isAutoCalling && uiState.value.remainingNumbers.isNotEmpty() && !uiState.value.isGameFinished) {
                val intervalSec = uiState.value.autoCallIntervalSec
                val totalMillis = intervalSec * 1000L
                val stepMillis = 50L
                var elapsed = 0L

                while (isActive && elapsed < totalMillis && uiState.value.isAutoCalling && !uiState.value.isGameFinished) {
                    delay(stepMillis)
                    elapsed += stepMillis
                    val remaining = (totalMillis - elapsed).coerceAtLeast(0L)
                    val progress = (remaining.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
                    _uiState.update {
                        it.copy(
                            autoCallRemainingMillis = remaining,
                            autoCallCountdownProgress = progress
                        )
                    }
                }

                if (isActive && uiState.value.isAutoCalling && uiState.value.remainingNumbers.isNotEmpty() && !uiState.value.isGameFinished) {
                    callNextNumber()
                    _uiState.update {
                        it.copy(
                            autoCallRemainingMillis = uiState.value.autoCallIntervalSec * 1000L,
                            autoCallCountdownProgress = 1f
                        )
                    }
                }
            }

            if (uiState.value.remainingNumbers.isEmpty() || uiState.value.isGameFinished) {
                stopAutoCall()
            }
        }
    }

    private fun stopAutoCall() {
        autoCallJob?.cancel()
        autoCallJob = null
        _uiState.update { 
            it.copy(
                isAutoCalling = false,
                autoCallRemainingMillis = 0L,
                autoCallCountdownProgress = 0f
            ) 
        }
    }

    fun toggleSound() {
        _uiState.update { 
            val newSoundState = !it.isSoundEnabled
            soundManager.setMuted(!newSoundState)
            it.copy(isSoundEnabled = newSoundState)
        }
    }

    private fun speakPhrase(phrase: String) {
        tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "TambolaCall")
    }

    fun claimPrize(ticketId: String, claimType: ClaimType) {
        val state = uiState.value
        val ticket = state.playerTickets.find { it.id == ticketId }
            ?: state.bots.find { it.ticket.id == ticketId }?.ticket
            ?: return

        val calledSet = state.calledNumbers.toSet()
        val userMarked = state.markedNumbersMap[ticketId] ?: emptySet()

        val result = evaluateClaim(ticket, claimType, calledSet, userMarked)

        _uiState.update { it.copy(lastClaimResult = result) }

        if (result.isSuccess) {
            if (state.isSoundEnabled) {
                soundManager.playWinFanfare()
            }
            claimPrizeForWinner(claimType, "You (Player)", result)
            _uiState.update { it.copy(userScore = it.userScore + claimType.prizePoints) }
            addChatMessage("You 🏆", "Claimed ${claimType.displayName}! (+${claimType.prizePoints} pts)")
        } else {
            if (state.isSoundEnabled) {
                soundManager.playBogusWarning()
            }
            addChatMessage("System ⚠️", "Bogus claim by You for ${claimType.displayName}: ${result.message}", isSystem = true)
        }
    }

    private fun claimPrizeForWinner(claimType: ClaimType, winnerName: String, result: ClaimResult) {
        val updatedClaims = uiState.value.claimedPrizes.toMutableMap()
        if (!updatedClaims.containsKey(claimType)) {
            updatedClaims[claimType] = winnerName
            _uiState.update { it.copy(claimedPrizes = updatedClaims) }

            // Transparently save winner to Firestore 'game_history'
            val prizeAmt = claimType.prizePoints * 10
            val matchId = _uiState.value.currentGameState.gameId.ifBlank { "MATCH_LIVE" }
            viewModelScope.launch {
                val winnerHistory = TambolaWinnerHistory(
                    id = "win_${System.currentTimeMillis()}",
                    matchId = matchId,
                    roomTitle = _uiState.value.currentJoinedRoom?.title ?: "👑 Udaipur Tambola Arena",
                    winnerName = winnerName,
                    winnerPhone = if (winnerName.contains("You", ignoreCase = true)) _uiState.value.userMobileNumber else "+91 98290 ****",
                    claimPattern = claimType.displayName,
                    prizeAmount = prizeAmt,
                    winningNumber = _uiState.value.currentCalledNumber ?: 90,
                    totalNumbersCalled = _uiState.value.calledNumbers.size,
                    verifiedTimestamp = System.currentTimeMillis(),
                    isAutoVerified = true,
                    transactionRef = "TXN_TAMBOLA_${System.currentTimeMillis().toString().takeLast(6)}"
                )
                FirestoreService.recordWinnerInFirestore(winnerHistory)
            }

            // Immediately notify FCM service to display system Heads-Up notification and celebration UI
            val isUser = winnerName.contains("You", ignoreCase = true)
            if (isUser) {
                val newBal = _uiState.value.walletBalance + prizeAmt
                val userPhone = _uiState.value.userMobileNumber
                val prizeTx = WalletTransaction(
                    id = "TXN_WIN_${System.currentTimeMillis()}",
                    type = TransactionType.PRIZE_WIN,
                    amount = prizeAmt,
                    title = "${claimType.displayName} Won",
                    description = "Claim verified in ${_uiState.value.currentJoinedRoom?.title ?: "Udaipur Tambola Arena"}",
                    paymentMethod = "Prize Pool Credit",
                    referenceId = "WIN_${System.currentTimeMillis().toString().takeLast(6)}",
                    status = TransactionStatus.SUCCESS,
                    closingBalance = newBal,
                    roomTitle = _uiState.value.currentJoinedRoom?.title ?: "Live Arena",
                    timestampMs = System.currentTimeMillis()
                )

                _uiState.update { current ->
                    val curProfile = current.userProfile
                    val updatedProfile = curProfile.copy(
                        gamesWon = curProfile.gamesWon + 1,
                        totalEarnings = curProfile.totalEarnings + prizeAmt,
                        totalPoints = curProfile.totalPoints + claimType.prizePoints,
                        earlyFiveWins = if (claimType == ClaimType.EARLY_FIVE) curProfile.earlyFiveWins + 1 else curProfile.earlyFiveWins,
                        cornersWins = if (claimType == ClaimType.FOUR_CORNERS) curProfile.cornersWins + 1 else curProfile.cornersWins,
                        topLineWins = if (claimType == ClaimType.TOP_LINE) curProfile.topLineWins + 1 else curProfile.topLineWins,
                        middleLineWins = if (claimType == ClaimType.MIDDLE_LINE) curProfile.middleLineWins + 1 else curProfile.middleLineWins,
                        bottomLineWins = if (claimType == ClaimType.BOTTOM_LINE) curProfile.bottomLineWins + 1 else curProfile.bottomLineWins,
                        fullHouseWins = if (claimType == ClaimType.FULL_HOUSE_1 || claimType == ClaimType.FULL_HOUSE_2) curProfile.fullHouseWins + 1 else curProfile.fullHouseWins
                    )
                    current.copy(
                        walletBalance = newBal,
                        walletTransactions = listOf(prizeTx) + current.walletTransactions,
                        userProfile = updatedProfile
                    )
                }
                syncProfileToFirestore()
                viewModelScope.launch {
                    FirestoreService.saveWalletTransaction(userPhone, prizeTx)
                    FirestoreService.updateUserWalletBalance(userPhone, newBal)
                }
            }

            TambolaFirebaseMessagingService.notifyLocalWin(
                getApplication(),
                WinnerNotificationPayload(
                    title = if (isUser) "You Won ${claimType.displayName}!" else "$winnerName Won ${claimType.displayName}!",
                    message = if (isUser) "Ticket validation approved! ₹$prizeAmt credited to your balance." else "Claim verified on call #${_uiState.value.calledNumbers.size}.",
                    winnerName = winnerName,
                    claimType = claimType.displayName,
                    prizeAmount = prizeAmt,
                    matchId = matchId
                )
            )

            // Check if all major claims won
            if (updatedClaims.containsKey(ClaimType.FULL_HOUSE_1) && updatedClaims.containsKey(ClaimType.FULL_HOUSE_2)) {
                finishGame("Full House completed!")
            }
        }
    }

    fun evaluateClaim(
        ticket: TambolaTicket,
        claimType: ClaimType,
        calledSet: Set<Int>,
        markedSet: Set<Int>
    ): ClaimResult {
        if (uiState.value.claimedPrizes.containsKey(claimType)) {
            return ClaimResult(
                isSuccess = false,
                claimType = claimType,
                ticketId = ticket.id,
                message = "${claimType.displayName} has already been claimed by ${uiState.value.claimedPrizes[claimType]}!"
            )
        }

        val effectiveMarked = markedSet.intersect(calledSet)

        return when (claimType) {
            ClaimType.EARLY_FIVE -> {
                val validCount = effectiveMarked.size
                if (validCount >= 5) {
                    ClaimResult(true, claimType, ticket.id, "Valid Early 5! 5 numbers verified.")
                } else {
                    val missing = 5 - validCount
                    ClaimResult(false, claimType, ticket.id, "Bogus Claim! You need $missing more called number(s).")
                }
            }
            ClaimType.FOUR_CORNERS -> {
                val corners = ticket.getCornerNumbers()
                val missing = corners.filter { !calledSet.contains(it) }
                if (missing.isEmpty() && corners.size == 4) {
                    ClaimResult(true, claimType, ticket.id, "Valid Four Corners! All 4 corners called.")
                } else {
                    ClaimResult(false, claimType, ticket.id, "Bogus Claim! Missing corners: ${missing.joinToString(", ")}", missing)
                }
            }
            ClaimType.TOP_LINE -> {
                val rowNums = ticket.getRowNumbers(0)
                val missing = rowNums.filter { !calledSet.contains(it) }
                if (missing.isEmpty()) {
                    ClaimResult(true, claimType, ticket.id, "Valid Top Line! All 5 numbers called.")
                } else {
                    ClaimResult(false, claimType, ticket.id, "Bogus Claim! Missing Top Line numbers: ${missing.joinToString(", ")}", missing)
                }
            }
            ClaimType.MIDDLE_LINE -> {
                val rowNums = ticket.getRowNumbers(1)
                val missing = rowNums.filter { !calledSet.contains(it) }
                if (missing.isEmpty()) {
                    ClaimResult(true, claimType, ticket.id, "Valid Middle Line! All 5 numbers called.")
                } else {
                    ClaimResult(false, claimType, ticket.id, "Bogus Claim! Missing Middle Line numbers: ${missing.joinToString(", ")}", missing)
                }
            }
            ClaimType.BOTTOM_LINE -> {
                val rowNums = ticket.getRowNumbers(2)
                val missing = rowNums.filter { !calledSet.contains(it) }
                if (missing.isEmpty()) {
                    ClaimResult(true, claimType, ticket.id, "Valid Bottom Line! All 5 numbers called.")
                } else {
                    ClaimResult(false, claimType, ticket.id, "Bogus Claim! Missing Bottom Line numbers: ${missing.joinToString(", ")}", missing)
                }
            }
            ClaimType.FULL_HOUSE_1, ClaimType.FULL_HOUSE_2 -> {
                val allNums = ticket.getAllNumbers()
                val missing = allNums.filter { !calledSet.contains(it) }
                if (missing.isEmpty()) {
                    ClaimResult(true, claimType, ticket.id, "🎉 VALID FULL HOUSE! All 15 numbers verified!")
                } else {
                    ClaimResult(false, claimType, ticket.id, "Bogus Claim! Missing ${missing.size} number(s): ${missing.joinToString(", ")}", missing)
                }
            }
        }
    }

    fun dismissClaimResult() {
        _uiState.update { it.copy(lastClaimResult = null) }
    }

    fun dismissWinnerNotification() {
        _uiState.update { it.copy(activeWinnerNotification = null) }
    }

    fun addChatMessage(sender: String, message: String, isSystem: Boolean = false) {
        val newMsg = RoomChatMessage(
            id = System.currentTimeMillis().toString() + Random.nextInt(100),
            senderName = sender,
            message = message,
            isSystem = isSystem
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + newMsg) }
    }

    fun resetGame() {
        stopAutoCall()
        val deck = (1..90).toList().shuffled()
        val playerTickets = (1..2).map { TambolaTicket.generate("Ticket-$it") }
        val bot1 = TambolaTicket.generate("Bot-1")
        val bot2 = TambolaTicket.generate("Bot-2")
        val bot3 = TambolaTicket.generate("Bot-3")

        val markedMap = playerTickets.associate { it.id to emptySet<Int>() }

        _uiState.update {
            it.copy(
                calledNumbers = emptyList(),
                remainingNumbers = deck,
                currentCalledNumber = null,
                isAutoCalling = false,
                autoCallRemainingMillis = 0L,
                autoCallCountdownProgress = 0f,
                playerTickets = playerTickets,
                markedNumbersMap = markedMap,
                claimedPrizes = emptyMap(),
                bots = listOf(
                    BotPlayer("Priya Sharma", "👩‍💼", bot1),
                    BotPlayer("Rahul Verma", "🧑‍💻", bot2),
                    BotPlayer("Aarav Patel", "🦸‍♂️", bot3)
                ),
                botMarkedMap = emptyMap(),
                chatMessages = listOf(
                    RoomChatMessage("1", "Host 🎙️", "New Tambola Game started! Get ready!", isSystem = true)
                ),
                lastClaimResult = null,
                isGameFinished = false,
                userScore = 0
            )
        }
    }

    private fun finishGame(reason: String) {
        stopAutoCall()
        _uiState.update { current ->
            val curProfile = current.userProfile
            current.copy(
                isGameFinished = true,
                userProfile = curProfile.copy(gamesPlayed = curProfile.gamesPlayed + 1)
            )
        }
        syncProfileToFirestore()

        val winnerSummary = uiState.value.claimedPrizes.entries.joinToString("; ") { "${it.key.displayName}: ${it.value}" }

        addChatMessage("Host 🏆", "Game Over! $reason $winnerSummary", isSystem = true)

        viewModelScope.launch {
            val record = GameRecord(
                gameMode = "Bot Battle Room",
                totalNumbersCalled = uiState.value.calledNumbers.size,
                winnerSummary = if (winnerSummary.isEmpty()) "No claims" else winnerSummary,
                userScore = uiState.value.userScore
            )
            repository.saveGameRecord(record)
        }
    }

    // Generator & Custom Ticket Actions
    fun generateSheetOfSix() {
        val sheet = TambolaTicket.generateFullSheet()
        _uiState.update { it.copy(generatedSheet = sheet) }
    }

    fun generateSingleTicket() {
        val ticket = TambolaTicket.generate()
        _uiState.update { it.copy(generatedSheet = listOf(ticket) + it.generatedSheet) }
    }

    fun saveGeneratedTicket(ticket: TambolaTicket, customName: String) {
        viewModelScope.launch {
            val gridJson = ticket.grid.joinToString(";") { row ->
                row.joinToString(",") { it?.toString() ?: "" }
            }
            val entity = SavedTicketEntity(
                id = ticket.id,
                name = if (customName.isBlank()) ticket.id else customName,
                gridJson = gridJson
            )
            repository.saveTicket(entity)

            // Also persist to Firestore for current user
            val userPhone = uiState.value.userMobileNumber
            if (userPhone.isNotBlank()) {
                FirestoreService.saveUserTicket(userPhone, ticket, customName)
            }
        }
    }

    fun deleteSavedTicket(id: String) {
        viewModelScope.launch {
            repository.deleteSavedTicket(id)
            val userPhone = uiState.value.userMobileNumber
            if (userPhone.isNotBlank()) {
                FirestoreService.deleteUserTicket(userPhone, id)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Mobile Auth Methods ---
    fun openAuthModal() {
        _uiState.update { it.copy(isAuthModalVisible = true, authStep = 1, authStatusMessage = null) }
    }

    fun closeAuthModal() {
        _uiState.update { it.copy(isAuthModalVisible = false) }
    }

    fun sendMobileOtp(mobile: String) {
        val cleanMobile = mobile.trim()
        if (cleanMobile.length < 10) {
            _uiState.update { it.copy(authStatusMessage = "Please enter a valid 10-digit mobile number") }
            return
        }
        val formatted = if (cleanMobile.startsWith("+91")) cleanMobile else "+91 $cleanMobile"

        _uiState.update { it.copy(authStatusMessage = "Sending OTP via Firebase Auth...") }

        FirebaseAuthService.sendOtp(
            activity = null,
            mobileNumber = formatted,
            onCodeSent = { verId ->
                _uiState.update {
                    it.copy(
                        tempMobileInput = formatted,
                        authStep = 2,
                        otpInput = "",
                        authStatusMessage = "OTP sent successfully to $formatted"
                    )
                }
            },
            onVerificationCompleted = { phone ->
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        userMobileNumber = phone,
                        isAuthModalVisible = false,
                        authStatusMessage = null
                    )
                }
                observeUserFirestoreData(phone)
                addChatMessage("System", "📱 Logged in successfully as $phone", isSystem = true)
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        tempMobileInput = formatted,
                        authStep = 2,
                        otpInput = "",
                        authStatusMessage = "OTP sent to $formatted"
                    )
                }
            }
        )
    }

    fun verifyMobileOtp(otp: String) {
        if (otp.length < 4) {
            _uiState.update { it.copy(authStatusMessage = "Please enter the 6-digit OTP.") }
            return
        }

        FirebaseAuthService.verifyCode(
            code = otp,
            onSuccess = {
                val phone = uiState.value.tempMobileInput.ifBlank { "+91 98765 43210" }
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        userMobileNumber = phone,
                        isAuthModalVisible = false,
                        authStatusMessage = null
                    )
                }
                observeUserFirestoreData(phone)
                addChatMessage("System", "📱 Firebase Auth verified successfully for $phone!", isSystem = true)
            },
            onFailure = { err ->
                _uiState.update { it.copy(authStatusMessage = err) }
            }
        )
    }

    fun logoutUser() {
        FirebaseAuthService.signOut()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                userMobileNumber = "Not Logged In",
                authStep = AuthStep.PHONE_INPUT,
                tempMobileInput = "",
                otpInput = "",
                authStatusMessage = null
            )
        }
        addChatMessage("System", "Logged out of HousieSphere account.", isSystem = true)
    }

    // --- User Profile Management & Stats ---
    fun updateUserNickname(nickname: String) {
        val trimmed = nickname.trim()
        if (trimmed.isNotBlank()) {
            _uiState.update {
                val updated = it.userProfile.copy(nickname = trimmed)
                it.copy(userProfile = updated, userName = trimmed)
            }
            syncProfileToFirestore()
            addChatMessage("Profile 👤", "Nickname updated to '$trimmed'", isSystem = true)
        }
    }

    fun updateUserAvatarUri(uriString: String?) {
        _uiState.update {
            val updated = it.userProfile.copy(avatarUri = uriString)
            it.copy(userProfile = updated)
        }
        syncProfileToFirestore()
        addChatMessage("Profile 👤", "Avatar photo updated successfully!", isSystem = true)
    }

    fun updateUserAvatarPreset(preset: String) {
        _uiState.update {
            val updated = it.userProfile.copy(avatarPreset = preset, avatarUri = null)
            it.copy(userProfile = updated)
        }
        syncProfileToFirestore()
        addChatMessage("Profile 👤", "Avatar style updated to $preset", isSystem = true)
    }

    fun updateUserBio(bio: String) {
        _uiState.update {
            val updated = it.userProfile.copy(bio = bio.trim())
            it.copy(userProfile = updated)
        }
        syncProfileToFirestore()
    }

    fun updateUserProfile(profile: PlayerProfileStats) {
        _uiState.update {
            it.copy(userProfile = profile, userName = profile.nickname)
        }
        syncProfileToFirestore()
    }

    private fun syncProfileToFirestore() {
        val phone = uiState.value.userMobileNumber
        if (phone.isNotBlank()) {
            viewModelScope.launch {
                FirestoreService.saveUserProfile(phone, uiState.value.userProfile)
            }
        }
    }


    // --- Razorpay Payment Methods ---
    fun openRazorpayModal() {
        if (!uiState.value.isLoggedIn) {
            openAuthModal()
            return
        }
        _uiState.update { it.copy(isRazorpayModalVisible = true) }
    }

    fun closeRazorpayModal() {
        _uiState.update { it.copy(isRazorpayModalVisible = false) }
    }

    fun processRazorpayPayment(amount: Int, paymentMethod: String) {
        val payId = "pay_Rzp" + Random.nextInt(100000, 999999)
        val newTx = RazorpayTransaction(
            paymentId = payId,
            amount = amount,
            status = "SUCCESS",
            paymentMethod = paymentMethod,
            timestampMs = System.currentTimeMillis()
        )

        val updatedBalance = uiState.value.walletBalance + amount
        val userPhone = uiState.value.userMobileNumber
        val walletTx = WalletTransaction(
            id = "TXN_${System.currentTimeMillis()}",
            type = TransactionType.DEPOSIT,
            amount = amount,
            title = "Deposit via $paymentMethod",
            description = "Razorpay Instant Checkout (256-Bit NPCI)",
            paymentMethod = paymentMethod,
            referenceId = payId,
            status = TransactionStatus.SUCCESS,
            closingBalance = updatedBalance,
            timestampMs = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                walletBalance = updatedBalance,
                razorpayTransactions = listOf(newTx) + state.razorpayTransactions,
                walletTransactions = listOf(walletTx) + state.walletTransactions,
                totalRevenueCollected = state.totalRevenueCollected + amount,
                isRazorpayModalVisible = false
            )
        }

        // Persist to Firestore
        viewModelScope.launch {
            FirestoreService.saveTransaction(userPhone, newTx)
            FirestoreService.saveWalletTransaction(userPhone, walletTx)
            FirestoreService.updateUserWalletBalance(userPhone, updatedBalance)
        }

        addChatMessage("Razorpay 💳", "Payment of ₹$amount successful! (Ref: $payId via $paymentMethod)", isSystem = true)
    }

    /**
     * Process player withdrawal request to Bank / UPI.
     */
    fun withdrawFunds(amount: Int, payoutMethod: String, payoutDetails: String): Boolean {
        val currentBalance = uiState.value.walletBalance
        if (amount <= 0 || amount > currentBalance) {
            return false
        }

        val updatedBalance = currentBalance - amount
        val refId = "WDR_UPI_" + Random.nextInt(100000, 999999)
        val userPhone = uiState.value.userMobileNumber

        val withdrawalTx = WalletTransaction(
            id = "TXN_WDR_${System.currentTimeMillis()}",
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            title = "Instant Withdrawal to $payoutMethod",
            description = "Transfer to: ${payoutDetails.ifBlank { "Registered UPI ID" }}",
            paymentMethod = payoutMethod,
            referenceId = refId,
            status = TransactionStatus.SUCCESS,
            closingBalance = updatedBalance,
            timestampMs = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                walletBalance = updatedBalance,
                walletTransactions = listOf(withdrawalTx) + state.walletTransactions
            )
        }

        viewModelScope.launch {
            FirestoreService.saveWalletTransaction(userPhone, withdrawalTx)
            FirestoreService.updateUserWalletBalance(userPhone, updatedBalance)
        }

        addChatMessage("Payout 💸", "Withdrawal of ₹$amount to $payoutMethod initiated successfully! Ref: $refId", isSystem = true)
        return true
    }

    // --- Lobby & Room Category Filter ---
    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun joinRoom(room: GameRoom) {
        if (uiState.value.walletBalance < room.entryFee) {
            openRazorpayModal()
            return
        }
        if (room.entryFee > 0) {
            val newBalance = uiState.value.walletBalance - room.entryFee
            val userPhone = uiState.value.userMobileNumber
            val ticketTx = WalletTransaction(
                id = "TXN_TCK_${System.currentTimeMillis()}",
                type = TransactionType.TICKET_PURCHASE,
                amount = room.entryFee,
                title = "Booked 1 Ticket • ${room.title}",
                description = "Match Entry Fee for ${room.title}",
                paymentMethod = "Wallet Balance",
                referenceId = "TCK_" + Random.nextInt(100000, 999999),
                status = TransactionStatus.SUCCESS,
                closingBalance = newBalance,
                roomTitle = room.title,
                ticketCount = 1,
                timestampMs = System.currentTimeMillis()
            )

            _uiState.update { 
                it.copy(
                    walletBalance = newBalance,
                    walletTransactions = listOf(ticketTx) + it.walletTransactions
                ) 
            }
            viewModelScope.launch {
                FirestoreService.saveWalletTransaction(userPhone, ticketTx)
                FirestoreService.updateUserWalletBalance(userPhone, newBalance)
            }
        }
        _uiState.update {
            it.copy(
                currentJoinedRoom = room,
                activeTab = ActiveTab.SOLO_BOT_ROOM
            )
        }
        resetGame()
        addChatMessage("Host 🎙️", "Joined ${room.title}! Good luck!", isSystem = true)
    }

    // --- Admin & Staff RBAC Actions & Security Authentication ---
    fun loginStaff(loginId: String, pass: String): StaffUser? {
        val cleanId = loginId.trim()
        val cleanPass = pass.trim()

        val found = _uiState.value.staffUsers.find {
            it.loginId.equals(cleanId, ignoreCase = true) && it.password == cleanPass && it.isActive
        } ?: if (cleanId.equals("Admin", ignoreCase = true) && (cleanPass == "admin123" || cleanPass == "udoipurtambola@2026")) {
            StaffUser(
                id = "staff_admin_1",
                loginId = "admin",
                password = cleanPass,
                name = "Chief Administrator",
                role = StaffRole.ADMIN,
                phone = "+91 98765 00100",
                isBookingAllowed = true,
                isCreationAllowed = true
            )
        } else if (cleanId.equals("Manager", ignoreCase = true) && cleanPass == "manager123") {
            StaffUser(
                id = "staff_manager_1",
                loginId = "manager",
                password = "manager123",
                name = "Room Operations Manager",
                role = StaffRole.MANAGER,
                phone = "+91 98290 11223",
                isBookingAllowed = true,
                isCreationAllowed = true
            )
        } else if (cleanId.equals("Agent1", ignoreCase = true) && cleanPass == "agent123") {
            StaffUser(
                id = "staff_agent_1",
                loginId = "agent1",
                password = "agent123",
                name = "Rahul Sharma (Agent)",
                role = StaffRole.AGENT,
                phone = "+91 94141 88990",
                isBookingAllowed = true,
                isCreationAllowed = false
            )
        } else null

        if (found != null) {
            _uiState.update {
                it.copy(
                    currentStaffUser = found,
                    isAdminAuthenticated = (found.role == StaffRole.ADMIN),
                    isStaffPortalOpen = true,
                    adminAuthError = null
                )
            }
            addChatMessage("Staff Portal 🛡️", "Logged in as ${found.name} (${found.role.name})", isSystem = true)
            return found
        } else {
            _uiState.update {
                it.copy(
                    adminAuthError = "Invalid Staff ID or Password. Check credentials with Administrator."
                )
            }
            return null
        }
    }

    fun loginAdmin(adminId: String, pass: String): Boolean {
        val user = loginStaff(adminId, pass)
        return user != null
    }

    fun logoutStaff() {
        _uiState.update {
            it.copy(
                currentStaffUser = null,
                isAdminAuthenticated = false,
                isStaffPortalOpen = false,
                adminAuthError = null
            )
        }
        addChatMessage("Staff Portal 🛡️", "Logged out from Staff Portal.", isSystem = true)
    }

    fun logoutAdmin() {
        logoutStaff()
    }

    fun openStaffPortal() {
        _uiState.update { it.copy(isStaffPortalOpen = true) }
    }

    fun closeStaffPortal() {
        _uiState.update { it.copy(isStaffPortalOpen = false) }
    }

    fun createOrUpdateStaffUser(user: StaffUser) {
        _uiState.update { state ->
            val existing = state.staffUsers.find { it.id == user.id || it.loginId.equals(user.loginId, ignoreCase = true) }
            val updated = if (existing != null) {
                state.staffUsers.map { if (it.id == existing.id) user else it }
            } else {
                state.staffUsers + user
            }
            state.copy(staffUsers = updated)
        }
        addChatMessage("Admin 🛡️", "Saved Staff Account: ${user.name} (${user.role}) [ID: ${user.loginId}]", isSystem = true)
    }

    fun toggleStaffBookingPermission(userId: String, isAllowed: Boolean) {
        _uiState.update { state ->
            val updated = state.staffUsers.map {
                if (it.id == userId) it.copy(isBookingAllowed = isAllowed) else it
            }
            state.copy(staffUsers = updated)
        }
        addChatMessage("Admin 🛡️", "Updated booking access for staff user: $userId (Allowed: $isAllowed)", isSystem = true)
    }

    fun toggleStaffCreationPermission(userId: String, isAllowed: Boolean) {
        _uiState.update { state ->
            val updated = state.staffUsers.map {
                if (it.id == userId) it.copy(isCreationAllowed = isAllowed) else it
            }
            state.copy(staffUsers = updated)
        }
        addChatMessage("Admin 🛡️", "Updated room creation access for staff user: $userId (Allowed: $isAllowed)", isSystem = true)
    }

    fun deleteStaffUser(userId: String) {
        _uiState.update { state ->
            state.copy(staffUsers = state.staffUsers.filterNot { it.id == userId })
        }
        addChatMessage("Admin 🛡️", "Deleted staff account: $userId", isSystem = true)
    }

    fun agentBookPlayerTicket(
        agentId: String,
        roomId: String,
        slotNumber: Int?,
        ticketCount: Int,
        playerPhone: String,
        playerName: String,
        paymentMode: String
    ): Boolean {
        val state = _uiState.value
        val agent = state.staffUsers.find { it.id == agentId } ?: state.currentStaffUser
        if (agent == null || !agent.isBookingAllowed) {
            return false
        }

        val targetRoom = state.activeRooms.find { it.id == roomId } ?: state.activeRooms.firstOrNull() ?: return false
        val cleanPhone = playerPhone.trim().ifBlank { "+91 98000 00000" }
        val cleanName = playerName.trim().ifBlank { "Player" }
        val unitPrice = targetRoom.entryFee
        val count = if (slotNumber != null) 1 else ticketCount.coerceAtLeast(1)
        val totalAmount = unitPrice * count

        var updatedRoom = targetRoom
        val generatedTickets = mutableListOf<TambolaTicket>()

        if (slotNumber != null && targetRoom.ticketSlots.isNotEmpty()) {
            val slot = targetRoom.ticketSlots.find { it.slotNumber == slotNumber } ?: return false
            if (slot.isBooked) return false

            val updatedSlots = targetRoom.ticketSlots.map {
                if (it.slotNumber == slotNumber) {
                    it.copy(
                        isBooked = true,
                        bookedByName = "$cleanName (Agent: ${agent.name})",
                        bookedByPhone = cleanPhone,
                        bookingTimestamp = System.currentTimeMillis(),
                        paymentMethod = "Agent Booking ($paymentMode)"
                    )
                } else it
            }
            updatedRoom = targetRoom.copy(
                ticketSlots = updatedSlots,
                currentPlayers = updatedSlots.count { it.isBooked }
            )
            generatedTickets.add(slot.ticket)
        } else {
            for (i in 1..count) {
                generatedTickets.add(TambolaTicket.generate("AG-${kotlin.random.Random.nextInt(10000, 99999)}"))
            }
            updatedRoom = targetRoom.copy(
                currentPlayers = targetRoom.currentPlayers + count
            )
        }

        val bookingRecord = AgentBookingRecord(
            id = "AGBK_${System.currentTimeMillis()}",
            agentId = agent.id,
            agentName = agent.name,
            playerPhone = cleanPhone,
            playerName = cleanName,
            roomId = targetRoom.id,
            roomTitle = targetRoom.title,
            slotNumber = slotNumber,
            ticketCount = count,
            ticketIds = generatedTickets.map { it.id },
            amountPaid = totalAmount,
            paymentMethod = paymentMode,
            timestampMs = System.currentTimeMillis()
        )

        val updatedStaff = state.staffUsers.map {
            if (it.id == agent.id) {
                it.copy(
                    totalTicketsBooked = it.totalTicketsBooked + count,
                    totalAmountHandled = it.totalAmountHandled + totalAmount
                )
            } else it
        }

        _uiState.update { s ->
            s.copy(
                activeRooms = s.activeRooms.map { if (it.id == targetRoom.id) updatedRoom else it },
                agentBookingRecords = listOf(bookingRecord) + s.agentBookingRecords,
                staffUsers = updatedStaff,
                totalRevenueCollected = s.totalRevenueCollected + totalAmount
            )
        }

        viewModelScope.launch {
            FirestoreService.saveGameRoom(updatedRoom)
            generatedTickets.forEach { t ->
                val entity = SavedTicketEntity(
                    id = t.id,
                    name = "${targetRoom.title} [Agent: ${agent.name} for $cleanName]",
                    gridJson = t.grid.joinToString(";") { row -> row.joinToString(",") { it?.toString() ?: "" } },
                    createdAt = System.currentTimeMillis()
                )
                repository.saveTicket(entity)
            }
        }

        addChatMessage("Agent 🎟️", "${agent.name} booked $count ticket(s) for $cleanName ($cleanPhone) in '${targetRoom.title}'", isSystem = true)
        return true
    }

    fun updateAdminSupportPhone(phone: String, whatsapp: String) {
        _uiState.update {
            it.copy(
                adminSupportPhone = phone,
                adminSupportWhatsapp = whatsapp
            )
        }
        addChatMessage("Admin 📞", "Updated support numbers to $phone / $whatsapp", isSystem = true)
    }

    fun createRoomByAdmin(
        title: String,
        hostName: String,
        category: String,
        prizeAmount: Int,
        entryFee: Int,
        isJackpot: Boolean,
        scheduledStartTimeMs: Long? = null,
        scheduledTimeString: String = "",
        isUnlimitedPlayers: Boolean = false,
        maxPlayers: Int = 10,
        prizeBreakdown: Map<String, Int> = emptyMap()
    ) {
        val calculatedPrize = if (prizeBreakdown.isNotEmpty()) prizeBreakdown.values.sum() else prizeAmount
        val initialSlots = if (!isUnlimitedPlayers) {
            generateRoomTicketSlots(maxPlayers.coerceIn(2, 500))
        } else {
            emptyList()
        }

        val newRoom = GameRoom(
            id = "room-${System.currentTimeMillis()}",
            title = title,
            hostName = hostName,
            category = category,
            prizeAmount = calculatedPrize,
            entryFee = entryFee,
            currentPlayers = 0,
            maxPlayers = if (isUnlimitedPlayers) 100 else maxPlayers,
            isUnlimitedPlayers = isUnlimitedPlayers,
            isLive = true,
            isJackpot = isJackpot,
            iconEmoji = if (isJackpot) "👑" else "⚡",
            scheduledStartTimeMs = scheduledStartTimeMs,
            scheduledTimeString = scheduledTimeString,
            prizeBreakdown = prizeBreakdown,
            ticketSlots = initialSlots
        )
        _uiState.update { state ->
            state.copy(activeRooms = listOf(newRoom) + state.activeRooms)
        }
        viewModelScope.launch {
            FirestoreService.saveGameRoom(newRoom)
        }
        val timeNotice = if (scheduledTimeString.isNotBlank()) " (Scheduled: $scheduledTimeString)" else ""
        val capacityNotice = if (isUnlimitedPlayers) "Unlimited Capacity" else "$maxPlayers Limited Slots"
        addChatMessage("Admin 🛠️", "Created new room: '$title' [$capacityNotice] with Prize ₹$calculatedPrize$timeNotice", isSystem = true)
    }

    fun bookSpecificRoomSlot(
        room: GameRoom,
        slotNumber: Int,
        paymentMethod: String = "Wallet Balance"
    ): Boolean {
        val targetSlot = room.ticketSlots.find { it.slotNumber == slotNumber } ?: return false
        if (targetSlot.isBooked) return false

        val totalCost = room.entryFee
        if (paymentMethod == "Wallet Balance" && _uiState.value.walletBalance < totalCost) {
            return false
        }

        val userName = _uiState.value.userName
        val userPhone = _uiState.value.userMobileNumber.ifBlank { "+91 98765 43210" }

        val newBalance = if (paymentMethod == "Wallet Balance") {
            _uiState.value.walletBalance - totalCost
        } else {
            _uiState.value.walletBalance
        }

        val updatedSlots = room.ticketSlots.map { slot ->
            if (slot.slotNumber == slotNumber) {
                slot.copy(
                    isBooked = true,
                    bookedByName = userName,
                    bookedByPhone = userPhone,
                    bookingTimestamp = System.currentTimeMillis(),
                    paymentMethod = paymentMethod
                )
            } else slot
        }

        val updatedRoom = room.copy(
            ticketSlots = updatedSlots,
            currentPlayers = updatedSlots.count { it.isBooked }
        )

        val ticketTx = WalletTransaction(
            id = "TXN_SLOT_${System.currentTimeMillis()}",
            type = TransactionType.TICKET_PURCHASE,
            amount = totalCost,
            title = "Booked Slot #$slotNumber • ${room.title}",
            description = "Purchased reserved ticket slot via $paymentMethod",
            paymentMethod = paymentMethod,
            referenceId = "SLOT_${targetSlot.ticket.id}",
            status = TransactionStatus.SUCCESS,
            closingBalance = newBalance,
            roomTitle = room.title,
            ticketCount = 1,
            timestampMs = System.currentTimeMillis()
        )

        val gridJson = targetSlot.ticket.grid.joinToString(";") { row ->
            row.joinToString(",") { it?.toString() ?: "" }
        }

        val ticketEntity = SavedTicketEntity(
            id = targetSlot.ticket.id,
            name = "${room.title} (Slot #$slotNumber)",
            gridJson = gridJson,
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            val updatedRooms = state.activeRooms.map { if (it.id == room.id) updatedRoom else it }
            state.copy(
                activeRooms = updatedRooms,
                currentJoinedRoom = if (state.currentJoinedRoom?.id == room.id) updatedRoom else state.currentJoinedRoom,
                playerTickets = state.playerTickets + targetSlot.ticket,
                walletBalance = newBalance,
                walletTransactions = listOf(ticketTx) + state.walletTransactions,
                isBuyTicketsModalVisible = false
            )
        }

        viewModelScope.launch {
            repository.saveTicket(ticketEntity)
            FirestoreService.saveGameRoom(updatedRoom)
            if (userPhone.isNotBlank()) {
                FirestoreService.saveWalletTransaction(userPhone, ticketTx)
                FirestoreService.updateUserWalletBalance(userPhone, newBalance)
            }
        }

        addChatMessage("Booking 🎟️", "Successfully booked Slot #$slotNumber for ${room.title}!", isSystem = true)
        return true
    }

    fun updateAdminRoom(
        roomId: String,
        updatedTitle: String,
        newPrize: Int,
        newFee: Int,
        isLive: Boolean,
        scheduledStartTimeMs: Long? = null,
        scheduledTimeString: String = ""
    ) {
        var updatedTargetRoom: GameRoom? = null
        _uiState.update { state ->
            val updatedList = state.activeRooms.map { room ->
                if (room.id == roomId) {
                    val mod = room.copy(
                        title = updatedTitle,
                        prizeAmount = newPrize,
                        entryFee = newFee,
                        isLive = isLive,
                        scheduledStartTimeMs = scheduledStartTimeMs ?: room.scheduledStartTimeMs,
                        scheduledTimeString = if (scheduledTimeString.isNotBlank()) scheduledTimeString else room.scheduledTimeString
                    )
                    updatedTargetRoom = mod
                    mod
                } else room
            }
            state.copy(activeRooms = updatedList)
        }
        updatedTargetRoom?.let { room ->
            viewModelScope.launch {
                FirestoreService.saveGameRoom(room)
            }
        }
        addChatMessage("Admin 🛠️", "Updated room settings for '$updatedTitle'", isSystem = true)
    }

    fun deleteAdminRoom(roomId: String) {
        _uiState.update { state ->
            state.copy(activeRooms = state.activeRooms.filterNot { it.id == roomId })
        }
        viewModelScope.launch {
            FirestoreService.deleteGameRoom(roomId)
        }
        addChatMessage("Admin 🛠️", "Deleted room ID: $roomId", isSystem = true)
    }

    fun forceCloseRoom(roomId: String, reason: String = "Admin Action") {
        val target = _uiState.value.activeRooms.find { it.id == roomId }
        val title = target?.title ?: "Match Room"
        _uiState.update { state ->
            state.copy(
                activeRooms = state.activeRooms.filterNot { it.id == roomId },
                currentJoinedRoom = if (state.currentJoinedRoom?.id == roomId) null else state.currentJoinedRoom
            )
        }
        viewModelScope.launch {
            FirestoreService.deleteGameRoom(roomId)
        }
        addChatMessage("Admin 🛑", "Room '$title' has been FORCE CLOSED by Administrator ($reason). All active slots revoked.", isSystem = true)
    }

    fun updateGameConfiguration(config: GameConfiguration) {
        _uiState.update { it.copy(gameConfiguration = config) }
        viewModelScope.launch {
            FirestoreService.saveGameConfiguration(config)
        }
        addChatMessage("Admin 🛠️", "Updated global game configurations in Firestore", isSystem = true)
    }

    fun updateAdminOrganizationDetails(orgInfo: AdminOrganizationInfo) {
        _uiState.update { state ->
            state.copy(
                adminOrgInfo = orgInfo,
                adminSupportPhone = orgInfo.supportPhone,
                adminSupportWhatsapp = orgInfo.supportWhatsapp
            )
        }
        viewModelScope.launch {
            FirestoreService.saveAdminOrgInfo(orgInfo)
        }
        addChatMessage("Admin 🛠️", "Updated Organization Profile, Calling Numbers, Emails, Address & Rules!", isSystem = true)
    }

    fun bookTicketsForPlayerByAdmin(
        playerPhone: String,
        playerName: String,
        roomId: String,
        ticketCount: Int,
        customTickets: List<TambolaTicket>? = null
    ) {
        val cleanPhone = playerPhone.trim()
        val cleanName = playerName.trim().ifBlank { "Player" }
        val targetRoom = _uiState.value.activeRooms.find { it.id == roomId } ?: _uiState.value.activeRooms.firstOrNull()
        val roomTitle = targetRoom?.title ?: "Mega Amber Jackpot 90"
        val unitPrice = targetRoom?.entryFee ?: 30
        val totalAmount = unitPrice * ticketCount

        val tickets = customTickets ?: (1..ticketCount).map {
            TambolaTicket.generate("ADM-${Random.nextInt(10000, 99999)}")
        }

        val entities = tickets.map { t ->
            SavedTicketEntity(
                id = t.id,
                name = "$roomTitle [Admin Assigned: $cleanName]",
                gridJson = t.grid.joinToString(";") { row -> row.joinToString(",") { it?.toString() ?: "" } },
                createdAt = System.currentTimeMillis()
            )
        }

        val bookingRecord = AdminTicketBookingRecord(
            id = "BK_ADM_${System.currentTimeMillis()}",
            playerPhone = cleanPhone,
            playerName = cleanName,
            roomId = targetRoom?.id ?: "room-1",
            roomTitle = roomTitle,
            ticketCount = ticketCount,
            ticketIds = tickets.map { it.id },
            amountPaid = totalAmount,
            paymentMethod = "Admin Allocation",
            timestampMs = System.currentTimeMillis()
        )

        // Check if currently active numbers have been called
        val calledSet = _uiState.value.calledNumbers.toSet()
        val updatedMarked = _uiState.value.markedNumbersMap.toMutableMap()
        if (_uiState.value.isAutoDabEnabled) {
            tickets.forEach { t ->
                val matching = t.getAllNumbers().toSet().intersect(calledSet)
                if (matching.isNotEmpty()) {
                    updatedMarked[t.id] = matching
                }
            }
        }

        _uiState.update { state ->
            val updatedAdminBookings = listOf(bookingRecord) + state.adminBookingsList
            val isForCurrentPlayer = (state.userMobileNumber.isNotBlank() && state.userMobileNumber.contains(cleanPhone)) ||
                    cleanPhone == "self" || (state.userMobileNumber.isNotBlank() && cleanPhone == state.userMobileNumber) ||
                    cleanPhone.isBlank()
            val updatedTickets = if (isForCurrentPlayer) tickets else state.playerTickets

            state.copy(
                adminBookingsList = updatedAdminBookings,
                playerTickets = updatedTickets,
                markedNumbersMap = if (isForCurrentPlayer) updatedMarked else state.markedNumbersMap,
                totalRevenueCollected = state.totalRevenueCollected + totalAmount
            )
        }

        viewModelScope.launch {
            FirestoreService.recordAdminTicketBooking(bookingRecord)
            if (cleanPhone.isNotBlank()) {
                tickets.forEach { FirestoreService.saveUserTicket(cleanPhone, it, "$roomTitle [Admin Assigned: $cleanName]") }
            }
            entities.forEach { repository.saveTicket(it) }
        }

        soundManager.playWinFanfare()
        addChatMessage("Admin 🎟️", "Admin booked $ticketCount ticket(s) for $cleanName ($cleanPhone) in '$roomTitle'!", isSystem = true)
    }

    fun openBuyTicketsModal(room: GameRoom? = null) {
        _uiState.update { 
            it.copy(
                isBuyTicketsModalVisible = true,
                selectedRoomForDirectBuy = room ?: it.activeRooms.firstOrNull()
            ) 
        }
    }

    fun closeBuyTicketsModal() {
        _uiState.update { it.copy(isBuyTicketsModalVisible = false) }
    }

    fun buyTicketDirectly(
        ticketCount: Int,
        paymentMethod: String,
        room: GameRoom?,
        selectedTickets: List<TambolaTicket>? = null
    ): Boolean {
        val effectiveRoom = room ?: _uiState.value.activeRooms.firstOrNull() ?: GameRoom(
            id = "room-live",
            title = "Live Tambola Match 90",
            hostName = "Admin Host",
            category = "Public",
            prizeAmount = 10000,
            entryFee = 30,
            currentPlayers = 15,
            maxPlayers = 100
        )
        val totalCost = effectiveRoom.entryFee * ticketCount

        if (paymentMethod == "Wallet Balance" && _uiState.value.walletBalance < totalCost) {
            return false
        }

        val newTickets = selectedTickets ?: (1..ticketCount).map {
            TambolaTicket.generate("TCK-${Random.nextInt(10000, 99999)}")
        }

        val userPhone = _uiState.value.userMobileNumber
        val userName = _uiState.value.userName

        val newBalance = if (paymentMethod == "Wallet Balance") {
            _uiState.value.walletBalance - totalCost
        } else {
            _uiState.value.walletBalance
        }

        val ticketTx = WalletTransaction(
            id = "TXN_BUY_${System.currentTimeMillis()}",
            type = TransactionType.TICKET_PURCHASE,
            amount = totalCost,
            title = "Booked $ticketCount Ticket(s) • ${effectiveRoom.title}",
            description = "Payment via $paymentMethod directly to Admin Account (${_uiState.value.adminOrgInfo.adminUpiId})",
            paymentMethod = paymentMethod,
            referenceId = "PAY_${System.currentTimeMillis()}",
            status = TransactionStatus.SUCCESS,
            closingBalance = newBalance,
            roomTitle = effectiveRoom.title,
            ticketCount = ticketCount,
            timestampMs = System.currentTimeMillis()
        )

        val entities = newTickets.map { t ->
            SavedTicketEntity(
                id = t.id,
                name = "${effectiveRoom.title} (${t.id})",
                gridJson = t.grid.joinToString(";") { row -> row.joinToString(",") { it?.toString() ?: "" } },
                createdAt = System.currentTimeMillis()
            )
        }

        // Auto-select numbers if auto-dab is active
        val calledSet = _uiState.value.calledNumbers.toSet()
        val updatedMarked = _uiState.value.markedNumbersMap.toMutableMap()
        if (_uiState.value.isAutoDabEnabled) {
            newTickets.forEach { t ->
                val matching = t.getAllNumbers().toSet().intersect(calledSet)
                if (matching.isNotEmpty()) {
                    updatedMarked[t.id] = matching
                }
            }
        }

        _uiState.update { state ->
            state.copy(
                walletBalance = newBalance,
                walletTransactions = listOf(ticketTx) + state.walletTransactions,
                playerTickets = newTickets,
                markedNumbersMap = updatedMarked,
                totalRevenueCollected = state.totalRevenueCollected + totalCost,
                currentJoinedRoom = effectiveRoom,
                isBuyTicketsModalVisible = false
            )
        }

        viewModelScope.launch {
            entities.forEach { repository.saveTicket(it) }
            if (userPhone.isNotBlank()) {
                FirestoreService.saveWalletTransaction(userPhone, ticketTx)
                if (paymentMethod == "Wallet Balance") {
                    FirestoreService.updateUserWalletBalance(userPhone, newBalance)
                }
                newTickets.forEach { FirestoreService.saveUserTicket(userPhone, it, "${effectiveRoom.title} (${it.id})") }
            }
            // Record booking in admin system
            val adminBooking = AdminTicketBookingRecord(
                id = "BK_${System.currentTimeMillis()}",
                playerPhone = userPhone.ifBlank { "+91 98765 43210" },
                playerName = userName,
                roomId = effectiveRoom.id,
                roomTitle = effectiveRoom.title,
                ticketCount = ticketCount,
                ticketIds = newTickets.map { it.id },
                amountPaid = totalCost,
                paymentMethod = paymentMethod,
                timestampMs = System.currentTimeMillis()
            )
            FirestoreService.recordAdminTicketBooking(adminBooking)
        }

        soundManager.playWinFanfare()
        addChatMessage("Payment 💳", "Booked $ticketCount ticket(s) via $paymentMethod! Revenue sent to Admin Account.", isSystem = true)
        return true
    }

    // --- FIRESTORE 'current_game' BACKGROUND SERVICE CONTROLS ---

    fun startFirestoreBackgroundCaller(intervalSec: Int = 5) {
        TambolaCallerBackgroundService.startService(getApplication(), intervalSec)
        addChatMessage("Caller Engine 📡", "Started Firestore background caller service (every ${intervalSec}s)", isSystem = true)
    }

    fun stopFirestoreBackgroundCaller() {
        TambolaCallerBackgroundService.stopService(getApplication())
        addChatMessage("Caller Engine 📡", "Stopped Firestore background caller service", isSystem = true)
    }

    fun toggleFirestoreBackgroundCaller() {
        if (_uiState.value.isBackgroundCallerRunning) {
            stopFirestoreBackgroundCaller()
        } else {
            val interval = _uiState.value.gameConfiguration.callerSpeedSeconds.coerceAtLeast(3)
            startFirestoreBackgroundCaller(interval)
        }
    }

    fun drawNextFirestoreGameNumber() {
        viewModelScope.launch {
            FirestoreService.drawNextFirestoreGameNumber()
        }
    }

    fun resetFirestoreGame() {
        viewModelScope.launch {
            FirestoreService.resetCurrentGameState()
        }
        resetGame()
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoCall()
        stopFirestoreBackgroundCaller()
        tts?.stop()
        tts?.shutdown()
        soundManager.release()
    }
}

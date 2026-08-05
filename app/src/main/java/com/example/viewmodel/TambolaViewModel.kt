package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TambolaDatabase
import com.example.data.TambolaRepository
import com.example.model.BotPlayer
import com.example.model.CallerPhrases
import com.example.model.ClaimResult
import com.example.model.ClaimType
import com.example.model.GameRecord
import com.example.model.RoomChatMessage
import com.example.model.SavedTicketEntity
import com.example.model.TambolaTicket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

enum class ActiveTab {
    SOLO_BOT_ROOM,
    CALLER_BOARD,
    CLAIM_VERIFIER,
    TICKET_GENERATOR,
    GAME_HISTORY
}

data class TambolaUiState(
    val activeTab: ActiveTab = ActiveTab.SOLO_BOT_ROOM,
    // Caller Deck & Draw State
    val calledNumbers: List<Int> = emptyList(),
    val remainingNumbers: List<Int> = (1..90).toList().shuffled(),
    val currentCalledNumber: Int? = null,
    val isAutoCalling: Boolean = false,
    val autoCallIntervalSec: Int = 4,
    val isSoundEnabled: Boolean = true,

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
        RoomChatMessage("1", "Host", "Welcome to Tambola Studio! Room open. Match starting...", isSystem = true)
    ),

    // Active Verification Modal Result
    val lastClaimResult: ClaimResult? = null,
    val isGameFinished: Boolean = false,
    val userScore: Int = 0,

    // Custom Generator State
    val generatedSheet: List<TambolaTicket> = emptyList(),
    val savedTicketsList: List<SavedTicketEntity> = emptyList(),
    val gameRecordsList: List<GameRecord> = emptyList()
)

class TambolaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TambolaRepository
    private var tts: TextToSpeech? = null

    private val _uiState = MutableStateFlow(TambolaUiState())
    val uiState: StateFlow<TambolaUiState> = _uiState.asStateFlow()

    private var autoCallJob: Job? = null

    init {
        val dao = TambolaDatabase.getDatabase(application).tambolaDao()
        repository = TambolaRepository(dao)

        initTextToSpeech(application)
        observeDatabase()
        resetGame()
    }

    private fun initTextToSpeech(context: Application) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
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
        _uiState.update { state ->
            val currentMarked = state.markedNumbersMap[ticketId] ?: emptySet()
            val newSet = if (currentMarked.contains(number)) {
                currentMarked - number
            } else {
                currentMarked + number
            }
            val updatedMap = state.markedNumbersMap.toMutableMap()
            updatedMap[ticketId] = newSet
            state.copy(markedNumbersMap = updatedMap)
        }
    }

    fun autoMarkAllCalledNumbers() {
        val calledSet = uiState.value.calledNumbers.toSet()
        _uiState.update { state ->
            val updatedMap = state.markedNumbersMap.toMutableMap()
            state.playerTickets.forEach { ticket ->
                val ticketNumbers = ticket.getAllNumbers().toSet()
                val newlyMarked = ticketNumbers.intersect(calledSet)
                updatedMap[ticket.id] = newlyMarked
            }
            state.copy(markedNumbersMap = updatedMap)
        }
        addChatMessage("Host", "Auto-dabbed all called numbers on tickets!", isSystem = true)
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
            speakPhrase(phrase)
        }

        addChatMessage("Caller 🎙️", phrase, isSystem = true)

        // Update Bot actions & check bot claims
        processBotTurns(nextNum)
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

        // Check if any bot satisfies an unclaimed prize with ~70% chance of claiming instantly
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
        _uiState.update { it.copy(isAutoCalling = true) }
        autoCallJob = viewModelScope.launch {
            while (uiState.value.isAutoCalling && uiState.value.remainingNumbers.isNotEmpty() && !uiState.value.isGameFinished) {
                callNextNumber()
                delay(uiState.value.autoCallIntervalSec * 1000L)
            }
            if (uiState.value.remainingNumbers.isEmpty()) {
                _uiState.update { it.copy(isAutoCalling = false) }
            }
        }
    }

    private fun stopAutoCall() {
        autoCallJob?.cancel()
        autoCallJob = null
        _uiState.update { it.copy(isAutoCalling = false) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
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
            claimPrizeForWinner(claimType, "You (Player)", result)
            _uiState.update { it.copy(userScore = it.userScore + claimType.prizePoints) }
            addChatMessage("You 🏆", "Claimed ${claimType.displayName}! (+${claimType.prizePoints} pts)")
        } else {
            addChatMessage("System ⚠️", "Bogus claim by You for ${claimType.displayName}: ${result.message}", isSystem = true)
        }
    }

    private fun claimPrizeForWinner(claimType: ClaimType, winnerName: String, result: ClaimResult) {
        val updatedClaims = uiState.value.claimedPrizes.toMutableMap()
        if (!updatedClaims.containsKey(claimType)) {
            updatedClaims[claimType] = winnerName
            _uiState.update { it.copy(claimedPrizes = updatedClaims) }

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
        _uiState.update { it.copy(isGameFinished = true) }

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
        }
    }

    fun deleteSavedTicket(id: String) {
        viewModelScope.launch {
            repository.deleteSavedTicket(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoCall()
        tts?.stop()
        tts?.shutdown()
    }
}

package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.random.Random

enum class ClaimType(val displayName: String, val description: String, val prizePoints: Int) {
    EARLY_FIVE("Jaldi 5 (Early 5)", "First 5 numbers marked anywhere on the ticket", 50),
    FOUR_CORNERS("Four Corners", "1st and last numbers of top and bottom rows", 60),
    TOP_LINE("Top Line", "All 5 numbers in the 1st row marked", 100),
    MIDDLE_LINE("Middle Line", "All 5 numbers in the 2nd row marked", 100),
    BOTTOM_LINE("Bottom Line", "All 5 numbers in the 3rd row marked", 100),
    FULL_HOUSE_1("1st Full House", "All 15 numbers marked on the ticket", 300),
    FULL_HOUSE_2("2nd Full House", "All 15 numbers marked on the ticket", 200)
}

data class TambolaTicket(
    val id: String,
    val grid: List<List<Int?>>, // 3 rows, 9 columns. Null means blank space
    val markedNumbers: Set<Int> = emptySet()
) {
    fun getAllNumbers(): List<Int> = grid.flatten().filterNotNull()

    fun getRowNumbers(rowIndex: Int): List<Int> {
        if (rowIndex !in 0..2) return emptyList()
        return grid[rowIndex].filterNotNull()
    }

    fun getCornerNumbers(): List<Int> {
        val topRow = grid[0].filterNotNull()
        val bottomRow = grid[2].filterNotNull()
        if (topRow.isEmpty() || bottomRow.isEmpty()) return emptyList()
        return listOf(topRow.first(), topRow.last(), bottomRow.first(), bottomRow.last())
    }

    companion object {
        /**
         * Generates a valid Tambola ticket following official rules:
         * 3 rows x 9 columns. Total 15 numbers (5 numbers per row).
         * Each column contains numbers in its specific tens range.
         */
        fun generate(id: String = "T-${Random.nextInt(1000, 9999)}"): TambolaTicket {
            return TambolaTicketGenerator.generateSingleTicket(id)
        }

        /**
         * Generates a full sheet of 6 tickets containing numbers 1 to 90.
         */
        fun generateFullSheet(): List<TambolaTicket> {
            return TambolaTicketGenerator.generateFullSheet()
        }
    }
}

data class ClaimResult(
    val isSuccess: Boolean,
    val claimType: ClaimType,
    val ticketId: String,
    val message: String,
    val missingNumbers: List<Int> = emptyList()
)

data class BotPlayer(
    val name: String,
    val avatarEmoji: String,
    val ticket: TambolaTicket,
    val claimsWon: MutableList<ClaimType> = mutableListOf()
)

data class RoomChatMessage(
    val id: String,
    val senderName: String,
    val message: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String,
    val totalNumbersCalled: Int,
    val winnerSummary: String,
    val userScore: Int
)

@Entity(tableName = "saved_tickets")
data class SavedTicketEntity(
    @PrimaryKey val id: String,
    val name: String,
    val gridJson: String, // JSON representation of 3x9 grid
    val createdAt: Long = System.currentTimeMillis()
)

data class RoomTicketSlot(
    val slotNumber: Int,
    val ticket: TambolaTicket,
    val isBooked: Boolean = false,
    val bookedByPhone: String = "",
    val bookedByName: String = "",
    val bookingTimestamp: Long = 0L,
    val paymentMethod: String = ""
)

fun generateRoomTicketSlots(count: Int, bookedDetails: List<Pair<String, String>> = emptyList()): List<RoomTicketSlot> {
    return (1..count).map { slotNum ->
        val bookedInfo = bookedDetails.getOrNull(slotNum - 1)
        val isBooked = bookedInfo != null
        RoomTicketSlot(
            slotNumber = slotNum,
            ticket = TambolaTicket.generate("SLOT-$slotNum"),
            isBooked = isBooked,
            bookedByName = bookedInfo?.first ?: "",
            bookedByPhone = bookedInfo?.second ?: "",
            bookingTimestamp = if (isBooked) System.currentTimeMillis() - ((count - slotNum + 1) * 180000L) else 0L,
            paymentMethod = if (isBooked) "Google Pay UPI" else ""
        )
    }
}

data class GameRoom(
    val id: String,
    val title: String,
    val hostName: String,
    val category: String, // "Public", "Quick 90", "High Roller"
    val prizeAmount: Int,
    val entryFee: Int = 0,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val isUnlimitedPlayers: Boolean = false,
    val isLive: Boolean = true,
    val isJackpot: Boolean = false,
    val iconEmoji: String = "👑",
    val scheduledStartTimeMs: Long? = null,
    val scheduledTimeString: String = "",
    val prizeBreakdown: Map<String, Int> = emptyMap(),
    val ticketSlots: List<RoomTicketSlot> = emptyList()
)

enum class TransactionType {
    ALL,
    DEPOSIT,
    WITHDRAWAL,
    TICKET_PURCHASE,
    PRIZE_WIN
}

enum class TransactionStatus {
    SUCCESS,
    PROCESSING,
    FAILED
}

data class WalletTransaction(
    val id: String = "TXN_${System.currentTimeMillis()}",
    val type: TransactionType = TransactionType.DEPOSIT,
    val amount: Int = 0,
    val title: String = "Deposit",
    val description: String = "",
    val paymentMethod: String = "UPI (GPay)",
    val referenceId: String = "pay_Rzp123456",
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val closingBalance: Int = 0,
    val roomTitle: String = "",
    val ticketCount: Int = 1,
    val timestampMs: Long = System.currentTimeMillis()
)

data class RazorpayTransaction(
    val paymentId: String,
    val amount: Int,
    val status: String = "SUCCESS",
    val paymentMethod: String = "UPI (GPay)",
    val timestampMs: Long = System.currentTimeMillis()
)

data class GameConfiguration(
    val callerSpeedSeconds: Int = 5,
    val voiceLanguage: String = "English",
    val autoClaimVerification: Boolean = true,
    val minWithdrawalAmount: Int = 100,
    val announcementBanner: String = "Welcome to Udaipurtambola! 90-Ball Live Games Every 15 Mins.",
    val maintenanceMode: Boolean = false,
    val allowGuestJoin: Boolean = true,
    val maxTicketsPerPlayer: Int = 6,
    val updatedAt: Long = System.currentTimeMillis()
)

data class CurrentGameState(
    val gameId: String = "LIVE_TAMBOLA_MATCH",
    val currentNumber: Int? = null,
    val calledNumbers: List<Int> = emptyList(),
    val lastPhrase: String = "",
    val isRunning: Boolean = false,
    val drawIntervalSec: Int = 5,
    val updatedAt: Long = System.currentTimeMillis()
)

data class TambolaWinnerHistory(
    val id: String = "",
    val matchId: String = "",
    val roomTitle: String = "Grand 90 Live",
    val winnerName: String = "Player",
    val winnerPhone: String = "",
    val claimPattern: String = "Full House", // Early 5, Top Line, Middle Line, Bottom Line, Full House, Corners
    val prizeAmount: Int = 500,
    val winningNumber: Int = 90,
    val totalNumbersCalled: Int = 45,
    val verifiedTimestamp: Long = System.currentTimeMillis(),
    val isAutoVerified: Boolean = true,
    val transactionRef: String = "TXN_TAMBOLA_WIN"
)

data class AdminOrganizationInfo(
    val organizationName: String = "Udaipurtambola Official Gaming Club",
    val supportPhone: String = "+91 98765 00100",
    val supportWhatsapp: String = "+91 98765 00100",
    val supportEmail: String = "admin@udaipurtambola.club",
    val address: String = "Plot 42, Amber Tower, Fatehsagar Lake Road, Udaipur, Rajasthan 313001",
    val adminUpiId: String = "udaipurtambola.admin@oksbi",
    val adminBankName: String = "State Bank of India (SBI)",
    val adminAccountNumber: String = "•••• •••• 9821",
    val adminIfscCode: String = "SBIN0001234",
    val rulesAndRegulations: String = """
1. 90-Ball Standard Tambola Rules: Each ticket has 15 numbers arranged across 3 rows and 9 columns (5 numbers per row).
2. Numbers 1 to 90 are drawn randomly by the Caller Engine.
3. Automatic Selection (Auto-Daub): Ticket numbers are automatically highlighted and marked as soon as the Caller announces them. Players can also tap manually.
4. Winning Patterns & Valid Claims:
   • Jaldi 5 (Early 5): The first 5 numbers marked on your ticket.
   • Four Corners: The first and last numbers of the top and bottom rows.
   • Top Line: All 5 numbers in the 1st row marked.
   • Middle Line: All 5 numbers in the 2nd row marked.
   • Bottom Line: All 5 numbers in the 3rd row marked.
   • Full House: All 15 numbers marked on the entire ticket.
5. Ticket Booking & Revenue:
   • Players can select and buy tickets using Google Pay, Online Payment (Razorpay/Cards/Netbanking), or Wallet Balance.
   • All booking funds directly credit to the Admin Official Account.
   • Admin can also assign/book tickets directly for any player.
6. The Admin reserves the right to manage match schedules and verify all prize claims.
    """.trimIndent(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class StaffRole {
    ADMIN,
    MANAGER,
    AGENT
}

data class StaffUser(
    val id: String = "staff_${System.currentTimeMillis()}",
    val loginId: String,
    val password: String,
    val name: String,
    val role: StaffRole,
    val phone: String = "",
    val isBookingAllowed: Boolean = true, // For Agents
    val isCreationAllowed: Boolean = true, // For Managers
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "Admin",
    val totalTicketsBooked: Int = 0,
    val totalAmountHandled: Int = 0
)

data class AgentBookingRecord(
    val id: String = "AGBK_${System.currentTimeMillis()}",
    val agentId: String = "",
    val agentName: String = "Agent",
    val playerPhone: String = "",
    val playerName: String = "Player",
    val roomId: String = "room-1",
    val roomTitle: String = "Mega Amber Jackpot 90",
    val slotNumber: Int? = null,
    val ticketCount: Int = 1,
    val ticketIds: List<String> = emptyList(),
    val amountPaid: Int = 50,
    val paymentMethod: String = "Agent Cash/UPI",
    val timestampMs: Long = System.currentTimeMillis()
)

data class AdminTicketBookingRecord(
    val id: String = "BK_${System.currentTimeMillis()}",
    val playerPhone: String = "",
    val playerName: String = "Player",
    val roomId: String = "room-1",
    val roomTitle: String = "Mega Amber Jackpot 90",
    val ticketCount: Int = 1,
    val ticketIds: List<String> = emptyList(),
    val amountPaid: Int = 50,
    val paymentMethod: String = "Admin Direct Booking",
    val timestampMs: Long = System.currentTimeMillis()
)

data class PlayerProfileStats(
    val nickname: String = "Lucky Striker",
    val avatarUri: String? = null,
    val avatarPreset: String = "👑",
    val bio: String = "Tambola enthusiast & Housie champion 🎯",
    val gamesPlayed: Int = 18,
    val gamesWon: Int = 7,
    val totalEarnings: Int = 4350,
    val totalPoints: Int = 1850,
    val earlyFiveWins: Int = 3,
    val cornersWins: Int = 2,
    val topLineWins: Int = 4,
    val middleLineWins: Int = 2,
    val bottomLineWins: Int = 1,
    val fullHouseWins: Int = 3,
    val memberSince: Long = System.currentTimeMillis() - (15L * 86400000L)
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed.toDouble()) * 100).toInt() else 0

    val totalClaimsWon: Int
        get() = earlyFiveWins + cornersWins + topLineWins + middleLineWins + bottomLineWins + fullHouseWins
}



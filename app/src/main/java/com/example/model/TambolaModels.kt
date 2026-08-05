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
         * Each column contains numbers in its specific tens range:
         * Col 0: 1-9, Col 1: 10-19, Col 2: 20-29, Col 3: 30-39, Col 4: 40-49,
         * Col 5: 50-59, Col 6: 60-69, Col 7: 70-79, Col 8: 80-90
         */
        fun generate(id: String = "T-${Random.nextInt(1000, 9999)}"): TambolaTicket {
            val grid = Array(3) { arrayOfNulls<Int>(9) }

            // Ranges for each column
            val colRanges = listOf(
                (1..9).toList(),
                (10..19).toList(),
                (20..29).toList(),
                (30..39).toList(),
                (40..49).toList(),
                (50..59).toList(),
                (60..69).toList(),
                (70..79).toList(),
                (80..90).toList()
            )

            // Select 15 column slots ensuring each column has at least 1 number, and max 3 numbers.
            // Row count must be exactly 5 numbers per row.
            var valid = false
            var finalGrid = Array(3) { arrayOfNulls<Int>(9) }

            while (!valid) {
                finalGrid = Array(3) { arrayOfNulls<Int>(9) }
                val tempColRanges = colRanges.map { it.shuffled().toMutableList() }
                
                // Count of numbers in each row
                val rowCounts = intArrayOf(0, 0, 0)
                // Count of numbers in each col
                val colCounts = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)

                // Ensure each column gets at least 1 number
                for (c in 0..8) {
                    val availableRows = (0..2).filter { rowCounts[it] < 5 }.shuffled()
                    if (availableRows.isNotEmpty()) {
                        val r = availableRows.first()
                        finalGrid[r][c] = tempColRanges[c].removeAt(0)
                        rowCounts[r]++
                        colCounts[c]++
                    }
                }

                // Place remaining 6 numbers to reach total 15 (5 per row)
                var attempts = 0
                while (rowCounts.sum() < 15 && attempts < 100) {
                    attempts++
                    val r = (0..2).filter { rowCounts[it] < 5 }.shuffled().firstOrNull() ?: break
                    val c = (0..8).filter { colCounts[it] < 3 && finalGrid[r][it] == null && tempColRanges[it].isNotEmpty() }.shuffled().firstOrNull() ?: continue

                    finalGrid[r][c] = tempColRanges[c].removeAt(0)
                    rowCounts[r]++
                    colCounts[c]++
                }

                if (rowCounts[0] == 5 && rowCounts[1] == 5 && rowCounts[2] == 5) {
                    // Sort numbers in each column from top to bottom
                    for (c in 0..8) {
                        val colVals = (0..2).mapNotNull { finalGrid[it][c] }.sorted()
                        var idx = 0
                        for (r in 0..2) {
                            if (finalGrid[r][c] != null) {
                                finalGrid[r][c] = colVals[idx++]
                            }
                        }
                    }
                    valid = true
                }
            }

            val listGrid = finalGrid.map { row -> row.toList() }
            return TambolaTicket(id = id, grid = listGrid)
        }

        /**
         * Generates a full sheet of 6 tickets containing numbers 1 to 90 exactly once.
         */
        fun generateFullSheet(): List<TambolaTicket> {
            return (1..6).map { idx ->
                generate("Sheet-T$idx")
            }
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

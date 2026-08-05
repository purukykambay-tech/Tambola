package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.GameRecord
import com.example.model.SavedTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TambolaDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllGameRecords(): Flow<List<GameRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameRecord(record: GameRecord)

    @Query("DELETE FROM game_records")
    suspend fun clearAllGameRecords()

    @Query("SELECT * FROM saved_tickets ORDER BY createdAt DESC")
    fun getAllSavedTickets(): Flow<List<SavedTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedTicket(ticket: SavedTicketEntity)

    @Query("DELETE FROM saved_tickets WHERE id = :ticketId")
    suspend fun deleteSavedTicket(ticketId: String)
}

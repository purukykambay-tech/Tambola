package com.example.data

import com.example.model.GameRecord
import com.example.model.SavedTicketEntity
import kotlinx.coroutines.flow.Flow

class TambolaRepository(private val dao: TambolaDao) {
    val gameRecords: Flow<List<GameRecord>> = dao.getAllGameRecords()
    val savedTickets: Flow<List<SavedTicketEntity>> = dao.getAllSavedTickets()

    suspend fun saveGameRecord(record: GameRecord) {
        dao.insertGameRecord(record)
    }

    suspend fun clearHistory() {
        dao.clearAllGameRecords()
    }

    suspend fun saveTicket(ticket: SavedTicketEntity) {
        dao.insertSavedTicket(ticket)
    }

    suspend fun deleteSavedTicket(id: String) {
        dao.deleteSavedTicket(id)
    }
}

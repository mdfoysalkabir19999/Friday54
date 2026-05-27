package com.example.data.repository

import com.example.data.database.AutonomousLogEntity
import com.example.data.database.ChatMessageEntity
import com.example.data.database.FridayDao
import com.example.data.database.VaultItemEntity
import kotlinx.coroutines.flow.Flow

class FridayRepository(private val dao: FridayDao) {

    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    val allLogs: Flow<List<AutonomousLogEntity>> = dao.getAllLogs()
    val allVaultItems: Flow<List<VaultItemEntity>> = dao.getAllVaultItems()

    suspend fun insertChatMessage(message: ChatMessageEntity) {
        dao.insertChatMessage(message)
    }

    suspend fun clearChat() {
        dao.clearAllChat()
    }

    suspend fun insertLog(log: AutonomousLogEntity) {
        dao.insertLog(log)
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun insertVaultItem(item: VaultItemEntity) {
        dao.insertVaultItem(item)
    }

    suspend fun deleteVaultItemById(id: Int) {
        dao.deleteVaultItemById(id)
    }
}

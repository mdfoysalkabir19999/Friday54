package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class FridayRepository(private val dao: FridayDao) {

    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    val allLogs: Flow<List<AutonomousLogEntity>> = dao.getAllLogs()
    val allVaultItems: Flow<List<VaultItemEntity>> = dao.getAllVaultItems()
    val allTraining: Flow<List<TrainingEntity>> = dao.getAllTraining()
    val allCodeSubmissions: Flow<List<CustomCodeSubmissionEntity>> = dao.getAllCodeSubmissions()

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

    suspend fun insertTraining(item: TrainingEntity) {
        dao.insertTraining(item)
    }

    suspend fun deleteTrainingById(id: Int) {
        dao.deleteTrainingById(id)
    }

    suspend fun insertCodeSubmission(item: CustomCodeSubmissionEntity) {
        dao.insertCodeSubmission(item)
    }

    suspend fun clearCodeSubmissions() {
        dao.clearAllCodeSubmissions()
    }
}

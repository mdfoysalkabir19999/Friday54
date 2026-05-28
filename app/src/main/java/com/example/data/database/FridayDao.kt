package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FridayDao {

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllChat()

    // --- Autonomous Terminal Logs ---
    @Query("SELECT * FROM autonomous_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<AutonomousLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AutonomousLogEntity)

    @Query("DELETE FROM autonomous_logs")
    suspend fun clearLogs()

    // --- Secured Vault Data ---
    @Query("SELECT * FROM vault_items ORDER BY timestamp DESC")
    fun getAllVaultItems(): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItemById(id: Int)

    // --- Friday Training ---
    @Query("SELECT * FROM friday_training ORDER BY timestamp DESC")
    fun getAllTraining(): Flow<List<TrainingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(item: TrainingEntity)

    @Query("DELETE FROM friday_training WHERE id = :id")
    suspend fun deleteTrainingById(id: Int)

    // --- Custom Code Submissions ---
    @Query("SELECT * FROM custom_code_submissions ORDER BY timestamp DESC")
    fun getAllCodeSubmissions(): Flow<List<CustomCodeSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeSubmission(item: CustomCodeSubmissionEntity)

    @Query("DELETE FROM custom_code_submissions")
    suspend fun clearAllCodeSubmissions()
}

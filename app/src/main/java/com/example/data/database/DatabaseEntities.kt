package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "creator" or "friday"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)

@Entity(tableName = "autonomous_logs")
data class AutonomousLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "SECURITY", "SEO_AGENCY", "OPTIMIZATION", "CREATOR_DEFENSE", "SYSTEM"
    val messageText: String,
    val isSuccess: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val secretContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "friday_training")
data class TrainingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val triggerPattern: String,
    val responseText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_code_submissions")
data class CustomCodeSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codeSnippet: String,
    val note: String,
    val isCompiled: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

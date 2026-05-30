package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        AutonomousLogEntity::class,
        VaultItemEntity::class,
        TrainingEntity::class,
        CustomCodeSubmissionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FridayDatabase : RoomDatabase() {
    abstract fun fridayDao(): FridayDao

    companion object {
        @Volatile
        private var INSTANCE: FridayDatabase? = null

        fun getDatabase(context: Context): FridayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FridayDatabase::class.java,
                    "friday_matrix_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

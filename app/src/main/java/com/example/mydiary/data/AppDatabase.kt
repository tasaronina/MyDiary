package com.example.mydiary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Общая база приложения. Для ЛР-7 достаточно двух сущностей: категории и советы.
@Database(
    entities = [
        AdviceCategoryEntity::class,
        AdviceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun adviceDao(): AdviceDao

    companion object {
        // Синглтон, чтобы не держать несколько экземпляров БД.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_diary.db" // имя файла БД
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

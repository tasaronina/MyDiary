package com.example.mydiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Таблица категорий советов
@Entity(tableName = "advice_categories")
data class AdviceCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,     // id категории
    val name: String       // человекочитаемое имя категории
)

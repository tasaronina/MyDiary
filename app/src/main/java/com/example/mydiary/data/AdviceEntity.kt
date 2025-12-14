package com.example.mydiary.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Таблица советов: каждый совет относится к какой-то категории.
@Entity(
    tableName = "advices",
    foreignKeys = [
        ForeignKey(
            entity = AdviceCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId") // индекс по внешнему ключу для ускорения запросов
    ]
)
data class AdviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,     // id записи в БД
    val title: String,     // заголовок
    val text: String,      // текст
    val categoryId: Long   // связь с таблицей категорий
)

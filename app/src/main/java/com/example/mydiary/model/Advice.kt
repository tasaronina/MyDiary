package com.example.mydiary.model


data class Advice(
    val id: Long,          // Уникальный идентификатор записи (нужен для стабильности списка)
    var title: String,     // Краткий заголовок рекомендации
    var text: String       // Подробный текст рекомендации
)

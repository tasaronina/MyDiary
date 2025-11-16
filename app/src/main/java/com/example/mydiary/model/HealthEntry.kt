package com.example.mydiary.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// @Parcelize автоматически генерирует реализацию интерфейса Parcelable.

@Parcelize
data class HealthEntry(
    // dateTime — строка с датой и временем формирования отчёта
    val dateTime: String,
    // diseases — список выбранных хронических заболеваний
    val diseases: List<String>,
    // symptoms — список отмеченных симптомов за текущий день
    val symptoms: List<String>,
    // triggers — список возможных триггеров
    val triggers: List<String>,
    // report — уже сформированный текстовый отчёт, который я показываю на разных экранах.
    val report: String
) : Parcelable {
    companion object {
        // Статическая константа ключа для передачи объекта через Intent.putExtra().
        // Использую один и тот же ключ во всех активностях, чтобы не допустить опечаток
        const val EXTRA_KEY = "extra_health_entry"
    }
}

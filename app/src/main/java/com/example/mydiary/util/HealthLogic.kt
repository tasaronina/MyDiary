package com.example.mydiary.util

import com.example.mydiary.model.HealthEntry

object HealthLogic {

    fun buildHints(entry: HealthEntry): List<String> {
        val has = object {
            val diabetes get() = entry.diseases.contains("Диабет")
            val hypertension get() = entry.diseases.contains("Гипертония")
            val migraine get() = entry.diseases.contains("Мигрень")
            val asthma get() = entry.diseases.contains("Астма")
        }
        val s = entry.symptoms
        val t = entry.triggers

        val hints = mutableListOf<String>()
        if (has.diabetes && (s.contains("Жажда/частое мочеиспускание") || s.contains("Туман в глазах"))) {
            hints += "Диабет + жажда/\"туман\": проверьте уровень сахара, обсудите коррекцию терапии."
        }
        if (has.hypertension && (s.contains("Боль в груди") || s.contains("Головокружение"))) {
            hints += "Гипертония + боль в груди/головокружение: мониторинг давления, оценка терапии."
        }
        if (has.migraine && (t.contains("Стресс") || t.contains("Недосып") || t.contains("Кофеин"))) {
            hints += "Мигрень + стресс/недосып/кофеин: ведите дневник триггеров и режим сна."
        }
        if (has.asthma && (s.contains("Одышка") || t.contains("Физнагрузка") || t.contains("Погода/перепады давления"))) {
            hints += "Астма + одышка/нагрузка/погода: проверьте ингалятор и план действия."
        }
        return hints
    }

    fun fmtOrDash(items: List<String>) = if (items.isEmpty()) "—" else items.joinToString(", ")
}

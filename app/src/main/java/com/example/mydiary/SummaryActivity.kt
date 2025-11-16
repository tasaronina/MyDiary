package com.example.mydiary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.model.HealthEntry
import com.google.android.material.bottomnavigation.BottomNavigationView


class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяю ту же тему, что и в главной активности
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // TextView, в котором отображаю текст отчёта
        val tv: TextView = findViewById(R.id.tvSummary)
        // Нижнее меню навигации для перехода между экранами
        val bottom: BottomNavigationView = findViewById(R.id.bottomNav)

        // Получаю переданный из MainActivity объект HealthEntry


        val entry: HealthEntry? =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(HealthEntry.EXTRA_KEY, HealthEntry::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(HealthEntry.EXTRA_KEY)
            }

        // Если объект передан, показываю его поле report
        // если нет вывожу заглушку пока данных нет
        tv.text = entry?.report ?: getString(R.string.summary_empty)

        // Подсвечиваю пункт "Сводка" как активный
        bottom.selectedItemId = R.id.menu_summary
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Переход обратно на главный экран
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                // Если текущий пункт – Сводка никуда не переходим
                R.id.menu_summary -> true
                // Переход к экрану Подсказки с передачей того же объекта HealthEntry
                R.id.menu_tips -> {
                    startActivity(
                        Intent(this, TipsActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, entry)
                    )
                    true
                }
                else -> false
            }
        }
    }
}

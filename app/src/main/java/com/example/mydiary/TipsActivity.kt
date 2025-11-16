package com.example.mydiary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.model.HealthEntry
import com.google.android.material.bottomnavigation.BottomNavigationView


class TipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяю тему приложения перед созданием разметки.
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        // TextView, в котором я отображаю текст подсказок для пользователя.
        val tv: TextView = findViewById(R.id.tvTips)
        // Нижнее навигационное меню для переключения между тремя экранами.
        val bottom: BottomNavigationView = findViewById(R.id.bottomNav)



        val entry: HealthEntry? =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(HealthEntry.EXTRA_KEY, HealthEntry::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(HealthEntry.EXTRA_KEY)
            }

        // Из общего отчёта мне нужен только блок "Подсказки".
        // Я нахожу в тексте отчёта подстроку с заголовком "Подсказки:"
        // и, если он есть, показываю всё, что идёт дальше.
        tv.text = entry?.report?.let { report ->
            val title = getString(R.string.hints_title) // "Подсказки:"
            val idx = report.indexOf(title)
            // Если заголовок найден, беру подстроку от него до конца,
            // иначе вывожу текст-заглушку, что подсказок пока нет.
            if (idx >= 0) report.substring(idx) else getString(R.string.tips_empty)
        } ?: getString(R.string.tips_empty)

        // В нижнем меню подсвечиваю активным пункт "Подсказки".
        bottom.selectedItemId = R.id.menu_tips
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Переход на главный экран. Я передаю тот же объект HealthEntry,
                // чтобы при возврате данные не терялись.
                R.id.menu_home -> {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, entry)
                    )
                    true
                }
                // Переход на экран "Сводка" с теми же данными.
                R.id.menu_summary -> {
                    startActivity(
                        Intent(this, SummaryActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, entry)
                    )
                    true
                }
                // Если выбран текущий пункт "Подсказки", ничего не делаю.
                R.id.menu_tips -> true
                else -> false
            }
        }
    }
}

package com.example.mydiary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.model.HealthEntry
import com.example.mydiary.util.DiaryStorage
import com.google.android.material.bottomnavigation.BottomNavigationView


class SummaryActivity : AppCompatActivity() {

    // Текст с полным текущим отчётом.
    private lateinit var tvSummary: TextView

    // Текст с историей отчётов из внутреннего бинарного файла
    private lateinit var tvHistory: TextView
    private lateinit var btnClearHistory: Button

    // Блок для работы с экспортированным отчётом
    private lateinit var tvExport: TextView
    private lateinit var btnSaveExport: Button
    private lateinit var btnLoadExport: Button
    private lateinit var btnDeleteExport: Button

    // Объект с текущими данными, который пришёл с главного экрана.
    private var currentEntry: HealthEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Подключаю тему приложения.
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Привязка всех элементов интерфейса.
        tvSummary = findViewById(R.id.tvSummary)
        tvHistory = findViewById(R.id.tvHistory)
        tvExport = findViewById(R.id.tvExport)

        btnClearHistory = findViewById(R.id.btnClearHistory)
        btnSaveExport = findViewById(R.id.btnSaveExport)
        btnLoadExport = findViewById(R.id.btnLoadExport)
        btnDeleteExport = findViewById(R.id.btnDeleteExport)

        val bottom: BottomNavigationView = findViewById(R.id.bottomNav)

        // Получаю переданный из MainActivity объект HealthEntry.
        currentEntry =
            if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(HealthEntry.EXTRA_KEY, HealthEntry::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(HealthEntry.EXTRA_KEY)
            }

        val entry = currentEntry

        // Показ текущего отчёта
        // Если данные есть — показываю отчёт, если нет — заглушку
        tvSummary.text = entry?.report ?: getString(R.string.summary_empty)

        // чтение истории из внутреннего бинарного файла readHistoryInternal мне возвращает список строк
        val historyLines = DiaryStorage.readHistoryInternal(this)
        tvHistory.text =
            if (historyLines.isNotEmpty())
                historyLines.joinToString(separator = "\n\n")
            else
                getString(R.string.history_empty)

        // Кнопка "Очистить историю" удаляет файл history.bin
        btnClearHistory.setOnClickListener {
            DiaryStorage.clearHistoryInternal(this)
            tvHistory.text = getString(R.string.history_empty)
        }


        // Операция Read будет выполняться именно по кнопке "Загрузить отчёт из файла".
        tvExport.text = getString(R.string.export_empty)


        // Если файла не было — он создаётся, если был — перезаписывается.
        btnSaveExport.setOnClickListener {
            // Беру текст из блока "Сводный отчёт".
            val textToSave = tvSummary.text?.toString().orEmpty()

            if (textToSave.isNotBlank()) {
                // Запись в бинарный файл во внешнем хранилище
                DiaryStorage.writeExternalReport(this, textToSave)
                // Обновляю блок "Экспортированный отчёт" тем же текстом,
                // чтобы сразу было видно, что файл сохранён.
                tvExport.text = textToSave
            } else {
                // Если отчёт пустой — просто показываю заглушку.
                tvExport.text = getString(R.string.export_empty)
            }
        }


        // нажали "Загрузить отчёт из файла" и получили текст из бинарного файла.
        btnLoadExport.setOnClickListener {
            val loaded = DiaryStorage.readExternalReport(this)
            tvExport.text = loaded ?: getString(R.string.export_empty)
        }

        // удалить бинарный файл и показать, что данных нет
        btnDeleteExport.setOnClickListener {
            DiaryStorage.deleteExternalReport(this)
            tvExport.text = getString(R.string.export_empty)
        }

        // Нижнее меню навигации между экранами
        bottom.selectedItemId = R.id.menu_summary
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Переход обратно на главный экран
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                // Уже находимся на вкладке "Сводка".
                R.id.menu_summary -> true

                // Переход на экран "Подсказки" с теми же данными.
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

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

    private lateinit var tvSummary: TextView
    private lateinit var tvHistory: TextView
    private lateinit var tvExport: TextView
    private lateinit var btnClearHistory: Button
    private lateinit var btnDeleteExport: Button

    private var currentEntry: HealthEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        tvSummary = findViewById(R.id.tvSummary)
        tvHistory = findViewById(R.id.tvHistory)
        tvExport = findViewById(R.id.tvExport)
        btnClearHistory = findViewById(R.id.btnClearHistory)
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

        // Текущий отчёт.
        tvSummary.text = entry?.report ?: getString(R.string.summary_empty)

        // ----- S10: читаем историю отчётов из внутреннего бинарного файла -----
        val historyLines = DiaryStorage.readHistoryInternal(this)
        tvHistory.text =
            if (historyLines.isNotEmpty())
                historyLines.joinToString(separator = "\n\n")
            else
                getString(R.string.history_empty)

        // ----- S11: экспортируем текущий отчёт и читаем его из внешнего файла -----
        if (entry != null) {
            DiaryStorage.writeExternalReport(this, entry.report)
        }
        val exported = DiaryStorage.readExternalReport(this)
        tvExport.text = exported ?: getString(R.string.export_empty)

        // Кнопка "Очистить историю" (удаляет внутренний бинарный файл S10).
        btnClearHistory.setOnClickListener {
            DiaryStorage.clearHistoryInternal(this)
            tvHistory.text = getString(R.string.history_empty)
        }

        // Кнопка "Удалить экспортированный отчёт" (удаляет файл S11).
        btnDeleteExport.setOnClickListener {
            DiaryStorage.deleteExternalReport(this)
            tvExport.text = getString(R.string.export_empty)
        }

        // Нижнее меню.
        bottom.selectedItemId = R.id.menu_summary
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.menu_summary -> true
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

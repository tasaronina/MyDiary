package com.example.mydiary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.model.HealthEntry
import com.example.mydiary.util.DiaryStorage
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Главный экран приложения: дневник здоровья.
class MainActivity : AppCompatActivity() {

    private lateinit var tvReport: TextView
    private lateinit var btnBuildReport: Button
    private lateinit var btnAdviceList: Button      // кнопка перехода к списку рекомендаций (ЛР-5)
    private lateinit var btnClearPrefs: Button
    private lateinit var bottomNav: BottomNavigationView

    // Чекбоксы хронических заболеваний
    private lateinit var cbDiabetes: CheckBox
    private lateinit var cbHypertension: CheckBox
    private lateinit var cbMigraine: CheckBox
    private lateinit var cbAsthma: CheckBox

    // Чекбоксы симптомов
    private lateinit var cbHeadache: CheckBox
    private lateinit var cbDizziness: CheckBox
    private lateinit var cbNausea: CheckBox
    private lateinit var cbChestPain: CheckBox
    private lateinit var cbDyspnea: CheckBox
    private lateinit var cbWeakness: CheckBox
    private lateinit var cbTremor: CheckBox
    private lateinit var cbThirst: CheckBox
    private lateinit var cbBlurredVision: CheckBox

    // Чекбоксы триггеров
    private lateinit var cbStress: CheckBox
    private lateinit var cbLackOfSleep: CheckBox
    private lateinit var cbWeather: CheckBox
    private lateinit var cbMissedMed: CheckBox
    private lateinit var cbCaffeine: CheckBox
    private lateinit var cbWorkout: CheckBox
    private lateinit var cbDiet: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        // Восстанавливаю последнюю запись из SharedPreferences (S1).
        DiaryStorage.loadLastEntryFromPrefs(this)?.let { last ->
            applyEntryToCheckboxes(last)
            tvReport.text = last.report
        }

        // Явное создание нового отчёта
        btnBuildReport.setOnClickListener {
            val entry = buildAndPersistEntry()
            tvReport.text = entry.report
        }

        //  переход к экрану со списком рекомендаций
        btnAdviceList.setOnClickListener {
            startActivity(Intent(this, AdviceListActivity::class.java))
        }

        // Сброс сохранённых настроек в SharedPreferences.
        btnClearPrefs.setOnClickListener {
            DiaryStorage.clearPrefs(this)
            clearAllCheckboxes()
            tvReport.text = ""
        }

        // Нижнее меню.
        bottomNav.selectedItemId = R.id.menu_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true

                // Переход на "Сводку" без лишней записи в историю.
                R.id.menu_summary -> {
                    startActivity(
                        Intent(this, SummaryActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, buildEntry())
                    )
                    true
                }

                // Переход на "Подсказки" без записи в историю.
                R.id.menu_tips -> {
                    startActivity(
                        Intent(this, TipsActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, buildEntry())
                    )
                    true
                }

                else -> false
            }
        }

        savedInstanceState?.getString(STATE_REPORT_TEXT)?.let { tvReport.text = it }
    }

    // Привязка всех View по id.
    private fun bindViews() {
        tvReport = findViewById(R.id.tvReport)
        btnBuildReport = findViewById(R.id.btnBuildReport)
        btnAdviceList = findViewById(R.id.btnAdviceList)   // ВАЖНО: id в разметке должен быть @+id/btnAdviceList
        btnClearPrefs = findViewById(R.id.btnClearPrefs)
        bottomNav = findViewById(R.id.bottomNav)

        cbDiabetes = findViewById(R.id.cbDiabetes)
        cbHypertension = findViewById(R.id.cbHypertension)
        cbMigraine = findViewById(R.id.cbMigraine)
        cbAsthma = findViewById(R.id.cbAsthma)

        cbHeadache = findViewById(R.id.cbHeadache)
        cbDizziness = findViewById(R.id.cbDizziness)
        cbNausea = findViewById(R.id.cbNausea)
        cbChestPain = findViewById(R.id.cbChestPain)
        cbDyspnea = findViewById(R.id.cbDyspnea)
        cbWeakness = findViewById(R.id.cbWeakness)
        cbTremor = findViewById(R.id.cbTremor)
        cbThirst = findViewById(R.id.cbThirst)
        cbBlurredVision = findViewById(R.id.cbBlurredVision)

        cbStress = findViewById(R.id.cbStress)
        cbLackOfSleep = findViewById(R.id.cbLackOfSleep)
        cbWeather = findViewById(R.id.cbWeather)
        cbMissedMed = findViewById(R.id.cbMissedMed)
        cbCaffeine = findViewById(R.id.cbCaffeine)
        cbWorkout = findViewById(R.id.cbWorkout)
        cbDiet = findViewById(R.id.cbDiet)
    }

    // Собираю объект HealthEntry по текущему состоянию чекбоксов (без сохранения).
    private fun buildEntry(): HealthEntry {
        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date())

        val diseases = selectedLabels(
            cbDiabetes to getString(R.string.diabetes),
            cbHypertension to getString(R.string.hypertension),
            cbMigraine to getString(R.string.migraine),
            cbAsthma to getString(R.string.asthma)
        )

        val symptoms = selectedLabels(
            cbHeadache to getString(R.string.sym_headache),
            cbDizziness to getString(R.string.sym_dizziness),
            cbNausea to getString(R.string.sym_nausea),
            cbChestPain to getString(R.string.sym_chest_pain),
            cbDyspnea to getString(R.string.sym_dyspnea),
            cbWeakness to getString(R.string.sym_weakness),
            cbTremor to getString(R.string.sym_tremor),
            cbThirst to getString(R.string.sym_thirst),
            cbBlurredVision to getString(R.string.sym_blurred_vision)
        )

        val triggers = selectedLabels(
            cbStress to getString(R.string.tr_stress),
            cbLackOfSleep to getString(R.string.tr_lack_of_sleep),
            cbWeather to getString(R.string.tr_weather),
            cbMissedMed to getString(R.string.tr_missed_med),
            cbCaffeine to getString(R.string.tr_caffeine),
            cbWorkout to getString(R.string.tr_workout),
            cbDiet to getString(R.string.tr_diet)
        )

        val report = buildString {
            appendLine(getString(R.string.report_title))
            appendLine(getString(R.string.report_dt, dateStr))
            appendLine()
            appendLine(getString(R.string.report_diseases, fmtOrDash(diseases)))
            appendLine(getString(R.string.report_symptoms, fmtOrDash(symptoms)))
            appendLine(getString(R.string.report_triggers, fmtOrDash(triggers)))
            appendLine()

            val hints = makeHints(diseases, symptoms, triggers)
            if (hints.isNotEmpty()) {
                appendLine(getString(R.string.hints_title))
                hints.forEachIndexed { i, h -> appendLine("${i + 1}) $h") }
            } else {
                appendLine(getString(R.string.hints_empty))
            }
            appendLine()
            appendLine(getString(R.string.disclaimer))
        }

        return HealthEntry(dateStr, diseases, symptoms, triggers, report)
    }

    // Строю отчёт и одновременно сохраняю его в S1 и S10.
    private fun buildAndPersistEntry(): HealthEntry {
        val entry = buildEntry()
        DiaryStorage.saveLastEntryToPrefs(this, entry)
        DiaryStorage.appendHistoryInternal(this, entry)
        return entry
    }

    private fun makeHints(
        d: List<String>,
        s: List<String>,
        t: List<String>
    ): List<String> {
        val h = mutableListOf<String>()
        fun has(x: String) = x in (d + s + t)

        if (has(getString(R.string.migraine)) &&
            (has(getString(R.string.tr_stress)) ||
                    has(getString(R.string.tr_lack_of_sleep)) ||
                    has(getString(R.string.tr_caffeine)))
        ) {
            h += getString(R.string.hint_migraine)
        }

        if (has(getString(R.string.hypertension)) &&
            (has(getString(R.string.sym_chest_pain)) ||
                    has(getString(R.string.sym_dizziness)))
        ) {
            h += getString(R.string.hint_hypertension)
        }

        if (has(getString(R.string.asthma)) &&
            (has(getString(R.string.sym_dyspnea)) ||
                    has(getString(R.string.tr_workout)) ||
                    has(getString(R.string.tr_weather)))
        ) {
            h += getString(R.string.hint_asthma)
        }

        if (has(getString(R.string.diabetes)) &&
            (has(getString(R.string.sym_thirst)) ||
                    has(getString(R.string.sym_blurred_vision)))
        ) {
            h += getString(R.string.hint_diabetes)
        }

        return h
    }

    private fun fmtOrDash(items: List<String>) =
        if (items.isEmpty()) "—" else items.joinToString(", ")

    private fun selectedLabels(vararg pairs: Pair<CheckBox, String>): List<String> =
        pairs.filter { it.first.isChecked }.map { it.second }

    private fun applyEntryToCheckboxes(entry: HealthEntry) {
        fun checkIfContains(cb: CheckBox, label: String) {
            cb.isChecked = label in (entry.diseases + entry.symptoms + entry.triggers)
        }

        checkIfContains(cbDiabetes, getString(R.string.diabetes))
        checkIfContains(cbHypertension, getString(R.string.hypertension))
        checkIfContains(cbMigraine, getString(R.string.migraine))
        checkIfContains(cbAsthma, getString(R.string.asthma))

        checkIfContains(cbHeadache, getString(R.string.sym_headache))
        checkIfContains(cbDizziness, getString(R.string.sym_dizziness))
        checkIfContains(cbNausea, getString(R.string.sym_nausea))
        checkIfContains(cbChestPain, getString(R.string.sym_chest_pain))
        checkIfContains(cbDyspnea, getString(R.string.sym_dyspnea))
        checkIfContains(cbWeakness, getString(R.string.sym_weakness))
        checkIfContains(cbTremor, getString(R.string.sym_tremor))
        checkIfContains(cbThirst, getString(R.string.sym_thirst))
        checkIfContains(cbBlurredVision, getString(R.string.sym_blurred_vision))

        checkIfContains(cbStress, getString(R.string.tr_stress))
        checkIfContains(cbLackOfSleep, getString(R.string.tr_lack_of_sleep))
        checkIfContains(cbWeather, getString(R.string.tr_weather))
        checkIfContains(cbMissedMed, getString(R.string.tr_missed_med))
        checkIfContains(cbCaffeine, getString(R.string.tr_caffeine))
        checkIfContains(cbWorkout, getString(R.string.tr_workout))
        checkIfContains(cbDiet, getString(R.string.tr_diet))
    }

    private fun clearAllCheckboxes() {
        listOf(
            cbDiabetes, cbHypertension, cbMigraine, cbAsthma,
            cbHeadache, cbDizziness, cbNausea, cbChestPain,
            cbDyspnea, cbWeakness, cbTremor, cbThirst, cbBlurredVision,
            cbStress, cbLackOfSleep, cbWeather, cbMissedMed,
            cbCaffeine, cbWorkout, cbDiet
        ).forEach { it.isChecked = false }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_REPORT_TEXT, tvReport.text?.toString() ?: "")
    }

    companion object {
        private const val STATE_REPORT_TEXT = "state_report_text"
    }
}

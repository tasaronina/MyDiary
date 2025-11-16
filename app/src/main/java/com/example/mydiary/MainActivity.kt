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

// Главная активность приложения – здесь пользователь отмечает симптомы,
// хронические заболевания и триггеры, а также формирует текстовый отчёт.
class MainActivity : AppCompatActivity() {

    // Текстовое поле, куда я вывожу готовый отчёт.
    private lateinit var tvReport: TextView
    // Кнопка для построения отчёта по выбранным чекбоксам.
    private lateinit var btnBuildReport: Button
    // Кнопка для открытия новой страницы со списком рекомендаций (ЛР5).
    private lateinit var btnOpenAdviceList: Button
    // Кнопка для сброса сохранённых данных (SharedPreferences, S1).
    private lateinit var btnClearPrefs: Button
    // Нижнее меню навигации между экранами.
    private lateinit var bottomNav: BottomNavigationView

    // Блок чекбоксов для хронических заболеваний.
    private lateinit var cbDiabetes: CheckBox
    private lateinit var cbHypertension: CheckBox
    private lateinit var cbMigraine: CheckBox
    private lateinit var cbAsthma: CheckBox

    // Блок чекбоксов для симптомов за текущий день.
    private lateinit var cbHeadache: CheckBox
    private lateinit var cbDizziness: CheckBox
    private lateinit var cbNausea: CheckBox
    private lateinit var cbChestPain: CheckBox
    private lateinit var cbDyspnea: CheckBox
    private lateinit var cbWeakness: CheckBox
    private lateinit var cbTremor: CheckBox
    private lateinit var cbThirst: CheckBox
    private lateinit var cbBlurredVision: CheckBox

    // Блок чекбоксов для возможных триггеров.
    private lateinit var cbStress: CheckBox
    private lateinit var cbLackOfSleep: CheckBox
    private lateinit var cbWeather: CheckBox
    private lateinit var cbMissedMed: CheckBox
    private lateinit var cbCaffeine: CheckBox
    private lateinit var cbWorkout: CheckBox
    private lateinit var cbDiet: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяю тему Material3 до вызова super.onCreate,
        // чтобы сразу отображалась корректная тема.
        setTheme(R.style.Theme_MyDiary)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализирую все вьюшки из разметки.
        bindViews()

        // Пытаюсь восстановить последнюю сохранённую запись из SharedPreferences
        DiaryStorage.loadLastEntryFromPrefs(this)?.let { last ->
            applyEntryToCheckboxes(last)
            tvReport.text = last.report
        }

        // Обработчик нажатия на кнопку "Сформировать отчёт".
        btnBuildReport.setOnClickListener {
            // Собираю данные, сохраняю их в хранилища (S1, S10) и показываю отчёт.
            val entry = buildAndPersistEntry()
            tvReport.text = entry.report
        }

        // Переход на экран со списком рекомендаций (ЛР5).
        btnOpenAdviceList.setOnClickListener {
            startActivity(Intent(this, AdviceListActivity::class.java))
        }

        // Сброс сохранённых данных (SharedPreferences, S1).
        btnClearPrefs.setOnClickListener {
            DiaryStorage.clearPrefs(this)
            clearAllCheckboxes()
            tvReport.text = ""
        }

        // Настраиваю нижнее меню: на главном экране активен пункт "Главная".
        bottomNav.selectedItemId = R.id.menu_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true

                R.id.menu_summary -> {
                    val entry = buildAndPersistEntry()
                    startActivity(
                        Intent(this, SummaryActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, entry)
                    )
                    true
                }

                R.id.menu_tips -> {
                    val entry = buildAndPersistEntry()
                    startActivity(
                        Intent(this, TipsActivity::class.java)
                            .putExtra(HealthEntry.EXTRA_KEY, entry)
                    )
                    true
                }

                else -> false
            }
        }

        // Восстанавливаю текст отчёта при перевороте экрана.
        savedInstanceState?.getString(STATE_REPORT_TEXT)?.let { tvReport.text = it }
    }

    // Привязка всех элементов интерфейса к полям активности.
    private fun bindViews() {
        tvReport = findViewById(R.id.tvReport)
        btnBuildReport = findViewById(R.id.btnBuildReport)
        btnOpenAdviceList = findViewById(R.id.btnOpenAdviceList)
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

    // Строю объект HealthEntry и параллельно сохраняю его
    private fun buildAndPersistEntry(): HealthEntry {
        val entry = buildEntry()
        DiaryStorage.saveLastEntryToPrefs(this, entry)       // SharedPreferences
        DiaryStorage.appendHistoryInternal(this, entry)      // Внутренний бинарный файл (S10)
        return entry
    }

    // Формирую объект HealthEntry из состояния всех чекбоксов.
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

    // Восстанавливаю чекбоксы из сохранённой записи.
    private fun applyEntryToCheckboxes(entry: HealthEntry) {
        val d = entry.diseases
        val s = entry.symptoms
        val t = entry.triggers

        fun has(list: List<String>, resId: Int) = list.contains(getString(resId))

        cbDiabetes.isChecked = has(d, R.string.diabetes)
        cbHypertension.isChecked = has(d, R.string.hypertension)
        cbMigraine.isChecked = has(d, R.string.migraine)
        cbAsthma.isChecked = has(d, R.string.asthma)

        cbHeadache.isChecked = has(s, R.string.sym_headache)
        cbDizziness.isChecked = has(s, R.string.sym_dizziness)
        cbNausea.isChecked = has(s, R.string.sym_nausea)
        cbChestPain.isChecked = has(s, R.string.sym_chest_pain)
        cbDyspnea.isChecked = has(s, R.string.sym_dyspnea)
        cbWeakness.isChecked = has(s, R.string.sym_weakness)
        cbTremor.isChecked = has(s, R.string.sym_tremor)
        cbThirst.isChecked = has(s, R.string.sym_thirst)
        cbBlurredVision.isChecked = has(s, R.string.sym_blurred_vision)

        cbStress.isChecked = has(t, R.string.tr_stress)
        cbLackOfSleep.isChecked = has(t, R.string.tr_lack_of_sleep)
        cbWeather.isChecked = has(t, R.string.tr_weather)
        cbMissedMed.isChecked = has(t, R.string.tr_missed_med)
        cbCaffeine.isChecked = has(t, R.string.tr_caffeine)
        cbWorkout.isChecked = has(t, R.string.tr_workout)
        cbDiet.isChecked = has(t, R.string.tr_diet)
    }

    // Формирование подсказок по сочетанию симптомов/триггеров.
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

    private fun clearAllCheckboxes() {
        val boxes = listOf(
            cbDiabetes, cbHypertension, cbMigraine, cbAsthma,
            cbHeadache, cbDizziness, cbNausea, cbChestPain, cbDyspnea,
            cbWeakness, cbTremor, cbThirst, cbBlurredVision,
            cbStress, cbLackOfSleep, cbWeather, cbMissedMed,
            cbCaffeine, cbWorkout, cbDiet
        )
        boxes.forEach { it.isChecked = false }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_REPORT_TEXT, tvReport.text?.toString() ?: "")
    }

    companion object {
        private const val STATE_REPORT_TEXT = "state_report_text"
    }
}

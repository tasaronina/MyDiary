package com.example.mydiary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.model.HealthEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    // Текстовое поле, куда я вывожу готовый отчёт.
    private lateinit var tvReport: TextView
    // Кнопка для построения отчёта по выбранным чекбоксам.
    private lateinit var btnBuildReport: Button
    // Кнопка для открытия новой страницы со списком рекомендаций (ЛР5).
    private lateinit var btnOpenAdviceList: Button
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

        // Обработчик нажатия на кнопку "Сформировать отчёт".
        btnBuildReport.setOnClickListener {
            // Собираю данные из чекбоксов в объект HealthEntry.
            val entry = buildEntry()
            // Показываю сформированный текст отчёта на текущем экране.
            tvReport.text = entry.report
        }

        // Кнопка для перехода на новый экран со списком рекомендаций (ЛР5).
        btnOpenAdviceList.setOnClickListener {
            startActivity(Intent(this, AdviceListActivity::class.java))
        }

        // Настраиваю нижнее меню: на главном экране активен пункт "Главная".
        bottomNav.selectedItemId = R.id.menu_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Если пользователь нажал "Главная", остаёмся на этом экране.
                R.id.menu_home -> true
                // Переход к экрану "Сводка".
                R.id.menu_summary -> {
                    startActivity(
                        Intent(this, SummaryActivity::class.java)
                            // Передаю объект HealthEntry через Intent
                            // в соответствии с требованием варианта T1
                            .putExtra(HealthEntry.EXTRA_KEY, buildEntry())
                    )
                    true
                }
                // Переход к экрану "Подсказки".
                R.id.menu_tips -> {
                    startActivity(
                        Intent(this, TipsActivity::class.java)
                            // Точно так же прикладываю к Intent сформированные данные.
                            .putExtra(HealthEntry.EXTRA_KEY, buildEntry())
                    )
                    true
                }
                else -> false
            }
        }

        // Восстанавливаю текст отчёта при перевороте экрана,
        // чтобы данные не терялись при смене конфигурации.
        savedInstanceState?.getString(STATE_REPORT_TEXT)?.let { tvReport.text = it }
    }

    // Отдельный метод, где я "привязываю" все поля и чекбоксы к id из layout.
    private fun bindViews() {
        tvReport = findViewById(R.id.tvReport)
        btnBuildReport = findViewById(R.id.btnBuildReport)
        btnOpenAdviceList = findViewById(R.id.btnOpenAdviceList)
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

    // Метод, который формирует объект HealthEntry на основе состояния всех чекбоксов.
    private fun buildEntry(): HealthEntry {
        // Форматирую текущую дату и время для отчёта.
        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date())

        // Собираю выбранные заболевания.
        val diseases = selectedLabels(
            cbDiabetes to getString(R.string.diabetes),
            cbHypertension to getString(R.string.hypertension),
            cbMigraine to getString(R.string.migraine),
            cbAsthma to getString(R.string.asthma)
        )

        // Собираю отмеченные симптомы.
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

        // Собираю возможные триггеры.
        val triggers = selectedLabels(
            cbStress to getString(R.string.tr_stress),
            cbLackOfSleep to getString(R.string.tr_lack_of_sleep),
            cbWeather to getString(R.string.tr_weather),
            cbMissedMed to getString(R.string.tr_missed_med),
            cbCaffeine to getString(R.string.tr_caffeine),
            cbWorkout to getString(R.string.tr_workout),
            cbDiet to getString(R.string.tr_diet)
        )

        // Формирую многострочный текст отчёта.
        val report = buildString {
            appendLine(getString(R.string.report_title))
            appendLine(getString(R.string.report_dt, dateStr))
            appendLine()
            appendLine(getString(R.string.report_diseases, fmtOrDash(diseases)))
            appendLine(getString(R.string.report_symptoms, fmtOrDash(symptoms)))
            appendLine(getString(R.string.report_triggers, fmtOrDash(triggers)))
            appendLine()

            // На основе выбранных значений формирую список подсказок.
            val hints = makeHints(diseases, symptoms, triggers)
            if (hints.isNotEmpty()) {
                appendLine(getString(R.string.hints_title))
                hints.forEachIndexed { i, h -> appendLine("${i + 1}) $h") }
            } else {
                appendLine(getString(R.string.hints_empty))
            }
            appendLine()
            // В конце добавляю дисклеймер, что это не является мед. рекомендацией.
            appendLine(getString(R.string.disclaimer))
        }

        // Возвращаю готовый объект, который можно показать на этом экране
        // и передать в другие активности через Intent.
        return HealthEntry(dateStr, diseases, symptoms, triggers, report)
    }

    // Функция подбирает подсказки (рекомендации) в зависимости от того,
    // какие заболевания, симптомы и триггеры были отмечены.
    private fun makeHints(
        d: List<String>,
        s: List<String>,
        t: List<String>
    ): List<String> {
        val h = mutableListOf<String>()
        // Вспомогательная функция: проверяю, присутствует ли строка в любом из списков.
        fun has(x: String) = x in (d + s + t)

        // Пример: если мигрень + триггеры стресс/недосып/кофеин — добавляю подсказку про мигрень.
        if (has(getString(R.string.migraine)) &&
            (has(getString(R.string.tr_stress)) ||
                    has(getString(R.string.tr_lack_of_sleep)) ||
                    has(getString(R.string.tr_caffeine)))
        ) {
            h += getString(R.string.hint_migraine)
        }

        // Для гипертонии и типичных симптомов добавляю соответствующую подсказку.
        if (has(getString(R.string.hypertension)) &&
            (has(getString(R.string.sym_chest_pain)) ||
                    has(getString(R.string.sym_dizziness)))
        ) {
            h += getString(R.string.hint_hypertension)
        }

        // Для астмы учитываю одышку, нагрузку и погоду.
        if (has(getString(R.string.asthma)) &&
            (has(getString(R.string.sym_dyspnea)) ||
                    has(getString(R.string.tr_workout)) ||
                    has(getString(R.string.tr_weather)))
        ) {
            h += getString(R.string.hint_asthma)
        }

        // Для диабета обращаю внимание на жажду и туман в глазах.
        if (has(getString(R.string.diabetes)) &&
            (has(getString(R.string.sym_thirst)) ||
                    has(getString(R.string.sym_blurred_vision)))
        ) {
            h += getString(R.string.hint_diabetes)
        }

        return h
    }

    // Если ни один элемент не выбран, возвращаю длинное тире,
    // иначе соединяю все элементы через запятую.
    private fun fmtOrDash(items: List<String>) =
        if (items.isEmpty()) "—" else items.joinToString(", ")

    // Универсальная функция: принимает пары "чекбокс — подпись"
    // и возвращает список только тех подписей, у которых чекбокс отмечен.
    private fun selectedLabels(vararg pairs: Pair<CheckBox, String>): List<String> =
        pairs.filter { it.first.isChecked }.map { it.second }

    // Сохраняю текст отчёта в Bundle при смене конфигурации (например, поворот экрана),
    // чтобы после пересоздания активности восстановить его.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_REPORT_TEXT, tvReport.text?.toString() ?: "")
    }

    companion object {
        // Ключ для сохранения и восстановления текста отчёта.
        private const val STATE_REPORT_TEXT = "state_report_text"
    }
}

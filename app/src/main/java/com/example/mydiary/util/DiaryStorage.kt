package com.example.mydiary.util

import android.content.Context
import android.os.Environment
import com.example.mydiary.model.HealthEntry
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object DiaryStorage {



    private const val PREFS_NAME = "health_diary_prefs"

    private const val KEY_LAST_DT = "last_dt"
    private const val KEY_LAST_DISEASES = "last_diseases"
    private const val KEY_LAST_SYMPTOMS = "last_symptoms"
    private const val KEY_LAST_TRIGGERS = "last_triggers"
    private const val KEY_LAST_REPORT = "last_report"

    // Сохраняю последнюю запись дневника в SharedPreferences.
    fun saveLastEntryToPrefs(context: Context, entry: HealthEntry) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_DT, entry.dateTime)
            .putString(KEY_LAST_DISEASES, encodeList(entry.diseases))
            .putString(KEY_LAST_SYMPTOMS, encodeList(entry.symptoms))
            .putString(KEY_LAST_TRIGGERS, encodeList(entry.triggers))
            .putString(KEY_LAST_REPORT, entry.report)
            .apply()
    }

    // Читаю последнюю запись из SharedPreferences. Если её нет — возвращаю null.
    fun loadLastEntryFromPrefs(context: Context): HealthEntry? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dt = prefs.getString(KEY_LAST_DT, null) ?: return null

        val diseases = decodeList(prefs.getString(KEY_LAST_DISEASES, null))
        val symptoms = decodeList(prefs.getString(KEY_LAST_SYMPTOMS, null))
        val triggers = decodeList(prefs.getString(KEY_LAST_TRIGGERS, null))
        val report = prefs.getString(KEY_LAST_REPORT, "") ?: ""

        return HealthEntry(
            dateTime = dt,
            diseases = diseases,
            symptoms = symptoms,
            triggers = triggers,
            report = report
        )
    }

    // Полностью очищаю сохранённые данные в SharedPreferences.
    fun clearPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun encodeList(list: List<String>): String =
        list.joinToString("|")

    private fun decodeList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split("|")


    /* бинарный файл во внутреннем хранилище (history.bin) */

    private const val HISTORY_FILE = "history.bin"

    // Добавляю строку в историю отчётов. Каждая строка пишется в бинарный файл через writeUTF.
    fun appendHistoryInternal(context: Context, entry: HealthEntry) {
        val line = buildString {
            append(entry.dateTime)
            append(" — ")
            append(
                if (entry.symptoms.isEmpty())
                    "симптомы не отмечены"
                else
                    entry.symptoms.joinToString(", ")
            )
        }

        try {
            context.openFileOutput(HISTORY_FILE, Context.MODE_APPEND).use { fos ->
                DataOutputStream(fos).use { out ->
                    out.writeUTF(line)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Читаю все строки истории из бинарного файла. Каждую строку читает readUTF до EOF.
    fun readHistoryInternal(context: Context): List<String> {
        val result = mutableListOf<String>()
        try {
            context.openFileInput(HISTORY_FILE).use { fis ->
                DataInputStream(fis).use { input ->
                    while (true) {
                        val line = input.readUTF()
                        result += line
                    }
                }
            }
        } catch (_: FileNotFoundException) {
            // файл ещё не создан — истории нет
        } catch (_: EOFException) {
            // дошли до конца файла — это нормальная ситуация
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    // Полностью удаляю файл истории из внутреннего хранилища.
    fun clearHistoryInternal(context: Context) {
        context.deleteFile(HISTORY_FILE)
    }


    /* бинарный файл в общем (shared) хранилище */

    private const val EXPORT_FILE_NAME = "health_report_export.bin"

    // Получаю файл в общем (external) хранилище приложения в папке Documents.
    private fun getExternalReportFile(context: Context): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return if (dir != null && (dir.exists() || dir.mkdirs())) {
            File(dir, EXPORT_FILE_NAME)
        } else {
            null
        }
    }

    // Записываю полный текст отчёта в бинарный файл в общем хранилище.
    fun writeExternalReport(context: Context, report: String) {
        val file = getExternalReportFile(context) ?: return
        try {
            FileOutputStream(file).use { fos ->
                DataOutputStream(fos).use { out ->
                    out.writeUTF(report)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Читаю экспортированный отчёт из бинарного файла. Если файла нет — null.
    fun readExternalReport(context: Context): String? {
        val file = getExternalReportFile(context) ?: return null
        if (!file.exists()) return null
        return try {
            FileInputStream(file).use { fis ->
                DataInputStream(fis).use { input ->
                    input.readUTF()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Удаляю бинарный файл с экспортированным отчётом из общего хранилища.
    fun deleteExternalReport(context: Context) {
        val file = getExternalReportFile(context) ?: return
        if (file.exists()) {
            file.delete()
        }
    }
}

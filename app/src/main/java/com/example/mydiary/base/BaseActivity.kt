package com.example.mydiary.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.mydiary.MainActivity
import com.example.mydiary.R
import com.example.mydiary.SummaryActivity
import com.example.mydiary.TipsActivity
import com.example.mydiary.model.HealthEntry
import com.google.android.material.bottomnavigation.BottomNavigationView


abstract class BaseActivity : AppCompatActivity() {


    protected fun setupBottomNav(
        selectedItemId: Int,
        provideEntry: (() -> HealthEntry?)? = null
    ) {
        // Находим BottomNavigationView на текущем экране
        val nav = findViewById<BottomNavigationView>(R.id.bottomNav) ?: return

        // Подсвечиваем текущий пункт меню как выбранный.
        nav.selectedItemId = selectedItemId

        // Назначаю обработчик нажатий на пункты нижнего меню.
        nav.setOnItemSelectedListener { item ->
            // Если пользователь нажал на уже активный пункт, ничего не делаем.
            if (item.itemId == selectedItemId) return@setOnItemSelectedListener true

            // При необходимости получаю текущие данные для передачи в другую активность.
            val entry = provideEntry?.invoke()

            // В зависимости от того, какой пункт меню выбран,
            // открываю соответствующую активность и при необходимости добавляю в Intent объект HealthEntry.
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        if (entry != null) putExtra(HealthEntry.EXTRA_KEY, entry)
                    })
                }
                R.id.menu_summary -> {
                    startActivity(Intent(this, SummaryActivity::class.java).apply {
                        if (entry != null) putExtra(HealthEntry.EXTRA_KEY, entry)
                    })
                }
                R.id.menu_tips -> {
                    startActivity(Intent(this, TipsActivity::class.java).apply {
                        if (entry != null) putExtra(HealthEntry.EXTRA_KEY, entry)
                    })
                }
            }

            true
        }
    }
}

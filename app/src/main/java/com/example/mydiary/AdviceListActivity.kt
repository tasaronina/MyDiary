package com.example.mydiary

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiary.model.Advice
import com.example.mydiary.util.AdviceAdapter


class AdviceListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdviceAdapter
    private lateinit var btnBack: Button

    // Коллекция объектов, которые отображаются в списке.
    private val items = mutableListOf<Advice>()

    // Позиция элемента, с которым сейчас работает контекстное меню.
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    // Счётчик для генерации уникальных id.
    private var nextId: Long = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice_list)

        recyclerView = findViewById(R.id.rvAdvice)
        btnBack = findViewById(R.id.btnAdviceBack)

        // Кнопка "Назад к дневнику" просто закрывает текущую активность
        // и возвращает пользователя на предыдущий экран (MainActivity).
        btnBack.setOnClickListener {
            finish()
        }

        // O1, N1 – вертикальный список, по одному элементу в строке.
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdviceAdapter(items) { position ->
            // Запоминаю выбранную позицию и открываю контекстное меню.
            selectedPosition = position
            openContextMenu(recyclerView)
        }
        recyclerView.adapter = adapter

        // Регистрирую RecyclerView для работы с контекстным меню (C1, U1).
        registerForContextMenu(recyclerView)

        // Заполняю список несколькими стартовыми рекомендациями.
        seedInitialData()

        // Подключаю свайпы для удаления элементов (D1).
        attachSwipeToDelete()
    }

    private fun seedInitialData() {
        items.add(
            Advice(
                id = nextId++,
                title = "Контроль давления",
                text = "Измерять артериальное давление утром и вечером и записывать значения в дневник."
            )
        )
        items.add(
            Advice(
                id = nextId++,
                title = "Режим сна",
                text = "Стараться спать не менее 7–8 часов, ложиться и вставать в одно и то же время."
            )
        )
        items.add(
            Advice(
                id = nextId++,
                title = "Физическая активность",
                text = "Ежедневная прогулка 20–30 минут в удобном темпе при отсутствии противопоказаний."
            )
        )

        adapter.notifyDataSetChanged()
    }

    private fun attachSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Перетаскивание элементов по условию варианта не требуется.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Свайп по элементу — удаляю его из коллекции и уведомляю адаптер.
                val pos = viewHolder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items.removeAt(pos)
                    adapter.notifyItemRemoved(pos)
                }
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    // Создание контекстного меню
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.advice_context_menu, menu)
    }

    // Обработка выбора пункта контекстного меню.
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                // Создание новой записи через контекстное меню.
                showEditDialog(isNew = true)
                true
            }

            R.id.action_edit -> {
                // Редактирование выбранного элемента через контекстное меню.
                if (selectedPosition != RecyclerView.NO_POSITION) {
                    showEditDialog(isNew = false)
                }
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    // Диалог для добавления/редактирования записи.
    private fun showEditDialog(isNew: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_advice_edit, null)
        val etTitle: EditText = dialogView.findViewById(R.id.etAdviceTitle)
        val etText: EditText = dialogView.findViewById(R.id.etAdviceText)

        if (!isNew && selectedPosition != RecyclerView.NO_POSITION) {
            val advice = items[selectedPosition]
            etTitle.setText(advice.title)
            etText.setText(advice.text)
        }

        val dialogTitle = if (isNew) {
            getString(R.string.advice_dialog_add_title)
        } else {
            getString(R.string.advice_dialog_edit_title)
        }

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(R.string.advice_action_save) { _, _ ->
                val title = etTitle.text.toString().trim()
                val text = etText.text.toString().trim()

                if (isNew) {
                    // Создаю новый объект и добавляю его в коллекцию.
                    val advice = Advice(
                        id = nextId++,
                        title = if (title.isNotEmpty()) title else getString(R.string.advice_default_title),
                        text = if (text.isNotEmpty()) text else getString(R.string.advice_default_text)
                    )
                    items.add(advice)
                    adapter.notifyItemInserted(items.lastIndex)
                } else if (selectedPosition != RecyclerView.NO_POSITION) {
                    // Обновляю существующий элемент.
                    val advice = items[selectedPosition]
                    advice.title = if (title.isNotEmpty()) title else advice.title
                    advice.text = if (text.isNotEmpty()) text else advice.text
                    adapter.notifyItemChanged(selectedPosition)
                }
            }
            .setNegativeButton(R.string.advice_action_cancel, null)
            .show()
    }
}

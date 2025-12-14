package com.example.mydiary

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiary.data.AdviceCategoryEntity
import com.example.mydiary.data.AdviceDao
import com.example.mydiary.data.AdviceEntity
import com.example.mydiary.data.AppDatabase
import com.example.mydiary.model.Advice
import com.example.mydiary.util.AdviceAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdviceListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdviceAdapter
    private lateinit var btnBack: Button

    // Коллекция для отображения в RecyclerView.
    private val items = mutableListOf<Advice>()

    // Позиция элемента, с которым работает контекстное меню.
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    // Room: ссылка на DAO и id категории "по умолчанию".
    private lateinit var adviceDao: AdviceDao
    private var defaultCategoryId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyDiary)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice_list)

        // Инициализирую БД и DAO.
        val db = AppDatabase.getInstance(applicationContext)
        adviceDao = db.adviceDao()

        recyclerView = findViewById(R.id.rvAdvice)
        btnBack = findViewById(R.id.btnAdviceBack)

        // Кнопка "Назад к дневнику" — просто закрываю экран.
        btnBack.setOnClickListener {
            finish()
        }

        // Вертикальный список.
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdviceAdapter(items) { position ->
            // Запоминаю выбранную позицию и открываю контекстное меню.
            selectedPosition = position
            openContextMenu(recyclerView)
        }
        recyclerView.adapter = adapter

        // Регистрирую RecyclerView для контекстного меню.
        registerForContextMenu(recyclerView)

        // Гружу данные из БД.
        loadDataFromDb()

        // Подключаю свайпы для удаления элементов.
        attachSwipeToDelete()
    }

    // Загружаю категорию и советы из БД, при первом запуске — создаю начальные записи.
    private fun loadDataFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Категория по умолчанию для всех советов (простая группировка).
            val categoryName = "Общие рекомендации"
            val existingCategory = adviceDao.getCategoryByName(categoryName)
            val categoryId = if (existingCategory != null) {
                existingCategory.id
            } else {
                val newId = adviceDao.insertCategory(
                    AdviceCategoryEntity(name = categoryName)
                )
                newId
            }
            defaultCategoryId = categoryId

            // Читаю существующие советы по категории.
            var advices = adviceDao.getAdvicesByCategory(defaultCategoryId)

            // Если в таблице пусто — заполняю её стартовыми данными (как раньше в seedInitialData()).
            if (advices.isEmpty()) {
                val initialEntities = listOf(
                    AdviceEntity(
                        title = "Контроль давления",
                        text = "Измерять артериальное давление утром и вечером и записывать значения в дневник.",
                        categoryId = defaultCategoryId
                    ),
                    AdviceEntity(
                        title = "Режим сна",
                        text = "Стараться спать не менее 7–8 часов, ложиться и вставать в одно и то же время.",
                        categoryId = defaultCategoryId
                    ),
                    AdviceEntity(
                        title = "Физическая активность",
                        text = "Ежедневная прогулка 20–30 минут в удобном темпе при отсутствии противопоказаний.",
                        categoryId = defaultCategoryId
                    )
                )
                val ids = adviceDao.insertAdvices(initialEntities)
                // Перечитываю, чтобы получить все поля вместе с id.
                advices = adviceDao.getAdvicesByCategory(defaultCategoryId)
            }

            // Преобразую сущности Room в модель для UI.
            val uiItems = advices.map { entity ->
                Advice(
                    id = entity.id,
                    title = entity.title,
                    text = entity.text
                )
            }

            // Обновляю список на главном потоке.
            withContext(Dispatchers.Main) {
                items.clear()
                items.addAll(uiItems)
                adapter.notifyDataSetChanged()
            }
        }
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
                // Перетаскивание мне здесь не нужно.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val advice = items[pos]

                    // Сначала удаляю из БД, потом из списка.
                    lifecycleScope.launch(Dispatchers.IO) {
                        val entity = AdviceEntity(
                            id = advice.id,
                            title = advice.title,
                            text = advice.text,
                            categoryId = defaultCategoryId
                        )
                        adviceDao.deleteAdvice(entity)

                        withContext(Dispatchers.Main) {
                            items.removeAt(pos)
                            adapter.notifyItemRemoved(pos)
                        }
                    }
                }
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    // Создание контекстного меню.
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
                // Создание новой записи.
                showEditDialog(isNew = true)
                true
            }

            R.id.action_edit -> {
                // Редактирование выбранного элемента.
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
                val titleInput = etTitle.text.toString().trim()
                val textInput = etText.text.toString().trim()

                val finalTitle = if (titleInput.isNotEmpty()) {
                    titleInput
                } else {
                    if (isNew) getString(R.string.advice_default_title)
                    else items.getOrNull(selectedPosition)?.title ?: getString(R.string.advice_default_title)
                }

                val finalText = if (textInput.isNotEmpty()) {
                    textInput
                } else {
                    if (isNew) getString(R.string.advice_default_text)
                    else items.getOrNull(selectedPosition)?.text ?: getString(R.string.advice_default_text)
                }

                if (isNew) {
                    // Добавляю новую запись в БД и в список.
                    lifecycleScope.launch(Dispatchers.IO) {
                        val entity = AdviceEntity(
                            title = finalTitle,
                            text = finalText,
                            categoryId = defaultCategoryId
                        )
                        val newId = adviceDao.insertAdvice(entity)

                        val newAdvice = Advice(
                            id = newId,
                            title = finalTitle,
                            text = finalText
                        )

                        withContext(Dispatchers.Main) {
                            items.add(newAdvice)
                            adapter.notifyItemInserted(items.lastIndex)
                        }
                    }
                } else if (selectedPosition != RecyclerView.NO_POSITION) {
                    val oldAdvice = items[selectedPosition]

                    // Обновляю модель и БД.
                    val updatedAdvice = oldAdvice.copy(
                        title = finalTitle,
                        text = finalText
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        val entity = AdviceEntity(
                            id = updatedAdvice.id,
                            title = updatedAdvice.title,
                            text = updatedAdvice.text,
                            categoryId = defaultCategoryId
                        )
                        adviceDao.updateAdvice(entity)

                        withContext(Dispatchers.Main) {
                            items[selectedPosition] = updatedAdvice
                            adapter.notifyItemChanged(selectedPosition)
                        }
                    }
                }
            }
            .setNegativeButton(R.string.advice_action_cancel, null)
            .show()
    }
}

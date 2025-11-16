package com.example.mydiary.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiary.R
import com.example.mydiary.model.Advice

// Адаптер для отображения списка объектов Advice в RecyclerView
class AdviceAdapter(
    private val items: MutableList<Advice>,
    // Колбэк, который вызывается при долгом нажатии на элемент (для контекстного меню)
    private val onItemLongClick: (position: Int) -> Unit
) : RecyclerView.Adapter<AdviceAdapter.AdviceViewHolder>() {

    inner class AdviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvAdviceTitle)
        val tvText: TextView = itemView.findViewById(R.id.tvAdviceText)

        init {
            // Долгое нажатие по элементу списка — запоминаем позицию
            // и даём активити команду открыть контекстное меню
            itemView.setOnLongClickListener {

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemLongClick(pos)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_advice, parent, false)
        return AdviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        val advice = items[position]
        holder.tvTitle.text = advice.title
        holder.tvText.text = advice.text
    }

    override fun getItemCount(): Int = items.size
}

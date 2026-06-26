package com.example.todolist

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: ArrayList<Task>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBoxTask: CheckBox = itemView.findViewById(R.id.checkBoxTask)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.checkBoxTask.setOnCheckedChangeListener(null)

        holder.checkBoxTask.text = task.title
        holder.checkBoxTask.isChecked = task.isDone

        if (task.isDone) {
            holder.checkBoxTask.paintFlags =
                holder.checkBoxTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.checkBoxTask.paintFlags =
                holder.checkBoxTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        if (task.priority.isNullOrBlank()) {
            holder.tvPriority.visibility = View.GONE
        } else {
            holder.tvPriority.visibility = View.VISIBLE
            holder.tvPriority.text = "● ${task.priority}"

            when (task.priority) {
                "High" -> holder.tvPriority.setTextColor(Color.parseColor("#EF4444"))
                "Medium" -> holder.tvPriority.setTextColor(Color.parseColor("#F59E0B"))
                "Low" -> holder.tvPriority.setTextColor(Color.parseColor("#10B981"))
                else -> holder.tvPriority.setTextColor(Color.parseColor("#3B82F6"))
            }
        }

        val infoText = listOfNotNull(
            task.date?.takeIf { it.isNotBlank() },
            task.time?.takeIf { it.isNotBlank() }
        ).joinToString(" • ")

        if (infoText.isBlank()) {
            holder.tvDueDate.visibility = View.GONE
        } else {
            holder.tvDueDate.visibility = View.VISIBLE
            holder.tvDueDate.text = infoText
        }

        holder.checkBoxTask.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked

            if (isChecked) {
                holder.checkBoxTask.paintFlags =
                    holder.checkBoxTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.checkBoxTask.paintFlags =
                    holder.checkBoxTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }
}
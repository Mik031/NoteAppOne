package com.example.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ExpenseAdapter(
    private val expenses: ArrayList<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExpenseName: TextView = itemView.findViewById(R.id.tvExpenseName)
        val tvExpenseDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
        val tvExpenseAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        val tvExpenseCategory: TextView = itemView.findViewById(R.id.tvExpenseCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)

        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.tvExpenseName.text = expense.name
        holder.tvExpenseDate.text = expense.date
        holder.tvExpenseCategory.text = expense.category

        val sign = if (expense.type == "Income") "+" else "-"
        holder.tvExpenseAmount.text =
            String.format(Locale.US, "%s$%.2f", sign, expense.amount)
    }

    override fun getItemCount(): Int = expenses.size
}
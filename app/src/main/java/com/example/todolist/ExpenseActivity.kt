package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ExpenseActivity : AppCompatActivity() {

    private lateinit var db: ExpenseDatabaseHelper
    private lateinit var adapter: ExpenseAdapter

    private lateinit var recyclerViewExpenses: RecyclerView
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var btnAddExpense: ImageButton

    private val expenseList = ArrayList<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        db = ExpenseDatabaseHelper(this)

        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        btnAddExpense = findViewById(R.id.btnAddExpense)

        adapter = ExpenseAdapter(expenseList)

        recyclerViewExpenses.layoutManager = LinearLayoutManager(this)
        recyclerViewExpenses.adapter = adapter

        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        expenseList.clear()
        expenseList.addAll(db.getTransactions())
        adapter.notifyDataSetChanged()

        val income = db.getTotalIncome()
        val expense = db.getTotalExpense()
        val balance = db.getBalance()

        tvTotalBalance.text = String.format(Locale.US, "$%.2f", balance)
        tvIncome.text = String.format(Locale.US, "Income\n$%.2f", income)
        tvExpense.text = String.format(Locale.US, "Expense\n$%.2f", expense)
    }
}
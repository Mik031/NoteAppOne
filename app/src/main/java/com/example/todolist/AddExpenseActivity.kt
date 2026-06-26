package com.example.todolist

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var db: ExpenseDatabaseHelper

    private lateinit var etExpenseName: EditText
    private lateinit var etExpenseAmount: EditText
    private lateinit var btnExpenseDate: Button
    private lateinit var btnSaveExpense: ImageButton
    private lateinit var btnAddCategory: Button

    private lateinit var chipIncome: TextView
    private lateinit var chipExpense: TextView
    private lateinit var spinnerCategory: Spinner

    private var selectedType = "Expense"
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        db = ExpenseDatabaseHelper(this)

        etExpenseName = findViewById(R.id.etExpenseName)
        etExpenseAmount = findViewById(R.id.etExpenseAmount)
        btnExpenseDate = findViewById(R.id.btnExpenseDate)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        chipIncome = findViewById(R.id.chipIncome)
        chipExpense = findViewById(R.id.chipExpense)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        setupTypeChips()
        loadCategories()

        btnExpenseDate.setOnClickListener {
            showDatePicker()
        }

        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        btnSaveExpense.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupTypeChips() {
        selectType("Expense")

        chipIncome.setOnClickListener {
            selectType("Income")
        }

        chipExpense.setOnClickListener {
            selectType("Expense")
        }
    }

    private fun selectType(type: String) {
        selectedType = type

        chipIncome.alpha = if (type == "Income") 1f else 0.45f
        chipExpense.alpha = if (type == "Expense") 1f else 0.45f

        loadCategories()
    }

    private fun loadCategories() {
        val categories = db.getCategories(selectedType)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Category name"

        AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setMessage("Create custom $selectedType category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isNotEmpty()) {
                    db.addCategory(name, selectedType)
                    loadCategories()
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                btnExpenseDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTransaction() {
        val name = etExpenseName.text.toString().trim()
        val amountText = etExpenseAmount.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()

        if (amount == null) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val category = spinnerCategory.selectedItem?.toString() ?: "Other"

        db.addTransaction(
            name = name,
            amount = amount,
            category = category,
            date = selectedDate,
            type = selectedType
        )

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
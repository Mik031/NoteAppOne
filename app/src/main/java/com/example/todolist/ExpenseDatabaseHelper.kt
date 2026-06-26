package com.example.todolist

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "finance.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                category TEXT NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """
        )

        insertDefaultCategories(db)
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val defaults = listOf(
            "Salary" to "Income",
            "Bonus" to "Income",
            "Freelance" to "Income",
            "Gift" to "Income",
            "Other Income" to "Income",

            "Food" to "Expense",
            "Shopping" to "Expense",
            "Transport" to "Expense",
            "Rent" to "Expense",
            "Bills" to "Expense",
            "Health" to "Expense",
            "Education" to "Expense",
            "Travel" to "Expense",
            "Other Expense" to "Expense"
        )

        defaults.forEach {
            db.execSQL(
                "INSERT INTO categories (name, type) VALUES (?, ?)",
                arrayOf(it.first, it.second)
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS categories")
        onCreate(db)
    }

    fun addTransaction(
        name: String,
        amount: Double,
        category: String,
        date: String,
        type: String
    ) {
        writableDatabase.execSQL(
            "INSERT INTO transactions (name, amount, category, date, type) VALUES (?, ?, ?, ?, ?)",
            arrayOf(name, amount, category, date, type)
        )
    }

    fun getTransactions(): ArrayList<Expense> {
        val list = ArrayList<Expense>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, name, amount, category, date, type FROM transactions ORDER BY id DESC",
            null
        )

        while (cursor.moveToNext()) {
            list.add(
                Expense(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                )
            )
        }

        cursor.close()
        return list
    }

    fun getCategories(type: String): ArrayList<String> {
        val list = ArrayList<String>()
        val cursor = readableDatabase.rawQuery(
            "SELECT name FROM categories WHERE type = ? ORDER BY name ASC",
            arrayOf(type)
        )

        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }

        cursor.close()
        return list
    }

    fun addCategory(name: String, type: String) {
        writableDatabase.execSQL(
            "INSERT INTO categories (name, type) VALUES (?, ?)",
            arrayOf(name, type)
        )
    }

    fun getTotalIncome(): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(amount) FROM transactions WHERE type = 'Income'",
            null
        )

        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }

    fun getTotalExpense(): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(amount) FROM transactions WHERE type = 'Expense'",
            null
        )

        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }

    fun getBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }
}
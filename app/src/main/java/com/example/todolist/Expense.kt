package com.example.todolist

data class Expense(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String
)
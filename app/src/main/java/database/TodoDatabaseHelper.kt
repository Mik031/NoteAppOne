package com.example.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TodoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "todo.db", null, 6) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE todos (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                date TEXT,
                time TEXT,
                priority TEXT,
                isDone INTEGER NOT NULL DEFAULT 0
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS todos")
        onCreate(db)
    }

    fun addTask(task: Task) {
        val values = ContentValues().apply {
            put("id", task.id)
            put("title", task.title)
            put("date", task.date)
            put("time", task.time)
            put("priority", task.priority)
            put("isDone", if (task.isDone) 1 else 0)
        }

        writableDatabase.insert("todos", null, values)
    }

    fun getTasks(): ArrayList<Task> {
        val tasks = ArrayList<Task>()

        val cursor = readableDatabase.rawQuery(
            "SELECT id, title, date, time, priority, isDone FROM todos ORDER BY id DESC",
            null
        )

        while (cursor.moveToNext()) {
            tasks.add(
                Task(
                    id = cursor.getLong(0),
                    title = cursor.getString(1),
                    date = cursor.getString(2),
                    time = cursor.getString(3),
                    priority = cursor.getString(4),
                    isDone = cursor.getInt(5) == 1
                )
            )
        }

        cursor.close()
        return tasks
    }

    fun deleteTask(id: Long) {
        writableDatabase.delete(
            "todos",
            "id = ?",
            arrayOf(id.toString())
        )
    }
}
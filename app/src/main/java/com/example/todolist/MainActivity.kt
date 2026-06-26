package com.example.todolist

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val tasks = ArrayList<Task>()
    private lateinit var adapter: TaskAdapter

    private lateinit var etTask: EditText
    private lateinit var chipHigh: TextView
    private lateinit var chipMedium: TextView
    private lateinit var chipLow: TextView
    private lateinit var btnDueDate: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var btnExpenseScreen: ImageButton
    private lateinit var recyclerViewTasks: RecyclerView

    private var selectedPriority: String? = null
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedReminderTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etTask = findViewById(R.id.etTask)
        chipHigh = findViewById(R.id.chipHigh)
        chipMedium = findViewById(R.id.chipMedium)
        chipLow = findViewById(R.id.chipLow)
        btnDueDate = findViewById(R.id.btnDueDate)
        btnAdd = findViewById(R.id.btnAdd)
        btnExpenseScreen = findViewById(R.id.btnExpenseScreen)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)

        adapter = TaskAdapter(tasks)

        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = adapter

        chipHigh.setOnClickListener { selectPriority("High") }
        chipMedium.setOnClickListener { selectPriority("Medium") }
        chipLow.setOnClickListener { selectPriority("Low") }

        btnDueDate.setOnClickListener { showDatePicker() }

        btnAdd.setOnClickListener { addTask() }

        btnExpenseScreen.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }
    }

    private fun selectPriority(priority: String) {
        selectedPriority = if (selectedPriority == priority) null else priority

        resetChipColors()

        chipHigh.alpha = if (selectedPriority == "High") 1f else 0.5f
        chipMedium.alpha = if (selectedPriority == "Medium") 1f else 0.5f
        chipLow.alpha = if (selectedPriority == "Low") 1f else 0.5f
    }

    private fun resetChipColors() {
        chipHigh.setBackgroundResource(R.drawable.chip_high)
        chipMedium.setBackgroundResource(R.drawable.chip_medium)
        chipLow.setBackgroundResource(R.drawable.chip_low)

        chipHigh.alpha = 1f
        chipMedium.alpha = 1f
        chipLow.alpha = 1f
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = "$year-${month + 1}-$day"

                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        selectedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hour,
                            minute
                        )

                        val reminderCalendar = Calendar.getInstance()
                        reminderCalendar.set(Calendar.YEAR, year)
                        reminderCalendar.set(Calendar.MONTH, month)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, day)
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        reminderCalendar.set(Calendar.MINUTE, minute)
                        reminderCalendar.set(Calendar.SECOND, 0)
                        reminderCalendar.set(Calendar.MILLISECOND, 0)

                        selectedReminderTime = reminderCalendar.timeInMillis

                        Toast.makeText(
                            this,
                            "Reminder: $selectedDate $selectedTime",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addTask() {
        val title = etTask.text.toString().trim()

        if (title.isEmpty()) {
            etTask.error = "Task required"
            return
        }

        val task = Task(
            title = title,
            date = selectedDate,
            time = selectedTime,
            priority = selectedPriority,
            reminderTime = selectedReminderTime
        )

        tasks.add(task)
        adapter.notifyItemInserted(tasks.size - 1)

        scheduleReminder(task)

        etTask.text.clear()
        selectedPriority = null
        selectedDate = null
        selectedTime = null
        selectedReminderTime = null
        resetChipColors()

        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleReminder(task: Task) {
        val reminderTime = task.reminderTime ?: return

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
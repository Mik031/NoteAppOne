package com.example.noteappux

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TextShortcutsActivity : AppCompatActivity() {

    private lateinit var btnEnableShortcutService: TextView
    private lateinit var btnSortTextShortcuts: TextView
    private lateinit var recyclerViewTextShortcuts: RecyclerView
    private lateinit var tvEmptyTextShortcuts: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var textShortcutAdapter: TextShortcutAdapter
    private lateinit var shortcutList: ArrayList<NoteShortcut>

    private var currentSortMode: Int = SORT_SHORTCUT_A_TO_Z

    companion object {
        private const val PREF_NAME = "text_shortcuts_sort_pref"
        private const val PREF_SORT_MODE = "sort_mode"

        private const val SORT_SHORTCUT_A_TO_Z = 0
        private const val SORT_SHORTCUT_Z_TO_A = 1
        private const val SORT_NOTE_TITLE_A_TO_Z = 2
        private const val SORT_NOTE_TITLE_Z_TO_A = 3
        private const val SORT_NEWEST_FIRST = 4
        private const val SORT_OLDEST_FIRST = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_shortcuts)

        btnEnableShortcutService = findViewById(R.id.btnEnableShortcutService)
        btnSortTextShortcuts = findViewById(R.id.btnSortTextShortcuts)
        recyclerViewTextShortcuts = findViewById(R.id.recyclerViewTextShortcuts)
        tvEmptyTextShortcuts = findViewById(R.id.tvEmptyTextShortcuts)

        databaseHelper = DatabaseHelper(this)

        currentSortMode = getSavedSortMode()
        updateSortButtonText()

        recyclerViewTextShortcuts.layoutManager = LinearLayoutManager(this)

        btnEnableShortcutService.setOnClickListener {
            openAccessibilitySettings()
        }

        btnSortTextShortcuts.setOnClickListener {
            showSortDialog()
        }

        updateServiceButtonText()
        loadTextShortcuts()
    }

    override fun onResume() {
        super.onResume()
        updateServiceButtonText()
        loadTextShortcuts()
    }

    private fun getSavedSortMode(): Int {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(PREF_SORT_MODE, SORT_SHORTCUT_A_TO_Z)
    }

    private fun saveSortMode(sortMode: Int) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .putInt(PREF_SORT_MODE, sortMode)
            .apply()
    }

    private fun updateSortButtonText() {
        btnSortTextShortcuts.text = when (currentSortMode) {
            SORT_SHORTCUT_A_TO_Z -> "A-Z"
            SORT_SHORTCUT_Z_TO_A -> "Z-A"
            SORT_NOTE_TITLE_A_TO_Z -> "Title A"
            SORT_NOTE_TITLE_Z_TO_A -> "Title Z"
            SORT_NEWEST_FIRST -> "Newest"
            SORT_OLDEST_FIRST -> "Oldest"
            else -> "Sort"
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Shortcut A to Z",
            "Shortcut Z to A",
            "Note Title A to Z",
            "Note Title Z to A",
            "Newest First",
            "Oldest First"
        )

        AlertDialog.Builder(this)
            .setTitle("Sort Text Shortcuts")
            .setSingleChoiceItems(sortOptions, currentSortMode) { dialog, which ->
                currentSortMode = which
                saveSortMode(currentSortMode)
                updateSortButtonText()
                loadTextShortcuts()

                dialog.dismiss()

                Toast.makeText(
                    this,
                    "Sorted by ${sortOptions[which]}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateServiceButtonText() {
        val isEnabled = AccessibilityHelper.isTextExpansionServiceEnabled(this)

        if (isEnabled) {
            btnEnableShortcutService.text = "Shortcut Service: Enabled"
        } else {
            btnEnableShortcutService.text = "Enable Shortcut Service"
        }
    }

    private fun openAccessibilitySettings() {
        Toast.makeText(
            this,
            "Find NoteAppOne and enable Text Shortcut Service",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun loadTextShortcuts() {
        shortcutList = databaseHelper.getAllActiveShortcuts()
        shortcutList = sortTextShortcuts(shortcutList)

        if (shortcutList.isEmpty()) {
            recyclerViewTextShortcuts.visibility = View.GONE
            tvEmptyTextShortcuts.visibility = View.VISIBLE
        } else {
            recyclerViewTextShortcuts.visibility = View.VISIBLE
            tvEmptyTextShortcuts.visibility = View.GONE
        }

        textShortcutAdapter = TextShortcutAdapter(
            shortcutList,
            onShortcutClick = { selectedShortcut ->
                openNoteForEditing(selectedShortcut)
            },
            onEditShortcutClick = { selectedShortcut ->
                showEditShortcutDialog(selectedShortcut)
            },
            onDeleteShortcutClick = { selectedShortcut ->
                showDeleteShortcutDialog(selectedShortcut)
            }
        )

        recyclerViewTextShortcuts.adapter = textShortcutAdapter
    }

    private fun sortTextShortcuts(originalList: ArrayList<NoteShortcut>): ArrayList<NoteShortcut> {
        val sortedList = when (currentSortMode) {
            SORT_SHORTCUT_A_TO_Z -> {
                originalList.sortedBy { it.shortcutKeyword.lowercase() }
            }

            SORT_SHORTCUT_Z_TO_A -> {
                originalList.sortedByDescending { it.shortcutKeyword.lowercase() }
            }

            SORT_NOTE_TITLE_A_TO_Z -> {
                originalList.sortedWith(
                    compareBy<NoteShortcut> { it.noteTitle.lowercase() }
                        .thenBy { it.shortcutKeyword.lowercase() }
                )
            }

            SORT_NOTE_TITLE_Z_TO_A -> {
                originalList.sortedWith(
                    compareByDescending<NoteShortcut> { it.noteTitle.lowercase() }
                        .thenBy { it.shortcutKeyword.lowercase() }
                )
            }

            SORT_NEWEST_FIRST -> {
                originalList.sortedByDescending { it.createdDate }
            }

            SORT_OLDEST_FIRST -> {
                originalList.sortedBy { it.createdDate }
            }

            else -> {
                originalList.sortedBy { it.shortcutKeyword.lowercase() }
            }
        }

        return ArrayList(sortedList)
    }

    private fun openNoteForEditing(shortcut: NoteShortcut) {
        val note = databaseHelper.getNoteById(shortcut.noteId)

        if (note == null) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            loadTextShortcuts()
            return
        }

        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra("note_id", note.id)
        intent.putExtra("note_title", note.title)
        intent.putExtra("note_content", note.content)
        intent.putExtra("note_date", note.date)
        startActivity(intent)
    }

    private fun showEditShortcutDialog(shortcut: NoteShortcut) {
        val input = EditText(this)
        input.hint = "Example: /dd"
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setText(shortcut.shortcutKeyword)
        input.setSelection(input.text.length)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("Edit Shortcut")
            .setMessage("Update shortcut keyword for \"${shortcut.noteTitle}\"")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val keyword = input.text.toString().trim()

                if (keyword.isEmpty()) {
                    Toast.makeText(this, "Shortcut keyword is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.updateNoteShortcutKeyword(
                    shortcutId = shortcut.id,
                    newKeyword = keyword
                )

                when {
                    result > 0 -> {
                        Toast.makeText(this, "Shortcut updated", Toast.LENGTH_SHORT).show()
                        loadTextShortcuts()
                    }

                    result == -1 -> {
                        Toast.makeText(this, "Shortcut is too short", Toast.LENGTH_SHORT).show()
                    }

                    result == -3 -> {
                        Toast.makeText(this, "This shortcut is already used", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        Toast.makeText(this, "Failed to update shortcut", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteShortcutDialog(shortcut: NoteShortcut) {
        AlertDialog.Builder(this)
            .setTitle("Delete Shortcut")
            .setMessage("Remove shortcut \"${shortcut.shortcutKeyword}\" from \"${shortcut.noteTitle}\"?")
            .setPositiveButton("Delete") { _, _ ->
                val result = databaseHelper.deleteShortcutById(shortcut.id)

                if (result > 0) {
                    Toast.makeText(this, "Shortcut removed", Toast.LENGTH_SHORT).show()
                    loadTextShortcuts()
                } else {
                    Toast.makeText(this, "Failed to remove shortcut", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
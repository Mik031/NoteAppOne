package com.example.noteappux

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import android.widget.ImageButton

class NotesActivity : AppCompatActivity() {

    private lateinit var recyclerViewCategoryFilters: RecyclerView
    private lateinit var recyclerViewNotes: RecyclerView
    private lateinit var tvEmptyNotes: TextView
    private lateinit var btnCreateNote: TextView
    private lateinit var btnSortNotes: ImageButton
    private lateinit var btnPreviewSettings: ImageButton
    private lateinit var btnSettings: ImageButton

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var notesList: ArrayList<Note>

    private lateinit var categoryFilterAdapter: CategoryFilterAdapter
    private lateinit var categoryFilterList: ArrayList<Category>

    private var selectedCategoryId: Int = -1

    private var currentSortMode: Int = SORT_NEWEST_FIRST

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnOpenDrawer: TextView
    private lateinit var btnDrawerNotes: TextView
    private lateinit var btnDrawerSearch: TextView
    private lateinit var btnDrawerCategories: TextView
    private lateinit var btnDrawerFolders: TextView
    private lateinit var btnDrawerRecentlyDeleted: TextView

    companion object {
        private const val PREF_NAME = "notes_sort_pref"
        private const val PREF_SORT_MODE = "sort_mode"

        private const val SORT_NEWEST_FIRST = 0
        private const val SORT_OLDEST_FIRST = 1
        private const val SORT_TITLE_A_TO_Z = 2
        private const val SORT_TITLE_Z_TO_A = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        drawerLayout = findViewById(R.id.drawerLayout)
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer)
        btnDrawerNotes = findViewById(R.id.btnDrawerNotes)
        btnDrawerSearch = findViewById(R.id.btnDrawerSearch)
        btnDrawerCategories = findViewById(R.id.btnDrawerCategories)
        btnDrawerFolders = findViewById(R.id.btnDrawerFolders)
        btnDrawerRecentlyDeleted = findViewById(R.id.btnDrawerRecentlyDeleted)

        recyclerViewCategoryFilters = findViewById(R.id.recyclerViewCategoryFilters)
        recyclerViewNotes = findViewById(R.id.recyclerViewNotes)
        tvEmptyNotes = findViewById(R.id.tvEmptyNotes)

        btnCreateNote = findViewById(R.id.btnCreateNote)
        btnSortNotes = findViewById(R.id.btnSortNotes)
        btnPreviewSettings = findViewById(R.id.btnPreviewSettings)
        btnSettings = findViewById(R.id.btnSettings)

        databaseHelper = DatabaseHelper(this)

        currentSortMode = getSavedSortMode()
        updateSortButtonText()

        recyclerViewNotes.layoutManager = LinearLayoutManager(this)

        recyclerViewCategoryFilters.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        btnCreateNote.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            startActivity(intent)
        }

        btnSortNotes.setOnClickListener {
            showSortDialog()
        }

        btnPreviewSettings.setOnClickListener {
            val intent = Intent(this, AllAttachmentsActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        setupNavigationDrawer()

        loadCategoryFilters()
        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        loadCategoryFilters()
        loadNotes()
    }

    private fun setupNavigationDrawer() {
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnDrawerNotes.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnDrawerSearch.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, SearchActivity::class.java))
        }

        btnDrawerCategories.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        btnDrawerFolders.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, FoldersActivity::class.java))
        }

        btnDrawerRecentlyDeleted.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, RecentlyDeletedActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun getSavedSortMode(): Int {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(PREF_SORT_MODE, SORT_NEWEST_FIRST)
    }

    private fun saveSortMode(sortMode: Int) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .putInt(PREF_SORT_MODE, sortMode)
            .apply()
    }

    private fun updateSortButtonText() {
        btnSortNotes.contentDescription = when (currentSortMode) {
            SORT_NEWEST_FIRST -> "Sort notes: Newest first"
            SORT_OLDEST_FIRST -> "Sort notes: Oldest first"
            SORT_TITLE_A_TO_Z -> "Sort notes: Title A to Z"
            SORT_TITLE_Z_TO_A -> "Sort notes: Title Z to A"
            else -> "Sort notes"
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Newest First",
            "Oldest First",
            "Title A to Z",
            "Title Z to A"
        )

        AlertDialog.Builder(this)
            .setTitle("Sort Notes")
            .setSingleChoiceItems(sortOptions, currentSortMode) { dialog, which ->
                currentSortMode = which
                saveSortMode(currentSortMode)
                updateSortButtonText()
                loadNotes()

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

    private fun loadCategoryFilters() {
        categoryFilterList = ArrayList()

        categoryFilterList.add(
            Category(
                id = -1,
                name = "All"
            )
        )

        val realCategories = databaseHelper.getAllCategories()
        categoryFilterList.addAll(realCategories)

        categoryFilterAdapter = CategoryFilterAdapter(
            categoryFilterList,
            selectedCategoryId
        ) { selectedCategory ->
            selectedCategoryId = selectedCategory.id
            loadNotes()
        }

        recyclerViewCategoryFilters.adapter = categoryFilterAdapter
    }

    private fun loadNotes() {
        notesList = if (selectedCategoryId == -1) {
            databaseHelper.getAllNotes()
        } else {
            databaseHelper.getNotesByCategory(selectedCategoryId)
        }

        notesList = sortNotesWithPinnedFirst(notesList)

        if (notesList.isEmpty()) {
            recyclerViewNotes.visibility = View.GONE
            tvEmptyNotes.visibility = View.VISIBLE

            tvEmptyNotes.text = if (selectedCategoryId == -1) {
                "No notes yet.\nCreate your first note."
            } else {
                "No notes in this category."
            }
        } else {
            recyclerViewNotes.visibility = View.VISIBLE
            tvEmptyNotes.visibility = View.GONE
        }

        noteAdapter = NoteAdapter(
            notesList = notesList,
            databaseHelper = databaseHelper,
            showImagePreview = NotePreviewSettingsHelper.isImagePreviewEnabled(this),
            showFilePreview = NotePreviewSettingsHelper.isFilePreviewEnabled(this),
            showLinkPreview = NotePreviewSettingsHelper.isLinkPreviewEnabled(this),
            onNoteClick = { selectedNote ->
                handleOpenNote(selectedNote)
            },
            onPinClick = { selectedNote ->
                togglePinNote(selectedNote)
            },
            onShareClick = { selectedNote ->
                handleShareNote(selectedNote)
            },
            onDeleteClick = { selectedNote ->
                handleDeleteNote(selectedNote)
            }
        )

        recyclerViewNotes.adapter = noteAdapter
    }

    private fun sortNotesWithPinnedFirst(originalList: ArrayList<Note>): ArrayList<Note> {
        val pinnedNotes = originalList.filter { it.isPinned == 1 }
        val normalNotes = originalList.filter { it.isPinned == 0 }

        val sortedPinnedNotes = sortSingleGroup(pinnedNotes)
        val sortedNormalNotes = sortSingleGroup(normalNotes)

        val finalList = ArrayList<Note>()
        finalList.addAll(sortedPinnedNotes)
        finalList.addAll(sortedNormalNotes)

        return finalList
    }

    private fun sortSingleGroup(noteGroup: List<Note>): List<Note> {
        return when (currentSortMode) {
            SORT_NEWEST_FIRST -> {
                noteGroup.sortedByDescending { it.id }
            }

            SORT_OLDEST_FIRST -> {
                noteGroup.sortedBy { it.id }
            }

            SORT_TITLE_A_TO_Z -> {
                noteGroup.sortedBy { it.title.lowercase() }
            }

            SORT_TITLE_Z_TO_A -> {
                noteGroup.sortedByDescending { it.title.lowercase() }
            }

            else -> {
                noteGroup.sortedByDescending { it.id }
            }
        }
    }

    private fun handleOpenNote(note: Note) {
        if (note.isLocked == 1) {
            unlockLockedNote(
                note = note,
                successAction = {
                    openNote(note)
                }
            )
        } else {
            openNote(note)
        }
    }

    private fun handleShareNote(note: Note) {
        if (note.isLocked == 1) {
            unlockLockedNote(
                note = note,
                successAction = {
                    copyNoteToClipboard(note)
                }
            )
        } else {
            copyNoteToClipboard(note)
        }
    }

    private fun handleDeleteNote(note: Note) {
        if (note.isLocked == 1) {
            unlockLockedNote(
                note = note,
                successAction = {
                    confirmMoveToRecentlyDeleted(note)
                }
            )
        } else {
            confirmMoveToRecentlyDeleted(note)
        }
    }

    private fun unlockLockedNote(note: Note, successAction: () -> Unit) {
        when (note.lockType) {
            DatabaseHelper.LOCK_PASSCODE_ONLY -> {
                showPasscodeDialog(
                    note = note,
                    successAction = successAction
                )
            }

            DatabaseHelper.LOCK_FINGERPRINT_ONLY -> {
                showFingerprintOnlyUnlock(
                    note = note,
                    successAction = successAction
                )
            }

            DatabaseHelper.LOCK_BOTH -> {
                showTwoFactorUnlock(
                    note = note,
                    successAction = successAction
                )
            }

            else -> {
                showPasscodeDialog(
                    note = note,
                    successAction = successAction
                )
            }
        }
    }

    private fun showFingerprintOnlyUnlock(note: Note, successAction: () -> Unit) {
        if (BiometricHelper.canUseBiometric(this)) {
            BiometricHelper.showBiometricPrompt(
                activity = this,
                onSuccess = {
                    successAction()
                },
                onFailedOrCancel = {
                    Toast.makeText(
                        this,
                        "Fingerprint unlock cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            Toast.makeText(
                this,
                "Fingerprint is not available on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showTwoFactorUnlock(note: Note, successAction: () -> Unit) {
        if (BiometricHelper.canUseBiometric(this)) {
            BiometricHelper.showBiometricPrompt(
                activity = this,
                onSuccess = {
                    showPasscodeDialog(
                        note = note,
                        successAction = successAction
                    )
                },
                onFailedOrCancel = {
                    Toast.makeText(
                        this,
                        "Fingerprint is required first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            Toast.makeText(
                this,
                "Fingerprint is not available. Two-factor unlock cannot continue.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPasscodeDialog(note: Note, successAction: () -> Unit) {
        val input = EditText(this)
        input.hint = "Enter passcode"
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("Unlock Note")
            .setMessage("This note is locked")
            .setView(input)
            .setPositiveButton("Unlock") { _, _ ->
                val passcode = input.text.toString().trim()

                if (passcode.isEmpty()) {
                    Toast.makeText(this, "Passcode is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val isCorrect = SecurityHelper.verifyPasscode(
                    inputPasscode = passcode,
                    savedHash = note.passcodeHash
                )

                if (isCorrect) {
                    successAction()
                } else {
                    Toast.makeText(this, "Wrong passcode", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openNote(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra("note_id", note.id)
        intent.putExtra("note_title", note.title)
        intent.putExtra("note_content", note.content)
        intent.putExtra("note_date", note.date)
        startActivity(intent)
    }

    private fun togglePinNote(note: Note) {
        val result = databaseHelper.togglePinNote(
            id = note.id,
            currentPinnedState = note.isPinned
        )

        if (result > 0) {
            if (note.isPinned == 1) {
                Toast.makeText(this, "Note unpinned", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Note pinned", Toast.LENGTH_SHORT).show()
            }

            loadNotes()
        } else {
            Toast.makeText(this, "Failed to update pin", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyNoteToClipboard(note: Note) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val noteText = """
            ${note.title}
            
            ${note.content}
        """.trimIndent()

        val clip = ClipData.newPlainText(note.title, noteText)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun confirmMoveToRecentlyDeleted(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Move \"${note.title}\" to Recently Deleted?")
            .setPositiveButton("Delete") { _, _ ->
                moveToRecentlyDeleted(note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun moveToRecentlyDeleted(note: Note) {
        val result = databaseHelper.moveToRecentlyDeleted(note.id)

        if (result > 0) {
            Toast.makeText(this, "Moved to Recently Deleted", Toast.LENGTH_SHORT).show()
            loadNotes()
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }
}
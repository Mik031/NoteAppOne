package com.example.noteappux

import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FolderNotesActivity : AppCompatActivity() {

    private lateinit var tvFolderNotesTitle: TextView
    private lateinit var tvFolderNotesSubtitle: TextView
    private lateinit var btnAddNotesToFolder: TextView
    private lateinit var recyclerViewFolderNotes: RecyclerView
    private lateinit var tvEmptyFolderNotes: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var notesList: ArrayList<Note>

    private var folderId: Int = 0
    private var folderName: String = ""
    private var isPinnedFolder: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_notes)

        tvFolderNotesTitle = findViewById(R.id.tvFolderNotesTitle)
        tvFolderNotesSubtitle = findViewById(R.id.tvFolderNotesSubtitle)
        btnAddNotesToFolder = findViewById(R.id.btnAddNotesToFolder)
        recyclerViewFolderNotes = findViewById(R.id.recyclerViewFolderNotes)
        tvEmptyFolderNotes = findViewById(R.id.tvEmptyFolderNotes)

        databaseHelper = DatabaseHelper(this)

        recyclerViewFolderNotes.layoutManager = LinearLayoutManager(this)

        getFolderData()

        btnAddNotesToFolder.setOnClickListener {
            showAddNotesDialog()
        }

        loadFolderNotes()
    }

    override fun onResume() {
        super.onResume()
        loadFolderNotes()
    }

    private fun getFolderData() {
        folderId = intent.getIntExtra("folder_id", 0)
        folderName = intent.getStringExtra("folder_name") ?: "Folder"
        isPinnedFolder = intent.getBooleanExtra("is_pinned_folder", false)

        tvFolderNotesTitle.text = folderName

        tvFolderNotesSubtitle.text = if (isPinnedFolder) {
            "Your pinned and favorite notes"
        } else {
            "Notes in this folder"
        }
    }

    private fun loadFolderNotes() {
        notesList = if (isPinnedFolder) {
            databaseHelper.getPinnedNotes()
        } else {
            databaseHelper.getNotesByFolder(folderId)
        }

        if (notesList.isEmpty()) {
            recyclerViewFolderNotes.visibility = View.GONE
            tvEmptyFolderNotes.visibility = View.VISIBLE

            tvEmptyFolderNotes.text = if (isPinnedFolder) {
                "No pinned notes yet."
            } else {
                "No notes in this folder."
            }
        } else {
            recyclerViewFolderNotes.visibility = View.VISIBLE
            tvEmptyFolderNotes.visibility = View.GONE
        }

//        noteAdapter = NoteAdapter(
//            notesList,
//            databaseHelper,
//            onNoteClick = { selectedNote ->
//                handleOpenNote(selectedNote)
//            },
//            onPinClick = { selectedNote ->
//                togglePinNote(selectedNote)
//            },
//            onShareClick = { selectedNote ->
//                handleShareNote(selectedNote)
//            },
//            onDeleteClick = { selectedNote ->
//                handleDeleteNote(selectedNote)
//            }
//        )

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

        recyclerViewFolderNotes.adapter = noteAdapter
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

            loadFolderNotes()
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
            loadFolderNotes()
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddNotesDialog() {
        val allNotes = databaseHelper.getAllNotes()

        if (allNotes.isEmpty()) {
            Toast.makeText(this, "No notes available", Toast.LENGTH_SHORT).show()
            return
        }

        val selectableNotes = ArrayList<Note>()

        for (note in allNotes) {
            if (isPinnedFolder) {
                if (note.isPinned == 0) {
                    selectableNotes.add(note)
                }
            } else {
                if (note.folderId != folderId) {
                    selectableNotes.add(note)
                }
            }
        }

        if (selectableNotes.isEmpty()) {
            val message = if (isPinnedFolder) {
                "All notes are already pinned."
            } else {
                "All notes are already in this folder."
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }

        val noteTitles = selectableNotes.map {
            if (it.isLocked == 1 && it.showTitleWhenLocked == 0) {
                "Locked Note"
            } else {
                it.title
            }
        }.toTypedArray()

        val checkedItems = BooleanArray(selectableNotes.size)
        val selectedNoteIds = ArrayList<Int>()

        AlertDialog.Builder(this)
            .setTitle("Add Notes")
            .setMultiChoiceItems(noteTitles, checkedItems) { _, which, isChecked ->
                val noteId = selectableNotes[which].id

                if (isChecked) {
                    if (!selectedNoteIds.contains(noteId)) {
                        selectedNoteIds.add(noteId)
                    }
                } else {
                    selectedNoteIds.remove(noteId)
                }
            }
            .setPositiveButton("Add") { _, _ ->
                if (selectedNoteIds.isEmpty()) {
                    Toast.makeText(this, "No notes selected", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addSelectedNotes(selectedNoteIds)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addSelectedNotes(selectedNoteIds: ArrayList<Int>) {
        var successCount = 0

        for (noteId in selectedNoteIds) {
            val result = if (isPinnedFolder) {
                databaseHelper.setNotePinned(noteId, 1)
            } else {
                databaseHelper.updateNoteFolder(noteId, folderId)
            }

            if (result > 0) {
                successCount++
            }
        }

        if (successCount > 0) {
            val message = if (isPinnedFolder) {
                "$successCount note(s) added to Pinned / Favorites"
            } else {
                "$successCount note(s) added to $folderName"
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            loadFolderNotes()
        } else {
            Toast.makeText(this, "Failed to add notes", Toast.LENGTH_SHORT).show()
        }
    }
}
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

class CategoryNotesActivity : AppCompatActivity() {

    private lateinit var tvCategoryNotesTitle: TextView
    private lateinit var recyclerViewCategoryNotes: RecyclerView
    private lateinit var tvEmptyCategoryNotes: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var notesList: ArrayList<Note>

    private var categoryId: Int = -1
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_notes)

        tvCategoryNotesTitle = findViewById(R.id.tvCategoryNotesTitle)
        recyclerViewCategoryNotes = findViewById(R.id.recyclerViewCategoryNotes)
        tvEmptyCategoryNotes = findViewById(R.id.tvEmptyCategoryNotes)

        databaseHelper = DatabaseHelper(this)

        recyclerViewCategoryNotes.layoutManager = LinearLayoutManager(this)

        getCategoryData()
        loadCategoryNotes()
    }

    override fun onResume() {
        super.onResume()
        loadCategoryNotes()
    }

    private fun getCategoryData() {
        categoryId = intent.getIntExtra("category_id", -1)
        categoryName = intent.getStringExtra("category_name") ?: "Category"

        tvCategoryNotesTitle.text = categoryName
    }

    private fun loadCategoryNotes() {
        if (categoryId == -1) {
            recyclerViewCategoryNotes.visibility = View.GONE
            tvEmptyCategoryNotes.visibility = View.VISIBLE
            return
        }

        notesList = databaseHelper.getNotesByCategory(categoryId)

        if (notesList.isEmpty()) {
            recyclerViewCategoryNotes.visibility = View.GONE
            tvEmptyCategoryNotes.visibility = View.VISIBLE
        } else {
            recyclerViewCategoryNotes.visibility = View.VISIBLE
            tvEmptyCategoryNotes.visibility = View.GONE
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

        recyclerViewCategoryNotes.adapter = noteAdapter
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

            loadCategoryNotes()
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
            loadCategoryNotes()
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }
}
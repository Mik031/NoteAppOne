package com.example.noteappux

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearchInput: EditText
    private lateinit var recyclerViewSearchResults: RecyclerView
    private lateinit var tvEmptySearch: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var searchResults: ArrayList<SearchResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        etSearchInput = findViewById(R.id.etSearchInput)
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults)
        tvEmptySearch = findViewById(R.id.tvEmptySearch)

        databaseHelper = DatabaseHelper(this)

        recyclerViewSearchResults.layoutManager = LinearLayoutManager(this)

        searchResults = ArrayList()

        searchResultAdapter = SearchResultAdapter(searchResults) { selectedResult ->
            openSearchResult(selectedResult)
        }

        recyclerViewSearchResults.adapter = searchResultAdapter

        setupSearchInput()
    }

    private fun setupSearchInput() {
        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No action needed
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val searchText = s.toString().trim()
                performSearch(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        })
    }

    private fun performSearch(searchText: String) {
        searchResults.clear()

        if (searchText.isEmpty()) {
            recyclerViewSearchResults.visibility = View.GONE
            tvEmptySearch.visibility = View.VISIBLE
            tvEmptySearch.text = "Start typing to search."
            searchResultAdapter.notifyDataSetChanged()
            return
        }

        val resultsFromDatabase = databaseHelper.searchEverything(searchText)
        searchResults.addAll(resultsFromDatabase)

        if (searchResults.isEmpty()) {
            recyclerViewSearchResults.visibility = View.GONE
            tvEmptySearch.visibility = View.VISIBLE
            tvEmptySearch.text = "No results found."
        } else {
            recyclerViewSearchResults.visibility = View.VISIBLE
            tvEmptySearch.visibility = View.GONE
        }

        searchResultAdapter.notifyDataSetChanged()
    }

    private fun openSearchResult(result: SearchResult) {
        when (result.type) {
            "note" -> {
                handleOpenNoteFromSearch(result.id)
            }

            "category" -> {
                val intent = Intent(this, CategoryNotesActivity::class.java)
                intent.putExtra("category_id", result.id)
                intent.putExtra("category_name", result.title)
                startActivity(intent)
            }

            "folder" -> {
                val intent = Intent(this, FolderNotesActivity::class.java)
                intent.putExtra("folder_id", result.id)
                intent.putExtra("folder_name", result.title)
                intent.putExtra("is_pinned_folder", false)
                startActivity(intent)
            }

            "pinned_folder" -> {
                val intent = Intent(this, FolderNotesActivity::class.java)
                intent.putExtra("folder_id", -1)
                intent.putExtra("folder_name", "Pinned / Favorites")
                intent.putExtra("is_pinned_folder", true)
                startActivity(intent)
            }
        }
    }

    private fun handleOpenNoteFromSearch(noteId: Int) {
        val selectedNote = databaseHelper.getNoteById(noteId)

        if (selectedNote == null) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedNote.isLocked == 1) {
            unlockLockedNote(
                note = selectedNote,
                successAction = {
                    openNote(selectedNote)
                }
            )
        } else {
            openNote(selectedNote)
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

    private fun showFingerprintOnlyUnlock(successAction: () -> Unit) {
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
}
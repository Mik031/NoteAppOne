package com.example.noteappux

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecentlyDeletedActivity : AppCompatActivity() {

    private lateinit var recyclerViewDeletedNotes: RecyclerView
    private lateinit var tvEmptyDeletedNotes: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var deletedNoteAdapter: DeletedNoteAdapter
    private lateinit var deletedNotesList: ArrayList<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recently_deleted)

        recyclerViewDeletedNotes = findViewById(R.id.recyclerViewDeletedNotes)
        tvEmptyDeletedNotes = findViewById(R.id.tvEmptyDeletedNotes)

        databaseHelper = DatabaseHelper(this)

        recyclerViewDeletedNotes.layoutManager = LinearLayoutManager(this)

        loadDeletedNotes()
    }

    override fun onResume() {
        super.onResume()
        loadDeletedNotes()
    }

    private fun loadDeletedNotes() {
        databaseHelper.deleteOldRecentlyDeletedNotes()

        deletedNotesList = databaseHelper.getDeletedNotes()

        if (deletedNotesList.isEmpty()) {
            recyclerViewDeletedNotes.visibility = View.GONE
            tvEmptyDeletedNotes.visibility = View.VISIBLE
        } else {
            recyclerViewDeletedNotes.visibility = View.VISIBLE
            tvEmptyDeletedNotes.visibility = View.GONE
        }

        deletedNoteAdapter = DeletedNoteAdapter(
            deletedNotesList,
            onDeletedNoteClick = { selectedNote ->
                val intent = Intent(this, DeletedNoteViewActivity::class.java)
                intent.putExtra("note_id", selectedNote.id)
                intent.putExtra("note_title", selectedNote.title)
                intent.putExtra("note_content", selectedNote.content)
                intent.putExtra("note_date", selectedNote.date)
                startActivity(intent)
            },
            onRecoverClick = { selectedNote ->
                recoverNote(selectedNote)
            },
            onDeleteForeverClick = { selectedNote ->
                confirmDeleteForever(selectedNote)
            }
        )

        recyclerViewDeletedNotes.adapter = deletedNoteAdapter
    }

    private fun recoverNote(note: Note) {
        val result = databaseHelper.restoreNote(note.id)

        if (result > 0) {
            Toast.makeText(this, "Note recovered", Toast.LENGTH_SHORT).show()
            loadDeletedNotes()
        } else {
            Toast.makeText(this, "Failed to recover note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteForever(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Forever")
            .setMessage("Are you sure you want to permanently delete \"${note.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteForever(note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteForever(note: Note) {
        val result = databaseHelper.deleteNoteForever(note.id)

        if (result > 0) {
            Toast.makeText(this, "Note permanently deleted", Toast.LENGTH_SHORT).show()
            loadDeletedNotes()
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }
}
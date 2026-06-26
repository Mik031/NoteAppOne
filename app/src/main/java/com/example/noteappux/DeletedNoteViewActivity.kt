package com.example.noteappux

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DeletedNoteViewActivity : AppCompatActivity() {

    private lateinit var etDeletedNoteTitle: EditText
    private lateinit var etDeletedNoteContent: EditText

    private lateinit var recyclerViewDeletedNoteImages: RecyclerView
    private lateinit var tvEmptyDeletedImages: TextView

    private lateinit var recyclerViewDeletedNoteFiles: RecyclerView
    private lateinit var tvEmptyDeletedFiles: TextView

    private lateinit var btnRecoverNote: TextView
    private lateinit var btnDeleteForever: TextView

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var noteImageAdapter: NoteImageAdapter
    private lateinit var deletedNoteImageList: ArrayList<NoteImage>

    private lateinit var noteFileAdapter: NoteFileAdapter
    private lateinit var deletedNoteFileList: ArrayList<NoteFile>

    private var noteId: Int = -1
    private var noteTitle: String = ""
    private var noteContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deleted_note_view)

        etDeletedNoteTitle = findViewById(R.id.etDeletedNoteTitle)
        etDeletedNoteContent = findViewById(R.id.etDeletedNoteContent)

        recyclerViewDeletedNoteImages = findViewById(R.id.recyclerViewDeletedNoteImages)
        tvEmptyDeletedImages = findViewById(R.id.tvEmptyDeletedImages)

        recyclerViewDeletedNoteFiles = findViewById(R.id.recyclerViewDeletedNoteFiles)
        tvEmptyDeletedFiles = findViewById(R.id.tvEmptyDeletedFiles)

        btnRecoverNote = findViewById(R.id.btnRecoverNote)
        btnDeleteForever = findViewById(R.id.btnDeleteForever)

        databaseHelper = DatabaseHelper(this)

        recyclerViewDeletedNoteImages.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewDeletedNoteFiles.layoutManager = LinearLayoutManager(this)

        getNoteData()
        setupDeletedNoteView()
        loadDeletedNoteImages()
        loadDeletedNoteFiles()

        btnRecoverNote.setOnClickListener {
            recoverNote()
        }

        btnDeleteForever.setOnClickListener {
            confirmDeleteForever()
        }
    }

    private fun getNoteData() {
        noteId = intent.getIntExtra("note_id", -1)
        noteTitle = intent.getStringExtra("note_title") ?: ""
        noteContent = intent.getStringExtra("note_content") ?: ""
    }

    private fun setupDeletedNoteView() {
        etDeletedNoteTitle.setText(noteTitle)
        etDeletedNoteContent.setText(noteContent)

        lockEditText(etDeletedNoteTitle)
        lockDeletedContentWithLinks(etDeletedNoteContent)
    }

    private fun lockEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isCursorVisible = false

        editText.setOnClickListener {
            showRecoverBeforeEditingDialog()
        }
    }

    private fun lockDeletedContentWithLinks(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isCursorVisible = false

        LinkUtils.setupReadOnlyWebLinks(this, editText)
    }

    private fun loadDeletedNoteImages() {
        if (noteId == -1) {
            recyclerViewDeletedNoteImages.visibility = View.GONE
            tvEmptyDeletedImages.visibility = View.VISIBLE
            return
        }

        deletedNoteImageList = databaseHelper.getImagesForNote(noteId)

        if (deletedNoteImageList.isEmpty()) {
            recyclerViewDeletedNoteImages.visibility = View.GONE
            tvEmptyDeletedImages.visibility = View.VISIBLE
        } else {
            recyclerViewDeletedNoteImages.visibility = View.VISIBLE
            tvEmptyDeletedImages.visibility = View.GONE
        }

        noteImageAdapter = NoteImageAdapter(
            deletedNoteImageList,
            showDeleteButton = false,
            onImageClick = { _, selectedPosition ->
                openImageViewer(selectedPosition)
            },
            onDeleteImageClick = {
                // Disabled in Recently Deleted
            }
        )

        recyclerViewDeletedNoteImages.adapter = noteImageAdapter
    }

    private fun loadDeletedNoteFiles() {
        if (noteId == -1) {
            recyclerViewDeletedNoteFiles.visibility = View.GONE
            tvEmptyDeletedFiles.visibility = View.VISIBLE
            return
        }

        deletedNoteFileList = databaseHelper.getFilesForNote(noteId)

        if (deletedNoteFileList.isEmpty()) {
            recyclerViewDeletedNoteFiles.visibility = View.GONE
            tvEmptyDeletedFiles.visibility = View.VISIBLE
        } else {
            recyclerViewDeletedNoteFiles.visibility = View.VISIBLE
            tvEmptyDeletedFiles.visibility = View.GONE
        }

        noteFileAdapter = NoteFileAdapter(
            deletedNoteFileList,
            showDeleteButton = false,
            onFileClick = { selectedFile ->
                openDeletedNoteFile(selectedFile)
            },
            onDeleteFileClick = {
                // Disabled in Recently Deleted
            }
        )

        recyclerViewDeletedNoteFiles.adapter = noteFileAdapter
    }

    private fun openDeletedNoteFile(noteFile: NoteFile) {
        try {
            val savedUri = Uri.parse(noteFile.fileUri)
            val file = File(savedUri.path ?: "")

            if (!file.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val mimeType = getMimeTypeFromFileName(noteFile.fileName)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Open file with"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "No app found to open this file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getMimeTypeFromFileName(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()

        if (extension.isEmpty()) {
            return "*/*"
        }

        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension) ?: "*/*"
    }

    private fun openImageViewer(selectedPosition: Int) {
        if (noteId == -1) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, NoteImageViewerActivity::class.java)
        intent.putExtra("note_id", noteId)
        intent.putExtra("selected_position", selectedPosition)
        startActivity(intent)
    }

    private fun showRecoverBeforeEditingDialog() {
        AlertDialog.Builder(this)
            .setTitle("Recover Note")
            .setMessage("This note is in Recently Deleted. Recover it before editing.")
            .setPositiveButton("Recover") { _, _ ->
                recoverNote()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun recoverNote() {
        if (noteId == -1) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            return
        }

        val result = databaseHelper.restoreNote(noteId)

        if (result > 0) {
            Toast.makeText(this, "Note recovered", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to recover note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteForever() {
        AlertDialog.Builder(this)
            .setTitle("Delete Forever")
            .setMessage("Are you sure you want to permanently delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                deleteForever()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteForever() {
        if (noteId == -1) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            return
        }

        val result = databaseHelper.deleteNoteForever(noteId)

        if (result > 0) {
            Toast.makeText(this, "Note permanently deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }
}
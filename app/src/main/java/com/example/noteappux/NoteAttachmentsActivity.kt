package com.example.noteappux

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class NoteAttachmentsActivity : AppCompatActivity() {

    private lateinit var tvAttachmentNoteTitle: TextView
    private lateinit var tvAllAttachmentsCount: TextView

    private lateinit var recyclerViewAttachmentImages: RecyclerView
    private lateinit var tvEmptyAttachmentImages: TextView

    private lateinit var recyclerViewAttachmentFiles: RecyclerView
    private lateinit var tvEmptyAttachmentFiles: TextView

    private lateinit var recyclerViewAttachmentLinks: RecyclerView
    private lateinit var tvEmptyAttachmentLinks: TextView

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var noteImageAdapter: NoteImageAdapter
    private lateinit var noteFileAdapter: NoteFileAdapter
    private lateinit var noteLinkAdapter: NoteLinkAdapter

    private lateinit var imageList: ArrayList<NoteImage>
    private lateinit var fileList: ArrayList<NoteFile>
    private lateinit var linkList: ArrayList<String>

    private var noteId: Int = -1
    private var noteTitle: String = ""
    private var noteContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_attachments)

        tvAttachmentNoteTitle = findViewById(R.id.tvAttachmentNoteTitle)
        tvAllAttachmentsCount = findViewById(R.id.tvAllAttachmentsCount)

        recyclerViewAttachmentImages = findViewById(R.id.recyclerViewAttachmentImages)
        tvEmptyAttachmentImages = findViewById(R.id.tvEmptyAttachmentImages)

        recyclerViewAttachmentFiles = findViewById(R.id.recyclerViewAttachmentFiles)
        tvEmptyAttachmentFiles = findViewById(R.id.tvEmptyAttachmentFiles)

        recyclerViewAttachmentLinks = findViewById(R.id.recyclerViewAttachmentLinks)
        tvEmptyAttachmentLinks = findViewById(R.id.tvEmptyAttachmentLinks)

        databaseHelper = DatabaseHelper(this)

        noteId = intent.getIntExtra("note_id", -1)
        noteTitle = intent.getStringExtra("note_title") ?: "Untitled Note"

        recyclerViewAttachmentImages.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewAttachmentFiles.layoutManager = LinearLayoutManager(this)
        recyclerViewAttachmentLinks.layoutManager = LinearLayoutManager(this)

        if (noteId == -1) {
            Toast.makeText(
                this,
                "Save the note first to view attachments",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        val note = databaseHelper.getNoteById(noteId)

        if (note != null) {
            noteTitle = if (note.title.isNotBlank()) {
                note.title
            } else {
                noteTitle
            }

            noteContent = note.content
        }

        tvAttachmentNoteTitle.text = noteTitle

        loadAttachments()
    }

    override fun onResume() {
        super.onResume()

        if (noteId != -1) {
            val note = databaseHelper.getNoteById(noteId)

            if (note != null) {
                noteTitle = if (note.title.isNotBlank()) {
                    note.title
                } else {
                    "Untitled Note"
                }

                noteContent = note.content
                tvAttachmentNoteTitle.text = noteTitle
            }

            loadAttachments()
        }
    }

    private fun loadAttachments() {
        imageList = databaseHelper.getImagesForNote(noteId)
        fileList = databaseHelper.getFilesForNote(noteId)
        linkList = LinkUtils.extractWebLinks(noteContent)

        val totalCount = imageList.size + fileList.size + linkList.size

        tvAllAttachmentsCount.text =
            "$totalCount attachment(s) • ${imageList.size} image(s) • ${fileList.size} file(s) • ${linkList.size} link(s)"

        loadImages()
        loadFiles()
        loadLinks()
    }

    private fun loadImages() {
        if (imageList.isEmpty()) {
            recyclerViewAttachmentImages.visibility = View.GONE
            tvEmptyAttachmentImages.visibility = View.VISIBLE
        } else {
            recyclerViewAttachmentImages.visibility = View.VISIBLE
            tvEmptyAttachmentImages.visibility = View.GONE
        }

        noteImageAdapter = NoteImageAdapter(
            imageList,
            showDeleteButton = false,
            onImageClick = { _, selectedPosition ->
                openImageViewer(selectedPosition)
            },
            onDeleteImageClick = {
                // Read-only in attachment page
            }
        )

        recyclerViewAttachmentImages.adapter = noteImageAdapter
    }

    private fun loadFiles() {
        if (fileList.isEmpty()) {
            recyclerViewAttachmentFiles.visibility = View.GONE
            tvEmptyAttachmentFiles.visibility = View.VISIBLE
        } else {
            recyclerViewAttachmentFiles.visibility = View.VISIBLE
            tvEmptyAttachmentFiles.visibility = View.GONE
        }

        noteFileAdapter = NoteFileAdapter(
            fileList,
            showDeleteButton = false,
            onFileClick = { selectedFile ->
                openNoteFile(selectedFile)
            },
            onDeleteFileClick = {
                // Read-only in attachment page
            }
        )

        recyclerViewAttachmentFiles.adapter = noteFileAdapter
    }

    private fun loadLinks() {
        if (linkList.isEmpty()) {
            recyclerViewAttachmentLinks.visibility = View.GONE
            tvEmptyAttachmentLinks.visibility = View.VISIBLE
        } else {
            recyclerViewAttachmentLinks.visibility = View.VISIBLE
            tvEmptyAttachmentLinks.visibility = View.GONE
        }

        noteLinkAdapter = NoteLinkAdapter(
            linkList,
            onLinkClick = { selectedLink ->
                LinkUtils.openWebUrl(this, selectedLink)
            }
        )

        recyclerViewAttachmentLinks.adapter = noteLinkAdapter
    }

    private fun openImageViewer(selectedPosition: Int) {
        val intent = Intent(this, NoteImageViewerActivity::class.java)
        intent.putExtra("note_id", noteId)
        intent.putExtra("selected_position", selectedPosition)
        startActivity(intent)
    }

    private fun openNoteFile(noteFile: NoteFile) {
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
}
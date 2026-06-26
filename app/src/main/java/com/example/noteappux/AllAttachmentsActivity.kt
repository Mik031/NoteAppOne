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

class AllAttachmentsActivity : AppCompatActivity() {

    private lateinit var tvAllAttachmentsCount: TextView

    private lateinit var recyclerViewImageFilters: RecyclerView
    private lateinit var recyclerViewAllAttachmentImages: RecyclerView
    private lateinit var tvEmptyAllAttachmentImages: TextView

    private lateinit var recyclerViewFileFilters: RecyclerView
    private lateinit var recyclerViewAllAttachmentFiles: RecyclerView
    private lateinit var tvEmptyAllAttachmentFiles: TextView

    private lateinit var recyclerViewLinkFilters: RecyclerView
    private lateinit var recyclerViewAllAttachmentLinks: RecyclerView
    private lateinit var tvEmptyAllAttachmentLinks: TextView

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var imageFilterAdapter: AttachmentFilterAdapter
    private lateinit var fileFilterAdapter: AttachmentFilterAdapter
    private lateinit var linkFilterAdapter: AttachmentFilterAdapter

    private lateinit var noteImageAdapter: NoteImageAdapter
    private lateinit var noteFileAdapter: NoteFileAdapter
    private lateinit var noteLinkAdapter: NoteLinkAdapter

    private lateinit var allImageList: ArrayList<NoteImage>
    private lateinit var allFileList: ArrayList<NoteFile>
    private lateinit var allLinkItemList: ArrayList<AllNoteLinkItem>

    private lateinit var currentImageList: ArrayList<NoteImage>
    private lateinit var currentFileList: ArrayList<NoteFile>
    private lateinit var currentLinkItemList: ArrayList<AllNoteLinkItem>

    private var selectedImageNoteId: Int = FILTER_ALL
    private var selectedFileNoteId: Int = FILTER_ALL
    private var selectedLinkNoteId: Int = FILTER_ALL

    companion object {
        private const val FILTER_ALL = -1
    }

    private data class AllNoteLinkItem(
        val noteId: Int,
        val noteTitle: String,
        val linkUrl: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_attachments)

        tvAllAttachmentsCount = findViewById(R.id.tvAllAttachmentsCount)

        recyclerViewImageFilters = findViewById(R.id.recyclerViewImageFilters)
        recyclerViewAllAttachmentImages = findViewById(R.id.recyclerViewAllAttachmentImages)
        tvEmptyAllAttachmentImages = findViewById(R.id.tvEmptyAllAttachmentImages)

        recyclerViewFileFilters = findViewById(R.id.recyclerViewFileFilters)
        recyclerViewAllAttachmentFiles = findViewById(R.id.recyclerViewAllAttachmentFiles)
        tvEmptyAllAttachmentFiles = findViewById(R.id.tvEmptyAllAttachmentFiles)

        recyclerViewLinkFilters = findViewById(R.id.recyclerViewLinkFilters)
        recyclerViewAllAttachmentLinks = findViewById(R.id.recyclerViewAllAttachmentLinks)
        tvEmptyAllAttachmentLinks = findViewById(R.id.tvEmptyAllAttachmentLinks)

        databaseHelper = DatabaseHelper(this)

        recyclerViewImageFilters.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewFileFilters.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewLinkFilters.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewAllAttachmentImages.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewAllAttachmentFiles.layoutManager = LinearLayoutManager(this)
        recyclerViewAllAttachmentLinks.layoutManager = LinearLayoutManager(this)

        loadAllAttachments()
    }

    override fun onResume() {
        super.onResume()
        loadAllAttachments()
    }

    private fun loadAllAttachments() {
        allImageList = databaseHelper.getAllActiveNoteImages()
        allFileList = databaseHelper.getAllActiveNoteFiles()
        allLinkItemList = getAllActiveNoteLinkItems()

        val totalCount = allImageList.size + allFileList.size + allLinkItemList.size

        tvAllAttachmentsCount.text =
            "$totalCount attachment(s) • ${allImageList.size} image(s) • ${allFileList.size} file(s) • ${allLinkItemList.size} link(s)"

        selectedImageNoteId = FILTER_ALL
        selectedFileNoteId = FILTER_ALL
        selectedLinkNoteId = FILTER_ALL

        currentImageList = ArrayList(allImageList)
        currentFileList = ArrayList(allFileList)
        currentLinkItemList = ArrayList(allLinkItemList)

        setupImageFilters()
        setupFileFilters()
        setupLinkFilters()

        loadImages()
        loadFiles()
        loadLinks()
    }

    // ---------------------------
    // IMAGE FILTERS
    // ---------------------------

    private fun setupImageFilters() {
        val imageFilters = buildImageFilters()

        imageFilterAdapter = AttachmentFilterAdapter(
            imageFilters,
            selectedImageNoteId
        ) { selectedFilter ->
            selectedImageNoteId = selectedFilter.noteId

            currentImageList = if (selectedFilter.noteId == FILTER_ALL) {
                ArrayList(allImageList)
            } else {
                ArrayList(allImageList.filter { it.noteId == selectedFilter.noteId })
            }

            loadImages()
        }

        recyclerViewImageFilters.adapter = imageFilterAdapter
    }

    private fun buildImageFilters(): ArrayList<AttachmentFilterItem> {
        val filterList = ArrayList<AttachmentFilterItem>()

        filterList.add(
            AttachmentFilterItem(
                noteId = FILTER_ALL,
                noteTitle = "All",
                count = allImageList.size
            )
        )

        val filterMap = linkedMapOf<Int, AttachmentFilterItem>()

        for (image in allImageList) {
            val currentFilter = filterMap[image.noteId]

            if (currentFilter == null) {
                filterMap[image.noteId] = AttachmentFilterItem(
                    noteId = image.noteId,
                    noteTitle = getSafeNoteTitle(image.noteId),
                    count = 1
                )
            } else {
                filterMap[image.noteId] = currentFilter.copy(
                    count = currentFilter.count + 1
                )
            }
        }

        filterList.addAll(filterMap.values)

        return filterList
    }

    private fun loadImages() {
        if (currentImageList.isEmpty()) {
            recyclerViewAllAttachmentImages.visibility = View.GONE
            tvEmptyAllAttachmentImages.visibility = View.VISIBLE
        } else {
            recyclerViewAllAttachmentImages.visibility = View.VISIBLE
            tvEmptyAllAttachmentImages.visibility = View.GONE
        }

        noteImageAdapter = NoteImageAdapter(
            currentImageList,
            showDeleteButton = false,
            onImageClick = { _, selectedPosition ->
                openImageViewer(selectedPosition)
            },
            onDeleteImageClick = {
                // Read-only in global attachment page
            }
        )

        recyclerViewAllAttachmentImages.adapter = noteImageAdapter
    }

    private fun openImageViewer(selectedPosition: Int) {
        val intent = Intent(this, NoteImageViewerActivity::class.java)

        if (selectedImageNoteId == FILTER_ALL) {
            intent.putExtra("image_mode", "all_attachments")
            intent.putExtra("selected_position", selectedPosition)
        } else {
            intent.putExtra("note_id", selectedImageNoteId)
            intent.putExtra("selected_position", selectedPosition)
        }

        startActivity(intent)
    }

    // ---------------------------
    // FILE FILTERS
    // ---------------------------

    private fun setupFileFilters() {
        val fileFilters = buildFileFilters()

        fileFilterAdapter = AttachmentFilterAdapter(
            fileFilters,
            selectedFileNoteId
        ) { selectedFilter ->
            selectedFileNoteId = selectedFilter.noteId

            currentFileList = if (selectedFilter.noteId == FILTER_ALL) {
                ArrayList(allFileList)
            } else {
                ArrayList(allFileList.filter { it.noteId == selectedFilter.noteId })
            }

            loadFiles()
        }

        recyclerViewFileFilters.adapter = fileFilterAdapter
    }

    private fun buildFileFilters(): ArrayList<AttachmentFilterItem> {
        val filterList = ArrayList<AttachmentFilterItem>()

        filterList.add(
            AttachmentFilterItem(
                noteId = FILTER_ALL,
                noteTitle = "All",
                count = allFileList.size
            )
        )

        val filterMap = linkedMapOf<Int, AttachmentFilterItem>()

        for (file in allFileList) {
            val currentFilter = filterMap[file.noteId]

            if (currentFilter == null) {
                filterMap[file.noteId] = AttachmentFilterItem(
                    noteId = file.noteId,
                    noteTitle = getSafeNoteTitle(file.noteId),
                    count = 1
                )
            } else {
                filterMap[file.noteId] = currentFilter.copy(
                    count = currentFilter.count + 1
                )
            }
        }

        filterList.addAll(filterMap.values)

        return filterList
    }

    private fun loadFiles() {
        if (currentFileList.isEmpty()) {
            recyclerViewAllAttachmentFiles.visibility = View.GONE
            tvEmptyAllAttachmentFiles.visibility = View.VISIBLE
        } else {
            recyclerViewAllAttachmentFiles.visibility = View.VISIBLE
            tvEmptyAllAttachmentFiles.visibility = View.GONE
        }

        noteFileAdapter = NoteFileAdapter(
            currentFileList,
            showDeleteButton = false,
            onFileClick = { selectedFile ->
                openNoteFile(selectedFile)
            },
            onDeleteFileClick = {
                // Read-only in global attachment page
            }
        )

        recyclerViewAllAttachmentFiles.adapter = noteFileAdapter
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

    // ---------------------------
    // LINK FILTERS
    // ---------------------------

    private fun setupLinkFilters() {
        val linkFilters = buildLinkFilters()

        linkFilterAdapter = AttachmentFilterAdapter(
            linkFilters,
            selectedLinkNoteId
        ) { selectedFilter ->
            selectedLinkNoteId = selectedFilter.noteId

            currentLinkItemList = if (selectedFilter.noteId == FILTER_ALL) {
                ArrayList(allLinkItemList)
            } else {
                ArrayList(allLinkItemList.filter { it.noteId == selectedFilter.noteId })
            }

            loadLinks()
        }

        recyclerViewLinkFilters.adapter = linkFilterAdapter
    }

    private fun buildLinkFilters(): ArrayList<AttachmentFilterItem> {
        val filterList = ArrayList<AttachmentFilterItem>()

        filterList.add(
            AttachmentFilterItem(
                noteId = FILTER_ALL,
                noteTitle = "All",
                count = allLinkItemList.size
            )
        )

        val filterMap = linkedMapOf<Int, AttachmentFilterItem>()

        for (linkItem in allLinkItemList) {
            val currentFilter = filterMap[linkItem.noteId]

            if (currentFilter == null) {
                filterMap[linkItem.noteId] = AttachmentFilterItem(
                    noteId = linkItem.noteId,
                    noteTitle = linkItem.noteTitle,
                    count = 1
                )
            } else {
                filterMap[linkItem.noteId] = currentFilter.copy(
                    count = currentFilter.count + 1
                )
            }
        }

        filterList.addAll(filterMap.values)

        return filterList
    }

    private fun getAllActiveNoteLinkItems(): ArrayList<AllNoteLinkItem> {
        val linkItemList = ArrayList<AllNoteLinkItem>()
        val notes = databaseHelper.getAllNotes()

        for (note in notes) {
            if (note.isLocked == 1) {
                continue
            }

            val safeTitle = if (note.title.isNotBlank()) {
                note.title
            } else {
                "Untitled Note"
            }

            val linksFromNote = LinkUtils.extractWebLinks(note.content)

            for (link in linksFromNote) {
                val alreadyAddedForThisNote = linkItemList.any {
                    it.noteId == note.id &&
                            it.linkUrl.equals(link, ignoreCase = true)
                }

                if (!alreadyAddedForThisNote) {
                    linkItemList.add(
                        AllNoteLinkItem(
                            noteId = note.id,
                            noteTitle = safeTitle,
                            linkUrl = link
                        )
                    )
                }
            }
        }

        return linkItemList
    }

    private fun loadLinks() {
        val displayLinkList = ArrayList(
            currentLinkItemList.map { it.linkUrl }
        )

        if (displayLinkList.isEmpty()) {
            recyclerViewAllAttachmentLinks.visibility = View.GONE
            tvEmptyAllAttachmentLinks.visibility = View.VISIBLE
        } else {
            recyclerViewAllAttachmentLinks.visibility = View.VISIBLE
            tvEmptyAllAttachmentLinks.visibility = View.GONE
        }

        noteLinkAdapter = NoteLinkAdapter(
            displayLinkList,
            onLinkClick = { selectedLink ->
                LinkUtils.openWebUrl(this, selectedLink)
            }
        )

        recyclerViewAllAttachmentLinks.adapter = noteLinkAdapter
    }

    // ---------------------------
    // HELPERS
    // ---------------------------

    private fun getSafeNoteTitle(noteId: Int): String {
        val note = databaseHelper.getNoteById(noteId)

        return if (note != null && note.title.isNotBlank()) {
            note.title
        } else {
            "Untitled Note"
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
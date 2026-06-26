package com.example.noteappux

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.recyclerview.widget.ItemTouchHelper
import android.content.Context
import android.widget.ImageButton

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var tvAttachedLinksTitle: TextView
    private lateinit var btnBottomToggleLinks: ImageButton
    private var showAttachedLinksSection: Boolean = false

    private lateinit var tvAddEditTitle: TextView
    private lateinit var tvAddEditSubtitle: TextView

    private lateinit var btnViewAttachments: ImageButton
    private lateinit var btnTopChooseFolder: ImageButton
    private lateinit var btnTopDeleteNote: ImageButton

    private lateinit var etNoteTitle: EditText
    private lateinit var etNoteContent: EditText

    private lateinit var btnClassicMode: TextView
    private lateinit var btnChatMode: TextView
    private lateinit var classicContentSection: LinearLayout
    private lateinit var chatContentSection: LinearLayout
    private lateinit var recyclerViewNoteMessages: RecyclerView
    private lateinit var etChatMessage: EditText
    private lateinit var btnSendChatMessage: TextView
    private lateinit var btnChatAddImage: TextView
    private lateinit var btnChatAddFile: TextView

    private lateinit var btnAddCategories: ImageButton
    private lateinit var categoryChipContainer: LinearLayout
    private lateinit var tvSelectedFolder: TextView

    private lateinit var tvPrivacyStatus: TextView
    private lateinit var btnPrivacyLock: ImageButton
    private lateinit var btnTextShortcut: ImageButton
    private lateinit var btnToggleBulletSection: ImageButton
    private var showBulletSection: Boolean = false

    private lateinit var tvShortcutStatus: TextView

    private lateinit var recyclerViewNoteLinks: RecyclerView
    private lateinit var tvEmptyLinks: TextView

    private lateinit var tvAttachedImagesTitle: TextView
    private lateinit var recyclerViewNoteImages: RecyclerView

    private lateinit var tvAttachedFilesTitle: TextView
    private lateinit var recyclerViewNoteFiles: RecyclerView

    private lateinit var btnBottomAddImage: ImageButton
    private lateinit var btnBottomAddFile: ImageButton
    private lateinit var btnSaveNote: ImageButton

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var noteImageAdapter: NoteImageAdapter
    private var noteImageList = ArrayList<NoteImage>()

    private lateinit var noteFileAdapter: NoteFileAdapter
    private var noteFileList = ArrayList<NoteFile>()

    private lateinit var noteLinkAdapter: NoteLinkAdapter
    private var noteLinkList = ArrayList<String>()

    private lateinit var noteMessageAdapter: NoteMessageAdapter
    private var noteMessageList = ArrayList<NoteMessage>()
    private var pendingNoteMessages = ArrayList<NoteMessage>()

    private var noteId: Int = -1
    private var isEditMode: Boolean = false

    private var selectedCategoryIds = ArrayList<Int>()
    private var selectedFolderId: Int = 0

    private var isLocked: Int = 0
    private var passcodeHash: String = ""
    private var lockType: Int = DatabaseHelper.LOCK_NONE
    private var showTitleWhenLocked: Int = 0

    private var shortcutEnabled: Int = 0
    private var shortcutKeyword: String = ""
    private var pendingShortcutKeywords = ArrayList<String>()

    private var currentNoteMode: String = DatabaseHelper.NOTE_MODE_CLASSIC
    private var lastSyncedClassicContent: String = ""

    private var currentCameraImageFile: File? = null
    private var currentCameraImageUri: Uri? = null
    private var pendingImageUris = ArrayList<String>()
    private var pendingFiles = ArrayList<NoteFile>()

    private lateinit var bulletTitleOptionsSection: LinearLayout
    private lateinit var tvBulletPointsTitle: TextView
    private lateinit var btnBulletOptions: TextView
    private lateinit var btnAddBulletPoint: TextView
    private lateinit var recyclerViewNoteBullets: RecyclerView
    private lateinit var tvEmptyBullets: TextView

    private lateinit var noteBulletAdapter: NoteBulletAdapter

    // This list is only what the RecyclerView displays.
// It can be filtered or sorted depending on display settings.
    private var noteBulletList = ArrayList<NoteBullet>()

    // This list always contains every bullet point.
// Use this for progress count and saving correct order.
    private var allNoteBulletList = ArrayList<NoteBullet>()

    private var pendingBullets = ArrayList<NoteBullet>()
    private lateinit var bulletItemTouchHelper: ItemTouchHelper

    private var autoMoveCompletedBullets: Boolean = false
    private var hideCompletedBullets: Boolean = false

    private var shouldShowPrivacyStatusTemporarily: Boolean = false
    private var shouldShowShortcutStatusTemporarily: Boolean = false


    private val galleryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { selectedUris ->
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        saveGalleryImagesToNote(selectedUris)
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            saveCameraImageToNote()
        } else {
            Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { selectedUris ->
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        saveSelectedFilesToNote(selectedUris)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        tvAttachedLinksTitle = findViewById(R.id.tvAttachedLinksTitle)
        btnBottomToggleLinks = findViewById(R.id.btnBottomToggleLinks)

        tvAddEditTitle = findViewById(R.id.tvAddEditTitle)
        tvAddEditSubtitle = findViewById(R.id.tvAddEditSubtitle)

        btnViewAttachments = findViewById(R.id.btnViewAttachments)
        btnTopChooseFolder = findViewById(R.id.btnTopChooseFolder)
        btnTopDeleteNote = findViewById(R.id.btnTopDeleteNote)

        etNoteTitle = findViewById(R.id.etNoteTitle)
        etNoteContent = findViewById(R.id.etNoteContent)

        btnClassicMode = findViewById(R.id.btnClassicMode)
        btnChatMode = findViewById(R.id.btnChatMode)
        classicContentSection = findViewById(R.id.classicContentSection)
        chatContentSection = findViewById(R.id.chatContentSection)

        recyclerViewNoteMessages = findViewById(R.id.recyclerViewNoteMessages)
        etChatMessage = findViewById(R.id.etChatMessage)

        btnSendChatMessage = findViewById(R.id.btnSendChatMessage)
        btnChatAddImage = findViewById(R.id.btnChatAddImage)
        btnChatAddFile = findViewById(R.id.btnChatAddFile)

        btnAddCategories = findViewById(R.id.btnAddCategories)
        categoryChipContainer = findViewById(R.id.categoryChipContainer)
        tvSelectedFolder = findViewById(R.id.tvSelectedFolder)

        btnPrivacyLock = findViewById(R.id.btnPrivacyLock)
        tvPrivacyStatus = findViewById(R.id.tvPrivacyStatus)
        btnTextShortcut = findViewById(R.id.btnTextShortcut)
        btnToggleBulletSection = findViewById(R.id.btnToggleBulletSection)
        tvShortcutStatus = findViewById(R.id.tvShortcutStatus)

        recyclerViewNoteLinks = findViewById(R.id.recyclerViewNoteLinks)
        tvEmptyLinks = findViewById(R.id.tvEmptyLinks)

        tvAttachedImagesTitle = findViewById(R.id.tvAttachedImagesTitle)
        recyclerViewNoteImages = findViewById(R.id.recyclerViewNoteImages)

        tvAttachedFilesTitle = findViewById(R.id.tvAttachedFilesTitle)
        recyclerViewNoteFiles = findViewById(R.id.recyclerViewNoteFiles)

        bulletTitleOptionsSection = findViewById(R.id.bulletTitleOptionsSection)
        tvBulletPointsTitle = findViewById(R.id.tvBulletPointsTitle)
        btnBulletOptions = findViewById(R.id.btnBulletOptions)
        btnAddBulletPoint = findViewById(R.id.btnAddBulletPoint)
        recyclerViewNoteBullets = findViewById(R.id.recyclerViewNoteBullets)
        tvEmptyBullets = findViewById(R.id.tvEmptyBullets)

        btnBottomAddImage = findViewById(R.id.btnBottomAddImage)
        btnBottomAddFile = findViewById(R.id.btnBottomAddFile)
        btnSaveNote = findViewById(R.id.btnSaveNote)

        databaseHelper = DatabaseHelper(this)

        loadBulletDisplaySettings()

        recyclerViewNoteImages.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewNoteFiles.layoutManager = LinearLayoutManager(this)
        recyclerViewNoteLinks.layoutManager = LinearLayoutManager(this)
        recyclerViewNoteBullets.layoutManager = LinearLayoutManager(this)
        setupBulletDragAndDrop()

        recyclerViewNoteMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        checkEditMode()

        LinkUtils.setupEditableWebLinks(this, etNoteContent) {
            loadNoteLinks()
        }

        loadNoteImages()
        loadNoteFiles()
        loadNoteLinks()
        loadNoteMessages()
        loadNoteBullets()

        btnBottomToggleLinks.setOnClickListener {
            showAttachedLinksSection = !showAttachedLinksSection
            loadNoteLinks()
        }


        btnAddCategories.setOnClickListener {
            showCategoryPickerDialog()
        }

        btnPrivacyLock.setOnClickListener {
            shouldShowPrivacyStatusTemporarily = true
            showPrivacyOptions()
        }

        btnTextShortcut.setOnClickListener {
            shouldShowShortcutStatusTemporarily = true
            showTextShortcutOptions()
        }

        btnToggleBulletSection.setOnClickListener {
            showBulletSection = !showBulletSection
            updateBulletSectionVisibility()
        }

        btnBottomAddImage.setOnClickListener {
            showAddImageOptions()
        }

        btnChatAddImage.setOnClickListener {
            showAddImageOptions()
        }

        btnBottomAddFile.setOnClickListener {
            openFilePicker()
        }

        btnChatAddFile.setOnClickListener {
            openFilePicker()
        }

        btnViewAttachments.setOnClickListener {
            openAttachmentsPage()
        }

        btnTopChooseFolder.setOnClickListener {
            showFolderPickerDialog()
        }

        btnTopDeleteNote.setOnClickListener {
            deleteNote()
        }

        btnSaveNote.setOnClickListener {
            saveNote()
        }


        btnClassicMode.setOnClickListener {
            switchNoteMode(DatabaseHelper.NOTE_MODE_CLASSIC)
        }

        btnChatMode.setOnClickListener {
            switchNoteMode(DatabaseHelper.NOTE_MODE_CHAT)
        }

        btnSendChatMessage.setOnClickListener {
            sendTextMessage()
        }

        btnAddBulletPoint.setOnClickListener {
            showAddBulletDialog()
        }

        btnBulletOptions.setOnClickListener {
            showBulletDisplayOptionsDialog()
        }

        etChatMessage.imeOptions = EditorInfo.IME_ACTION_SEND
        etChatMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextMessage()
                true
            } else {
                false
            }
        }
    }

    private fun checkEditMode() {
        noteId = intent.getIntExtra("note_id", -1)

        if (noteId != -1) {
            isEditMode = true

            val noteFromDatabase = databaseHelper.getNoteById(noteId)

            if (noteFromDatabase != null) {
                etNoteTitle.setText(noteFromDatabase.title)
                etNoteContent.setText(noteFromDatabase.content)

                currentNoteMode = noteFromDatabase.noteMode

                isLocked = noteFromDatabase.isLocked
                passcodeHash = noteFromDatabase.passcodeHash
                lockType = noteFromDatabase.lockType
                showTitleWhenLocked = noteFromDatabase.showTitleWhenLocked

                shortcutEnabled = noteFromDatabase.shortcutEnabled
                shortcutKeyword = noteFromDatabase.shortcutKeyword
            } else {
                val title = intent.getStringExtra("note_title") ?: ""
                val content = intent.getStringExtra("note_content") ?: ""

                etNoteTitle.setText(title)
                etNoteContent.setText(content)

                currentNoteMode = DatabaseHelper.NOTE_MODE_CLASSIC

                isLocked = 0
                passcodeHash = ""
                lockType = DatabaseHelper.LOCK_NONE
                showTitleWhenLocked = 0

                shortcutEnabled = 0
                shortcutKeyword = ""
            }

            selectedCategoryIds = databaseHelper.getCategoryIdsForNote(noteId)
            selectedFolderId = databaseHelper.getFolderIdForNote(noteId)

            updateSelectedCategoriesText()
            updateSelectedFolderText()
            updatePrivacyText()
            updateShortcutText()
            updateAttachmentButtonText()

            tvAddEditTitle.text = "Edit Note"
            btnSaveNote.contentDescription = "Update note"
            btnTopDeleteNote.visibility = View.VISIBLE
        } else {
            isEditMode = false

            selectedCategoryIds.clear()
            selectedFolderId = 0

            isLocked = 0
            passcodeHash = ""
            lockType = DatabaseHelper.LOCK_NONE
            showTitleWhenLocked = 0

            shortcutEnabled = 0
            shortcutKeyword = ""

            currentNoteMode = DatabaseHelper.NOTE_MODE_CLASSIC

            pendingShortcutKeywords.clear()
            pendingImageUris.clear()
            pendingFiles.clear()
            pendingNoteMessages.clear()
            pendingBullets.clear()

            updateSelectedCategoriesText()
            updateSelectedFolderText()
            updatePrivacyText()
            updateShortcutText()

            tvAddEditTitle.text = "Create Note"
            btnSaveNote.contentDescription = "Save note"
            btnTopDeleteNote.visibility = View.GONE
        }

//        switchNoteMode(currentNoteMode)

        applyNoteModeUI(
            mode = currentNoteMode,
            syncData = false
        )
    }

    // ---------------------------
// CHAT NOTE MODE FUNCTIONS
// ---------------------------

    private fun switchNoteMode(mode: String) {
        applyNoteModeUI(
            mode = mode,
            syncData = true
        )
    }

    private fun applyNoteModeUI(
        mode: String,
        syncData: Boolean
    ) {
        if (syncData) {
            if (mode == DatabaseHelper.NOTE_MODE_CHAT) {
                syncMessagesFromClassicContent()
            } else {
                syncClassicContentFromMessages()
            }
        }

        currentNoteMode = mode

        if (mode == DatabaseHelper.NOTE_MODE_CHAT) {
            classicContentSection.visibility = View.GONE
            chatContentSection.visibility = View.VISIBLE

            btnClassicMode.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            btnChatMode.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))

            tvAddEditSubtitle.text = "Chat with yourself"

            loadNoteMessages()
        } else {
            classicContentSection.visibility = View.VISIBLE
            chatContentSection.visibility = View.GONE

            btnClassicMode.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            btnChatMode.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

            tvAddEditSubtitle.text = if (isEditMode) {
                "Update your note"
            } else {
                "Write your thoughts here"
            }
        }

        updateImageSectionVisibility()
        updateFileSectionVisibility()
        updateBottomClassicAttachmentButtonsVisibility()
        updateLinkSectionVisibility()
        updateBulletSectionVisibility()
    }

    private fun splitClassicContentIntoBubbles(content: String): ArrayList<String> {
        val bubbleList = ArrayList<String>()

        val parts = content
            .trim()
            .split(Regex("\\n\\s*\\n"))

        for (part in parts) {
            val cleanedPart = part.trim()

            if (cleanedPart.isNotEmpty()) {
                bubbleList.add(cleanedPart)
            }
        }

        return bubbleList
    }

    private fun joinMessagesIntoClassicContent(messages: List<NoteMessage>): String {
        return messages
            .filter { it.messageType == DatabaseHelper.MESSAGE_TYPE_TEXT }
            .map { it.textContent.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
    }

    private fun hasAnyChatMessages(): Boolean {
        return if (isEditMode && noteId != -1) {
            databaseHelper.getMessagesForNote(noteId).isNotEmpty()
        } else {
            pendingNoteMessages.isNotEmpty()
        }
    }

    private fun syncMessagesFromClassicContent() {
        val classicContent = etNoteContent.text.toString().trim()

        val hasExistingMessages = if (isEditMode && noteId != -1) {
            databaseHelper.getMessagesForNote(noteId).isNotEmpty()
        } else {
            pendingNoteMessages.isNotEmpty()
        }

        val classicContentWasGeneratedFromChat =
            classicContent == lastSyncedClassicContent

        if (hasExistingMessages && classicContentWasGeneratedFromChat) {
            loadNoteMessages()
            return
        }

        val bubbleTexts = if (classicContent.isEmpty()) {
            ArrayList()
        } else {
            splitClassicContentIntoBubbles(classicContent)
        }

        if (isEditMode && noteId != -1) {
            val existingMessages = databaseHelper.getMessagesForNote(noteId)
            val nonTextMessages = existingMessages.filter {
                it.messageType != DatabaseHelper.MESSAGE_TYPE_TEXT
            }

            databaseHelper.deleteAllMessagesForNote(noteId)

            var order = 0

            for (text in bubbleTexts) {
                databaseHelper.addNoteMessage(
                    NoteMessage(
                        id = 0,
                        noteId = noteId,
                        messageType = DatabaseHelper.MESSAGE_TYPE_TEXT,
                        textContent = text,
                        imageUri = "",
                        fileName = "",
                        fileUri = "",
                        linkUrl = "",
                        createdAt = System.currentTimeMillis(),
                        messageOrder = order
                    )
                )

                order++
            }

            for (message in nonTextMessages) {
                databaseHelper.addNoteMessage(
                    message.copy(
                        id = 0,
                        noteId = noteId,
                        messageOrder = order
                    )
                )

                order++
            }
        } else {
            val nonTextMessages = pendingNoteMessages.filter {
                it.messageType != DatabaseHelper.MESSAGE_TYPE_TEXT
            }

            pendingNoteMessages.clear()

            var order = 0

            for (text in bubbleTexts) {
                pendingNoteMessages.add(
                    NoteMessage(
                        id = -1 - order,
                        noteId = -1,
                        messageType = DatabaseHelper.MESSAGE_TYPE_TEXT,
                        textContent = text,
                        imageUri = "",
                        fileName = "",
                        fileUri = "",
                        linkUrl = "",
                        createdAt = System.currentTimeMillis(),
                        messageOrder = order
                    )
                )

                order++
            }

            for (message in nonTextMessages) {
                pendingNoteMessages.add(
                    message.copy(
                        id = -1 - order,
                        noteId = -1,
                        messageOrder = order
                    )
                )

                order++
            }
        }

        lastSyncedClassicContent = classicContent

        loadNoteMessages()
    }

    private fun syncClassicContentFromMessages() {
        val messages = if (isEditMode && noteId != -1) {
            databaseHelper.getMessagesForNote(noteId)
        } else {
            pendingNoteMessages
        }

        val classicContent = joinMessagesIntoClassicContent(messages)

        etNoteContent.setText(classicContent)
        etNoteContent.setSelection(etNoteContent.text.length)

        lastSyncedClassicContent = classicContent

        loadNoteLinks()
    }

    private fun loadNoteMessages() {
        noteMessageList = if (isEditMode && noteId != -1) {
            databaseHelper.getMessagesForNote(noteId)
        } else {
            ArrayList()
        }

        if (!isEditMode || noteId == -1) {
            noteMessageList.addAll(pendingNoteMessages)
        }

        noteMessageAdapter = NoteMessageAdapter(
            messageList = noteMessageList,
            onMessageLongClick = { message ->
                showMessageOptions(message)
            },
            onImageClick = { message ->
                openChatImage(message)
            },
            onFileClick = { message ->
                openChatFile(message)
            }
        )

        recyclerViewNoteMessages.adapter = noteMessageAdapter

        if (noteMessageList.isNotEmpty()) {
            recyclerViewNoteMessages.scrollToPosition(noteMessageList.size - 1)
        }
    }

    private fun sendTextMessage() {
        val messageText = etChatMessage.text.toString().trim()

        if (messageText.isEmpty()) {
            return
        }

        val newMessage = NoteMessage(
            id = 0,
            noteId = if (isEditMode && noteId != -1) noteId else -1,
            messageType = DatabaseHelper.MESSAGE_TYPE_TEXT,
            textContent = messageText,
            imageUri = "",
            fileName = "",
            fileUri = "",
            linkUrl = "",
            createdAt = System.currentTimeMillis(),
            messageOrder = noteMessageList.size
        )

        if (isEditMode && noteId != -1) {
            val result = databaseHelper.addNoteMessage(
                newMessage.copy(noteId = noteId)
            )

            if (result == -1L) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            pendingNoteMessages.add(newMessage)
        }

        etChatMessage.text.clear()
        loadNoteMessages()
        syncClassicContentFromMessages()
    }

    private fun showMessageOptions(message: NoteMessage) {
        val options = arrayOf(
            "Delete Message"
        )

        AlertDialog.Builder(this)
            .setTitle("Message Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> confirmDeleteMessage(message)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteMessage(message: NoteMessage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: NoteMessage) {
        if (message.messageType == DatabaseHelper.MESSAGE_TYPE_IMAGE) {
            deleteImageBubbleMessage(message)
            return
        }

        if (message.messageType == DatabaseHelper.MESSAGE_TYPE_FILE) {
            deleteFileBubbleMessage(message)
            return
        }

        if (message.id <= 0) {
            pendingNoteMessages.remove(message)
            loadNoteMessages()
            syncClassicContentFromMessages()
            Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
            return
        }

        val result = databaseHelper.deleteNoteMessage(message.id)

        if (result > 0) {
            Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
            loadNoteMessages()
            syncClassicContentFromMessages()
        } else {
            Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImageBubbleMessage(message: NoteMessage) {
        if (message.id <= 0) {
            pendingNoteMessages.remove(message)
            pendingImageUris.remove(message.imageUri)
            deleteLocalImageFile(message.imageUri)

            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
            loadNoteMessages()
            loadNoteImages()
            updateAttachmentButtonText()
            return
        }

        val messageDeleteResult = databaseHelper.deleteNoteMessage(message.id)
        databaseHelper.deleteImageFromNoteByUri(noteId, message.imageUri)

        if (messageDeleteResult > 0) {
            deleteLocalImageFile(message.imageUri)

            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
            loadNoteMessages()
            loadNoteImages()
            updateAttachmentButtonText()
        } else {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFileBubbleMessage(message: NoteMessage) {
        if (message.id <= 0) {
            pendingNoteMessages.remove(message)

            pendingFiles.removeAll {
                it.fileUri == message.fileUri
            }

            deleteLocalFile(message.fileUri)

            Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
            loadNoteMessages()
            loadNoteFiles()
            updateAttachmentButtonText()
            return
        }

        val messageDeleteResult = databaseHelper.deleteNoteMessage(message.id)
        databaseHelper.deleteFileFromNoteByUri(noteId, message.fileUri)

        if (messageDeleteResult > 0) {
            deleteLocalFile(message.fileUri)

            Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
            loadNoteMessages()
            loadNoteFiles()
            updateAttachmentButtonText()
        } else {
            Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePendingMessagesToNewNote(newNoteId: Int) {
        for (i in pendingNoteMessages.indices) {
            val message = pendingNoteMessages[i].copy(
                noteId = newNoteId,
                messageOrder = i
            )

            databaseHelper.addNoteMessage(message)
        }

        pendingNoteMessages.clear()
    }

    private fun buildChatPreviewText(): String {
        val messages = if (isEditMode && noteId != -1) {
            databaseHelper.getMessagesForNote(noteId)
        } else {
            pendingNoteMessages
        }

        return joinMessagesIntoClassicContent(messages)
    }

    private fun getNextMessageOrder(): Int {
        return if (isEditMode && noteId != -1) {
            databaseHelper.getMessagesForNote(noteId).size
        } else {
            pendingNoteMessages.size
        }
    }

    private fun addImageBubbleToChat(imageUri: String) {
        val imageMessage = NoteMessage(
            id = 0,
            noteId = if (isEditMode && noteId != -1) noteId else -1,
            messageType = DatabaseHelper.MESSAGE_TYPE_IMAGE,
            textContent = "",
            imageUri = imageUri,
            fileName = "",
            fileUri = "",
            linkUrl = "",
            createdAt = System.currentTimeMillis(),
            messageOrder = getNextMessageOrder()
        )

        if (isEditMode && noteId != -1) {
            databaseHelper.addNoteMessage(
                imageMessage.copy(noteId = noteId)
            )
        } else {
            pendingNoteMessages.add(
                imageMessage.copy(
                    id = -1 - pendingNoteMessages.size,
                    noteId = -1
                )
            )
        }

        loadNoteMessages()
    }

    private fun openChatImage(message: NoteMessage) {
        if (message.imageUri.isBlank()) {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isEditMode || noteId == -1) {
            Toast.makeText(
                this,
                "Save the note first to view image full screen",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val images = databaseHelper.getImagesForNote(noteId)

        var selectedPosition = images.indexOfFirst {
            it.imageUri == message.imageUri
        }

        if (selectedPosition == -1) {
            selectedPosition = 0
        }

        val intent = Intent(this, NoteImageViewerActivity::class.java)
        intent.putExtra("note_id", noteId)
        intent.putExtra("selected_position", selectedPosition)
        startActivity(intent)
    }

    private fun addFileBubbleToChat(
        fileName: String,
        fileUri: String
    ) {
        val fileMessage = NoteMessage(
            id = 0,
            noteId = if (isEditMode && noteId != -1) noteId else -1,
            messageType = DatabaseHelper.MESSAGE_TYPE_FILE,
            textContent = "",
            imageUri = "",
            fileName = fileName,
            fileUri = fileUri,
            linkUrl = "",
            createdAt = System.currentTimeMillis(),
            messageOrder = getNextMessageOrder()
        )

        if (isEditMode && noteId != -1) {
            databaseHelper.addNoteMessage(
                fileMessage.copy(noteId = noteId)
            )
        } else {
            pendingNoteMessages.add(
                fileMessage.copy(
                    id = -1 - pendingNoteMessages.size,
                    noteId = -1
                )
            )
        }

        loadNoteMessages()
    }

    private fun openChatFile(message: NoteMessage) {
        if (message.fileUri.isBlank()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        val noteFile = NoteFile(
            id = message.id,
            noteId = message.noteId,
            fileName = message.fileName,
            fileUri = message.fileUri,
            createdDate = message.createdAt
        )

        openNoteFile(noteFile)
    }

    // ---------------------------
    // PRIVACY FUNCTIONS
    // ---------------------------

    private fun showPrivacyOptions() {
        if (isLocked == 1) {
            val titleOption = if (showTitleWhenLocked == 1) {
                "Hide Title When Locked"
            } else {
                "Show Title When Locked"
            }

            val options = arrayOf(
                "Change Unlock Method",
                "Change Passcode",
                titleOption,
                "Remove Lock"
            )

            AlertDialog.Builder(this)
                .setTitle("Privacy Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showLockMethodDialog()
                        1 -> {
                            if (lockType == DatabaseHelper.LOCK_FINGERPRINT_ONLY) {
                                Toast.makeText(
                                    this,
                                    "This note uses fingerprint only. No passcode to change.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showSetPasscodeDialog(
                                    targetLockType = lockType,
                                    isChangingPasscode = true
                                )
                            }
                        }

                        2 -> toggleShowTitleWhenLocked()
                        3 -> confirmRemoveLock()
                    }
                }
                .show()
        } else {
            showLockMethodDialog()
        }
    }

    private fun showLockMethodDialog() {
        val options = arrayOf(
            "Passcode Only",
            "Fingerprint Only",
            "Fingerprint + Passcode"
        )

        val checkedIndex = when (lockType) {
            DatabaseHelper.LOCK_PASSCODE_ONLY -> 0
            DatabaseHelper.LOCK_FINGERPRINT_ONLY -> 1
            DatabaseHelper.LOCK_BOTH -> 2
            else -> -1
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Unlock Method")
            .setSingleChoiceItems(options, checkedIndex) { dialog, which ->
                dialog.dismiss()

                when (which) {
                    0 -> {
                        showSetPasscodeDialog(
                            targetLockType = DatabaseHelper.LOCK_PASSCODE_ONLY,
                            isChangingPasscode = false
                        )
                    }

                    1 -> {
                        if (BiometricHelper.canUseBiometric(this)) {
                            isLocked = 1
                            lockType = DatabaseHelper.LOCK_FINGERPRINT_ONLY
                            passcodeHash = ""

                            disableShortcutBecauseNoteLocked()
                            savePrivacyToDatabaseIfEditing()
                            updatePrivacyText()

                            Toast.makeText(
                                this,
                                "Fingerprint lock selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Fingerprint is not available. Please choose passcode.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    2 -> {
                        if (BiometricHelper.canUseBiometric(this)) {
                            showSetPasscodeDialog(
                                targetLockType = DatabaseHelper.LOCK_BOTH,
                                isChangingPasscode = false
                            )
                        } else {
                            Toast.makeText(
                                this,
                                "Fingerprint is not available. Please choose passcode only.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetPasscodeDialog(
        targetLockType: Int,
        isChangingPasscode: Boolean
    ) {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(40, 20, 40, 10)

        val passcodeInput = EditText(this)
        passcodeInput.hint = "Enter passcode"
        passcodeInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        passcodeInput.setSingleLine(true)
        passcodeInput.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        passcodeInput.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        val confirmInput = EditText(this)
        confirmInput.hint = "Confirm passcode"
        confirmInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        confirmInput.setSingleLine(true)
        confirmInput.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        confirmInput.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        container.addView(passcodeInput)
        container.addView(confirmInput)

        val title = if (isChangingPasscode) {
            "Change Passcode"
        } else {
            when (targetLockType) {
                DatabaseHelper.LOCK_PASSCODE_ONLY -> "Lock With Passcode"
                DatabaseHelper.LOCK_BOTH -> "Lock With Fingerprint + Passcode"
                else -> "Set Passcode"
            }
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Set a passcode for this note")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val passcode = passcodeInput.text.toString().trim()
                val confirmPasscode = confirmInput.text.toString().trim()

                if (passcode.length < 4) {
                    Toast.makeText(
                        this,
                        "Passcode must be at least 4 digits",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (passcode != confirmPasscode) {
                    Toast.makeText(
                        this,
                        "Passcodes do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                passcodeHash = SecurityHelper.hashPasscode(passcode)
                isLocked = 1
                lockType = targetLockType

                disableShortcutBecauseNoteLocked()
                savePrivacyToDatabaseIfEditing()
                updatePrivacyText()

                Toast.makeText(
                    this,
                    "Privacy updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleShowTitleWhenLocked() {
        showTitleWhenLocked = if (showTitleWhenLocked == 1) 0 else 1

        savePrivacyToDatabaseIfEditing()
        updatePrivacyText()

        val message = if (showTitleWhenLocked == 1) {
            "Title will be shown when locked"
        } else {
            "Title will be hidden when locked"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun confirmRemoveLock() {
        AlertDialog.Builder(this)
            .setTitle("Remove Lock")
            .setMessage("Remove privacy lock from this note?")
            .setPositiveButton("Remove") { _, _ ->
                isLocked = 0
                passcodeHash = ""
                lockType = DatabaseHelper.LOCK_NONE
                showTitleWhenLocked = 0

                if (isEditMode && noteId != -1) {
                    val result = databaseHelper.removeNoteLock(noteId)

                    if (result > 0) {
                        Toast.makeText(this, "Lock removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to remove lock", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Lock removed", Toast.LENGTH_SHORT).show()
                }

                updatePrivacyText()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun savePrivacyToDatabaseIfEditing() {
        if (isLocked == 1) {
            disableShortcutBecauseNoteLocked()
        }

        if (isEditMode && noteId != -1) {
            databaseHelper.setNotePrivacy(
                noteId = noteId,
                isLocked = isLocked,
                lockType = lockType,
                passcodeHash = passcodeHash,
                showTitleWhenLocked = showTitleWhenLocked
            )
        } else {
            Toast.makeText(
                this,
                "Privacy will be applied when note is saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showStatusForSevenSeconds(statusView: TextView) {
        statusView.removeCallbacks(null)
        statusView.visibility = View.VISIBLE

        statusView.postDelayed({
            statusView.visibility = View.GONE
        }, 7000)
    }

    private fun updatePrivacyText() {
        if (isLocked == 1) {
            val methodText = when (lockType) {
                DatabaseHelper.LOCK_PASSCODE_ONLY -> "Passcode only"
                DatabaseHelper.LOCK_FINGERPRINT_ONLY -> "Fingerprint only"
                DatabaseHelper.LOCK_BOTH -> "Fingerprint + Passcode"
                else -> "Locked"
            }

            val titleText = if (showTitleWhenLocked == 1) {
                "Title visible"
            } else {
                "Title hidden"
            }

            btnPrivacyLock.contentDescription = "Privacy locked"
            tvPrivacyStatus.text = "$methodText • $titleText"
        } else {
            btnPrivacyLock.contentDescription = "Lock note"
            tvPrivacyStatus.text = "Privacy: Not locked"
        }

        if (shouldShowPrivacyStatusTemporarily) {
            shouldShowPrivacyStatusTemporarily = false
            showStatusForSevenSeconds(tvPrivacyStatus)
        } else {
            tvPrivacyStatus.visibility = View.GONE
        }
    }


    // ---------------------------
    // TEXT SHORTCUT FUNCTIONS
    // ---------------------------


    private fun showTextShortcutOptions() {
        if (isLocked == 1) {
            tvShortcutStatus.text = "Locked notes cannot use text shortcuts."
            showStatusForSevenSeconds(tvShortcutStatus)
            shouldShowShortcutStatusTemporarily = false

            Toast.makeText(
                this,
                "Locked notes cannot use text shortcuts for privacy.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        showShortcutManagerDialog()
    }

    private fun showShortcutManagerDialog() {
        val shortcuts = if (isEditMode && noteId != -1) {
            databaseHelper.getShortcutsForNote(noteId)
        } else {
            val pendingList = ArrayList<NoteShortcut>()

            for (i in pendingShortcutKeywords.indices) {
                pendingList.add(
                    NoteShortcut(
                        id = -1 - i,
                        noteId = -1,
                        shortcutKeyword = pendingShortcutKeywords[i],
                        createdDate = System.currentTimeMillis()
                    )
                )
            }

            pendingList
        }

        val shortcutCount = shortcuts.size

        if (shortcutCount == 0) {
            AlertDialog.Builder(this)
                .setTitle("Text Shortcuts")
                .setMessage("No shortcuts yet.\n\nYou can add up to 4 shortcuts for this note.")
                .setPositiveButton("Add Shortcut") { _, _ ->
                    showAddShortcutDialog()
                }
                .setNegativeButton("Close", null)
                .show()

            return
        }

        val optionList = ArrayList<String>()
        optionList.add("+ Add Shortcut")

        for (shortcut in shortcuts) {
            optionList.add(shortcut.shortcutKeyword)
        }

        AlertDialog.Builder(this)
            .setTitle("Text Shortcuts (${shortcutCount}/4)")
            .setItems(optionList.toTypedArray()) { _, which ->
                if (which == 0) {
                    if (shortcutCount >= 4) {
                        Toast.makeText(
                            this,
                            "Maximum 4 shortcuts per note",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showAddShortcutDialog()
                    }
                } else {
                    val selectedShortcut = shortcuts[which - 1]
                    showShortcutActionDialog(selectedShortcut)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showShortcutActionDialog(shortcut: NoteShortcut) {
        val options = arrayOf(
            "Edit Shortcut",
            "Delete Shortcut"
        )

        AlertDialog.Builder(this)
            .setTitle(shortcut.shortcutKeyword)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditShortcutDialog(shortcut)
                    1 -> confirmDeleteShortcut(shortcut)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddShortcutDialog() {
        val input = EditText(this)
        input.hint = "Example: /dd"
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("Add Shortcut")
            .setMessage("Add a shortcut for this note. Example: /dd")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val rawKeyword = input.text.toString().trim()
                addShortcutKeyword(rawKeyword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addShortcutKeyword(rawKeyword: String) {
        val keyword = normalizeShortcutKeyword(rawKeyword)

        if (keyword.length < 2) {
            Toast.makeText(this, "Shortcut is too short", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && noteId != -1) {
            val result = databaseHelper.addShortcutToNote(
                noteId = noteId,
                keyword = keyword
            )

            when {
                result > 0 -> {
                    Toast.makeText(this, "Shortcut added", Toast.LENGTH_SHORT).show()
                    updateShortcutText()
                }

                result == -2L -> {
                    Toast.makeText(this, "Maximum 4 shortcuts per note", Toast.LENGTH_SHORT).show()
                }

                result == -3L -> {
                    Toast.makeText(this, "This shortcut is already used", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Failed to add shortcut", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if (pendingShortcutKeywords.size >= 4) {
                Toast.makeText(this, "Maximum 4 shortcuts per note", Toast.LENGTH_SHORT).show()
                return
            }

            val alreadyExists = pendingShortcutKeywords.any {
                it.equals(keyword, ignoreCase = true)
            }

            if (alreadyExists) {
                Toast.makeText(this, "This shortcut is already added", Toast.LENGTH_SHORT).show()
                return
            }

            val isTaken = databaseHelper.isShortcutKeywordTaken(keyword)

            if (isTaken) {
                Toast.makeText(this, "This shortcut is already used", Toast.LENGTH_SHORT).show()
                return
            }

            pendingShortcutKeywords.add(keyword)
            Toast.makeText(this, "Shortcut added", Toast.LENGTH_SHORT).show()
            updateShortcutText()
        }
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
            .setMessage("Update this shortcut keyword")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newKeyword = normalizeShortcutKeyword(input.text.toString().trim())
                updateShortcutKeyword(shortcut, newKeyword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateShortcutKeyword(shortcut: NoteShortcut, newKeyword: String) {
        if (newKeyword.length < 2) {
            Toast.makeText(this, "Shortcut is too short", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && noteId != -1 && shortcut.id > 0) {
            val result = databaseHelper.updateNoteShortcutKeyword(
                shortcutId = shortcut.id,
                newKeyword = newKeyword
            )

            when {
                result > 0 -> {
                    Toast.makeText(this, "Shortcut updated", Toast.LENGTH_SHORT).show()
                    updateShortcutText()
                }

                result == -3 -> {
                    Toast.makeText(this, "This shortcut is already used", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Failed to update shortcut", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val index = pendingShortcutKeywords.indexOf(shortcut.shortcutKeyword)

            if (index == -1) {
                Toast.makeText(this, "Shortcut not found", Toast.LENGTH_SHORT).show()
                return
            }

            val duplicateInPending = pendingShortcutKeywords.any {
                it.equals(newKeyword, ignoreCase = true) &&
                        !it.equals(shortcut.shortcutKeyword, ignoreCase = true)
            }

            if (duplicateInPending) {
                Toast.makeText(this, "This shortcut is already added", Toast.LENGTH_SHORT).show()
                return
            }

            val isTaken = databaseHelper.isShortcutKeywordTaken(newKeyword)

            if (isTaken) {
                Toast.makeText(this, "This shortcut is already used", Toast.LENGTH_SHORT).show()
                return
            }

            pendingShortcutKeywords[index] = newKeyword
            Toast.makeText(this, "Shortcut updated", Toast.LENGTH_SHORT).show()
            updateShortcutText()
        }
    }

    private fun confirmDeleteShortcut(shortcut: NoteShortcut) {
        AlertDialog.Builder(this)
            .setTitle("Delete Shortcut")
            .setMessage("Remove shortcut \"${shortcut.shortcutKeyword}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteShortcut(shortcut)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteShortcut(shortcut: NoteShortcut) {
        if (isEditMode && noteId != -1 && shortcut.id > 0) {
            val result = databaseHelper.deleteShortcutById(shortcut.id)

            if (result > 0) {
                Toast.makeText(this, "Shortcut removed", Toast.LENGTH_SHORT).show()
                updateShortcutText()
            } else {
                Toast.makeText(this, "Failed to remove shortcut", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingShortcutKeywords.remove(shortcut.shortcutKeyword)
            Toast.makeText(this, "Shortcut removed", Toast.LENGTH_SHORT).show()
            updateShortcutText()
        }
    }

    private fun disableShortcutBecauseNoteLocked() {
        if (isEditMode && noteId != -1) {
            databaseHelper.deleteAllShortcutsForNote(noteId)

            databaseHelper.updateNoteShortcut(
                noteId = noteId,
                shortcutEnabled = 0,
                shortcutKeyword = ""
            )
        } else {
            pendingShortcutKeywords.clear()
        }

        shortcutEnabled = 0
        shortcutKeyword = ""

        updateShortcutText()
    }

    private fun updateShortcutText() {
        val shortcutKeywords = if (isEditMode && noteId != -1) {
            databaseHelper.getShortcutsForNote(noteId).map { it.shortcutKeyword }
        } else {
            pendingShortcutKeywords
        }

        if (shortcutKeywords.isNotEmpty()) {
            btnTextShortcut.contentDescription = "Text shortcuts enabled"
            tvShortcutStatus.text = "Shortcuts: ${shortcutKeywords.joinToString(", ")}"
        } else {
            btnTextShortcut.contentDescription = "Text shortcut"
            tvShortcutStatus.text = "Shortcut: Off"
        }

        if (shouldShowShortcutStatusTemporarily) {
            shouldShowShortcutStatusTemporarily = false
            showStatusForSevenSeconds(tvShortcutStatus)
        } else {
            tvShortcutStatus.visibility = View.GONE
        }
    }

    private fun normalizeShortcutKeyword(rawKeyword: String): String {
        val cleanedKeyword = rawKeyword.trim()

        return if (cleanedKeyword.startsWith("/")) {
            cleanedKeyword
        } else {
            "/$cleanedKeyword"
        }
    }

    private fun savePendingShortcutsToNewNote(newNoteId: Int) {
        if (isLocked == 1) {
            pendingShortcutKeywords.clear()
            return
        }

        for (keyword in pendingShortcutKeywords) {
            databaseHelper.addShortcutToNote(
                noteId = newNoteId,
                keyword = keyword
            )
        }

        pendingShortcutKeywords.clear()
    }

    // ---------------------------
    // CATEGORY / FOLDER FUNCTIONS
    // ---------------------------

    private fun showCategoryPickerDialog() {
        val categories = databaseHelper.getAllCategories()

        if (categories.isEmpty()) {
            showAddCategoryDialog()
            return
        }

        val categoryNames = categories.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(categories.size)

        for (i in categories.indices) {
            checkedItems[i] = selectedCategoryIds.contains(categories[i].id)
        }

        AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setMultiChoiceItems(categoryNames, checkedItems) { _, which, isChecked ->
                val categoryId = categories[which].id

                if (isChecked) {
                    if (!selectedCategoryIds.contains(categoryId)) {
                        selectedCategoryIds.add(categoryId)
                    }
                } else {
                    selectedCategoryIds.remove(categoryId)
                }
            }
            .setPositiveButton("Done") { _, _ ->
                updateSelectedCategoriesText()
            }
            .setNeutralButton("Add New") { _, _ ->
                showAddCategoryDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Category name"
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setMessage("Enter category name")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = input.text.toString().trim()

                if (categoryName.isEmpty()) {
                    Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.addCategory(categoryName)

                if (result != -1L) {
                    selectedCategoryIds.add(result.toInt())
                    updateSelectedCategoriesText()
                    Toast.makeText(this, "Category added and selected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                    showCategoryPickerDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFolderPickerDialog() {
        val folders = databaseHelper.getAllFolders()

        val folderNames = ArrayList<String>()
        val folderIds = ArrayList<Int>()

        folderNames.add("No Folder")
        folderIds.add(0)

        for (folder in folders) {
            folderNames.add(folder.name)
            folderIds.add(folder.id)
        }

        folderNames.add("+ Add New Folder")
        folderIds.add(-999)

        val selectedIndex = folderIds.indexOf(selectedFolderId).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("Choose Folder")
            .setSingleChoiceItems(folderNames.toTypedArray(), selectedIndex) { dialog, which ->
                val chosenFolderId = folderIds[which]

                if (chosenFolderId == -999) {
                    dialog.dismiss()
                    showAddFolderDialog()
                } else {
                    selectedFolderId = chosenFolderId
                    updateSelectedFolderText()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddFolderDialog() {
        val input = EditText(this)
        input.hint = "Folder name"
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("New Folder")
            .setMessage("Enter folder name")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val folderName = input.text.toString().trim()

                if (folderName.isEmpty()) {
                    Toast.makeText(this, "Folder name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.addFolder(folderName)

                if (result != -1L) {
                    selectedFolderId = result.toInt()
                    updateSelectedFolderText()
                    Toast.makeText(this, "Folder added and selected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show()
                    showFolderPickerDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSelectedCategoriesText() {
        categoryChipContainer.removeAllViews()

        val allCategories = databaseHelper.getAllCategories()

        if (selectedCategoryIds.isEmpty()) {
            val emptyTextView = TextView(this)
            emptyTextView.text = "No categories selected"
            emptyTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            emptyTextView.textSize = 14f

            categoryChipContainer.addView(emptyTextView)
            return
        }

        for (category in allCategories) {
            if (selectedCategoryIds.contains(category.id)) {
                addCategoryChip(
                    categoryId = category.id,
                    categoryName = category.name
                )
            }
        }

        if (categoryChipContainer.childCount == 0) {
            val emptyTextView = TextView(this)
            emptyTextView.text = "No categories selected"
            emptyTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            emptyTextView.textSize = 14f

            categoryChipContainer.addView(emptyTextView)
        }
    }

    private fun addCategoryChip(
        categoryId: Int,
        categoryName: String
    ) {
        val chipLayout = LinearLayout(this)
        chipLayout.orientation = LinearLayout.HORIZONTAL
        chipLayout.gravity = android.view.Gravity.CENTER_VERTICAL
        chipLayout.setBackgroundResource(R.drawable.bg_category_chip)
        chipLayout.isClickable = true
        chipLayout.isFocusable = true

        val chipParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        chipParams.marginEnd = 10
        chipLayout.layoutParams = chipParams

        val categoryText = TextView(this)
        categoryText.text = categoryName
        categoryText.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        categoryText.textSize = 14f
        categoryText.maxLines = 1
        categoryText.ellipsize = android.text.TextUtils.TruncateAt.END

        val closeButton = ImageButton(this)
        closeButton.setImageResource(R.drawable.ic_close_small)
        closeButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        closeButton.contentDescription = "Remove category"
        closeButton.setPadding(8, 8, 8, 8)

        val closeParams = LinearLayout.LayoutParams(
            36,
            36
        )

        closeParams.marginStart = 6
        closeButton.layoutParams = closeParams

        closeButton.setOnClickListener {
            selectedCategoryIds.remove(categoryId)
            updateSelectedCategoriesText()
        }

        chipLayout.setOnClickListener {
            selectedCategoryIds.remove(categoryId)
            updateSelectedCategoriesText()
        }

        chipLayout.addView(categoryText)
        chipLayout.addView(closeButton)

        categoryChipContainer.addView(chipLayout)
    }

    private fun updateSelectedFolderText() {
        if (selectedFolderId == 0) {
            tvSelectedFolder.text = "No folder selected"
            return
        }

        val folders = databaseHelper.getAllFolders()
        val selectedFolder = folders.find { it.id == selectedFolderId }

        tvSelectedFolder.text = if (selectedFolder != null) {
            "Folder: ${selectedFolder.name}"
        } else {
            "No folder selected"
        }
    }

    // ---------------------------
// BULLET POINT FUNCTIONS
// ---------------------------

    private fun setupBulletDragAndDrop() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = source.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                    return false
                }

                noteBulletAdapter.moveItem(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                // No swipe action
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                if (::noteBulletAdapter.isInitialized) {
                    noteBulletAdapter.finishMove()
                }
            }
        }

        bulletItemTouchHelper = ItemTouchHelper(callback)
        bulletItemTouchHelper.attachToRecyclerView(recyclerViewNoteBullets)
    }

    private fun saveBulletOrderAfterDrag(reorderedBullets: ArrayList<NoteBullet>) {
        val finalOrderedBullets = buildFinalBulletOrderAfterDrag(reorderedBullets)

        if (!isEditMode || noteId == -1) {
            pendingBullets.clear()

            for (i in finalOrderedBullets.indices) {
                pendingBullets.add(
                    finalOrderedBullets[i].copy(
                        bulletOrder = i
                    )
                )
            }

            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateBulletOrders(finalOrderedBullets)

        if (result) {
            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to save bullet order", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
        }
    }

    private fun loadBulletDisplaySettings() {
        val prefs = getSharedPreferences(
            "bullet_display_preferences",
            Context.MODE_PRIVATE
        )

        autoMoveCompletedBullets = prefs.getBoolean(
            "auto_move_completed_bullets",
            false
        )

        hideCompletedBullets = prefs.getBoolean(
            "hide_completed_bullets",
            false
        )
    }

    private fun saveBulletDisplaySettings() {
        val prefs = getSharedPreferences(
            "bullet_display_preferences",
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putBoolean("auto_move_completed_bullets", autoMoveCompletedBullets)
            .putBoolean("hide_completed_bullets", hideCompletedBullets)
            .apply()
    }

    private fun showBulletDisplayOptionsDialog() {
        val options = arrayOf(
            "View Options",
            if (autoMoveCompletedBullets) "✓ Auto-move completed to bottom" else "Auto-move completed to bottom",
            if (hideCompletedBullets) "✓ Hide completed bullets" else "Hide completed bullets",
            "Change All Bullet Types",
            "Text only",
            "• Make all Dot bullets",
            "- Make all Dash bullets",
            "1. Make all Number bullets",
            "Task Checkbox Options",
            "☑ Show checkbox for all bullets",
            "☐ Hide checkbox for all bullets"
        )

        AlertDialog.Builder(this)
            .setTitle("Bullet Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Section title only
                    }

                    1 -> {
                        autoMoveCompletedBullets = !autoMoveCompletedBullets
                        saveBulletDisplaySettings()
                        loadNoteBullets()

                        Toast.makeText(
                            this,
                            if (autoMoveCompletedBullets) {
                                "Auto-move completed enabled"
                            } else {
                                "Auto-move completed disabled"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    2 -> {
                        hideCompletedBullets = !hideCompletedBullets
                        saveBulletDisplaySettings()
                        loadNoteBullets()

                        Toast.makeText(
                            this,
                            if (hideCompletedBullets) {
                                "Hide completed bullets enabled"
                            } else {
                                "Hide completed bullets disabled"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    3 -> {
                        // Section title only
                    }

                    4 -> {
                        confirmChangeAllBulletTypes(DatabaseHelper.BULLET_TYPE_NONE)
                    }

                    5 -> {
                        confirmChangeAllBulletTypes(DatabaseHelper.BULLET_TYPE_DOT)
                    }

                    6 -> {
                        confirmChangeAllBulletTypes(DatabaseHelper.BULLET_TYPE_DASH)
                    }

                    7 -> {
                        confirmChangeAllBulletTypes(DatabaseHelper.BULLET_TYPE_NUMBER)
                    }

                    8 -> {
                        // Section title only
                    }

                    9 -> {
                        confirmChangeAllBulletTaskStates(1)
                    }

                    10 -> {
                        confirmChangeAllBulletTaskStates(0)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmChangeAllBulletTypes(newType: String) {
        if (allNoteBulletList.isEmpty()) {
            Toast.makeText(this, "No bullet points to change", Toast.LENGTH_SHORT).show()
            return
        }

        val typeName = getBulletTypeDisplayName(newType)

        AlertDialog.Builder(this)
            .setTitle("Change All Bullet Types")
            .setMessage("Make all bullet points $typeName?")
            .setPositiveButton("Change All") { _, _ ->
                changeAllBulletTypes(newType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeAllBulletTypes(newType: String) {
        if (!isEditMode || noteId == -1) {
            for (i in pendingBullets.indices) {
                pendingBullets[i] = pendingBullets[i].copy(
                    bulletType = newType
                )
            }

            Toast.makeText(
                this,
                "All bullet types changed to ${getBulletTypeDisplayName(newType)}",
                Toast.LENGTH_SHORT
            ).show()

            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateAllBulletTypesForNote(
            noteId = noteId,
            newType = newType
        )

        if (result > 0) {
            Toast.makeText(
                this,
                "All bullet types changed to ${getBulletTypeDisplayName(newType)}",
                Toast.LENGTH_SHORT
            ).show()

            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to change bullet types", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmChangeAllBulletTaskStates(newTaskState: Int) {
        if (allNoteBulletList.isEmpty()) {
            Toast.makeText(this, "No bullet points to change", Toast.LENGTH_SHORT).show()
            return
        }

        val message = if (newTaskState == 1) {
            "Show checkbox for all bullet points?"
        } else {
            "Hide checkbox for all bullet points?\n\nCompleted status will be cleared."
        }

        AlertDialog.Builder(this)
            .setTitle("Change Checkbox Option")
            .setMessage(message)
            .setPositiveButton("Change All") { _, _ ->
                changeAllBulletTaskStates(newTaskState)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeAllBulletTaskStates(newTaskState: Int) {
        if (!isEditMode || noteId == -1) {
            for (i in pendingBullets.indices) {
                pendingBullets[i] = pendingBullets[i].copy(
                    isTask = newTaskState,
                    isCompleted = if (newTaskState == 1) pendingBullets[i].isCompleted else 0
                )
            }

            Toast.makeText(
                this,
                if (newTaskState == 1) {
                    "Checkbox shown for all bullets"
                } else {
                    "Checkbox hidden for all bullets"
                },
                Toast.LENGTH_SHORT
            ).show()

            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateAllBulletTaskStatesForNote(
            noteId = noteId,
            isTask = newTaskState
        )

        if (result > 0) {
            Toast.makeText(
                this,
                if (newTaskState == 1) {
                    "Checkbox shown for all bullets"
                } else {
                    "Checkbox hidden for all bullets"
                },
                Toast.LENGTH_SHORT
            ).show()

            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to update checkbox options", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBulletTypeDisplayName(bulletType: String): String {
        return when (bulletType) {
            DatabaseHelper.BULLET_TYPE_NONE -> "Text only"
            DatabaseHelper.BULLET_TYPE_DASH -> "Dash"
            DatabaseHelper.BULLET_TYPE_NUMBER -> "Number"
            else -> "Dot"
        }
    }

    private fun updateBulletOptionsButtonText() {
        btnBulletOptions.text = when {
            autoMoveCompletedBullets && hideCompletedBullets -> {
                "Options ✓✓"
            }

            autoMoveCompletedBullets || hideCompletedBullets -> {
                "Options ✓"
            }

            else -> {
                "Options"
            }
        }
    }

    private fun buildDisplayedBulletList(
        sourceList: ArrayList<NoteBullet>
    ): ArrayList<NoteBullet> {
        val sortedList = if (autoMoveCompletedBullets) {
            sourceList.sortedWith(
                compareBy<NoteBullet> { it.isCompleted }
                    .thenBy { it.bulletOrder }
                    .thenBy { it.id }
            )
        } else {
            sourceList.sortedWith(
                compareBy<NoteBullet> { it.bulletOrder }
                    .thenBy { it.id }
            )
        }

        val displayList = if (hideCompletedBullets) {
            sortedList.filter {
                !(it.isTask == 1 && it.isCompleted == 1)
            }
        } else {
            sortedList
        }
        return ArrayList(displayList)
    }

    private fun buildFinalBulletOrderAfterDrag(
        reorderedBullets: ArrayList<NoteBullet>
    ): ArrayList<NoteBullet> {
        val reorderedIds = reorderedBullets.map {
            it.id
        }.toSet()

        val hiddenBullets = allNoteBulletList
            .filter {
                !reorderedIds.contains(it.id)
            }
            .sortedWith(
                compareBy<NoteBullet> { it.bulletOrder }
                    .thenBy { it.id }
            )

        val combinedList = ArrayList<NoteBullet>()

        combinedList.addAll(reorderedBullets)
        combinedList.addAll(hiddenBullets)

        val groupedList = if (autoMoveCompletedBullets || hideCompletedBullets) {
            val pendingList = combinedList.filter {
                !(it.isTask == 1 && it.isCompleted == 1)
            }

            val completedList = combinedList.filter {
                it.isTask == 1 && it.isCompleted == 1
            }

            pendingList + completedList
        } else {
            combinedList
        }

        val finalOrderedList = ArrayList<NoteBullet>()

        for (i in groupedList.indices) {
            finalOrderedList.add(
                groupedList[i].copy(
                    bulletOrder = i
                )
            )
        }

        return finalOrderedList
    }

    private fun toggleBulletCompleted(noteBullet: NoteBullet) {
        if (noteBullet.isTask != 1) {
            Toast.makeText(this, "This bullet has no checkbox", Toast.LENGTH_SHORT).show()
            return
        }

        val newCompletedValue = if (noteBullet.isCompleted == 1) 0 else 1

        if (noteBullet.id < 0) {
            val index = pendingBullets.indexOfFirst {
                it.id == noteBullet.id
            }

            if (index == -1) {
                Toast.makeText(this, "Bullet point not found", Toast.LENGTH_SHORT).show()
                return
            }

            pendingBullets[index] = pendingBullets[index].copy(
                isCompleted = newCompletedValue
            )

            if (autoMoveCompletedBullets || hideCompletedBullets) {
                val normalizedList = buildFinalBulletOrderAfterDrag(
                    buildDisplayedBulletList(pendingBullets)
                )

                pendingBullets.clear()

                for (i in normalizedList.indices) {
                    pendingBullets.add(
                        normalizedList[i].copy(
                            bulletOrder = i
                        )
                    )
                }
            }

            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateBulletCompleted(
            bulletId = noteBullet.id,
            isCompleted = newCompletedValue
        )

        if (result > 0) {
            if (autoMoveCompletedBullets || hideCompletedBullets) {
                val latestBullets = databaseHelper.getBulletsForNote(noteId)
                allNoteBulletList = latestBullets

                val normalizedList = buildFinalBulletOrderAfterDrag(
                    buildDisplayedBulletList(latestBullets)
                )

                databaseHelper.updateBulletOrders(normalizedList)
            }

            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to update task status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddBulletDialog() {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(40, 20, 40, 10)

        val bulletInput = EditText(this)
        bulletInput.hint = "Enter bullet point"
        bulletInput.setSingleLine(false)
        bulletInput.minLines = 2
        bulletInput.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        bulletInput.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        val typeTitle = TextView(this)
        typeTitle.text = "Bullet type"
        typeTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        typeTitle.textSize = 15f
        typeTitle.setPadding(0, 20, 0, 8)

        val textOnlyOption = TextView(this)
        textOnlyOption.text = "Text only"
        textOnlyOption.textSize = 16f
        textOnlyOption.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        textOnlyOption.setPadding(0, 12, 0, 12)

        val dotOption = TextView(this)
        dotOption.text = "• Dot bullet"
        dotOption.textSize = 16f
        dotOption.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        dotOption.setPadding(0, 12, 0, 12)

        val dashOption = TextView(this)
        dashOption.text = "- Dash bullet"
        dashOption.textSize = 16f
        dashOption.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dashOption.setPadding(0, 12, 0, 12)

        val numberOption = TextView(this)
        numberOption.text = "1. Number bullet"
        numberOption.textSize = 16f
        numberOption.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        numberOption.setPadding(0, 12, 0, 12)

        var selectedBulletType = DatabaseHelper.BULLET_TYPE_DOT

        var selectedIsTask = 1

        val taskOption = TextView(this)
        taskOption.text = "☑ Show checkbox"
        taskOption.textSize = 16f
        taskOption.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        taskOption.setPadding(0, 18, 0, 12)

        fun updateSelectedTypeColors() {
            textOnlyOption.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selectedBulletType == DatabaseHelper.BULLET_TYPE_NONE) R.color.primary_blue else R.color.text_primary
                )
            )

            dotOption.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selectedBulletType == DatabaseHelper.BULLET_TYPE_DOT) R.color.primary_blue else R.color.text_primary
                )
            )

            dashOption.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selectedBulletType == DatabaseHelper.BULLET_TYPE_DASH) R.color.primary_blue else R.color.text_primary
                )
            )

            numberOption.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selectedBulletType == DatabaseHelper.BULLET_TYPE_NUMBER) R.color.primary_blue else R.color.text_primary
                )
            )

            taskOption.text = if (selectedIsTask == 1) {
                "☑ Show checkbox"
            } else {
                "☐ Show checkbox"
            }

            taskOption.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selectedIsTask == 1) R.color.primary_blue else R.color.text_primary
                )
            )
        }

        textOnlyOption.setOnClickListener {
            selectedBulletType = DatabaseHelper.BULLET_TYPE_NONE
            updateSelectedTypeColors()
        }

        dotOption.setOnClickListener {
            selectedBulletType = DatabaseHelper.BULLET_TYPE_DOT
            updateSelectedTypeColors()
        }

        dashOption.setOnClickListener {
            selectedBulletType = DatabaseHelper.BULLET_TYPE_DASH
            updateSelectedTypeColors()
        }

        numberOption.setOnClickListener {
            selectedBulletType = DatabaseHelper.BULLET_TYPE_NUMBER
            updateSelectedTypeColors()
        }

        taskOption.setOnClickListener {
            selectedIsTask = if (selectedIsTask == 1) 0 else 1
            updateSelectedTypeColors()
        }


        container.addView(bulletInput)
        container.addView(typeTitle)

        container.addView(textOnlyOption)
        container.addView(dotOption)
        container.addView(dashOption)
        container.addView(numberOption)
        container.addView(taskOption)

        AlertDialog.Builder(this)
            .setTitle("Add Bullet Point")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val bulletText = bulletInput.text.toString().trim()

                if (bulletText.isEmpty()) {
                    Toast.makeText(this, "Bullet point cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addBulletPoint(
                    bulletText = bulletText,
                    bulletType = selectedBulletType,
                    isTask = selectedIsTask
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addBulletPoint(
        bulletText: String,
        bulletType: String,
        isTask: Int
    ) {
        val nextOrder = if (isEditMode && noteId != -1) {
            databaseHelper.getBulletsForNote(noteId).size
        } else {
            pendingBullets.size
        }

        if (isEditMode && noteId != -1) {
            val result = databaseHelper.addBulletToNote(
                noteId = noteId,
                bulletText = bulletText,
                bulletType = bulletType,
                bulletOrder = nextOrder,
                isTask = isTask,
                isCompleted = 0
            )

            if (result == -1L) {
                Toast.makeText(this, "Failed to add bullet point", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            pendingBullets.add(
                NoteBullet(
                    id = -1 - pendingBullets.size,
                    noteId = -1,
                    bulletText = bulletText,
                    bulletType = bulletType,
                    bulletOrder = nextOrder,
                    isTask = isTask,
                    isCompleted = 0,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        Toast.makeText(this, "Bullet point added", Toast.LENGTH_SHORT).show()
        loadNoteBullets()
    }

    private fun loadNoteBullets() {
        allNoteBulletList = if (isEditMode && noteId != -1) {
            databaseHelper.getBulletsForNote(noteId)
        } else {
            ArrayList()
        }

        if (!isEditMode || noteId == -1) {
            allNoteBulletList.addAll(pendingBullets)
        }

        noteBulletList = buildDisplayedBulletList(allNoteBulletList)

        if (!showBulletSection) {
            bulletTitleOptionsSection.visibility = View.GONE
            btnAddBulletPoint.visibility = View.GONE
            recyclerViewNoteBullets.visibility = View.GONE
            tvEmptyBullets.visibility = View.GONE
        } else if (allNoteBulletList.isEmpty()) {
            recyclerViewNoteBullets.visibility = View.GONE
            tvEmptyBullets.visibility = View.VISIBLE
            tvEmptyBullets.text = "No bullet points added."
        } else if (noteBulletList.isEmpty() && hideCompletedBullets) {
            recyclerViewNoteBullets.visibility = View.GONE
            tvEmptyBullets.visibility = View.VISIBLE
            tvEmptyBullets.text = "All completed bullets are hidden."
        } else {
            recyclerViewNoteBullets.visibility = View.VISIBLE
            tvEmptyBullets.visibility = View.GONE
            tvEmptyBullets.text = "No bullet points added."
        }

        updateBulletProgressText()
        updateBulletOptionsButtonText()

        noteBulletAdapter = NoteBulletAdapter(
            bulletList = noteBulletList,
            onBulletCompletedClick = { selectedBullet ->
                toggleBulletCompleted(selectedBullet)
            },
            onBulletTypeClick = { selectedBullet ->
                showChangeBulletTypeDialog(selectedBullet)
            },
            onBulletTextClick = { selectedBullet ->
                showEditBulletTextDialog(selectedBullet)
            },
            onDeleteBulletClick = { selectedBullet ->
                confirmDeleteBullet(selectedBullet)
            },
            onStartDrag = { viewHolder ->
                bulletItemTouchHelper.startDrag(viewHolder)
            },
            onBulletOrderChanged = { reorderedBullets ->
                saveBulletOrderAfterDrag(reorderedBullets)
            }
        )

        recyclerViewNoteBullets.adapter = noteBulletAdapter
    }

    private fun updateBulletProgressText() {
        val taskBulletCount = allNoteBulletList.count {
            it.isTask == 1
        }

        val completedTaskCount = allNoteBulletList.count {
            it.isTask == 1 && it.isCompleted == 1
        }

        tvBulletPointsTitle.text = when {
            allNoteBulletList.isEmpty() -> {
                "Bullet Points"
            }

            taskBulletCount == 0 -> {
                "Bullet Points"
            }

            completedTaskCount == taskBulletCount -> {
                "Bullet Points • All completed ($completedTaskCount/$taskBulletCount)"
            }

            else -> {
                "Bullet Points • $completedTaskCount/$taskBulletCount completed"
            }
        }
    }

    private fun confirmDeleteBullet(noteBullet: NoteBullet) {
        AlertDialog.Builder(this)
            .setTitle("Delete Bullet Point")
            .setMessage("Remove this bullet point?")
            .setPositiveButton("Delete") { _, _ ->
                deleteBullet(noteBullet)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBullet(noteBullet: NoteBullet) {
        if (noteBullet.id < 0) {
            pendingBullets.remove(noteBullet)

            for (i in pendingBullets.indices) {
                pendingBullets[i] = pendingBullets[i].copy(
                    bulletOrder = i
                )
            }

            Toast.makeText(this, "Bullet point removed", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
            return
        }

        val result = databaseHelper.deleteBulletById(noteBullet.id)

        if (result > 0) {
            val remainingBullets = databaseHelper.getBulletsForNote(noteId)
            databaseHelper.updateBulletOrders(remainingBullets)

            Toast.makeText(this, "Bullet point removed", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to remove bullet point", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditBulletTextDialog(noteBullet: NoteBullet) {
        val input = EditText(this)
        input.hint = "Edit bullet point"
        input.setSingleLine(false)
        input.minLines = 2
        input.setText(noteBullet.bulletText)
        input.setSelection(input.text.length)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("Edit Bullet Point")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newText = input.text.toString().trim()

                if (newText.isEmpty()) {
                    Toast.makeText(this, "Bullet point cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateBulletText(
                    noteBullet = noteBullet,
                    newText = newText
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBulletText(
        noteBullet: NoteBullet,
        newText: String
    ) {
        if (noteBullet.id < 0) {
            val index = pendingBullets.indexOfFirst {
                it.id == noteBullet.id
            }

            if (index == -1) {
                Toast.makeText(this, "Bullet point not found", Toast.LENGTH_SHORT).show()
                return
            }

            pendingBullets[index] = pendingBullets[index].copy(
                bulletText = newText
            )

            Toast.makeText(this, "Bullet point updated", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateBulletText(
            bulletId = noteBullet.id,
            newText = newText
        )

        if (result > 0) {
            Toast.makeText(this, "Bullet point updated", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to update bullet point", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangeBulletTypeDialog(noteBullet: NoteBullet) {
        val taskText = if (noteBullet.isTask == 1) {
            "✓ Show checkbox"
        } else {
            "Show checkbox"
        }

        val bulletOptions = arrayOf(
            "Text only",
            "• Dot bullet",
            "- Dash bullet",
            "1. Number bullet",
            taskText
        )

        AlertDialog.Builder(this)
            .setTitle("Bullet Options")
            .setItems(bulletOptions) { _, which ->
                when (which) {
                    0 -> {
                        updateBulletType(
                            noteBullet = noteBullet,
                            newType = DatabaseHelper.BULLET_TYPE_NONE
                        )
                    }

                    1 -> {
                        updateBulletType(
                            noteBullet = noteBullet,
                            newType = DatabaseHelper.BULLET_TYPE_DOT
                        )
                    }

                    2 -> {
                        updateBulletType(
                            noteBullet = noteBullet,
                            newType = DatabaseHelper.BULLET_TYPE_DASH
                        )
                    }

                    3 -> {
                        updateBulletType(
                            noteBullet = noteBullet,
                            newType = DatabaseHelper.BULLET_TYPE_NUMBER
                        )
                    }

                    4 -> {
                        toggleBulletTaskState(noteBullet)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBulletType(
        noteBullet: NoteBullet,
        newType: String
    ) {
        if (noteBullet.bulletType == newType) {
            return
        }

        if (noteBullet.id < 0) {
            val index = pendingBullets.indexOfFirst {
                it.id == noteBullet.id
            }

            if (index == -1) {
                Toast.makeText(this, "Bullet point not found", Toast.LENGTH_SHORT).show()
                return
            }

            pendingBullets[index] = pendingBullets[index].copy(
                bulletType = newType
            )

            Toast.makeText(this, "Bullet type updated", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateBulletType(
            bulletId = noteBullet.id,
            newType = newType
        )

        if (result > 0) {
            Toast.makeText(this, "Bullet type updated", Toast.LENGTH_SHORT).show()
            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to update bullet type", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBulletTaskState(noteBullet: NoteBullet) {
        val newTaskState = if (noteBullet.isTask == 1) 0 else 1

        if (noteBullet.id < 0) {
            val index = pendingBullets.indexOfFirst {
                it.id == noteBullet.id
            }

            if (index == -1) {
                Toast.makeText(this, "Bullet point not found", Toast.LENGTH_SHORT).show()
                return
            }

            pendingBullets[index] = pendingBullets[index].copy(
                isTask = newTaskState,
                isCompleted = if (newTaskState == 1) pendingBullets[index].isCompleted else 0
            )

            Toast.makeText(
                this,
                if (newTaskState == 1) "Checkbox shown" else "Checkbox hidden",
                Toast.LENGTH_SHORT
            ).show()

            loadNoteBullets()
            return
        }

        val result = databaseHelper.updateBulletTaskState(
            bulletId = noteBullet.id,
            isTask = newTaskState
        )

        if (result > 0) {
            Toast.makeText(
                this,
                if (newTaskState == 1) "Checkbox shown" else "Checkbox hidden",
                Toast.LENGTH_SHORT
            ).show()

            loadNoteBullets()
        } else {
            Toast.makeText(this, "Failed to update checkbox option", Toast.LENGTH_SHORT).show()
        }
    }


    private fun savePendingBulletsToNewNote(newNoteId: Int) {
        for (i in pendingBullets.indices) {
            val bullet = pendingBullets[i]

            databaseHelper.addBulletToNote(
                noteId = newNoteId,
                bulletText = bullet.bulletText,
                bulletType = bullet.bulletType,
                bulletOrder = i,
                isTask = bullet.isTask,
                isCompleted = bullet.isCompleted
            )
        }

        pendingBullets.clear()
    }

    private fun hasAnyBulletPoints(): Boolean {
        return if (isEditMode && noteId != -1) {
            databaseHelper.getBulletsForNote(noteId).isNotEmpty()
        } else {
            pendingBullets.isNotEmpty()
        }
    }

    // ---------------------------
    // IMAGE FUNCTIONS
    // ---------------------------

    private fun showAddImageOptions() {
        val options = arrayOf(
            "Choose from Gallery",
            "Take Photo"
        )

        AlertDialog.Builder(this)
            .setTitle("Add Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGalleryPicker()
                    1 -> checkCameraPermissionAndOpen()
                }
            }
            .show()
    }

    private fun openGalleryPicker() {
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun saveGalleryImagesToNote(selectedUris: List<Uri>) {
        var successCount = 0

        for (uri in selectedUris) {
            val copiedImageUri = copyImageToAppStorage(uri)

            if (copiedImageUri != null) {
                val imageUriString = copiedImageUri.toString()

                if (isEditMode && noteId != -1) {
                    val result = databaseHelper.addImageToNote(
                        noteId = noteId,
                        imageUri = imageUriString
                    )

                    if (result != -1L) {
                        successCount++

                        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
                            addImageBubbleToChat(imageUriString)
                        }
                    }
                } else {
                    pendingImageUris.add(imageUriString)
                    successCount++

                    if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
                        addImageBubbleToChat(imageUriString)
                    }
                }
            }
        }

        if (successCount > 0) {
            Toast.makeText(this, "$successCount image(s) added", Toast.LENGTH_SHORT).show()
            loadNoteImages()
            updateAttachmentButtonText()
        } else {
            Toast.makeText(this, "Failed to add images", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        val permissionResult = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val imageDirectory = File(filesDir, "note_images")

        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs()
        }

        currentCameraImageFile = File(
            imageDirectory,
            "camera_${System.currentTimeMillis()}.jpg"
        )

        val imageFile = currentCameraImageFile

        if (imageFile == null) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
            return
        }

        currentCameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )

        val cameraUri = currentCameraImageUri

        if (cameraUri == null) {
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show()
            return
        }

        takePictureLauncher.launch(cameraUri)
    }

    private fun saveCameraImageToNote() {
        val imageFile = currentCameraImageFile

        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "Camera image not found", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUri = Uri.fromFile(imageFile)
        val imageUriString = imageUri.toString()

        if (isEditMode && noteId != -1) {
            val result = databaseHelper.addImageToNote(
                noteId = noteId,
                imageUri = imageUriString
            )

            if (result != -1L) {
                if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
                    addImageBubbleToChat(imageUriString)
                }

                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show()
                loadNoteImages()
                updateAttachmentButtonText()
            } else {
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingImageUris.add(imageUriString)

            if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
                addImageBubbleToChat(imageUriString)
            }

            Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show()
            loadNoteImages()
            updateAttachmentButtonText()
        }
    }

    private fun copyImageToAppStorage(sourceUri: Uri): Uri? {
        return try {
            val imageDirectory = File(filesDir, "note_images")

            if (!imageDirectory.exists()) {
                imageDirectory.mkdirs()
            }

            val imageFile = File(
                imageDirectory,
                "gallery_${UUID.randomUUID()}.jpg"
            )

            contentResolver.openInputStream(sourceUri).use { inputStream ->
                FileOutputStream(imageFile).use { outputStream ->
                    if (inputStream == null) {
                        return null
                    }

                    inputStream.copyTo(outputStream)
                }
            }

            Uri.fromFile(imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadNoteImages() {
        noteImageList = if (isEditMode && noteId != -1) {
            databaseHelper.getImagesForNote(noteId)
        } else {
            ArrayList()
        }

        if (!isEditMode || noteId == -1) {
            for (i in pendingImageUris.indices) {
                noteImageList.add(
                    NoteImage(
                        id = -1 - i,
                        noteId = -1,
                        imageUri = pendingImageUris[i],
                        createdDate = System.currentTimeMillis()
                    )
                )
            }
        }

        updateImageSectionVisibility()

        noteImageAdapter = NoteImageAdapter(
            noteImageList,
            showDeleteButton = true,
            onImageClick = { _, selectedPosition ->
                openImageViewer(selectedPosition)
            },
            onDeleteImageClick = { selectedImage ->
                confirmDeleteNoteImage(selectedImage)
            }
        )

        recyclerViewNoteImages.adapter = noteImageAdapter
        updateAttachmentButtonText()
    }

    private fun openImageViewer(selectedPosition: Int) {
        if (!isEditMode || noteId == -1) {
            Toast.makeText(
                this,
                "Save the note first to view images full screen",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(this, NoteImageViewerActivity::class.java)
        intent.putExtra("note_id", noteId)
        intent.putExtra("selected_position", selectedPosition)
        startActivity(intent)
    }

    private fun confirmDeleteNoteImage(noteImage: NoteImage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Remove this image from the note?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNoteImage(noteImage)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNoteImage(noteImage: NoteImage) {
        if (noteImage.id < 0) {
            pendingImageUris.remove(noteImage.imageUri)

            pendingNoteMessages.removeAll {
                it.messageType == DatabaseHelper.MESSAGE_TYPE_IMAGE &&
                        it.imageUri == noteImage.imageUri
            }

            deleteLocalImageFile(noteImage.imageUri)

            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
            loadNoteImages()
            loadNoteMessages()
            updateAttachmentButtonText()
            return
        }

        val result = databaseHelper.deleteImageFromNote(noteImage.id)

        if (result > 0) {
            databaseHelper.deleteImageMessagesByUri(noteId, noteImage.imageUri)

            deleteLocalImageFile(noteImage.imageUri)

            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
            loadNoteImages()
            loadNoteMessages()
            updateAttachmentButtonText()
        } else {
            Toast.makeText(this, "Failed to remove image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLocalImageFile(imageUriString: String) {
        try {
            val uri = Uri.parse(imageUriString)

            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")

                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun savePendingImagesToNewNote(newNoteId: Int) {
        for (imageUri in pendingImageUris) {
            databaseHelper.addImageToNote(
                noteId = newNoteId,
                imageUri = imageUri
            )
        }

        pendingImageUris.clear()
    }

    // ---------------------------
    // FILE FUNCTIONS
    // ---------------------------

    private fun openFilePicker() {
        if (isLocked == 1) {
            Toast.makeText(
                this,
                "Locked notes cannot add files for privacy.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        filePickerLauncher.launch(arrayOf("*/*"))
    }

    private fun saveSelectedFilesToNote(selectedUris: List<Uri>) {
        var successCount = 0

        for (uri in selectedUris) {
            val fileName = getFileNameFromUri(uri)
            val copiedFileUri = copyFileToAppStorage(uri, fileName)

            if (copiedFileUri != null) {
                val fileUriString = copiedFileUri.toString()

                if (isEditMode && noteId != -1) {
                    val result = databaseHelper.addFileToNote(
                        noteId = noteId,
                        fileName = fileName,
                        fileUri = fileUriString
                    )

                    if (result != -1L) {
                        successCount++

                        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
                            addFileBubbleToChat(
                                fileName = fileName,
                                fileUri = fileUriString
                            )
                        }
                    }
                } else {
                    pendingFiles.add(
                        NoteFile(
                            id = -1 - pendingFiles.size,
                            noteId = -1,
                            fileName = fileName,
                            fileUri = fileUriString,
                            createdDate = System.currentTimeMillis()
                        )
                    )

                    successCount++

                    if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
                        addFileBubbleToChat(
                            fileName = fileName,
                            fileUri = fileUriString
                        )
                    }
                }
            }
        }

        if (successCount > 0) {
            Toast.makeText(this, "$successCount file(s) added", Toast.LENGTH_SHORT).show()
            loadNoteFiles()
            updateAttachmentButtonText()
        } else {
            Toast.makeText(this, "Failed to add files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "file_${System.currentTimeMillis()}"

        try {
            val cursor: Cursor? = contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                    if (nameIndex != -1) {
                        val foundName = it.getString(nameIndex)

                        if (!foundName.isNullOrBlank()) {
                            fileName = foundName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fileName
    }

    private fun copyFileToAppStorage(sourceUri: Uri, originalFileName: String): Uri? {
        return try {
            val fileDirectory = File(filesDir, "note_files")

            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }

            val safeFileName = originalFileName
                .replace("/", "_")
                .replace("\\", "_")

            val file = File(
                fileDirectory,
                "${System.currentTimeMillis()}_${UUID.randomUUID()}_$safeFileName"
            )

            contentResolver.openInputStream(sourceUri).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    if (inputStream == null) {
                        return null
                    }

                    inputStream.copyTo(outputStream)
                }
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadNoteFiles() {
        noteFileList = if (isEditMode && noteId != -1) {
            databaseHelper.getFilesForNote(noteId)
        } else {
            ArrayList()
        }

        if (!isEditMode || noteId == -1) {
            noteFileList.addAll(pendingFiles)
        }

//        if (noteFileList.isEmpty()) {
//            recyclerViewNoteFiles.visibility = View.GONE
//            tvEmptyFiles.visibility = View.VISIBLE
//        } else {
//            recyclerViewNoteFiles.visibility = View.VISIBLE
//            tvEmptyFiles.visibility = View.GONE
//        }

        updateFileSectionVisibility()

        noteFileAdapter = NoteFileAdapter(
            noteFileList,
            showDeleteButton = true,
            onFileClick = { selectedFile ->
                openNoteFile(selectedFile)
            },
            onDeleteFileClick = { selectedFile ->
                confirmDeleteNoteFile(selectedFile)
            }
        )

        recyclerViewNoteFiles.adapter = noteFileAdapter
        updateAttachmentButtonText()
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

    private fun confirmDeleteNoteFile(noteFile: NoteFile) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Remove \"${noteFile.fileName}\" from this note?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNoteFile(noteFile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNoteFile(noteFile: NoteFile) {
        if (noteFile.id < 0) {
            pendingFiles.remove(noteFile)

            pendingNoteMessages.removeAll {
                it.messageType == DatabaseHelper.MESSAGE_TYPE_FILE &&
                        it.fileUri == noteFile.fileUri
            }

            deleteLocalFile(noteFile.fileUri)

            Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show()
            loadNoteFiles()
            loadNoteMessages()
            updateAttachmentButtonText()
            return
        }

        val result = databaseHelper.deleteFileFromNote(noteFile.id)

        if (result > 0) {
            databaseHelper.deleteFileMessagesByUri(noteId, noteFile.fileUri)

            deleteLocalFile(noteFile.fileUri)

            Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show()
            loadNoteFiles()
            loadNoteMessages()
            updateAttachmentButtonText()
        } else {
            Toast.makeText(this, "Failed to remove file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLocalFile(fileUriString: String) {
        try {
            val uri = Uri.parse(fileUriString)

            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")

                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun savePendingFilesToNewNote(newNoteId: Int) {
        for (noteFile in pendingFiles) {
            databaseHelper.addFileToNote(
                noteId = newNoteId,
                fileName = noteFile.fileName,
                fileUri = noteFile.fileUri
            )
        }

        pendingFiles.clear()
    }

    // ---------------------------
    // LINK FUNCTIONS
    // ---------------------------

    private fun loadNoteLinks() {
        noteLinkList = LinkUtils.extractWebLinks(
            etNoteContent.text.toString()
        )

        noteLinkAdapter = NoteLinkAdapter(
            noteLinkList,
            onLinkClick = { selectedLink ->
                LinkUtils.openWebUrl(this, selectedLink)
            }
        )

        recyclerViewNoteLinks.adapter = noteLinkAdapter
        updateLinkSectionVisibility()
    }

    private fun updateLinkSectionVisibility() {
        if (
            !showAttachedLinksSection ||
            currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT ||
            noteLinkList.isEmpty()
        ) {
            tvAttachedLinksTitle.visibility = View.GONE
            recyclerViewNoteLinks.visibility = View.GONE
            tvEmptyLinks.visibility = View.GONE

            if (showAttachedLinksSection) {
                btnBottomToggleLinks.setImageResource(R.drawable.ic_link_off_red)
                btnBottomToggleLinks.contentDescription = "Hide attached links"
            } else {
                btnBottomToggleLinks.setImageResource(R.drawable.ic_link_simple)
                btnBottomToggleLinks.contentDescription = "Show attached links"
            }

            return
        }

        tvAttachedLinksTitle.visibility = View.VISIBLE
        recyclerViewNoteLinks.visibility = View.VISIBLE
        tvEmptyLinks.visibility = View.GONE

        btnBottomToggleLinks.setImageResource(R.drawable.ic_link_off_red)
        btnBottomToggleLinks.contentDescription = "Hide attached links"
    }

    private fun openAttachmentsPage() {
        if (!isEditMode || noteId == -1) {
            Toast.makeText(
                this,
                "Save the note first to view all attachments",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(this, NoteAttachmentsActivity::class.java)
        intent.putExtra("note_id", noteId)
        intent.putExtra("note_title", etNoteTitle.text.toString().trim())
        startActivity(intent)
    }

    private fun updateAttachmentButtonText() {
        val imageCount = if (isEditMode && noteId != -1) {
            databaseHelper.getImagesForNote(noteId).size
        } else {
            pendingImageUris.size
        }

        val fileCount = if (isEditMode && noteId != -1) {
            databaseHelper.getFilesForNote(noteId).size
        } else {
            pendingFiles.size
        }

        val totalCount = imageCount + fileCount

        btnViewAttachments.contentDescription = if (totalCount > 0) {
            "View attachments. $totalCount attachment(s)"
        } else {
            "View attachments"
        }
    }

    // ---------------------------
    // SAVE / DELETE
    // ---------------------------

    private fun saveNote() {
        val title = etNoteTitle.text.toString().trim()

        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CLASSIC) {
            syncMessagesFromClassicContent()
        }

        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
            syncClassicContentFromMessages()
        }

        val content = etNoteContent.text.toString().trim()
        val date = getCurrentDate()

        val existingImageCount = if (isEditMode && noteId != -1) {
            databaseHelper.getImagesForNote(noteId).size
        } else {
            0
        }

        val existingFileCount = if (isEditMode && noteId != -1) {
            databaseHelper.getFilesForNote(noteId).size
        } else {
            0
        }

//        val hasAnyNoteData =
//            title.isNotEmpty() ||
//                    content.isNotEmpty() ||
//                    pendingImageUris.isNotEmpty() ||
//                    pendingFiles.isNotEmpty() ||
//                    existingImageCount > 0 ||
//                    existingFileCount > 0 ||
//                    hasAnyChatMessages()

        val hasAnyNoteData =
            title.isNotEmpty() ||
                    content.isNotEmpty() ||
                    pendingImageUris.isNotEmpty() ||
                    pendingFiles.isNotEmpty() ||
                    existingImageCount > 0 ||
                    existingFileCount > 0 ||
                    hasAnyChatMessages() ||
                    hasAnyBulletPoints()

        if (!hasAnyNoteData) {
            etNoteTitle.error = "Title or content is required"
            etNoteContent.error = "Title or content is required"

            Toast.makeText(
                this,
//                "Please enter a title, content, or image",
                "Please enter a title, content, bullet point, image, or file",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (isEditMode) {
            val result = databaseHelper.updateNote(
                id = noteId,
                title = title,
                content = content,
                date = date,
                noteMode = currentNoteMode
            )

            if (result > 0) {
                databaseHelper.assignCategoriesToNote(noteId, selectedCategoryIds)
                databaseHelper.updateNoteFolder(noteId, selectedFolderId)

                databaseHelper.setNotePrivacy(
                    noteId = noteId,
                    isLocked = isLocked,
                    lockType = lockType,
                    passcodeHash = passcodeHash,
                    showTitleWhenLocked = showTitleWhenLocked
                )

                databaseHelper.updateNoteShortcut(
                    noteId = noteId,
                    shortcutEnabled = 0,
                    shortcutKeyword = ""
                )

                if (isLocked == 1) {
                    databaseHelper.deleteAllShortcutsForNote(noteId)
                }

                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
            }
        } else {
            val result = databaseHelper.addNote(
                title = title,
                content = content,
                date = date,
                noteMode = currentNoteMode
            )

            if (result != -1L) {
                val newNoteId = result.toInt()

                databaseHelper.assignCategoriesToNote(newNoteId, selectedCategoryIds)
                databaseHelper.updateNoteFolder(newNoteId, selectedFolderId)

                databaseHelper.setNotePrivacy(
                    noteId = newNoteId,
                    isLocked = isLocked,
                    lockType = lockType,
                    passcodeHash = passcodeHash,
                    showTitleWhenLocked = showTitleWhenLocked
                )

                databaseHelper.updateNoteShortcut(
                    noteId = newNoteId,
                    shortcutEnabled = 0,
                    shortcutKeyword = ""
                )

                savePendingShortcutsToNewNote(newNoteId)
                savePendingImagesToNewNote(newNoteId)
                savePendingFilesToNewNote(newNoteId)
                savePendingMessagesToNewNote(newNoteId)
                savePendingBulletsToNewNote(newNoteId)

                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNote() {
        if (!isEditMode || noteId == -1) {
            for (imageUri in pendingImageUris) {
                deleteLocalImageFile(imageUri)
            }

            for (noteFile in pendingFiles) {
                deleteLocalFile(noteFile.fileUri)
            }

            pendingImageUris.clear()
            pendingFiles.clear()
            pendingShortcutKeywords.clear()
            pendingNoteMessages.clear()
            pendingBullets.clear()

            finish()
            return
        }

        val result = databaseHelper.moveToRecentlyDeleted(noteId)

        if (result > 0) {
            Toast.makeText(this, "Moved to Recently Deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun updateImageSectionVisibility() {
        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT || noteImageList.isEmpty()) {
            tvAttachedImagesTitle.visibility = View.GONE
            recyclerViewNoteImages.visibility = View.GONE
            return
        }

        tvAttachedImagesTitle.visibility = View.VISIBLE
        recyclerViewNoteImages.visibility = View.VISIBLE
    }

    private fun updateFileSectionVisibility() {
        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT || noteFileList.isEmpty()) {
            tvAttachedFilesTitle.visibility = View.GONE
            recyclerViewNoteFiles.visibility = View.GONE
            return
        }

        tvAttachedFilesTitle.visibility = View.VISIBLE
        recyclerViewNoteFiles.visibility = View.VISIBLE
    }

    private fun updateBottomClassicAttachmentButtonsVisibility() {
        if (currentNoteMode == DatabaseHelper.NOTE_MODE_CHAT) {
            btnBottomAddImage.visibility = View.GONE
            btnBottomAddFile.visibility = View.GONE
            btnBottomToggleLinks.visibility = View.GONE
        } else {
            btnBottomAddImage.visibility = View.VISIBLE
            btnBottomAddFile.visibility = View.VISIBLE
            btnBottomToggleLinks.visibility = View.VISIBLE
        }
    }

    private fun bulletTitleOptionsSectionVisibility(visible: Boolean) {
        val targetVisibility = if (visible) View.VISIBLE else View.GONE

        tvBulletPointsTitle.visibility = targetVisibility
        btnBulletOptions.visibility = targetVisibility
        btnAddBulletPoint.visibility = targetVisibility

        if (!visible) {
            recyclerViewNoteBullets.visibility = View.GONE
            tvEmptyBullets.visibility = View.GONE
        }
    }

    private fun updateBulletSectionVisibility() {
        if (!showBulletSection) {
            bulletTitleOptionsSection.visibility = View.GONE
            btnAddBulletPoint.visibility = View.GONE
            recyclerViewNoteBullets.visibility = View.GONE
            tvEmptyBullets.visibility = View.GONE

            btnToggleBulletSection.contentDescription = "Show bullet points"
            return
        }

        bulletTitleOptionsSection.visibility = View.VISIBLE
        btnAddBulletPoint.visibility = View.VISIBLE

        btnToggleBulletSection.contentDescription = "Hide bullet points"

        loadNoteBullets()
    }
}
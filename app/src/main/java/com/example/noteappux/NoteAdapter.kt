package com.example.noteappux

import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import android.widget.ImageButton

class NoteAdapter(
    private val notesList: ArrayList<Note>,
    private val databaseHelper: DatabaseHelper,
    private val showImagePreview: Boolean,
    private val showFilePreview: Boolean,
    private val showLinkPreview: Boolean,
    private val onNoteClick: (Note) -> Unit,
    private val onPinClick: (Note) -> Unit,
    private val onShareClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var openedPosition: Int = -1
    private var openedDirection: Int = 0
    private var openedHolder: NoteViewHolder? = null
    private val swipeInterpolator = DecelerateInterpolator()

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteSectionLabel: TextView = itemView.findViewById(R.id.tvNoteSectionLabel)
        val noteItemLayout: LinearLayout = itemView.findViewById(R.id.noteItemLayout)

        val tvNoteTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        val tvNoteContent: TextView = itemView.findViewById(R.id.tvNoteContent)

        val layoutNoteImagePreview: LinearLayout = itemView.findViewById(R.id.layoutNoteImagePreview)
        val imgNotePreview1: ImageView = itemView.findViewById(R.id.imgNotePreview1)
        val imgNotePreview2: ImageView = itemView.findViewById(R.id.imgNotePreview2)
        val imgNotePreview3: ImageView = itemView.findViewById(R.id.imgNotePreview3)

        val layoutNoteFilePreview: LinearLayout = itemView.findViewById(R.id.layoutNoteFilePreview)
        val tvFilePreview1: TextView = itemView.findViewById(R.id.tvFilePreview1)
        val tvFilePreview2: TextView = itemView.findViewById(R.id.tvFilePreview2)
        val tvFilePreview3: TextView = itemView.findViewById(R.id.tvFilePreview3)

        val layoutNoteLinkPreview: LinearLayout = itemView.findViewById(R.id.layoutNoteLinkPreview)
        val tvLinkPreview1: TextView = itemView.findViewById(R.id.tvLinkPreview1)
        val tvLinkPreview2: TextView = itemView.findViewById(R.id.tvLinkPreview2)
        val tvLinkPreview3: TextView = itemView.findViewById(R.id.tvLinkPreview3)

        val tvNoteDate: TextView = itemView.findViewById(R.id.tvNoteDate)

        val btnSwipePinNote: ImageButton = itemView.findViewById(R.id.btnSwipePinNote)
        val btnShareNote: ImageButton = itemView.findViewById(R.id.btnShareNote)
        val btnDeleteSwipeNote: ImageButton = itemView.findViewById(R.id.btnDeleteSwipeNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)

        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]

        val rightActionWidth = dpToPx(holder.itemView, 170f)
        val leftActionWidth = dpToPx(holder.itemView, 90f)

        bindNoteText(holder, note)

        if (showImagePreview) {
            bindLatestImages(holder, note)
        } else {
            hideLatestImages(holder)
        }

        if (showFilePreview) {
            bindLatestFiles(holder, note)
        } else {
            hideLatestFiles(holder)
        }

        if (showLinkPreview) {
            bindLatestLinks(holder, note)
        } else {
            hideLatestLinks(holder)
        }

        if (note.isPinned == 1) {
            holder.btnSwipePinNote.setImageResource(R.drawable.ic_unpin)
            holder.btnSwipePinNote.contentDescription = "Unpin note"
        } else {
            holder.btnSwipePinNote.setImageResource(R.drawable.ic_pin)
            holder.btnSwipePinNote.contentDescription = "Pin note"
        }

        setupSectionLabel(holder, position)

        holder.noteItemLayout.animate().cancel()

        holder.noteItemLayout.translationX = if (position == openedPosition) {
            openedHolder = holder

            if (openedDirection == 1) {
                leftActionWidth
            } else {
                -rightActionWidth
            }
        } else {
            0f
        }

        holder.btnSwipePinNote.setOnClickListener {
            closeOpenedItemSmoothly()
            onPinClick(note)
        }

        holder.btnShareNote.setOnClickListener {
            closeOpenedItemSmoothly()
            onShareClick(note)
        }

        holder.btnDeleteSwipeNote.setOnClickListener {
            closeOpenedItemSmoothly()
            onDeleteClick(note)
        }

        setupSwipe(holder, position, leftActionWidth, rightActionWidth)
    }

    private fun bindNoteText(holder: NoteViewHolder, note: Note) {
        if (note.isLocked == 1) {
            bindLockedNoteText(holder, note)
        } else {
            bindUnlockedNoteText(holder, note)
        }
    }

    private fun bindLockedNoteText(holder: NoteViewHolder, note: Note) {
        if (note.showTitleWhenLocked == 1) {
            holder.tvNoteTitle.text = note.title
        } else {
            holder.tvNoteTitle.text = "🔒 Locked Note"
        }

        val lockMethodText = when (note.lockType) {
            DatabaseHelper.LOCK_PASSCODE_ONLY -> "Passcode protected"
            DatabaseHelper.LOCK_FINGERPRINT_ONLY -> "Fingerprint protected"
            DatabaseHelper.LOCK_BOTH -> "Two-factor protected"
            else -> "Private content hidden"
        }

        holder.tvNoteContent.text = "🔒 $lockMethodText"
        holder.tvNoteDate.text = note.date
    }

    private fun bindUnlockedNoteText(holder: NoteViewHolder, note: Note) {
        holder.tvNoteTitle.text = note.title

        holder.tvNoteContent.text = if (note.noteMode == DatabaseHelper.NOTE_MODE_CHAT) {
            buildChatNotePreview(note)
        } else {
            buildClassicNotePreview(note)
        }

        holder.tvNoteDate.text = note.date
    }

    private fun buildClassicNotePreview(note: Note): String {
        return if (note.content.isBlank()) {
            "No content"
        } else {
            note.content
        }
    }

    private fun buildChatNotePreview(note: Note): String {
        return if (note.content.isBlank()) {
            "💬 Chat note"
        } else {
            "💬 ${note.content}"
        }
    }

    private fun hideLatestImages(holder: NoteViewHolder) {
        holder.layoutNoteImagePreview.visibility = View.GONE

        holder.imgNotePreview1.visibility = View.GONE
        holder.imgNotePreview2.visibility = View.GONE
        holder.imgNotePreview3.visibility = View.GONE

        holder.imgNotePreview1.setImageDrawable(null)
        holder.imgNotePreview2.setImageDrawable(null)
        holder.imgNotePreview3.setImageDrawable(null)
    }

    private fun bindLatestImages(holder: NoteViewHolder, note: Note) {
        hideLatestImages(holder)

        if (note.isLocked == 1) {
            return
        }

        val latestImages = databaseHelper.getLatestImagesForNote(note.id, 3)

        if (latestImages.isEmpty()) {
            return
        }

        holder.layoutNoteImagePreview.visibility = View.VISIBLE

        val previewViews = arrayOf(
            holder.imgNotePreview1,
            holder.imgNotePreview2,
            holder.imgNotePreview3
        )

        for (i in latestImages.indices) {
            if (i >= previewViews.size) {
                break
            }

            previewViews[i].visibility = View.VISIBLE
            previewViews[i].setImageURI(Uri.parse(latestImages[i].imageUri))
        }
    }

    private fun hideLatestFiles(holder: NoteViewHolder) {
        holder.layoutNoteFilePreview.visibility = View.GONE

        holder.tvFilePreview1.visibility = View.GONE
        holder.tvFilePreview2.visibility = View.GONE
        holder.tvFilePreview3.visibility = View.GONE

        holder.tvFilePreview1.text = ""
        holder.tvFilePreview2.text = ""
        holder.tvFilePreview3.text = ""
    }

    private fun bindLatestFiles(holder: NoteViewHolder, note: Note) {
        hideLatestFiles(holder)

        if (note.isLocked == 1) {
            return
        }

        val latestFiles = databaseHelper.getLatestFilesForNote(note.id, 3)

        if (latestFiles.isEmpty()) {
            return
        }

        holder.layoutNoteFilePreview.visibility = View.VISIBLE

        val fileViews = arrayOf(
            holder.tvFilePreview1,
            holder.tvFilePreview2,
            holder.tvFilePreview3
        )

        for (i in latestFiles.indices) {
            if (i >= fileViews.size) {
                break
            }

            fileViews[i].visibility = View.VISIBLE
            fileViews[i].text = "${getFileIcon(latestFiles[i].fileName)} ${latestFiles[i].fileName}"
        }
    }

    private fun hideLatestLinks(holder: NoteViewHolder) {
        holder.layoutNoteLinkPreview.visibility = View.GONE

        holder.tvLinkPreview1.visibility = View.GONE
        holder.tvLinkPreview2.visibility = View.GONE
        holder.tvLinkPreview3.visibility = View.GONE

        holder.tvLinkPreview1.text = ""
        holder.tvLinkPreview2.text = ""
        holder.tvLinkPreview3.text = ""
    }

    private fun bindLatestLinks(holder: NoteViewHolder, note: Note) {
        hideLatestLinks(holder)

        if (note.isLocked == 1) {
            return
        }

        val latestLinks = if (note.noteMode == DatabaseHelper.NOTE_MODE_CHAT) {
            getLatestLinksForChatNote(note)
        } else {
            LinkUtils.extractWebLinks(note.content)
        }

        if (latestLinks.isEmpty()) {
            return
        }

        holder.layoutNoteLinkPreview.visibility = View.VISIBLE

        val linkViews = arrayOf(
            holder.tvLinkPreview1,
            holder.tvLinkPreview2,
            holder.tvLinkPreview3
        )

        for (i in latestLinks.indices) {
            if (i >= linkViews.size) {
                break
            }

            linkViews[i].visibility = View.VISIBLE
            linkViews[i].text = "🔗 ${latestLinks[i]}"
        }
    }

    private fun getLatestLinksForChatNote(note: Note): ArrayList<String> {
        val resultLinks = ArrayList<String>()

        val messages = databaseHelper.getMessagesForNote(note.id)

        for (message in messages.asReversed()) {
            if (resultLinks.size >= 3) {
                break
            }

            if (message.messageType == DatabaseHelper.MESSAGE_TYPE_TEXT) {
                val linksFromText = LinkUtils.extractWebLinks(message.textContent)

                for (link in linksFromText) {
                    if (resultLinks.size >= 3) {
                        break
                    }

                    resultLinks.add(link)
                }
            }

            if (message.messageType == DatabaseHelper.MESSAGE_TYPE_LINK && message.linkUrl.isNotBlank()) {
                resultLinks.add(message.linkUrl)
            }
        }

        return resultLinks
    }

    private fun getFileIcon(fileName: String): String {
        val lowerName = fileName.lowercase()

        return when {
            lowerName.endsWith(".pdf") -> "📕"
            lowerName.endsWith(".doc") || lowerName.endsWith(".docx") -> "📘"
            lowerName.endsWith(".ppt") || lowerName.endsWith(".pptx") -> "📙"
            lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx") -> "📗"
            lowerName.endsWith(".zip") || lowerName.endsWith(".rar") || lowerName.endsWith(".7z") -> "🗜"
            lowerName.endsWith(".txt") -> "📄"
            lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") -> "🎵"
            lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || lowerName.endsWith(".mkv") -> "🎬"
            else -> "📎"
        }
    }

    private fun setupSectionLabel(holder: NoteViewHolder, position: Int) {
        val currentNote = notesList[position]
        val previousNote = if (position > 0) notesList[position - 1] else null

        when {
            position == 0 && currentNote.isPinned == 1 -> {
                holder.tvNoteSectionLabel.visibility = View.VISIBLE
                holder.tvNoteSectionLabel.text = "Pinned"
            }

            currentNote.isPinned == 0 && previousNote?.isPinned == 1 -> {
                holder.tvNoteSectionLabel.visibility = View.VISIBLE
                holder.tvNoteSectionLabel.text = "• • •\nNotes"
            }

            position == 0 && currentNote.isPinned == 0 -> {
                holder.tvNoteSectionLabel.visibility = View.VISIBLE
                holder.tvNoteSectionLabel.text = "Notes"
            }

            else -> {
                holder.tvNoteSectionLabel.visibility = View.GONE
            }
        }
    }

    private fun setupSwipe(
        holder: NoteViewHolder,
        position: Int,
        leftActionWidth: Float,
        rightActionWidth: Float
    ) {
        var downX = 0f
        var downY = 0f
        var startTranslationX = 0f
        var isHorizontalSwipe = false
        var isVerticalScroll = false
        var velocityTracker: VelocityTracker? = null

        val tapSlop = 12f
        val swipeStartThreshold = 18f
        val openPercent = 0.35f
        val flingVelocityThreshold = 850f

        holder.noteItemLayout.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().cancel()

                    downX = event.rawX
                    downY = event.rawY
                    startTranslationX = view.translationX
                    isHorizontalSwipe = false
                    isVerticalScroll = false

                    velocityTracker?.recycle()
                    velocityTracker = VelocityTracker.obtain()
                    velocityTracker?.addMovement(event)

                    view.parent.requestDisallowInterceptTouchEvent(false)
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    velocityTracker?.addMovement(event)

                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY

                    if (!isHorizontalSwipe && !isVerticalScroll) {
                        if (abs(deltaY) > swipeStartThreshold && abs(deltaY) > abs(deltaX)) {
                            isVerticalScroll = true
                            view.parent.requestDisallowInterceptTouchEvent(false)

                            if (view.translationX != 0f && openedPosition != position) {
                                animateToPosition(view, 0f)
                            }

                            return@setOnTouchListener false
                        }

                        if (abs(deltaX) > swipeStartThreshold && abs(deltaX) > abs(deltaY)) {
                            isHorizontalSwipe = true
                            view.parent.requestDisallowInterceptTouchEvent(true)

                            if (openedPosition != -1 && openedPosition != position) {
                                closeOpenedItemSmoothly()
                            }
                        }
                    }

                    if (isVerticalScroll) {
                        return@setOnTouchListener false
                    }

                    if (isHorizontalSwipe) {
                        var targetTranslation = startTranslationX + deltaX

                        targetTranslation = targetTranslation.coerceIn(
                            -rightActionWidth,
                            leftActionWidth
                        )

                        view.translationX = targetTranslation
                        return@setOnTouchListener true
                    }

                    false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    velocityTracker?.addMovement(event)
                    velocityTracker?.computeCurrentVelocity(1000)

                    val velocityX = velocityTracker?.xVelocity ?: 0f
                    velocityTracker?.recycle()
                    velocityTracker = null

                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY
                    val currentTranslation = view.translationX

                    view.parent.requestDisallowInterceptTouchEvent(false)

                    if (isVerticalScroll) {
                        isVerticalScroll = false
                        isHorizontalSwipe = false
                        return@setOnTouchListener false
                    }

                    if (isHorizontalSwipe) {
                        val shouldOpenRightActions =
                            currentTranslation < -(rightActionWidth * openPercent) ||
                                    velocityX < -flingVelocityThreshold

                        val shouldOpenLeftAction =
                            currentTranslation > (leftActionWidth * openPercent) ||
                                    velocityX > flingVelocityThreshold

                        when {
                            shouldOpenRightActions -> {
                                openedPosition = position
                                openedDirection = -1
                                openedHolder = holder
                                animateToPosition(view, -rightActionWidth)
                            }

                            shouldOpenLeftAction -> {
                                openedPosition = position
                                openedDirection = 1
                                openedHolder = holder
                                animateToPosition(view, leftActionWidth)
                            }

                            else -> {
                                if (openedPosition == position) {
                                    openedPosition = -1
                                    openedDirection = 0
                                    openedHolder = null
                                }

                                animateToPosition(view, 0f)
                            }
                        }

                        isVerticalScroll = false
                        isHorizontalSwipe = false
                        return@setOnTouchListener true
                    }

                    val isRealTap = abs(deltaX) < tapSlop && abs(deltaY) < tapSlop

                    if (isRealTap) {
                        if (openedPosition == position) {
                            openedPosition = -1
                            openedDirection = 0
                            openedHolder = null
                            animateToPosition(view, 0f)
                        } else {
                            onNoteClick(notesList[position])
                        }

                        return@setOnTouchListener true
                    }

                    false
                }

                else -> false
            }
        }
    }

    private fun closeOpenedItemSmoothly() {
        val holderToClose = openedHolder
        val oldOpenedPosition = openedPosition

        openedPosition = -1
        openedDirection = 0
        openedHolder = null

        if (holderToClose != null) {
            animateToPosition(holderToClose.noteItemLayout, 0f)
        } else if (oldOpenedPosition != -1) {
            notifyItemChanged(oldOpenedPosition)
        }
    }

    private fun animateToPosition(view: View, targetTranslationX: Float) {
        val distance = abs(view.translationX - targetTranslationX)

        val duration = when {
            distance > 140f -> 220L
            distance > 60f -> 180L
            else -> 140L
        }

        view.animate()
            .translationX(targetTranslationX)
            .setDuration(duration)
            .setInterpolator(swipeInterpolator)
            .start()
    }

    private fun dpToPx(view: View, dp: Float): Float {
        return dp * view.resources.displayMetrics.density
    }

    override fun getItemCount(): Int {
        return notesList.size
    }
}
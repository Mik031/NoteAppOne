package com.example.noteappux

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class DeletedNoteAdapter(
    private val deletedNotesList: ArrayList<Note>,
    private val onDeletedNoteClick: (Note) -> Unit,
    private val onRecoverClick: (Note) -> Unit,
    private val onDeleteForeverClick: (Note) -> Unit
) : RecyclerView.Adapter<DeletedNoteAdapter.DeletedNoteViewHolder>() {

    private var openedPosition: Int = -1
    private var openedHolder: DeletedNoteViewHolder? = null

    private val swipeInterpolator = DecelerateInterpolator()

    class DeletedNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deletedNoteItemLayout: LinearLayout = itemView.findViewById(R.id.deletedNoteItemLayout)
        val tvDeletedNoteTitle: TextView = itemView.findViewById(R.id.tvDeletedNoteTitle)
        val tvDeletedNoteContent: TextView = itemView.findViewById(R.id.tvDeletedNoteContent)
        val tvDeletedNoteDate: TextView = itemView.findViewById(R.id.tvDeletedNoteDate)
        val btnRecoverDeletedNote: TextView = itemView.findViewById(R.id.btnRecoverDeletedNote)
        val btnDeleteForeverNote: TextView = itemView.findViewById(R.id.btnDeleteForeverNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedNoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deleted_note, parent, false)

        return DeletedNoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeletedNoteViewHolder, position: Int) {
        val note = deletedNotesList[position]
        val actionWidth = dpToPx(holder.itemView, 170f)

        holder.tvDeletedNoteTitle.text = note.title
        holder.tvDeletedNoteContent.text = note.content
        holder.tvDeletedNoteDate.text = note.date

        holder.deletedNoteItemLayout.animate().cancel()

        holder.deletedNoteItemLayout.translationX = if (position == openedPosition) {
            openedHolder = holder
            -actionWidth
        } else {
            0f
        }

        holder.btnRecoverDeletedNote.setOnClickListener {
            closeOpenedItemSmoothly()
            onRecoverClick(note)
        }

        holder.btnDeleteForeverNote.setOnClickListener {
            closeOpenedItemSmoothly()
            onDeleteForeverClick(note)
        }

        setupSwipe(holder, position, actionWidth)
    }

    private fun setupSwipe(
        holder: DeletedNoteViewHolder,
        position: Int,
        actionWidth: Float
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

        holder.deletedNoteItemLayout.setOnTouchListener { view, event ->
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
                            -actionWidth,
                            0f
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
                        val shouldOpenActions =
                            currentTranslation < -(actionWidth * openPercent) ||
                                    velocityX < -flingVelocityThreshold

                        if (shouldOpenActions) {
                            openedPosition = position
                            openedHolder = holder
                            animateToPosition(view, -actionWidth)
                        } else {
                            if (openedPosition == position) {
                                openedPosition = -1
                                openedHolder = null
                            }

                            animateToPosition(view, 0f)
                        }

                        isVerticalScroll = false
                        isHorizontalSwipe = false
                        return@setOnTouchListener true
                    }

                    val isRealTap = abs(deltaX) < tapSlop && abs(deltaY) < tapSlop

                    if (isRealTap) {
                        if (openedPosition == position) {
                            openedPosition = -1
                            openedHolder = null
                            animateToPosition(view, 0f)
                        } else {
                            onDeletedNoteClick(deletedNotesList[position])
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
        openedHolder = null

        if (holderToClose != null) {
            animateToPosition(holderToClose.deletedNoteItemLayout, 0f)
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
        return deletedNotesList.size
    }
}
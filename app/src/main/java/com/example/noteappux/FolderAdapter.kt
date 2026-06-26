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

class FolderAdapter(
    private val folderList: ArrayList<Folder>,
    private val onFolderClick: (Folder) -> Unit,
    private val onEditFolderClick: (Folder) -> Unit,
    private val onDeleteFolderClick: (Folder) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var openedPosition: Int = -1
    private var openedHolder: FolderViewHolder? = null

    private val swipeInterpolator = DecelerateInterpolator()

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderItemLayout: LinearLayout = itemView.findViewById(R.id.folderItemLayout)
        val tvFolderName: TextView = itemView.findViewById(R.id.tvFolderName)
        val btnEditFolder: TextView = itemView.findViewById(R.id.btnEditFolder)
        val btnDeleteFolder: TextView = itemView.findViewById(R.id.btnDeleteFolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)

        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folderList[position]
        val actionWidth = dpToPx(holder.itemView, 150f)

        holder.tvFolderName.text = folder.name

        holder.folderItemLayout.animate().cancel()

        holder.folderItemLayout.translationX = if (position == openedPosition) {
            openedHolder = holder
            -actionWidth
        } else {
            0f
        }

        holder.btnEditFolder.setOnClickListener {
            closeOpenedItemSmoothly()
            onEditFolderClick(folder)
        }

        holder.btnDeleteFolder.setOnClickListener {
            closeOpenedItemSmoothly()
            onDeleteFolderClick(folder)
        }

        setupSwipe(holder, position, actionWidth)
    }

    private fun setupSwipe(
        holder: FolderViewHolder,
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

        holder.folderItemLayout.setOnTouchListener { view, event ->
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
                            onFolderClick(folderList[position])
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
            animateToPosition(holderToClose.folderItemLayout, 0f)
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
        return folderList.size
    }

    fun getFolderAt(position: Int): Folder {
        return folderList[position]
    }
}
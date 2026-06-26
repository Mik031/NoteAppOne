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

class CategoryAdapter(
    private val categoryList: ArrayList<Category>,
    private val onCategoryClick: (Category) -> Unit,
    private val onEditCategoryClick: (Category) -> Unit,
    private val onDeleteCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var openedPosition: Int = -1
    private var openedHolder: CategoryViewHolder? = null

    private val swipeInterpolator = DecelerateInterpolator()

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryItemLayout: LinearLayout = itemView.findViewById(R.id.categoryItemLayout)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val btnEditCategory: TextView = itemView.findViewById(R.id.btnEditCategory)
        val btnDeleteCategory: TextView = itemView.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        val actionWidth = dpToPx(holder.itemView, 150f)

        holder.tvCategoryName.text = category.name

        holder.categoryItemLayout.animate().cancel()

        holder.categoryItemLayout.translationX = if (position == openedPosition) {
            openedHolder = holder
            -actionWidth
        } else {
            0f
        }

        holder.btnEditCategory.setOnClickListener {
            closeOpenedItemSmoothly()
            onEditCategoryClick(category)
        }

        holder.btnDeleteCategory.setOnClickListener {
            closeOpenedItemSmoothly()
            onDeleteCategoryClick(category)
        }

        setupSwipe(holder, position, actionWidth)
    }

    private fun setupSwipe(
        holder: CategoryViewHolder,
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

        holder.categoryItemLayout.setOnTouchListener { view, event ->
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
                            onCategoryClick(categoryList[position])
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
            animateToPosition(holderToClose.categoryItemLayout, 0f)
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
        return categoryList.size
    }

    fun getCategoryAt(position: Int): Category {
        return categoryList[position]
    }
}
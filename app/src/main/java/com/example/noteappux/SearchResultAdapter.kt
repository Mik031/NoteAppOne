package com.example.noteappux

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class SearchResultAdapter(
    private val searchResults: ArrayList<SearchResult>,
    private val onResultClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchResultItemLayout: LinearLayout =
            itemView.findViewById(R.id.searchResultItemLayout)

        val tvSearchResultIcon: TextView =
            itemView.findViewById(R.id.tvSearchResultIcon)

        val tvSearchResultTitle: TextView =
            itemView.findViewById(R.id.tvSearchResultTitle)

        val tvSearchResultSubtitle: TextView =
            itemView.findViewById(R.id.tvSearchResultSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)

        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val result = searchResults[position]

        holder.tvSearchResultTitle.text = result.title
        holder.tvSearchResultSubtitle.text = result.subtitle

        holder.tvSearchResultIcon.text = when (result.type) {
            "note" -> "📝"
            "category" -> "🏷"
            "folder" -> "📁"
            "pinned_folder" -> "📌"
            else -> "🔎"
        }

        setupTapSafeClick(holder, position)
    }

    private fun setupTapSafeClick(holder: SearchResultViewHolder, position: Int) {
        var downX = 0f
        var downY = 0f

        val tapSlop = 12f
        val scrollThreshold = 18f

        holder.searchResultItemLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY

                    if (abs(deltaY) > scrollThreshold && abs(deltaY) > abs(deltaX)) {
                        return@setOnTouchListener false
                    }

                    false
                }

                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY

                    val isRealTap = abs(deltaX) < tapSlop && abs(deltaY) < tapSlop

                    if (isRealTap) {
                        onResultClick(searchResults[position])
                        return@setOnTouchListener true
                    }

                    false
                }

                else -> false
            }
        }
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }
}
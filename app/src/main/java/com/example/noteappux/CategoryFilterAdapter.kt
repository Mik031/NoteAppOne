package com.example.noteappux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryFilterAdapter(
    private val categoryList: ArrayList<Category>,
    private var selectedCategoryId: Int,
    private val onFilterClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryFilterAdapter.CategoryFilterViewHolder>() {

    class CategoryFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryFilterName: TextView = itemView.findViewById(R.id.tvCategoryFilterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_filter, parent, false)

        return CategoryFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryFilterViewHolder, position: Int) {
        val category = categoryList[position]
        val context = holder.itemView.context

        holder.tvCategoryFilterName.text = category.name

        if (category.id == selectedCategoryId) {
            holder.tvCategoryFilterName.setBackgroundResource(R.drawable.bg_category_filter_selected)
            holder.tvCategoryFilterName.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
        } else {
            holder.tvCategoryFilterName.setBackgroundResource(R.drawable.bg_category_filter_normal)
            holder.tvCategoryFilterName.setTextColor(
                ContextCompat.getColor(context, R.color.text_primary)
            )
        }

        holder.tvCategoryFilterName.setOnClickListener {
            selectedCategoryId = category.id
            notifyDataSetChanged()
            onFilterClick(category)
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun updateSelectedCategory(newSelectedCategoryId: Int) {
        selectedCategoryId = newSelectedCategoryId
        notifyDataSetChanged()
    }
}
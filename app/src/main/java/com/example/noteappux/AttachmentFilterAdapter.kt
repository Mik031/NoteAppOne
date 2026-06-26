package com.example.noteappux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AttachmentFilterAdapter(
    private val filterList: ArrayList<AttachmentFilterItem>,
    private var selectedNoteId: Int,
    private val onFilterClick: (AttachmentFilterItem) -> Unit
) : RecyclerView.Adapter<AttachmentFilterAdapter.AttachmentFilterViewHolder>() {

    class AttachmentFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAttachmentFilterName: TextView = itemView.findViewById(R.id.tvAttachmentFilterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attachment_filter, parent, false)

        return AttachmentFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentFilterViewHolder, position: Int) {
        val filter = filterList[position]

        holder.tvAttachmentFilterName.text = "${filter.noteTitle} (${filter.count})"

        if (filter.noteId == selectedNoteId) {
            holder.tvAttachmentFilterName.setBackgroundResource(R.drawable.bg_create_note_button)
            holder.tvAttachmentFilterName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        } else {
            holder.tvAttachmentFilterName.setBackgroundResource(R.drawable.bg_note_input)
            holder.tvAttachmentFilterName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
            )
        }

        holder.tvAttachmentFilterName.setOnClickListener {
            selectedNoteId = filter.noteId
            notifyDataSetChanged()
            onFilterClick(filter)
        }
    }

    override fun getItemCount(): Int {
        return filterList.size
    }
}

data class AttachmentFilterItem(
    val noteId: Int,
    val noteTitle: String,
    val count: Int
)
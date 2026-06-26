package com.example.noteappux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteLinkAdapter(
    private val linkList: ArrayList<String>,
    private val onLinkClick: (String) -> Unit
) : RecyclerView.Adapter<NoteLinkAdapter.NoteLinkViewHolder>() {

    class NoteLinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteLinkItemLayout: LinearLayout = itemView.findViewById(R.id.noteLinkItemLayout)
        val tvNoteLinkUrl: TextView = itemView.findViewById(R.id.tvNoteLinkUrl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteLinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_link, parent, false)

        return NoteLinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteLinkViewHolder, position: Int) {
        val link = linkList[position]

        holder.tvNoteLinkUrl.text = link

        holder.noteLinkItemLayout.setOnClickListener {
            onLinkClick(link)
        }
    }

    override fun getItemCount(): Int {
        return linkList.size
    }
}
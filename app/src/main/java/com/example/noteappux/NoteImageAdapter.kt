package com.example.noteappux

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteImageAdapter(
    private val imageList: ArrayList<NoteImage>,
    private val showDeleteButton: Boolean,
    private val onImageClick: (NoteImage, Int) -> Unit,
    private val onDeleteImageClick: (NoteImage) -> Unit
) : RecyclerView.Adapter<NoteImageAdapter.NoteImageViewHolder>() {

    class NoteImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgNoteAttachment: ImageView = itemView.findViewById(R.id.imgNoteAttachment)
        val btnDeleteNoteImage: TextView = itemView.findViewById(R.id.btnDeleteNoteImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_image, parent, false)

        return NoteImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteImageViewHolder, position: Int) {
        val noteImage = imageList[position]

        holder.imgNoteAttachment.setImageURI(Uri.parse(noteImage.imageUri))

        holder.imgNoteAttachment.setOnClickListener {
            onImageClick(noteImage, position)
        }

        if (showDeleteButton) {
            holder.btnDeleteNoteImage.visibility = View.VISIBLE

            holder.btnDeleteNoteImage.setOnClickListener {
                onDeleteImageClick(noteImage)
            }
        } else {
            holder.btnDeleteNoteImage.visibility = View.GONE
            holder.btnDeleteNoteImage.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
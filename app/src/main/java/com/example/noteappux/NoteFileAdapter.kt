package com.example.noteappux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteFileAdapter(
    private val fileList: ArrayList<NoteFile>,
    private val showDeleteButton: Boolean,
    private val onFileClick: (NoteFile) -> Unit,
    private val onDeleteFileClick: (NoteFile) -> Unit
) : RecyclerView.Adapter<NoteFileAdapter.NoteFileViewHolder>() {

    class NoteFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteFileItemLayout: LinearLayout = itemView.findViewById(R.id.noteFileItemLayout)
        val tvNoteFileIcon: TextView = itemView.findViewById(R.id.tvNoteFileIcon)
        val tvNoteFileName: TextView = itemView.findViewById(R.id.tvNoteFileName)
        val tvNoteFileType: TextView = itemView.findViewById(R.id.tvNoteFileType)
        val btnDeleteNoteFile: TextView = itemView.findViewById(R.id.btnDeleteNoteFile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_file, parent, false)

        return NoteFileViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteFileViewHolder, position: Int) {
        val noteFile = fileList[position]

        holder.tvNoteFileName.text = noteFile.fileName
        holder.tvNoteFileIcon.text = getFileIcon(noteFile.fileName)
        holder.tvNoteFileType.text = getFileTypeText(noteFile.fileName)

        holder.noteFileItemLayout.setOnClickListener {
            onFileClick(noteFile)
        }

        if (showDeleteButton) {
            holder.btnDeleteNoteFile.visibility = View.VISIBLE
            holder.btnDeleteNoteFile.setOnClickListener {
                onDeleteFileClick(noteFile)
            }
        } else {
            holder.btnDeleteNoteFile.visibility = View.GONE
            holder.btnDeleteNoteFile.setOnClickListener(null)
        }
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

    private fun getFileTypeText(fileName: String): String {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")

        return if (extension.isNotEmpty()) {
            extension.uppercase() + " file"
        } else {
            "Attached file"
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }
}
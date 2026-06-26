package com.example.noteappux

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteMessageAdapter(
    private val messageList: ArrayList<NoteMessage>,
    private val onMessageLongClick: (NoteMessage) -> Unit,
    private val onImageClick: (NoteMessage) -> Unit,
    private val onFileClick: (NoteMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TEXT = 1
        private const val VIEW_TYPE_IMAGE = 2
        private const val VIEW_TYPE_FILE = 3
    }

    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessageText: TextView = itemView.findViewById(R.id.tvMessageText)
        val tvMessageTime: TextView = itemView.findViewById(R.id.tvMessageTime)
    }

    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMessageImage: ImageView = itemView.findViewById(R.id.imgMessageImage)
        val tvImageMessageTime: TextView = itemView.findViewById(R.id.tvImageMessageTime)
    }

    class FileMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessageFileIcon: TextView = itemView.findViewById(R.id.tvMessageFileIcon)
        val tvMessageFileName: TextView = itemView.findViewById(R.id.tvMessageFileName)
        val tvMessageFileType: TextView = itemView.findViewById(R.id.tvMessageFileType)
        val tvFileMessageTime: TextView = itemView.findViewById(R.id.tvFileMessageTime)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]

        return when (message.messageType) {
            DatabaseHelper.MESSAGE_TYPE_IMAGE -> VIEW_TYPE_IMAGE
            DatabaseHelper.MESSAGE_TYPE_FILE -> VIEW_TYPE_FILE
            else -> VIEW_TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_note_message_image, parent, false)

                ImageMessageViewHolder(view)
            }

            VIEW_TYPE_FILE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_note_message_file, parent, false)

                FileMessageViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_note_message_text, parent, false)

                TextMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        when (holder) {
            is ImageMessageViewHolder -> bindImageMessage(holder, message)
            is FileMessageViewHolder -> bindFileMessage(holder, message)
            is TextMessageViewHolder -> bindTextMessage(holder, message)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    private fun bindTextMessage(
        holder: TextMessageViewHolder,
        message: NoteMessage
    ) {
        setupClickableLinks(
            textView = holder.tvMessageText,
            text = message.textContent
        )

        holder.tvMessageTime.text = formatTime(message.createdAt)

        holder.itemView.setOnLongClickListener {
            onMessageLongClick(message)
            true
        }
    }

    private fun bindImageMessage(
        holder: ImageMessageViewHolder,
        message: NoteMessage
    ) {
        holder.imgMessageImage.setImageURI(Uri.parse(message.imageUri))
        holder.tvImageMessageTime.text = formatTime(message.createdAt)

        holder.imgMessageImage.setOnClickListener {
            onImageClick(message)
        }

        holder.itemView.setOnLongClickListener {
            onMessageLongClick(message)
            true
        }
    }

    private fun bindFileMessage(
        holder: FileMessageViewHolder,
        message: NoteMessage
    ) {
        holder.tvMessageFileIcon.text = getFileIcon(message.fileName)
        holder.tvMessageFileName.text = message.fileName
        holder.tvMessageFileType.text = getFileTypeText(message.fileName)
        holder.tvFileMessageTime.text = formatTime(message.createdAt)

        holder.itemView.setOnClickListener {
            onFileClick(message)
        }

        holder.itemView.setOnLongClickListener {
            onMessageLongClick(message)
            true
        }
    }

    private fun setupClickableLinks(
        textView: TextView,
        text: String
    ) {
        val spannableString = SpannableString(text)

        val linkRegex = Regex(
            pattern = """((https?://|www\.)[^\s]+)""",
            option = RegexOption.IGNORE_CASE
        )

        val matches = linkRegex.findAll(text).toList()

        if (matches.isEmpty()) {
            textView.text = text
            textView.movementMethod = null
            textView.highlightColor = Color.TRANSPARENT
            return
        }

        for (match in matches) {
            val rawUrl = match.value
            val startIndex = match.range.first
            val endIndex = match.range.last + 1

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openWebUrl(widget, rawUrl)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)

                    ds.color = ContextCompat.getColor(
                        textView.context,
                        R.color.primary_blue
                    )

                    ds.isUnderlineText = true
                }
            }

            spannableString.setSpan(
                clickableSpan,
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun openWebUrl(view: View, rawUrl: String) {
        try {
            val finalUrl = if (
                rawUrl.startsWith("http://", ignoreCase = true) ||
                rawUrl.startsWith("https://", ignoreCase = true)
            ) {
                rawUrl
            } else {
                "https://$rawUrl"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            view.context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                view.context,
                "No app found to open this link",
                Toast.LENGTH_SHORT
            ).show()
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

    private fun formatTime(timeMillis: Long): String {
        if (timeMillis <= 0L) {
            return ""
        }

        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(Date(timeMillis))
    }
}
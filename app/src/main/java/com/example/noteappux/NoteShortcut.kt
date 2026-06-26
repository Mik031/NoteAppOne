package com.example.noteappux

data class NoteShortcut(
    val id: Int,
    val noteId: Int,
    val shortcutKeyword: String,
    val createdDate: Long,
    val noteTitle: String = "",
    val noteContent: String = ""
)
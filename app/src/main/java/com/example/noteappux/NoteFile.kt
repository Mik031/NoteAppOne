package com.example.noteappux

data class NoteFile(
    val id: Int,
    val noteId: Int,
    val fileName: String,
    val fileUri: String,
    val createdDate: Long
)
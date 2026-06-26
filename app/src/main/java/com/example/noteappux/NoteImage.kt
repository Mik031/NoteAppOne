package com.example.noteappux

data class NoteImage(
    val id: Int,
    val noteId: Int,
    val imageUri: String,
    val createdDate: Long
)
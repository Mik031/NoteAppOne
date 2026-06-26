package com.example.noteappux

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val date: String,
    val isDeleted: Int,
    val deletedDate: Long,
    val isPinned: Int,
    val folderId: Int,
    val isLocked: Int,
    val passcodeHash: String,
    val lockType: Int,
    val showTitleWhenLocked: Int,
    val shortcutEnabled: Int,
    val shortcutKeyword: String,
    val noteMode: String
)
package com.example.noteappux

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TextExpansionAccessibilityService : AccessibilityService() {

    private lateinit var databaseHelper: DatabaseHelper

    private var lastExpandedText: String = ""
    private var lastExpansionTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        databaseHelper = DatabaseHelper(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (
            event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_FOCUSED
        ) {
            return
        }

        val sourceNode = event.source ?: return

        if (!sourceNode.isEditable) {
            sourceNode.recycle()
            return
        }

        val currentText = sourceNode.text?.toString() ?: ""

        if (currentText.isBlank()) {
            sourceNode.recycle()
            return
        }

        val currentTime = System.currentTimeMillis()

        if (
            currentText == lastExpandedText &&
            currentTime - lastExpansionTime < 1000L
        ) {
            sourceNode.recycle()
            return
        }

        val shortcutList = databaseHelper.getAllActiveShortcuts()

        if (shortcutList.isEmpty()) {
            sourceNode.recycle()
            return
        }

        val matchedShortcut = findMatchedShortcut(
            currentText = currentText,
            shortcutList = shortcutList
        )

        if (matchedShortcut == null) {
            sourceNode.recycle()
            return
        }

        val shortcutKeyword = matchedShortcut.shortcutKeyword

        if (shortcutKeyword.isEmpty()) {
            sourceNode.recycle()
            return
        }

        val textBeforeShortcut = currentText.dropLast(shortcutKeyword.length)
        val expandedText = textBeforeShortcut + matchedShortcut.noteContent

        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                expandedText
            )
        }

        val success = sourceNode.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            arguments
        )

        if (success) {
            lastExpandedText = expandedText
            lastExpansionTime = System.currentTimeMillis()
        }

        sourceNode.recycle()
    }

    override fun onInterrupt() {
        // No action needed
    }

    private fun findMatchedShortcut(
        currentText: String,
        shortcutList: ArrayList<NoteShortcut>
    ): NoteShortcut? {
        val sortedShortcutList = shortcutList.sortedByDescending {
            it.shortcutKeyword.length
        }

        for (shortcut in sortedShortcutList) {
            val keyword = shortcut.shortcutKeyword.trim()

            if (keyword.isEmpty()) {
                continue
            }

            if (currentText.endsWith(keyword, ignoreCase = true)) {
                return shortcut
            }
        }

        return null
    }
}
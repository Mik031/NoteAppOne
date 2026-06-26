package com.example.noteappux

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NotePreviewSettingsActivity : AppCompatActivity() {

    private lateinit var btnImagePreview: LinearLayout
    private lateinit var btnFilePreview: LinearLayout
    private lateinit var btnLinkPreview: LinearLayout

    private lateinit var tvImagePreviewStatus: TextView
    private lateinit var tvFilePreviewStatus: TextView
    private lateinit var tvLinkPreviewStatus: TextView

    private var showImages: Boolean = true
    private var showFiles: Boolean = true
    private var showLinks: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_preview_settings)

        btnImagePreview = findViewById(R.id.btnImagePreview)
        btnFilePreview = findViewById(R.id.btnFilePreview)
        btnLinkPreview = findViewById(R.id.btnLinkPreview)

        tvImagePreviewStatus = findViewById(R.id.tvImagePreviewStatus)
        tvFilePreviewStatus = findViewById(R.id.tvFilePreviewStatus)
        tvLinkPreviewStatus = findViewById(R.id.tvLinkPreviewStatus)

        loadSettings()
        updateStatusText()

        btnImagePreview.setOnClickListener {
            showImages = !showImages
            NotePreviewSettingsHelper.setImagePreviewEnabled(this, showImages)
            updateStatusText()
            showChangedToast()
        }

        btnFilePreview.setOnClickListener {
            showFiles = !showFiles
            NotePreviewSettingsHelper.setFilePreviewEnabled(this, showFiles)
            updateStatusText()
            showChangedToast()
        }

        btnLinkPreview.setOnClickListener {
            showLinks = !showLinks
            NotePreviewSettingsHelper.setLinkPreviewEnabled(this, showLinks)
            updateStatusText()
            showChangedToast()
        }
    }

    private fun loadSettings() {
        showImages = NotePreviewSettingsHelper.isImagePreviewEnabled(this)
        showFiles = NotePreviewSettingsHelper.isFilePreviewEnabled(this)
        showLinks = NotePreviewSettingsHelper.isLinkPreviewEnabled(this)
    }

    private fun updateStatusText() {
        tvImagePreviewStatus.text = if (showImages) {
            "On"
        } else {
            "Off"
        }

        tvFilePreviewStatus.text = if (showFiles) {
            "On"
        } else {
            "Off"
        }

        tvLinkPreviewStatus.text = if (showLinks) {
            "On"
        } else {
            "Off"
        }
    }

    private fun showChangedToast() {
        Toast.makeText(
            this,
            "Preview settings updated",
            Toast.LENGTH_SHORT
        ).show()
    }
}
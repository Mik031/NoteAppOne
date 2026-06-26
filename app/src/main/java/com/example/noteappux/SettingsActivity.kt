package com.example.noteappux

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnTheme: LinearLayout
    private lateinit var btnTextShortcuts: LinearLayout
    private lateinit var btnPreviewSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnTheme = findViewById(R.id.btnTheme)
        btnTextShortcuts = findViewById(R.id.btnTextShortcuts)
        btnPreviewSettings = findViewById(R.id.btnPreviewSettings)

        btnTheme.setOnClickListener {
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
        }

        btnTextShortcuts.setOnClickListener {
            val intent = Intent(this, TextShortcutsActivity::class.java)
            startActivity(intent)
        }

        btnPreviewSettings.setOnClickListener {
            val intent = Intent(this, NotePreviewSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
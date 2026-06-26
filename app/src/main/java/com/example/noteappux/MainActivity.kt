package com.example.noteappux

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnNotes: LinearLayout
    private lateinit var btnSearch: LinearLayout
    private lateinit var btnCategories: LinearLayout
    private lateinit var btnFolders: LinearLayout
    private lateinit var btnRecentlyDeleted: LinearLayout
    private lateinit var btnSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNotes = findViewById(R.id.btnNotes)
        btnSearch = findViewById(R.id.btnSearch)
        btnCategories = findViewById(R.id.btnCategories)
        btnFolders = findViewById(R.id.btnFolders)
        btnRecentlyDeleted = findViewById(R.id.btnRecentlyDeleted)
        btnSettings = findViewById(R.id.btnSettings)

        btnNotes.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            startActivity(intent)
        }

        btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        btnCategories.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        btnFolders.setOnClickListener {
            val intent = Intent(this, FoldersActivity::class.java)
            startActivity(intent)
        }

        btnRecentlyDeleted.setOnClickListener {
            val intent = Intent(this, RecentlyDeletedActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
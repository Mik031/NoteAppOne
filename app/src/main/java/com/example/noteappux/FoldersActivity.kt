package com.example.noteappux

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FoldersActivity : AppCompatActivity() {

    private lateinit var layoutPinnedFolder: LinearLayout
    private lateinit var btnNewFolder: TextView
    private lateinit var btnSortFolders: TextView
    private lateinit var recyclerViewFolders: RecyclerView
    private lateinit var tvEmptyFolders: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var folderList: ArrayList<Folder>

    private var currentSortMode: Int = SORT_A_TO_Z

    companion object {
        private const val PREF_NAME = "folders_sort_pref"
        private const val PREF_SORT_MODE = "sort_mode"

        private const val SORT_NEWEST_FIRST = 0
        private const val SORT_OLDEST_FIRST = 1
        private const val SORT_A_TO_Z = 2
        private const val SORT_Z_TO_A = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folders)

        layoutPinnedFolder = findViewById(R.id.layoutPinnedFolder)
        btnNewFolder = findViewById(R.id.btnNewFolder)
        btnSortFolders = findViewById(R.id.btnSortFolders)
        recyclerViewFolders = findViewById(R.id.recyclerViewFolders)
        tvEmptyFolders = findViewById(R.id.tvEmptyFolders)

        databaseHelper = DatabaseHelper(this)

        currentSortMode = getSavedSortMode()
        updateSortButtonText()

        recyclerViewFolders.layoutManager = LinearLayoutManager(this)

        layoutPinnedFolder.setOnClickListener {
            val intent = Intent(this, FolderNotesActivity::class.java)
            intent.putExtra("folder_id", -1)
            intent.putExtra("folder_name", "Pinned / Favorites")
            intent.putExtra("is_pinned_folder", true)
            startActivity(intent)
        }

        btnNewFolder.setOnClickListener {
            showAddFolderDialog()
        }

        btnSortFolders.setOnClickListener {
            showSortDialog()
        }

        loadFolders()
    }

    override fun onResume() {
        super.onResume()
        loadFolders()
    }

    private fun getSavedSortMode(): Int {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(PREF_SORT_MODE, SORT_A_TO_Z)
    }

    private fun saveSortMode(sortMode: Int) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .putInt(PREF_SORT_MODE, sortMode)
            .apply()
    }

    private fun updateSortButtonText() {
        btnSortFolders.text = when (currentSortMode) {
            SORT_NEWEST_FIRST -> "Newest"
            SORT_OLDEST_FIRST -> "Oldest"
            SORT_A_TO_Z -> "A-Z"
            SORT_Z_TO_A -> "Z-A"
            else -> "Sort"
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Newest First",
            "Oldest First",
            "A to Z",
            "Z to A"
        )

        AlertDialog.Builder(this)
            .setTitle("Sort Folders")
            .setSingleChoiceItems(sortOptions, currentSortMode) { dialog, which ->
                currentSortMode = which
                saveSortMode(currentSortMode)
                updateSortButtonText()
                loadFolders()

                dialog.dismiss()

                Toast.makeText(
                    this,
                    "Sorted by ${sortOptions[which]}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadFolders() {
        folderList = databaseHelper.getAllFolders()
        folderList = sortFolders(folderList)

        if (folderList.isEmpty()) {
            recyclerViewFolders.visibility = View.GONE
            tvEmptyFolders.visibility = View.VISIBLE
        } else {
            recyclerViewFolders.visibility = View.VISIBLE
            tvEmptyFolders.visibility = View.GONE
        }

        folderAdapter = FolderAdapter(
            folderList,
            onFolderClick = { selectedFolder ->
                val intent = Intent(this, FolderNotesActivity::class.java)
                intent.putExtra("folder_id", selectedFolder.id)
                intent.putExtra("folder_name", selectedFolder.name)
                intent.putExtra("is_pinned_folder", false)
                startActivity(intent)
            },
            onEditFolderClick = { selectedFolder ->
                showEditFolderDialog(selectedFolder)
            },
            onDeleteFolderClick = { selectedFolder ->
                showDeleteFolderDialog(selectedFolder)
            }
        )

        recyclerViewFolders.adapter = folderAdapter
    }

    private fun sortFolders(originalList: ArrayList<Folder>): ArrayList<Folder> {
        val sortedList = when (currentSortMode) {
            SORT_NEWEST_FIRST -> {
                originalList.sortedByDescending { it.id }
            }

            SORT_OLDEST_FIRST -> {
                originalList.sortedBy { it.id }
            }

            SORT_A_TO_Z -> {
                originalList.sortedBy { it.name.lowercase() }
            }

            SORT_Z_TO_A -> {
                originalList.sortedByDescending { it.name.lowercase() }
            }

            else -> {
                originalList.sortedBy { it.name.lowercase() }
            }
        }

        return ArrayList(sortedList)
    }

    private fun showAddFolderDialog() {
        val input = EditText(this)
        input.hint = "Folder name"
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("New Folder")
            .setMessage("Enter folder name")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val folderName = input.text.toString().trim()

                if (folderName.isEmpty()) {
                    Toast.makeText(this, "Folder name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.addFolder(folderName)

                if (result != -1L) {
                    Toast.makeText(this, "Folder added", Toast.LENGTH_SHORT).show()
                    loadFolders()
                } else {
                    Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditFolderDialog(folder: Folder) {
        val input = EditText(this)
        input.setText(folder.name)
        input.setSelection(input.text.length)
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("Edit Folder")
            .setMessage("Update folder name")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newFolderName = input.text.toString().trim()

                if (newFolderName.isEmpty()) {
                    Toast.makeText(this, "Folder name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.updateFolder(folder.id, newFolderName)

                if (result > 0) {
                    Toast.makeText(this, "Folder updated", Toast.LENGTH_SHORT).show()
                    loadFolders()
                } else {
                    Toast.makeText(this, "Failed to update folder", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteFolderDialog(folder: Folder) {
        AlertDialog.Builder(this)
            .setTitle("Delete Folder")
            .setMessage("Delete \"${folder.name}\"? Notes inside this folder will not be deleted. They will only be removed from this folder.")
            .setPositiveButton("Delete") { _, _ ->
                val result = databaseHelper.deleteFolder(folder.id)

                if (result > 0) {
                    Toast.makeText(this, "Folder deleted", Toast.LENGTH_SHORT).show()
                    loadFolders()
                } else {
                    Toast.makeText(this, "Failed to delete folder", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
package com.example.noteappux

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesActivity : AppCompatActivity() {

    private lateinit var btnNewCategory: TextView
    private lateinit var btnSortCategories: TextView
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var tvEmptyCategories: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: ArrayList<Category>

    private var currentSortMode: Int = SORT_A_TO_Z

    companion object {
        private const val PREF_NAME = "categories_sort_pref"
        private const val PREF_SORT_MODE = "sort_mode"

        private const val SORT_NEWEST_FIRST = 0
        private const val SORT_OLDEST_FIRST = 1
        private const val SORT_A_TO_Z = 2
        private const val SORT_Z_TO_A = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        btnNewCategory = findViewById(R.id.btnNewCategory)
        btnSortCategories = findViewById(R.id.btnSortCategories)
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories)
        tvEmptyCategories = findViewById(R.id.tvEmptyCategories)

        databaseHelper = DatabaseHelper(this)

        currentSortMode = getSavedSortMode()
        updateSortButtonText()

        recyclerViewCategories.layoutManager = LinearLayoutManager(this)

        btnNewCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        btnSortCategories.setOnClickListener {
            showSortDialog()
        }

        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
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
        btnSortCategories.text = when (currentSortMode) {
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
            .setTitle("Sort Categories")
            .setSingleChoiceItems(sortOptions, currentSortMode) { dialog, which ->
                currentSortMode = which
                saveSortMode(currentSortMode)
                updateSortButtonText()
                loadCategories()

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

    private fun loadCategories() {
        categoryList = databaseHelper.getAllCategories()
        categoryList = sortCategories(categoryList)

        if (categoryList.isEmpty()) {
            recyclerViewCategories.visibility = View.GONE
            tvEmptyCategories.visibility = View.VISIBLE
        } else {
            recyclerViewCategories.visibility = View.VISIBLE
            tvEmptyCategories.visibility = View.GONE
        }

        categoryAdapter = CategoryAdapter(
            categoryList,
            onCategoryClick = { selectedCategory ->
                val intent = Intent(this, CategoryNotesActivity::class.java)
                intent.putExtra("category_id", selectedCategory.id)
                intent.putExtra("category_name", selectedCategory.name)
                startActivity(intent)
            },
            onEditCategoryClick = { selectedCategory ->
                showEditCategoryDialog(selectedCategory)
            },
            onDeleteCategoryClick = { selectedCategory ->
                showDeleteCategoryDialog(selectedCategory)
            }
        )

        recyclerViewCategories.adapter = categoryAdapter
    }

    private fun sortCategories(originalList: ArrayList<Category>): ArrayList<Category> {
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

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Category name"
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setMessage("Enter category name")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = input.text.toString().trim()

                if (categoryName.isEmpty()) {
                    Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.addCategory(categoryName)

                if (result != -1L) {
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCategoryDialog(category: Category) {
        val input = EditText(this)
        input.setText(category.name)
        input.setSelection(input.text.length)
        input.setSingleLine(true)
        input.setPadding(40, 25, 40, 25)
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        input.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint))

        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setMessage("Update category name")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newCategoryName = input.text.toString().trim()

                if (newCategoryName.isEmpty()) {
                    Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = databaseHelper.updateCategory(category.id, newCategoryName)

                if (result > 0) {
                    Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteCategoryDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete \"${category.name}\"? Notes inside this category will not be deleted. They will only be removed from this category.")
            .setPositiveButton("Delete") { _, _ ->
                val result = databaseHelper.deleteCategory(category.id)

                if (result > 0) {
                    Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
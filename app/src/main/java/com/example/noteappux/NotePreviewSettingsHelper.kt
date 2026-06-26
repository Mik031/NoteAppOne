package com.example.noteappux

import android.content.Context

object NotePreviewSettingsHelper {

    private const val PREF_NAME = "note_preview_settings"

    private const val KEY_SHOW_IMAGES = "show_images_preview"
    private const val KEY_SHOW_FILES = "show_files_preview"
    private const val KEY_SHOW_LINKS = "show_links_preview"

    fun isImagePreviewEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_SHOW_IMAGES, true)
    }

    fun isFilePreviewEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_SHOW_FILES, true)
    }

    fun isLinkPreviewEnabled(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_SHOW_LINKS, true)
    }

    fun setImagePreviewEnabled(context: Context, enabled: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_IMAGES, enabled)
            .apply()
    }

    fun setFilePreviewEnabled(context: Context, enabled: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_FILES, enabled)
            .apply()
    }

    fun setLinkPreviewEnabled(context: Context, enabled: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_LINKS, enabled)
            .apply()
    }
}
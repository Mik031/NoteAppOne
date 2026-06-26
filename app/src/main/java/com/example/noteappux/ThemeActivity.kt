package com.example.noteappux

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ThemeActivity : AppCompatActivity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var rbLightMode: RadioButton
    private lateinit var rbDarkMode: RadioButton
    private lateinit var rbFollowSystem: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        radioGroupTheme = findViewById(R.id.radioGroupTheme)
        rbLightMode = findViewById(R.id.rbLightMode)
        rbDarkMode = findViewById(R.id.rbDarkMode)
        rbFollowSystem = findViewById(R.id.rbFollowSystem)

        loadCurrentThemeSelection()

        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLightMode -> {
                    saveAndApplyTheme(ThemeHelper.THEME_LIGHT, "Light mode selected")
                }

                R.id.rbDarkMode -> {
                    saveAndApplyTheme(ThemeHelper.THEME_DARK, "Dark mode selected")
                }

                R.id.rbFollowSystem -> {
                    saveAndApplyTheme(ThemeHelper.THEME_SYSTEM, "Following phone system")
                }
            }
        }
    }

    private fun loadCurrentThemeSelection() {
        when (ThemeHelper.getThemeMode(this)) {
            ThemeHelper.THEME_LIGHT -> rbLightMode.isChecked = true
            ThemeHelper.THEME_DARK -> rbDarkMode.isChecked = true
            ThemeHelper.THEME_SYSTEM -> rbFollowSystem.isChecked = true
            else -> rbFollowSystem.isChecked = true
        }
    }

    private fun saveAndApplyTheme(themeMode: String, message: String) {
        ThemeHelper.saveThemeMode(this, themeMode)
        ThemeHelper.applyTheme(themeMode)

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
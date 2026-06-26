package com.example.noteappux

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object AccessibilityHelper {

    fun isTextExpansionServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(
            context,
            TextExpansionAccessibilityService::class.java
        )

        val expectedComponentNameString = expectedComponentName.flattenToString()

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServicesSetting)

        while (splitter.hasNext()) {
            val enabledService = splitter.next()

            if (enabledService.equals(expectedComponentNameString, ignoreCase = true)) {
                return true
            }
        }

        return false
    }
}
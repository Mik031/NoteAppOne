package com.example.noteappux

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.regex.Pattern

object LinkUtils {

    private val webUrlPattern: Pattern = Pattern.compile(
        "(?i)\\b((?:https?://|www\\.)[^\\s<>()\\[\\]{}\"']+)"
    )

    fun setupEditableWebLinks(
        context: Context,
        editText: EditText,
        onLinksChanged: (() -> Unit)? = null
    ) {
        editText.linksClickable = true
        editText.movementMethod = LinkMovementMethod.getInstance()
        editText.highlightColor = Color.TRANSPARENT

        var isApplyingLinks = false

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Not needed
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                if (isApplyingLinks || s == null) {
                    return
                }

                isApplyingLinks = true

                val selectionStart = editText.selectionStart
                val selectionEnd = editText.selectionEnd

                val linkedText = buildLinkedText(
                    context = context,
                    rawText = s.toString()
                )

                editText.setText(linkedText)

                val safeStart = selectionStart.coerceIn(0, editText.text.length)
                val safeEnd = selectionEnd.coerceIn(0, editText.text.length)

                try {
                    editText.setSelection(safeStart, safeEnd)
                } catch (e: Exception) {
                    editText.setSelection(editText.text.length)
                }

                isApplyingLinks = false

                onLinksChanged?.invoke()
            }
        })

        applyWebLinksNow(context, editText)
        onLinksChanged?.invoke()
    }

    fun setupReadOnlyWebLinks(context: Context, editText: EditText) {
        editText.linksClickable = true
        editText.movementMethod = LinkMovementMethod.getInstance()
        editText.highlightColor = Color.TRANSPARENT

        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isCursorVisible = false

        applyWebLinksNow(context, editText)
    }

    fun applyWebLinksNow(context: Context, editText: EditText) {
        val currentText = editText.text.toString()
        editText.setText(
            buildLinkedText(
                context = context,
                rawText = currentText
            )
        )
    }

    fun extractWebLinks(rawText: String): ArrayList<String> {
        val linkList = ArrayList<String>()
        val matcher = webUrlPattern.matcher(rawText)

        while (matcher.find()) {
            var start = matcher.start(1)
            var end = matcher.end(1)

            while (
                end > start &&
                rawText[end - 1] in listOf('.', ',', ')', ']', '}', '!', '?', ';', ':')
            ) {
                end--
            }

            if (end <= start) {
                continue
            }

            val cleanUrl = rawText.substring(start, end)

            if (!linkList.any { it.equals(cleanUrl, ignoreCase = true) }) {
                linkList.add(cleanUrl)
            }
        }

        return linkList
    }

    private fun buildLinkedText(
        context: Context,
        rawText: String
    ): SpannableString {
        val spannableString = SpannableString(rawText)
        val matcher = webUrlPattern.matcher(rawText)

        while (matcher.find()) {
            var start = matcher.start(1)
            var end = matcher.end(1)

            while (
                end > start &&
                rawText[end - 1] in listOf('.', ',', ')', ']', '}', '!', '?', ';', ':')
            ) {
                end--
            }

            if (end <= start) {
                continue
            }

            val cleanUrl = rawText.substring(start, end)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openWebUrl(
                        context = context,
                        url = cleanUrl
                    )
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)

                    ds.color = ContextCompat.getColor(
                        context,
                        R.color.primary_blue
                    )

                    ds.isUnderlineText = true
                }
            }

            spannableString.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }

    fun openWebUrl(context: Context, url: String) {
        val finalUrl = if (
            url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)
        ) {
            url
        } else {
            "https://$url"
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No browser found to open this link",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to open link",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
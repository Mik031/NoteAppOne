package com.example.noteappux

import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class NoteImageViewerActivity : AppCompatActivity() {

    private lateinit var imgFullNoteImage: ImageView
    private lateinit var btnCloseImageViewer: TextView
    private lateinit var tvImageCounter: TextView
    private lateinit var btnPreviousImage: TextView
    private lateinit var btnNextImage: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var imageList: ArrayList<NoteImage>

    private lateinit var gestureDetector: GestureDetector

    private var noteId: Int = -1
    private var currentPosition: Int = 0
    private var imageMode: String = "single_note"

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_image_viewer)

        imgFullNoteImage = findViewById(R.id.imgFullNoteImage)
        btnCloseImageViewer = findViewById(R.id.btnCloseImageViewer)
        tvImageCounter = findViewById(R.id.tvImageCounter)
        btnPreviousImage = findViewById(R.id.btnPreviousImage)
        btnNextImage = findViewById(R.id.btnNextImage)

        databaseHelper = DatabaseHelper(this)

        noteId = intent.getIntExtra("note_id", -1)
        currentPosition = intent.getIntExtra("selected_position", 0)
        imageMode = intent.getStringExtra("image_mode") ?: "single_note"

        setupSwipeGesture()
        loadImages()

        btnCloseImageViewer.setOnClickListener {
            finish()
        }

        btnPreviousImage.setOnClickListener {
            showPreviousImage()
        }

        btnNextImage.setOnClickListener {
            showNextImage()
        }

        imgFullNoteImage.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupSwipeGesture() {
        gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) {
                        return false
                    }

                    val diffX = e2.x - e1.x
                    val diffY = e2.y - e1.y

                    val swipeDistance = 80
                    val swipeVelocity = 80

                    if (
                        abs(diffX) > abs(diffY) &&
                        abs(diffX) > swipeDistance &&
                        abs(velocityX) > swipeVelocity
                    ) {
                        if (diffX > 0) {
                            showPreviousImage()
                        } else {
                            showNextImage()
                        }

                        return true
                    }

                    return false
                }
            }
        )
    }

    private fun loadImages() {
        imageList = when (imageMode) {
            "all_attachments" -> {
                databaseHelper.getAllActiveNoteImages()
            }

            else -> {
                if (noteId == -1) {
                    Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                databaseHelper.getImagesForNote(noteId)
            }
        }

        if (imageList.isEmpty()) {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (currentPosition < 0) {
            currentPosition = 0
        }

        if (currentPosition >= imageList.size) {
            currentPosition = imageList.size - 1
        }

        showCurrentImage()
    }

    private fun showCurrentImage() {
        val currentImage = imageList[currentPosition]

        imgFullNoteImage.setImageURI(Uri.parse(currentImage.imageUri))

        tvImageCounter.text = "${currentPosition + 1} / ${imageList.size}"

        btnPreviousImage.visibility = if (currentPosition == 0) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        btnNextImage.visibility = if (currentPosition == imageList.size - 1) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    private fun showPreviousImage() {
        if (currentPosition > 0) {
            currentPosition--
            showCurrentImage()
        }
    }

    private fun showNextImage() {
        if (currentPosition < imageList.size - 1) {
            currentPosition++
            showCurrentImage()
        }
    }
}
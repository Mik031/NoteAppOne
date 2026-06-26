package com.example.noteappux

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import java.io.File

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notes_app.db"
        private const val DATABASE_VERSION = 16

        // Notes table
        private const val TABLE_NOTES = "notes"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IS_DELETED = "is_deleted"
        private const val COLUMN_DELETED_DATE = "deleted_date"
        private const val COLUMN_IS_PINNED = "is_pinned"
        private const val COLUMN_FOLDER_ID = "folder_id"
        private const val COLUMN_IS_LOCKED = "is_locked"
        private const val COLUMN_PASSCODE_HASH = "passcode_hash"
        private const val COLUMN_LOCK_TYPE = "lock_type"
        private const val COLUMN_SHOW_TITLE_WHEN_LOCKED = "show_title_when_locked"
        private const val COLUMN_SHORTCUT_ENABLED = "shortcut_enabled"
        private const val COLUMN_SHORTCUT_KEYWORD = "shortcut_keyword"

        // Note Shortcuts table
        private const val TABLE_NOTE_SHORTCUTS = "note_shortcuts"

        private const val COLUMN_SHORTCUT_ID = "id"
        private const val COLUMN_SHORTCUT_NOTE_ID = "note_id"
        private const val COLUMN_SHORTCUT_KEYWORD_NEW = "shortcut_keyword"
        private const val COLUMN_SHORTCUT_CREATED_DATE = "created_date"

        private const val MAX_SHORTCUTS_PER_NOTE = 4

//        note mode

        private const val COLUMN_NOTE_MODE = "note_mode"

        const val NOTE_MODE_CLASSIC = "classic"
        const val NOTE_MODE_CHAT = "chat"

        // Note Messages table for Chat Note Mode
        private const val TABLE_NOTE_MESSAGES = "note_messages"

        private const val COLUMN_MESSAGE_ID = "id"
        private const val COLUMN_MESSAGE_NOTE_ID = "note_id"
        private const val COLUMN_MESSAGE_TYPE = "message_type"
        private const val COLUMN_MESSAGE_TEXT = "text_content"
        private const val COLUMN_MESSAGE_IMAGE_URI = "image_uri"
        private const val COLUMN_MESSAGE_FILE_NAME = "file_name"
        private const val COLUMN_MESSAGE_FILE_URI = "file_uri"
        private const val COLUMN_MESSAGE_LINK_URL = "link_url"
        private const val COLUMN_MESSAGE_CREATED_AT = "created_at"
        private const val COLUMN_MESSAGE_ORDER = "message_order"

        const val MESSAGE_TYPE_TEXT = "TEXT"
        const val MESSAGE_TYPE_IMAGE = "IMAGE"
        const val MESSAGE_TYPE_FILE = "FILE"
        const val MESSAGE_TYPE_LINK = "LINK"

        // Note Bullets table
        private const val TABLE_NOTE_BULLETS = "note_bullets"

        private const val COLUMN_BULLET_ID = "id"
        private const val COLUMN_BULLET_NOTE_ID = "note_id"
        private const val COLUMN_BULLET_TEXT = "bullet_text"
        private const val COLUMN_BULLET_TYPE = "bullet_type"

        private const val COLUMN_BULLET_ORDER = "bullet_order"
        private const val COLUMN_BULLET_IS_TASK = "is_task"
        private const val COLUMN_BULLET_IS_COMPLETED = "is_completed"
        private const val COLUMN_BULLET_CREATED_AT = "created_at"

        const val BULLET_TYPE_NONE = "NONE"
        const val BULLET_TYPE_DOT = "DOT"
        const val BULLET_TYPE_DASH = "DASH"
        const val BULLET_TYPE_NUMBER = "NUMBER"

        // Lock type values
        const val LOCK_NONE = 0
        const val LOCK_PASSCODE_ONLY = 1
        const val LOCK_FINGERPRINT_ONLY = 2
        const val LOCK_BOTH = 3

        // Note Images table
        private const val TABLE_NOTE_IMAGES = "note_images"

        private const val COLUMN_IMAGE_ID = "id"
        private const val COLUMN_IMAGE_NOTE_ID = "note_id"
        private const val COLUMN_IMAGE_URI = "image_uri"
        private const val COLUMN_IMAGE_CREATED_DATE = "created_date"

        // Note Files table
        private const val TABLE_NOTE_FILES = "note_files"

        private const val COLUMN_FILE_ID = "id"
        private const val COLUMN_FILE_NOTE_ID = "note_id"
        private const val COLUMN_FILE_NAME = "file_name"
        private const val COLUMN_FILE_URI = "file_uri"
        private const val COLUMN_FILE_CREATED_DATE = "created_date"

        // Categories table
        private const val TABLE_CATEGORIES = "categories"

        private const val COLUMN_CATEGORY_ID = "id"
        private const val COLUMN_CATEGORY_NAME = "name"

        // Note-Categories relation table
        private const val TABLE_NOTE_CATEGORIES = "note_categories"

        private const val COLUMN_RELATION_ID = "id"
        private const val COLUMN_NOTE_ID = "note_id"
        private const val COLUMN_RELATION_CATEGORY_ID = "category_id"

        // Folders table
        private const val TABLE_FOLDERS = "folders"

        private const val COLUMN_FOLDER_TABLE_ID = "id"
        private const val COLUMN_FOLDER_NAME = "name"

        // TEST MODE: 1 minute
        private const val AUTO_DELETE_TIME = 1 * 60 * 1000L

        // Later for 30 days, change it to:
        // private const val AUTO_DELETE_TIME = 30L * 24L * 60L * 60L * 1000L
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNotesTable = """
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_IS_DELETED INTEGER DEFAULT 0,
                $COLUMN_DELETED_DATE INTEGER DEFAULT 0,
                $COLUMN_IS_PINNED INTEGER DEFAULT 0,
                $COLUMN_FOLDER_ID INTEGER DEFAULT 0,
                $COLUMN_IS_LOCKED INTEGER DEFAULT 0,
                $COLUMN_PASSCODE_HASH TEXT DEFAULT '',
                $COLUMN_LOCK_TYPE INTEGER DEFAULT 0,
                $COLUMN_SHOW_TITLE_WHEN_LOCKED INTEGER DEFAULT 0,
                $COLUMN_SHORTCUT_ENABLED INTEGER DEFAULT 0,
                $COLUMN_SHORTCUT_KEYWORD TEXT DEFAULT '',
                $COLUMN_NOTE_MODE TEXT DEFAULT '$NOTE_MODE_CLASSIC'
            )
        """.trimIndent()

        val createNoteBulletsTable = """
            CREATE TABLE $TABLE_NOTE_BULLETS (
                $COLUMN_BULLET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_BULLET_NOTE_ID INTEGER NOT NULL,
                $COLUMN_BULLET_TEXT TEXT NOT NULL,
                $COLUMN_BULLET_TYPE TEXT NOT NULL,
                $COLUMN_BULLET_ORDER INTEGER DEFAULT 0,
                $COLUMN_BULLET_IS_TASK INTEGER DEFAULT 1,
                $COLUMN_BULLET_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_BULLET_CREATED_AT INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createNoteImagesTable = """
            CREATE TABLE $TABLE_NOTE_IMAGES (
                $COLUMN_IMAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IMAGE_NOTE_ID INTEGER NOT NULL,
                $COLUMN_IMAGE_URI TEXT NOT NULL,
                $COLUMN_IMAGE_CREATED_DATE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createNoteFilesTable = """
            CREATE TABLE $TABLE_NOTE_FILES (
                $COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FILE_NOTE_ID INTEGER NOT NULL,
                $COLUMN_FILE_NAME TEXT NOT NULL,
                $COLUMN_FILE_URI TEXT NOT NULL,
                $COLUMN_FILE_CREATED_DATE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createNoteShortcutsTable = """
            CREATE TABLE $TABLE_NOTE_SHORTCUTS (
                $COLUMN_SHORTCUT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SHORTCUT_NOTE_ID INTEGER NOT NULL,
                $COLUMN_SHORTCUT_KEYWORD_NEW TEXT NOT NULL,
                $COLUMN_SHORTCUT_CREATED_DATE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createNoteMessagesTable = """
            CREATE TABLE $TABLE_NOTE_MESSAGES (
                $COLUMN_MESSAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MESSAGE_NOTE_ID INTEGER NOT NULL,
                $COLUMN_MESSAGE_TYPE TEXT NOT NULL,
                $COLUMN_MESSAGE_TEXT TEXT DEFAULT '',
                $COLUMN_MESSAGE_IMAGE_URI TEXT DEFAULT '',
                $COLUMN_MESSAGE_FILE_NAME TEXT DEFAULT '',
                $COLUMN_MESSAGE_FILE_URI TEXT DEFAULT '',
                $COLUMN_MESSAGE_LINK_URL TEXT DEFAULT '',
                $COLUMN_MESSAGE_CREATED_AT INTEGER DEFAULT 0,
                $COLUMN_MESSAGE_ORDER INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL UNIQUE
            )
        """.trimIndent()

        val createNoteCategoriesTable = """
            CREATE TABLE $TABLE_NOTE_CATEGORIES (
                $COLUMN_RELATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOTE_ID INTEGER NOT NULL,
                $COLUMN_RELATION_CATEGORY_ID INTEGER NOT NULL
            )
        """.trimIndent()

        val createFoldersTable = """
            CREATE TABLE $TABLE_FOLDERS (
                $COLUMN_FOLDER_TABLE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FOLDER_NAME TEXT NOT NULL UNIQUE
            )
        """.trimIndent()

        db.execSQL(createNotesTable)
        db.execSQL(createNoteImagesTable)
        db.execSQL(createNoteFilesTable)
        db.execSQL(createNoteShortcutsTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createNoteCategoriesTable)
        db.execSQL(createFoldersTable)
        db.execSQL(createNoteMessagesTable)
        db.execSQL(createNoteBulletsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_DELETED_DATE INTEGER DEFAULT 0")
        }

        if (oldVersion < 4) {
            val createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                    $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CATEGORY_NAME TEXT NOT NULL UNIQUE
                )
            """.trimIndent()

            val createNoteCategoriesTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_NOTE_CATEGORIES (
                    $COLUMN_RELATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_NOTE_ID INTEGER NOT NULL,
                    $COLUMN_RELATION_CATEGORY_ID INTEGER NOT NULL
                )
            """.trimIndent()

            db.execSQL(createCategoriesTable)
            db.execSQL(createNoteCategoriesTable)
        }

        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_IS_PINNED INTEGER DEFAULT 0")
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_FOLDER_ID INTEGER DEFAULT 0")

            val createFoldersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_FOLDERS (
                    $COLUMN_FOLDER_TABLE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_FOLDER_NAME TEXT NOT NULL UNIQUE
                )
            """.trimIndent()

            db.execSQL(createFoldersTable)
        }

        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_IS_LOCKED INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_PASSCODE_HASH TEXT DEFAULT ''")
        }

        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_LOCK_TYPE INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_SHOW_TITLE_WHEN_LOCKED INTEGER DEFAULT 0")

            db.execSQL("""
                UPDATE $TABLE_NOTES
                SET $COLUMN_LOCK_TYPE = $LOCK_PASSCODE_ONLY
                WHERE $COLUMN_IS_LOCKED = 1
                AND ($COLUMN_LOCK_TYPE = 0 OR $COLUMN_LOCK_TYPE IS NULL)
            """.trimIndent())
        }

        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_SHORTCUT_ENABLED INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_SHORTCUT_KEYWORD TEXT DEFAULT ''")
        }

        if (oldVersion < 10) {
            val createNoteImagesTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_NOTE_IMAGES (
            $COLUMN_IMAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_IMAGE_NOTE_ID INTEGER NOT NULL,
            $COLUMN_IMAGE_URI TEXT NOT NULL,
            $COLUMN_IMAGE_CREATED_DATE INTEGER DEFAULT 0
        )
    """.trimIndent()

            db.execSQL(createNoteImagesTable)
        }
        if (oldVersion < 11) {
            val createNoteShortcutsTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_NOTE_SHORTCUTS (
            $COLUMN_SHORTCUT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_SHORTCUT_NOTE_ID INTEGER NOT NULL,
            $COLUMN_SHORTCUT_KEYWORD_NEW TEXT NOT NULL,
            $COLUMN_SHORTCUT_CREATED_DATE INTEGER DEFAULT 0
        )
    """.trimIndent()

            db.execSQL(createNoteShortcutsTable)

            // Migrate old one-shortcut system into the new multi-shortcut table
            db.execSQL("""
        INSERT INTO $TABLE_NOTE_SHORTCUTS (
            $COLUMN_SHORTCUT_NOTE_ID,
            $COLUMN_SHORTCUT_KEYWORD_NEW,
            $COLUMN_SHORTCUT_CREATED_DATE
        )
        SELECT
            $COLUMN_ID,
            $COLUMN_SHORTCUT_KEYWORD,
            ${System.currentTimeMillis()}
        FROM $TABLE_NOTES
        WHERE $COLUMN_SHORTCUT_ENABLED = 1
        AND $COLUMN_SHORTCUT_KEYWORD != ''
    """.trimIndent())
        }

        if (oldVersion < 12) {
            val createNoteFilesTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_NOTE_FILES (
            $COLUMN_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_FILE_NOTE_ID INTEGER NOT NULL,
            $COLUMN_FILE_NAME TEXT NOT NULL,
            $COLUMN_FILE_URI TEXT NOT NULL,
            $COLUMN_FILE_CREATED_DATE INTEGER DEFAULT 0
        )
    """.trimIndent()

            db.execSQL(createNoteFilesTable)
        }
        if (oldVersion < 13) {
            db.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_NOTE_MODE TEXT DEFAULT '$NOTE_MODE_CLASSIC'")

            val createNoteMessagesTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_NOTE_MESSAGES (
            $COLUMN_MESSAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_MESSAGE_NOTE_ID INTEGER NOT NULL,
            $COLUMN_MESSAGE_TYPE TEXT NOT NULL,
            $COLUMN_MESSAGE_TEXT TEXT DEFAULT '',
            $COLUMN_MESSAGE_IMAGE_URI TEXT DEFAULT '',
            $COLUMN_MESSAGE_FILE_NAME TEXT DEFAULT '',
            $COLUMN_MESSAGE_FILE_URI TEXT DEFAULT '',
            $COLUMN_MESSAGE_LINK_URL TEXT DEFAULT '',
            $COLUMN_MESSAGE_CREATED_AT INTEGER DEFAULT 0,
            $COLUMN_MESSAGE_ORDER INTEGER DEFAULT 0
        )
    """.trimIndent()

            db.execSQL(createNoteMessagesTable)
        }

        if (oldVersion < 14) {
            val createNoteBulletsTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_NOTE_BULLETS (
            $COLUMN_BULLET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_BULLET_NOTE_ID INTEGER NOT NULL,
            $COLUMN_BULLET_TEXT TEXT NOT NULL,
            $COLUMN_BULLET_TYPE TEXT NOT NULL,
            $COLUMN_BULLET_ORDER INTEGER DEFAULT 0,
            $COLUMN_BULLET_CREATED_AT INTEGER DEFAULT 0
        )
    """.trimIndent()

            db.execSQL(createNoteBulletsTable)
        }

        if (oldVersion < 15) {
            db.execSQL(
                "ALTER TABLE $TABLE_NOTE_BULLETS ADD COLUMN $COLUMN_BULLET_IS_COMPLETED INTEGER DEFAULT 0"
            )
        }

        if (oldVersion < 16) {
            db.execSQL(
                "ALTER TABLE $TABLE_NOTE_BULLETS ADD COLUMN $COLUMN_BULLET_IS_TASK INTEGER DEFAULT 1"
            )
        }
    }

    private fun cursorToNote(cursor: Cursor): Note {
        val noteMode = if (cursor.getColumnIndex(COLUMN_NOTE_MODE) != -1) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_MODE))
        } else {
            NOTE_MODE_CLASSIC
        }

        return Note(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
            isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DELETED)),
            deletedDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DELETED_DATE)),
            isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PINNED)),
            folderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID)),
            isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED)),
            passcodeHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSCODE_HASH)),
            lockType = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOCK_TYPE)),
            showTitleWhenLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHOW_TITLE_WHEN_LOCKED)),
            shortcutEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHORTCUT_ENABLED)),
            shortcutKeyword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SHORTCUT_KEYWORD)),
            noteMode = noteMode
        )
    }

    private fun cursorToNoteImage(cursor: Cursor): NoteImage {
        return NoteImage(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_ID)),
            noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_NOTE_ID)),
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
            createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_CREATED_DATE))
        )
    }

    private fun cursorToNoteFile(cursor: Cursor): NoteFile {
        return NoteFile(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILE_ID)),
            noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FILE_NOTE_ID)),
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_NAME)),
            fileUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_URI)),
            createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FILE_CREATED_DATE))
        )
    }

    private fun cursorToNoteMessage(cursor: Cursor): NoteMessage {
        return NoteMessage(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)),
            noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_NOTE_ID)),
            messageType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TYPE)),
            textContent = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TEXT)),
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_IMAGE_URI)),
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_FILE_NAME)),
            fileUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_FILE_URI)),
            linkUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_LINK_URL)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_CREATED_AT)),
            messageOrder = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ORDER))
        )
    }

    private fun cursorToNoteBullet(cursor: Cursor): NoteBullet {
        val isTask = if (cursor.getColumnIndex(COLUMN_BULLET_IS_TASK) != -1) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BULLET_IS_TASK))
        } else {
            1
        }

        val isCompleted = if (cursor.getColumnIndex(COLUMN_BULLET_IS_COMPLETED) != -1) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BULLET_IS_COMPLETED))
        } else {
            0
        }

        return NoteBullet(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BULLET_ID)),
            noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BULLET_NOTE_ID)),
            bulletText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BULLET_TEXT)),
            bulletType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BULLET_TYPE)),
            bulletOrder = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BULLET_ORDER)),
            isTask = isTask,
            isCompleted = isCompleted,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BULLET_CREATED_AT))
        )
    }

    private fun cursorToNoteShortcut(cursor: Cursor): NoteShortcut {
        return NoteShortcut(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHORTCUT_ID)),
            noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHORTCUT_NOTE_ID)),
            shortcutKeyword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SHORTCUT_KEYWORD_NEW)),
            createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SHORTCUT_CREATED_DATE)),
            noteTitle = if (cursor.getColumnIndex(COLUMN_TITLE) != -1) {
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            } else {
                ""
            },
            noteContent = if (cursor.getColumnIndex(COLUMN_CONTENT) != -1) {
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            } else {
                ""
            }
        )
    }

//    fun addNote(title: String, content: String, date: String): Long {
////        val db = writableDatabase
////
////        val values = ContentValues().apply {
////            put(COLUMN_TITLE, title)
////            put(COLUMN_CONTENT, content)
////            put(COLUMN_DATE, date)
////            put(COLUMN_IS_DELETED, 0)
////            put(COLUMN_DELETED_DATE, 0)
////            put(COLUMN_IS_PINNED, 0)
////            put(COLUMN_FOLDER_ID, 0)
////            put(COLUMN_IS_LOCKED, 0)
////            put(COLUMN_PASSCODE_HASH, "")
////            put(COLUMN_LOCK_TYPE, LOCK_NONE)
////            put(COLUMN_SHOW_TITLE_WHEN_LOCKED, 0)
////            put(COLUMN_SHORTCUT_ENABLED, 0)
////            put(COLUMN_SHORTCUT_KEYWORD, "")
////        }
////
////        val result = db.insert(TABLE_NOTES, null, values)
////        db.close()
////
////        return result
////    }

    fun addNote(
        title: String,
        content: String,
        date: String,
        noteMode: String = NOTE_MODE_CLASSIC
    ): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
            put(COLUMN_DATE, date)
            put(COLUMN_IS_DELETED, 0)
            put(COLUMN_DELETED_DATE, 0)
            put(COLUMN_IS_PINNED, 0)
            put(COLUMN_FOLDER_ID, 0)
            put(COLUMN_IS_LOCKED, 0)
            put(COLUMN_PASSCODE_HASH, "")
            put(COLUMN_LOCK_TYPE, LOCK_NONE)
            put(COLUMN_SHOW_TITLE_WHEN_LOCKED, 0)
            put(COLUMN_SHORTCUT_ENABLED, 0)
            put(COLUMN_SHORTCUT_KEYWORD, "")
            put(COLUMN_NOTE_MODE, noteMode)
        }

        val result = db.insert(TABLE_NOTES, null, values)
        db.close()

        return result
    }

    fun getAllNotes(): ArrayList<Note> {
        val notesList = ArrayList<Note>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_NOTES
            WHERE $COLUMN_IS_DELETED = 0
            ORDER BY $COLUMN_IS_PINNED DESC, $COLUMN_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                notesList.add(cursorToNote(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return notesList
    }

    fun getNoteById(noteId: Int): Note? {
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_NOTES
            WHERE $COLUMN_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        val note = if (cursor.moveToFirst()) {
            cursorToNote(cursor)
        } else {
            null
        }

        cursor.close()
        db.close()

        return note
    }

//    fun updateNote(id: Int, title: String, content: String, date: String): Int {
//        val db = writableDatabase
//
//        val values = ContentValues().apply {
//            put(COLUMN_TITLE, title)
//            put(COLUMN_CONTENT, content)
//            put(COLUMN_DATE, date)
//        }
//
//        val result = db.update(
//            TABLE_NOTES,
//            values,
//            "$COLUMN_ID = ?",
//            arrayOf(id.toString())
//        )
//
//        db.close()
//
//        return result
//    }

    fun updateNote(
        id: Int,
        title: String,
        content: String,
        date: String,
        noteMode: String = NOTE_MODE_CLASSIC
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
            put(COLUMN_DATE, date)
            put(COLUMN_NOTE_MODE, noteMode)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result
    }

    fun togglePinNote(id: Int, currentPinnedState: Int): Int {
        val db = writableDatabase

        val newPinnedState = if (currentPinnedState == 1) 0 else 1

        val values = ContentValues().apply {
            put(COLUMN_IS_PINNED, newPinnedState)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result
    }

    fun setNotePinned(id: Int, pinnedState: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IS_PINNED, pinnedState)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result
    }

    fun lockNote(noteId: Int, passcodeHash: String): Int {
        return setNotePrivacy(
            noteId = noteId,
            isLocked = 1,
            lockType = LOCK_PASSCODE_ONLY,
            passcodeHash = passcodeHash,
            showTitleWhenLocked = 0
        )
    }

    fun removeNoteLock(noteId: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IS_LOCKED, 0)
            put(COLUMN_PASSCODE_HASH, "")
            put(COLUMN_LOCK_TYPE, LOCK_NONE)
            put(COLUMN_SHOW_TITLE_WHEN_LOCKED, 0)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun updateNotePasscode(noteId: Int, newPasscodeHash: String): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_PASSCODE_HASH, newPasscodeHash)
            put(COLUMN_IS_LOCKED, 1)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun setNotePrivacy(
        noteId: Int,
        isLocked: Int,
        lockType: Int,
        passcodeHash: String,
        showTitleWhenLocked: Int
    ): Int {
        val db = writableDatabase

        val finalLockType = if (isLocked == 1) lockType else LOCK_NONE
        val finalPasscodeHash = if (isLocked == 1) passcodeHash else ""
        val finalShowTitle = if (isLocked == 1) showTitleWhenLocked else 0

        val values = ContentValues().apply {
            put(COLUMN_IS_LOCKED, isLocked)
            put(COLUMN_LOCK_TYPE, finalLockType)
            put(COLUMN_PASSCODE_HASH, finalPasscodeHash)
            put(COLUMN_SHOW_TITLE_WHEN_LOCKED, finalShowTitle)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun updateNoteLockType(noteId: Int, lockType: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_LOCK_TYPE, lockType)
            put(COLUMN_IS_LOCKED, if (lockType == LOCK_NONE) 0 else 1)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun updateShowTitleWhenLocked(noteId: Int, showTitleWhenLocked: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_SHOW_TITLE_WHEN_LOCKED, showTitleWhenLocked)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun getPasscodeHashForNote(noteId: Int): String {
        val db = readableDatabase

        var passcodeHash = ""

        val query = """
            SELECT $COLUMN_PASSCODE_HASH FROM $TABLE_NOTES
            WHERE $COLUMN_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            passcodeHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSCODE_HASH))
        }

        cursor.close()
        db.close()

        return passcodeHash
    }

    fun moveToRecentlyDeleted(id: Int): Int {
        val db = writableDatabase
        val currentTime = System.currentTimeMillis()

        val values = ContentValues().apply {
            put(COLUMN_IS_DELETED, 1)
            put(COLUMN_DELETED_DATE, currentTime)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result
    }

    fun getDeletedNotes(): ArrayList<Note> {
        val deletedNotesList = ArrayList<Note>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_NOTES
            WHERE $COLUMN_IS_DELETED = 1
            ORDER BY $COLUMN_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                deletedNotesList.add(cursorToNote(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return deletedNotesList
    }

    fun restoreNote(id: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IS_DELETED, 0)
            put(COLUMN_DELETED_DATE, 0)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result
    }

    private fun deleteLocalFileFromUri(fileUriString: String) {
        try {
            val uri = Uri.parse(fileUriString)

            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")

                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun deleteNoteForever(id: Int): Int {
        val filesToDelete = getFilesForNote(id)

        for (noteFile in filesToDelete) {
            deleteLocalFileFromUri(noteFile.fileUri)
        }

        val db = writableDatabase

        db.delete(
            TABLE_NOTE_CATEGORIES,
            "$COLUMN_NOTE_ID = ?",
            arrayOf(id.toString())
        )

        db.delete(
            TABLE_NOTE_IMAGES,
            "$COLUMN_IMAGE_NOTE_ID = ?",
            arrayOf(id.toString())
        )

        db.delete(
            TABLE_NOTE_FILES,
            "$COLUMN_FILE_NOTE_ID = ?",
            arrayOf(id.toString())
        )

        db.delete(
            TABLE_NOTE_SHORTCUTS,
            "$COLUMN_SHORTCUT_NOTE_ID = ?",
            arrayOf(id.toString())
        )

        db.delete(
            TABLE_NOTE_MESSAGES,
            "$COLUMN_MESSAGE_NOTE_ID = ?",
            arrayOf(id.toString())
        )

        db.delete(
            TABLE_NOTE_BULLETS,
            "$COLUMN_BULLET_NOTE_ID = ?",
            arrayOf(id.toString())
        )

        val result = db.delete(
            TABLE_NOTES,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result
    }

    fun deleteOldRecentlyDeletedNotes(): Int {
        val db = writableDatabase

        val currentTime = System.currentTimeMillis()
        val deleteBeforeTime = currentTime - AUTO_DELETE_TIME

        val oldDeletedNotes = ArrayList<Int>()

        val query = """
            SELECT $COLUMN_ID FROM $TABLE_NOTES
            WHERE $COLUMN_IS_DELETED = 1
            AND $COLUMN_DELETED_DATE > 0
            AND $COLUMN_DELETED_DATE <= ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(deleteBeforeTime.toString()))

        if (cursor.moveToFirst()) {
            do {
                oldDeletedNotes.add(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)))
            } while (cursor.moveToNext())
        }

        cursor.close()

        var deletedCount = 0


        for (noteId in oldDeletedNotes) {
            db.delete(
                TABLE_NOTE_CATEGORIES,
                "$COLUMN_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            db.delete(
                TABLE_NOTE_IMAGES,
                "$COLUMN_IMAGE_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            db.delete(
                TABLE_NOTE_FILES,
                "$COLUMN_FILE_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            db.delete(
                TABLE_NOTE_SHORTCUTS,
                "$COLUMN_SHORTCUT_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            db.delete(
                TABLE_NOTE_MESSAGES,
                "$COLUMN_MESSAGE_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            db.delete(
                TABLE_NOTE_BULLETS,
                "$COLUMN_BULLET_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            deletedCount += db.delete(
                TABLE_NOTES,
                "$COLUMN_ID = ?",
                arrayOf(noteId.toString())
            )
        }

        db.close()

        return deletedCount
    }

    fun updateNoteShortcut(
        noteId: Int,
        shortcutEnabled: Int,
        shortcutKeyword: String
    ): Int {
        val db = writableDatabase

        val cleanedKeyword = shortcutKeyword.trim()

        val values = ContentValues().apply {
            put(COLUMN_SHORTCUT_ENABLED, shortcutEnabled)
            put(COLUMN_SHORTCUT_KEYWORD, cleanedKeyword)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun removeNoteShortcut(noteId: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_SHORTCUT_ENABLED, 0)
            put(COLUMN_SHORTCUT_KEYWORD, "")
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun getEnabledShortcutNotes(): ArrayList<Note> {
        val shortcutNotes = ArrayList<Note>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTES
        WHERE $COLUMN_IS_DELETED = 0
        AND $COLUMN_IS_LOCKED = 0
        AND $COLUMN_SHORTCUT_ENABLED = 1
        AND $COLUMN_SHORTCUT_KEYWORD != ''
        ORDER BY $COLUMN_SHORTCUT_KEYWORD ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                shortcutNotes.add(cursorToNote(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return shortcutNotes
    }

    fun getShortcutNoteByKeyword(keyword: String): Note? {
        val db = readableDatabase

        val cleanedKeyword = keyword.trim()

        val query = """
        SELECT * FROM $TABLE_NOTES
        WHERE $COLUMN_IS_DELETED = 0
        AND $COLUMN_IS_LOCKED = 0
        AND $COLUMN_SHORTCUT_ENABLED = 1
        AND LOWER($COLUMN_SHORTCUT_KEYWORD) = LOWER(?)
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(cleanedKeyword))

        val note = if (cursor.moveToFirst()) {
            cursorToNote(cursor)
        } else {
            null
        }

        cursor.close()
        db.close()

        return note
    }

    // ---------------------------
// MULTI TEXT SHORTCUT FUNCTIONS
// ---------------------------

    private fun cleanShortcutKeyword(keyword: String): String {
        val cleanedKeyword = keyword.trim()

        return if (cleanedKeyword.startsWith("/")) {
            cleanedKeyword
        } else {
            "/$cleanedKeyword"
        }
    }

    fun getShortcutsForNote(noteId: Int): ArrayList<NoteShortcut> {
        val shortcutList = ArrayList<NoteShortcut>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_SHORTCUTS
        WHERE $COLUMN_SHORTCUT_NOTE_ID = ?
        ORDER BY $COLUMN_SHORTCUT_ID ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                shortcutList.add(cursorToNoteShortcut(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return shortcutList
    }

    fun getShortcutCountForNote(noteId: Int): Int {
        val db = readableDatabase

        val query = """
        SELECT COUNT(*) AS shortcut_count
        FROM $TABLE_NOTE_SHORTCUTS
        WHERE $COLUMN_SHORTCUT_NOTE_ID = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        val count = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("shortcut_count"))
        } else {
            0
        }

        cursor.close()
        db.close()

        return count
    }

    fun isShortcutKeywordTaken(
        keyword: String,
        ignoreShortcutId: Int = -1
    ): Boolean {
        val db = readableDatabase
        val cleanedKeyword = cleanShortcutKeyword(keyword)

        val query: String
        val args: Array<String>

        if (ignoreShortcutId == -1) {
            query = """
            SELECT $COLUMN_SHORTCUT_ID
            FROM $TABLE_NOTE_SHORTCUTS
            WHERE LOWER($COLUMN_SHORTCUT_KEYWORD_NEW) = LOWER(?)
            LIMIT 1
        """.trimIndent()

            args = arrayOf(cleanedKeyword)
        } else {
            query = """
            SELECT $COLUMN_SHORTCUT_ID
            FROM $TABLE_NOTE_SHORTCUTS
            WHERE LOWER($COLUMN_SHORTCUT_KEYWORD_NEW) = LOWER(?)
            AND $COLUMN_SHORTCUT_ID != ?
            LIMIT 1
        """.trimIndent()

            args = arrayOf(cleanedKeyword, ignoreShortcutId.toString())
        }

        val cursor = db.rawQuery(query, args)
        val isTaken = cursor.moveToFirst()

        cursor.close()
        db.close()

        return isTaken
    }

    fun addShortcutToNote(noteId: Int, keyword: String): Long {
        val cleanedKeyword = cleanShortcutKeyword(keyword)

        if (cleanedKeyword.length < 2) {
            return -1L
        }

        if (getShortcutCountForNote(noteId) >= MAX_SHORTCUTS_PER_NOTE) {
            return -2L
        }

        if (isShortcutKeywordTaken(cleanedKeyword)) {
            return -3L
        }

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_SHORTCUT_NOTE_ID, noteId)
            put(COLUMN_SHORTCUT_KEYWORD_NEW, cleanedKeyword)
            put(COLUMN_SHORTCUT_CREATED_DATE, System.currentTimeMillis())
        }

        val result = db.insert(TABLE_NOTE_SHORTCUTS, null, values)

        db.close()

        return result
    }

    fun updateNoteShortcutKeyword(shortcutId: Int, newKeyword: String): Int {
        val cleanedKeyword = cleanShortcutKeyword(newKeyword)

        if (cleanedKeyword.length < 2) {
            return -1
        }

        if (isShortcutKeywordTaken(cleanedKeyword, shortcutId)) {
            return -3
        }

        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_SHORTCUT_KEYWORD_NEW, cleanedKeyword)
        }

        val result = db.update(
            TABLE_NOTE_SHORTCUTS,
            values,
            "$COLUMN_SHORTCUT_ID = ?",
            arrayOf(shortcutId.toString())
        )

        db.close()

        return result
    }

    fun deleteShortcutById(shortcutId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_SHORTCUTS,
            "$COLUMN_SHORTCUT_ID = ?",
            arrayOf(shortcutId.toString())
        )

        db.close()

        return result
    }

    fun deleteAllShortcutsForNote(noteId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_SHORTCUTS,
            "$COLUMN_SHORTCUT_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun getAllActiveShortcuts(): ArrayList<NoteShortcut> {
        val shortcutList = ArrayList<NoteShortcut>()
        val db = readableDatabase

        val query = """
        SELECT 
            s.$COLUMN_SHORTCUT_ID,
            s.$COLUMN_SHORTCUT_NOTE_ID,
            s.$COLUMN_SHORTCUT_KEYWORD_NEW,
            s.$COLUMN_SHORTCUT_CREATED_DATE,
            n.$COLUMN_TITLE,
            n.$COLUMN_CONTENT
        FROM $TABLE_NOTE_SHORTCUTS s
        INNER JOIN $TABLE_NOTES n
        ON s.$COLUMN_SHORTCUT_NOTE_ID = n.$COLUMN_ID
        WHERE n.$COLUMN_IS_DELETED = 0
        AND n.$COLUMN_IS_LOCKED = 0
        ORDER BY s.$COLUMN_SHORTCUT_KEYWORD_NEW ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                shortcutList.add(cursorToNoteShortcut(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return shortcutList
    }

    fun getShortcutByKeyword(keyword: String): NoteShortcut? {
        val db = readableDatabase
        val cleanedKeyword = cleanShortcutKeyword(keyword)

        val query = """
        SELECT 
            s.$COLUMN_SHORTCUT_ID,
            s.$COLUMN_SHORTCUT_NOTE_ID,
            s.$COLUMN_SHORTCUT_KEYWORD_NEW,
            s.$COLUMN_SHORTCUT_CREATED_DATE,
            n.$COLUMN_TITLE,
            n.$COLUMN_CONTENT
        FROM $TABLE_NOTE_SHORTCUTS s
        INNER JOIN $TABLE_NOTES n
        ON s.$COLUMN_SHORTCUT_NOTE_ID = n.$COLUMN_ID
        WHERE n.$COLUMN_IS_DELETED = 0
        AND n.$COLUMN_IS_LOCKED = 0
        AND LOWER(s.$COLUMN_SHORTCUT_KEYWORD_NEW) = LOWER(?)
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(cleanedKeyword))

        val shortcut = if (cursor.moveToFirst()) {
            cursorToNoteShortcut(cursor)
        } else {
            null
        }

        cursor.close()
        db.close()

        return shortcut
    }

    // ---------------------------
    // CATEGORY FUNCTIONS
    // ---------------------------

    fun addCategory(name: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, name.trim())
        }

        val result = db.insertWithOnConflict(
            TABLE_CATEGORIES,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        db.close()

        return result
    }

    fun getAllCategories(): ArrayList<Category> {
        val categoryList = ArrayList<Category>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_CATEGORIES
            ORDER BY $COLUMN_CATEGORY_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                )

                categoryList.add(category)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return categoryList
    }

    fun deleteCategory(categoryId: Int): Int {
        val db = writableDatabase

        db.delete(
            TABLE_NOTE_CATEGORIES,
            "$COLUMN_RELATION_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )

        val result = db.delete(
            TABLE_CATEGORIES,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )

        db.close()

        return result
    }

    fun updateCategory(categoryId: Int, newName: String): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, newName.trim())
        }

        val result = db.update(
            TABLE_CATEGORIES,
            values,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )

        db.close()

        return result
    }

    fun assignCategoriesToNote(noteId: Int, categoryIds: ArrayList<Int>) {
        val db = writableDatabase

        db.delete(
            TABLE_NOTE_CATEGORIES,
            "$COLUMN_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        for (categoryId in categoryIds) {
            val values = ContentValues().apply {
                put(COLUMN_NOTE_ID, noteId)
                put(COLUMN_RELATION_CATEGORY_ID, categoryId)
            }

            db.insert(TABLE_NOTE_CATEGORIES, null, values)
        }

        db.close()
    }

    fun getCategoryIdsForNote(noteId: Int): ArrayList<Int> {
        val categoryIds = ArrayList<Int>()
        val db = readableDatabase

        val query = """
            SELECT $COLUMN_RELATION_CATEGORY_ID FROM $TABLE_NOTE_CATEGORIES
            WHERE $COLUMN_NOTE_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                categoryIds.add(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RELATION_CATEGORY_ID))
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return categoryIds
    }

    fun getCategoryNamesForNote(noteId: Int): ArrayList<String> {
        val categoryNames = ArrayList<String>()
        val db = readableDatabase

        val query = """
            SELECT c.$COLUMN_CATEGORY_NAME
            FROM $TABLE_CATEGORIES c
            INNER JOIN $TABLE_NOTE_CATEGORIES nc
            ON c.$COLUMN_CATEGORY_ID = nc.$COLUMN_RELATION_CATEGORY_ID
            WHERE nc.$COLUMN_NOTE_ID = ?
            ORDER BY c.$COLUMN_CATEGORY_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                categoryNames.add(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return categoryNames
    }

    fun getNotesByCategory(categoryId: Int): ArrayList<Note> {
        val notesList = ArrayList<Note>()
        val db = readableDatabase

        val query = """
            SELECT n.*
            FROM $TABLE_NOTES n
            INNER JOIN $TABLE_NOTE_CATEGORIES nc
            ON n.$COLUMN_ID = nc.$COLUMN_NOTE_ID
            WHERE nc.$COLUMN_RELATION_CATEGORY_ID = ?
            AND n.$COLUMN_IS_DELETED = 0
            ORDER BY n.$COLUMN_IS_PINNED DESC, n.$COLUMN_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))

        if (cursor.moveToFirst()) {
            do {
                notesList.add(cursorToNote(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return notesList
    }

    // ---------------------------
    // FOLDER FUNCTIONS
    // ---------------------------

    fun addFolder(name: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_FOLDER_NAME, name.trim())
        }

        val result = db.insertWithOnConflict(
            TABLE_FOLDERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        db.close()

        return result
    }

    fun getAllFolders(): ArrayList<Folder> {
        val folderList = ArrayList<Folder>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_FOLDERS
            ORDER BY $COLUMN_FOLDER_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val folder = Folder(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_TABLE_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME))
                )

                folderList.add(folder)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return folderList
    }

    fun deleteFolder(folderId: Int): Int {
        val db = writableDatabase

        val clearValues = ContentValues().apply {
            put(COLUMN_FOLDER_ID, 0)
        }

        db.update(
            TABLE_NOTES,
            clearValues,
            "$COLUMN_FOLDER_ID = ?",
            arrayOf(folderId.toString())
        )

        val result = db.delete(
            TABLE_FOLDERS,
            "$COLUMN_FOLDER_TABLE_ID = ?",
            arrayOf(folderId.toString())
        )

        db.close()

        return result
    }

    fun updateFolder(folderId: Int, newName: String): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_FOLDER_NAME, newName.trim())
        }

        val result = db.update(
            TABLE_FOLDERS,
            values,
            "$COLUMN_FOLDER_TABLE_ID = ?",
            arrayOf(folderId.toString())
        )

        db.close()

        return result
    }

    fun updateNoteFolder(noteId: Int, folderId: Int): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_FOLDER_ID, folderId)
        }

        val result = db.update(
            TABLE_NOTES,
            values,
            "$COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun getFolderIdForNote(noteId: Int): Int {
        val db = readableDatabase

        var folderId = 0

        val query = """
            SELECT $COLUMN_FOLDER_ID FROM $TABLE_NOTES
            WHERE $COLUMN_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            folderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID))
        }

        cursor.close()
        db.close()

        return folderId
    }

    fun getNotesByFolder(folderId: Int): ArrayList<Note> {
        val notesList = ArrayList<Note>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_NOTES
            WHERE $COLUMN_FOLDER_ID = ?
            AND $COLUMN_IS_DELETED = 0
            ORDER BY $COLUMN_IS_PINNED DESC, $COLUMN_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(folderId.toString()))

        if (cursor.moveToFirst()) {
            do {
                notesList.add(cursorToNote(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return notesList
    }

    fun getPinnedNotes(): ArrayList<Note> {
        val notesList = ArrayList<Note>()
        val db = readableDatabase

        val query = """
            SELECT * FROM $TABLE_NOTES
            WHERE $COLUMN_IS_PINNED = 1
            AND $COLUMN_IS_DELETED = 0
            ORDER BY $COLUMN_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                notesList.add(cursorToNote(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return notesList
    }

    // ---------------------------
    // SEARCH FUNCTIONS
    // ---------------------------

    fun searchNotes(searchText: String): ArrayList<SearchResult> {
        val searchResults = ArrayList<SearchResult>()
        val db = readableDatabase
        val keyword = "%${searchText.trim()}%"

        val query = """
        SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_CONTENT, $COLUMN_IS_LOCKED, $COLUMN_SHOW_TITLE_WHEN_LOCKED
        FROM $TABLE_NOTES
        WHERE $COLUMN_IS_DELETED = 0
        AND (
            (
                $COLUMN_IS_LOCKED = 0
                AND (
                    $COLUMN_TITLE LIKE ?
                    OR $COLUMN_CONTENT LIKE ?
                )
            )
            OR
            (
                $COLUMN_IS_LOCKED = 1
                AND $COLUMN_SHOW_TITLE_WHEN_LOCKED = 1
                AND $COLUMN_TITLE LIKE ?
            )
        )
        ORDER BY $COLUMN_IS_PINNED DESC, $COLUMN_ID DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(keyword, keyword, keyword))

        if (cursor.moveToFirst()) {
            do {
                val noteId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                val isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LOCKED))

                val resultTitle: String
                val subtitle: String

                if (isLocked == 1) {
                    resultTitle = title
                    subtitle = "Locked Note • Private content hidden"
                } else {
                    resultTitle = title

                    subtitle = if (content.length > 60) {
                        "Note • ${content.take(60)}..."
                    } else {
                        "Note • $content"
                    }
                }

                val result = SearchResult(
                    id = noteId,
                    title = resultTitle,
                    subtitle = subtitle,
                    type = "note"
                )

                searchResults.add(result)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return searchResults
    }

    fun searchCategories(searchText: String): ArrayList<SearchResult> {
        val searchResults = ArrayList<SearchResult>()
        val db = readableDatabase
        val keyword = "%${searchText.trim()}%"

        val query = """
            SELECT $COLUMN_CATEGORY_ID, $COLUMN_CATEGORY_NAME
            FROM $TABLE_CATEGORIES
            WHERE $COLUMN_CATEGORY_NAME LIKE ?
            ORDER BY $COLUMN_CATEGORY_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(keyword))

        if (cursor.moveToFirst()) {
            do {
                val result = SearchResult(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                    subtitle = "Category",
                    type = "category"
                )

                searchResults.add(result)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return searchResults
    }

    fun searchFolders(searchText: String): ArrayList<SearchResult> {
        val searchResults = ArrayList<SearchResult>()
        val db = readableDatabase
        val keyword = "%${searchText.trim()}%"

        val query = """
            SELECT $COLUMN_FOLDER_TABLE_ID, $COLUMN_FOLDER_NAME
            FROM $TABLE_FOLDERS
            WHERE $COLUMN_FOLDER_NAME LIKE ?
            ORDER BY $COLUMN_FOLDER_NAME ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(keyword))

        if (cursor.moveToFirst()) {
            do {
                val result = SearchResult(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_TABLE_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME)),
                    subtitle = "Folder",
                    type = "folder"
                )

                searchResults.add(result)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        val lowerSearchText = searchText.lowercase().trim()

        if (
            "pinned".contains(lowerSearchText) ||
            "pin".contains(lowerSearchText) ||
            "favorites".contains(lowerSearchText) ||
            "favourite".contains(lowerSearchText) ||
            "favorite".contains(lowerSearchText)
        ) {
            searchResults.add(
                SearchResult(
                    id = -1,
                    title = "Pinned / Favorites",
                    subtitle = "Folder",
                    type = "pinned_folder"
                )
            )
        }

        return searchResults
    }

    fun searchEverything(searchText: String): ArrayList<SearchResult> {
        val allResults = ArrayList<SearchResult>()

        if (searchText.trim().isEmpty()) {
            return allResults
        }

        allResults.addAll(searchNotes(searchText))
        allResults.addAll(searchCategories(searchText))
        allResults.addAll(searchFolders(searchText))

        return allResults
    }

    // ---------------------------
    // NOTE IMAGE FUNCTIONS
    // ---------------------------

    fun addImageToNote(noteId: Int, imageUri: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_IMAGE_NOTE_ID, noteId)
            put(COLUMN_IMAGE_URI, imageUri)
            put(COLUMN_IMAGE_CREATED_DATE, System.currentTimeMillis())
        }

        val result = db.insert(TABLE_NOTE_IMAGES, null, values)

        db.close()

        return result
    }

    fun getImagesForNote(noteId: Int): ArrayList<NoteImage> {
        val imageList = ArrayList<NoteImage>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_IMAGES
        WHERE $COLUMN_IMAGE_NOTE_ID = ?
        ORDER BY $COLUMN_IMAGE_ID DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                imageList.add(cursorToNoteImage(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return imageList
    }

    fun getLatestImagesForNote(noteId: Int, limit: Int = 3): ArrayList<NoteImage> {
        val imageList = ArrayList<NoteImage>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_IMAGES
        WHERE $COLUMN_IMAGE_NOTE_ID = ?
        ORDER BY $COLUMN_IMAGE_ID DESC
        LIMIT ?
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(noteId.toString(), limit.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                imageList.add(cursorToNoteImage(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return imageList
    }

    fun getAllActiveNoteImages(): ArrayList<NoteImage> {
        val imageList = ArrayList<NoteImage>()
        val db = readableDatabase

        val query = """
        SELECT i.*
        FROM $TABLE_NOTE_IMAGES i
        INNER JOIN $TABLE_NOTES n
        ON i.$COLUMN_IMAGE_NOTE_ID = n.$COLUMN_ID
        WHERE n.$COLUMN_IS_DELETED = 0
        AND n.$COLUMN_IS_LOCKED = 0
        ORDER BY i.$COLUMN_IMAGE_ID DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                imageList.add(cursorToNoteImage(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return imageList
    }

    fun deleteImageFromNote(imageId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_IMAGES,
            "$COLUMN_IMAGE_ID = ?",
            arrayOf(imageId.toString())
        )

        db.close()

        return result
    }

    fun deleteAllImagesForNote(noteId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_IMAGES,
            "$COLUMN_IMAGE_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    // ---------------------------
// NOTE FILE FUNCTIONS
// ---------------------------

    fun addFileToNote(
        noteId: Int,
        fileName: String,
        fileUri: String
    ): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_FILE_NOTE_ID, noteId)
            put(COLUMN_FILE_NAME, fileName)
            put(COLUMN_FILE_URI, fileUri)
            put(COLUMN_FILE_CREATED_DATE, System.currentTimeMillis())
        }

        val result = db.insert(TABLE_NOTE_FILES, null, values)

        db.close()

        return result
    }

    fun getFilesForNote(noteId: Int): ArrayList<NoteFile> {
        val fileList = ArrayList<NoteFile>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_FILES
        WHERE $COLUMN_FILE_NOTE_ID = ?
        ORDER BY $COLUMN_FILE_ID DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                fileList.add(cursorToNoteFile(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return fileList
    }

    fun getLatestFilesForNote(noteId: Int, limit: Int = 3): ArrayList<NoteFile> {
        val fileList = ArrayList<NoteFile>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_FILES
        WHERE $COLUMN_FILE_NOTE_ID = ?
        ORDER BY $COLUMN_FILE_ID DESC
        LIMIT ?
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(noteId.toString(), limit.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                fileList.add(cursorToNoteFile(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return fileList
    }

    fun getAllActiveNoteFiles(): ArrayList<NoteFile> {
        val fileList = ArrayList<NoteFile>()
        val db = readableDatabase

        val query = """
        SELECT f.*
        FROM $TABLE_NOTE_FILES f
        INNER JOIN $TABLE_NOTES n
        ON f.$COLUMN_FILE_NOTE_ID = n.$COLUMN_ID
        WHERE n.$COLUMN_IS_DELETED = 0
        AND n.$COLUMN_IS_LOCKED = 0
        ORDER BY f.$COLUMN_FILE_ID DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                fileList.add(cursorToNoteFile(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return fileList
    }

    fun deleteFileFromNote(fileId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_FILES,
            "$COLUMN_FILE_ID = ?",
            arrayOf(fileId.toString())
        )

        db.close()

        return result
    }

    fun deleteAllFilesForNote(noteId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_FILES,
            "$COLUMN_FILE_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    // ---------------------------
// NOTE MESSAGE / CHAT MODE FUNCTIONS
// ---------------------------

    fun addNoteMessage(message: NoteMessage): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_MESSAGE_NOTE_ID, message.noteId)
            put(COLUMN_MESSAGE_TYPE, message.messageType)
            put(COLUMN_MESSAGE_TEXT, message.textContent)
            put(COLUMN_MESSAGE_IMAGE_URI, message.imageUri)
            put(COLUMN_MESSAGE_FILE_NAME, message.fileName)
            put(COLUMN_MESSAGE_FILE_URI, message.fileUri)
            put(COLUMN_MESSAGE_LINK_URL, message.linkUrl)
            put(COLUMN_MESSAGE_CREATED_AT, message.createdAt)
            put(COLUMN_MESSAGE_ORDER, message.messageOrder)
        }

        val result = db.insert(TABLE_NOTE_MESSAGES, null, values)

        db.close()

        return result
    }

    fun getMessagesForNote(noteId: Int): ArrayList<NoteMessage> {
        val messageList = ArrayList<NoteMessage>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_MESSAGES
        WHERE $COLUMN_MESSAGE_NOTE_ID = ?
        ORDER BY $COLUMN_MESSAGE_ORDER ASC, $COLUMN_MESSAGE_ID ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                messageList.add(cursorToNoteMessage(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return messageList
    }

    fun deleteNoteMessage(messageId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_MESSAGES,
            "$COLUMN_MESSAGE_ID = ?",
            arrayOf(messageId.toString())
        )

        db.close()

        return result
    }

    fun deleteAllMessagesForNote(noteId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_MESSAGES,
            "$COLUMN_MESSAGE_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun getLatestTextMessagesForNote(noteId: Int, limit: Int = 3): ArrayList<NoteMessage> {
        val messageList = ArrayList<NoteMessage>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_MESSAGES
        WHERE $COLUMN_MESSAGE_NOTE_ID = ?
        AND $COLUMN_MESSAGE_TYPE = ?
        ORDER BY $COLUMN_MESSAGE_ID DESC
        LIMIT ?
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(noteId.toString(), MESSAGE_TYPE_TEXT, limit.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                messageList.add(cursorToNoteMessage(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return messageList
    }

    fun replaceMessagesForNote(noteId: Int, messages: List<String>): Int {
        val db = writableDatabase

        db.beginTransaction()

        return try {
            db.delete(
                TABLE_NOTE_MESSAGES,
                "$COLUMN_MESSAGE_NOTE_ID = ?",
                arrayOf(noteId.toString())
            )

            var insertedCount = 0

            for (i in messages.indices) {
                val text = messages[i].trim()

                if (text.isEmpty()) {
                    continue
                }

                val values = ContentValues().apply {
                    put(COLUMN_MESSAGE_NOTE_ID, noteId)
                    put(COLUMN_MESSAGE_TYPE, MESSAGE_TYPE_TEXT)
                    put(COLUMN_MESSAGE_TEXT, text)
                    put(COLUMN_MESSAGE_IMAGE_URI, "")
                    put(COLUMN_MESSAGE_FILE_NAME, "")
                    put(COLUMN_MESSAGE_FILE_URI, "")
                    put(COLUMN_MESSAGE_LINK_URL, "")
                    put(COLUMN_MESSAGE_CREATED_AT, System.currentTimeMillis())
                    put(COLUMN_MESSAGE_ORDER, i)
                }

                val result = db.insert(TABLE_NOTE_MESSAGES, null, values)

                if (result != -1L) {
                    insertedCount++
                }
            }

            db.setTransactionSuccessful()
            insertedCount
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun deleteImageMessagesByUri(noteId: Int, imageUri: String): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_MESSAGES,
            "$COLUMN_MESSAGE_NOTE_ID = ? AND $COLUMN_MESSAGE_TYPE = ? AND $COLUMN_MESSAGE_IMAGE_URI = ?",
            arrayOf(noteId.toString(), MESSAGE_TYPE_IMAGE, imageUri)
        )

        db.close()

        return result
    }

    fun deleteImageFromNoteByUri(noteId: Int, imageUri: String): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_IMAGES,
            "$COLUMN_IMAGE_NOTE_ID = ? AND $COLUMN_IMAGE_URI = ?",
            arrayOf(noteId.toString(), imageUri)
        )

        db.close()

        return result
    }

    fun deleteFileMessagesByUri(noteId: Int, fileUri: String): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_MESSAGES,
            "$COLUMN_MESSAGE_NOTE_ID = ? AND $COLUMN_MESSAGE_TYPE = ? AND $COLUMN_MESSAGE_FILE_URI = ?",
            arrayOf(noteId.toString(), MESSAGE_TYPE_FILE, fileUri)
        )

        db.close()

        return result
    }

    fun deleteFileFromNoteByUri(noteId: Int, fileUri: String): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_FILES,
            "$COLUMN_FILE_NOTE_ID = ? AND $COLUMN_FILE_URI = ?",
            arrayOf(noteId.toString(), fileUri)
        )

        db.close()

        return result
    }

    // ---------------------------
// NOTE BULLET FUNCTIONS
// ---------------------------

    fun addBulletToNote(
        noteId: Int,
        bulletText: String,
        bulletType: String,
        bulletOrder: Int,
        isTask: Int = 1,
        isCompleted: Int = 0
    ): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_NOTE_ID, noteId)
            put(COLUMN_BULLET_TEXT, bulletText.trim())
            put(COLUMN_BULLET_TYPE, bulletType)
            put(COLUMN_BULLET_ORDER, bulletOrder)
            put(COLUMN_BULLET_IS_TASK, isTask)
            put(COLUMN_BULLET_IS_COMPLETED, if (isTask == 1) isCompleted else 0)
            put(COLUMN_BULLET_CREATED_AT, System.currentTimeMillis())
        }

        val result = db.insert(TABLE_NOTE_BULLETS, null, values)

        db.close()

        return result
    }

    fun getBulletsForNote(noteId: Int): ArrayList<NoteBullet> {
        val bulletList = ArrayList<NoteBullet>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $TABLE_NOTE_BULLETS
        WHERE $COLUMN_BULLET_NOTE_ID = ?
        ORDER BY $COLUMN_BULLET_ORDER ASC, $COLUMN_BULLET_ID ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(noteId.toString()))

        if (cursor.moveToFirst()) {
            do {
                bulletList.add(cursorToNoteBullet(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return bulletList
    }

    fun deleteBulletById(bulletId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_BULLETS,
            "$COLUMN_BULLET_ID = ?",
            arrayOf(bulletId.toString())
        )

        db.close()

        return result
    }

    fun deleteAllBulletsForNote(noteId: Int): Int {
        val db = writableDatabase

        val result = db.delete(
            TABLE_NOTE_BULLETS,
            "$COLUMN_BULLET_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun updateBulletText(
        bulletId: Int,
        newText: String
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_TEXT, newText.trim())
        }

        val result = db.update(
            TABLE_NOTE_BULLETS,
            values,
            "$COLUMN_BULLET_ID = ?",
            arrayOf(bulletId.toString())
        )

        db.close()

        return result
    }

    fun updateBulletType(
        bulletId: Int,
        newType: String
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_TYPE, newType)
        }

        val result = db.update(
            TABLE_NOTE_BULLETS,
            values,
            "$COLUMN_BULLET_ID = ?",
            arrayOf(bulletId.toString())
        )

        db.close()

        return result
    }

    fun updateAllBulletTypesForNote(
        noteId: Int,
        newType: String
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_TYPE, newType)
        }

        val result = db.update(
            TABLE_NOTE_BULLETS,
            values,
            "$COLUMN_BULLET_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }

    fun updateBulletOrders(
        reorderedBullets: ArrayList<NoteBullet>
    ): Boolean {
        val db = writableDatabase

        db.beginTransaction()

        return try {
            for (i in reorderedBullets.indices) {
                val bullet = reorderedBullets[i]

                val values = ContentValues().apply {
                    put(COLUMN_BULLET_ORDER, i)
                }

                db.update(
                    TABLE_NOTE_BULLETS,
                    values,
                    "$COLUMN_BULLET_ID = ?",
                    arrayOf(bullet.id.toString())
                )
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun updateBulletCompleted(
        bulletId: Int,
        isCompleted: Int
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_IS_COMPLETED, isCompleted)
        }

        val result = db.update(
            TABLE_NOTE_BULLETS,
            values,
            "$COLUMN_BULLET_ID = ?",
            arrayOf(bulletId.toString())
        )

        db.close()

        return result
    }

    fun updateBulletTaskState(
        bulletId: Int,
        isTask: Int
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_IS_TASK, isTask)

            if (isTask == 0) {
                put(COLUMN_BULLET_IS_COMPLETED, 0)
            }
        }

        val result = db.update(
            TABLE_NOTE_BULLETS,
            values,
            "$COLUMN_BULLET_ID = ?",
            arrayOf(bulletId.toString())
        )

        db.close()

        return result
    }

    fun updateAllBulletTaskStatesForNote(
        noteId: Int,
        isTask: Int
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_BULLET_IS_TASK, isTask)

            if (isTask == 0) {
                put(COLUMN_BULLET_IS_COMPLETED, 0)
            }
        }

        val result = db.update(
            TABLE_NOTE_BULLETS,
            values,
            "$COLUMN_BULLET_NOTE_ID = ?",
            arrayOf(noteId.toString())
        )

        db.close()

        return result
    }
}
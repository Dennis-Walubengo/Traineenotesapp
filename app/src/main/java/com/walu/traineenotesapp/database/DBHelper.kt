package com.example.traineenotes.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TraineeNotes.db"
        private const val DATABASE_VERSION = 1

        // Tables
        private const val TABLE_USERS = "users"
        private const val TABLE_SUBJECTS = "subjects"
        private const val TABLE_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsers = """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT
            );
        """.trimIndent()

        val createSubjects = """
            CREATE TABLE $TABLE_SUBJECTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                subject_name TEXT
            );
        """.trimIndent()

        val createNotes = """
            CREATE TABLE $TABLE_NOTES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                subject_name TEXT,
                title TEXT,
                subtitle TEXT,
                content TEXT
            );
        """.trimIndent()

        db.execSQL(createUsers)
        db.execSQL(createSubjects)
        db.execSQL(createNotes)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    // ========== USER METHODS ==========
    fun registerUser(username: String, password: String): Pair<Boolean, String> {
        val usernamePattern = Regex("^[0-9]{4}[A-Z]{2}[0-9]{6}$")
        if (!usernamePattern.matches(username)) {
            return Pair(false, "Username must follow the pattern: 4 digits, 2 uppercase letters, and 6 digits (e.g., 1234AB567890).")
        }

        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
        if (!passwordPattern.matches(password)) {
            return Pair(false, "Password must be at least 8 characters long and include letters, numbers, and special characters.")
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }

        return try {
            db.insertOrThrow("users", null, values)
            Pair(true, "Registration successful.")
        } catch (e: Exception) {
            Pair(false, "Username already exists.")
        }
    }
    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE username=? AND password=?",
            arrayOf(username, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // ========== SUBJECT METHODS ==========
    fun addSubject(username: String, subjectName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("subject_name", subjectName)
        }
        return db.insert(TABLE_SUBJECTS, null, values) > 0
    }

    fun getSubjects(username: String): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT subject_name FROM $TABLE_SUBJECTS WHERE username=?",
            arrayOf(username)
        )
        val list = mutableListOf<String>()
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    fun deleteSubject(username: String, subjectName: String): Boolean {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "subject_name=?", arrayOf(subjectName))
        return db.delete(TABLE_SUBJECTS, "username=? AND subject_name=?", arrayOf(username, subjectName)) > 0
    }

    // ========== NOTE METHODS ==========
    fun saveNote(subjectName: String, title: String, subtitle: String, content: String): Boolean {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_NOTES WHERE subject_name=? AND title=?",
            arrayOf(subjectName, title)
        )

        val values = ContentValues().apply {
            put("subject_name", subjectName)
            put("title", title)
            put("subtitle", subtitle)
            put("content", content)
        }

        return if (cursor.count > 0) {
            cursor.moveToFirst()
            val id = cursor.getInt(0)
            db.update(TABLE_NOTES, values, "id=?", arrayOf(id.toString())) > 0
        } else {
            db.insert(TABLE_NOTES, null, values) > 0
        }.also {
            cursor.close()
        }
    }

    fun getNoteTitles(subjectName: String): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT title FROM $TABLE_NOTES WHERE subject_name=?",
            arrayOf(subjectName)
        )
        val list = mutableListOf<String>()
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    data class Note(val title: String, val subtitle: String, val content: String)

    fun getNote(subjectName: String, title: String): Note {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT title, subtitle, content FROM $TABLE_NOTES WHERE subject_name=? AND title=?",
            arrayOf(subjectName, title)
        )

        return if (cursor.moveToFirst()) {
            val note = Note(
                title = cursor.getString(0),
                subtitle = cursor.getString(1),
                content = cursor.getString(2)
            )
            cursor.close()
            note
        } else {
            cursor.close()
            Note("", "", "")
        }
    }

    fun deleteNote(subjectName: String, title: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_NOTES, "subject_name=? AND title=?", arrayOf(subjectName, title)) > 0
    }
    fun getTopicsForSubject(username: String, subjectName: String): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT title FROM $TABLE_NOTES WHERE subject_name=? ORDER BY title ASC",
            arrayOf(subjectName)
        )
        val topics = mutableListOf<String>()
        while (cursor.moveToNext()) {
            topics.add(cursor.getString(0))
        }
        cursor.close()
        return topics
    }
    fun updateSubject(username: String, oldSubjectName: String, newSubjectName: String): Boolean {
        val db = writableDatabase

        // Check if the new subject name already exists for this user
        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_SUBJECTS WHERE username=? AND subject_name=?",
            arrayOf(username, newSubjectName)
        )

        if (cursor.count > 0) {
            cursor.close()
            return false // Subject already exists
        }
        cursor.close()

        // Update the subject name in subjects table
        val values = ContentValues().apply {
            put("subject_name", newSubjectName)
        }

        val rowsUpdated = db.update(
            TABLE_SUBJECTS,
            values,
            "username=? AND subject_name=?",
            arrayOf(username, oldSubjectName)
        )

        // Also update subject_name in notes table
        db.update(
            TABLE_NOTES,
            values,
            "subject_name=?",
            arrayOf(oldSubjectName)
        )

        return rowsUpdated > 0
    }

}

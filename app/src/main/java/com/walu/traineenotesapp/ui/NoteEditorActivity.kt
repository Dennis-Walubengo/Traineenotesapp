package com.walu.traineenotesapp.ui

import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.traineenotes.database.DBHelper
import com.example.traineenotes.database.DBHelper.Note
import com.walu.traineenotesapp.R

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var username: String
    private lateinit var subjectName: String
    private var existingTopic: String? = null

    private lateinit var editTopic: EditText
    private lateinit var editSubtitle: EditText
    private lateinit var editContent: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        db = DBHelper(this)
        username = intent.getStringExtra("username") ?: ""
        subjectName = intent.getStringExtra("subjectName") ?: ""
        existingTopic = intent.getStringExtra("topic")

        editTopic = findViewById(R.id.editTopic)
        editSubtitle = findViewById(R.id.editSubtitle)
        editContent = findViewById(R.id.editContent)
        btnSave = findViewById(R.id.btnSaveNote)
        btnDelete = findViewById(R.id.btnDeleteNote)

        if (existingTopic != null) {
            // Load existing note
            val note: Note = db.getNote(subjectName, existingTopic!!)
            editTopic.setText(note.title)
            editSubtitle.setText(note.subtitle)
            editContent.setText(note.content)
            btnDelete.isEnabled = true
        } else {
            btnDelete.isEnabled = false
        }

        btnSave.setOnClickListener {
            val topic = editTopic.text.toString().trim()
            val subtitle = editSubtitle.text.toString().trim()
            val content = editContent.text.toString().trim()

            if (topic.isEmpty()) {
                editTopic.error = "Topic required"
                return@setOnClickListener
            }

            val success = db.saveNote(subjectName, topic, subtitle, content)

            if (success) {
                Toast.makeText(this, if (existingTopic != null) "Note updated" else "Note saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                    db.deleteNote(subjectName, existingTopic!!)
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}

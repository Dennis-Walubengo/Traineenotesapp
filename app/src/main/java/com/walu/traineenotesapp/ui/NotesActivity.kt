package com.example.traineenotes

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.traineenotes.database.DBHelper
import com.walu.traineenotesapp.R
import com.walu.traineenotesapp.ui.NoteEditorActivity

class NotesActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var username: String
    private lateinit var subjectName: String
    private lateinit var listView: ListView
    private lateinit var notesAdapter: ArrayAdapter<String>
    private var notesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        db = DBHelper(this)
        username = intent.getStringExtra("username") ?: ""
        subjectName = intent.getStringExtra("subjectName") ?: ""
        title = "Notes - $subjectName"

        listView = findViewById(R.id.noteListView)

        loadNotes()

        findViewById<Button>(R.id.btnAddNote).setOnClickListener {
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("subjectName", subjectName)
            startActivity(intent)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val topic = notesList[position]
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("subjectName", subjectName)
            intent.putExtra("topic", topic)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        notesList = db.getTopicsForSubject(username, subjectName).toMutableList()
        notesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notesList)
        listView.adapter = notesAdapter
    }
}

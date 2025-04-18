package com.walu.traineenotesapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.traineenotes.NotesActivity
import com.example.traineenotes.database.DBHelper
import com.walu.traineenotesapp.R

class SubjectActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var username: String
    private lateinit var listView: ListView
    private lateinit var subjectsAdapter: ArrayAdapter<String>
    private var subjects = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        db = DBHelper(this)
        username = intent.getStringExtra("username") ?: ""
        listView = findViewById(R.id.subjectListView)

        loadSubjects()

        findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            showSubjectDialog()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val subject = subjects[position]
            val intent = Intent(this, NotesActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("subjectName", subject)
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val subject = subjects[position]
            showSubjectOptionsDialog(subject)
            true
        }
    }

    private fun loadSubjects() {
        subjects = db.getSubjects(username).toMutableList()
        subjectsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, subjects)
        listView.adapter = subjectsAdapter
    }

    private fun showSubjectDialog(existingSubject: String? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null)
        val input = dialogView.findViewById<EditText>(R.id.editSubjectName)

        if (existingSubject != null) input.setText(existingSubject)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existingSubject == null) "Add Subject" else "Edit Subject")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    input.error = "Enter a subject name"
                } else {
                    val success = if (existingSubject == null) {
                        db.addSubject(username, name)
                    } else {
                        db.updateSubject(username, existingSubject, name)
                    }

                    if (success) {
                        loadSubjects()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Subject already exists or failed to save", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showSubjectOptionsDialog(subject: String) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Manage Subject: $subject")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSubjectDialog(subject)
                    1 -> {
                        db.deleteSubject(username, subject)
                        loadSubjects()
                    }
                }
            }
            .show()
    }
}

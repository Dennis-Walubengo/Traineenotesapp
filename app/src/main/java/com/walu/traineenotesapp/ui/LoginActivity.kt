package com.walu.traineenotesapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traineenotes.database.DBHelper
import com.walu.traineenotesapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DBHelper(this)

        val usernameInput = findViewById<EditText>(R.id.editUsername)
        val passwordInput = findViewById<EditText>(R.id.editPassword)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (db.checkUser(username, password)) {
                val intent = Intent(this, SubjectActivity::class.java)
                intent.putExtra("username", username)
                Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

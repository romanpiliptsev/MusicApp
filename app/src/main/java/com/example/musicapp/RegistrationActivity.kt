package com.example.musicapp

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant

class RegistrationActivity : AppCompatActivity() {

    lateinit var loginEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var registrationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        registrationButton = findViewById(R.id.registration_button)
        loginEditText = findViewById(R.id.login)
        passwordEditText = findViewById(R.id.password)
    }

    class CreateUserTask : AsyncTask<ArrayList<String>, Void, Boolean>() {
        override fun doInBackground(vararg p0: ArrayList<String>): Boolean {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val sql = "SELECT * FROM users WHERE email='${p0[0][0]}';"
                    val statement: PreparedStatement = connection.prepareStatement(sql)
                    val resultSet: ResultSet = statement.executeQuery()
                    if (!resultSet.next()) {
                        connection.prepareStatement("INSERT INTO users (email, created_at, password) VALUES ('${p0[0][0]}','${Instant.now().toString().subSequence(0, 10)}','${p0[0][1]}');")
                            .executeUpdate()
                        return true
                    } else {
                        return false
                    }
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)

            }
            return false
        }
    }

    fun createAccount(view: View) {
        if (loginEditText.text.toString().trim() == "" || passwordEditText.text.toString().trim() == "") {
            Toast.makeText(this, "Login or password field is empty!", Toast.LENGTH_LONG).show()
            return
        }

        if (CreateUserTask()
                .execute(arrayListOf(loginEditText.text.toString(), passwordEditText.text.toString()))
                .get()
        ) {
            Toast.makeText(this, "User are created successfully!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "User with this email exists!", Toast.LENGTH_LONG).show()
        }
    }
}
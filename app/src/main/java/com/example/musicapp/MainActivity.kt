package com.example.musicapp

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class MainActivity : AppCompatActivity() {

    lateinit var loginEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.login_button)
        loginEditText = findViewById(R.id.login)
        passwordEditText = findViewById(R.id.password)
    }

    class DBTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    Log.i("DBTask", "Good Bro!")
                    val info: Map<String, String> = HashMap()
                    val sql = "SELECT * FROM albums LIMIT 1"
                    val statement: PreparedStatement = connection.prepareStatement(sql)
                    val resultSet: ResultSet = statement.executeQuery()
                    /*if (resultSet.next()) {
                        info.put("name", resultSet.getString("name"))
                        info.put("address", resultSet.getString("address"))
                        info.put("phone_number", resultSet.getString("phone_number"))
                    }*/
                    resultSet.next()
                    return resultSet.getString("name") ?: "HUI"
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error reading information", e)
            }
            return "JUIHUI"
        }
    }

    class CheckUserTask : AsyncTask<ArrayList<String>, Void, Int>() {
        override fun doInBackground(vararg p0: ArrayList<String>): Int {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val sql = "SELECT * FROM users WHERE email='${p0[0][0]}' and password='${p0[0][1]}';"
                    val statement: PreparedStatement = connection.prepareStatement(sql)
                    val resultSet: ResultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        return resultSet.getString("user_id").toInt()
                    } else {
                        return -1
                    }
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error reading information", e)
            }
            return -1
        }
    }

    fun signIn(view: View) {
        val id = CheckUserTask()
            .execute(arrayListOf(loginEditText.text.toString(), passwordEditText.text.toString()))
            .get()

        if (id != -1) {
            val intent = Intent(this, SongsActivity::class.java)
            intent.putExtra("userId", id)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_LONG).show()
        }
    }

    fun createAccount(view: View) {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}
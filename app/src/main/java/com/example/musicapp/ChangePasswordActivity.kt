package com.example.musicapp

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import java.sql.DriverManager
import java.sql.ResultSet

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        userId = intent.getIntExtra("userId", -1)

        oldPasswordEditText = findViewById(R.id.old_password)
        newPasswordEditText = findViewById(R.id.new_password)
    }

    inner class ChangePasswordTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg p0: String): Boolean {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection
                        .prepareStatement("UPDATE users SET password = \"${p0[0]}\" WHERE user_id = $userId;")
                        .executeUpdate()
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return true
        }
    }

    class CheckPasswordTask : AsyncTask<Int, Void, String>() {
        override fun doInBackground(vararg p0: Int?): String {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val resultSet: ResultSet = connection
                        .prepareStatement("SELECT password FROM users WHERE user_id = ${p0[0]};")
                        .executeQuery()
                    resultSet.next()

                    return resultSet.getString("password")
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return ""
        }
    }

    fun changePassword(view: View) {
        if (CheckPasswordTask()
                .execute(userId)
                .get() == oldPasswordEditText.text.toString()
        ) {
            val password = newPasswordEditText.text.toString()
            if (password.trim() == "") {
                Toast.makeText(this, "New password field is empty!", Toast.LENGTH_LONG).show()
                return
            }
            if (password == oldPasswordEditText.text.toString()) {
                Toast.makeText(this, "Old and new passwords are similar!", Toast.LENGTH_LONG).show()
                return
            }
            ChangePasswordTask().execute(password)
            Toast.makeText(this, "Password are changed successfully!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Toast.makeText(this, "Old password entered incorrectly!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fav_songs -> {
                val intent = Intent(this, FavoriteSongsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.fav_artists -> {
                val intent = Intent(this, FavoriteArtistsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.fav_playlists -> {
                val intent = Intent(this, FavoritePlaylistsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.fav_albums -> {
                val intent = Intent(this, FavoriteAlbumsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.all_songs -> {
                val intent = Intent(this, SongsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.all_artists -> {
                val intent = Intent(this, ArtistsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.all_albums -> {
                val intent = Intent(this, AlbumsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.all_playlists -> {
                val intent = Intent(this, PlaylistsActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.logout -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
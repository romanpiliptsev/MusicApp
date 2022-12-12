package com.example.musicapp

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.adapters.ArtistsAdapter
import com.example.musicapp.data.Artist
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class ArtistsActivity : AppCompatActivity() {

    private lateinit var rvArtists: RecyclerView
    private lateinit var artistList: ArrayList<Artist>
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artists)

        userId = intent.getIntExtra("userId", -1)

        rvArtists = findViewById(R.id.rv_artists)
        rvArtists.layoutManager = LinearLayoutManager(this)
        artistList = GetArtistsTask().execute(userId).get()
        rvArtists.adapter = ArtistsAdapter(artistList)
    }

    inner class GetArtistsTask : AsyncTask<Int, Void, ArrayList<Artist>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Artist> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val statement: PreparedStatement = connection.prepareStatement("SELECT * FROM artists;")
                    val resultSet: ResultSet = statement.executeQuery()
                    val res = arrayListOf<Artist>()
                    while (resultSet.next()) {
                        val resultSetIsFav: ResultSet = connection
                            .prepareStatement("SELECT * FROM user_liked_artists WHERE user_id = $userId AND artist_id = ${resultSet.getString("artist_id")};")
                            .executeQuery()
                        val isFav = resultSetIsFav.next()

                        res.add(
                            Artist(resultSet.getString("artist_id").toInt(), resultSet.getString("name"), isFav)
                        )
                    }
                    return res
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return arrayListOf()
        }
    }

    inner class DeleteArtistTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("DELETE FROM user_liked_artists WHERE user_id = $userId AND artist_id = ${p0[0]};")
                        .executeUpdate()
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return null
        }
    }

    inner class AddArtistTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("INSERT INTO user_liked_artists (user_id, artist_id) VALUES ('$userId','${p0[0]}');")
                        .executeUpdate()
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return null
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
            R.id.fav_albums -> {
                val intent = Intent(this, FavoriteAlbumsActivity::class.java)
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
            R.id.all_songs -> {
                val intent = Intent(this, SongsActivity::class.java)
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
            R.id.change_password -> {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            R.id.logout -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Delete or add artist to favorites
    fun deleteArtist(view: View) {
        val numInArr = rvArtists.indexOfChild(view.parent as View)
        val artist = artistList[numInArr]

        if (artist.isFav) {
            DeleteArtistTask().execute(artist.artistId)
            (view as ImageView).setImageResource(R.drawable.ic_empty_star)
            Toast.makeText(this, "Deleted from favorites!", Toast.LENGTH_LONG).show()
        } else {
            AddArtistTask().execute(artist.artistId)
            (view as ImageView).setImageResource(R.drawable.ic_yellow_star)
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_LONG).show()
        }
        artist.isFav = !artist.isFav
    }

    fun openArtistProfile(view: View) {
        val numInArr = rvArtists.indexOfChild(view.parent as View)

        val intent = Intent(this, ArtistProfileActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("artistId", artistList[numInArr].artistId)
        intent.putExtra("artistName", artistList[numInArr].artistName)
        startActivity(intent)
    }
}
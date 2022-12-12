package com.example.musicapp

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.adapters.ArtistsAdapter
import com.example.musicapp.data.Artist
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class FavoriteArtistsActivity : AppCompatActivity() {

    private lateinit var rvFavouriteArtists: RecyclerView
    private lateinit var artistList: ArrayList<Artist>
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_artists)

        userId = intent.getIntExtra("userId", -1)

        rvFavouriteArtists = findViewById(R.id.rv_favorite_artists)
        rvFavouriteArtists.layoutManager = LinearLayoutManager(this)
        artistList = GetArtistsTask().execute(userId).get()
        rvFavouriteArtists.adapter = ArtistsAdapter(artistList)
    }

    class GetArtistsTask : AsyncTask<Int, Void, ArrayList<Artist>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Artist> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val sql = "SELECT * FROM artists WHERE artist_id IN (SELECT artist_id FROM user_liked_artists WHERE user_id = ${p0[0]});"
                    val statement: PreparedStatement = connection.prepareStatement(sql)
                    val resultSet: ResultSet = statement.executeQuery()
                    val res = arrayListOf<Artist>()
                    while (resultSet.next()) {
                        res.add(
                            Artist(resultSet.getString("artist_id").toInt(), resultSet.getString("name"), true)
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

    fun deleteArtist(view: View) {
        val numInArr = rvFavouriteArtists.indexOfChild(view.parent as View)
        val artist = artistList[numInArr]

        artistList.removeAt(numInArr)
        rvFavouriteArtists.adapter?.notifyDataSetChanged()
        DeleteArtistTask().execute(artist.artistId)

        Toast.makeText(this, "Deleted from favorite!", Toast.LENGTH_LONG).show()
    }

    fun openArtistProfile(view: View) {
        val intent = Intent(this, ArtistProfileActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("artistId", artistList[rvFavouriteArtists.indexOfChild(view.parent as View)].artistId)
        startActivity(intent)
    }
}
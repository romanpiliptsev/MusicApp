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
import com.example.musicapp.adapters.AlbumsAdapter
import com.example.musicapp.data.Album
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class FavoriteAlbumsActivity : AppCompatActivity() {

    private lateinit var rvFavouriteAlbums: RecyclerView
    private lateinit var albumList: ArrayList<Album>
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_albums)

        userId = intent.getIntExtra("userId", -1)

        rvFavouriteAlbums = findViewById(R.id.rv_favorite_albums)
        rvFavouriteAlbums.layoutManager = LinearLayoutManager(this)
        albumList = GetAlbumsTask().execute(userId).get()
        rvFavouriteAlbums.adapter = AlbumsAdapter(albumList)
    }

    class GetAlbumsTask : AsyncTask<Int, Void, ArrayList<Album>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Album> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val sql = "SELECT * FROM albums WHERE album_id IN (SELECT album_id FROM user_liked_albums WHERE user_id = ${p0[0]});"
                    val statement: PreparedStatement = connection.prepareStatement(sql)
                    val resultSet: ResultSet = statement.executeQuery()
                    val res = arrayListOf<Album>()

                    while (resultSet.next()) {
                        val artistId = resultSet.getString("artist_id")

                        val resultSetArtistName = connection
                            .prepareStatement("SELECT name FROM artists WHERE artist_id = $artistId;")
                            .executeQuery()
                        resultSetArtistName.next()

                        val resultSetTrackList: ResultSet = connection
                            .prepareStatement("SELECT title FROM songs WHERE album_id = ${resultSet.getString("album_id")};")
                            .executeQuery()

                        var trackList: String = "Track List:"
                        var i = 1
                        while (resultSetTrackList.next()) {
                            trackList += "\n${i++}. ${resultSetTrackList.getString("title")}"
                        }

                        res.add(
                            Album(resultSet.getString("album_id").toInt(), resultSet.getString("name"),
                                resultSetArtistName.getString("name"), trackList, true)
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

    inner class DeleteAlbumTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("DELETE FROM user_liked_albums WHERE user_id = $userId AND album_id = ${p0[0]};")
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

    fun deleteAlbum(view: View) {
        val numInArr = rvFavouriteAlbums.indexOfChild(view.parent as View)
        val album = albumList[numInArr]

        albumList.removeAt(numInArr)
        rvFavouriteAlbums.adapter?.notifyDataSetChanged()
        DeleteAlbumTask().execute(album.albumId)

        Toast.makeText(this, "Deleted from favorite!", Toast.LENGTH_LONG).show()
    }
}
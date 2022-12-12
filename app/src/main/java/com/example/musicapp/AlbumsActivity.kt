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
import com.example.musicapp.adapters.AlbumsAdapter
import com.example.musicapp.data.Album
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class AlbumsActivity : AppCompatActivity() {

    private lateinit var rvAlbums: RecyclerView
    private lateinit var albumList: ArrayList<Album>
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_albums)

        userId = intent.getIntExtra("userId", -1)

        rvAlbums = findViewById(R.id.rv_albums)
        rvAlbums.layoutManager = LinearLayoutManager(this)
        albumList = GetAlbumsTask().execute(userId).get()
        rvAlbums.adapter = AlbumsAdapter(albumList)
    }

    inner class GetAlbumsTask : AsyncTask<Int, Void, ArrayList<Album>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Album> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val statement: PreparedStatement = connection.prepareStatement("SELECT * FROM albums;")
                    val resultSet: ResultSet = statement.executeQuery()
                    val res = arrayListOf<Album>()

                    while (resultSet.next()) {
                        val resultSetIsFav: ResultSet = connection
                            .prepareStatement("SELECT * FROM user_liked_albums WHERE user_id = $userId AND album_id = ${resultSet.getString("album_id")};")
                            .executeQuery()
                        val isFav = resultSetIsFav.next()

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
                                resultSetArtistName.getString("name"), trackList, isFav)
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

    inner class AddAlbumTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("INSERT INTO user_liked_albums (user_id, album_id) VALUES ('$userId','${p0[0]}');")
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

    // Delete or add album to favorites
    fun deleteAlbum(view: View) {
        val numInArr = rvAlbums.indexOfChild(view.parent as View)
        val album = albumList[numInArr]

        if (album.isFav) {
            DeleteAlbumTask().execute(album.albumId)
            (view as ImageView).setImageResource(R.drawable.ic_empty_star)
            Toast.makeText(this, "Deleted from favorites!", Toast.LENGTH_LONG).show()
        } else {
            AddAlbumTask().execute(album.albumId)
            (view as ImageView).setImageResource(R.drawable.ic_yellow_star)
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_LONG).show()
        }
        album.isFav = !album.isFav
    }
}
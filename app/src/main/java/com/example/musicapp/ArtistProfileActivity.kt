package com.example.musicapp

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.adapters.AlbumsAdapter
import com.example.musicapp.adapters.SongsAdapter
import com.example.musicapp.data.Album
import com.example.musicapp.data.Song
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class ArtistProfileActivity : AppCompatActivity() {

    private lateinit var rvSongs: RecyclerView
    private lateinit var rvAlbums: RecyclerView
    private lateinit var songList: ArrayList<Song>
    private lateinit var albumList: ArrayList<Album>
    private var userId: Int = -1
    private var artistId: Int = -1
    lateinit var artistName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_profile)

        userId = intent.getIntExtra("userId", -1)
        artistId = intent.getIntExtra("artistId", -1)
        artistName = intent.getStringExtra("artistName") ?: ""

        val artistNameTextView: TextView = findViewById(R.id.artist_name)
        artistNameTextView.text = artistName

        rvSongs = findViewById(R.id.rv_songs)
        rvSongs.layoutManager = LinearLayoutManager(this)
        songList = GetSongsTask().execute(artistId).get()
        rvSongs.adapter = SongsAdapter(songList)

        rvAlbums = findViewById(R.id.rv_albums)
        rvAlbums.layoutManager = LinearLayoutManager(this)
        albumList = GetAlbumsTask().execute(artistId).get()
        rvAlbums.adapter = AlbumsAdapter(albumList)
    }

    inner class GetSongsTask : AsyncTask<Int, Void, ArrayList<Song>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Song> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val resultSet: ResultSet = connection
                        .prepareStatement("SELECT * FROM songs WHERE artist_id = $artistId;")
                        .executeQuery()
                    val res = arrayListOf<Song>()
                    while (resultSet.next()) {
                        val resultSetIsFav: ResultSet = connection
                            .prepareStatement("SELECT * FROM user_liked_songs WHERE user_id = $userId AND song_id = ${resultSet.getString("song_id")};")
                            .executeQuery()
                        val isFav = resultSetIsFav.next()

                        val artistId = resultSet.getString("artist_id")
                        val sqlArtist = "SELECT name FROM artists WHERE artist_id = ${artistId};"
                        val statementArtist: PreparedStatement = connection.prepareStatement(sqlArtist)
                        val resultSetArtist: ResultSet = statementArtist.executeQuery()
                        resultSetArtist.next()

                        val albumId = resultSet.getString("album_id")
                        val sqlAlbum = "SELECT name FROM albums WHERE album_id = ${albumId};"
                        val statementAlbum: PreparedStatement = connection.prepareStatement(sqlAlbum)
                        val resultSetAlbum: ResultSet = statementAlbum.executeQuery()
                        resultSetAlbum.next()

                        res.add(
                            Song(resultSet.getString("song_id").toInt(), resultSet.getString("title"),
                                resultSetArtist.getString("name"), resultSetAlbum.getString("name"),
                                resultSet.getString("length"), resultSet.getString("youtube_link"), isFav)
                        )
                    }
                    return res
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error reading information", e)
            }
            return arrayListOf()
        }
    }

    inner class GetAlbumsTask : AsyncTask<Int, Void, ArrayList<Album>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Album> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val statement: PreparedStatement = connection.prepareStatement("SELECT * FROM albums WHERE artist_id = $artistId;;")
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

                        var trackList = "Track List:"
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

    inner class DeleteSongTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("DELETE FROM user_liked_songs WHERE user_id = $userId AND song_id = ${p0[0]};")
                        .executeUpdate()
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return null
        }
    }

    inner class AddSongTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("INSERT INTO user_liked_songs (user_id, song_id) VALUES ('$userId','${p0[0]}');")
                        .executeUpdate()
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return null
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
            R.id.fav_songs -> {
                val intent = Intent(this, FavoriteSongsActivity::class.java)
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

    // Delete or add song to favorites
    fun deleteSong(view: View) {
        val numInArr = rvSongs.indexOfChild(view.parent as View)
        val song = songList[numInArr]

        if (song.isFav) {
            DeleteSongTask().execute(song.songId)
            (view as ImageView).setImageResource(R.drawable.ic_empty_star)
            Toast.makeText(this, "Deleted from favorites!", Toast.LENGTH_LONG).show()
        } else {
            AddSongTask().execute(song.songId)
            (view as ImageView).setImageResource(R.drawable.ic_yellow_star)
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_LONG).show()
        }
        song.isFav = !song.isFav
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

    fun openYouTube(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(songList[rvSongs.indexOfChild(view.parent as View)].ytLink))
        startActivity(intent)
    }
}
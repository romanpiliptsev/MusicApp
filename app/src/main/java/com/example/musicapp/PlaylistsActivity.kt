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
import com.example.musicapp.adapters.PlaylistsAdapter
import com.example.musicapp.data.Playlist
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class PlaylistsActivity : AppCompatActivity() {

    private lateinit var rvPlaylists: RecyclerView
    private lateinit var playlistList: ArrayList<Playlist>
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        userId = intent.getIntExtra("userId", -1)

        rvPlaylists = findViewById(R.id.rv_playlists)
        rvPlaylists.layoutManager = LinearLayoutManager(this)
        playlistList = GetPlaylistsTask().execute(userId).get()
        rvPlaylists.adapter = PlaylistsAdapter(playlistList)
    }

    inner class GetPlaylistsTask : AsyncTask<Int, Void, ArrayList<Playlist>>() {
        override fun doInBackground(vararg p0: Int?): ArrayList<Playlist> {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    val statement: PreparedStatement = connection.prepareStatement("SELECT * FROM playlists;")
                    val resultSet: ResultSet = statement.executeQuery()
                    val res = arrayListOf<Playlist>()

                    while (resultSet.next()) {
                        val resultSetIsFav: ResultSet = connection
                            .prepareStatement("SELECT * FROM user_playlists WHERE user_id = $userId AND playlist_id = ${resultSet.getString("playlist_id")};")
                            .executeQuery()
                        val isFav = resultSetIsFav.next()

                        val resultSetTrackList: ResultSet = connection
                            .prepareStatement("SELECT * FROM songs WHERE song_id IN (SELECT song_id FROM songs_in_playlist WHERE playlist_id = ${resultSet.getString("playlist_id")});")
                            .executeQuery()

                        var trackList = "Track List:"
                        var i = 1
                        while (resultSetTrackList.next()) {
                            val resultSetArtist: ResultSet = connection
                                .prepareStatement("SELECT name FROM artists WHERE artist_id = ${resultSetTrackList.getString("artist_id")};")
                                .executeQuery()
                            resultSetArtist.next()

                            trackList += "\n${i++}. ${resultSetTrackList.getString("title")} (by ${resultSetArtist.getString("name")})"
                        }

                        res.add(
                            Playlist(resultSet.getString("playlist_id").toInt(),
                                resultSet.getString("name"), trackList, isFav)
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

    inner class DeletePlaylistTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("DELETE FROM user_playlists WHERE user_id = $userId AND playlist_id = ${p0[0]};")
                        .executeUpdate()
                }
            } catch (e: Exception) {
                Log.e("DBTask", "Error", e)
            }
            return null
        }
    }

    inner class AddPlaylistTask : AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg p0: Int?): Void? {
            try {
                DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/music_service", "bestuser", "14112002Aa!").use { connection ->
                    connection.prepareStatement("INSERT INTO user_playlists (user_id, playlist_id) VALUES ('$userId','${p0[0]}');")
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
            R.id.all_albums -> {
                val intent = Intent(this, AlbumsActivity::class.java)
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

    // Delete or add playlist to favorites
    fun deletePlaylist(view: View) {
        val numInArr = rvPlaylists.indexOfChild(view.parent as View)
        val playlist = playlistList[numInArr]

        if (playlist.isFav) {
            DeletePlaylistTask().execute(playlist.playlistId)
            (view as ImageView).setImageResource(R.drawable.ic_empty_star)
            Toast.makeText(this, "Deleted from favorites!", Toast.LENGTH_LONG).show()
        } else {
            AddPlaylistTask().execute(playlist.playlistId)
            (view as ImageView).setImageResource(R.drawable.ic_yellow_star)
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_LONG).show()
        }
        playlist.isFav = !playlist.isFav
    }
}
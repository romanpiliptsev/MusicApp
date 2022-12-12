package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.Song

class SongsAdapter(private val songs: List<Song>)
    : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val songItem = songs[position]

        var album = ""
        album = if (songItem.album.length > 12) {
            songItem.album.substring(0, 12) + "..."
        } else {
            songItem.album
        }

        holder.songName.text = songItem.name
        holder.artist.text = songItem.artist
        holder.albumName.text = album
        holder.duration.text = songItem.duration

        if (!songItem.isFav) {
            holder.star.setImageResource(R.drawable.ic_empty_star)
        }
    }

    class SongViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.song_name)
        val artist: TextView = itemView.findViewById(R.id.artist)
        val albumName: TextView = itemView.findViewById(R.id.album_name)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val star: ImageView = itemView.findViewById(R.id.star)
    }
}

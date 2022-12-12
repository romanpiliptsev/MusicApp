package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.Playlist

class PlaylistsAdapter(private val playlists: List<Playlist>)
    : RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlistItem = playlists[position]
        holder.playlistName.text = "${playlistItem.playlistName}"
        holder.trackList.text = playlistItem.trackList

        if (!playlistItem.isFav) {
            holder.star.setImageResource(R.drawable.ic_empty_star)
        }
    }

    class PlaylistViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        val trackList: TextView = itemView.findViewById(R.id.track_list)
        val star: ImageView = itemView.findViewById(R.id.star)
    }
}
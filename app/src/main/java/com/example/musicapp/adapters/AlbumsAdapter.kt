package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.Album

class AlbumsAdapter(private val albums: List<Album>)
    : RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>() {

    override fun getItemCount(): Int {
        return albums.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_item, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val albumItem = albums[position]
        holder.albumAndArtistName.text = "${albumItem.name} (by ${albumItem.artist})"
        holder.trackList.text = albumItem.trackList

        if (!albumItem.isFav) {
            holder.star.setImageResource(R.drawable.ic_empty_star)
        }
    }

    class AlbumViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumAndArtistName: TextView = itemView.findViewById(R.id.album_and_artist_name)
        val trackList: TextView = itemView.findViewById(R.id.track_list)
        val star: ImageView = itemView.findViewById(R.id.star)
    }
}
package com.example.musicapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.Artist

class ArtistsAdapter(private val artists: List<Artist>)
    : RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>() {

    override fun getItemCount(): Int {
        return artists.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.artist_item, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artistItem = artists[position]
        holder.artistName.text = artistItem.artistName

        if (!artistItem.isFav) {
            holder.star.setImageResource(R.drawable.ic_empty_star)
        }
    }

    class ArtistViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
        val star: ImageView = itemView.findViewById(R.id.star)
    }
}
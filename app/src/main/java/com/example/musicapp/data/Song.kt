package com.example.musicapp.data

data class Song(val songId: Int, val name: String, val artist: String, val album: String,
                val duration: String, val ytLink: String, var isFav: Boolean)
package com.example.musicapp.data

data class Album(val albumId: Int, val name: String, val artist: String, val trackList: String,
                 var isFav: Boolean)
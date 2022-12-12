package com.example.musicapp.data

data class Playlist(val playlistId: Int, val playlistName: String, val trackList: String,
                    var isFav: Boolean)
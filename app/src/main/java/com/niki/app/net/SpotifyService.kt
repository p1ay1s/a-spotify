package com.niki.app.net

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface SpotifyService {
    @GET("tracks/{id}/lyrics")
    fun getLyrics(
        @Header("Authorization") auth: String,
        @Path("id") trackId: String
    ): Call<LyricsResponse>
}

data class LyricsResponse(
    val lyrics: String?,
    val syncType: String?
)
package com.niki.spotify.web.service

import com.niki.spotify.web.models.CursorPager
import com.niki.spotify.web.models.RecentlyPlayedTrack
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * getRecentlyPlayedTracks
 */
interface PlayerService {
    /**
     * Get the Current User's Recently Played Tracks.
     *
     * @param options Optional parameters. For list of available parameters see
     * @return Recently played tracks with their context (e.g: while playing a playlist)
     *
     * [Get Recently Played Tracks](https://developer.spotify.com/documentation/documentation/web-api/reference/reference-beta/#endpoint-get-recently-played)
     */
    @GET("me/player/recently-played")
    fun getRecentlyPlayedTracks(@QueryMap options: Map<String, String> = emptyMap()): Call<CursorPager<RecentlyPlayedTrack?>>
}
package com.niki.spotify.web.service

import com.niki.spotify.web.models.AlbumsPager
import com.niki.spotify.web.models.ArtistsPager
import com.niki.spotify.web.models.PlaylistsPager
import com.niki.spotify.web.models.SearchResult
import com.niki.spotify.web.models.TracksPager
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * search
 *
 * searchTracks
 *
 * searchArtists
 *
 * searchAlbums
 *
 * searchPlaylists
 *
 */
interface SearchService {
    /**
     * Get Spotify catalog information that match a keyword string.
     * Search results include hits from all the specified item types.
     *
     * @param q       The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param type    Valid types are: album , artist, playlist, track, show and episode.
     * @param options Optional parameters. For list of supported parameters see
     * @return A SearchResult object with item for each type
     *
     * [Search for an Item](https://developer.spotify.com/documentation/web-api/reference/search-item/)
     */
    @GET("search")
    fun search(
        @Query("q") q: String?,
        @Query("type") type: String?, // in "album", "artist", "playlist", "track", "show", "episode", "audiobook"
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<SearchResult>

    @GET("search?type=track")
    fun searchTracks(
        @Query("q") q: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<TracksPager>

    @GET("search?type=artist")
    fun searchArtists(
        @Query("q") q: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<ArtistsPager>

    @GET("search?type=album")
    fun searchAlbums(
        @Query("q") q: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<AlbumsPager>

    @GET("search?type=playlist")
    fun searchPlaylists(
        @Query("q") q: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<PlaylistsPager>
}
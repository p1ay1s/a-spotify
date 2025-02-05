package com.niki.spotify.web.service

import com.niki.spotify.web.models.Album
import com.niki.spotify.web.models.Artist
import com.niki.spotify.web.models.Artists
import com.niki.spotify.web.models.Pager
import com.niki.spotify.web.models.Tracks
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * getArtist
 *
 * getSeveralArtists
 *
 * getArtistsAlbums
 *
 * getArtistTopTracks
 */
interface ArtistsService {
    /**
     * Get Spotify catalog information for a single artist identified by their unique Spotify ID.
     *
     * @param artistId The Spotify ID for the artist.
     * @return Requested artist information
     *
     * [Get Artist](https://developer.spotify.com/documentation/web-api/reference/get-artist/)
     */
    @GET("artists/{id}")
    fun getArtist(@Path("id") artistId: String?): Call<Artist>

    /**
     * Get Spotify catalog information for several artists based on their Spotify IDs.
     *
     * @param artistIds A comma-separated list of the Spotify IDs for the artists
     * @return An object whose key is "artists" and whose value is an array of artist objects.
     *
     * [Get Several Artists](https://developer.spotify.com/documentation/web-api/reference/get-several-artists/)
     */
    @GET("artists")
    fun getSeveralArtists(@Query("ids") artistIds: String?): Call<Artists>

    /**
     * Get Spotify catalog information about an artist’s albums.
     *
     * @param artistId The Spotify ID for the artist.
     * @param options  Optional parameters. For list of supported parameters see
     * @return An array of simplified album objects wrapped in a paging object.
     *
     * [Get Artist's Albums](https://developer.spotify.com/documentation/web-api/reference/get-artists-albums/)
     */
    @GET("artists/{id}/albums")
    fun getArtistsAlbums(
        @Path("id") artistId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<Album?>>

    /**
     * Get Spotify catalog information about an artist’s top tracks by country.
     *
     * @param artistId The Spotify ID for the artist.
     * @param country  The country: an ISO 3166-1 alpha-2 country code.
     * @return An object whose key is "tracks" and whose value is an array of track objects.
     *
     * [Get Artist’s Top Tracks](https://developer.spotify.com/documentation/web-api/reference/get-artists-top-tracks/)
     */
    @GET("artists/{id}/top-tracks")
    fun getArtistTopTracks(
        @Path("id") artistId: String?,
        @Query("country") country: String?
    ): Call<Tracks>
}
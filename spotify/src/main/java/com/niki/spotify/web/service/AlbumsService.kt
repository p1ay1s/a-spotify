package com.niki.spotify.web.service

import com.niki.spotify.web.models.Album
import com.niki.spotify.web.models.Albums
import com.niki.spotify.web.models.NewReleases
import com.niki.spotify.web.models.Pager
import com.niki.spotify.web.models.Result
import com.niki.spotify.web.models.SavedAlbum
import com.niki.spotify.web.models.Track
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * getAlbum
 *
 * getSeveralAlbums
 *
 * getAlbumTracks
 *
 * getUsersSavedAlbums
 *
 * saveAlbumsForCurrentUser
 *
 * removeUsersSavedAlbums
 *
 * checkUsersSavedAlbums
 *
 * getNewReleases
 */
interface AlbumsService {
    /**
     * Get Spotify catalog information for a single album.
     *
     * @param albumId The Spotify ID for the album.
     * @param options Optional parameters. For list of supported parameters see
     * @return Requested album information
     *
     * [Get Album](https://developer.spotify.com/documentation/web-api/reference/get-album/)
     */
    @GET("albums/{id}")
    fun getAlbum(
        @Path("id") albumId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Album>

    /**
     * Get Spotify catalog information for multiple albums identified by their Spotify IDs.
     *
     * @param albumIds A comma-separated list of the Spotify IDs for the albums
     * @param options  Optional parameters. For list of supported parameters see
     * @return Object whose key is "albums" and whose value is an array of album objects.
     *
     * [Get Several Albums](https://developer.spotify.com/documentation/web-api/reference/get-several-albums/)
     */
    @GET("albums")
    fun getSeveralAlbums(
        @Query("ids") albumIds: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Albums>

    /**
     * Get Spotify catalog information about an album’s tracks.
     *
     * @param albumId The Spotify ID for the album.
     * @param options Optional parameters. For list of supported parameters see
     * @return List of simplified album objects wrapped in a Pager object
     *
     * [Get an Album’s Tracks](https://developer.spotify.com/documentation/web-api/reference/get-albums-tracks/)
     */
    @GET("albums/{id}/tracks")
    fun getAlbumTracks(
        @Path("id") albumId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<Track?>>

    /**
     * Get a list of the albums saved in the current Spotify user’s “Your Music” library.
     *
     * @param options Optional parameters. For list of supported parameters see
     * @return A paginated list of saved albums
     *
     * [Get User’s Saved Albums](https://developer.spotify.com/documentation/web-api/reference/get-users-saved-albums/)
     */
    @GET("me/albums")
    fun getUsersSavedAlbums(@QueryMap options: Map<String, String> = emptyMap()): Call<Pager<SavedAlbum?>>

    /**
     * Save one or more albums to the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the albums
     * @return An empty result
     *
     * [Save Albums for Current User](https://developer.spotify.com/documentation/web-api/reference/save-albums-user/)
     */
    @PUT("me/albums")
    fun saveAlbumsForCurrentUser(@Query("ids") ids: String?): Call<Result>

    /**
     * Remove one or more albums from the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the albums
     * @return An empty result
     *
     * [Remove User’s Saved Albums](https://developer.spotify.com/documentation/web-api/reference/remove-albums-user/)
     */
    @DELETE("me/albums")
    fun removeUsersSavedAlbums(@Query("ids") ids: String?): Call<Result>

    /**
     * Check if one or more albums is already saved in the current Spotify user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the albums
     * @return An array with boolean values that indicate whether the albums are in the current Spotify user’s “Your Music” library.
     *
     * [Check User’s Saved Albums](https://developer.spotify.com/documentation/web-api/reference/check-users-saved-albums/)
     */
    @GET("me/albums/contains")
    fun checkUsersSavedAlbums(@Query("ids") ids: String?): Call<Array<Boolean?>>

    /**
     * Get a list of new album releases featured in Spotify (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @param options Optional parameters. For list of supported parameters see
     * @return A NewReleases object with the new album releases
     *
     * [Get New Releases](https://developer.spotify.com/documentation/web-api/reference/get-list-new-releases/)
     */
    @GET("browse/new-releases")
    fun getNewReleases(@QueryMap options: Map<String, String> = emptyMap()): Call<NewReleases>
}
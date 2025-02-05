package com.niki.spotify.web.service

import com.niki.spotify.web.models.FeaturedPlaylists
import com.niki.spotify.web.models.Pager
import com.niki.spotify.web.models.Playlist
import com.niki.spotify.web.models.PlaylistSimple
import com.niki.spotify.web.models.PlaylistTrack
import com.niki.spotify.web.models.PlaylistsPager
import com.niki.spotify.web.models.Result
import com.niki.spotify.web.models.SnapshotId
import com.niki.spotify.web.models.TracksToRemove
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * getPlaylist
 *
 * changePlaylistDetails
 *
 * getPlaylistItems
 *
 * updatePlaylistItems
 *
 * addItemsToPlaylist
 *
 * removePlaylistItems
 *
 * getCurrentUsersPlaylists
 *
 * getUsersPlaylists
 *
 * createPlaylist
 *
 * getFeaturedPlaylists
 *
 * getPlaylistsForCategory
 */
interface PlaylistsService {

    /**
     * Get a playlist owned by a Spotify user.
     *
     * @param playlistId The Spotify ID for the playlist.
     * @param options    Optional parameters. For list of supported parameters see
     * @return Requested Playlist.
     *
     * [Get Playlist](https://developer.spotify.com/documentation/web-api/reference/get-playlist/)
     */
    @GET("playlists/{playlist_id}")
    fun getPlaylist(
        @Path("playlist_id") playlistId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Playlist>

    /**
     * Change a playlist’s name and public/private state. (The user must, of course, own the playlist.)
     *
     * @param playlistId The playlist's Id
     * @param body       The body parameters. For list of supported parameters see [endpoint documentation](https://developer.spotify.com/documentation/web-api/reference/change-playlist-details/)
     * @return An empty result
     *
     * [Change Playlist's Details](https://developer.spotify.com/documentation/web-api/reference/change-playlist-details/)
     */
    @PUT("playlists/{playlist_id}")
    fun changePlaylistDetails(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body body: Map<String, String>
    ): Call<Result>

    /**
     * Get full details of the tracks of a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @param options    Optional parameters. For list of supported parameters see
     * @return List of playlist's tracks wrapped in a `Pager` object
     *
     * [Get Playlist Items](https://developer.spotify.com/documentation/web-api/reference/get-playlists-tracks/)
     */
    @GET("playlists/{playlist_id}/tracks")
    fun getPlaylistItems(
        @Path("playlist_id") playlistId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<PlaylistTrack?>>

    /**
     * Replace all the tracks in a playlist, overwriting its existing tracks. This powerful request can be useful for
     * replacing tracks, re-ordering existing tracks, or clearing the playlist.
     *
     * @param playlistId The playlist's Id
     * @param trackUris  A list of comma-separated track uris
     * @return An empty result
     *
     * [Update Playlist Items](https://developer.spotify.com/documentation/web-api/reference/replace-playlists-tracks/)
     */
    @PUT("playlists/{playlist_id}/tracks")
    fun updatePlaylistItems(
        @Path("playlist_id") playlistId: String?,
        @Query("uris") trackUris: String?,
        @Body body: Map<String, String>
    ): Call<Result>

    /**
     * Add tracks to a playlist
     *
     * @param playlistId      The playlist's ID
     * @param queryParameters Query parameters
     * @param body            The body parameters
     * @return A snapshot ID (the version of the playlist)
     *
     * [Add Items to Playlist](https://developer.spotify.com/documentation/web-api/reference/add-tracks-to-playlist/)
     */
    @POST("playlists/{playlist_id}/tracks")
    fun addItemsToPlaylist(
        @Path("playlist_id") playlistId: String?,
        @QueryMap queryParameters: Map<String, String>,
        @Body body: Map<String, String>
    ): Call<SnapshotId>

    /**
     * Remove one or more tracks from a user’s playlist.
     *
     * @param playlistId     The playlist's Id
     * @param tracksToRemove A list of tracks to remove
     * @return A snapshot ID (the version of the playlist)
     *
     * [Remove Playlist Items](https://developer.spotify.com/documentation/web-api/reference/remove-tracks-playlist/)
     */
    @DELETE("playlists/{playlist_id}/tracks")
    fun removePlaylistItems(
        @Path("playlist_id") playlistId: String?,
        @Body tracksToRemove: TracksToRemove?
    ): Call<SnapshotId>

    /**
     *
     * Get a list of the playlists owned or followed by the current Spotify user.
     *
     * @param options Optional parameters. For list of supported parameters see
     * @return List of user's playlists wrapped in a `Pager` object
     *
     * [Get Current User's Playlists](https://developer.spotify.com/documentation/web-api/reference/get-a-list-of-current-users-playlists/)
     */
    @GET("me/playlists")
    fun getCurrentUsersPlaylists(@QueryMap options: Map<String, String> = emptyMap()): Call<Pager<PlaylistSimple?>>

    /**
     * Get a list of the playlists owned or followed by a Spotify user.
     *
     * @param userId  The user's Spotify user ID.
     * @param options Optional parameters. For list of supported parameters see
     * @return List of user's playlists wrapped in a `Pager` object
     *
     * [Get User’s Playlists](https://developer.spotify.com/documentation/web-api/reference/get-list-users-playlists/)
     */
    @GET("users/{user_id}/playlists")
    fun getUsersPlaylists(
        @Path("user_id") userId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<PlaylistSimple?>>

    /**
     * Create a playlist
     *
     * @param userId  The playlist's owner's User ID
     * @param options The body parameters
     * @return The created playlist
     *
     * [Create Playlist](https://developer.spotify.com/documentation/web-api/reference/create-playlist/)
     */
    @POST("users/{user_id}/playlists")
    fun createPlaylist(
        @Path("user_id") userId: String?,
        @Body options: Map<String, String> = emptyMap()
    ): Playlist?

    /**
     * Get a list of Spotify featured playlists (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @param options Optional parameters. For list of supported parameters see
     * @return n FeaturedPlaylists object with the featured playlists
     *
     * [Get Featured Playlists](https://developer.spotify.com/documentation/web-api/reference/get-list-featured-playlists/)
     */
    @GET("browse/featured-playlists")
    @Deprecated("")
    fun getFeaturedPlaylists(
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<FeaturedPlaylists>

    /**
     * Retrieve playlists for a Spotify Category.
     *
     * @param categoryId The category's ID.
     * @param options    Optional parameters.
     * @return Playlists for a Spotify Category.
     *
     * [Get Category's playlists](https://developer.spotify.com/documentation/web-api/reference/get-categorys-playlists/)
     */
    @GET("browse/categories/{category_id}/playlists")
    @Deprecated("")
    fun getPlaylistsForCategory(
        @Path("category_id") categoryId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<PlaylistsPager>

    // 有两个没写
}
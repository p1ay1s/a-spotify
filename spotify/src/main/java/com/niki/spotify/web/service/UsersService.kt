package com.niki.spotify.web.service

import com.niki.spotify.web.models.Artist
import com.niki.spotify.web.models.ArtistsCursorPager
import com.niki.spotify.web.models.Pager
import com.niki.spotify.web.models.PlaylistFollowPrivacy
import com.niki.spotify.web.models.Result
import com.niki.spotify.web.models.Track
import com.niki.spotify.web.models.UserPrivate
import com.niki.spotify.web.models.UserPublic
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * getCurrentUsersProfile
 *
 * getUsersTopArtists
 *
 * getUsesTopTracks
 *
 * getUsersProfile
 *
 * followPlaylist
 *
 * unfollowPlaylist
 *
 * getFollowedArtists
 *
 * followUsers
 *
 * followArtists
 *
 * unfollowUsers
 *
 * unfollowArtists
 *
 * checkIfFollowingUsers
 *
 * checkIfFollowingArtists
 *
 * checkIfFollowingPlaylist
 */
interface UsersService {
    @GET("me")
    fun getCurrentUsersProfile(): Call<UserPrivate>


    /**
     * Get the current user’s top artists based on calculated affinity.
     *
     * @param options Optional parameters. For list of available parameters see
     * @return The objects whose response body contains an artists or tracks object.
     *
     * The object in turn contains a paging object of Artists or Tracks
     *
     * [Get User's Top Artists](https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks/)
     */
    @GET("me/top/artists")
    fun getUsersTopArtists(
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<Artist?>>


    /**
     * Get the current user’s top tracks based on calculated affinity.
     *
     * @param options Optional parameters. For list of available parameters see
     * @return The objects whose response body contains an artists or tracks object.
     *
     * The object in turn contains a paging object of Artists or Tracks
     *
     * [Get User's Top Tracks](https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks/)
     */
    @GET("me/top/tracks")
    fun getUsesTopTracks(
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<Track?>>

    /**
     * Get a user's profile information.
     *
     * @param userId The user's User ID
     * @return The user's profile information.
     *
     * [Get User's Profile](https://developer.spotify.com/documentation/web-api/reference/get-users-profile/)
     */
    @GET("users/{id}")
    fun getUsersProfile(@Path("id") userId: String?): Call<UserPublic>

    /**
     * Add the current user as a follower of a playlist.
     *
     * @param playlistId            The Spotify ID of the playlist
     * @param playlistFollowPrivacy The privacy state of the playlist
     * @return An empty result
     *
     * [Follow Playlist](https://developer.spotify.com/documentation/web-api/reference/follow-playlist/)
     */
    @PUT("playlists/{playlist_id}/followers")
    fun followPlaylist(
        @Path("playlist_id") playlistId: String?,
        @Body playlistFollowPrivacy: PlaylistFollowPrivacy?
    ): Call<Result>

    /**
     * Unfollow a Playlist
     *
     * @param playlistId The Spotify ID of the playlist
     * @return An empty result
     *
     * [Unfollow Playlist](https://developer.spotify.com/documentation/web-api/reference/unfollow-playlist/)
     */
    @DELETE("playlists/{playlist_id}/followers")
    fun unfollowPlaylist(
        @Path("playlist_id") playlistId: String?
    ): Call<Result>

    /**
     * Get the current user's followed artists.
     *
     * @param options Optional parameters. For list of supported parameters see
     * @return Object containing a list of artists that user follows wrapped in a cursor object.
     *
     * [Get Followed Artists](https://developer.spotify.com/documentation/web-api/reference/get-followed-artists/)
     */
    @GET("me/following?type=artist")
    fun getFollowedArtists(@QueryMap options: Map<String, String> = emptyMap()): Call<ArtistsCursorPager>

    /**
     * follow users
     */
    @PUT("me/following?type=user")
    fun followUsers(@Query("ids") ids: String?): Call<Result>

    /**
     * follow artists
     */
    @PUT("me/following?type=artist")
    fun followArtists(@Query("ids") ids: String?): Call<Result>


    /**
     * unfollow users
     */
    @DELETE("me/following?type=user")
    fun unfollowUsers(@Query("ids") ids: String?): Call<Result>

    /**
     * unfollow artists
     */
    @DELETE("me/following?type=artist")
    fun unfollowArtists(@Query("ids") ids: String?): Call<Result>

    @GET("me/following/contains?type=user")
    fun checkIfFollowingUsers(@Query("ids") ids: String?): Call<Array<Boolean?>>

    @GET("me/following/contains?type=artist")
    fun checkIfFollowingArtists(@Query("ids") ids: String?): Call<Array<Boolean?>>

    /**
     * Check to see if one or more Spotify users are following a specified playlist.
     *
     * @param playlistId The Spotify ID of the playlist.
     * @param ids        A comma-separated list of the Spotify IDs for the users
     * @return An array with boolean values indicating whether the playlist is followed by the users
     * @see [Check if Users Follow a Playlist](https://developer.spotify.com/documentation/web-api/reference/check-user-following-playlist/)
     */
    @GET("playlists/{playlist_id}/followers/contains")
    fun checkIfFollowingPlaylist(
        @Path("playlist_id") playlistId: String?,
        @Query("ids") ids: String?
    ): Call<Array<Boolean?>>
}
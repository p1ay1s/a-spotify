package com.niki.spotify.web.service

import com.niki.spotify.web.models.Pager
import com.niki.spotify.web.models.Result
import com.niki.spotify.web.models.SavedTrack
import com.niki.spotify.web.models.Track
import com.niki.spotify.web.models.Tracks
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * getTrack
 *
 * getSeveralTracks
 *
 * getUsersSavedTracks
 *
 * saveTracksForCurrentUser
 *
 * removeUsersSavedTracks
 *
 * checkUsersSavedTracks(@Query("ids") ids: String?)
 */
interface TracksService {
    /**
     * Get Spotify catalog information for a single track identified by their unique Spotify ID.
     *
     * @param trackId The Spotify ID for the track.
     * @param options Optional parameters. For list of supported parameters see
     * @return Requested track information
     *
     * [Get Track](https://developer.spotify.com/documentation/web-api/reference/get-track/)
     */
    @GET("tracks/{id}")
    fun getTrack(
        @Path("id") trackId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Track>

    /**
     * Get Several Tracks
     *
     * @param trackIds A comma-separated list of the Spotify IDs for the tracks
     * @param options  Optional parameters. For list of supported parameters see
     * @return An object whose key is "tracks" and whose value is an array of track objects.
     *
     * [Get Several Tracks](https://developer.spotify.com/documentation/web-api/reference/get-several-tracks/)
     */
    @GET("tracks")
    fun getSeveralTracks(
        @Query("ids") trackIds: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Tracks>

    /**
     * Get a list of the songs saved in the current Spotify user’s “Your Music” library.
     *
     * @param options Optional parameters. For list of supported parameters see
     * @return A paginated list of saved tracks
     *
     * [Get User’s Saved Tracks](https://developer.spotify.com/documentation/web-api/reference/get-users-saved-tracks/)
     */
    @GET("me/tracks")
    fun getUsersSavedTracks(
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Pager<SavedTrack?>>

    /**
     * Save one or more tracks to the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks
     * @return An empty result
     *
     * [Save Tracks for Current User](https://developer.spotify.com/documentation/web-api/reference/save-tracks-user/)
     */
    @PUT("me/tracks")
    fun saveTracksForCurrentUser(@Query("ids") ids: String?): Call<Result>

    /**
     * Remove one or more tracks from the current user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks
     * @return An empty result
     *
     * [Remove User’s Saved Tracks](https://developer.spotify.com/documentation/web-api/reference/remove-tracks-user/)
     */
    @DELETE("me/tracks")
    fun removeUsersSavedTracks(@Query("ids") ids: String?): Call<Result>

    /**
     * Check if one or more tracks is already saved in the current Spotify user’s “Your Music” library.
     *
     * @param ids A comma-separated list of the Spotify IDs for the tracks
     * @return An array with boolean values that indicate whether the tracks are in the current Spotify user’s “Your Music” library.
     *
     * [Check User's Saved Tracks](https://developer.spotify.com/documentation/web-api/reference/check-users-saved-tracks/)
     */
    @GET("me/tracks/contains")
    fun checkUsersSavedTracks(@Query("ids") ids: String?): Call<Array<Boolean?>>
}
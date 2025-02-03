package kaaes.spotify.webapi.android

import kaaes.spotify.webapi.android.annotations.DELETEWITHBODY
import kaaes.spotify.webapi.android.models.Album
import kaaes.spotify.webapi.android.models.Albums
import kaaes.spotify.webapi.android.models.AlbumsPager
import kaaes.spotify.webapi.android.models.Artist
import kaaes.spotify.webapi.android.models.Artists
import kaaes.spotify.webapi.android.models.ArtistsCursorPager
import kaaes.spotify.webapi.android.models.ArtistsPager
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack
import kaaes.spotify.webapi.android.models.AudioFeaturesTracks
import kaaes.spotify.webapi.android.models.CategoriesPager
import kaaes.spotify.webapi.android.models.Category
import kaaes.spotify.webapi.android.models.FeaturedPlaylists
import kaaes.spotify.webapi.android.models.NewReleases
import kaaes.spotify.webapi.android.models.Pager
import kaaes.spotify.webapi.android.models.Playlist
import kaaes.spotify.webapi.android.models.PlaylistFollowPrivacy
import kaaes.spotify.webapi.android.models.PlaylistSimple
import kaaes.spotify.webapi.android.models.PlaylistTrack
import kaaes.spotify.webapi.android.models.PlaylistsPager
import kaaes.spotify.webapi.android.models.Recommendations
import kaaes.spotify.webapi.android.models.Result
import kaaes.spotify.webapi.android.models.SavedAlbum
import kaaes.spotify.webapi.android.models.SavedTrack
import kaaes.spotify.webapi.android.models.SeedsGenres
import kaaes.spotify.webapi.android.models.SnapshotId
import kaaes.spotify.webapi.android.models.Track
import kaaes.spotify.webapi.android.models.Tracks
import kaaes.spotify.webapi.android.models.TracksPager
import kaaes.spotify.webapi.android.models.TracksToRemove
import kaaes.spotify.webapi.android.models.TracksToRemoveWithPosition
import kaaes.spotify.webapi.android.models.UserPrivate
import kaaes.spotify.webapi.android.models.UserPublic
import retrofit.http.Body
import retrofit.http.DELETE
import retrofit.http.GET
import retrofit.http.POST
import retrofit.http.PUT
import retrofit.http.Path
import retrofit.http.Query
import retrofit.http.QueryMap
import retrofit2.Call

/************
 * Profiles *
 */
interface ProfilesService {

    /**
     * Get the currently logged in user profile information.
     * The contents of the User object may differ depending on application's scope.
     *
     * @see [Get Current User's Profile](https://developer.spotify.com/web-api/get-current-users-profile/)
     */
    @GET("/me")
    fun getMe(): Call<UserPrivate>

    /**
     * Get a user's profile information.
     *
     * @param userId   The user's User ID
     * @see [Get User's Public Profile](https://developer.spotify.com/web-api/get-users-profile/)
     */
    @GET("/users/{id}")
    fun getUser(@Path("id") userId: String?): Call<UserPublic>
}

/*************
 * Playlists *
 */
interface PlaylistsService {

    /**
     * Get a list of the playlists owned or followed by the current Spotify user.
     *
     */
    @GET("/me/playlists")
    fun getMyPlaylists(): Call<Pager<PlaylistSimple?>>

    /**
     * Get a list of the playlists owned or followed by the current Spotify user.
     *
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-a-list-of-current-users-playlists/)
     */
    @GET("/me/playlists")
    fun getMyPlaylists(
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<PlaylistSimple?>>

    /**
     * Get a list of the playlists owned or followed by a Spotify user.
     *
     * @param userId   The user's Spotify user ID.
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-list-users-playlists/)
     * @see [Get a List of a User’s Playlists](https://developer.spotify.com/web-api/get-list-users-playlists/)
     */
    @GET("/users/{id}/playlists")
    fun getPlaylists(
        @Path("id") userId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<PlaylistSimple?>>

    /**
     * Get a list of the playlists owned or followed by a Spotify user.
     *
     * @param userId   The user's Spotify user ID.
     * @see [Get a List of a User’s Playlists](https://developer.spotify.com/web-api/get-list-users-playlists/)
     */
    @GET("/users/{id}/playlists")
    fun getPlaylists(@Path("id") userId: String?): Call<Pager<PlaylistSimple?>>

    /**
     * Get a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @param options    Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-playlist/)
     * @see [Get a Playlist](https://developer.spotify.com/web-api/get-playlist/)
     */
    @GET("/users/{user_id}/playlists/{playlist_id}")
    fun getPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Playlist>

    /**
     * Get a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @see [Get a Playlist](https://developer.spotify.com/web-api/get-playlist/)
     */
    @GET("/users/{user_id}/playlists/{playlist_id}")
    fun getPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ): Call<Playlist>

    /**
     * Get full details of the tracks of a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @param options    Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-playlists-tracks/)
     * @see [Get a Playlist’s Tracks](https://developer.spotify.com/web-api/get-playlists-tracks/)
     */
    @GET("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun getPlaylistTracks(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<PlaylistTrack?>>

    /**
     * Get full details of the tracks of a playlist owned by a Spotify user.
     *
     * @param userId     The user's Spotify user ID.
     * @param playlistId The Spotify ID for the playlist.
     * @see [Get a Playlist’s Tracks](https://developer.spotify.com/web-api/get-playlists-tracks/)
     */
    @GET("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun getPlaylistTracks(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ): Call<Pager<PlaylistTrack?>>

    /**
     * Create a playlist
     *
     * @param userId   The playlist's owner's User ID
     * @param body     The body parameters
     * @see [Create a Playlist](https://developer.spotify.com/web-api/create-playlist/)
     */
    @POST("/users/{user_id}/playlists")
    fun createPlaylist(
        @Path("user_id") userId: String?,
        @Body body: Map<String?, Any?>?
    ): Call<Playlist>
//    /**
//     * Add tracks to a playlist
//     *
//     * @param userId          The owner of the playlist
//     * @param playlistId      The playlist's ID
//     * @param queryParameters Query parameters
//     * @param body            The body parameters
//     * @return A snapshot ID (the version of the playlist)
//     * @see [Add Tracks to a Playlist](https://developer.spotify.com/web-api/add-tracks-to-playlist/)
//     */
//    @POST("/users/{user_id}/playlists/{playlist_id}/tracks")
//    fun addTracksToPlaylist(
//        @Path("user_id") userId: String?,
//        @Path("playlist_id") playlistId: String?,
//        @QueryMap queryParameters: Map<String?, Any?>?,
//        @Body body: Map<String?, Any?>?
//    ): Call<SnapshotId>

    /**
     * Add tracks to a playlist
     *
     * @param userId          The owner of the playlist
     * @param playlistId      The playlist's Id
     * @param queryParameters Query parameters
     * @param body            The body parameters
     * @see [Add Tracks to a Playlist](https://developer.spotify.com/web-api/add-tracks-to-playlist/)
     */
    @POST("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun addTracksToPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @QueryMap queryParameters: Map<String?, Any?>?,
        @Body body: Map<String?, Any?>?
    ): Call<Pager<PlaylistTrack?>>

    /**
     * Remove one or more tracks from a user’s playlist.
     *
     * @param userId         The owner of the playlist
     * @param playlistId     The playlist's Id
     * @param tracksToRemove A list of tracks to remove
     * @see [Remove Tracks from a Playlist](https://developer.spotify.com/web-api/remove-tracks-playlist/)
     */
    @DELETEWITHBODY("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun removeTracksFromPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body tracksToRemove: TracksToRemove?
    ): Call<SnapshotId>

    /**
     * Remove one or more tracks from a user’s playlist.
     *
     * @param userId                     The owner of the playlist
     * @param playlistId                 The playlist's Id
     * @param tracksToRemoveWithPosition A list of tracks to remove, together with their specific positions
     * @see [Remove Tracks from a Playlist](https://developer.spotify.com/web-api/remove-tracks-playlist/)
     */
    @DELETEWITHBODY("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun removeTracksFromPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body tracksToRemoveWithPosition: TracksToRemoveWithPosition?
    ): Call<SnapshotId>

    /**
     * Replace all the tracks in a playlist, overwriting its existing tracks. This powerful request can be useful for
     * replacing tracks, re-ordering existing tracks, or clearing the playlist.
     *
     * @param userId     The owner of the playlist
     * @param playlistId The playlist's Id
     * @param trackUris  A list of comma-separated track uris
     * @see [Replace a Playlist’s Tracks](https://developer.spotify.com/web-api/replace-playlists-tracks/)
     */
    @PUT("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun replaceTracksInPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Query("uris") trackUris: String?,
        @Body body: Any?
    ): Call<Result>

    /**
     * Change a playlist’s name and public/private state. (The user must, of course, own the playlist.)
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The playlist's Id
     * @param body       The body parameters. For list of supported parameters see [endpoint documentation](https://developer.spotify.com/web-api/change-playlist-details/)
     * @see [Change a Playlist's Details](https://developer.spotify.com/web-api/change-playlist-details/)
     */
    @PUT("/users/{user_id}/playlists/{playlist_id}")
    fun changePlaylistDetails(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body body: Map<String?, Any?>?
    ): Call<Result>

    /**
     * Add the current user as a follower of a playlist.
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The Spotify ID of the playlist
     * @see [Follow a Playlist](https://developer.spotify.com/web-api/follow-playlist/)
     */
    @PUT("/users/{user_id}/playlists/{playlist_id}/followers")
    fun followPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ): Call<Result>

    /**
     * Add the current user as a follower of a playlist.
     *
     * @param userId                The Spotify user ID of the user who owns the playlist.
     * @param playlistId            The Spotify ID of the playlist
     * @param playlistFollowPrivacy The privacy state of the playlist
     * @see [Follow a Playlist](https://developer.spotify.com/web-api/follow-playlist/)
     */
    @PUT("/users/{user_id}/playlists/{playlist_id}/followers")
    fun followPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body playlistFollowPrivacy: PlaylistFollowPrivacy?
    ): Call<Result>

    /**
     * Unfollow a Playlist
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The Spotify ID of the playlist
     * @see [Unfollow a Playlist](https://developer.spotify.com/web-api/unfollow-playlist/)
     */
    @DELETE("/users/{user_id}/playlists/{playlist_id}/followers")
    fun unfollowPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?
    ): Call<Result>

    /**
     * Reorder a Playlist's tracks
     *
     * @param userId     The Spotify user ID of the user who owns the playlist.
     * @param playlistId The Spotify ID of the playlist
     * @param body       The body parameters. For list of supported parameters see [endpoint documentation](https://developer.spotify.com/web-api/reorder-playlists-tracks/)
     * @see [Reorder a Playlist](https://developer.spotify.com/web-api/reorder-playlists-tracks/)
     */
    @PUT("/users/{user_id}/playlists/{playlist_id}/tracks")
    fun reorderPlaylistTracks(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Body body: Map<String?, Any?>?
    ): Call<SnapshotId>
}

/**********
 * Albums *
 */
interface AlbumsService {
    /**
     * Get Spotify catalog information for a single album.
     *
     * @param albumId  The Spotify ID for the album.
     * @see [Get an Album](https://developer.spotify.com/web-api/get-album/)
     */
    @GET("/albums/{id}")
    fun getAlbum(@Path("id") albumId: String?): Call<Album>

    /**
     * Get Spotify catalog information for a single album.
     *
     * @param albumId  The Spotify ID for the album.
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-album/)
     * @see [Get an Album](https://developer.spotify.com/web-api/get-album/)
     */
    @GET("/albums/{id}")
    fun getAlbum(
        @Path("id") albumId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Album>

    /**
     * Get Spotify catalog information for multiple albums identified by their Spotify IDs.
     *
     * @param albumIds A comma-separated list of the Spotify IDs for the albums
     * @see [Get Several Albums](https://developer.spotify.com/web-api/get-several-albums/)
     */
    @GET("/albums")
    fun getAlbums(@Query("ids") albumIds: String?): Call<Albums>

    /**
     * Get Spotify catalog information for multiple albums identified by their Spotify IDs.
     *
     * @param albumIds A comma-separated list of the Spotify IDs for the albums
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-several-albums/)
     * @see [Get Several Albums](https://developer.spotify.com/web-api/get-several-albums/)
     */
    @GET("/albums")
    fun getAlbums(
        @Query("ids") albumIds: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Albums>

    /**
     * Get Spotify catalog information about an album’s tracks.
     *
     * @param albumId  The Spotify ID for the album.
     * @see [Get an Album’s Tracks](https://developer.spotify.com/web-api/get-albums-tracks/)
     */
    @GET("/albums/{id}/tracks")
    fun getAlbumTracks(@Path("id") albumId: String?): Call<Pager<Track?>>

    /**
     * Get Spotify catalog information about an album’s tracks.
     *
     * @param albumId  The Spotify ID for the album.
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-albums-tracks/)
     * @see [Get an Album’s Tracks](https://developer.spotify.com/web-api/get-albums-tracks/)
     */
    @GET("/albums/{id}/tracks")
    fun getAlbumTracks(
        @Path("id") albumId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<Track?>>
}

/***********
 * Artists *
 */
interface ArtistsService {
    /**
     * Get Spotify catalog information for a single artist identified by their unique Spotify ID.
     *
     * @param artistId The Spotify ID for the artist.
     * @see [Get an Artist](https://developer.spotify.com/web-api/get-artist/)
     */
    @GET("/artists/{id}")
    fun getArtist(@Path("id") artistId: String?): Call<Artist>

    /**
     * Get Spotify catalog information for several artists based on their Spotify IDs.
     *
     * @param artistIds A comma-separated list of the Spotify IDs for the artists
     * @see [Get Several Artists](https://developer.spotify.com/web-api/get-several-artists/)
     */
    @GET("/artists")
    fun getArtists(@Query("ids") artistIds: String?): Call<Artists>

    /**
     * Get Spotify catalog information about an artist’s albums.
     *
     * @param artistId The Spotify ID for the artist.
     * @see [Get an Artist's Albums](https://developer.spotify.com/web-api/get-artists-albums/)
     */
    @GET("/artists/{id}/albums")
    fun getArtistAlbums(@Path("id") artistId: String?): Call<Pager<Album?>>

    /**
     * Get Spotify catalog information about an artist’s albums.
     *
     * @param artistId The Spotify ID for the artist.
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-artists-albums/)
     * @see [Get an Artist's Albums](https://developer.spotify.com/web-api/get-artists-albums/)
     */
    @GET("/artists/{id}/albums")
    fun getArtistAlbums(
        @Path("id") artistId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<Album?>>

    /**
     * Get Spotify catalog information about an artist’s top tracks by country.
     *
     * @param artistId The Spotify ID for the artist.
     * @param country  The country: an ISO 3166-1 alpha-2 country code.
     * @see [Get an Artist’s Top Tracks](https://developer.spotify.com/web-api/get-artists-top-tracks/)
     */
    @GET("/artists/{id}/top-tracks")
    fun getArtistTopTrack(
        @Path("id") artistId: String?,
        @Query("country") country: String?
    ): Call<Tracks>

    /**
     * Get Spotify catalog information about artists similar to a given artist.
     *
     * @param artistId The Spotify ID for the artist.
     * @see [Get an Artist’s Related Artists](https://developer.spotify.com/web-api/get-related-artists/)
     */
    @GET("/artists/{id}/related-artists")
    fun getRelatedArtists(@Path("id") artistId: String?): Call<Artists>
}

/**********
 * Tracks *
 */
interface TracksService {

    /**
     * Get Spotify catalog information for a single track identified by their unique Spotify ID.
     *
     * @param trackId  The Spotify ID for the track.
     * @see [Get a Track](https://developer.spotify.com/web-api/get-track/)
     */
    @GET("/tracks/{id}")
    fun getTrack(@Path("id") trackId: String?): Call<Track>

    /**
     * Get Spotify catalog information for a single track identified by their unique Spotify ID.
     *
     * @param trackId  The Spotify ID for the track.
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-track/)
     * @see [Get a Track](https://developer.spotify.com/web-api/get-track/)
     */
    @GET("/tracks/{id}")
    fun getTrack(
        @Path("id") trackId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Track>

    /**
     * Get Several Tracks
     *
     * @param trackIds A comma-separated list of the Spotify IDs for the tracks
     * @see [Get Several Tracks](https://developer.spotify.com/web-api/get-several-tracks/)
     */
    @GET("/tracks")
    fun getTracks(@Query("ids") trackIds: String?): Call<Tracks>

    /**
     * Get Several Tracks
     *
     * @param trackIds A comma-separated list of the Spotify IDs for the tracks
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-several-tracks/)
     * @see [Get Several Tracks](https://developer.spotify.com/web-api/get-several-tracks/)
     */
    @GET("/tracks")
    fun getTracks(
        @Query("ids") trackIds: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Tracks>
}

/**********
 * Browse *
 */
interface BrowseService {
    /**
     * Get a list of Spotify featured playlists (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @see [Get a List of Featured Playlists](https://developer.spotify.com/web-api/get-list-featured-playlists/)
     */
    @GET("/browse/featured-playlists")
    fun getFeaturedPlaylists(): Call<FeaturedPlaylists>

    /**
     * Get a list of Spotify featured playlists (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-list-featured-playlists/)
     * @see [Get a List of Featured Playlists](https://developer.spotify.com/web-api/get-list-featured-playlists/)
     */
    @GET("/browse/featured-playlists")
    fun getFeaturedPlaylists(
        @QueryMap options: Map<String?, Any?>?
    ): Call<FeaturedPlaylists>

    /**
     * Get a list of new album releases featured in Spotify (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @see [Get a List of New Releases](https://developer.spotify.com/web-api/get-list-new-releases/)
     */
    @GET("/browse/new-releases")
    fun getNewReleases(): Call<NewReleases>

    /**
     * Get a list of new album releases featured in Spotify (shown, for example, on a Spotify player’s “Browse” tab).
     *
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-list-new-releases/)
     * @see [Get a List of New Releases](https://developer.spotify.com/web-api/get-list-new-releases/)
     */
    @GET("/browse/new-releases")
    fun getNewReleases(@QueryMap options: Map<String?, Any?>?): Call<NewReleases>

    /**
     * Retrieve Spotify categories. Categories used to tag items in
     * Spotify (on, for example, the Spotify player’s “Browse” tab).
     *
     * @param options  Optional parameters.
     * @see [Get a List of Categories](https://developer.spotify.com/web-api/get-list-categories/)
     */
    @GET("/browse/categories")
    fun getCategories(@QueryMap options: Map<String?, Any?>?): Call<CategoriesPager>

    /**
     * Retrieve a Spotify category.
     *
     * @param categoryId The category's ID.
     * @param options    Optional parameters.
     * @see [Get a Spotify Category](https://developer.spotify.com/web-api/get-category/)
     */
    @GET("/browse/categories/{category_id}")
    fun getCategory(
        @Path("category_id") categoryId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<Category>

    /**
     * Retrieve playlists for a Spotify Category.
     *
     * @param categoryId The category's ID.
     * @param options    Optional parameters.
     * @see [Get playlists for a Spotify Category](https://developer.spotify.com/web-api/get-categorys-playlists/)
     */
    @GET("/browse/categories/{category_id}/playlists")
    fun getPlaylistsForCategory(
        @Path("category_id") categoryId: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<PlaylistsPager>
}

/************************
 * Library / Your Music *
 */
interface LibraryService {

    /**
     * Get a list of the songs saved in the current Spotify user’s “Your Music” library.
     *
     * @see [Get a User’s Saved Tracks](https://developer.spotify.com/web-api/get-users-saved-tracks/)
     */
    @GET("/me/tracks")
    fun getMySavedTracks(): Call<Pager<SavedTrack?>>

    /**
     * Get a list of the songs saved in the current Spotify user’s “Your Music” library.
     *
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-saved-tracks/)
     * @see [Get a User’s Saved Tracks](https://developer.spotify.com/web-api/get-users-saved-tracks/)
     */
    @GET("/me/tracks")
    fun getMySavedTracks(
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<SavedTrack?>>

    /**
     * Check if one or more tracks is already saved in the current Spotify user’s “Your Music” library.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the tracks
     * @see [Check User’s Saved Tracks](https://developer.spotify.com/web-api/check-users-saved-tracks/)
     */
    @GET("/me/tracks/contains")
    fun containsMySavedTracks(@Query("ids") ids: String?): Call<BooleanArray>

    /**
     * Save one or more tracks to the current user’s “Your Music” library.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the tracks
     * @see [Save Tracks for User](https://developer.spotify.com/web-api/save-tracks-user/)
     */
    @PUT("/me/tracks")
    fun addToMySavedTracks(@Query("ids") ids: String?): Call<Any>

    /**
     * Remove one or more tracks from the current user’s “Your Music” library.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the tracks
     * @see [Remove User’s Saved Tracks](https://developer.spotify.com/web-api/remove-tracks-user/)
     */
    @DELETE("/me/tracks")
    fun removeFromMySavedTracks(@Query("ids") ids: String?): Call<Any>

    /**
     * Get a list of the albums saved in the current Spotify user’s “Your Music” library.
     *
     * @see [Get a User’s Saved Albums](https://developer.spotify.com/web-api/get-users-saved-albums/)
     */
    @GET("/me/albums")
    fun getMySavedAlbums(): Call<Pager<SavedAlbum?>>

    /**
     * Get a list of the albums saved in the current Spotify user’s “Your Music” library.
     *
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-saved-albums/)
     * @see [Get a User’s Saved Albums](https://developer.spotify.com/web-api/get-users-saved-albums/)
     */
    @GET("/me/albums")
    fun getMySavedAlbums(
        @QueryMap options: Map<String?, Any?>?
    ): Call<Pager<SavedAlbum?>>

    /**
     * Check if one or more albums is already saved in the current Spotify user’s “Your Music” library.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the albums
     * @see [Check User’s Saved Albums](https://developer.spotify.com/web-api/check-users-saved-albums/)
     */
    @GET("/me/albums/contains")
    fun containsMySavedAlbums(@Query("ids") ids: String?): Call<BooleanArray>

    /**
     * Save one or more albums to the current user’s “Your Music” library.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the albums
     * @see [Save Albums for User](https://developer.spotify.com/web-api/save-albums-user/)
     */
    @PUT("/me/albums")
    fun addToMySavedAlbums(@Query("ids") ids: String?): Call<Any>

    /**
     * Remove one or more albums from the current user’s “Your Music” library.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the albums
     * @see [Remove User’s Saved Albums](https://developer.spotify.com/web-api/remove-albums-user/)
     */
    @DELETE("/me/albums")
    fun removeFromMySavedAlbums(@Query("ids") ids: String?): Call<Any>
}

/**********
 * Follow *
 */
interface FollowService {
    /**
     * Add the current user as a follower of one or more Spotify users.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the users
     * @see [Follow Artists or Users](https://developer.spotify.com/web-api/follow-artists-users/)
     */
    @PUT("/me/following?type=user")
    fun followUsers(@Query("ids") ids: String?): Call<Any>

    /**
     * Add the current user as a follower of one or more artists.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the artists
     * @see [Follow Artists or Users](https://developer.spotify.com/web-api/follow-artists-users/)
     */
    @PUT("/me/following?type=artist")
    fun followArtists(@Query("ids") ids: String?): Call<Any>

    /**
     * Remove the current user as a follower of one or more Spotify users.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the users
     * @see [Unfollow Artists or Users](https://developer.spotify.com/web-api/unfollow-artists-users/)
     */
    @DELETE("/me/following?type=user")
    fun unfollowUsers(@Query("ids") ids: String?): Call<Any>

    /**
     * Remove the current user as a follower of one or more Spotify artists.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the artists
     * @see [Unfollow Artists or Users](https://developer.spotify.com/web-api/unfollow-artists-users/)
     */
    @DELETE("/me/following?type=artist")
    fun unfollowArtists(@Query("ids") ids: String?): Call<Any>

    /**
     * Check to see if the current user is following one or more other Spotify users.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the users
     * @see [Check if Current User Follows Artists or Users](https://developer.spotify.com/web-api/check-current-user-follows/)
     */
    @GET("/me/following/contains?type=user")
    fun isFollowingUsers(@Query("ids") ids: String?): Call<BooleanArray>

    /**
     * Check to see if the current user is following one or more other Spotify artists.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the artists
     * @see [Check if Current User Follows Artists or Users](https://developer.spotify.com/web-api/check-current-user-follows/)
     */
    @GET("/me/following/contains?type=artist")
    fun isFollowingArtists(@Query("ids") ids: String?): Call<BooleanArray>

    /**
     * Check to see if one or more Spotify users are following a specified playlist.
     *
     * @param userId     The Spotify user ID of the person who owns the playlist.
     * @param playlistId The Spotify ID of the playlist.
     * @param ids        A comma-separated list of the Spotify IDs for the users
     * @see [Check if Users Follow a Playlist](https://developer.spotify.com/web-api/check-user-following-playlist/)
     */
    @GET("/users/{user_id}/playlists/{playlist_id}/followers/contains")
    fun areFollowingPlaylist(
        @Path("user_id") userId: String?,
        @Path("playlist_id") playlistId: String?,
        @Query("ids") ids: String?
    ): Call<BooleanArray>

    /**
     * Get the current user's followed artists.
     *
     * @return An empty result
     * @see [Get User's Followed Artists](https://developer.spotify.com/web-api/get-followed-artists/)
     */
    @GET("/me/following?type=artist")
    fun getFollowedArtists(): Call<ArtistsCursorPager>

    /**
     * Get the current user's followed artists.
     *
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-followed-artists/)
     * @return An empty result
     * @see [Get User's Followed Artists](https://developer.spotify.com/web-api/get-followed-artists/)
     */
    @GET("/me/following?type=artist")
    fun getFollowedArtists(@QueryMap options: Map<String?, Any?>?): Call<ArtistsCursorPager>
}

/**********
 * Search *
 */
interface SearchService {
    /**
     * Get Spotify catalog information about tracks that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=track")
    fun searchTracks(@Query("q") q: String?): Call<TracksPager>

    /**
     * Get Spotify catalog information about tracks that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=track")
    fun searchTracks(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<TracksPager>

    /**
     * Get Spotify catalog information about artists that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=artist")
    fun searchArtists(@Query("q") q: String?): Call<ArtistsPager>

    /**
     * Get Spotify catalog information about artists that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=artist")
    fun searchArtists(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<ArtistsPager>

    /**
     * Get Spotify catalog information about albums that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=album")
    fun searchAlbums(@Query("q") q: String?): Call<AlbumsPager>

    /**
     * Get Spotify catalog information about albums that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=album")
    fun searchAlbums(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<AlbumsPager>

    /**
     * Get Spotify catalog information about playlists that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=playlist")
    fun searchPlaylists(@Query("q") q: String?): Call<PlaylistsPager>

    /**
     * Get Spotify catalog information about playlists that match a keyword string.
     *
     * @param q        The search query's keywords (and optional field filters and operators), for example "roadhouse+blues"
     * @param options  Optional parameters. For list of supported parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/search-item/)
     * @see [Search for an Item](https://developer.spotify.com/web-api/search-item/)
     */
    @GET("/search?type=playlist")
    fun searchPlaylists(
        @Query("q") q: String?,
        @QueryMap options: Map<String?, Any?>?
    ): Call<PlaylistsPager>
}

/******************
 * AudioFeatures *
 */
interface AudioFeaturesService {

    /**
     * Get audio features for multiple tracks based on their Spotify IDs.
     *
     * @param ids      A comma-separated list of the Spotify IDs for the tracks. Maximum: 100 IDs
     */
    @GET("/audio-features")
    fun getTracksAudioFeatures(
        @Query("ids") ids: String?
    ): Call<AudioFeaturesTracks>

    /**
     * Get audio feature information for a single track identified by its unique Spotify ID.
     *
     * @param id       The Spotify ID for the track.
     */
    @GET("/audio-features/{id}")
    fun getTrackAudioFeatures(@Path("id") id: String?): Call<AudioFeaturesTrack>
}

/*******************
 * Recommendations *
 */
interface RecommendationsService {
    /**
     * Create a playlist-style listening experience based on seed artists, tracks and genres.
     *
     * @param options  Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-recommendations/)
     */
    @GET("/recommendations")
    fun getRecommendations(
        @QueryMap options: Map<String?, Any?>?
    ): Call<Recommendations>

    /**
     * Retrieve a list of available genres seed parameter values for recommendations.
     *
     */
    @GET("/recommendations/available-genre-seeds")
    fun getSeedsGenres(): Call<SeedsGenres>
}

/*****************************
 * User Top Artists & Tracks *
 */
interface UserService {
    /**
     * Get the current user’s top artists based on calculated affinity.
     *
     * @param options  Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-top-artists-and-tracks/)
     */
    @GET("/me/top/artists")
    fun getTopArtists(@QueryMap options: Map<String?, Any?>?): Call<Pager<Artist?>>

    /**
     * Get the current user’s top tracks based on calculated affinity.
     *
     */
    @GET("/me/top/tracks")
    fun getTopTracks(): Call<Pager<Track?>>

    /**
     * Get the current user’s top tracks based on calculated affinity.
     *
     * @param options  Optional parameters. For list of available parameters see
     * [endpoint documentation](https://developer.spotify.com/web-api/get-users-top-artists-and-tracks/)
     */
    @GET("/me/top/tracks")
    fun getTopTracks(@QueryMap options: Map<String?, Any?>?): Call<Pager<Track?>>
}


/**
 * The maximum number of objects to return.
 */
const val LIMIT: String = "limit"

/**
 * The index of the first playlist to return. Default: 0 (the first object).
 * Use with limit to get the next set of objects (albums, playlists, etc).
 */
const val OFFSET: String = "offset"

/**
 * A comma-separated list of keywords that will be used to filter the response.
 * Valid values are: `album`, `single`, `appears_on`, `compilation`
 */
const val ALBUM_TYPE: String = "album_type"

/**
 * The country: an ISO 3166-1 alpha-2 country code.
 * Limit the response to one particular geographical market.
 * Synonym to [.COUNTRY]
 */
const val MARKET: String = "market"

/**
 * Same as [.MARKET]
 */
const val COUNTRY: String = "country"

/**
 * The desired language, consisting of a lowercase ISO 639 language code
 * and an uppercase ISO 3166-1 alpha-2 country code, joined by an underscore.
 * For example: es_MX, meaning "Spanish (Mexico)".
 */
const val LOCALE: String = "locale"

/*
  Filters for the query: a comma-separated list of the fields to return.
  If omitted, all fields are returned.

  查询的过滤器: 要返回的字段的逗号分隔列表
  如果省略, 则返回所有字段
 */
const val FIELDS: String = "fields"

/*
  A timestamp in ISO 8601 format: yyyy-MM-ddTHH:mm:ss.
  Use this parameter to specify the user's local time to get results tailored for that specific date and time in the day.
  If not provided, the response defaults to the current UTC time

  ISO 8601 格式的时间戳: yyyy-MM-ddTHH:mm:ss
  使用此参数可指定用户的当地时间, 以获取为一天中的特定日期和时间量身定制的结果
  如果未提供, 则响应默认为当前 UTC 时间
 */
const val TIMESTAMP: String = "timestamp"

const val TIME_RANGE: String = "time_range"
package com.niki.app.net.web_api

//import kaaes.spotify.webapi.android.annotations.DELETEWITHBODY
//import kaaes.spotify.webapi.android.models.Album
//import kaaes.spotify.webapi.android.models.Albums
//import kaaes.spotify.webapi.android.models.AlbumsPager
//import kaaes.spotify.webapi.android.models.Artist
//import kaaes.spotify.webapi.android.models.Artists
//import kaaes.spotify.webapi.android.models.ArtistsCursorPager
//import kaaes.spotify.webapi.android.models.ArtistsPager
//import kaaes.spotify.webapi.android.models.AudioFeaturesTrack
//import kaaes.spotify.webapi.android.models.AudioFeaturesTracks
//import kaaes.spotify.webapi.android.models.CategoriesPager
//import kaaes.spotify.webapi.android.models.Category
//import kaaes.spotify.webapi.android.models.FeaturedPlaylists
//import kaaes.spotify.webapi.android.models.NewReleases
//import kaaes.spotify.webapi.android.models.PlaylistFollowPrivacy
//import kaaes.spotify.webapi.android.models.PlaylistSimple
//import kaaes.spotify.webapi.android.models.PlaylistTrack
//import kaaes.spotify.webapi.android.models.PlaylistsPager
//import kaaes.spotify.webapi.android.models.Recommendations
//import kaaes.spotify.webapi.android.models.Result
//import kaaes.spotify.webapi.android.models.SavedAlbum
//import kaaes.spotify.webapi.android.models.SavedTrack
//import kaaes.spotify.webapi.android.models.SeedsGenres
//import kaaes.spotify.webapi.android.models.SnapshotId
//import kaaes.spotify.webapi.android.models.Track
//import kaaes.spotify.webapi.android.models.Tracks
//import kaaes.spotify.webapi.android.models.TracksPager
//import kaaes.spotify.webapi.android.models.TracksToRemove
//import kaaes.spotify.webapi.android.models.TracksToRemoveWithPosition
//import retrofit2.http.DELETE
//import retrofit2.http.PUT
//import retrofit2.http.Query
import kaaes.spotify.webapi.android.models.Pager
import kaaes.spotify.webapi.android.models.Playlist
import kaaes.spotify.webapi.android.models.PlaylistSimple
import kaaes.spotify.webapi.android.models.PlaylistTrack
import kaaes.spotify.webapi.android.models.UserPrivate
import kaaes.spotify.webapi.android.models.UserPublic
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface SService {

    companion object {
        const val LIMIT: String = "limit"
        const val OFFSET: String = "offset"
        const val ALBUM_TYPE: String = "album_type"
        const val MARKET: String = "market"
        const val COUNTRY: String = "country"
        const val LOCALE: String = "locale"
        const val FIELDS: String = "fields"
        const val TIMESTAMP: String = "timestamp"
        const val TIME_RANGE: String = "time_range"
    }

    @GET("me")
    fun getMe(): Call<UserPrivate>

//    @get:GET("me")
//    val me: UserPrivate?

    @GET("users/{id}")
    fun getUser(@Path("id") var1: String?): Call<UserPublic>

//    @GET("users/{id}")
//    fun getUser(@Path("id") var1: String?): UserPublic?

    @GET("me/playlists")
    fun getMyPlaylists(): Call<Pager<PlaylistSimple?>>

//    @get:GET("me/playlists")
//    val myPlaylists: Pager<PlaylistSimple?>?

    @GET("me/playlists")
    fun getMyPlaylists(
        @QueryMap var1: Map<String?, Any?>?
    ): Call<Pager<PlaylistSimple?>>

//    @GET("me/playlists")
//    fun getMyPlaylists(@QueryMap var1: Map<String?, Any?>?): Pager<PlaylistSimple?>?

    @GET("users/{id}/playlists")
    fun getPlaylists(
        @Path("id") var1: String?,
        @QueryMap var2: Map<String?, Any?>?
    ): Call<Pager<PlaylistSimple?>>

//    @GET("users/{id}/playlists")
//    fun getPlaylists(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?
//    ): Pager<PlaylistSimple?>?

    @GET("users/{id}/playlists")
    fun getPlaylists(@Path("id") var1: String?): Call<Pager<PlaylistSimple?>>

//    @GET("users/{id}/playlists")
//    fun getPlaylists(@Path("id") var1: String?): Pager<PlaylistSimple?>?

    @GET("users/{user_id}/playlists/{playlist_id}")
    fun getPlaylist(
        @Path("user_id") var1: String?,
        @Path("playlist_id") var2: String?,
        @QueryMap var3: Map<String?, Any?>?
    ): Call<Playlist>

//    @GET("users/{user_id}/playlists/{playlist_id}")
//    fun getPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @QueryMap var3: Map<String?, Any?>?
//    ): Playlist?

    @GET("users/{user_id}/playlists/{playlist_id}")
    fun getPlaylist(
        @Path("user_id") var1: String?,
        @Path("playlist_id") var2: String?
    ): Call<Playlist>

//    @GET("users/{user_id}/playlists/{playlist_id}")
//    fun getPlaylist(@Path("user_id") var1: String?, @Path("playlist_id") var2: String?): Playlist?

    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
    fun getPlaylistTracks(
        @Path("user_id") var1: String?,
        @Path("playlist_id") var2: String?,
        @QueryMap var3: Map<String?, Any?>?
    ): Call<Pager<PlaylistTrack?>>

//    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun getPlaylistTracks(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @QueryMap var3: Map<String?, Any?>?
//    ): Pager<PlaylistTrack?>?

    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
    fun getPlaylistTracks(
        @Path("user_id") var1: String?,
        @Path("playlist_id") var2: String?
    ): Call<Pager<PlaylistTrack?>>

//    @GET("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun getPlaylistTracks(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?
//    ): Pager<PlaylistTrack?>?

    @POST("users/{user_id}/playlists")
    fun createPlaylist(
        @Path("user_id") var1: String?,
        @Body var2: Map<String?, Any?>?
    ): Call<Playlist>

//    @POST("users/{user_id}/playlists")
//    fun createPlaylist(@Path("user_id") var1: String?, @Body var2: Map<String?, Any?>?): Playlist?
//
//    @POST("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun addTracksToPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @QueryMap var3: Map<String?, Any?>?,
//        @Body var4: Map<String?, Any?>?
//    ): SnapshotId?
//
//    @POST("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun addTracksToPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @QueryMap var3: Map<String?, Any?>?,
//        @Body var4: Map<String?, Any?>?,
//        var5: Callback<Pager<PlaylistTrack?>?>?
//    )
//
//    @DELETEWITHBODY("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun removeTracksFromPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: TracksToRemove?,
//        var4: Callback<SnapshotId?>?
//    )
//
//    @DELETEWITHBODY("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun removeTracksFromPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: TracksToRemove?
//    ): SnapshotId?
//
//    @DELETEWITHBODY("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun removeTracksFromPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: TracksToRemoveWithPosition?,
//        var4: Callback<SnapshotId?>?
//    )
//
//    @DELETEWITHBODY("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun removeTracksFromPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: TracksToRemoveWithPosition?
//    ): SnapshotId?
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun replaceTracksInPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Query("uris") var3: String?,
//        @Body var4: Any?,
//        var5: Callback<Result?>?
//    )
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun replaceTracksInPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Query("uris") var3: String?,
//        @Body var4: Any?
//    ): Result?
//
//    @PUT("users/{user_id}/playlists/{playlist_id}")
//    fun changePlaylistDetails(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: Map<String?, Any?>?
//    ): Result?
//
//    @PUT("users/{user_id}/playlists/{playlist_id}")
//    fun changePlaylistDetails(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: Map<String?, Any?>?,
//        var4: Callback<Result?>?
//    )
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/followers")
//    fun followPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        var3: Callback<Result?>?
//    )
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/followers")
//    fun followPlaylist(@Path("user_id") var1: String?, @Path("playlist_id") var2: String?): Result?
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/followers")
//    fun followPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: PlaylistFollowPrivacy?,
//        var4: Callback<Result?>?
//    )
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/followers")
//    fun followPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: PlaylistFollowPrivacy?
//    ): Result?
//
//    @DELETE("users/{user_id}/playlists/{playlist_id}/followers")
//    fun unfollowPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        var3: Callback<Result?>?
//    )
//
//    @DELETE("users/{user_id}/playlists/{playlist_id}/followers")
//    fun unfollowPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?
//    ): Result?
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun reorderPlaylistTracks(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: Map<String?, Any?>?
//    ): SnapshotId?
//
//    @PUT("users/{user_id}/playlists/{playlist_id}/tracks")
//    fun reorderPlaylistTracks(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Body var3: Map<String?, Any?>?,
//        var4: Callback<SnapshotId?>?
//    )
//
//    @GET("albums/{id}")
//    fun getAlbum(@Path("id") var1: String?, var2: Callback<Album?>?)
//
//    @GET("albums/{id}")
//    fun getAlbum(@Path("id") var1: String?): Album?
//
//    @GET("albums/{id}")
//    fun getAlbum(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Album?>?
//    )
//
//    @GET("albums/{id}")
//    fun getAlbum(@Path("id") var1: String?, @QueryMap var2: Map<String?, Any?>?): Album?
//
//    @GET("albums")
//    fun getAlbums(@Query("ids") var1: String?, var2: Callback<Albums?>?)
//
//    @GET("albums")
//    fun getAlbums(@Query("ids") var1: String?): Albums?
//
//    @GET("albums")
//    fun getAlbums(
//        @Query("ids") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Albums?>?
//    )
//
//    @GET("albums")
//    fun getAlbums(@Query("ids") var1: String?, @QueryMap var2: Map<String?, Any?>?): Albums?
//
//    @GET("albums/{id}/tracks")
//    fun getAlbumTracks(@Path("id") var1: String?): Pager<Track?>?
//
//    @GET("albums/{id}/tracks")
//    fun getAlbumTracks(@Path("id") var1: String?, var2: Callback<Pager<Track?>?>?)
//
//    @GET("albums/{id}/tracks")
//    fun getAlbumTracks(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Pager<Track?>?>?
//    )
//
//    @GET("albums/{id}/tracks")
//    fun getAlbumTracks(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?
//    ): Pager<Track?>?
//
//    @GET("artists/{id}")
//    fun getArtist(@Path("id") var1: String?, var2: Callback<Artist?>?)
//
//    @GET("artists/{id}")
//    fun getArtist(@Path("id") var1: String?): Artist?
//
//    @GET("artists")
//    fun getArtists(@Query("ids") var1: String?, var2: Callback<Artists?>?)
//
//    @GET("artists")
//    fun getArtists(@Query("ids") var1: String?): Artists?
//
//    @GET("artists/{id}/albums")
//    fun getArtistAlbums(@Path("id") var1: String?, var2: Callback<Pager<Album?>?>?)
//
//    @GET("artists/{id}/albums")
//    fun getArtistAlbums(@Path("id") var1: String?): Pager<Album?>?
//
//    @GET("artists/{id}/albums")
//    fun getArtistAlbums(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Pager<Album?>?>?
//    )
//
//    @GET("artists/{id}/albums")
//    fun getArtistAlbums(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?
//    ): Pager<Album?>?
//
//    @GET("artists/{id}/top-tracks")
//    fun getArtistTopTrack(
//        @Path("id") var1: String?,
//        @Query("country") var2: String?,
//        var3: Callback<Tracks?>?
//    )
//
//    @GET("artists/{id}/top-tracks")
//    fun getArtistTopTrack(@Path("id") var1: String?, @Query("country") var2: String?): Tracks?
//
//    @GET("artists/{id}/related-artists")
//    fun getRelatedArtists(@Path("id") var1: String?, var2: Callback<Artists?>?)
//
//    @GET("artists/{id}/related-artists")
//    fun getRelatedArtists(@Path("id") var1: String?): Artists?
//
//    @GET("tracks/{id}")
//    fun getTrack(@Path("id") var1: String?, var2: Callback<Track?>?)
//
//    @GET("tracks/{id}")
//    fun getTrack(@Path("id") var1: String?): Track?
//
//    @GET("tracks/{id}")
//    fun getTrack(
//        @Path("id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Track?>?
//    )
//
//    @GET("tracks/{id}")
//    fun getTrack(@Path("id") var1: String?, @QueryMap var2: Map<String?, Any?>?): Track?
//
//    @GET("tracks")
//    fun getTracks(@Query("ids") var1: String?, var2: Callback<Tracks?>?)
//
//    @GET("tracks")
//    fun getTracks(@Query("ids") var1: String?): Tracks?
//
//    @GET("tracks")
//    fun getTracks(
//        @Query("ids") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Tracks?>?
//    )
//
//    @GET("tracks")
//    fun getTracks(@Query("ids") var1: String?, @QueryMap var2: Map<String?, Any?>?): Tracks?
//
//    @GET("browse/featured-playlists")
//    fun getFeaturedPlaylists(var1: Callback<FeaturedPlaylists?>?)
//
//    @get:GET("browse/featured-playlists")
//    val featuredPlaylists: FeaturedPlaylists?
//
//    @GET("browse/featured-playlists")
//    fun getFeaturedPlaylists(
//        @QueryMap var1: Map<String?, Any?>?,
//        var2: Callback<FeaturedPlaylists?>?
//    )
//
//    @GET("browse/featured-playlists")
//    fun getFeaturedPlaylists(@QueryMap var1: Map<String?, Any?>?): FeaturedPlaylists?
//
//    @GET("browse/new-releases")
//    fun getNewReleases(var1: Callback<NewReleases?>?)
//
//    @get:GET("browse/new-releases")
//    val newReleases: NewReleases?
//
//    @GET("browse/new-releases")
//    fun getNewReleases(@QueryMap var1: Map<String?, Any?>?, var2: Callback<NewReleases?>?)
//
//    @GET("browse/new-releases")
//    fun getNewReleases(@QueryMap var1: Map<String?, Any?>?): NewReleases?
//
//    @GET("browse/categories")
//    fun getCategories(@QueryMap var1: Map<String?, Any?>?, var2: Callback<CategoriesPager?>?)
//
//    @GET("browse/categories")
//    fun getCategories(@QueryMap var1: Map<String?, Any?>?): CategoriesPager?
//
//    @GET("browse/categories/{category_id}")
//    fun getCategory(
//        @Path("category_id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<Category?>?
//    )
//
//    @GET("browse/categories/{category_id}")
//    fun getCategory(
//        @Path("category_id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?
//    ): Category?
//
//    @GET("browse/categories/{category_id}/playlists")
//    fun getPlaylistsForCategory(
//        @Path("category_id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<PlaylistsPager?>?
//    )
//
//    @GET("browse/categories/{category_id}/playlists")
//    fun getPlaylistsForCategory(
//        @Path("category_id") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?
//    ): PlaylistsPager?
//
//    @GET("me/tracks")
//    fun getMySavedTracks(var1: Callback<Pager<SavedTrack?>?>?)
//
//    @get:GET("me/tracks")
//    val mySavedTracks: Pager<SavedTrack?>?
//
//    @GET("me/tracks")
//    fun getMySavedTracks(@QueryMap var1: Map<String?, Any?>?, var2: Callback<Pager<SavedTrack?>?>?)
//
//    @GET("me/tracks")
//    fun getMySavedTracks(@QueryMap var1: Map<String?, Any?>?): Pager<SavedTrack?>?
//
//    @GET("me/tracks/contains")
//    fun containsMySavedTracks(@Query("ids") var1: String?, var2: Callback<BooleanArray?>?)
//
//    @GET("me/tracks/contains")
//    fun containsMySavedTracks(@Query("ids") var1: String?): Array<Boolean?>?
//
//    @PUT("me/tracks")
//    fun addToMySavedTracks(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @PUT("me/tracks")
//    fun addToMySavedTracks(@Query("ids") var1: String?): Result?
//
//    @DELETE("me/tracks")
//    fun removeFromMySavedTracks(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @DELETE("me/tracks")
//    fun removeFromMySavedTracks(@Query("ids") var1: String?): Result?
//
//    @GET("me/albums")
//    fun getMySavedAlbums(var1: Callback<Pager<SavedAlbum?>?>?)
//
//    @get:GET("me/albums")
//    val mySavedAlbums: Pager<SavedAlbum?>?
//
//    @GET("me/albums")
//    fun getMySavedAlbums(@QueryMap var1: Map<String?, Any?>?, var2: Callback<Pager<SavedAlbum?>?>?)
//
//    @GET("me/albums")
//    fun getMySavedAlbums(@QueryMap var1: Map<String?, Any?>?): Pager<SavedAlbum?>?
//
//    @GET("me/albums/contains")
//    fun containsMySavedAlbums(@Query("ids") var1: String?, var2: Callback<BooleanArray?>?)
//
//    @GET("me/albums/contains")
//    fun containsMySavedAlbums(@Query("ids") var1: String?): Array<Boolean?>?
//
//    @PUT("me/albums")
//    fun addToMySavedAlbums(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @PUT("me/albums")
//    fun addToMySavedAlbums(@Query("ids") var1: String?): Result?
//
//    @DELETE("me/albums")
//    fun removeFromMySavedAlbums(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @DELETE("me/albums")
//    fun removeFromMySavedAlbums(@Query("ids") var1: String?): Result?
//
//    @PUT("me/following?type=user")
//    fun followUsers(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @PUT("me/following?type=user")
//    fun followUsers(@Query("ids") var1: String?): Result?
//
//    @PUT("me/following?type=artist")
//    fun followArtists(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @PUT("me/following?type=artist")
//    fun followArtists(@Query("ids") var1: String?): Result?
//
//    @DELETE("me/following?type=user")
//    fun unfollowUsers(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @DELETE("me/following?type=user")
//    fun unfollowUsers(@Query("ids") var1: String?): Result?
//
//    @DELETE("me/following?type=artist")
//    fun unfollowArtists(@Query("ids") var1: String?, var2: Callback<Any?>?)
//
//    @DELETE("me/following?type=artist")
//    fun unfollowArtists(@Query("ids") var1: String?): Result?
//
//    @GET("me/following/contains?type=user")
//    fun isFollowingUsers(@Query("ids") var1: String?, var2: Callback<BooleanArray?>?)
//
//    @GET("me/following/contains?type=user")
//    fun isFollowingUsers(@Query("ids") var1: String?): Array<Boolean?>?
//
//    @GET("me/following/contains?type=artist")
//    fun isFollowingArtists(@Query("ids") var1: String?, var2: Callback<BooleanArray?>?)
//
//    @GET("me/following/contains?type=artist")
//    fun isFollowingArtists(@Query("ids") var1: String?): Array<Boolean?>?
//
//    @GET("users/{user_id}/playlists/{playlist_id}/followers/contains")
//    fun areFollowingPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Query("ids") var3: String?
//    ): Array<Boolean?>?
//
//    @GET("users/{user_id}/playlists/{playlist_id}/followers/contains")
//    fun areFollowingPlaylist(
//        @Path("user_id") var1: String?,
//        @Path("playlist_id") var2: String?,
//        @Query("ids") var3: String?,
//        var4: Callback<BooleanArray?>?
//    )
//
//    @get:GET("me/following?type=artist")
//    val followedArtists: ArtistsCursorPager?
//
//    @GET("me/following?type=artist")
//    fun getFollowedArtists(var1: Callback<ArtistsCursorPager?>?)
//
//    @GET("me/following?type=artist")
//    fun getFollowedArtists(@QueryMap var1: Map<String?, Any?>?): ArtistsCursorPager?
//
//    @GET("me/following?type=artist")
//    fun getFollowedArtists(
//        @QueryMap var1: Map<String?, Any?>?,
//        var2: Callback<ArtistsCursorPager?>?
//    )
//
//    @GET("search?type=track")
//    fun searchTracks(@Query("q") var1: String?, var2: Callback<TracksPager?>?)
//
//    @GET("search?type=track")
//    fun searchTracks(@Query("q") var1: String?): TracksPager?
//
//    @GET("search?type=track")
//    fun searchTracks(
//        @Query("q") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<TracksPager?>?
//    )
//
//    @GET("search?type=track")
//    fun searchTracks(@Query("q") var1: String?, @QueryMap var2: Map<String?, Any?>?): TracksPager?
//
//    @GET("search?type=artist")
//    fun searchArtists(@Query("q") var1: String?, var2: Callback<ArtistsPager?>?)
//
//    @GET("search?type=artist")
//    fun searchArtists(@Query("q") var1: String?): ArtistsPager?
//
//    @GET("search?type=artist")
//    fun searchArtists(
//        @Query("q") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<ArtistsPager?>?
//    )
//
//    @GET("search?type=artist")
//    fun searchArtists(@Query("q") var1: String?, @QueryMap var2: Map<String?, Any?>?): ArtistsPager?
//
//    @GET("search?type=album")
//    fun searchAlbums(@Query("q") var1: String?, var2: Callback<AlbumsPager?>?)
//
//    @GET("search?type=album")
//    fun searchAlbums(@Query("q") var1: String?): AlbumsPager?
//
//    @GET("search?type=album")
//    fun searchAlbums(
//        @Query("q") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<AlbumsPager?>?
//    )
//
//    @GET("search?type=album")
//    fun searchAlbums(@Query("q") var1: String?, @QueryMap var2: Map<String?, Any?>?): AlbumsPager?
//
//    @GET("search?type=playlist")
//    fun searchPlaylists(@Query("q") var1: String?, var2: Callback<PlaylistsPager?>?)
//
//    @GET("search?type=playlist")
//    fun searchPlaylists(@Query("q") var1: String?): PlaylistsPager?
//
//    @GET("search?type=playlist")
//    fun searchPlaylists(
//        @Query("q") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?,
//        var3: Callback<PlaylistsPager?>?
//    )
//
//    @GET("search?type=playlist")
//    fun searchPlaylists(
//        @Query("q") var1: String?,
//        @QueryMap var2: Map<String?, Any?>?
//    ): PlaylistsPager?
//
//    @GET("audio-features")
//    fun getTracksAudioFeatures(@Query("ids") var1: String?, var2: Callback<AudioFeaturesTracks?>?)
//
//    @GET("audio-features")
//    fun getTracksAudioFeatures(@Query("ids") var1: String?): AudioFeaturesTracks?
//
//    @GET("audio-features/{id}")
//    fun getTrackAudioFeatures(@Path("id") var1: String?, var2: Callback<AudioFeaturesTrack?>?)
//
//    @GET("audio-features/{id}")
//    fun getTrackAudioFeatures(@Path("id") var1: String?): AudioFeaturesTrack?
//
//    @GET("recommendations")
//    fun getRecommendations(@QueryMap var1: Map<String?, Any?>?): Recommendations?
//
//    @GET("recommendations")
//    fun getRecommendations(@QueryMap var1: Map<String?, Any?>?, var2: Callback<Recommendations?>?)
//
//    @get:GET("recommendations/available-genre-seeds")
//    val seedsGenres: SeedsGenres?
//
//    @GET("recommendations/available-genre-seeds")
//    fun getSeedsGenres(var1: Callback<SeedsGenres?>?):Call<SeedsGenres>
//
//    @get:GET("me/top/artists")
//    val topArtists: Pager<Artist?>?
//
//    @GET("me/top/artists")
//    fun getTopArtists(var1: Callback<Pager<Artist?>?>?)
//
//    @GET("me/top/artists")
//    fun getTopArtists(@QueryMap var1: Map<String?, Any?>?): Pager<Artist?>?
//
//    @GET("me/top/artists")
//    fun getTopArtists(@QueryMap var1: Map<String?, Any?>?, var2: Callback<Pager<Artist?>?>?)
//
//    @get:GET("me/top/tracks")
//    val topTracks: Pager<Track?>?
//
//    @GET("me/top/tracks")
//    fun getTopTracks(var1: Callback<Pager<Track?>?>?)
//
//    @GET("me/top/tracks")
//    fun getTopTracks(@QueryMap var1: Map<String?, Any?>?): Pager<Track?>?
//
//    @GET("me/top/tracks")
//    fun getTopTracks(@QueryMap var1: Map<String?, Any?>?, var2: Callback<Pager<Track?>?>?)
}

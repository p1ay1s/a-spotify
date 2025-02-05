package com.niki.spotify.web.service

import com.niki.spotify.web.models.CategoriesPager
import com.niki.spotify.web.models.Category
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * getCategories
 *
 * getCategory
 */
interface CategoriesService {
    /**
     * Retrieve Spotify categories. Categories used to tag items in
     * Spotify (on, for example, the Spotify player’s “Browse” tab).
     *
     * @param options Optional parameters.
     * @return A paging object containing categories.
     *
     * [Get Several Browse Categories](https://developer.spotify.com/documentation/web-api/reference/get-list-categories/)
     */
    @GET("browse/categories")
    fun getCategories(@QueryMap options: Map<String, String> = emptyMap()): Call<CategoriesPager>

    /**
     * Retrieve a Spotify category.
     *
     * @param categoryId The category's ID.
     * @param options    Optional parameters.
     * @return A Spotify category.
     *
     * [Get Single Browse Category](https://developer.spotify.com/documentation/web-api/reference/get-category/)
     */
    @GET("browse/categories/{category_id}")
    fun getCategory(
        @Path("category_id") categoryId: String?,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Call<Category>
}
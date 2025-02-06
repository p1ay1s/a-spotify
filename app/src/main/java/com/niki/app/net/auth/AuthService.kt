package com.niki.app.net.auth

import com.niki.spotify.remote.CLIENT_ID
import com.niki.spotify.remote.REDIRECT_URI
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val TYPE_AUTHORIZATION_CODE = "authorization_code"
private const val TYPE_CODE = "code"
private const val TYPE_REFRESH_TOKEN = "refresh_token"
private const val TYPE_CLIENT = "client_credentials"

/**
 * 使用 baseurl: "https://accounts.spotify.com/"
 */
interface AuthService {
//    @GET("authorize")
//    fun getCode(
//        @Query("response_type") responseType: String = TYPE_CODE,
//        @Query("client_id") clientId: String = CLIENT_ID,
//        @Query("redirect_uri") redirectUri: String = REDIRECT_URI
//    ): Call<TokenResponse>

    @POST("api/token")
    @FormUrlEncoded
    fun getTokenWithApp(
        @Body map: Map<String, String> = mapOf("grant_type" to TYPE_CLIENT)
    ): Call<TokenResponse>

    @POST("api/token")
    @FormUrlEncoded
    fun getTokenWithCode(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = TYPE_AUTHORIZATION_CODE,
        @Field("redirect_uri") redirectUri: String = REDIRECT_URI
    ): Call<TokenResponse>

    @POST("api/token")
    @FormUrlEncoded
    fun refreshToken(
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = TYPE_REFRESH_TOKEN
    ): Call<TokenResponse>
}
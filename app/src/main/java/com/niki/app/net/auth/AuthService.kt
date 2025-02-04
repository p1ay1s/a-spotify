package com.niki.app.net.auth

import com.niki.spotify.remote.REDIRECT_URI
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

private const val TYPE_CODE = "authorization_code"
private const val TYPE_REFRESH_TOKEN = "refresh_token"

/**
 * 使用 baseurl: "https://accounts.spotify.com/"
 */
interface AuthService {
    @POST("api/token")
    @FormUrlEncoded
    fun getTokenWithCode(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = TYPE_CODE,
        @Field("redirect_uri") redirectUri: String = com.niki.spotify.remote.REDIRECT_URI
    ): Call<TokenResponse>

    @POST("api/token")
    @FormUrlEncoded
    fun refreshToken(
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = TYPE_REFRESH_TOKEN
    ): Call<TokenResponse>
}
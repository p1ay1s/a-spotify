package com.niki.app.net

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 使用 baseurl: "https://accounts.spotify.com/"
 */
interface AuthService {
    @POST("api/token")
    @FormUrlEncoded
    fun getAccessToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Header("Authorization") authorization: String
    ): Call<TokenResponse>

    @POST("api/token")
    @FormUrlEncoded
    fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Header("Authorization") authorization: String
    ): Call<TokenResponse>
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String?
)

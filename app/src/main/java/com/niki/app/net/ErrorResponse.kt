package com.niki.app.net

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error") val error: ErrorData?
)

data class ErrorData(
    @SerializedName("status") val code: Int?,
    @SerializedName("message") val msg: String?
)
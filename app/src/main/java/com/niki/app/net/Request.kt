package com.niki.app.net
//
//import com.zephyr.base.log.logD
//import com.zephyr.base.log.logE
//import com.zephyr.util.net.Error
//import com.zephyr.util.net.NetResult
//import com.zephyr.util.net.Success
//import com.zephyr.util.toLogString
//import com.zephyr.util.toPrettyJson
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.awaitResponse
//
//
////object ServiceBuilder {
////    private var CONNECT_TIMEOUT_SET = 15L
////    private const val READ_TIMEOUT_SET = 10L
////
////    val client: OkHttpClient by lazy {
////        OkHttpClient.Builder()
////            .readTimeout(READ_TIMEOUT_SET, TimeUnit.SECONDS)
////            .connectTimeout(CONNECT_TIMEOUT_SET, TimeUnit.SECONDS)
////            .build()
////    }
////
////    /**
////     * 创建并返回一个 retrofit 实例
////     */
////    fun retrofitBuilder(baseUrl: String = appBaseUrl): Retrofit = Retrofit.Builder()
////        .baseUrl(baseUrl)
////        .client(client)
////        .addConverterFactory(GsonConverterFactory.create())
////        .build()
////
////    /**
////     * 返回一个 Service 代理对象
////     *
////     * example:
////     * ServiceBuilder.create(LoginService::class.java)
////     */
////    @JvmName("create1")
////    fun <T> create(serviceClass: Class<T>, baseUrl: String = appBaseUrl): T =
////        retrofitBuilder(baseUrl).create(serviceClass)
////
////    /**
////     * ServiceBuilder.create<LoginService>()
////     */
////    @JvmName("create2")
////    inline fun <reified T> create(baseUrl: String = appBaseUrl): T = create(T::class.java, baseUrl)
////}
//
//private const val TAG = "net request"
//
//fun <T> Call<T>.getUrl(): String = request().url().toString()
//
//inline fun <T> NetResult<T>.handleResult(
//    crossinline onSuccess: (T?) -> Unit,
//    crossinline onError: (Int?, String) -> Unit
//) = when (this) {
//    is Success -> onSuccess(data)
//    is Error -> onError(code, msg)
//}
//
///**
// * 同步请求方法
// */
//@JvmName("requestExecute1")
//fun <T> Call<T>.requestExecute(
//    callback: (NetResult<T>) -> Unit
//) = try {
//    val response = execute()
//    handleOnResponse(response, callback)
//} catch (t: Throwable) {
//    handleOnFailure(t, callback)
//}
//
///**
// * 异步请求方法
// */
//@JvmName("requestEnqueue1")
//fun <T> Call<T>.requestEnqueue(
//    callback: (NetResult<T>) -> Unit
//) = enqueue(object : Callback<T> {
//    override fun onResponse(call: Call<T>, response: Response<T>) {
//        handleOnResponse(response, callback)
//    }
//
//    override fun onFailure(call: Call<T>, throwable: Throwable) {
//        handleOnFailure(throwable, callback)
//    }
//})
//
///**
// * 挂起请求方法
// */
//@JvmName("requestSuspend1")
//suspend fun <T> Call<T>.requestSuspend(
//    callback: (NetResult<T>) -> Unit
//) = withContext(Dispatchers.IO) {
//    try {
//        val response = awaitResponse()
//        handleOnResponse(response, callback)
//    } catch (t: Throwable) {
//        handleOnFailure(t, callback)
//    }
//}
//
//@JvmName("requestExecute2")
//fun <T> requestExecute(
//    call: Call<T>,
//    callback: (NetResult<T>) -> Unit
//) = call.requestExecute(callback)
//
//@JvmName("requestEnqueue2")
//fun <T> requestEnqueue(
//    call: Call<T>,
//    callback: (NetResult<T>) -> Unit
//) = call.requestEnqueue(callback)
//
//@JvmName("requestSuspend2")
//suspend fun <T> requestSuspend(
//    call: Call<T>,
//    callback: (NetResult<T>) -> Unit
//) = call.requestSuspend(callback)
//
//
//fun <T> Call<T>.handleOnResponse(
//    response: Response<T>?,
//    callback: (NetResult<T>) -> Unit
//) = response?.run {
//    val url = getUrl()
//    when {
//        isSuccessful -> {
//            logD(TAG, "[${code()}]request succeed:\n$url")
//            logD(TAG, "body:\n${body().toPrettyJson()}")
//            callback(Success(body()))
//        } // 成功
//
//        else -> {
//            val errorBodyString = errorBody().toPrettyJson()
//            logE(TAG, "[${code()}]request failed at:\n $url")
//            logE(TAG, "error body:\n${errorBodyString}")
//            val errorString = errorBodyString.ifBlank { message() ?: "Unknown error" }
//            callback(Error(code(), errorString))
//        } // 其他失败情况
//    }
//} ?: callback(Error(null, "response is null"))
//
//fun <T> Call<T>.handleOnFailure(
//    throwable: Throwable?,
//    callback: (NetResult<T>) -> Unit
//) {
//    if (throwable == null) return
//    val url = getUrl()
//    val throwableString = throwable.toLogString()
//    logE(TAG, "failed at:\n$url")
//    logE(TAG, "\nthrowable:\n$throwableString")
//    callback(Error(null, throwableString))
//}
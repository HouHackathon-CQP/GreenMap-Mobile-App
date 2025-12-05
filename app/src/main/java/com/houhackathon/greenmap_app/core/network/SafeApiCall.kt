package com.houhackathon.greenmap_app.core.network

import com.houhackathon.greenmap_app.extension.flow.Result
import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.Success(body)
            } else {
                Result.error(IllegalStateException("Response body is null"))
            }
        } else {
            Result.error(HttpException(response))
        }
    } catch (e: Exception) {
        Result.error(e)
    }
}

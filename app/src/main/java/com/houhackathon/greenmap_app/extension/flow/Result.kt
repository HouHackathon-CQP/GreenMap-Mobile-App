package com.houhackathon.greenmap_app.extension.flow


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    val isLoading: Boolean
        get() = this is Loading

    /**
     * Returns the data if this is a Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the data if this is a Success, throws the exception if Error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    /**
     * Returns the exception if this is an Error, null otherwise
     */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }

    /**
     * Transform the data if this is a Success
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Transform the data if this is a Success, otherwise return the result of onFailure
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Throwable) -> R,
        onLoading: () -> R,
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception)
        is Loading -> onLoading()
    }

    /**
     * Perform action if this is a Success
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Perform action if this is an Error
     */
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Perform action if this is Loading
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }

    companion object {
        /**
         * Create a Success result
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Create an Error result
         */
        fun <T> error(exception: Throwable): Result<T> = Error(exception)

        /**
         * Create an Error result with message
         */
        fun <T> error(message: String): Result<T> = Error(Exception(message))

        /**
         * Create a Loading result
         */
        fun <T> loading(): Result<T> = Loading

        /**
         * Wrap a suspend function call in a Result
         */
        suspend inline fun <T> safeCall(call: suspend () -> T): Result<T> {
            return try {
                Success(call())
            } catch (e: Exception) {
                Error(e)
            }
        }

        /**
         * Wrap a function call in a Result
         */
        inline fun <T> safeSyncCall(call: () -> T): Result<T> {
            return try {
                Success(call())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}
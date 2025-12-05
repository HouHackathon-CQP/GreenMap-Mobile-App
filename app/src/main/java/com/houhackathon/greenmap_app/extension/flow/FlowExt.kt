/*
 * Copyright 2025 HouHackathon-CQP
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.houhackathon.greenmap_app.extension.flow

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


inline fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit,
): Job =
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(state = minActiveState) {
            collect { action(it) }
        }
    }

/**
 * Wrap Flow emissions in Result.Success and catch exceptions as Result.Error
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it)) }
}

/**
 * Start with loading state and wrap in Result
 */
fun <T> Flow<T>.asResultWithLoading(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
}

/**
 * Filter out null values from Flow
 */
fun <T> Flow<T?>.filterNotNull(): Flow<T> {
    return this.map { it!! }.catch { /* Ignore null pointer exceptions */ }
}

/**
 * Transform Flow to emit only distinct values
 */
fun <T> Flow<List<T>>.distinctUntilChanged(): Flow<List<T>> {
    var lastEmitted: List<T>? = null
    return this.map { currentList ->
        if (currentList != lastEmitted) {
            lastEmitted = currentList
            currentList
        } else {
            lastEmitted!!
        }
    }
}
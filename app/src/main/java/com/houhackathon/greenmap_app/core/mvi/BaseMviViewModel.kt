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

package com.houhackathon.greenmap_app.core.mvi

import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn


abstract class BaseMviViewModel<I : MviIntent, S : MviViewState, E : MviSingleEvent> : ViewModel() {
    abstract val viewState: StateFlow<S>

    open fun restoreState(state: S) = Unit

    private val intentMutableFlow = MutableSharedFlow<I>(extraBufferCapacity = Int.MAX_VALUE)

    protected val intentSharedFlow: SharedFlow<I> get() = intentMutableFlow

    // Send and Receive E
    private val eventChannel = Channel<E>(Channel.UNLIMITED)

    // transform eventChannel to Flow for provides a one-time event stream
    val singleEvent: Flow<E> = eventChannel.receiveAsFlow()

    protected fun sendEvent(event: E) {
        eventChannel
            .trySend(event)
            .onFailure {
            }.getOrNull()
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
    }

    open fun resetState() = Unit

    @MainThread
    open fun processIntent(intent: I) {
        check(intentMutableFlow.tryEmit(intent)) { "Failed to emit intent: $intent" }
    }

    /**
     * Share the flow in [viewModelScope],
     * start when the first subscriber arrives,
     * and stop when the last subscriber leaves.
     */
    protected fun <T> Flow<T>.shareWhileSubscribed(): SharedFlow<T> =
        shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    protected fun <T> Flow<T>.stateWithInitialNullWhileSubscribed(): StateFlow<T?> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


}
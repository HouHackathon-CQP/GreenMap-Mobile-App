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

package com.houhackathon.greenmap_app.core.activity

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState
import com.houhackathon.greenmap_app.extension.flow.collectIn


abstract class BaseMviActivity<I : MviIntent, S : MviViewState, E : MviSingleEvent, VM : BaseMviViewModel<I, S, E>> : BaseActivity() {
    private companion object {
        private const val ARG_RESTORE_STATE = "ARG_RESTORE_STATE"
    }

    protected abstract val viewModel: VM

    @CallSuper
    override fun updateUI(savedInstanceState: Bundle?) {
        bindViewModel()
    }

    private var cacheStateRestore: S? = null

    private fun bindViewModel() {
        viewModel.viewState.collectIn(this) {
            cacheStateRestore = it
        }
    }

    final override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoreState = savedInstanceState.getParcelable(ARG_RESTORE_STATE) as S?
        if (restoreState != null) {
            viewModel.restoreState(restoreState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val cacheStateRestore = cacheStateRestore
        if (cacheStateRestore is Parcelable) {
            outState.putParcelable(ARG_RESTORE_STATE, cacheStateRestore)
        }
    }

    fun resetState() {
        viewModel.resetState()
    }
}
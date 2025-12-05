package com.houhackathon.greenmap_app.core.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.core.mvi.MviIntent
import com.houhackathon.greenmap_app.core.mvi.MviSingleEvent
import com.houhackathon.greenmap_app.core.mvi.MviViewState


abstract class ComposeMviActivity<I : MviIntent, S : MviViewState, E : MviSingleEvent, VM : BaseMviViewModel<I, S, E>> :
    BaseMviActivity<I, S, E, VM>() {

    open fun isSafeAreaView(): Boolean = false

    final override fun createContentView(savedInstanceState: Bundle?): View {
        return ComposeView(this).also {
            it.setContent {
                val uiState by viewModel.viewState.collectAsStateWithLifecycle()
                MaterialTheme {
                    SetContentView(uiState)
                }
            }
        }.also { viewRoot ->
            enableEdgeToEdge()
            if (isSafeAreaView()) {
                ViewCompat.setOnApplyWindowInsetsListener(viewRoot) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        0,
                    )
                    insets
                }
            }
        }
    }

    @Composable
    abstract fun SetContentView(viewState: S)
}
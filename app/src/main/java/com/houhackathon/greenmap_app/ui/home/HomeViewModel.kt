package com.houhackathon.greenmap_app.ui.home

import androidx.lifecycle.viewModelScope
import com.houhackathon.greenmap_app.core.mvi.BaseMviViewModel
import com.houhackathon.greenmap_app.domain.usecase.CheckDatabaseStatusUseCase
import com.houhackathon.greenmap_app.domain.usecase.GetHealthStatusUseCase
import com.houhackathon.greenmap_app.extension.flow.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHealthStatusUseCase: GetHealthStatusUseCase,
    private val checkDatabaseStatusUseCase: CheckDatabaseStatusUseCase,
) : BaseMviViewModel<HomeIntent, HomeViewState, HomeEvent>() {

    private val _viewState = MutableStateFlow(HomeViewState())
    override val viewState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            intentSharedFlow.collect(::handleIntent)
        }
        processIntent(HomeIntent.CheckServer)
        processIntent(HomeIntent.CheckDatabase)
    }

    private fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.CheckServer -> checkServer()
            HomeIntent.CheckDatabase -> checkDatabase()
            HomeIntent.NavigateToMap -> sendEvent(HomeEvent.NavigateMap)
        }
    }

    private fun checkServer() {
        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    isServerLoading = true,
                    isServerError = false,
                    serverStatus = null
                )
            }
            when (val result = getHealthStatusUseCase()) {
                is Result.Success -> {
                    val message = result.data.message ?: result.data.status ?: "Server OK"
                    _viewState.update {
                        it.copy(
                            isServerLoading = false,
                            isServerError = false,
                            serverStatus = message
                        )
                    }
                }

                is Result.Error -> {
                    _viewState.update {
                        it.copy(
                            isServerLoading = false,
                            isServerError = true,
                            serverStatus = result.exception.message ?: "Server error"
                        )
                    }
                }

                Result.Loading -> _viewState.update { it.copy(isServerLoading = true) }
            }
        }
    }

    private fun checkDatabase() {
        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    isDatabaseLoading = true,
                    isDatabaseError = false,
                    databaseStatus = null
                )
            }
            when (val result = checkDatabaseStatusUseCase()) {
                is Result.Success -> {
                    val message = result.data.message ?: result.data.status ?: "Database OK"
                    _viewState.update {
                        it.copy(
                            isDatabaseLoading = false,
                            isDatabaseError = false,
                            databaseStatus = message
                        )
                    }
                }

                is Result.Error -> {
                    _viewState.update {
                        it.copy(
                            isDatabaseLoading = false,
                            isDatabaseError = true,
                            databaseStatus = result.exception.message ?: "Database error"
                        )
                    }
                }

                Result.Loading -> _viewState.update { it.copy(isDatabaseLoading = true) }
            }
        }
    }
}

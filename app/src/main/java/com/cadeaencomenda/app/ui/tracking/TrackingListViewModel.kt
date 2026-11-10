package com.cadeaencomenda.app.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.usecase.AddPackageUseCase
import com.cadeaencomenda.app.domain.usecase.ArchivePackageUseCase
import com.cadeaencomenda.app.domain.usecase.DeletePackageUseCase
import com.cadeaencomenda.app.domain.usecase.GetPackagesUseCase
import com.cadeaencomenda.app.domain.usecase.RefreshTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackingListUiState {
    data object Loading : TrackingListUiState
    data class Success(val packages: List<TrackingPackage>) : TrackingListUiState
    data object Empty : TrackingListUiState
    data class Error(val message: String) : TrackingListUiState
}

@HiltViewModel
class TrackingListViewModel @Inject constructor(
    private val getPackagesUseCase: GetPackagesUseCase,
    private val addPackageUseCase: AddPackageUseCase,
    private val refreshTrackingUseCase: RefreshTrackingUseCase,
    private val archivePackageUseCase: ArchivePackageUseCase,
    private val deletePackageUseCase: DeletePackageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackingListUiState>(TrackingListUiState.Loading)
    val uiState: StateFlow<TrackingListUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    private val _packageAddedEvent = MutableStateFlow(false)
    val packageAddedEvent: StateFlow<Boolean> = _packageAddedEvent.asStateFlow()

    init {
        observePackages()
    }

    private fun observePackages() {
        viewModelScope.launch {
            getPackagesUseCase()
                .onStart { _uiState.value = TrackingListUiState.Loading }
                .catch { e ->
                    _uiState.value = TrackingListUiState.Error(
                        e.localizedMessage ?: "Erro ao carregar encomendas"
                    )
                }
                .collect { packages ->
                    _uiState.value = if (packages.isEmpty()) {
                        TrackingListUiState.Empty
                    } else {
                        TrackingListUiState.Success(packages)
                    }
                }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                refreshTrackingUseCase()
            } catch (e: Exception) {
                _uiState.value = TrackingListUiState.Error(
                    e.localizedMessage ?: "Erro ao atualizar"
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addPackage(code: String, nickname: String) {
        viewModelScope.launch {
            try {
                addPackageUseCase(code = code.trim().uppercase(), nickname = nickname.trim())
                _showAddSheet.value = false
                _packageAddedEvent.value = true
            } catch (e: Exception) {
                // Keep sheet open so user can retry
            }
        }
    }

    fun consumePackageAddedEvent() {
        _packageAddedEvent.value = false
    }

    fun archivePackage(code: String) {
        viewModelScope.launch {
            try {
                archivePackageUseCase(code)
            } catch (_: Exception) { }
        }
    }

    fun deletePackage(code: String) {
        viewModelScope.launch {
            try {
                deletePackageUseCase(code)
            } catch (_: Exception) { }
        }
    }

    fun toggleAddSheet(show: Boolean) {
        _showAddSheet.value = show
    }
}

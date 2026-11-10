package com.cadeaencomenda.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.usecase.ArchivePackageUseCase
import com.cadeaencomenda.app.domain.usecase.DeletePackageUseCase
import com.cadeaencomenda.app.domain.usecase.GetPackageDetailUseCase
import com.cadeaencomenda.app.domain.usecase.RefreshTrackingUseCase
import com.cadeaencomenda.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PackageDetailUiState {
    data object Loading : PackageDetailUiState
    data class Success(val pkg: TrackingPackage) : PackageDetailUiState
    data class Error(val msg: String) : PackageDetailUiState
}

@HiltViewModel
class PackageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPackageDetailUseCase: GetPackageDetailUseCase,
    private val refreshTrackingUseCase: RefreshTrackingUseCase,
    private val archivePackageUseCase: ArchivePackageUseCase,
    private val deletePackageUseCase: DeletePackageUseCase
) : ViewModel() {

    val trackingCode: String = savedStateHandle.get<String>(Screen.ARG_TRACKING_CODE).orEmpty()

    private val _uiState = MutableStateFlow<PackageDetailUiState>(PackageDetailUiState.Loading)
    val uiState: StateFlow<PackageDetailUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _packageDeletedEvent = MutableStateFlow(false)
    val packageDeletedEvent: StateFlow<Boolean> = _packageDeletedEvent.asStateFlow()

    init {
        loadPackageDetail()
    }

    private fun loadPackageDetail() {
        if (trackingCode.isBlank()) {
            _uiState.value = PackageDetailUiState.Error("Código de rastreio não informado")
            return
        }

        viewModelScope.launch {
            _uiState.value = PackageDetailUiState.Loading
            getPackageDetailUseCase(trackingCode)
                .catch { e ->
                    _uiState.value = PackageDetailUiState.Error(
                        e.localizedMessage ?: "Erro ao carregar detalhes da encomenda"
                    )
                }
                .collect { pkg ->
                    _uiState.value = if (pkg != null) {
                        PackageDetailUiState.Success(pkg)
                    } else {
                        PackageDetailUiState.Error("Encomenda não encontrada")
                    }
                }
        }
    }

    fun refresh() {
        if (trackingCode.isBlank()) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = refreshTrackingUseCase(trackingCode)
                if (result.isFailure) {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Falha ao atualizar status"
                    _userMessage.emit(errorMsg)
                }
            } catch (e: Exception) {
                _userMessage.emit(e.localizedMessage ?: "Erro na comunicação com a transportadora")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleArchive() {
        val currentState = _uiState.value
        if (currentState is PackageDetailUiState.Success) {
            val pkg = currentState.pkg
            viewModelScope.launch {
                try {
                    if (pkg.isArchived) {
                        archivePackageUseCase.unarchive(pkg.trackingCode)
                        _userMessage.emit("Encomenda desarquivada")
                    } else {
                        archivePackageUseCase.archive(pkg.trackingCode)
                        _userMessage.emit("Encomenda arquivada")
                    }
                } catch (e: Exception) {
                    _userMessage.emit(e.localizedMessage ?: "Erro ao alterar estado de arquivamento")
                }
            }
        }
    }

    fun delete() {
        if (trackingCode.isBlank()) return

        viewModelScope.launch {
            try {
                deletePackageUseCase(trackingCode)
                _packageDeletedEvent.value = true
            } catch (e: Exception) {
                _userMessage.emit(e.localizedMessage ?: "Erro ao excluir encomenda")
            }
        }
    }
}

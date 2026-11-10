package com.cadeaencomenda.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadeaencomenda.app.data.preferences.PreferencesManager
import com.cadeaencomenda.app.data.preferences.ThemeMode
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val updateIntervalMinutes: Int = 30,
    val activePackageCount: Int = 0,
    val archivedPackageCount: Int = 0,
    val totalPackageCount: Int = 0,
    val isClearingArchived: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val trackingRepository: TrackingRepository
) : ViewModel() {

    private val _isClearingArchived = MutableStateFlow(false)
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferencesManager.themeMode,
            preferencesManager.notificationsEnabled,
            preferencesManager.updateInterval
        ) { theme, notifications, interval ->
            Triple(theme, notifications, interval)
        },
        trackingRepository.getActivePackages(),
        trackingRepository.getArchivedPackages(),
        _isClearingArchived
    ) { (theme, notifications, interval), activeList, archivedList, isClearing ->
        SettingsUiState(
            themeMode = theme,
            notificationsEnabled = notifications,
            updateIntervalMinutes = interval,
            activePackageCount = activeList.size,
            archivedPackageCount = archivedList.size,
            totalPackageCount = activeList.size + archivedList.size,
            isClearingArchived = isClearing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun setUpdateInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            preferencesManager.setUpdateInterval(intervalMinutes)
        }
    }

    fun clearArchivedPackages() {
        viewModelScope.launch {
            _isClearingArchived.value = true
            try {
                val archived = trackingRepository.getArchivedPackages().first()
                for (pkg in archived) {
                    trackingRepository.deletePackage(pkg.trackingCode)
                }
                _userMessage.emit("${archived.size} encomenda(s) arquivada(s) removida(s)")
            } catch (e: Exception) {
                _userMessage.emit(e.localizedMessage ?: "Erro ao limpar encomendas arquivadas")
            } finally {
                _isClearingArchived.value = false
            }
        }
    }
}

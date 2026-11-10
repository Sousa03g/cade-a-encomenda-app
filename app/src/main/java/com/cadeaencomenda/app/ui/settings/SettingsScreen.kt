@file:OptIn(ExperimentalMaterial3Api::class)

package com.cadeaencomenda.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadeaencomenda.app.BuildConfig
import com.cadeaencomenda.app.R
import com.cadeaencomenda.app.data.preferences.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showClearArchivedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── APARÊNCIA ──────────────────────────────────────────────────────────
            SettingsSectionHeader(title = "Aparência")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                val themeLabel = when (uiState.themeMode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                }

                SettingsClickableItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.theme),
                    subtitle = themeLabel,
                    onClick = { showThemeDialog = true }
                )
            }

            // ── NOTIFICAÇÕES ───────────────────────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.notifications))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                SettingsSwitchItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Avisos de movimentação",
                    subtitle = "Notificar quando o status da encomenda mudar",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                val intervalLabel = when (uiState.updateIntervalMinutes) {
                    30 -> "A cada 30 minutos"
                    60 -> "A cada 1 hora"
                    120 -> "A cada 2 horas"
                    else -> "A cada ${uiState.updateIntervalMinutes} minutos"
                }

                SettingsClickableItem(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.update_interval),
                    subtitle = intervalLabel,
                    enabled = uiState.notificationsEnabled,
                    onClick = { showIntervalDialog = true }
                )
            }

            // ── ENCOMENDAS ─────────────────────────────────────────────────────────
            SettingsSectionHeader(title = "Encomendas")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                SettingsInfoItem(
                    icon = Icons.Outlined.Inventory2,
                    title = "Total rastreado",
                    subtitle = "${uiState.activePackageCount} ativa(s), ${uiState.archivedPackageCount} arquivada(s)"
                )

                if (uiState.archivedPackageCount > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsClickableItem(
                        icon = Icons.Outlined.CleaningServices,
                        title = "Limpar arquivadas",
                        subtitle = "Excluir permanentemente ${uiState.archivedPackageCount} encomenda(s)",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearArchivedDialog = true }
                    )
                }
            }

            // ── SOBRE ──────────────────────────────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.about))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                SettingsInfoItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.version),
                    subtitle = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsClickableItem(
                    icon = Icons.Outlined.Policy,
                    title = stringResource(R.string.privacy_policy),
                    subtitle = "Leia nossos termos e política de dados",
                    onClick = {
                        openUrl(context, "https://sites.google.com/view/cadeaencomenda-privacidade")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsClickableItem(
                    icon = Icons.Outlined.StarRate,
                    title = "Avaliar na Google Play",
                    subtitle = "Deixe sua opinião e ajude o app a evoluir",
                    onClick = {
                        val packageName = context.packageName
                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$packageName")
                                )
                            )
                        } catch (_: Exception) {
                            openUrl(context, "https://play.google.com/store/apps/details?id=$packageName")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── THEME SELECTION DIALOG ─────────────────────────────────────────────────
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(text = "Escolher Tema", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeOption(
                        label = stringResource(R.string.theme_system),
                        selected = uiState.themeMode == ThemeMode.SYSTEM,
                        onSelect = {
                            viewModel.setThemeMode(ThemeMode.SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        label = stringResource(R.string.theme_light),
                        selected = uiState.themeMode == ThemeMode.LIGHT,
                        onSelect = {
                            viewModel.setThemeMode(ThemeMode.LIGHT)
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        label = stringResource(R.string.theme_dark),
                        selected = uiState.themeMode == ThemeMode.DARK,
                        onSelect = {
                            viewModel.setThemeMode(ThemeMode.DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // ── INTERVAL SELECTION DIALOG ──────────────────────────────────────────────
    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text(text = "Intervalo de Atualização", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    IntervalOption(
                        label = "30 minutos (recomendado)",
                        selected = uiState.updateIntervalMinutes == 30,
                        onSelect = {
                            viewModel.setUpdateInterval(30)
                            showIntervalDialog = false
                        }
                    )
                    IntervalOption(
                        label = "1 hora",
                        selected = uiState.updateIntervalMinutes == 60,
                        onSelect = {
                            viewModel.setUpdateInterval(60)
                            showIntervalDialog = false
                        }
                    )
                    IntervalOption(
                        label = "2 horas (economia de bateria)",
                        selected = uiState.updateIntervalMinutes == 120,
                        onSelect = {
                            viewModel.setUpdateInterval(120)
                            showIntervalDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // ── CLEAR ARCHIVED CONFIRMATION DIALOG ─────────────────────────────────────
    if (showClearArchivedDialog) {
        AlertDialog(
            onDismissRequest = { showClearArchivedDialog = false },
            title = { Text(text = "Limpar encomendas arquivadas", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Deseja remover permanentemente todas as ${uiState.archivedPackageCount} encomendas arquivadas? Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearArchivedDialog = false
                        viewModel.clearArchivedPackages()
                    }
                ) {
                    Text(
                        text = "Excluir todas",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearArchivedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) titleColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun IntervalOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Não foi possível abrir o link", Toast.LENGTH_SHORT).show()
    }
}

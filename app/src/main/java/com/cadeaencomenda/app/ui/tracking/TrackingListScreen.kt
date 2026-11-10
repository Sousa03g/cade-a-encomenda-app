@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.cadeaencomenda.app.ui.tracking

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadeaencomenda.app.ui.components.AdBannerView
import com.cadeaencomenda.app.ui.components.AddPackageSheet
import com.cadeaencomenda.app.ui.components.EmptyState
import com.cadeaencomenda.app.ui.components.ErrorState
import com.cadeaencomenda.app.ui.components.InterstitialAdManager
import com.cadeaencomenda.app.ui.components.LoadingState
import com.cadeaencomenda.app.ui.components.PackageCard
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme

@Composable
fun TrackingListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: TrackingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showAddSheet by viewModel.showAddSheet.collectAsState()
    val packageAdded by viewModel.packageAddedEvent.collectAsState()

    val context = LocalContext.current

    // Show interstitial ad after adding a package
    LaunchedEffect(packageAdded) {
        if (packageAdded) {
            InterstitialAdManager.show(context)
            viewModel.consumePackageAddedEvent()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Cadê a Encomenda",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar encomenda"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshAll() },
                modifier = Modifier.weight(1f)
            ) {
                when (val state = uiState) {
                    is TrackingListUiState.Loading -> {
                        LoadingState(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is TrackingListUiState.Empty -> {
                        EmptyState(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is TrackingListUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.refreshAll() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is TrackingListUiState.Success -> {
                        PackageList(
                            packages = state.packages,
                            onPackageClick = onNavigateToDetail,
                            onArchive = { viewModel.archivePackage(it) },
                            onDelete = { viewModel.deletePackage(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Fixed ad banner at the bottom
            AdBannerView(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Add package bottom sheet
    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleAddSheet(false) },
            sheetState = sheetState
        ) {
            AddPackageSheet(
                onAdd = { code, nickname -> viewModel.addPackage(code, nickname) },
                onDismiss = { viewModel.toggleAddSheet(false) }
            )
        }
    }
}

@Composable
private fun PackageList(
    packages: List<com.cadeaencomenda.app.domain.model.TrackingPackage>,
    onPackageClick: (String) -> Unit,
    onArchive: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = packages,
            key = { _, pkg -> pkg.trackingCode }
        ) { index, pkg ->
            // Insert native ad placeholder every 5 items
            if (index > 0 && index % 5 == 0) {
                NativeAdPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }

            SwipeablePackageCard(
                trackingPackage = pkg,
                onClick = { onPackageClick(pkg.trackingCode) },
                onArchive = { onArchive(pkg.trackingCode) },
                onDelete = { onDelete(pkg.trackingCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }
    }
}

@Composable
private fun SwipeablePackageCard(
    trackingPackage: com.cadeaencomenda.app.domain.model.TrackingPackage,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onArchive()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection

            val backgroundColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "swipe_bg"
            )

            val iconTint by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onSecondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "swipe_icon"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = backgroundColor,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 20.dp)
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = "Arquivar",
                        tint = iconTint,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        tint = iconTint,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        },
        content = {
            PackageCard(
                trackingPackage = trackingPackage,
                onClick = onClick
            )
        }
    )
}

@Composable
private fun NativeAdPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Text(
            text = "Anúncio",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TrackingListScreenPreview() {
    CadeAEncomendaTheme {
        // Preview with empty state since ViewModel requires injection
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text("Cadê a Encomenda", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                Box(Modifier.weight(1f)) {
                    EmptyState(modifier = Modifier.fillMaxSize())
                }
                AdBannerView(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NativeAdPlaceholderPreview() {
    CadeAEncomendaTheme {
        NativeAdPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

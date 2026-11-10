package com.cadeaencomenda.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cadeaencomenda.app.R
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.model.TrackingStatus
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Material 3 ElevatedCard displaying summary information for a tracked package.
 * Includes status icon, nickname, tracking code, carrier tag, latest event info, relative time,
 * and StatusBadge. Optionally supports direct action callbacks (Archive, Delete).
 */
@Composable
fun PackageCard(
    trackingPackage: TrackingPackage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onArchive: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val statusColor = resolveStatusColor(trackingPackage.status)
    val statusIcon = resolveStatusIcon(trackingPackage.status)
    val relativeTime = remember(trackingPackage.lastEventDate, trackingPackage.updatedAt) {
        formatRelativeTime(trackingPackage.lastEventDate ?: trackingPackage.updatedAt)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Status Icon + (Title & Tracking Code) + StatusBadge / Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon in soft colored circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title (Nickname or Code) + subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trackingPackage.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!trackingPackage.nickname.isNullOrBlank()) {
                        Text(
                            text = trackingPackage.trackingCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                StatusBadge(status = trackingPackage.status)

                // Optional Action Dropdown Menu
                if (onArchive != null || onDelete != null) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Mais opções",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            onArchive?.let { archiveAction ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (trackingPackage.isArchived) "Desarquivar"
                                            else stringResource(R.string.action_archive)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Archive, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        archiveAction()
                                    }
                                )
                            }
                            onDelete?.let { deleteAction ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.action_delete),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        deleteAction()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Last Event Description
            val eventText = trackingPackage.lastEvent
                ?: stringResource(R.string.status_pending)

            Text(
                text = eventText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Carrier + Location + Relative Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Carrier pill / name
                    val carrier = trackingPackage.carrierName
                        ?: trackingPackage.carrierCode
                        ?: stringResource(R.string.carrier)

                    Text(
                        text = carrier,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    // Optional location indicator
                    if (!trackingPackage.lastEventLocation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = trackingPackage.lastEventLocation,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Relative Time
                if (relativeTime.isNotBlank()) {
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Helper composable wrapping [PackageCard] with Material 3 [SwipeToDismissBox] for
 * quick gesture-driven Archive (start to end) and Delete (end to start).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeablePackageCard(
    trackingPackage: TrackingPackage,
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
                label = "swipe_card_bg"
            )

            val iconTint by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onSecondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "swipe_card_icon"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp)
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = stringResource(R.string.action_archive),
                        tint = iconTint,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = iconTint,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        },
        content = {
            PackageCard(
                trackingPackage = trackingPackage,
                onClick = onClick,
                onArchive = onArchive,
                onDelete = onDelete
            )
        }
    )
}

/**
 * Returns an appropriate icon based on [TrackingStatus].
 */
fun resolveStatusIcon(status: TrackingStatus): ImageVector {
    return when (status) {
        TrackingStatus.POSTED -> Icons.Outlined.Inventory2
        TrackingStatus.IN_TRANSIT -> Icons.Outlined.LocalShipping
        TrackingStatus.OUT_FOR_DELIVERY -> Icons.Outlined.DirectionsBike
        TrackingStatus.DELIVERED -> Icons.Outlined.CheckCircle
        TrackingStatus.RETURNED -> Icons.Outlined.AssignmentReturn
        TrackingStatus.EXCEPTION -> Icons.Outlined.ErrorOutline
        TrackingStatus.UNKNOWN -> Icons.Outlined.HelpOutline
    }
}

/**
 * Formats a [LocalDateTime] into a user-friendly relative Portuguese string.
 */
fun formatRelativeTime(dateTime: LocalDateTime?): String {
    if (dateTime == null) return ""
    val now = LocalDateTime.now()
    val duration = Duration.between(dateTime, now)
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        minutes < 1 -> "agora mesmo"
        minutes < 60 -> "há ${minutes}m"
        hours < 24 && dateTime.toLocalDate() == now.toLocalDate() -> "há ${hours}h"
        dateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> {
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
            "ontem às ${dateTime.format(formatter)}"
        }
        days in 2..6 -> "há $days dias"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            dateTime.format(formatter)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PackageCardPreview() {
    CadeAEncomendaTheme {
        PackageCard(
            trackingPackage = TrackingPackage(
                trackingCode = "NL123456789BR",
                nickname = "Controle PS5",
                carrierName = "Correios",
                carrierCode = "correios",
                status = TrackingStatus.OUT_FOR_DELIVERY,
                lastEvent = "Objeto saiu para entrega ao destinatário",
                lastEventDate = LocalDateTime.now().minusHours(2),
                lastEventLocation = "São Paulo - SP",
                isArchived = false,
                createdAt = LocalDateTime.now().minusDays(3),
                updatedAt = LocalDateTime.now().minusHours(2)
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

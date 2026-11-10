package com.cadeaencomenda.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cadeaencomenda.app.domain.model.TrackingStatus
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme
import com.cadeaencomenda.app.ui.theme.StatusDelivered
import com.cadeaencomenda.app.ui.theme.StatusInTransit
import com.cadeaencomenda.app.ui.theme.StatusOutForDelivery
import com.cadeaencomenda.app.ui.theme.StatusPending
import com.cadeaencomenda.app.ui.theme.StatusPosted
import com.cadeaencomenda.app.ui.theme.StatusProblem

/**
 * Reusable badge displaying a chip with a colored background corresponding to [TrackingStatus].
 * Displays the localized status name with white text and rounded corners.
 */
@Composable
fun StatusBadge(
    status: TrackingStatus,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val backgroundColor = remember(status) {
        resolveStatusColor(status)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = stringResource(id = status.displayNameRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Resolves a Compose [Color] from the [TrackingStatus.colorHex] string or falls back to theme values.
 */
fun resolveStatusColor(status: TrackingStatus): Color {
    return try {
        Color(android.graphics.Color.parseColor(status.colorHex))
    } catch (_: Exception) {
        when (status) {
            TrackingStatus.POSTED -> StatusPosted
            TrackingStatus.IN_TRANSIT -> StatusInTransit
            TrackingStatus.OUT_FOR_DELIVERY -> StatusOutForDelivery
            TrackingStatus.DELIVERED -> StatusDelivered
            TrackingStatus.RETURNED -> StatusProblem
            TrackingStatus.EXCEPTION -> StatusProblem
            TrackingStatus.UNKNOWN -> StatusPending
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    CadeAEncomendaTheme {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            StatusBadge(status = TrackingStatus.IN_TRANSIT)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = TrackingStatus.DELIVERED)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = TrackingStatus.EXCEPTION)
        }
    }
}

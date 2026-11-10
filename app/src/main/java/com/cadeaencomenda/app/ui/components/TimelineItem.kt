package com.cadeaencomenda.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cadeaencomenda.app.domain.model.TrackingEvent
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Reusable vertical timeline item for the package detail screen.
 * Displays a connected vertical line with an accent circular node for the latest event
 * and muted nodes for older events, alongside the event status, description, location, and date/time.
 */
@Composable
fun TimelineItem(
    event: TrackingEvent,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val lineCol = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    val formattedDateTime = remember(event.timestamp) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
        event.timestamp.format(formatter)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left timeline column with line and node
        Box(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
        ) {
            Canvas(modifier = Modifier.fillMaxHeight().width(36.dp)) {
                val centerX = size.width / 2f
                val nodeCenterY = 18.dp.toPx()
                val lineWidth = 2.dp.toPx()

                // Top connection line
                if (!isFirst) {
                    drawLine(
                        color = lineCol,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, nodeCenterY - (if (isFirst) 8.dp.toPx() else 6.dp.toPx())),
                        strokeWidth = lineWidth
                    )
                }

                // Bottom connection line
                if (!isLast) {
                    drawLine(
                        color = lineCol,
                        start = Offset(centerX, nodeCenterY + (if (isFirst) 8.dp.toPx() else 6.dp.toPx())),
                        end = Offset(centerX, size.height),
                        strokeWidth = lineWidth
                    )
                }

                // Circular node
                if (isFirst) {
                    // Outer accent circle
                    drawCircle(
                        color = primaryColor,
                        radius = 8.dp.toPx(),
                        center = Offset(centerX, nodeCenterY)
                    )
                    // Inner dot
                    drawCircle(
                        color = surfaceColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(centerX, nodeCenterY)
                    )
                } else {
                    // Gray muted circle
                    drawCircle(
                        color = lineCol,
                        radius = 5.dp.toPx(),
                        center = Offset(centerX, nodeCenterY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right event details column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 8.dp else 24.dp)
        ) {
            // Status and timestamp header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.status,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isFirst) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isFirst) primaryColor else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formattedDateTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Event Description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Optional Location with pin icon
            if (!event.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = "Localização",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineItemPreview() {
    CadeAEncomendaTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TimelineItem(
                event = TrackingEvent(
                    id = 1L,
                    trackingCode = "NL123456789BR",
                    status = "Saiu para entrega",
                    description = "Objeto saiu para entrega ao destinatário",
                    location = "CDD São Paulo - SP",
                    timestamp = LocalDateTime.now().minusHours(1)
                ),
                isFirst = true,
                isLast = false
            )
            TimelineItem(
                event = TrackingEvent(
                    id = 2L,
                    trackingCode = "NL123456789BR",
                    status = "Em trânsito",
                    description = "Objeto em trânsito - de Unidade de Tratamento para Unidade de Distribuição",
                    location = "CTE Cajamar - SP",
                    timestamp = LocalDateTime.now().minusDays(1)
                ),
                isFirst = false,
                isLast = false
            )
            TimelineItem(
                event = TrackingEvent(
                    id = 3L,
                    trackingCode = "NL123456789BR",
                    status = "Postado",
                    description = "Objeto postado após o horário limite da agência",
                    location = "AC Curitiba - PR",
                    timestamp = LocalDateTime.now().minusDays(3)
                ),
                isFirst = false,
                isLast = true
            )
        }
    }
}

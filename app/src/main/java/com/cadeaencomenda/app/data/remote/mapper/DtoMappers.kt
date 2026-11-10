package com.cadeaencomenda.app.data.remote.mapper

import com.cadeaencomenda.app.data.local.entity.PackageEntity
import com.cadeaencomenda.app.data.local.entity.TrackingEventEntity
import com.cadeaencomenda.app.data.remote.dto.TrackingEventDto
import com.cadeaencomenda.app.data.remote.dto.TrackingResponse
import com.cadeaencomenda.app.domain.model.TrackingEvent
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.model.TrackingStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun parseIsoDateTime(dateString: String?): LocalDateTime {
    if (dateString.isNullOrBlank()) {
        return LocalDateTime.now()
    }
    val trimmed = dateString.trim()

    return try {
        val instant = Instant.parse(trimmed)
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    } catch (_: Exception) {
        try {
            val offsetDateTime = OffsetDateTime.parse(trimmed)
            offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        } catch (_: Exception) {
            try {
                val zonedDateTime = ZonedDateTime.parse(trimmed)
                zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
            } catch (_: Exception) {
                try {
                    LocalDateTime.parse(trimmed)
                } catch (_: Exception) {
                    try {
                        val spaceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        LocalDateTime.parse(trimmed, spaceFormatter)
                    } catch (_: Exception) {
                        try {
                            LocalDate.parse(trimmed).atStartOfDay()
                        } catch (_: Exception) {
                            try {
                                LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_DATE_TIME)
                            } catch (_: Exception) {
                                LocalDateTime.now()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseIsoToEpochMillis(dateString: String?): Long {
    val dateTime = parseIsoDateTime(dateString)
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun TrackingEventDto.toDomain(trackingCode: String, id: Long = 0): TrackingEvent {
    return TrackingEvent(
        id = id,
        trackingCode = trackingCode,
        status = status,
        description = description,
        location = location,
        timestamp = parseIsoDateTime(timestamp)
    )
}

fun TrackingEventDto.toEntity(trackingCode: String, id: Long = 0): TrackingEventEntity {
    return TrackingEventEntity(
        id = id,
        trackingCode = trackingCode,
        status = status,
        description = description,
        location = location,
        timestamp = parseIsoToEpochMillis(timestamp)
    )
}

fun TrackingResponse.toDomainEvents(): List<TrackingEvent> {
    return events?.mapIndexed { index, dto ->
        dto.toDomain(trackingCode = trackingCode, id = (index + 1).toLong())
    }?.sortedByDescending { it.timestamp } ?: emptyList()
}

fun TrackingResponse.toEventEntities(): List<TrackingEventEntity> {
    return events?.map { dto ->
        dto.toEntity(trackingCode = trackingCode)
    } ?: emptyList()
}

fun TrackingResponse.toEntity(
    nickname: String? = null,
    carrierCode: String? = null,
    isArchived: Boolean = false,
    createdAt: Long = System.currentTimeMillis()
): PackageEntity {
    val sortedEventDtos = events?.sortedByDescending { parseIsoToEpochMillis(it.timestamp) }
    val latestEventDto = sortedEventDtos?.firstOrNull()
    val now = System.currentTimeMillis()

    val finalCarrierCode = carrier?.code ?: carrierCode
    val finalCarrierName = carrier?.name

    val currentStatusString = latestEventDto?.status ?: status
    val lastEventDateMillis = latestEventDto?.let { parseIsoToEpochMillis(it.timestamp) }

    return PackageEntity(
        trackingCode = trackingCode,
        nickname = nickname,
        carrierCode = finalCarrierCode,
        carrierName = finalCarrierName,
        currentStatus = currentStatusString,
        lastEvent = latestEventDto?.description,
        lastEventDate = lastEventDateMillis,
        lastEventLocation = latestEventDto?.location,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = now
    )
}

fun TrackingResponse.toDomain(
    nickname: String? = null,
    isArchived: Boolean = false,
    createdAt: LocalDateTime = LocalDateTime.now()
): TrackingPackage {
    val domainEvents = toDomainEvents()
    val latestEvent = domainEvents.firstOrNull()
    val currentStatusString = latestEvent?.status ?: status

    return TrackingPackage(
        trackingCode = trackingCode,
        nickname = nickname,
        carrierName = carrier?.name,
        carrierCode = carrier?.code,
        status = TrackingStatus.fromString(currentStatusString),
        lastEvent = latestEvent?.description,
        lastEventDate = latestEvent?.timestamp,
        lastEventLocation = latestEvent?.location,
        events = domainEvents,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = LocalDateTime.now()
    )
}

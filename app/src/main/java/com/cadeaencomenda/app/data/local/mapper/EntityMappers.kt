package com.cadeaencomenda.app.data.local.mapper

import com.cadeaencomenda.app.data.local.entity.PackageEntity
import com.cadeaencomenda.app.data.local.entity.TrackingEventEntity
import com.cadeaencomenda.app.domain.model.TrackingEvent
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.model.TrackingStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun Long?.toLocalDateTimeOrNull(): LocalDateTime? {
    return this?.let {
        try {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        } catch (_: Exception) {
            null
        }
    }
}

fun LocalDateTime.toEpochMillis(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun LocalDateTime?.toEpochMillisOrNull(): Long? {
    return this?.let {
        try {
            it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}

fun PackageEntity.toDomain(events: List<TrackingEvent> = emptyList()): TrackingPackage {
    return TrackingPackage(
        trackingCode = trackingCode,
        nickname = nickname,
        carrierName = carrierName,
        carrierCode = carrierCode,
        status = TrackingStatus.fromString(currentStatus),
        lastEvent = lastEvent,
        lastEventDate = lastEventDate.toLocalDateTimeOrNull(),
        lastEventLocation = lastEventLocation,
        events = events,
        isArchived = isArchived,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )
}

fun TrackingPackage.toEntity(): PackageEntity {
    return PackageEntity(
        trackingCode = trackingCode,
        nickname = nickname,
        carrierCode = carrierCode,
        carrierName = carrierName,
        currentStatus = status.name,
        lastEvent = lastEvent,
        lastEventDate = lastEventDate.toEpochMillisOrNull(),
        lastEventLocation = lastEventLocation,
        isArchived = isArchived,
        createdAt = createdAt.toEpochMillis(),
        updatedAt = updatedAt.toEpochMillis()
    )
}

fun TrackingEventEntity.toDomain(): TrackingEvent {
    return TrackingEvent(
        id = id,
        trackingCode = trackingCode,
        status = status,
        description = description,
        location = location,
        timestamp = timestamp.toLocalDateTime()
    )
}

fun TrackingEvent.toEntity(): TrackingEventEntity {
    return TrackingEventEntity(
        id = id,
        trackingCode = trackingCode,
        status = status,
        description = description,
        location = location,
        timestamp = timestamp.toEpochMillis()
    )
}

fun List<PackageEntity>.toDomain(): List<TrackingPackage> {
    return map { it.toDomain() }
}

fun List<TrackingEventEntity>.toEventDomainList(): List<TrackingEvent> {
    return map { it.toDomain() }
}

fun List<TrackingEvent>.toEventEntityList(): List<TrackingEventEntity> {
    return map { it.toEntity() }
}

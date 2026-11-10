package com.cadeaencomenda.app.domain.model

import java.time.LocalDateTime

data class TrackingPackage(
    val trackingCode: String,
    val nickname: String?,
    val carrierName: String?,
    val carrierCode: String?,
    val status: TrackingStatus,
    val lastEvent: String?,
    val lastEventDate: LocalDateTime?,
    val lastEventLocation: String?,
    val events: List<TrackingEvent> = emptyList(),
    val isArchived: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    val displayName: String
        get() = nickname ?: trackingCode
}

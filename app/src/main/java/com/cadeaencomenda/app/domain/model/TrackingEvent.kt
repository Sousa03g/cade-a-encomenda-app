package com.cadeaencomenda.app.domain.model

import java.time.LocalDateTime

data class TrackingEvent(
    val id: Long,
    val trackingCode: String,
    val status: String,
    val description: String,
    val location: String?,
    val timestamp: LocalDateTime
)

package com.cadeaencomenda.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrackingResponse(
    @Json(name = "trackingCode") val trackingCode: String,
    @Json(name = "carrier") val carrier: CarrierDto? = null,
    @Json(name = "events") val events: List<TrackingEventDto>? = null,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class CarrierDto(
    @Json(name = "code") val code: String? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class TrackingEventDto(
    @Json(name = "status") val status: String,
    @Json(name = "description") val description: String,
    @Json(name = "location") val location: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null
)

@JsonClass(generateAdapter = true)
data class TrackRequest(
    @Json(name = "trackingCode") val trackingCode: String
)

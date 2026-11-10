package com.cadeaencomenda.app.domain.model

import com.cadeaencomenda.app.R

enum class TrackingStatus(
    val displayNameRes: Int,
    val colorHex: String
) {
    POSTED(R.string.status_posted, "#2196F3"),
    IN_TRANSIT(R.string.status_in_transit, "#FF9800"),
    OUT_FOR_DELIVERY(R.string.status_out_for_delivery, "#4CAF50"),
    DELIVERED(R.string.status_delivered, "#009688"),
    RETURNED(R.string.status_returned, "#F44336"),
    EXCEPTION(R.string.status_exception, "#E91E63"),
    UNKNOWN(R.string.status_unknown, "#9E9E9E");

    companion object {
        fun fromString(status: String): TrackingStatus {
            return when (status.uppercase().trim()) {
                "POSTED", "LABEL_CREATED", "ACCEPTED", "COLLECTED" -> POSTED
                "IN_TRANSIT", "TRANSIT", "IN TRANSIT", "TRANSITO" -> IN_TRANSIT
                "OUT_FOR_DELIVERY", "OUT FOR DELIVERY", "DELIVERING", "SAIU_PARA_ENTREGA" -> OUT_FOR_DELIVERY
                "DELIVERED", "ENTREGUE" -> DELIVERED
                "RETURNED", "RETURN", "DEVOLVIDO" -> RETURNED
                "EXCEPTION", "ERROR", "FAILED", "FAILURE", "CUSTOMS_HOLD", "HELD" -> EXCEPTION
                else -> UNKNOWN
            }
        }
    }
}

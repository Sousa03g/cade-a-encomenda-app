package com.cadeaencomenda.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey
    val trackingCode: String,
    val nickname: String?,
    val carrierCode: String?,
    val carrierName: String?,
    val currentStatus: String,
    val lastEvent: String?,
    val lastEventDate: Long?,
    val lastEventLocation: String?,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

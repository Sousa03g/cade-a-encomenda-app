package com.cadeaencomenda.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracking_events",
    foreignKeys = [
        ForeignKey(
            entity = PackageEntity::class,
            parentColumns = ["trackingCode"],
            childColumns = ["trackingCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trackingCode"])]
)
data class TrackingEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackingCode: String,
    val status: String,
    val description: String,
    val location: String?,
    val timestamp: Long
)

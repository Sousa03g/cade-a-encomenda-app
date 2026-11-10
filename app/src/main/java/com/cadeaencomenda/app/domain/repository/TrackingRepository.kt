package com.cadeaencomenda.app.domain.repository

import com.cadeaencomenda.app.domain.model.TrackingPackage
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {

    fun getActivePackages(): Flow<List<TrackingPackage>>

    fun getArchivedPackages(): Flow<List<TrackingPackage>>

    fun getPackageWithEvents(trackingCode: String): Flow<TrackingPackage?>

    suspend fun addPackage(trackingCode: String, nickname: String?): Result<TrackingPackage>

    suspend fun refreshTracking(trackingCode: String): Result<TrackingPackage>

    suspend fun refreshAllTracking(): Result<Unit>

    suspend fun archivePackage(trackingCode: String)

    suspend fun unarchivePackage(trackingCode: String)

    suspend fun deletePackage(trackingCode: String)

    fun getPackageCount(): Flow<Int>
}

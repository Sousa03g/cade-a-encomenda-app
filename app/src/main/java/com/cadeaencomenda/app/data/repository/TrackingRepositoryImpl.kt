package com.cadeaencomenda.app.data.repository

import com.cadeaencomenda.app.data.local.dao.PackageDao
import com.cadeaencomenda.app.data.local.dao.TrackingEventDao
import com.cadeaencomenda.app.data.local.entity.PackageEntity
import com.cadeaencomenda.app.data.local.mapper.toDomain
import com.cadeaencomenda.app.data.local.mapper.toEventDomainList
import com.cadeaencomenda.app.data.remote.api.TrackingApiService
import com.cadeaencomenda.app.data.remote.dto.TrackRequest
import com.cadeaencomenda.app.data.remote.mapper.toEntity
import com.cadeaencomenda.app.data.remote.mapper.toEventEntities
import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.model.TrackingStatus
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    private val packageDao: PackageDao,
    private val trackingEventDao: TrackingEventDao,
    private val trackingApiService: TrackingApiService
) : TrackingRepository {

    override fun getActivePackages(): Flow<List<TrackingPackage>> {
        return packageDao.getActivePackages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedPackages(): Flow<List<TrackingPackage>> {
        return packageDao.getArchivedPackages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPackageWithEvents(trackingCode: String): Flow<TrackingPackage?> {
        return packageDao.getPackageFlowByCode(trackingCode).combine(
            trackingEventDao.getEventsForPackage(trackingCode)
        ) { packageEntity, eventEntities ->
            packageEntity?.toDomain(events = eventEntities.toEventDomainList())
        }
    }

    override suspend fun addPackage(
        trackingCode: String,
        nickname: String?
    ): Result<TrackingPackage> {
        return withContext(Dispatchers.IO) {
            val existing = packageDao.getPackageByCode(trackingCode)
            try {
                val response = try {
                    trackingApiService.trackPackage(TrackRequest(trackingCode = trackingCode))
                } catch (_: Exception) {
                    trackingApiService.getTracking(trackingCode = trackingCode)
                }

                val packageEntity = response.toEntity(
                    nickname = nickname ?: existing?.nickname,
                    carrierCode = existing?.carrierCode,
                    isArchived = existing?.isArchived ?: false,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
                val eventEntities = response.toEventEntities()

                packageDao.insertPackage(packageEntity)
                trackingEventDao.deleteEventsForPackage(trackingCode)
                if (eventEntities.isNotEmpty()) {
                    trackingEventDao.insertEvents(eventEntities)
                }

                val savedEvents = trackingEventDao.getEventsForPackageOnce(trackingCode).toEventDomainList()
                Result.success(packageEntity.toDomain(events = savedEvents))
            } catch (e: Exception) {
                // Fallback for offline or API error: create an initial local package entry
                val now = System.currentTimeMillis()
                val fallbackEntity = existing?.copy(
                    nickname = nickname ?: existing.nickname,
                    updatedAt = now
                ) ?: PackageEntity(
                    trackingCode = trackingCode,
                    nickname = nickname,
                    carrierCode = null,
                    carrierName = null,
                    currentStatus = TrackingStatus.UNKNOWN.name,
                    lastEvent = "Aguardando atualização",
                    lastEventDate = now,
                    lastEventLocation = null,
                    isArchived = false,
                    createdAt = now,
                    updatedAt = now
                )

                packageDao.insertPackage(fallbackEntity)
                val currentEvents = trackingEventDao.getEventsForPackageOnce(trackingCode).toEventDomainList()
                Result.success(fallbackEntity.toDomain(events = currentEvents))
            }
        }
    }

    override suspend fun refreshTracking(trackingCode: String): Result<TrackingPackage> {
        return withContext(Dispatchers.IO) {
            val existing = packageDao.getPackageByCode(trackingCode)
            try {
                val response = try {
                    trackingApiService.getTracking(trackingCode = trackingCode)
                } catch (_: Exception) {
                    trackingApiService.trackPackage(TrackRequest(trackingCode = trackingCode))
                }

                val packageEntity = response.toEntity(
                    nickname = existing?.nickname,
                    carrierCode = existing?.carrierCode,
                    isArchived = existing?.isArchived ?: false,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
                val eventEntities = response.toEventEntities()

                packageDao.insertPackage(packageEntity)
                trackingEventDao.deleteEventsForPackage(trackingCode)
                if (eventEntities.isNotEmpty()) {
                    trackingEventDao.insertEvents(eventEntities)
                }

                val savedEvents = trackingEventDao.getEventsForPackageOnce(trackingCode).toEventDomainList()
                Result.success(packageEntity.toDomain(events = savedEvents))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshAllTracking(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val activePackages = packageDao.getActivePackagesOnce()
                for (pkg in activePackages) {
                    try {
                        refreshTracking(pkg.trackingCode)
                    } catch (_: Exception) {
                        // Continue attempting refresh for remaining packages
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun archivePackage(trackingCode: String) {
        withContext(Dispatchers.IO) {
            packageDao.archivePackage(trackingCode)
        }
    }

    override suspend fun unarchivePackage(trackingCode: String) {
        withContext(Dispatchers.IO) {
            packageDao.unarchivePackage(trackingCode)
        }
    }

    override suspend fun deletePackage(trackingCode: String) {
        withContext(Dispatchers.IO) {
            trackingEventDao.deleteEventsForPackage(trackingCode)
            packageDao.deletePackage(trackingCode)
        }
    }

    override fun getPackageCount(): Flow<Int> {
        return packageDao.getPackageCount()
    }
}

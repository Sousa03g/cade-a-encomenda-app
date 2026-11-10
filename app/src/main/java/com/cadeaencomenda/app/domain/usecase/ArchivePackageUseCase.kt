package com.cadeaencomenda.app.domain.usecase

import com.cadeaencomenda.app.domain.repository.TrackingRepository
import javax.inject.Inject

class ArchivePackageUseCase @Inject constructor(
    private val repository: TrackingRepository
) {

    suspend operator fun invoke(trackingCode: String) {
        archive(trackingCode)
    }

    suspend fun archive(trackingCode: String) {
        repository.archivePackage(trackingCode)
    }

    suspend fun unarchive(trackingCode: String) {
        repository.unarchivePackage(trackingCode)
    }
}

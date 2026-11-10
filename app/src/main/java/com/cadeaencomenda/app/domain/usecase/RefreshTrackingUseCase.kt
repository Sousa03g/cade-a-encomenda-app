package com.cadeaencomenda.app.domain.usecase

import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import javax.inject.Inject

class RefreshTrackingUseCase @Inject constructor(
    private val repository: TrackingRepository
) {

    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshAllTracking()
    }

    suspend operator fun invoke(trackingCode: String): Result<TrackingPackage> {
        return repository.refreshTracking(trackingCode)
    }
}

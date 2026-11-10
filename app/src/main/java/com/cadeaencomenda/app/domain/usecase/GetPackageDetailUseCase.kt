package com.cadeaencomenda.app.domain.usecase

import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetPackageDetailUseCase @Inject constructor(
    private val repository: TrackingRepository
) {

    operator fun invoke(trackingCode: String): Flow<TrackingPackage?> {
        return repository.getPackageWithEvents(trackingCode)
            .onStart {
                // Trigger a background refresh when detail is opened
                repository.refreshTracking(trackingCode)
            }
    }
}

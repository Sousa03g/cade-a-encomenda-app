package com.cadeaencomenda.app.domain.usecase

import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPackagesUseCase @Inject constructor(
    private val repository: TrackingRepository
) {

    operator fun invoke(): Flow<List<TrackingPackage>> {
        return repository.getActivePackages()
    }
}

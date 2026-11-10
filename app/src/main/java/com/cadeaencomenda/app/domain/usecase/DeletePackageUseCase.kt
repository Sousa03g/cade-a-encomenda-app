package com.cadeaencomenda.app.domain.usecase

import com.cadeaencomenda.app.domain.repository.TrackingRepository
import javax.inject.Inject

class DeletePackageUseCase @Inject constructor(
    private val repository: TrackingRepository
) {

    suspend operator fun invoke(trackingCode: String) {
        repository.deletePackage(trackingCode)
    }
}

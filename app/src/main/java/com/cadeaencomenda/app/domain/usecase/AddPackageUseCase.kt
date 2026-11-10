package com.cadeaencomenda.app.domain.usecase

import com.cadeaencomenda.app.domain.model.TrackingPackage
import com.cadeaencomenda.app.domain.repository.TrackingRepository
import javax.inject.Inject

class AddPackageUseCase @Inject constructor(
    private val repository: TrackingRepository
) {

    suspend operator fun invoke(
        trackingCode: String,
        nickname: String?
    ): Result<TrackingPackage> {
        val trimmedCode = trackingCode.trim()

        if (trimmedCode.isBlank()) {
            return Result.failure(IllegalArgumentException("O código de rastreio não pode estar vazio."))
        }

        if (trimmedCode.length < 8) {
            return Result.failure(
                IllegalArgumentException("O código de rastreio deve ter pelo menos 8 caracteres.")
            )
        }

        return repository.addPackage(trimmedCode, nickname?.trim()?.ifBlank { null })
    }
}

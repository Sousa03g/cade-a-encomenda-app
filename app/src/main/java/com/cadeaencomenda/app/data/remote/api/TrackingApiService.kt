package com.cadeaencomenda.app.data.remote.api

import com.cadeaencomenda.app.data.remote.dto.TrackRequest
import com.cadeaencomenda.app.data.remote.dto.TrackingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TrackingApiService {

    @POST("trackPackage")
    suspend fun trackPackage(
        @Body request: TrackRequest
    ): TrackingResponse

    @GET("getTracking")
    suspend fun getTracking(
        @Query("code") trackingCode: String
    ): TrackingResponse
}

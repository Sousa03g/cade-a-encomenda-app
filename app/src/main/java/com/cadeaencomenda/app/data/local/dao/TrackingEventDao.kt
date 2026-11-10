package com.cadeaencomenda.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cadeaencomenda.app.data.local.entity.TrackingEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingEventDao {

    @Query("SELECT * FROM tracking_events WHERE trackingCode = :code ORDER BY timestamp DESC")
    fun getEventsForPackage(code: String): Flow<List<TrackingEventEntity>>

    @Query("SELECT * FROM tracking_events WHERE trackingCode = :code ORDER BY timestamp DESC")
    suspend fun getEventsForPackageOnce(code: String): List<TrackingEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<TrackingEventEntity>)

    @Query("DELETE FROM tracking_events WHERE trackingCode = :code")
    suspend fun deleteEventsForPackage(code: String)
}

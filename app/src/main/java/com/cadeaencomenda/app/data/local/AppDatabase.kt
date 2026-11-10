package com.cadeaencomenda.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cadeaencomenda.app.data.local.dao.PackageDao
import com.cadeaencomenda.app.data.local.dao.TrackingEventDao
import com.cadeaencomenda.app.data.local.entity.PackageEntity
import com.cadeaencomenda.app.data.local.entity.TrackingEventEntity

@Database(
    entities = [PackageEntity::class, TrackingEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun packageDao(): PackageDao

    abstract fun trackingEventDao(): TrackingEventDao

    companion object {
        const val DATABASE_NAME = "cade_a_encomenda.db"
    }
}

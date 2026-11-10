package com.cadeaencomenda.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cadeaencomenda.app.data.local.entity.PackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {

    @Query("SELECT * FROM packages WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getActivePackages(): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedPackages(): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE trackingCode = :code LIMIT 1")
    suspend fun getPackageByCode(code: String): PackageEntity?

    @Query("SELECT * FROM packages WHERE trackingCode = :code LIMIT 1")
    fun getPackageFlowByCode(code: String): Flow<PackageEntity?>

    @Query("SELECT * FROM packages WHERE isArchived = 0 ORDER BY updatedAt DESC")
    suspend fun getActivePackagesOnce(): List<PackageEntity>

    @Query("SELECT COUNT(*) FROM packages WHERE isArchived = 0")
    fun getPackageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity)

    @Update
    suspend fun updatePackage(pkg: PackageEntity)

    @Query("DELETE FROM packages WHERE trackingCode = :code")
    suspend fun deletePackage(code: String)

    @Query("UPDATE packages SET isArchived = 1 WHERE trackingCode = :code")
    suspend fun archivePackage(code: String)

    @Query("UPDATE packages SET isArchived = 0 WHERE trackingCode = :code")
    suspend fun unarchivePackage(code: String)
}

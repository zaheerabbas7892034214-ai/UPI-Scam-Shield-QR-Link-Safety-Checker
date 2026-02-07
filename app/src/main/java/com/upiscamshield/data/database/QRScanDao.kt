package com.upiscamshield.data.database

import androidx.room.*
import com.upiscamshield.data.model.QRScan
import com.upiscamshield.data.model.RiskLevel
import kotlinx.coroutines.flow.Flow

@Dao
interface QRScanDao {
    @Query("SELECT * FROM qr_scans ORDER BY scannedAt DESC")
    fun getAllScans(): Flow<List<QRScan>>
    
    @Query("SELECT * FROM qr_scans WHERE id = :id")
    suspend fun getScanById(id: Long): QRScan?
    
    @Query("SELECT * FROM qr_scans WHERE riskLevel = :riskLevel ORDER BY scannedAt DESC")
    fun getScansByRiskLevel(riskLevel: RiskLevel): Flow<List<QRScan>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: QRScan): Long
    
    @Delete
    suspend fun delete(scan: QRScan)
    
    @Query("DELETE FROM qr_scans")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM qr_scans")
    suspend fun getCount(): Int
}

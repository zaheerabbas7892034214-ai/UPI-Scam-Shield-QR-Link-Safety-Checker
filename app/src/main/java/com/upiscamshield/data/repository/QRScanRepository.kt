package com.upiscamshield.data.repository

import com.upiscamshield.data.database.QRScanDao
import com.upiscamshield.data.model.QRScan
import com.upiscamshield.data.model.RiskLevel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

class QRScanRepository(private val qrScanDao: QRScanDao) {
    
    fun getAllScans(): Flow<List<QRScan>> = qrScanDao.getAllScans()
    
    suspend fun getScanById(id: Long): QRScan? = qrScanDao.getScanById(id)
    
    fun getScansByRiskLevel(riskLevel: RiskLevel): Flow<List<QRScan>> =
        qrScanDao.getScansByRiskLevel(riskLevel)
    
    suspend fun insertScan(scan: QRScan): Long = qrScanDao.insert(scan)
    
    suspend fun deleteScan(scan: QRScan) = qrScanDao.delete(scan)
    
    suspend fun deleteAllScans() = qrScanDao.deleteAll()
    
    suspend fun getScansCount(): Int = qrScanDao.getCount()
}

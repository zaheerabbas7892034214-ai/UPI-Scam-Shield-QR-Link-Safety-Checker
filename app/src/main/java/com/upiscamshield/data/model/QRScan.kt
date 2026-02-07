package com.upiscamshield.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "qr_scans")
data class QRScan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val upiId: String,
    val merchantName: String?,
    val amount: String?,
    val transactionNote: String?,
    val riskLevel: RiskLevel,
    val riskScore: Float,
    val riskFactors: List<String>,
    val scannedAt: Long = System.currentTimeMillis(),
    val rawData: String
)

enum class RiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class UPIData(
    val upiId: String,
    val merchantName: String? = null,
    val amount: String? = null,
    val transactionNote: String? = null,
    val merchantCode: String? = null,
    val transactionRef: String? = null
)

data class RiskAnalysis(
    val riskLevel: RiskLevel,
    val riskScore: Float,
    val riskFactors: List<String>,
    val recommendations: List<String>
)

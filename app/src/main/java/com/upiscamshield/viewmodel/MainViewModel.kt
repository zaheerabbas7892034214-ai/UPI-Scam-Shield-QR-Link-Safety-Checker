package com.upiscamshield.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.upiscamshield.data.database.AppDatabase
import com.upiscamshield.data.model.*
import com.upiscamshield.data.repository.QRScanRepository
import com.upiscamshield.data.repository.SubscriptionRepository
import com.upiscamshield.utils.RiskAnalyzer
import com.upiscamshield.utils.UPIParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val qrScanRepository = QRScanRepository(database.qrScanDao())
    private val subscriptionRepository = SubscriptionRepository(application)
    
    val subscriptionState = subscriptionRepository.subscriptionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionState()
        )
    
    val scanHistory = qrScanRepository.getAllScans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _currentScanResult = MutableStateFlow<QRScan?>(null)
    val currentScanResult: StateFlow<QRScan?> = _currentScanResult.asStateFlow()
    
    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()
    
    sealed class ScannerState {
        object Idle : ScannerState()
        object Scanning : ScannerState()
        data class Result(val scan: QRScan) : ScannerState()
        data class Error(val message: String) : ScannerState()
        object RequiresSubscription : ScannerState()
    }
    
    fun processQRCode(qrCode: String) {
        viewModelScope.launch {
            try {
                _scannerState.value = ScannerState.Scanning
                
                // Check subscription/free scans
                val currentState = subscriptionState.value
                if (!currentState.isPremium && currentState.freeScansRemaining <= 0) {
                    _scannerState.value = ScannerState.RequiresSubscription
                    return@launch
                }
                
                // Parse UPI data
                val upiData = UPIParser.parseUPIString(qrCode)
                if (upiData == null) {
                    _scannerState.value = ScannerState.Error("Not a valid UPI QR code")
                    return@launch
                }
                
                // Analyze risk
                val riskAnalysis = RiskAnalyzer.analyzeRisk(upiData)
                
                // Create scan record
                val scan = QRScan(
                    upiId = upiData.upiId,
                    merchantName = upiData.merchantName,
                    amount = upiData.amount,
                    transactionNote = upiData.transactionNote,
                    riskLevel = riskAnalysis.riskLevel,
                    riskScore = riskAnalysis.riskScore,
                    riskFactors = riskAnalysis.riskFactors,
                    rawData = qrCode
                )
                
                // Save to database
                val id = qrScanRepository.insertScan(scan)
                
                // Decrement free scans if not premium
                if (!currentState.isPremium) {
                    subscriptionRepository.decrementFreeScans()
                }
                
                _currentScanResult.value = scan.copy(id = id)
                _scannerState.value = ScannerState.Result(scan.copy(id = id))
                
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetScannerState() {
        _scannerState.value = ScannerState.Idle
        _currentScanResult.value = null
    }
    
    fun deleteScan(scan: QRScan) {
        viewModelScope.launch {
            qrScanRepository.deleteScan(scan)
        }
    }
    
    fun clearAllScans() {
        viewModelScope.launch {
            qrScanRepository.deleteAllScans()
        }
    }
    
    fun activateSubscription() {
        viewModelScope.launch {
            // Calculate end date (1 year from now)
            val endDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
            subscriptionRepository.setSubscriptionActive(true, endDate)
        }
    }
}

package com.upiscamshield.utils

import com.upiscamshield.data.model.RiskAnalysis
import com.upiscamshield.data.model.RiskLevel
import com.upiscamshield.data.model.UPIData

object RiskAnalyzer {
    
    private val KNOWN_SCAM_KEYWORDS = listOf(
        "lottery", "prize", "winner", "claim", "urgent", "verify", "suspended",
        "block", "expired", "kbc", "kaun banega crorepati", "reward", "gift"
    )
    
    private val SUSPICIOUS_BANKS = listOf(
        "paytm", "phonepe", "googlepay", "unknown"
    )
    
    fun analyzeRisk(upiData: UPIData): RiskAnalysis {
        val riskFactors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var riskScore = 0f
        
        // Check UPI ID validity
        if (!UPIParser.isValidUPIId(upiData.upiId)) {
            riskFactors.add("Invalid UPI ID format")
            riskScore += 0.4f
        }
        
        // Check merchant name for scam keywords
        upiData.merchantName?.let { name ->
            val lowerName = name.lowercase()
            if (KNOWN_SCAM_KEYWORDS.any { lowerName.contains(it) }) {
                riskFactors.add("Merchant name contains suspicious keywords")
                riskScore += 0.3f
            }
            
            if (name.length < 3) {
                riskFactors.add("Merchant name too short")
                riskScore += 0.1f
            }
        } ?: run {
            riskFactors.add("No merchant name provided")
            riskScore += 0.15f
        }
        
        // Check transaction note
        upiData.transactionNote?.let { note ->
            val lowerNote = note.lowercase()
            if (KNOWN_SCAM_KEYWORDS.any { lowerNote.contains(it) }) {
                riskFactors.add("Transaction note contains suspicious keywords")
                riskScore += 0.25f
            }
        }
        
        // Check amount
        upiData.amount?.let { amount ->
            try {
                val amountValue = amount.toFloatOrNull()
                if (amountValue != null && amountValue > 10000) {
                    riskFactors.add("Large transaction amount (₹${amount})")
                    riskScore += 0.2f
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
        
        // Check bank/PSP
        val bank = upiData.upiId.substringAfter("@").lowercase()
        if (SUSPICIOUS_BANKS.any { bank.contains(it) }) {
            riskFactors.add("Using payment app UPI (not direct bank)")
            riskScore += 0.1f
        }
        
        // Check for generic/suspicious UPI IDs
        val username = upiData.upiId.substringBefore("@")
        if (username.matches(Regex("^[0-9]+$"))) {
            riskFactors.add("UPI ID is only numbers")
            riskScore += 0.15f
        }
        
        // Determine risk level
        val riskLevel = when {
            riskScore >= 0.8f -> RiskLevel.CRITICAL
            riskScore >= 0.6f -> RiskLevel.HIGH
            riskScore >= 0.4f -> RiskLevel.MEDIUM
            riskScore >= 0.2f -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }
        
        // Generate recommendations
        when (riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> {
                recommendations.add("⚠️ Do NOT proceed with this payment")
                recommendations.add("This transaction shows multiple red flags")
                recommendations.add("Report this QR code if received unsolicited")
            }
            RiskLevel.MEDIUM -> {
                recommendations.add("⚠️ Exercise caution")
                recommendations.add("Verify merchant identity before paying")
                recommendations.add("Consider using alternative payment method")
            }
            RiskLevel.LOW -> {
                recommendations.add("Minor concerns detected")
                recommendations.add("Verify transaction details before proceeding")
            }
            RiskLevel.SAFE -> {
                recommendations.add("✓ No major concerns detected")
                recommendations.add("Always verify recipient before paying")
            }
        }
        
        if (riskFactors.isEmpty()) {
            riskFactors.add("No suspicious indicators found")
        }
        
        return RiskAnalysis(
            riskLevel = riskLevel,
            riskScore = riskScore.coerceIn(0f, 1f),
            riskFactors = riskFactors,
            recommendations = recommendations
        )
    }
}

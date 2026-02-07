package com.upiscamshield.utils

import android.net.Uri
import com.upiscamshield.data.model.UPIData

object UPIParser {
    
    fun parseUPIString(upiString: String): UPIData? {
        try {
            if (!upiString.startsWith("upi://", ignoreCase = true)) {
                return null
            }
            
            val uri = Uri.parse(upiString)
            val path = uri.path ?: return null
            
            // Extract UPI ID from path (e.g., upi://pay?pa=merchant@bank)
            val upiId = uri.getQueryParameter("pa") ?: return null
            
            return UPIData(
                upiId = upiId,
                merchantName = uri.getQueryParameter("pn"),
                amount = uri.getQueryParameter("am"),
                transactionNote = uri.getQueryParameter("tn"),
                merchantCode = uri.getQueryParameter("mc"),
                transactionRef = uri.getQueryParameter("tr")
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    fun isValidUPIId(upiId: String): Boolean {
        // UPI ID format: username@bank
        val upiRegex = Regex("^[a-zA-Z0-9.\\-_]{3,}@[a-zA-Z]{3,}$")
        return upiRegex.matches(upiId)
    }
}

package com.upiscamshield.data.model

data class SubscriptionState(
    val isPremium: Boolean = false,
    val freeScansRemaining: Int = 3,
    val subscriptionEndDate: Long? = null
)

data class BillingProduct(
    val productId: String = "premium_yearly",
    val title: String = "Premium Yearly",
    val description: String = "Unlock all premium features",
    val price: String = "₹499",
    val priceMicros: Long = 499000000,
    val currency: String = "INR"
)

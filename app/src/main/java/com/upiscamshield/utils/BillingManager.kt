package com.upiscamshield.utils

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val context: Context) {
    
    private var billingClient: BillingClient? = null
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NotPurchased)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    sealed class PurchaseState {
        object NotPurchased : PurchaseState()
        object Purchased : PurchaseState()
        object Pending : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }
    
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseState.value = PurchaseState.NotPurchased
        } else {
            _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
        }
    }
    
    fun initialize(onReady: () -> Unit = {}) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    onReady()
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // Retry connection
            }
        })
    }
    
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchase = purchases.firstOrNull { 
                    it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED 
                }
                _purchaseState.value = if (activePurchase != null) {
                    PurchaseState.Purchased
                } else {
                    PurchaseState.NotPurchased
                }
            }
        }
    }
    
    suspend fun loadProductDetails(): ProductDetails? {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        val result = billingClient?.queryProductDetails(params)
        return result?.productDetailsList?.firstOrNull()
    }
    
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                _purchaseState.value = PurchaseState.Purchased
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Pending
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchaseState.value = PurchaseState.Purchased
            }
        }
    }
    
    fun endConnection() {
        billingClient?.endConnection()
    }
}

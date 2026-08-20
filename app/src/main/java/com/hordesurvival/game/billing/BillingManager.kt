package com.hordesurvival.game.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

/**
 * Google Play Billing manager for in-app purchases.
 * Products:
 * - remove_ads: Remove all ads
 * - premium_pack: Premium characters + skins
 * - gold_pack_1/2/3: Gold bundles
 */
object BillingManager {

    private var billingClient: BillingClient? = null
    private var isConnected = false

    // Product IDs
    const val PRODUCT_REMOVE_ADS = "remove_ads"
    const val PRODUCT_PREMIUM_PACK = "premium_pack"
    const val PRODUCT_GOLD_SMALL = "gold_pack_1"
    const val PRODUCT_GOLD_MEDIUM = "gold_pack_2"
    const val PRODUCT_GOLD_LARGE = "gold_pack_3"

    private val productIds = listOf(
        PRODUCT_REMOVE_ADS, PRODUCT_PREMIUM_PACK,
        PRODUCT_GOLD_SMALL, PRODUCT_GOLD_MEDIUM, PRODUCT_GOLD_LARGE
    )

    var onPurchaseComplete: ((String, Boolean) -> Unit)? = null

    /** Initialize billing client */
    fun initialize(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                isConnected = result.responseCode == BillingClient.BillingResponseCode.OK
                Log.d("BillingManager", "Setup: ${result.responseCode}")
            }
            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
        })
    }

    /** Query available products */
    fun queryProducts(callback: (List<ProductDetails>) -> Unit) {
        if (!isConnected) { callback(emptyList()); return }
        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient?.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                callback(details ?: emptyList())
            } else {
                callback(emptyList())
            }
        }
    }

    /** Launch purchase flow */
    fun purchase(activity: Activity, productDetails: ProductDetails) {
        if (!isConnected) return
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            ))
            .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    /** Check if user has purchased remove_ads */
    fun isAdFree(callback: (Boolean) -> Unit) {
        if (!isConnected) { callback(false); return }
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val adFree = purchases?.any { it.products.contains(PRODUCT_REMOVE_ADS) && it.purchaseState == Purchase.PurchaseState.PURCHASED } == true
                callback(adFree)
            } else {
                callback(false)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge purchase
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(params) { result ->
                    Log.d("BillingManager", "Acknowledge: ${result.responseCode}")
                }
            }
            for (product in purchase.products) {
                onPurchaseComplete?.invoke(product, true)
            }
        }
    }

    /** Cleanup */
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
        isConnected = false
    }
}

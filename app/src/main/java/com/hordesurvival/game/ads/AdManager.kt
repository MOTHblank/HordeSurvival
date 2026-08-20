package com.hordesurvival.game.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manages AdMob rewarded ads for Continue After Death feature.
 * Uses test ad unit IDs — replace with real ones before release.
 */
object AdManager {

    // Test ad unit IDs — REPLACE WITH REAL ONES BEFORE RELEASE
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var onRewardEarned: (() -> Unit)? = null

    /** Pre-load a rewarded ad */
    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoading = false
                Log.d("AdManager", "Rewarded ad loaded")
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                isLoading = false
                Log.w("AdManager", "Ad failed to load: ${error.message}")
            }
        })
    }

    /** Show rewarded ad. Returns true if ad was shown, false if not available. */
    fun showRewardedAd(activity: Activity, onReward: () -> Unit): Boolean {
        val ad = rewardedAd ?: return false
        onRewardEarned = onReward
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                onRewardEarned = null
                // Preload next ad
                preload(activity)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                onRewardEarned = null
                Log.w("AdManager", "Ad failed to show: ${error.message}")
            }
        }
        ad.show(activity) { rewardItem ->
            Log.d("AdManager", "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned?.invoke()
        }
        return true
    }

    /** Check if a rewarded ad is ready */
    fun isAdReady(): Boolean = rewardedAd != null

    /** Cleanup */
    fun destroy() {
        rewardedAd = null
        onRewardEarned = null
    }
}

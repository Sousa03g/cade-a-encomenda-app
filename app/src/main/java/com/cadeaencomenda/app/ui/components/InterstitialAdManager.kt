package com.cadeaencomenda.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.cadeaencomenda.app.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val TAG = "InterstitialAdManager"
private const val PREFS_NAME = "cade_a_encomenda_ad_prefs"
private const val KEY_LAST_SHOWN_TIMESTAMP = "last_interstitial_shown_millis"
private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

/**
 * Singleton manager to preload, throttle, and display AdMob Interstitial Ads.
 * Implements frequency capping via SharedPreferences and automatic fallback to test unit
 * during account warmup.
 */
object InterstitialAdManager {

    private const val FREQUENCY_CAP_INTERVAL_MILLIS = 3L * 60L * 60L * 1000L

    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false

    fun loadAd(context: Context) {
        loadWithUnitId(context, BuildConfig.ADMOB_INTERSTITIAL_ID)
    }

    private fun loadWithUnitId(context: Context, unitId: String) {
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            unitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully with unit: $unitId")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Interstitial failed to load ($unitId): ${loadAdError.message}. Code: ${loadAdError.code}")

                    // Fallback to Google test unit if production unit has no fill (new account)
                    if (unitId != TEST_INTERSTITIAL_ID) {
                        Log.i(TAG, "Retrying interstitial with test unit...")
                        loadWithUnitId(context, TEST_INTERSTITIAL_ID)
                    }
                }
            }
        )
    }

    fun canShowAd(context: Context, force: Boolean = false): Boolean {
        if (force) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastShown = prefs.getLong(KEY_LAST_SHOWN_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        return (now - lastShown) >= FREQUENCY_CAP_INTERVAL_MILLIS
    }

    fun showAdIfReady(
        activity: Activity,
        force: Boolean = false,
        onDismissed: () -> Unit = {}
    ) {
        val currentAd = interstitialAd
        if (currentAd != null && canShowAd(activity, force)) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    recordAdShown(activity)
                    loadAd(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadAd(activity)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed full screen.")
                }
            }
            currentAd.show(activity)
        } else {
            if (currentAd == null) {
                loadAd(activity)
            }
            onDismissed()
        }
    }

    fun show(context: Context, onDismissed: () -> Unit = {}) {
        val activity = context.findActivity()
        if (activity != null) {
            showAdIfReady(activity = activity, force = true, onDismissed = onDismissed)
        } else {
            loadAd(context)
            onDismissed()
        }
    }

    private fun recordAdShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SHOWN_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    private fun Context.findActivity(): Activity? {
        var current: Context? = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}

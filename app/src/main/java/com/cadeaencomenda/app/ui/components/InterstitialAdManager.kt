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

/**
 * Singleton manager to preload, throttle, and display AdMob Interstitial Ads.
 * Implements frequency capping via SharedPreferences (default: maximum 1 ad per 3 hours,
 * or immediate display on key actions like after adding a package).
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    private const val PREFS_NAME = "cade_a_encomenda_ad_prefs"
    private const val KEY_LAST_SHOWN_TIMESTAMP = "last_interstitial_shown_millis"

    // Default frequency cap: 3 hours between impressions
    private const val FREQUENCY_CAP_INTERVAL_MILLIS = 3L * 60L * 60L * 1000L

    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false

    /**
     * Preloads an interstitial ad using the configured AdMob unit ID.
     */
    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) {
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Verifies whether enough time has elapsed since the last ad impression according to frequency capping.
     */
    fun canShowAd(context: Context, force: Boolean = false): Boolean {
        if (force) return true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastShown = prefs.getLong(KEY_LAST_SHOWN_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()

        return (now - lastShown) >= FREQUENCY_CAP_INTERVAL_MILLIS
    }

    /**
     * Shows the preloaded interstitial ad if ready and frequency capping allows.
     * Automatically requests preloading of the next ad upon dismissal or failure.
     *
     * @param activity The host activity required by AdMob to display the fullscreen ad.
     * @param force Set to true to bypass the 3-hour frequency cap (e.g. after adding a package).
     * @param onDismissed Callback executed when the ad is closed, fails, or was not ready.
     */
    fun showAdIfReady(
        activity: Activity,
        force: Boolean = false,
        onDismissed: () -> Unit = {}
    ) {
        val currentAd = interstitialAd

        if (currentAd != null && canShowAd(activity, force)) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed by user.")
                    recordAdShown(activity)
                    interstitialAd = null
                    loadAd(activity) // Preload next ad
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Failed to display interstitial ad: ${adError.message}")
                    interstitialAd = null
                    loadAd(activity)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad presented on screen.")
                }
            }

            currentAd.show(activity)
        } else {
            if (currentAd == null) {
                // Ensure an ad is being loaded for next time
                loadAd(activity)
            }
            onDismissed()
        }
    }

    /**
     * Convenience method to show the ad given any [Context].
     * Resolves the underlying [Activity] automatically, forces show (e.g. after package added),
     * and ensures the app flow continues via [onDismissed].
     */
    fun show(context: Context, onDismissed: () -> Unit = {}) {
        val activity = context.findActivity()
        if (activity != null) {
            showAdIfReady(activity = activity, force = true, onDismissed = onDismissed)
        } else {
            loadAd(context)
            onDismissed()
        }
    }

    /**
     * Saves the current timestamp as the last ad impression timestamp.
     */
    private fun recordAdShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SHOWN_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    /**
     * Resolves the nearest [Activity] from a given [Context].
     */
    private fun Context.findActivity(): Activity? {
        var current: Context? = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}

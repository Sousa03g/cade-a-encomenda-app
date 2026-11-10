package com.cadeaencomenda.app.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cadeaencomenda.app.BuildConfig
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "AdBannerView"
private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

/**
 * Composable hosting an adaptive Google Mobile Ads [AdView].
 * Automatically configures adaptive banner sizing based on screen width,
 * binds to the lifecycle for pause/resume/destroy, and handles fallback to test ads
 * while new AdMob accounts are propagating.
 */
@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.ADMOB_BANNER_ID
) {
    if (LocalInspectionMode.current) {
        AdBannerPlaceholder(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val lifecycleOwner = LocalLifecycleOwner.current

    var adViewInstance by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(lifecycleOwner, adViewInstance) {
        val adView = adViewInstance ?: return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    val adaptiveSize = try {
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, screenWidthDp)
                    } catch (_: Exception) {
                        AdSize.BANNER
                    }
                    setAdSize(adaptiveSize)
                    setAdUnitId(adUnitId)

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "AdMob banner loaded successfully with unit: $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w(TAG, "Banner failed to load ($adUnitId): ${error.message}. Code: ${error.code}")
                            // If production unit has no fill (new account/unit propagation), retry with test unit
                            if (adUnitId != TEST_BANNER_ID) {
                                post {
                                    try {
                                        setAdUnitId(TEST_BANNER_ID)
                                        loadAd(AdRequest.Builder().build())
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Fallback banner load failed", e)
                                    }
                                }
                            }
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                    adViewInstance = this
                }
            }
        )
    }
}

@Composable
fun AdBannerPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Anúncio AdMob (Banner)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdBannerViewPreview() {
    CadeAEncomendaTheme {
        AdBannerView(modifier = Modifier.fillMaxWidth())
    }
}

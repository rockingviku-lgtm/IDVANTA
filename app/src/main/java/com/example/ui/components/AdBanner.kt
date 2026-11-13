package com.example.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.example.ads.AdsConsentState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun AnchoredAdaptiveBannerAd(
    consentState: AdsConsentState,
    modifier: Modifier = Modifier
) {
    val adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    if (!consentState.canRequestAds || adUnitId.isBlank()) {
        return
    }

    val context = LocalContext.current
    val hasInternet = remember(consentState.canRequestAds) { isNetworkAvailable(context) }
    if (!hasInternet) {
        return
    }

    var adLoaded by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }

    if (adFailed) {
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("anchored_banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        val adWidthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adSize = remember(adWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
        }
        val adView = remember(adUnitId, adSize) {
            AdView(context).apply {
                this.adUnitId = adUnitId
                setAdSize(adSize)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        adLoaded = true
                        adFailed = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        adLoaded = false
                        adFailed = true
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(adView) {
            onDispose {
                runCatching { adView.destroy() }
            }
        }

        AnimatedVisibility(
            visible = adLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                AndroidView(
                    factory = { adView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp)
                        .height(adSize.height.dp)
                )
            }
        }
    }
}

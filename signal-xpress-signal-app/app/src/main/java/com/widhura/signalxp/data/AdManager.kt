package com.signalxpress.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: AppConfigManager,
) {
    companion object {
        private const val TAG = "AdManager"
    }

    private var initialized = false
    private var _interstitialAd: InterstitialAd? = null
    private var _appOpenAd: AppOpenAd? = null
    var interstitialLoadCount = 0

    suspend fun initialize() {
        if (initialized) return
        val config = configManager.getConfig()
        if (config.ads.adStatus != "1") {
            Log.d(TAG, "Ads disabled by remote config")
            initialized = true
            return
        }
        try {
            MobileAds.initialize(context) { }
            initialized = true
            Log.d(TAG, "AdMob initialized")
        } catch (e: Exception) {
            Log.e(TAG, "AdMob init failed", e)
        }
    }

    fun isAdEnabled(config: AppConfigManager.ActiveConfig): Boolean =
        config.isLoaded && config.ads.adStatus == "1"

    fun isBannerEnabled(config: AppConfigManager.ActiveConfig): Boolean =
        isAdEnabled(config) && config.ads.bannerAdStatus == "1"

    fun isInterstitialEnabled(config: AppConfigManager.ActiveConfig): Boolean =
        isAdEnabled(config) && config.ads.interstitialAdStatus == "1"

    fun isNativeEnabled(config: AppConfigManager.ActiveConfig): Boolean =
        isAdEnabled(config) && config.ads.nativeAdStatus == "1"

    fun isAppOpenEnabled(config: AppConfigManager.ActiveConfig): Boolean =
        isAdEnabled(config) && config.ads.appOpenAdStatus == "1"

    fun getBannerUnitId(config: AppConfigManager.ActiveConfig): String? =
        if (isBannerEnabled(config)) config.ads.admobBannerUnitId.ifBlank { null } else null

    fun getNativeUnitId(config: AppConfigManager.ActiveConfig): String? =
        if (isNativeEnabled(config)) config.ads.admobNativeUnitId.ifBlank { null } else null

    fun getInterstitialUnitId(config: AppConfigManager.ActiveConfig): String? =
        if (isInterstitialEnabled(config)) config.ads.admobInterstitialUnitId.ifBlank { null } else null

    fun getAppOpenUnitId(config: AppConfigManager.ActiveConfig): String? =
        if (isAppOpenEnabled(config)) config.ads.admobAppOpenAdUnitId.ifBlank { null } else null

    fun loadInterstitial(config: AppConfigManager.ActiveConfig) {
        val adUnitId = getInterstitialUnitId(config) ?: return
        if (!initialized) return
        AdRequest.Builder().build().let { request ->
            InterstitialAd.load(context, adUnitId, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    _interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    _interstitialAd = null
                    Log.w(TAG, "Interstitial failed: ${error.message}")
                }
            })
        }
    }

    fun showInterstitial(activity: Activity, config: AppConfigManager.ActiveConfig) {
        if (!isInterstitialEnabled(config)) return
        if (interstitialLoadCount % config.ads.interstitialAdInterval != 0) {
            interstitialLoadCount++
            return
        }
        _interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    _interstitialAd = null
                    loadInterstitial(config)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    _interstitialAd = null
                }
            }
            ad.show(activity)
            interstitialLoadCount++
        }
    }

    fun loadAppOpenAd(config: AppConfigManager.ActiveConfig) {
        val adUnitId = getAppOpenUnitId(config) ?: return
        if (!initialized) return
        AdRequest.Builder().build().let { request ->
            AppOpenAd.load(context, adUnitId, request, object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    _appOpenAd = ad
                    Log.d(TAG, "App open ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    _appOpenAd = null
                    Log.w(TAG, "App open ad failed: ${error.message}")
                }
            })
        }
    }

    fun showAppOpenAd(activity: Activity) {
        _appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    _appOpenAd = null
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    _appOpenAd = null
                }
            }
            ad.show(activity)
        }
    }
}

@Composable
fun rememberAdConfig(configManager: AppConfigManager): AppConfigManager.ActiveConfig {
    val config by configManager.configFlow.collectAsState(initial = AppConfigManager.ActiveConfig())
    return config
}

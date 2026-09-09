package com.widhura.signalxp.data

import com.google.gson.annotations.SerializedName

data class RemoteAppConfig(
    @SerializedName("settings") val settings: List<ConfigSettings>? = null,
    @SerializedName("ads") val ads: List<ConfigAds>? = null,
    @SerializedName("socials") val socials: List<ConfigSocial>? = null,
)

data class ConfigSettings(
    @SerializedName("app_status") val appStatus: String = "1",
    @SerializedName("privacy_policy_url") val privacyPolicyUrl: String = "https://market.signalxpress.com/privacy",
    @SerializedName("terms_conditions_url") val termsUrl: String = "https://market.signalxpress.com/terms-conditions",
    @SerializedName("more_apps_url") val moreAppsUrl: String = "",
    @SerializedName("centrifugo_ws_url") val centrifugoWsUrl: String = "wss://socket.hadawatha.lk/connection/websocket",
    @SerializedName("laravel_api_url") val laravelApiUrl: String = "https://backend.signalxpress.com/api/",
    @SerializedName("x_api_key") val xApiKey: String = "Eky8ml0WwPG0Hp2swTtDgpugjtRX9NNa",
)

data class ConfigAds(
    @SerializedName("ad_status") val adStatus: String = "0",
    @SerializedName("ad_type") val adType: String = "admob",
    @SerializedName("backup_ads") val backupAds: String = "none",
    @SerializedName("banner_ad_status") val bannerAdStatus: String = "0",
    @SerializedName("interstitial_ad_status") val interstitialAdStatus: String = "0",
    @SerializedName("native_ad_status") val nativeAdStatus: String = "0",
    @SerializedName("app_open_ad_status") val appOpenAdStatus: String = "0",
    @SerializedName("admob_publisher_id") val admobPublisherId: String = "",
    @SerializedName("admob_banner_unit_id") val admobBannerUnitId: String = "",
    @SerializedName("admob_interstitial_unit_id") val admobInterstitialUnitId: String = "",
    @SerializedName("admob_native_unit_id") val admobNativeUnitId: String = "",
    @SerializedName("admob_app_open_ad_unit_id") val admobAppOpenAdUnitId: String = "",
    @SerializedName("interstitial_ad_interval") val interstitialAdInterval: Int = 2,
)

data class ConfigSocial(
    @SerializedName("social_name") val socialName: String = "",
    @SerializedName("social_icon") val socialIcon: String = "",
    @SerializedName("social_url") val socialUrl: String = "",
)

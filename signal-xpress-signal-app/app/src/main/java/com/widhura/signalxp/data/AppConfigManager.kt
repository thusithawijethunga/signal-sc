package com.widhura.signalxp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigManager @Inject constructor() {
    companion object {
        private const val TAG = "AppConfigManager"
    }

    data class ActiveConfig(
        val settings: ConfigSettings = ConfigSettings(),
        val ads: ConfigAds = ConfigAds(),
        val socials: List<ConfigSocial> = listOf(
            ConfigSocial(
                socialName = "Website",
                socialIcon = "https://market.signalxpress.com/website.png",
                socialUrl = "https://market.signalxpress.com",
            ),
        ),
        val isLoaded: Boolean = true,
    )

    private val hardcodedConfig = ActiveConfig()

    val configFlow: Flow<ActiveConfig> = flowOf(hardcodedConfig)

    suspend fun getConfig(): ActiveConfig = hardcodedConfig

    suspend fun getXApiKey(): String =
        hardcodedConfig.settings.xApiKey.ifBlank { "Eky8ml0WwPG0Hp2swTtDgpugjtRX9NNa" }

    suspend fun getLaravelApiUrl(): String =
        hardcodedConfig.settings.laravelApiUrl.ifBlank { "https://backend.signalxpress.com/api/" }

    suspend fun getCentrifugoWsUrl(): String =
        hardcodedConfig.settings.centrifugoWsUrl.ifBlank { "wss://socket.hadawatha.lk/connection/websocket" }

    suspend fun getPrivacyPolicyUrl(): String =
        hardcodedConfig.settings.privacyPolicyUrl.ifBlank { "https://market.signalxpress.com/privacy" }

    suspend fun getTermsUrl(): String =
        hardcodedConfig.settings.termsUrl.ifBlank { "https://market.signalxpress.com/terms-conditions" }
}

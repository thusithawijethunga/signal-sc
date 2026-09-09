package com.widhura.signalxp

import android.app.Application
import com.widhura.signalxp.data.AdManager
import com.widhura.signalxp.data.AppConfigManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RealtimeNotificationsApp : Application() {
    @Inject lateinit var configManager: AppConfigManager
    @Inject lateinit var adManager: AdManager
}

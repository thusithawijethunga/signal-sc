package com.widhura.signalxp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.widhura.signalxp.data.AdManager
import com.widhura.signalxp.data.AppConfigManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/** Process-wide UI visibility, no extra dependency (vs ProcessLifecycleOwner). */
object AppForeground {
    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    internal fun set(foreground: Boolean) {
        _isForeground.value = foreground
    }
}

@HiltAndroidApp
class RealtimeNotificationsApp : Application() {
    @Inject lateinit var configManager: AppConfigManager
    @Inject lateinit var adManager: AdManager

    private val startedActivities = AtomicInteger(0)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (startedActivities.incrementAndGet() == 1) {
                    AppForeground.set(true)
                }
            }

            override fun onActivityStopped(activity: Activity) {
                if (startedActivities.decrementAndGet() <= 0) {
                    startedActivities.set(0)
                    AppForeground.set(false)
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}

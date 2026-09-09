package com.widhura.signalxp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_settings")

class OnboardingPreferences(private val context: Context) {

    companion object {
        private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_completed")
    }

    val isOnboardingDone: Flow<Boolean> = context.onboardingDataStore.data.map { preferences ->
        preferences[ONBOARDING_DONE_KEY] ?: false
    }

    suspend fun setOnboardingDone() {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_DONE_KEY] = true
        }
    }
}

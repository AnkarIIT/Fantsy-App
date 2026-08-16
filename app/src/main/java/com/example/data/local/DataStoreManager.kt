package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.models.AppSettings
import com.example.data.models.KYCStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension for Context to get DataStore
private val Context.dataStore by preferencesDataStore("app_settings")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val KYC_STATUS = stringPreferencesKey("kyc_status")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val appSettingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            darkMode = preferences[PreferencesKeys.DARK_MODE] ?: true,
            language = preferences[PreferencesKeys.LANGUAGE] ?: "en"
        )
    }

    suspend fun updateAppSettings(newSettings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = newSettings.notificationsEnabled
            preferences[PreferencesKeys.SOUND_ENABLED] = newSettings.soundEnabled
            preferences[PreferencesKeys.VIBRATION_ENABLED] = newSettings.vibrationEnabled
            preferences[PreferencesKeys.DARK_MODE] = newSettings.darkMode
            preferences[PreferencesKeys.LANGUAGE] = newSettings.language
        }
    }

    val kycStatusFlow: Flow<KYCStatus?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KYC_STATUS]?.let { name ->
            KYCStatus.entries.firstOrNull { it.name == name }
        }
    }

    suspend fun saveKycStatus(status: KYCStatus) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KYC_STATUS] = status.name
        }
    }

    val userNameFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME]
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }
}

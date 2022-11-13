package com.example.inventory.encrypt

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

data class SettingsPreference(
    val defaultProviderName: String,
    val defaultProviderEmail: String,
    val defaultProviderPhoneNumber: String,
    val isSetDefaultValues: Boolean,
    val hideSensitiveData: Boolean,
    val disableShareData: Boolean
)

private const val DEFAULT_PROVIDER_NAME_KEY = "DEFAULT_PROVIDER_NAME_KEY"
private const val DEFAULT_PROVIDER_EMAIL_KEY = "DEFAULT_PROVIDER_EMAIL_KEY"
private const val DEFAULT_PROVIDER_PHONE_NUMBER_KEY = "DEFAULT_PROVIDER_PHONE_NUMBER_KEY"
private const val IS_SET_DEFAULT_VALUES_KEY = "IS_SET_DEFAULT_VALUES_KEY"
private const val HIDE_SENSITIVE_DATA_KEY = "HIDE_SENSITIVE_DATA_KEY"
private const val DISABLE_SHARE_DATA_KEY = "DISABLE_SHARE_DATA_KEY"

class EncryptedSettings {
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var currentSettings: SettingsPreference

    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPreferences = EncryptedSharedPreferences.create(
            "settingsPreferences",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        currentSettings = SettingsPreference(
            sharedPreferences.getString(DEFAULT_PROVIDER_NAME_KEY, "") ?: "",
            sharedPreferences.getString(DEFAULT_PROVIDER_EMAIL_KEY, "") ?: "",
            sharedPreferences.getString(DEFAULT_PROVIDER_PHONE_NUMBER_KEY, "") ?: "",
            sharedPreferences.getBoolean(IS_SET_DEFAULT_VALUES_KEY, false),
            sharedPreferences.getBoolean(HIDE_SENSITIVE_DATA_KEY, false),
            sharedPreferences.getBoolean(DISABLE_SHARE_DATA_KEY, false)
        )
    }

    fun setSettings(settings: SettingsPreference) {
        sharedPreferences.edit().putString(DEFAULT_PROVIDER_NAME_KEY, settings.defaultProviderName).apply()
        sharedPreferences.edit().putString(DEFAULT_PROVIDER_EMAIL_KEY, settings.defaultProviderEmail).apply()
        sharedPreferences.edit().putString(DEFAULT_PROVIDER_PHONE_NUMBER_KEY, settings.defaultProviderPhoneNumber).apply()
        sharedPreferences.edit().putBoolean(IS_SET_DEFAULT_VALUES_KEY, settings.isSetDefaultValues).apply()
        sharedPreferences.edit().putBoolean(HIDE_SENSITIVE_DATA_KEY, settings.hideSensitiveData).apply()
        sharedPreferences.edit().putBoolean(DISABLE_SHARE_DATA_KEY, settings.disableShareData).apply()

        currentSettings = settings
    }

    fun getSettings(): SettingsPreference {
        return currentSettings
    }
}
package com.pocketremodel.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pocketremodel.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Remembers the user's settings between launches — most importantly the
 * OpenRouter API key they paste in during setup. Because it's stored on the
 * device (not baked into the app), the user pastes it ONCE and never rebuilds.
 */
private val Context.settings by preferencesDataStore(name = "pocket_remodel_settings")

class SettingsStore(private val context: Context) {

    private val keyApi = stringPreferencesKey("openrouter_api_key")
    private val keyVerified = booleanPreferencesKey("key_verified")

    /** Live stream of the saved key (falls back to a build-time key if one was set). */
    val apiKeyFlow: Flow<String> = context.settings.data.map { prefs ->
        prefs[keyApi]?.takeIf { it.isNotBlank() } ?: BuildConfig.OPENROUTER_API_KEY
    }

    val verifiedFlow: Flow<Boolean> = context.settings.data.map { it[keyVerified] ?: false }

    suspend fun apiKey(): String = apiKeyFlow.first()

    suspend fun isConfigured(): Boolean = apiKey().isNotBlank()

    suspend fun saveApiKey(key: String, verified: Boolean) {
        context.settings.edit {
            it[keyApi] = key.trim()
            it[keyVerified] = verified
        }
    }
}

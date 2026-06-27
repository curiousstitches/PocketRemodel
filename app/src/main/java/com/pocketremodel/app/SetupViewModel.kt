package com.pocketremodel.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketremodel.app.ai.OpenRouterClient
import com.pocketremodel.app.data.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the one-time setup screen: paste key -> Test -> green/red -> continue.
 * The key is saved on the device, so this only happens the first time.
 */
class SetupViewModel(app: Application) : AndroidViewModel(app) {

    enum class TestState { IDLE, TESTING, CONNECTED, FAILED }

    data class UiState(
        val keyText: String = "",
        val testState: TestState = TestState.IDLE,
        val message: String = "",
        val canContinue: Boolean = false
    )

    private val settings = SettingsStore(app)
    private val ai = OpenRouterClient()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Where the user goes to make a free key. */
    val keyPageUrl = "https://openrouter.ai/keys"

    init {
        // Pre-fill if a key was already saved (e.g. returning user or build-time key).
        viewModelScope.launch {
            val existing = settings.apiKey()
            if (existing.isNotBlank()) _state.value = _state.value.copy(keyText = existing)
        }
    }

    fun onKeyChanged(text: String) {
        _state.value = _state.value.copy(
            keyText = text,
            testState = TestState.IDLE,
            message = "",
            canContinue = false
        )
    }

    fun test() {
        val key = _state.value.keyText.trim()
        _state.value = _state.value.copy(testState = TestState.TESTING, message = "Checking…")
        viewModelScope.launch {
            ai.testConnection(key).fold(
                onSuccess = { msg ->
                    settings.saveApiKey(key, verified = true)
                    ai.apiKey = key
                    _state.value = _state.value.copy(
                        testState = TestState.CONNECTED, message = msg, canContinue = true
                    )
                },
                onFailure = { err ->
                    _state.value = _state.value.copy(
                        testState = TestState.FAILED,
                        message = err.message ?: "Couldn't connect.",
                        canContinue = false
                    )
                }
            )
        }
    }
}

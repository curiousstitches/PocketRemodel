package com.pocketremodel.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketremodel.app.ai.ChatMessage
import com.pocketremodel.app.ai.CommandParser
import com.pocketremodel.app.ai.OpenRouterClient
import com.pocketremodel.app.ai.SystemPrompt
import com.pocketremodel.app.data.ModelCatalog
import com.pocketremodel.app.data.SettingsStore
import com.pocketremodel.app.domain.RemodelCommand
import com.pocketremodel.app.voice.VoiceManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The conductor. It owns the conversation loop:
 *
 *   mic -> text  (VoiceManager)
 *        -> AI   (OpenRouterClient + guardrail SystemPrompt)
 *        -> commands (CommandParser)
 *        -> scene (emitted to the AR screen, which runs SceneController)
 *        -> spoken reply (VoiceManager TTS)
 *
 * The AR engine lives in the Composable (it needs the Filament engine), so this
 * VM publishes commands through [commands] and the screen collects them.
 */
class ArViewModel(app: Application) : AndroidViewModel(app) {

    enum class Status { IDLE, LISTENING, THINKING, SPEAKING }

    data class UiState(
        val status: Status = Status.IDLE,
        val transcript: String = "",
        val reply: String = "",
        val hint: String = "Hold the mic and tell your room what to change.",
        val objectCount: Int = 0
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Stream of scene commands for the AR screen to execute. */
    val commands = MutableSharedFlow<RemodelCommand>(extraBufferCapacity = 16)

    private val voice = VoiceManager(app)
    private val ai = OpenRouterClient()
    private val settings = SettingsStore(app)
    private val history = mutableListOf<ChatMessage>()
    private val placedCatalogIds = mutableListOf<String>()

    init {
        // Load the key the user saved during setup so the brain is ready.
        viewModelScope.launch { ai.apiKey = settings.apiKey() }
        voice.init()
        voice.onListeningChanged = { listening ->
            _state.value = _state.value.copy(
                status = if (listening) Status.LISTENING else _state.value.status
            )
        }
        voice.onResult = { spoken -> handleUserText(spoken) }
        voice.onError = { msg -> _state.value = _state.value.copy(status = Status.IDLE, hint = msg) }
    }

    fun startListening() {
        _state.value = _state.value.copy(status = Status.LISTENING, transcript = "")
        voice.startListening()
    }

    fun stopListening() = voice.stopListening()

    private fun handleUserText(text: String) {
        _state.value = _state.value.copy(status = Status.THINKING, transcript = text)
        viewModelScope.launch {
            val systemPrompt = SystemPrompt.build(currentObjectsSummary())
            val raw = ai.complete(systemPrompt, history, text)

            if (raw == null) {
                val msg = "I couldn't reach the design brain — check the internet or your key."
                voice.speak(msg)
                _state.value = _state.value.copy(status = Status.IDLE, reply = msg)
                return@launch
            }

            history += ChatMessage("user", text)
            history += ChatMessage("assistant", raw)

            val parsed = CommandParser.parse(raw)
            parsed.commands.forEach { cmd ->
                trackForPrompt(cmd)
                commands.tryEmit(cmd)
            }

            _state.value = _state.value.copy(
                status = Status.SPEAKING,
                reply = parsed.spokenReply,
                objectCount = placedCatalogIds.size
            )
            voice.speak(parsed.spokenReply)
            _state.value = _state.value.copy(status = Status.IDLE)
        }
    }

    /** Keep a lightweight mirror so the AI knows what's already in the room. */
    private fun trackForPrompt(cmd: RemodelCommand) {
        when (cmd) {
            is RemodelCommand.AddModel -> placedCatalogIds += cmd.catalogId
            is RemodelCommand.ClearAll -> placedCatalogIds.clear()
            is RemodelCommand.RemoveModel -> placedCatalogIds.removeLastOrNull()
            else -> {}
        }
    }

    private fun currentObjectsSummary(): String =
        if (placedCatalogIds.isEmpty()) "  (room is empty)"
        else placedCatalogIds.joinToString("\n") { id ->
            "  - $id (${ModelCatalog.find(id)?.displayName ?: id})"
        }

    /** Tap a furniture item in the picker to drop it in front of you. */
    fun placeFromCatalog(catalogId: String) {
        val cmd = RemodelCommand.AddModel(catalogId)
        trackForPrompt(cmd)
        commands.tryEmit(cmd)
        _state.value = _state.value.copy(
            reply = "Added ${ModelCatalog.find(catalogId)?.displayName ?: "it"}.",
            objectCount = placedCatalogIds.size
        )
    }

    /**
     * Photo reference: hand a base64 image to the vision model, get back the
     * closest catalog item, and place it. [dataUrl] is "data:image/jpeg;base64,…".
     */
    fun matchPhotoAndPlace(dataUrl: String) {
        _state.value = _state.value.copy(status = Status.THINKING, hint = "Matching your photo…")
        viewModelScope.launch {
            val replyId = ai.visionMatchCatalog(dataUrl, ModelCatalog.promptManifest())
            val match = replyId?.let { raw ->
                // The model may add stray words — find the first known id inside its reply.
                ModelCatalog.items.firstOrNull { raw.contains(it.id, ignoreCase = true) }
            }
            if (match != null) {
                placeFromCatalog(match.id)
                val msg = "Closest match: ${match.displayName}."
                _state.value = _state.value.copy(status = Status.IDLE, reply = msg, hint = msg)
            } else {
                val msg = "I couldn't match that photo — try the furniture list instead."
                _state.value = _state.value.copy(status = Status.IDLE, hint = msg)
            }
        }
    }

    fun saveDesignRequested() {
        _state.value = _state.value.copy(hint = "Saved! Reopen anytime and scan to restore.")
    }

    override fun onCleared() {
        voice.release()
        super.onCleared()
    }
}

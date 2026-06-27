package com.pocketremodel.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * The app's ears and mouth — both 100% free and on-device:
 *   - SpeechRecognizer  : converts the user's spoken words into text.
 *   - TextToSpeech (TTS): reads the assistant's reply back aloud.
 *
 * No cloud speech service, no API cost. Works offline on most modern phones.
 */
class VoiceManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    var onResult: (String) -> Unit = {}
    var onListeningChanged: (Boolean) -> Unit = {}
    var onError: (String) -> Unit = {}

    fun init() {
        // ---- Text to speech ----
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setSpeechRate(1.05f)
                ttsReady = true
            } else {
                Log.w(TAG, "TTS init failed")
            }
        }

        // ---- Speech to text ----
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
        } else {
            Log.w(TAG, "On-device speech recognition not available.")
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        onListeningChanged(true)
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        onListeningChanged(false)
    }

    fun speak(text: String) {
        if (ttsReady && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "remi-reply")
        }
    }

    fun release() {
        recognizer?.destroy()
        tts?.shutdown()
    }

    private val listener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            onListeningChanged(false)
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) onResult(text)
        }

        override fun onError(error: Int) {
            onListeningChanged(false)
            onError(errorText(error))
        }

        override fun onEndOfSpeech() = onListeningChanged(false)
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun errorText(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that — try again."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything."
        SpeechRecognizer.ERROR_NETWORK -> "Network hiccup during recognition."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed."
        else -> "Speech error ($code)."
    }

    companion object { private const val TAG = "VoiceManager" }
}

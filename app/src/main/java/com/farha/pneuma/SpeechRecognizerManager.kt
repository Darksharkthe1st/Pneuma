package com.farha.pneuma

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognizerManager(
    private val context: Context,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val speechRecognizer: SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }

    init {
        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningStateChanged(true)
                }

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    onListeningStateChanged(false)
                }

                override fun onError(error: Int) {
                    onListeningStateChanged(false)
                    onError(errorMessageFor(error))
                }

                override fun onResults(results: Bundle?) {
                    onListeningStateChanged(false)
                    val matches = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        .orEmpty()
                    onFinalResult(matches.firstOrNull().orEmpty())
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        .orEmpty()
                    val text = matches.firstOrNull().orEmpty()
                    if (text.isNotBlank()) {
                        onPartialResult(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            }
        )
    }

    fun startListening() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            onError("Speech recognition is not available on this device.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        recognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
    }

    private fun errorMessageFor(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Try again."
            SpeechRecognizer.ERROR_CLIENT -> "Speech input stopped."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is missing."
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Speech recognition needs a better network connection."
            SpeechRecognizer.ERROR_NO_MATCH ->
                "I did not catch that. Try again or type the answer."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Speech recognizer is busy. Wait a second and retry."
            SpeechRecognizer.ERROR_SERVER ->
                "Speech service error. Use typed fallback if needed."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "No speech detected. Try again or type the answer."
            else -> "Speech recognition failed. Use typed fallback if needed."
        }
    }
}

package com.farha.pneuma

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class QuestionSpeaker(
    context: Context,
    private val onSpeechStart: () -> Unit,
    private val onSpeechDone: () -> Unit,
    private val onSpeechError: (String) -> Unit,
) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var pendingText: String? = null
    private var isReady = false

    private val textToSpeech = TextToSpeech(appContext, this).apply {
        setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStart()
                }

                override fun onDone(utteranceId: String?) {
                    onSpeechDone()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeechDone()
                    onSpeechError("Voice playback failed. The question text is still visible on screen.")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onSpeechDone()
                    onSpeechError("Voice playback failed. The question text is still visible on screen.")
                }
            }
        )
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            onSpeechError("Text-to-speech is unavailable on this device.")
            return
        }

        val result = textToSpeech.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            onSpeechError("The device does not have a supported English TTS voice.")
            return
        }

        isReady = true
        pendingText?.let {
            pendingText = null
            speak(it)
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return

        if (!isReady) {
            pendingText = text
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle()
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun destroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

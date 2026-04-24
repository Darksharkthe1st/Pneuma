package com.farha.pneuma

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class IntakeUiState(
    val turns: List<TranscriptTurn> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val playbackTick: Int = 0,
    val draftAnswer: String = "",
    val voiceError: String = "",
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val photoCaptured: Boolean = false,
    val visualSymptom: String = "",
)

private val scriptedIntakeQuestions = listOf(
    "Hi, where does it hurt today?",
    "On a scale of 0 to 10, how would you rate the pain?",
    "When did this begin?",
    "Any trouble breathing, nausea, or sweating?"
)

private val scriptedIntakeAnswers = listOf(
    "It hurts in the center of my chest and goes to my left arm.",
    "It is about an 8.",
    "About two hours ago.",
    "I feel sweaty and a little short of breath."
)

private const val mockVisionSummary =
    "Visible mild facial pallor with no obvious swelling, rash, or external bleeding."

class IntakeViewModel : ViewModel() {
    var uiState by mutableStateOf(IntakeUiState())
        private set

    val currentQuestion: String?
        get() = scriptedIntakeQuestions.getOrNull(uiState.currentQuestionIndex)

    val isComplete: Boolean
        get() = currentQuestion == null

    fun onDraftAnswerChange(answer: String) {
        uiState = uiState.copy(
            draftAnswer = answer,
            voiceError = ""
        )
    }

    fun startListening() {
        if (!uiState.isSpeaking && !isComplete) {
            uiState = uiState.copy(
                isListening = true,
                voiceError = ""
            )
        }
    }

    fun stopListening() {
        uiState = uiState.copy(isListening = false)
    }

    fun replayQuestion() {
        if (!isComplete) {
            uiState = uiState.copy(
                playbackTick = uiState.playbackTick + 1,
                isSpeaking = true,
                isListening = false,
                voiceError = ""
            )
        }
    }

    fun submitTypedAnswer() {
        val answer = uiState.draftAnswer.trim()
        if (answer.isEmpty() || isComplete) return

        appendAnswer(answer)
    }

    fun useDemoAnswer() {
        if (isComplete) return

        val answer = scriptedIntakeAnswers.getOrElse(uiState.currentQuestionIndex) {
            "The pain has been getting worse."
        }
        appendAnswer(answer)
    }

    fun captureMockPhoto() {
        uiState = uiState.copy(photoCaptured = true)
    }

    fun generateMockVisionSummary() {
        uiState = uiState.copy(visualSymptom = mockVisionSummary)
    }

    fun beginQuestionPlayback() {
        if (!isComplete) {
            uiState = uiState.copy(
                isSpeaking = true,
                isListening = false,
                voiceError = ""
            )
        }
    }

    fun finishQuestionPlayback() {
        if (!isComplete) {
            uiState = uiState.copy(isSpeaking = false)
        }
    }

    fun onSpeechPartialResult(text: String) {
        if (text.isNotBlank()) {
            uiState = uiState.copy(
                draftAnswer = text,
                voiceError = ""
            )
        }
    }

    fun onSpeechFinalResult(text: String) {
        val answer = text.trim()
        if (answer.isBlank() || isComplete) {
            uiState = uiState.copy(
                isListening = false,
                voiceError = "I did not catch that. Try again or type the answer."
            )
            return
        }

        uiState = uiState.copy(
            draftAnswer = answer,
            voiceError = ""
        )
        appendAnswer(answer)
    }

    fun onSpeechError(message: String) {
        uiState = uiState.copy(
            isListening = false,
            voiceError = message
        )
    }

    private fun appendAnswer(answer: String) {
        val question = currentQuestion ?: return

        uiState = uiState.copy(
            turns = uiState.turns + TranscriptTurn(
                question = question,
                answer = answer
            ),
            currentQuestionIndex = uiState.currentQuestionIndex + 1,
            draftAnswer = "",
            isListening = false,
            voiceError = ""
        )
    }
}

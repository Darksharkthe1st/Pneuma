package com.farha.pneuma

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farha.pneuma.ui.theme.AccentBlue
import com.farha.pneuma.ui.theme.AccentGreen
import com.farha.pneuma.ui.theme.CardSurface
import com.farha.pneuma.ui.theme.DangerRed
import com.farha.pneuma.ui.theme.DeepBackground
import com.farha.pneuma.ui.theme.PneumaTheme
import com.farha.pneuma.ui.theme.SoftAmber
import com.farha.pneuma.ui.theme.SubtleStroke

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PneumaTheme {
                TriageAidApp()
            }
        }
    }
}

private enum class DemoScreen(val step: Int, val title: String, val subtitle: String) {
    Vitals(1, "Vitals Scan", "Presage"),
    Intake(2, "AI Intake Nurse", "Voice + Vision"),
    Processing(3, "Processing", "Agent Thinking"),
    Soap(4, "Doctor SOAP Note", "Summary Card"),
}

private data class PatientInfo(
    val age: String,
    val sex: String,
)

data class TranscriptTurn(
    val question: String,
    val answer: String,
)

private data class AgentStep(
    val label: String,
    val detail: String,
    val complete: Boolean,
)

private data class Assessment(
    val urgency: String,
    val flags: List<String>,
)

private data class SoapNote(
    val subjective: String,
    val objective: String,
    val assessment: Assessment,
    val plan: List<String>,
)

private data class DemoState(
    val heartRate: Int,
    val patientInfo: PatientInfo,
    val transcript: List<TranscriptTurn>,
    val visualSymptom: String,
    val agentSteps: List<AgentStep>,
    val soapNote: SoapNote,
)

private fun demoState(): DemoState {
    return DemoState(
        heartRate = 112,
        patientInfo = PatientInfo(age = "58", sex = "Male"),
        transcript = listOf(
            TranscriptTurn(
                question = "Hi, where does it hurt today?",
                answer = "It hurts in the center of my chest and goes to my left arm."
            ),
            TranscriptTurn(
                question = "On a scale of 0 to 10, how would you rate the pain?",
                answer = "It's about an 8."
            ),
            TranscriptTurn(
                question = "When did this begin?",
                answer = "About two hours ago."
            ),
            TranscriptTurn(
                question = "Any trouble breathing, nausea, or sweating?",
                answer = "I feel sweaty and a little short of breath."
            ),
        ),
        visualSymptom = "Mild facial pallor with no visible rash, bleeding, or swelling.",
        agentSteps = listOf(
            AgentStep("Vitals captured", "Heart rate 112 bpm", complete = true),
            AgentStep("Voice intake complete", "4 adaptive questions answered", complete = true),
            AgentStep("Visual symptoms analyzed", "Gemini returned symptom summary", complete = true),
            AgentStep("Generating SOAP note", "Claude building final clinical brief", complete = false),
        ),
        soapNote = SoapNote(
            subjective = "Chest pain radiating to the left arm for 2 hours, pain 8/10, with sweating and mild shortness of breath.",
            objective = "Heart rate 112 bpm. Age 58. Sex male. Visual review shows mild facial pallor with no visible swelling or rash.",
            assessment = Assessment(
                urgency = "High Risk",
                flags = listOf("Possible cardiac event", "Chest pain", "Tachycardia")
            ),
            plan = listOf(
                "Obtain 12-lead ECG",
                "Check troponin levels",
                "Monitor vitals and pain",
                "Consider aspirin if not contraindicated",
                "Refer for urgent clinician evaluation",
            )
        )
    )
}

@Composable
private fun TriageAidApp() {
    val state = remember { demoState() }
    val intakeViewModel: IntakeViewModel = viewModel()
    val intakeState = intakeViewModel.uiState
    var currentScreen by remember { mutableStateOf(DemoScreen.Vitals) }
    var age by remember { mutableStateOf(state.patientInfo.age) }
    var sex by remember { mutableStateOf(state.patientInfo.sex) }
    val currentQuestion = intakeViewModel.currentQuestion

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepBackground,
                            Color(0xFF08202B),
                            Color(0xFF041118),
                        )
                    )
                )
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                AppHeader()
                Spacer(modifier = Modifier.height(18.dp))
                StepTracker(
                    currentScreen = currentScreen,
                    onSelect = { currentScreen = it }
                )
                Spacer(modifier = Modifier.height(18.dp))
                AnimatedContent(
                    targetState = currentScreen,
                    label = "screen_content"
                ) { screen ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (screen) {
                            DemoScreen.Vitals -> VitalsScreen(
                                heartRate = state.heartRate,
                                age = age,
                                sex = sex,
                                onAgeChange = { age = it },
                                onSexChange = { sex = it }
                            )

                            DemoScreen.Intake -> IntakeScreen(
                                transcript = intakeState.turns,
                                currentQuestion = currentQuestion,
                                draftAnswer = intakeState.draftAnswer,
                                voiceError = intakeState.voiceError,
                                isListening = intakeState.isListening,
                                isSpeaking = intakeState.isSpeaking,
                                photoCaptured = intakeState.photoCaptured,
                                visualSymptom = intakeState.visualSymptom,
                                onDraftAnswerChange = intakeViewModel::onDraftAnswerChange,
                                onStartListening = intakeViewModel::startListening,
                                onStopListening = intakeViewModel::stopListening,
                                onBeginQuestionPlayback = intakeViewModel::beginQuestionPlayback,
                                onFinishQuestionPlayback = intakeViewModel::finishQuestionPlayback,
                                onSpeechPartialResult = intakeViewModel::onSpeechPartialResult,
                                onSpeechFinalResult = intakeViewModel::onSpeechFinalResult,
                                onSpeechError = intakeViewModel::onSpeechError,
                                onReplayQuestion = intakeViewModel::replayQuestion,
                                onSubmitAnswer = intakeViewModel::submitTypedAnswer,
                                onUseDemoAnswer = intakeViewModel::useDemoAnswer,
                                onCapturePhoto = intakeViewModel::captureMockPhoto,
                                onGenerateVisionSummary = intakeViewModel::generateMockVisionSummary
                            )

                            DemoScreen.Processing -> ProcessingScreen(
                                steps = state.agentSteps
                            )

                            DemoScreen.Soap -> SoapScreen(
                                soapNote = state.soapNote
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                BottomNavigation(
                    currentScreen = currentScreen,
                    onBack = {
                        currentScreen = DemoScreen.entries[currentScreen.ordinal - 1]
                    },
                    onNext = {
                        currentScreen = DemoScreen.entries[currentScreen.ordinal + 1]
                    }
                )
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column {
        Text(
            text = "TriageAid v2",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Hackathon-ready triage flow scaffold for Presage, ElevenLabs, Gemini, LangChain, and Claude.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9FC1C8)
        )
    }
}

@Composable
private fun StepTracker(
    currentScreen: DemoScreen,
    onSelect: (DemoScreen) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoScreen.entries.forEach { screen ->
            val selected = screen == currentScreen
            Card(
                modifier = Modifier
                    .widthIn(min = 150.dp)
                    .clickable { onSelect(screen) },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color(0xFF12363E) else Color(0xFF0A232E)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) AccentGreen else SubtleStroke
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (selected) AccentGreen else Color(0xFF153844)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = screen.step.toString(),
                            color = if (selected) DeepBackground else Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Column {
                        Text(
                            text = screen.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = screen.subtitle,
                            color = Color(0xFF86ACB4),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalsScreen(
    heartRate: Int,
    age: String,
    sex: String,
    onAgeChange: (String) -> Unit,
    onSexChange: (String) -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "scan")
    val scanOffset by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    ScreenCard(
        eyebrow = "Screen 1",
        title = "Capture vitals before intake begins",
        supporting = "Use this as the Presage handoff surface. The UI already reserves space for heart rate, demographics, and scan feedback."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = "Position face within the frame",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF95B7BF)
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(280.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF3F7F7))
            ) {
                val height = maxHeight
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(198.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE1E8EA), Color(0xFFB3C3C7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Face",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF294650)
                    )
                }
                CornerFrame(modifier = Modifier.align(Alignment.Center))
                HorizontalDivider(
                    modifier = Modifier
                        .padding(top = height * scanOffset)
                        .fillMaxWidth(),
                    color = AccentGreen
                )
                Text(
                    text = "Scanning... please hold still",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF466671)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricBadge(
                    modifier = Modifier.weight(1f),
                    label = "Heart Rate",
                    value = "$heartRate",
                    suffix = "bpm",
                    accent = DangerRed
                )
                MetricBadge(
                    modifier = Modifier.weight(1f),
                    label = "Signal",
                    value = "Good",
                    suffix = "Presage ready",
                    accent = AccentGreen
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Age") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sex",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB5D0D6)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female").forEach { option ->
                            FilterChip(
                                selected = sex == option,
                                onClick = { onSexChange(option) },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentGreen,
                                    selectedLabelColor = DeepBackground
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntakeScreen(
    transcript: List<TranscriptTurn>,
    currentQuestion: String?,
    draftAnswer: String,
    voiceError: String,
    isListening: Boolean,
    isSpeaking: Boolean,
    photoCaptured: Boolean,
    visualSymptom: String,
    onDraftAnswerChange: (String) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onBeginQuestionPlayback: () -> Unit,
    onFinishQuestionPlayback: () -> Unit,
    onSpeechPartialResult: (String) -> Unit,
    onSpeechFinalResult: (String) -> Unit,
    onSpeechError: (String) -> Unit,
    onReplayQuestion: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onUseDemoAnswer: () -> Unit,
    onCapturePhoto: () -> Unit,
    onGenerateVisionSummary: () -> Unit,
) {
    val context = LocalContext.current
    val latestStartListening by rememberUpdatedState(onStartListening)
    val latestStopListening by rememberUpdatedState(onStopListening)
    val latestBeginQuestionPlayback by rememberUpdatedState(onBeginQuestionPlayback)
    val latestFinishQuestionPlayback by rememberUpdatedState(onFinishQuestionPlayback)
    val latestPartialResult by rememberUpdatedState(onSpeechPartialResult)
    val latestFinalResult by rememberUpdatedState(onSpeechFinalResult)
    val latestSpeechError by rememberUpdatedState(onSpeechError)
    val speechRecognizerManager = remember(context) {
        SpeechRecognizerManager(
            context = context,
            onListeningStateChanged = { listening ->
                if (listening) latestStartListening() else latestStopListening()
            },
            onPartialResult = { latestPartialResult(it) },
            onFinalResult = { latestFinalResult(it) },
            onError = { latestSpeechError(it) }
        )
    }
    val questionSpeaker = remember(context) {
        QuestionSpeaker(
            context = context,
            onSpeechStart = { latestBeginQuestionPlayback() },
            onSpeechDone = { latestFinishQuestionPlayback() },
            onSpeechError = { latestSpeechError(it) }
        )
    }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    DisposableEffect(speechRecognizerManager) {
        onDispose {
            speechRecognizerManager.destroy()
        }
    }
    DisposableEffect(questionSpeaker) {
        onDispose {
            questionSpeaker.destroy()
        }
    }
    val interviewComplete = currentQuestion == null

    LaunchedEffect(currentQuestion, interviewComplete) {
        if (!interviewComplete) {
            questionSpeaker.speak(currentQuestion.orEmpty())
        }
    }

    val statusText = when {
        !hasMicPermission -> "Mic permission needed"
        !hasCameraPermission && interviewComplete -> "Camera permission needed"
        isSpeaking -> "Speaking"
        isListening -> "Listening"
        interviewComplete -> "Questions complete"
        else -> "Awaiting answer"
    }

    ScreenCard(
        eyebrow = "Screen 2",
        title = "AI nurse intake with voice and symptom vision",
        supporting = "Questions now speak out loud with native Android voice, while typed fallback and mock photo flow keep the demo dependable."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusPill("Phase 5 voice playback enabled", AccentGreen)
            if (!hasMicPermission || !hasCameraPermission) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102C36)),
                    border = BorderStroke(1.dp, SoftAmber.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Permissions needed for the real intake flow",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = buildString {
                                if (!hasMicPermission) {
                                    append("Microphone access is required for voice intake. ")
                                }
                                if (!hasCameraPermission) {
                                    append("Camera access is required for symptom photos.")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB8D1D7)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!hasMicPermission) {
                                Button(
                                    onClick = {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftAmber)
                                ) {
                                    Text("Grant Mic", color = DeepBackground)
                                }
                            }
                            if (!hasCameraPermission) {
                                OutlinedButton(
                                    onClick = {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Grant Camera")
                                }
                            }
                        }
                    }
                }
            }
            transcript.forEach { turn ->
                ChatBubble(
                    speaker = "AI Nurse",
                    message = turn.question,
                    alignEnd = false
                )
                ChatBubble(
                    speaker = "Patient",
                    message = turn.answer,
                    alignEnd = true
                )
            }
            if (!interviewComplete) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFB))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Current question",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF4D6B74)
                        )
                        Text(
                            text = currentQuestion.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF16313A)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onReplayQuestion()
                                    questionSpeaker.speak(currentQuestion.orEmpty())
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Replay")
                            }
                            Button(
                                onClick = {
                                    if (!hasMicPermission) {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else if (isListening) {
                                        speechRecognizerManager.stopListening()
                                    } else {
                                        onStartListening()
                                        speechRecognizerManager.startListening()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isListening) DangerRed else Color(0xFF0F4A55)
                                )
                            ) {
                                Text(
                                    if (!hasMicPermission) "Grant Mic"
                                    else if (isListening) "Stop Mic"
                                    else "Start Mic"
                                )
                            }
                        }
                        OutlinedTextField(
                            value = draftAnswer,
                            onValueChange = onDraftAnswerChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Type fallback answer") },
                            placeholder = { Text("Chest pain in the middle of my chest...") },
                            shape = RoundedCornerShape(18.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onUseDemoAnswer,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Use Demo Answer")
                            }
                            Button(
                                onClick = onSubmitAnswer,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                enabled = draftAnswer.isNotBlank()
                            ) {
                                Text("Submit")
                            }
                        }
                    }
                }
            }
            if (voiceError.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.18f)),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = voiceError,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8F9))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Show us the affected area",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF16313A)
                    )
                    Text(
                        text = "Capture a symptom photo for Gemini Vision analysis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF53727D)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (hasCameraPermission) {
                                    onCapturePhoto()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F4A55)
                            )
                        ) {
                            Text(
                                if (!hasCameraPermission) "Grant Camera"
                                else if (photoCaptured) "Retake Mock Photo"
                                else "Capture Mock Photo"
                            )
                        }
                        OutlinedButton(
                            onClick = onGenerateVisionSummary,
                            enabled = photoCaptured && hasCameraPermission,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Analyze")
                        }
                    }
                    Text(
                        text = if (photoCaptured) {
                            "Mock symptom photo captured and ready for Gemini."
                        } else {
                            "No symptom photo captured yet."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF27434D)
                    )
                    Text(
                        text = if (visualSymptom.isBlank()) {
                            "Visual summary: waiting for analysis."
                        } else {
                            "Visual summary: $visualSymptom"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF27434D)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(DangerRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mic",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        text = if (interviewComplete) {
                            if (!hasCameraPermission) {
                                "Voice questions are done. Grant camera access next so the symptom-photo step can work."
                            } else {
                                "Voice questions are done. Capture the symptom photo and generate the mock vision summary."
                            }
                        } else {
                            if (!hasMicPermission) {
                                "Grant mic access now so the real speech input phase has a clean path. Typed answers still work today."
                            } else if (isListening) {
                                "Speak your answer now. Partial speech will appear in the text field, and the final result auto-submits."
                            } else if (isSpeaking) {
                                "The nurse is reading the next question aloud using Android text-to-speech."
                            } else {
                                "Use the mic for live Android speech input, or fall back to typed answers and the demo-answer button."
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8FB0B9)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingScreen(
    steps: List<AgentStep>,
) {
    val progress = steps.count { it.complete }.toFloat() / steps.size.toFloat()

    ScreenCard(
        eyebrow = "Screen 3",
        title = "Keep the room engaged while agents work",
        supporting = "Processing does not have to feel empty. This screen shows live progress and gives your shell/integration teammate a safe fallback story."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ProgressRing(progress = progress)
            Text(
                text = "Preparing your clinical brief...",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Our AI team is analyzing the data and assembling the SOAP note.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8DB0B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEachIndexed { index, step ->
                    StepRow(
                        step = step,
                        isActive = !step.complete && index == steps.indexOfFirst { !it.complete }
                    )
                }
            }
            Text(
                text = "Your data is secure and confidential",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7698A0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SoapScreen(
    soapNote: SoapNote,
) {
    ScreenCard(
        eyebrow = "Screen 4",
        title = "Final doctor handoff card",
        supporting = "Everything here is structured for a fast clinical readout and an impressive end-state for your judges."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            UrgencyBanner(level = soapNote.assessment.urgency)
            SoapSection(
                label = "Subjective",
                accent = AccentBlue,
                content = soapNote.subjective
            )
            SoapSection(
                label = "Objective",
                accent = AccentGreen,
                content = soapNote.objective
            )
            SoapSection(
                label = "Assessment",
                accent = SoftAmber,
                content = soapNote.assessment.flags.joinToString(separator = " | ")
            )
            SoapPlanSection(plan = soapNote.plan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Print")
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Share")
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("Hand-off", color = DeepBackground)
                }
            }
        }
    }
}

@Composable
private fun ScreenCard(
    eyebrow: String,
    title: String,
    supporting: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, SubtleStroke)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGreen
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF91B2BB)
                )
            }
            content()
        }
    }
}

@Composable
private fun MetricBadge(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    suffix: String,
    accent: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F7F8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (label == "Heart Rate") "HR" else "OK",
                    color = accent,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF637E86)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF162F36)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF637E86)
                    )
                }
            }
        }
    }
}

@Composable
private fun CornerFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(230.dp)) {
        listOf(
            Alignment.TopStart,
            Alignment.TopEnd,
            Alignment.BottomStart,
            Alignment.BottomEnd
        ).forEach { alignment ->
            Box(
                modifier = Modifier
                    .align(alignment)
                    .size(32.dp)
                    .border(
                        width = 2.dp,
                        color = AccentGreen,
                        shape = RoundedCornerShape(
                            topStart = if (alignment == Alignment.TopStart) 10.dp else 0.dp,
                            topEnd = if (alignment == Alignment.TopEnd) 10.dp else 0.dp,
                            bottomStart = if (alignment == Alignment.BottomStart) 10.dp else 0.dp,
                            bottomEnd = if (alignment == Alignment.BottomEnd) 10.dp else 0.dp,
                        )
                    )
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = accent
        )
    }
}

@Composable
private fun ChatBubble(
    speaker: String,
    message: String,
    alignEnd: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = speaker,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF84A7AF)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (alignEnd) 22.dp else 8.dp,
                bottomEnd = if (alignEnd) 8.dp else 22.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (alignEnd) Color(0xFFD8F3E3) else Color(0xFFF8FAFB)
            )
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF173139)
            )
        }
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                color = Color(0xFF143846),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(AccentGreen, AccentBlue, AccentGreen)
                ),
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawCircle(
                color = Color(0xFF0E2230),
                radius = size.minDimension / 3f
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AI",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF8FD9D4)
            )
        }
    }
}

@Composable
private fun StepRow(
    step: AgentStep,
    isActive: Boolean,
) {
    val accent = when {
        step.complete -> AccentGreen
        isActive -> SoftAmber
        else -> Color(0xFF54717B)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2430)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (step.complete) "OK" else "...",
                    color = accent,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = step.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF84A7AF)
                )
            }
        }
    }
}

@Composable
private fun UrgencyBanner(level: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DangerRed)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Urgency Level",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFFFD7D5)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = level,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Immediate evaluation recommended",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFE7E5)
            )
        }
    }
}

@Composable
private fun SoapSection(
    label: String,
    accent: Color,
    content: String,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.first().toString(),
                    color = accent,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1B353D)
                )
            }
        }
    }
}

@Composable
private fun SoapPlanSection(plan: List<String>) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PLAN",
                style = MaterialTheme.typography.labelMedium,
                color = SoftAmber
            )
            plan.forEach { item ->
                Text(
                    text = "- $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1B353D)
                )
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    currentScreen: DemoScreen,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentScreen != DemoScreen.Vitals) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Back")
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = currentScreen != DemoScreen.Soap,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            Text(
                text = if (currentScreen == DemoScreen.Soap) "Ready for Demo" else "Continue",
                color = DeepBackground
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TriageAidPreview() {
    PneumaTheme {
        TriageAidApp()
    }
}

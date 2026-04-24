package com.farha.pneuma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.farha.pneuma.ui.theme.AccentBlue
import com.farha.pneuma.ui.theme.AccentGreen
import com.farha.pneuma.ui.theme.AccentPurple
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

data class PatientInfo(
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

data class Assessment(
    val urgency: String,
    val narrative: String = "",
    val flags: List<String>,
)

data class SoapNote(
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
            subjective = "Patient reports chest pain in the center of the chest radiating to the left arm, started 2 hours ago. Pain rated 8/10. No prior cardiac history.",
            objective = "Heart Rate: 112 bpm\nAge/Sex: 58 / Male\nVisual: Mild facial pallor, no visible rash or swelling.",
            assessment = Assessment(
                urgency = "High Risk",
                narrative = "High risk for acute coronary syndrome.",
                flags = listOf("Possible Cardiac Event", "Chest Pain", "Tachycardia")
            ),
            plan = listOf(
                "Obtain 12-lead ECG",
                "Check troponin levels",
                "Monitor vital signs",
                "Consider aspirin if no contraindications",
                "Refer to ER / cardiology evaluation",
            )
        )
    )
}

@Composable
private fun TriageAidApp() {
    val state = remember { demoState() }
    var currentScreen by remember { mutableStateOf(DemoScreen.Vitals) }
    var age by remember { mutableStateOf(state.patientInfo.age) }
    var sex by remember { mutableStateOf(state.patientInfo.sex) }
    // Always start with mock data so Screen 4 is never blank; Claude updates it in background.
    var soapNote by remember { mutableStateOf(state.soapNote) }
    var isUpdatingSoap by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentScreen) {
        if (currentScreen == DemoScreen.Soap && !isUpdatingSoap) {
            isUpdatingSoap = true
            val result = try {
                ClaudeService.generateSoapNote(
                    heartRate = state.heartRate,
                    patientInfo = PatientInfo(age = age, sex = sex),
                    transcript = state.transcript,
                    visualSymptom = state.visualSymptom,
                )
            } catch (e: Exception) { null }
            if (result != null) soapNote = result
            isUpdatingSoap = false
        }
    }

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
                                transcript = state.transcript,
                                visualSymptom = state.visualSymptom
                            )

                            DemoScreen.Processing -> ProcessingScreen(
                                steps = state.agentSteps
                            )

                            DemoScreen.Soap -> SoapScreen(
                                soapNote = soapNote,
                                isUpdating = isUpdatingSoap,
                                onRegenerate = {
                                    coroutineScope.launch {
                                        isUpdatingSoap = true
                                        val result = try {
                                            ClaudeService.generateSoapNote(
                                                heartRate = state.heartRate,
                                                patientInfo = PatientInfo(age = age, sex = sex),
                                                transcript = state.transcript,
                                                visualSymptom = state.visualSymptom,
                                            )
                                        } catch (e: Exception) { null }
                                        if (result != null) soapNote = result
                                        isUpdatingSoap = false
                                    }
                                }
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
    visualSymptom: String,
) {
    ScreenCard(
        eyebrow = "Screen 2",
        title = "AI nurse intake with voice and symptom vision",
        supporting = "This is the demo centerpiece: question playback, patient response, then a symptom photo prompt with Gemini analysis."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusPill("ElevenLabs voice intro cached", AccentGreen)
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
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F4A55)
                        )
                    ) {
                        Text("Open Camera")
                    }
                    Text(
                        text = "Visual summary: $visualSymptom",
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
                        text = "Listening...",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Keep Chrome fallback or typed input ready if STT misbehaves.",
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
    isUpdating: Boolean,
    onRegenerate: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, SubtleStroke)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Scrollable SOAP content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clinical Brief (SOAP)",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isUpdating) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Updating with Claude...",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentGreen
                            )
                        }
                    }
                }

                UrgencyBanner(level = soapNote.assessment.urgency)

                // S — Subjective
                SoapSectionCard(letter = "S", label = "Subjective", accent = AccentBlue) {
                    Text(
                        text = soapNote.subjective,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCCE4EE)
                    )
                }

                // O — Objective (bullet per line)
                SoapSectionCard(letter = "O", label = "Objective", accent = AccentGreen) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        soapNote.objective.split("\n").filter { it.isNotBlank() }.forEach { line ->
                            Text(
                                text = "• $line",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCCE4EE)
                            )
                        }
                    }
                }

                // A — Assessment (narrative + flag chips)
                SoapSectionCard(letter = "A", label = "Assessment", accent = SoftAmber) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (soapNote.assessment.narrative.isNotBlank()) {
                            Text(
                                text = soapNote.assessment.narrative,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCCE4EE)
                            )
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            soapNote.assessment.flags.forEach { flag ->
                                FlagChip(text = flag, color = SoftAmber)
                            }
                        }
                    }
                }

                // P — Plan (bullet list)
                SoapSectionCard(letter = "P", label = "Plan", accent = AccentPurple) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        soapNote.plan.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCCE4EE)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Fixed action bar
            HorizontalDivider(color = SubtleStroke)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRegenerate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Regenerate", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Share", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("Hand-off", color = DeepBackground, style = MaterialTheme.typography.labelLarge)
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
    val isHigh = level.contains("high", ignoreCase = true)
    val isMod = level.contains("moderate", ignoreCase = true) || level.contains("medium", ignoreCase = true)
    val bgColor = when { isHigh -> DangerRed; isMod -> SoftAmber; else -> AccentGreen }
    val indicator = when { isHigh -> "🔴"; isMod -> "🟡"; else -> "🟢" }
    val subLabel = when {
        isHigh -> "Immediate Evaluation Recommended"
        isMod -> "Timely Evaluation Recommended"
        else -> "Routine Evaluation Appropriate"
    }
    val textColor = if (!isHigh && !isMod) DeepBackground else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "URGENCY LEVEL",
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.75f)
            )
            Text(
                text = "⚠  $level",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "$indicator $subLabel",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun SoapSectionCard(
    letter: String,
    label: String,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2030)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepBackground
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
                content()
            }
        }
    }
}

@Composable
private fun FlagChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
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

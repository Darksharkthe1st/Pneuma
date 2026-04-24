# Team Handoff

## Fast Ownership Split

This scaffold was set up so each person can mostly stay in their lane.

### Person 1: Vitals Scan + Presage

Primary area:

- [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt): `VitalsScreen()` and `demoState()`

What to own:

- camera/photo capture for vitals
- Presage request + parsing
- update `heartRate`
- update `patientInfo`

Look at:

- `VitalsScreen()` around line 348
- `MetricBadge()` around line 719
- `CornerFrame()` around line 774
- [PRESAGE_SMARTSPECTRA.md](C:/Users/farha/GITHUB/Pneuma2/docs/PRESAGE_SMARTSPECTRA.md)

### Person 2: AI Intake Nurse

Primary area:

- [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt): `IntakeScreen()` and `demoState()`

What to own:

- ElevenLabs TTS
- speech-to-text flow
- adaptive question loop
- Gemini symptom image analysis
- update `transcript`
- update `visualSymptom`

Look at:

- `IntakeScreen()` around line 481
- `ChatBubble()` around line 818
- `StatusPill()` around line 802

### Person 3: SOAP Note + Claude

Primary area:

- [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt): `SoapScreen()` and `demoState()`

What to own:

- Claude prompt + JSON parse
- map output into `soapNote`
- urgency/flags/plan rendering
- print/share/handoff actions

Look at:

- `SoapScreen()` around line 620
- `UrgencyBanner()` around line 953
- `SoapSection()` around line 986
- `SoapPlanSection()` around line 1032

### Person 4: App Shell + Processing + Fallbacks

Primary area:

- [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt): `TriageAidApp()`, `StepTracker()`, `ProcessingScreen()`, `BottomNavigation()`
- theme files in [ui/theme](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme)

What to own:

- top-level app flow
- state integration
- processing step updates
- fallback/mock mode if APIs fail
- design polish

Look at:

- `TriageAidApp()` around line 183
- `StepTracker()` around line 286
- `ProcessingScreen()` around line 574
- `StepRow()` around line 901
- `BottomNavigation()` around line 1060

## Team Rule: Avoid Merge Pain

Because most of the app is still in one file, the team should coordinate by editing mostly separate sections.

- Person 1 stays in the vitals section plus state fields they own.
- Person 2 stays in the intake section plus state fields they own.
- Person 3 stays in the SOAP section plus state fields they own.
- Person 4 owns top-level navigation, processing, theme, and integration.

If two people need to touch `demoState()` or `TriageAidApp()` at the same time, sync first.

## Safest Shared Contract

Treat this as the source of truth for integration:

```kotlin
DemoState(
    heartRate = ...,
    patientInfo = PatientInfo(age = ..., sex = ...),
    transcript = listOf(TranscriptTurn(question = ..., answer = ...)),
    visualSymptom = ...,
    agentSteps = listOf(AgentStep(label = ..., detail = ..., complete = ...)),
    soapNote = SoapNote(
        subjective = ...,
        objective = ...,
        assessment = Assessment(
            urgency = ...,
            flags = listOf(...)
        ),
        plan = listOf(...)
    )
)
```

If your feature returns data in a different shape, convert it before writing into app UI state.

## Demo Priority

If time gets tight:

1. Keep the 4-screen flow working.
2. Keep the transitions smooth.
3. Use mock fallback data if any API is unstable.
4. Do not spend the last 20 minutes on refactors.

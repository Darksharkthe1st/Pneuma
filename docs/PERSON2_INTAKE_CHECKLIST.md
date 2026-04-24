# Person 2 Checklist: Voice + Vision + Agent Layer

This is the working checklist for Screen 2 of TriageAid v2.

Your job is to make the intake nurse experience feel real:

- the app asks questions out loud
- the patient answers by voice
- the app adapts follow-up questions
- the patient shows the affected area
- the app produces a visual symptom summary

## Main Goal

By the end, Screen 2 should produce:

- `transcript`
- `visualSymptom`
- agent progress updates for Screen 3

## Important Reality Check

For this Android app:

- use Android `SpeechRecognizer`, not Web Speech API
- use a backend or simple local rules for agent logic, not LangChain JS directly in Android unless your team already has that working

## Phase 1: Get The Screen Working With Fake Data

- [ ] Keep `IntakeScreen()` as the main UI target in [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt)
- [ ] Replace only the data flow first, not the entire visual design
- [ ] Make sure the screen can render:
  - [ ] current question
  - [ ] transcript bubbles
  - [ ] listening state
  - [ ] symptom photo prompt
  - [ ] visual symptom summary
- [ ] Start with hardcoded questions and fake answers so the flow works before any API integration

## Phase 2: Create Person 2 State

- [ ] Create an intake-specific state holder, ideally `IntakeViewModel`
- [ ] Add fields for:
  - [ ] `turns: List<{ question, answer }>`
  - [ ] `currentQuestion`
  - [ ] `isListening`
  - [ ] `isSpeaking`
  - [ ] `capturedImageUri` or image reference
  - [ ] `visualSymptom`
  - [ ] `questionCount`
  - [ ] `isComplete`
- [ ] Keep a strict max of 4 to 5 questions
- [ ] Add one place where final Person 2 outputs can be copied into shared app state

Suggested shape:

```kotlin
data class IntakeTurn(
    val question: String,
    val answer: String
)

data class IntakeUiState(
    val turns: List<IntakeTurn> = emptyList(),
    val currentQuestion: String = "",
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val capturedImageUri: String? = null,
    val visualSymptom: String = "",
    val questionCount: Int = 0,
    val isComplete: Boolean = false
)
```

## Phase 3: Permissions

- [ ] Add `RECORD_AUDIO` permission
- [ ] Add `CAMERA` permission if not already present
- [ ] Keep `INTERNET` permission available for ElevenLabs and Gemini calls
- [ ] Add permission request flow in Compose
- [ ] Handle denial gracefully with a fallback UI

Definition of done:

- [ ] the app can request and receive mic permission
- [ ] the app can request and receive camera permission

## Phase 4: Voice Input

- [ ] Use Android `SpeechRecognizer`
- [ ] Build `startListening()`
- [ ] Build `stopListening()`
- [ ] Capture final recognized text
- [ ] Handle common STT failures:
  - [ ] no speech detected
  - [ ] timeout
  - [ ] partial recognition failure
- [ ] Show listening state in UI
- [ ] Add typed-text fallback input

Definition of done:

- [ ] user taps mic
- [ ] user speaks
- [ ] answer text appears in the transcript

## Phase 5: Voice Output

- [ ] Build `speakQuestion(question: String)`
- [ ] Decide implementation:
  - [ ] ElevenLabs API playback
  - [ ] fallback to Android TTS if ElevenLabs fails
- [ ] Prevent listening while the app is speaking
- [ ] Mark `isSpeaking` during playback
- [ ] Resume listening after question playback ends

Definition of done:

- [ ] first question is spoken aloud
- [ ] follow-up questions can also be played aloud

## Phase 6: Question Loop

- [ ] Start with a fixed first question:
  - [ ] `"Hi, where does it hurt today?"`
- [ ] After each answer, append to transcript
- [ ] Choose the next question
- [ ] Stop after enough info is collected or after 4 to 5 questions

### Demo-Safe First Version

- [ ] Use simple rules before trying a full agent

Example rule flow:

- [ ] if answer mentions chest pain -> ask pain severity
- [ ] then ask onset
- [ ] then ask radiation or associated symptoms
- [ ] then ask shortness of breath / nausea / sweating
- [ ] stop

Definition of done:

- [ ] the app asks multiple questions
- [ ] the next question depends on previous answers
- [ ] the loop ends reliably

## Phase 7: Agent Layer

- [ ] Start with local rule-based logic
- [ ] If backend exists later, replace rules with API call like `getNextQuestion(transcript)`
- [ ] Add retry / rephrase behavior if answer is vague
- [ ] Add timeout so the loop never hangs
- [ ] Add a hard stop condition

Recommended stop conditions:

- [ ] question count reached 5
- [ ] enough clinical info collected
- [ ] user skipped answer twice

Definition of done:

- [ ] the intake feels adaptive
- [ ] it never loops forever

## Phase 8: Symptom Photo Capture

- [ ] Add camera launcher or image capture flow
- [ ] Save captured image reference
- [ ] Show preview or confirmation state
- [ ] Add retry / retake button

Definition of done:

- [ ] after voice intake, user can capture one symptom image

## Phase 9: Gemini Vision

- [ ] Build `analyzeSymptomImage(image)`
- [ ] Send image plus a short medical/symptom prompt
- [ ] Return one concise visual summary string
- [ ] Show loading state while analysis runs
- [ ] Add fallback mock description if request fails

Suggested output style:

- [ ] one sentence
- [ ] visually descriptive
- [ ] no overclaiming diagnosis

Example:

`"Visible mild redness and localized swelling around the left forearm without active bleeding."`

Definition of done:

- [ ] image is analyzed
- [ ] `visualSymptom` is filled
- [ ] UI shows the returned description

## Phase 10: Shared State Handoff

- [ ] Copy transcript into the app's shared `transcript`
- [ ] Copy vision result into shared `visualSymptom`
- [ ] Update processing steps for Screen 3
- [ ] Signal when intake is complete so navigation can move forward

Definition of done:

- [ ] Screen 3 can display voice intake and vision steps as complete
- [ ] Screen 4 can use the transcript and visual summary

## Required Fallbacks

- [ ] typed answer input if STT fails
- [ ] Android TTS or silent text-only mode if ElevenLabs fails
- [ ] mock visual symptom text if Gemini fails
- [ ] fixed question flow if adaptive logic fails
- [ ] 30 second timeout for slow API requests

## Minimum Demo Success Criteria

- [ ] app speaks first question
- [ ] user answers by voice
- [ ] transcript fills on screen
- [ ] app asks 3 to 5 follow-up questions
- [ ] user captures symptom photo
- [ ] app shows a visual symptom summary
- [ ] Screen 2 outputs are ready for Screen 3 and Screen 4

## Nice-To-Haves If Time Remains

- [ ] waveform or speaking animation
- [ ] partial transcript preview while listening
- [ ] replay last question button
- [ ] retake image button
- [ ] confidence / uncertainty text for visual analysis

## Recommended Build Order

- [ ] make Screen 2 work with fake local data
- [ ] add intake view model/state
- [ ] add mic permission + STT
- [ ] add ElevenLabs playback
- [ ] add rule-based next-question logic
- [ ] add symptom photo capture
- [ ] add Gemini image analysis
- [ ] connect to shared app state
- [ ] test end-to-end on physical device

## Testing Checklist

- [ ] test on a real Android device
- [ ] test in noisy room conditions
- [ ] test mic permission denied path
- [ ] test no internet fallback
- [ ] test no speech detected path
- [ ] test image capture failure path
- [ ] test max-question stop path

## Personal Working Plan

If you want the fastest route to visible progress, do it in this order:

1. Make a local `IntakeViewModel`
2. Add typed-answer mode
3. Add mic/STT
4. Add ElevenLabs playback
5. Add rule-based adaptive questioning
6. Add symptom image capture
7. Add Gemini visual analysis
8. Hand state off to Screen 3 and Screen 4

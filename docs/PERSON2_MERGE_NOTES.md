# Person 2 Merge Notes

This doc is the handoff for the voice, vision, and intake work currently in the Android app.

Use this during merge/integration so the team keeps the important Screen 2 behavior intact.

## What Was Built

Screen 2 is no longer just static mock UI. It now has a working local intake flow with:

- spoken nurse questions using Android `TextToSpeech`
- real Android speech-to-text using `SpeechRecognizer`
- typed fallback input
- demo-answer fallback input
- runtime mic permission flow
- runtime camera permission flow
- local intake state in a dedicated `ViewModel`
- mock symptom photo capture flow
- mock visual symptom summary flow
- inline voice error handling

## Files Added

- [IntakeViewModel.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/IntakeViewModel.kt)
- [SpeechRecognizerManager.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/SpeechRecognizerManager.kt)
- [QuestionSpeaker.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/QuestionSpeaker.kt)

## Files Updated

- [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt)
- [AndroidManifest.xml](C:/Users/farha/GITHUB/Pneuma2/app/src/main/AndroidManifest.xml)
- [app/build.gradle.kts](C:/Users/farha/GITHUB/Pneuma2/app/build.gradle.kts)

## Key Features To Keep During Merge

These are the important parts the team should preserve.

### 1. `IntakeViewModel` owns Screen 2 state

Do not move Screen 2 logic back into `MainActivity`.

Keep:

- transcript turns
- current question index
- draft answer
- speaking state
- listening state
- voice error state
- photo captured state
- visual symptom string

Why it matters:

- this is the foundation for swapping local mocks with real APIs without rewriting the whole screen again

### 2. Real Android STT is already wired

Keep `SpeechRecognizerManager`.

Current behavior:

- mic button starts Android speech recognition
- partial results fill the answer field
- final speech result auto-submits into transcript
- typed fallback still works
- speech errors show inline instead of crashing the flow

Why it matters:

- this is already a real working voice input path for the demo

### 3. Real spoken questions are already wired

Keep `QuestionSpeaker`.

Current behavior:

- current question is spoken automatically
- replay button speaks the question again
- speaking state reflects active playback
- question text remains visible if TTS fails

Why it matters:

- this is the "AI nurse" effect the judges will notice immediately

### 4. Permission flow is already handled

Keep the runtime permission logic in `IntakeScreen()`.

Current behavior:

- asks for mic permission when needed
- asks for camera permission when needed
- shows permission guidance card when access is missing
- falls back safely instead of assuming permissions exist

Why it matters:

- avoids a broken demo on first app launch

### 5. The screen still has safe demo fallbacks

Keep:

- typed answer field
- `Use Demo Answer` button
- mock symptom photo path
- mock visual summary generation

Why it matters:

- if speech or future APIs misbehave, Screen 2 still demos cleanly

## Current Screen 2 Behavior

Right now the intake flow works like this:

1. App reaches Screen 2
2. Current nurse question is spoken aloud
3. User can:
   - use microphone
   - type answer
   - use demo answer
4. Answer is appended to transcript
5. Next question is spoken
6. After the question loop, user can capture a mock photo
7. User can generate a mock visual summary

## Merge Checklist

- [ ] Keep `IntakeViewModel.kt`
- [ ] Keep `SpeechRecognizerManager.kt`
- [ ] Keep `QuestionSpeaker.kt`
- [ ] Keep manifest permissions:
  - [ ] `RECORD_AUDIO`
  - [ ] `CAMERA`
  - [ ] `INTERNET`
  - [ ] `ACCESS_NETWORK_STATE`
- [ ] Keep `lifecycle-viewmodel-compose` dependency in `app/build.gradle.kts`
- [ ] Keep Screen 2 runtime permission UI
- [ ] Keep replay button behavior
- [ ] Keep mic start/stop behavior
- [ ] Keep typed fallback input
- [ ] Keep inline voice error card
- [ ] Keep mock symptom image flow until real vision API is ready

## What Is Still Local / Mock

These parts are intentionally still fake and can be replaced later:

- question order is still scripted
- spoken voice uses native Android TTS, not ElevenLabs yet
- vision flow uses mock capture state, not real camera image upload
- visual summary is still local mock text

That is okay for now. The important part is that the flow and interfaces already exist.

## Safe Next Replacements

These swaps should be safe without changing the whole UI:

- replace scripted question selection with lightweight adaptive logic
- replace Android TTS with ElevenLabs playback if time allows
- replace mock photo capture with real image capture
- replace mock visual summary with Gemini vision output

## What Teammates Should Not Accidentally Remove

- the `LaunchedEffect`-driven question playback path in `IntakeScreen`
- the speech recognizer lifecycle cleanup
- the TTS lifecycle cleanup
- the inline permission request behavior
- the `voiceError` state and UI
- the auto-submit behavior for final STT results

## Recommended Merge Priority

If there is a conflict during merge, prefer keeping the Person 2 behavior for:

1. `IntakeViewModel`
2. `SpeechRecognizerManager`
3. `QuestionSpeaker`
4. Screen 2 permission logic
5. typed/demo fallback controls

UI polish can be adjusted later. The working voice flow is the valuable part.

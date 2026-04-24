# Codebase Guide

## Project Structure

The project is intentionally lean.

```text
Pneuma2/
  app/
    src/main/java/com/farha/pneuma/
      MainActivity.kt
      ui/theme/
        Color.kt
        Theme.kt
        Type.kt
    src/main/res/
      values/
        strings.xml
        colors.xml
        themes.xml
  gradle/libs.versions.toml
```

## Main App File

Most of the product code currently lives in [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt).

### Top-Level Structure

- `MainActivity` at line 79: Android entry point
- `DemoScreen` at line 91: enum for the 4 screens
- `PatientInfo`, `TranscriptTurn`, `AgentStep`, `Assessment`, `SoapNote`, `DemoState` at lines 98-133: lightweight app models
- `demoState()` at line 135: mock data used by the whole scaffold
- `TriageAidApp()` at line 183: top-level composable that owns navigation and state

## Screen Map

Each screen already has a dedicated composable, even though they are all in one file for now.

- Screen 1: `VitalsScreen()` at line 348
- Screen 2: `IntakeScreen()` at line 481
- Screen 3: `ProcessingScreen()` at line 574
- Screen 4: `SoapScreen()` at line 620

The screen tracker at the top is `StepTracker()` at line 286, and the bottom nav buttons are `BottomNavigation()` at line 1060.

## Shared UI Components

These are the reusable building blocks in the second half of `MainActivity.kt`.

- `ScreenCard()` at line 678: shared container for all screens
- `MetricBadge()` at line 719: vitals/signal cards
- `CornerFrame()` at line 774: scan box corners on the vitals screen
- `StatusPill()` at line 802: small status badge
- `ChatBubble()` at line 818: intake conversation bubbles
- `ProgressRing()` at line 855: processing ring animation
- `StepRow()` at line 901: agent progress rows
- `UrgencyBanner()` at line 953: red urgency card
- `SoapSection()` at line 986: Subjective/Objective/Assessment card
- `SoapPlanSection()` at line 1032: Plan list card

## Theme Files

The visual system is separated from the screen logic.

- [Color.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme/Color.kt): named design tokens
- [Theme.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme/Theme.kt): Material 3 color scheme
- [Type.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme/Type.kt): typography setup

If you want to restyle the app without touching product logic, start there.

## Where To Edit Common Things

### Change the screen flow

Edit `DemoScreen` and the `when (screen)` block inside `TriageAidApp()`.

### Change mock patient/demo content

Edit `demoState()`.

### Change the header or app shell

Edit `AppHeader()` and `TriageAidApp()`.

### Change colors

Edit the values in `Color.kt`.

### Change type scale

Edit `Type.kt`.

## Recommended Post-Demo Refactor

Once the hackathon crunch is over, split `MainActivity.kt` into:

- `ui/app/TriageAidApp.kt`
- `ui/screens/VitalsScreen.kt`
- `ui/screens/IntakeScreen.kt`
- `ui/screens/ProcessingScreen.kt`
- `ui/screens/SoapScreen.kt`
- `ui/components/`
- `model/`

For the demo, keep the current layout unless a refactor directly helps an integration task.

# TriageAid v2

Android Studio / Jetpack Compose scaffold for the 4-screen triage demo:

1. Vitals Scan
2. AI Intake Nurse
3. Processing / Agent Thinking
4. Doctor SOAP Note

This repo is intentionally small right now so a hackathon team can move fast. Most of the UI is in one file, and each teammate can plug their API work into the existing mock state before we split the code into separate modules later.

## Quick Start

```powershell
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:assembleDebug
```

Open the project in Android Studio, run the `app` configuration, and launch on an emulator or Android device.

## Where To Start

- App entry point: [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt)
- Theme colors: [Color.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme/Color.kt)
- Material theme: [Theme.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme/Theme.kt)
- Typography: [Type.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/ui/theme/Type.kt)
- App name string: [strings.xml](C:/Users/farha/GITHUB/Pneuma2/app/src/main/res/values/strings.xml)

## Team Docs

- Codebase map: [docs/CODEBASE_GUIDE.md](C:/Users/farha/GITHUB/Pneuma2/docs/CODEBASE_GUIDE.md)
- Team ownership and handoff notes: [docs/TEAM_HANDOFF.md](C:/Users/farha/GITHUB/Pneuma2/docs/TEAM_HANDOFF.md)
- API/state integration notes: [docs/API_INTEGRATION.md](C:/Users/farha/GITHUB/Pneuma2/docs/API_INTEGRATION.md)
- Presage / SmartSpectra integration guide: [docs/PRESAGE_SMARTSPECTRA.md](C:/Users/farha/GITHUB/Pneuma2/docs/PRESAGE_SMARTSPECTRA.md)
- Person 2 merge notes: [docs/PERSON2_MERGE_NOTES.md](C:/Users/farha/GITHUB/Pneuma2/docs/PERSON2_MERGE_NOTES.md)

## Current Architecture

Right now the app is a single-activity Compose demo shell:

- `MainActivity` sets the app theme and launches `TriageAidApp`
- `TriageAidApp` owns the current screen and mock demo state
- Each of the 4 product screens is a separate composable in the same file
- Shared UI pieces like badges, chat bubbles, progress steps, and SOAP sections are also in that same file

This is deliberate for speed. After the demo, the next cleanup step is to split each screen into its own file and move models into a dedicated package.

## Build Goal For Teammates

Do not rewrite the scaffold unless you have to. The fastest path is:

1. Keep the existing UI.
2. Replace mock values with real API results.
3. Keep the `DemoState` shape aligned with the shared state contract.
4. Only refactor after the end-to-end demo flow works.

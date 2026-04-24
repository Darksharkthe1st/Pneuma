# API Integration Notes

## Current State

The app currently runs from mock data inside `demoState()` in [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt).

That is intentional. It gives the team a stable demo baseline before any external service is wired up.

## Suggested Integration Strategy

Do not connect every API at once. Wire the app in this order:

1. Replace mock vitals data.
2. Replace mock intake transcript data.
3. Replace mock visual symptom data.
4. Replace mock processing steps.
5. Replace mock SOAP note data.

This keeps the app usable after each integration step.

## Best Hook Points

### Vitals / Presage

Update these values when the scan completes:

- `heartRate`
- `patientInfo`

Render target:

- `VitalsScreen()`

Reference guide:

- [PRESAGE_SMARTSPECTRA.md](C:/Users/farha/GITHUB/Pneuma2/docs/PRESAGE_SMARTSPECTRA.md)

### Intake Voice + Vision

Update these values as the interview runs:

- `transcript`
- `visualSymptom`

Render target:

- `IntakeScreen()`

### Processing / Agent Progress

Update these values while requests are in flight:

- `agentSteps`

Render target:

- `ProcessingScreen()`

### Claude / SOAP

Update this object after the summary is generated:

- `soapNote`

Render target:

- `SoapScreen()`

## Fallback Mode

For the hackathon demo, fallback data is a feature, not a failure.

Recommended rule:

- if an API succeeds, show real data
- if an API times out or errors, keep the screen moving with mock data
- do not block the whole app on one broken provider

## Near-Term Refactor Suggestion

When the team has breathing room, replace `demoState()` with a state holder such as:

- a `ViewModel`
- a `mutableStateOf(DemoState(...))`
- separate screen models if the file gets too crowded

For now, moving fast matters more than perfect architecture.

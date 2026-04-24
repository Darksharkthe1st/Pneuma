# Presage / SmartSpectra Android SDK Guide

This doc adapts the SmartSpectra Android SDK notes for this TriageAid codebase so the vitals teammate can move quickly without guessing where the SDK fits.

## What Presage Owns In This App

Presage powers Screen 1, the vitals scan flow.

In this project, that means:

- camera-based scan UI
- authentication with the SmartSpectra SDK
- camera permissions
- measurement start/stop flow
- reading pulse rate from SDK metrics
- writing the result into app state for the rest of the demo

Primary UI target:

- [MainActivity.kt](C:/Users/farha/GITHUB/Pneuma2/app/src/main/java/com/farha/pneuma/MainActivity.kt): `VitalsScreen()`

State handoff target:

- `heartRate`
- `patientInfo`
- `agentSteps`

## Fastest Hackathon Path

If time is tight, optimize for the simplest working integration:

1. Set up camera permissions and SDK authentication.
2. Get a pulse value from the SDK.
3. Render that pulse value in Screen 1.
4. Push the pulse value into the shared app state.
5. Leave advanced customization for later.

For the demo, heart rate is the must-have value.

## SDK Quick Start

Minimal setup pattern from the SDK:

```kotlin
val apiKey = "YOUR_API_KEY_HERE"
SmartSpectraSdk.instance.setApiKey(apiKey)

SmartSpectraSdk.instance.setSmartSpectraMode(
    SmartSpectraSdkConfig.SmartSpectraMode.CONTINUOUS
)
SmartSpectraSdk.instance.setMeasurementDuration(30.0)
SmartSpectraSdk.instance.setCameraPosition(
    SmartSpectraSdkConfig.CameraPosition.FRONT
)
SmartSpectraSdk.instance.setRecordingDelay(3)
SmartSpectraSdk.instance.setShowFps(false)
```

Main Compose surface from the SDK:

```kotlin
SmartSpectraView()
```

To read measurements:

```kotlin
val viewModel: MediapipeGraphViewModel = viewModel()
val metricsBuffer by viewModel.metricsBuffer.observeAsState()

metricsBuffer?.let { metrics ->
    val pulseRate = metrics.pulse.strict.value.toInt()
}
```

## Project Changes Needed In This Repo

### 1. Add the SDK dependency

In [app/build.gradle.kts](C:/Users/farha/GITHUB/Pneuma2/app/build.gradle.kts), add the Presage dependency using whichever distribution your team has access to:

```kotlin
dependencies {
    implementation(project(":sdk"))
    // or, if published for your team:
    // implementation("com.presagetech:smartspectra-android-sdk:<version>")
}
```

You will likely also need:

```kotlin
implementation("androidx.compose.runtime:runtime-livedata")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:<version>")
implementation("androidx.security:security-crypto:<version>")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:<version>")
```

### 2. Add required permissions

In [AndroidManifest.xml](C:/Users/farha/GITHUB/Pneuma2/app/src/main/AndroidManifest.xml), add:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Recommended camera features:

```xml
<uses-feature
    android:name="android.hardware.camera"
    android:required="true" />
<uses-feature
    android:name="android.hardware.camera.front"
    android:required="false" />
```

### 3. Choose an auth mode

The SDK supports:

- API key auth for development
- OAuth config via `presage_services.xml` for production / Play Store flows

Fastest hackathon option:

```kotlin
SmartSpectraSdk.instance.setApiKey("YOUR_API_KEY_HERE")
```

OAuth option:

- create `res/xml/presage_services.xml`
- add the values from the Presage developer portal

Example:

```xml
<resources>
    <string name="BUNDLE_ID">com.your.registered.package.name</string>
    <bool name="IS_OAUTH_ENABLED">true</bool>
    <string name="CLIENT_ID">your_oauth_client_id</string>
    <string name="SUB">your_oauth_sub</string>
    <string name="PLIST_VERSION">1</string>
</resources>
```

## Where It Fits In TriageAid

## Option A: Fastest integration

Replace the placeholder scan UI in `VitalsScreen()` with the SDK's default view:

```kotlin
SmartSpectraView()
```

This is the fastest path to a working demo.

## Option B: Keep our custom TriageAid UI

Keep the existing branded scan screen and use the SDK for data only.

That approach usually looks like:

1. request camera permission
2. initialize `SmartSpectraSdk.instance`
3. create/use `MediapipeGraphViewModel`
4. start processing / recording
5. observe `metricsBuffer`
6. map `metrics.pulse.strict.value.toInt()` into app state
7. show the result inside our existing `MetricBadge`

That gives the team a more polished demo while still using Presage under the hood.

## Suggested Integration Sketch For This App

This is the safest conceptual target for the current scaffold:

```kotlin
@Composable
fun VitalsScreen(...) {
    val smartSpectraViewModel: MediapipeGraphViewModel = viewModel()
    val metricsBuffer by smartSpectraViewModel.metricsBuffer.observeAsState()

    LaunchedEffect(metricsBuffer) {
        metricsBuffer?.let { metrics ->
            val pulseRate = metrics.pulse.strict.value.toInt()
            if (pulseRate > 0) {
                // write into shared TriageAid state
            }
        }
    }

    // Keep custom TriageAid UI here
}
```

## Core SDK Concepts

### `SmartSpectraSdk`

Singleton SDK access point.

Use it for:

- API key setup
- mode selection
- measurement duration
- recording delay
- camera position
- headless mode
- reading SDK-level `LiveData`

Examples:

```kotlin
val sdk = SmartSpectraSdk.instance
sdk.setApiKey("YOUR_API_KEY_HERE")
sdk.setSmartSpectraMode(SmartSpectraSdkConfig.SmartSpectraMode.CONTINUOUS)
sdk.setMeasurementDuration(30.0)
sdk.setCameraPosition(SmartSpectraSdkConfig.CameraPosition.FRONT)
sdk.setRecordingDelay(3)
sdk.setShowFps(false)
sdk.setHeadlessMode(false)
```

### `MediapipeGraphViewModel`

Main processing controller for camera input and metrics extraction.

Use it for:

- `startProcessing()`
- `stopProcessing()`
- `startRecording()`
- `stopRecording()`
- observing `metricsBuffer`
- reading `statusHint`
- reading `processingStatus`

Example:

```kotlin
val viewModel: MediapipeGraphViewModel = viewModel()

viewModel.startProcessing()
viewModel.startRecording()
```

### `SmartSpectraView`

Full default UI from the SDK:

```kotlin
SmartSpectraView()
```

This is useful if the branded custom screen becomes too risky late in the hackathon.

## Measurements You Care About Most

For TriageAid v2, the main metric is pulse / heart rate.

Useful access patterns:

```kotlin
val pulseRate = metrics.pulse.strict.value.toInt()
val breathingRate = metrics.breathing.strict.value.toInt()
```

More available data from the SDK:

- pulse traces
- breathing traces
- apnea detection
- blood pressure-related values
- face metrics such as blinking/talking
- measurement metadata
- face mesh points

For the demo, do not overbuild. Heart rate is the key value to surface first.

## Camera Permission Pattern

Compose-friendly permission request pattern:

```kotlin
val context = LocalContext.current
var hasCameraPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    )
}

val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    hasCameraPermission = isGranted
}
```

If permission is denied, keep a simple fallback state on Screen 1 instead of crashing the demo.

## Recommended Runtime Settings

Good defaults from the supplied SDK guidance:

- mode: `CONTINUOUS`
- duration: `30.0`
- camera: `FRONT`
- recording delay: `3`
- show FPS: `false`

For demo safety:

- keep the phone steady
- keep lighting good
- prefer a physical device over emulator
- test camera permission before demo time

## Error Handling Notes

Useful status cases from the SDK:

- `FACE_NOT_VISIBLE`
- `TOO_MUCH_MOVEMENT`
- `POOR_LIGHTING`
- `PROCESSING_COMPLETE`

Simple response mapping for the UI:

- face not visible -> "Center your face in the frame"
- too much movement -> "Hold still for a few seconds"
- poor lighting -> "Move to a brighter area"

If the SDK returns invalid measurements, keep the current TriageAid scan card visible and show a retry hint rather than a raw crash or stack trace.

## Headless Mode

The SDK also supports headless operation:

```kotlin
SmartSpectraSdk.instance.setHeadlessMode(true)
```

Use this only if you want to keep all camera/result UI fully custom. For a hackathon demo, default SDK UI or hybrid UI is usually safer.

## Integration Checklist For This Repo

- [ ] Add Presage SDK dependency
- [ ] Add `runtime-livedata` and any missing ViewModel/security dependencies
- [ ] Add camera/network permissions to the manifest
- [ ] Choose API key or OAuth auth
- [ ] Request camera permission in Compose
- [ ] Initialize `SmartSpectraSdk.instance`
- [ ] Observe `metricsBuffer`
- [ ] Read `metrics.pulse.strict.value`
- [ ] Map heart rate into TriageAid shared state
- [ ] Show the result in Screen 1
- [ ] Update processing step state after vitals are captured
- [ ] Test on a physical Android device

## Recommended Team Notes

For the current codebase:

- Person 1 should own this integration end to end
- Person 4 should only help if app-level state wiring is needed
- if the custom branded screen gets unstable, temporarily swap in `SmartSpectraView()` and keep moving

## Original SDK Capabilities Covered By The Supplied Notes

The material you shared also includes:

- `SmartSpectraSdkConfig` patterns
- `AuthHandler` flow
- `SmartSpectraButton`
- `SmartSpectraResultView`
- tab-based navigation examples
- headless monitoring examples
- export/state-management snippets

Those are useful references, but for this hackathon codebase the highest-value slice is:

- auth
- permission handling
- `SmartSpectraView()` or `MediapipeGraphViewModel`
- pulse extraction
- mapping the pulse into TriageAid state

# GhostPin Development Guidelines

GhostPin is a modern Android location spoofing & simulation application written in Kotlin and Jetpack Compose.

## Key Technical Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (BOM 2024.10.00) & Material 3
- **Map & Geocoding**: osmdroid (OpenStreetMap native renderer)
- **Local Persistence**: Room 2.6.1 & AndroidX DataStore Preferences
- **Asynchronous / Concurrency**: Kotlin Coroutines & Flows
- **Android Target**: minSdk 26, compileSdk 35, targetSdk 35
- **Build System**: Gradle 8.9 with Version Catalogs (`gradle/libs.versions.toml`)

## Common Commands
- Build Debug APK: `./gradlew assembleDebug`
- Run Unit Tests: `./gradlew test`
- Run Android Lint: `./gradlew lint`
- Clean Build: `./gradlew clean`

# GhostPin 📍👻

<p align="center">
  <img src="app/src/main/res/drawable/ic_ghost_logo.xml" width="96" height="96" alt="GhostPin Logo" />
</p>

<p align="center">
  <b>A lightweight, privacy-focused, and developer-friendly Android GPS simulation & mock location toolkit.</b>
</p>

<p align="center">
  <a href="https://github.com/anumulamadhava2005/Ghost-pin/actions/workflows/android-ci.yml"><img src="https://github.com/anumulamadhava2005/Ghost-pin/actions/workflows/android-ci.yml/badge.svg" alt="CI Status" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.21-purple.svg" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-2024.10.00-blue.svg" alt="Compose" /></a>
  <a href="https://developer.android.com/about/versions/15"><img src="https://img.shields.io/badge/Target%20SDK-35-green.svg" alt="Target SDK" /></a>
  <a href="https://developer.android.com/about/dashboards"><img src="https://img.shields.io/badge/Min%20SDK-26-orange.svg" alt="Min SDK" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-brightgreen.svg" alt="License" /></a>
  <a href="CONTRIBUTING.md"><img src="https://img.shields.io/badge/PRs-Welcome-brightgreen.svg" alt="PRs Welcome" /></a>
</p>

---

## 🌟 Overview

**GhostPin** allows Android developers, QA engineers, and tech enthusiasts to simulate custom GPS locations and complex journeys without physical travel. Built entirely with **Jetpack Compose** and **OpenStreetMap (osmdroid)**, GhostPin delivers a fluid, modern, and open-source location spoofing experience without vendor lock-in or proprietary API keys.

---

## ✨ Features

- 📍 **Instant Location Mocking**: Teleport your device's GPS to any coordinate worldwide with a single tap.
- 🗺️ **Interactive OpenStreetMap**: Smooth, native map rendering with address search, geocoding, and interactive pin placement.
- 🚗 **Multi-Stop Journeys & Routes**:
  - Create customized routes with multiple waypoints.
  - Set custom stay durations per stop.
  - Configure realistic travel speeds (walking, cycling, driving).
  - Real-time progress interpolation between waypoints.
- 💾 **Favorites & Saved Presets**: Save frequently used spots and routes locally using an encrypted Room database.
- ⚙️ **Fine-Grained Simulation Settings**:
  - Configurable update intervals (500ms – 10s).
  - Accuracy jitter & altitude simulation.
  - Auto-stop timers / expiry durations.
- 🛡️ **Foreground Service Engine**: Uninterrupted background simulation with notification controls and wake lock management.
- 🎨 **Modern Material 3 UI**: Dynamic theme colors, dark mode support, and clean animations.

---

## 📱 Screenshots & Architecture

```
com.ghostpin.app
├── data/               # Room database, Entities, DAOs & Repositories
│   ├── local/          # AppDatabase, LocationDao, JourneyDao
│   └── repository/     # Repository implementations & DataStore preferences
├── domain/             # Business models & repository interfaces
│   ├── model/          # MockLocation, Journey, SimulationConfig, MockState
│   └── repository/     # JourneyRepository, LocationRepository, SettingsRepository
├── engine/             # Android Mock Location Provider & Foreground Service
│   ├── MockLocationController.kt
│   └── MockLocationService.kt
└── ui/                 # Jetpack Compose UI (MVVM)
    ├── home/           # Main dashboard & active simulation status
    ├── map/            # Interactive OSM map picker & search
    ├── journey/        # Route creator & journey playback
    ├── saved/          # Saved locations & presets
    ├── settings/       # App preferences & mock provider setup
    ├── navigation/     # Jetpack Navigation Compose graph
    └── theme/          # Material 3 color schemes & typography
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Android Studio Ladybug (2024.2.1+) or Koala / Meerkat recommended.
- **JDK**: Java Development Kit 17 (Eclipse Temurin or OpenJDK 17).
- **Android SDK**: Android 15 (API 35) SDK installed via SDK Manager.
- **Physical Device or Emulator**: Android 8.0 (API level 26) or higher.

### Installation & Build

1. **Clone the repository**:
   ```bash
   git clone https://github.com/anumulamadhava2005/Ghost-pin.git
   cd Ghost-pin
   ```

2. **Open in Android Studio**:
   - Open Android Studio -> *File* -> *Open...* -> Select the cloned `Ghost-pin` folder.
   - Allow Gradle to sync dependencies.

3. **Build via CLI**:
   ```bash
   # Build debug APK
   ./gradlew assembleDebug

   # Run unit tests
   ./gradlew test

   # Run Android lint
   ./gradlew lint
   ```

4. **Run on Device / Emulator**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🔧 Enabling Mock Locations on Android

To use GhostPin on your Android device:

1. Open **Settings** > **About Phone**.
2. Tap **Build Number** 7 times to enable **Developer Options**.
3. Go back to **Settings** > **System** > **Developer Options**.
4. Scroll down to **Select mock location app** (under the *Debugging* category).
5. Choose **GhostPin**.
6. Grant Location & Notification permissions when launching GhostPin.

---

## 🤝 Contributing

We welcome contributions from everyone! Whether you are fixing a bug, adding map providers, polishing the UI, or writing tests, your help is appreciated.

Please read our [Contributing Guide](CONTRIBUTING.md) to get started with our workflow, coding standards, and branch guidelines.

### Good First Issues
- Adding GPX route file import/export.
- Floating joystick overlay for real-time manual movement.
- Additional map tile provider presets (satellite, terrain, dark mode tiles).
- Automated UI tests with Compose Test Rule.

---

## 📜 Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## 🔒 Security

For security vulnerabilities and responsible disclosure guidelines, please refer to our [Security Policy](SECURITY.md).

---

## 📄 License

GhostPin is open-source software licensed under the [MIT License](LICENSE).

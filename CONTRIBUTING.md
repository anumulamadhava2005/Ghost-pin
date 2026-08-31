# Contributing to GhostPin 🚀

First off, thank you for considering contributing to **GhostPin**! Open-source projects thrive because of contributors like you.

Please take a moment to review this document to understand our development workflow, guidelines, and standards.

---

## 📜 Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please report unacceptable behavior to the project maintainers.

---

## 🛠️ Getting Started

1. **Fork the repository** on GitHub.
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/<your-username>/Ghost-pin.git
   cd Ghost-pin
   ```
3. **Add the upstream remote**:
   ```bash
   git remote add upstream https://github.com/anumulamadhava2005/Ghost-pin.git
   ```
4. **Create a new branch** for your work:
   ```bash
   git checkout -b feat/my-awesome-feature
   ```

---

## 🌿 Branch Naming Convention

Please use descriptive branch names with appropriate prefixes:

- `feat/<feature-name>`: A new feature or enhancement.
- `fix/<bug-name>`: A bug fix.
- `refactor/<module-name>`: Code refactoring without behavioral changes.
- `docs/<topic>`: Documentation changes or additions.
- `test/<test-suite>`: Adding or improving test cases.
- `chore/<task>`: Build scripts, dependencies, or maintenance.

*Example*: `feat/floating-joystick`, `fix/route-interpolation-crash`

---

## 📝 Commit Message Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<optional scope>): <description>

[optional body]

[optional footer(s)]
```

### Commit Types
- `feat`: A new feature (e.g., `feat(map): add satellite tile layer support`)
- `fix`: A bug fix (e.g., `fix(service): prevent foreground service notification leak`)
- `docs`: Documentation updates (e.g., `docs: update setup guide for Android 15`)
- `style`: Code style/formatting changes (no production code change)
- `refactor`: Code restructuring without bug fixes or new features
- `test`: Adding or updating test cases
- `chore`: Gradle changes, dependency updates, CI workflows

---

## 🎨 Coding Standards & Architecture

### Architecture Overview
GhostPin follows **Clean Architecture + MVVM (Model-View-ViewModel)** principles:
- **`ui`**: Jetpack Compose screens, ViewModels, UI state definitions, and theme styling. UI components should remain stateless and reactive to `StateFlow` updates.
- **`domain`**: Pure Kotlin models and repository interfaces. Free from Android framework dependencies.
- **`data`**: Room entities, DAOs, repository implementations, and DataStore preferences.
- **`engine`**: Android Mock Location Provider, LocationManager interactions, and Foreground Service lifecycle.

### Kotlin & Compose Guidelines
- Write idiomatic Kotlin (favor immutability `val`, expression bodies where clear, and null-safety).
- In Compose UI:
  - Hoist state to ViewModels or caller composables.
  - Composable functions should always accept a `modifier: Modifier = Modifier` default parameter.
  - Provide `@Preview` annotations with theme wrappers for UI components where applicable.
- In Coroutines & Flow:
  - Do not use `GlobalScope`.
  - Use `viewModelScope` for UI-bound operations.
  - Dispatch I/O operations to `Dispatchers.IO`.

---

## 🧪 Testing Guidelines

Before opening a Pull Request, ensure that all tests pass:

```bash
# Run unit tests
./gradlew test

# Run Android Lint
./gradlew lint
```

When introducing new domain logic or data transformations, please write corresponding JUnit test cases under `app/src/test/java/com/ghostpin/app/`.

---

## 🚀 Submitting a Pull Request

1. **Keep PRs focused**: One feature or bug fix per PR. Avoid bundling unrelated changes.
2. **Sync with upstream**:
   ```bash
   git checkout master
   git pull upstream master
   git checkout feat/my-awesome-feature
   git rebase master
   ```
3. **Push to your fork**:
   ```bash
   git push origin feat/my-awesome-feature
   ```
4. **Open a Pull Request**:
   - Provide a clear, descriptive title.
   - Complete the [Pull Request Template](.github/pull_request_template.md).
   - Link any related issue(s) (e.g., `Closes #12`).
   - Attach screenshots or screen recordings for UI-related changes.
5. **Ensure CI passes**: Address any test or lint failures reported by GitHub Actions.

---

## 🐛 Reporting Bugs & Suggesting Features

- **Bug Reports**: Use our [Bug Report Template](https://github.com/anumulamadhava2005/Ghost-pin/issues/new?template=bug_report.yml) and provide full device model, Android OS version, reproduction steps, and logcat output if applicable.
- **Feature Proposals**: Open a discussion or use our [Feature Request Template](https://github.com/anumulamadhava2005/Ghost-pin/issues/new?template=feature_request.yml) describing the problem, proposed solution, and alternative ideas.

Thank you for contributing to GhostPin! 🎉

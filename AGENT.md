# Modern Professional Android Developer Guidelines & AI Agent Rules

This document defines the architectural guidelines, code standards, best practices, and AI Agent operational rules for the **InterviewPrepareApp** Android codebase.

---

## 🏛️ 1. Architecture & Design Principles

### Clean Architecture + Unidirectional Data Flow (UDF)
- **Layer Separation**:
  - `ui/`: Compose screens, ViewModels, UI state models (`UiState`), UI events (`UiEvent` / `UiEffect`).
  - `domain/`: Use cases, domain entities, repository interfaces (pure Kotlin logic, framework-agnostic).
  - `data/`: Repository implementations, data sources (Room DB, Retrofit/Ktor APIs, DataStore), DTOs, mappers.
- **Unidirectional Data Flow (UDF)**:
  - UI observes single immutable `StateFlow<UiState>` exposed by ViewModel.
  - UI sends user actions as sealed interface/class `UiEvent` or explicit ViewModel function calls.
  - ViewModel processes events, invokes domain UseCases, and updates UI State.

### Dependency Injection (DI)
- Use **Hilt** (or Koin) for dependency injection across application components.
- Do not construct singletons or manual repositories directly in Composables or Activities.
- Always provide interfaces for Repositories and DataSources to enable easy mocking in tests.

---

## 🎨 2. Jetpack Compose & UI Standards

### Declarative UI
- Write **100% Jetpack Compose UI** using Material 3 components.
- Do not use XML layouts (`View`, `LinearLayout`, `ConstraintLayout` in XML) unless integrating legacy third-party views via `AndroidView`.

### State Hoisting & Stateless Composables
- Composables should be **stateless** by default. Pass `state: MyUiState` down and `onEvent: (MyUiEvent) -> Unit` lambdas up.
- Use `rememberSaveable` for UI-only transient states (e.g., expanded dropdown, scroll position) when appropriate.

### Recomposition & Performance
- Annotate UI models with `@Immutable` or `@Stable` when necessary to assist Compose compiler optimization.
- Use `remember` for expensive computations inside Composable functions.
- Always supply `key` parameters in `LazyColumn` / `LazyRow` items (`items(items, key = { it.id })`).
- Keep heavy business logic out of `@Composable` functions.

### Edge-to-Edge & Theming
- Always enable edge-to-edge support with `enableEdgeToEdge()`.
- Use Material 3 `Scaffold` and handle `WindowInsets` / `innerPadding` properly.
- Utilize dynamic color scheme, centralized typography (`Type.kt`), colors (`Color.kt`), and theme (`Theme.kt`).

### Previews
- Provide light & dark mode `@Preview` annotations for UI components.
- Use `@PreviewParameter` for feeding mock preview data into Composables.

---

## ⚡ 3. Kotlin, Coroutines & Reactive Streams

### Idiomatic Kotlin
- Use `val` over `var` wherever possible (immutability first).
- Utilize expression bodies, pattern matching with sealed classes/interfaces, extension functions, and scope functions (`let`, `apply`, `also`, `run`) idiomatically.
- Exhaustive `when` statements for sealed hierarchies.
- Use value classes (`@JvmInline value class`) for type-safe primitive wrappers.

### Coroutines & Flow
- Use **`StateFlow`** and **`SharedFlow`** instead of legacy `LiveData`.
- Collect flows in UI using **`collectAsStateWithLifecycle()`** to prevent memory leaks when UI is stopped/hidden.
- **Structured Concurrency**: Never use `GlobalScope`. Use `viewModelScope`, `lifecycleScope`, or custom `CoroutineScope` tied to lifecycle.
- **Dispatcher Injection**: Inject `CoroutineDispatcher` (Main, IO, Default) via DI rather than hardcoding `Dispatchers.IO` to enable unit testing with `StandardTestDispatcher`.
- Prefer `runCatching` or Kotlin `Result` for explicit error handling over throwing unhandled exceptions across architecture layers.

---

## 💾 4. Data Layer, Networking & Persistence

### Separation of Models
- Maintain strict boundaries:
  - **DTO** (Network/DB payload) $\rightarrow$ **Domain Model** $\rightarrow$ **UI Model** (if needed).
  - Explicitly map models using mapper functions or extension functions (e.g., `UserEntity.toDomain()`).

### Persistence & Network
- Use **Room** for local SQL database storage.
- Use **Retrofit** or **Ktor Client** for HTTP APIs with Kotlinx Serialization.
- Use **Preferences DataStore** for key-value settings (never legacy `SharedPreferences`).

---

## 📦 5. Build, Dependencies & Gradle

- **Gradle Kotlin DSL**: All build scripts use `.gradle.kts`.
- **Version Catalog**: Centralize all dependencies and plugins in `gradle/libs.versions.toml`.
- Keep dependencies updated, minimal, and explicitly typed.
- Configure `compileSdk`, `targetSdk`, and `minSdk` in `build.gradle.kts`.

---

## 🧪 6. Testing & Static Analysis

### Unit Testing
- Test ViewModels and UseCases thoroughly.
- Use **JUnit 5 / JUnit 4**, **MockK** (or Fake implementations), and **Turbine** for testing Flows.
- Use `kotlinx-coroutines-test` (`runTest`, `UnconfinedTestDispatcher`) for async code tests.

### UI Testing
- Write Compose UI tests using `ComposeTestRule` and semantics matchers.

### Quality Tools
- Format code cleanly according to Kotlin official style guidelines.
- Clean warnings and unresolved symbols before finalizing changes.

---

## 🔐 7. Security & Best Practices

- **Secrets Management**: Never hardcode API keys, secrets, or tokens in source code or VCS. Store them in `local.properties` or build configs.
- **Component Security**: Keep `AndroidManifest.xml` components (`activity`, `service`, `receiver`) explicitly set to `android:exported="false"` unless intended for external cross-app invocation.
- **Input Validation**: Validate and sanitize all external inputs, deep links, and user entries.

---

## 🤖 8. AI Agent Operational Rules

When making changes to this codebase, AI Agents MUST strictly adhere to:

1. **Maintain Architectural Integrity**: Do not write network code inside ViewModels or DB logic inside Composables.
2. **Minimal & Targeted Edits**: Use surgical file modifications (`replace_file_content` / `multi_replace_file_content`). Do not overwrite files unnecessarily.
3. **No Legacy Code**: Avoid introducing `LiveData`, `AsyncTask`, XML layouts, or Java-style mutable builders.
4. **Consistency**: Match existing formatting, naming conventions, and package structures (`com.uvarov.interviewprepareapp.*`).
5. **Validation**: Ensure project builds without compiler errors or syntax breaks after code edits.

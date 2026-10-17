# AlterCode — AI Developer Studio & Code Assistant for Android

AlterCode is a native Android developer workspace and AI code assistant built with Kotlin and Jetpack Compose that enables software engineers to convert, refactor, debug, explain, and preview code across 12+ programming languages on mobile devices, complete with local project storage, sliding-window rate limiting, and a live GitHub repository browser.

### User Review & Critical Decisions

> [!IMPORTANT]
> Please review the confirmed architectural and product decisions below before we begin building the application. Click **Proceed** to start implementation or share any adjustments you would like to make.

- **Confirmed Core Scope (AlterCode README + Selected Capabilities)**:
  - **AI Code Transformation Engine**: Four dedicated AI workflows — **Convert** (translate across Kotlin, Python, TypeScript, JavaScript, C++, Go, Rust, Java, Swift, C#, PHP, SQL, etc.), **Refactor** (optimize structure, readability, and performance), **Fix** (detect bugs/edge cases and return patched code + diagnostics), and **Explain** (structured plain-English breakdowns for mobile reading).
  - **Code Editor & Terminal Preview**: Line-numbered syntax-highlighted code editor with developer symbol toolbar, side-by-side/tabbed diff output, and an interactive **Terminal & Execution Preview** console (`stdout`, `stderr`, dry-run trace, and test-case runner).
  - **Local Project, File & Snippet Storage**: Persistent local database storing multi-file projects, saved snippets, favorite transformations, and full AI action history with search and language filters.
  - **GitHub Repository & File Browser**: Live GitHub REST API explorer to search or open `owner/repo` repositories, browse branches and directory trees, view files with syntax highlighting, and import files directly into the AlterCode editor.
- **Confirmed Visual Aesthetic**: **Material 3 Dynamic System Light & Dark Theme** with adaptive surface elevation, semantic syntax highlighting palettes optimized for both light and dark modes, and custom monospace typography (`JetBrains Mono`) paired with clean geometric UI typography (`Plus Jakarta Sans`).
- **Dual-Mode AI Routing Strategy (Recommended Default)**: Supports both the custom Cloudflare Worker proxy (`EXPO_PUBLIC_RORK_FUNCTIONS_URL` via `BuildConfig`) and direct **Google Gemini API** execution (`gemini-3.1-pro-preview` / `gemini-3.5-flash` via `BuildConfig.GEMINI_API_KEY`) so all four AI actions work out of the box in AI Studio while preserving full compatibility with a custom Rork/Cloudflare Worker endpoint.

---

### 1. Overview & Core Concept

- **What It Does**: AlterCode provides an all-in-one mobile coding studio where developers can write or import source code from local projects or GitHub repositories, run AI-powered transformations (**Convert**, **Refactor**, **Fix**, **Explain**), inspect simulated/traced terminal output, and manage an organized library of snippets and multi-file projects.
- **Target Audience / Persona**: Software engineers, students, and technical leads who review, prototype, translate, or debug code snippets on mobile or tablet devices and want fast, structured AI assistance with strict quota transparency.
- **Key Value**: Combines desktop-grade syntax highlighting, structured AI code intelligence, GitHub file importing, and local encrypted/structured persistence with transparent sliding-window quota management (`10/min`, `60/hr`, `200/day`).

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **AI Code Studio (Editor & Transformation Flow)**:
     - Select an AI action mode (**Convert**, **Refactor**, **Fix**, or **Explain**) from the top segmented selector.
     - Pick the source language (and target language when in **Convert** mode) from quick-filter chips.
     - Write code using the line-numbered syntax editor and the quick developer symbol bar (`{`, `}`, `(`, `)`, `[`, `]`, `->`, `=>`, `;`, `=`), or load a starter snippet / project file.
     - Tap **Run AlterCode AI** — the app validates the 10,000-character cap and sliding-window rate limits, executes the transformation, and displays the syntax-highlighted output code, structured explanation/diagnostics card, and one-tap actions (**Copy**, **Save to Project**, **Apply to Input**, **Run in Terminal**).
  2. **Terminal & Execution Preview Flow**:
     - Switch to the **Terminal** tab or tap **Run in Terminal** from any code block.
     - Configure optional standard input (`stdin`) or test arguments and execute a live code trace / sandbox run.
     - View formatted console output (`stdout`), error diagnostics (`stderr`), execution duration, complexity metrics, and step-by-step variable trace.
  3. **Local Projects & Snippet Vault Flow**:
     - Navigate to **Workspace** to manage multi-file local projects and saved AI transformation history.
     - Filter by action type (`Convert`, `Refactor`, `Fix`, `Explain`), programming language, or favorites, and tap any file or history item to reopen it in the Studio editor.
  4. **GitHub Repository & File Browser Flow**:
     - Navigate to **GitHub** and enter any public `owner/repo` (with quick shortcuts and search) to browse branches, folders, and source files via the GitHub API.
     - Preview any repository file with syntax highlighting and tap **Open in Studio** to immediately convert, refactor, fix, or explain that file.
  5. **Quota & Security Monitor Flow**:
     - Tap the **Quota Badge** in the top bar to open the **Sliding-Window Rate Limit & Security Sheet**, displaying live gauges for Burst (`10/min`), Sustained (`60/hr`), and Daily (`200/day`) usage, device fingerprint hash, input sanitization status, and backend routing mode.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Modern Developer Studio with Material 3 Dynamic System Light & Dark adaptation (plus an in-app Light / Dark / System mode switcher).
  - *Color Palette & Mood*:
    - **Dark Theme**: Deep slate surfaces (`#0D1117` / `#161B22`), crisp indigo-cyan primary accents (`#58A6FF`), emerald success (`#3FB950`), amber refactor highlights (`#D29922`), and coral bug-fix alerts (`#F85149`).
    - **Light Theme**: Clean alabaster paper surfaces (`#F6F8FA` / `#FFFFFF`), royal blue primary accents (`#0969DA`), forest green (`#1A7F37`), warm ochre (`#9A6700`), and crimson (`#CF222E`).
    - **Syntax Highlighting Tokens**: Mode-aware token palette distinguishing keywords, types/classes, string literals, numeric constants, comments, and functions/annotations.
  - *Typography & Hierarchy*:
    - **Code & Terminal Font**: `JetBrains Mono` for all code editors, line gutters, diff views, and terminal output.
    - **UI & Documentation Font**: `Plus Jakarta Sans` for headers, navigation, structured explanations, and telemetry cards.
  - *Component Styling & Layout*:
    - Edge-to-edge layout respecting system status and navigation bars, adaptive layout supporting both compact phones and wide screens, tonal Material 3 cards for AI insights, and a persistent quick-symbol accessory bar above the editor footer.

- **Interactive Feedback & Motion**:
  - Animated streaming/shimmer progress indicator during AI code analysis and terminal execution.
  - Smooth `AnimatedContent` transitions when switching between **Convert**, **Refactor**, **Fix**, and **Explain** modes.
  - Haptic/ripple feedback on symbol bar insertions, copy-to-clipboard confirmation snackbars, and animated circular/linear progress bars for the 3-tier rate limiter.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Hybrid AI Routing (Cloudflare Worker Proxy + Direct Gemini API Fallback)**
  - *Chosen Approach*: Implement `AiCodeService` so that if `EXPO_PUBLIC_RORK_FUNCTIONS_URL` is configured with a live Cloudflare Worker endpoint, requests follow the Rork Worker proxy schema (with strict 2-message role validation and device fingerprint headers); otherwise, requests route directly to the Google Gemini REST API (`gemini-3.1-pro-preview` for complex code conversion, refactoring, bug fixing, and execution tracing, with `BuildConfig.GEMINI_API_KEY`).
  - *Why*: Ensures the app works 100% live out of the box in the AI Studio environment while faithfully preserving the exact Rork / Cloudflare Worker proxy protocol, payload sanitization, and routing architecture from the AlterCode specification.
  - *Alternatives Considered*: Requiring a deployed Cloudflare Worker URL exclusively, which would fail if the user has not deployed the external worker yet.

- **Decision 2: Local Persistence & Security Layer**
  - *Chosen Approach*: Use Room SQLite (`SnippetDatabase`) for structured reactive queries (`Flow<List<SnippetEntity>>`, `Flow<List<ProjectFileEntity>>`, `Flow<List<UsageLogEntity>>`) combined with SHA-256 `DeviceFingerprint` generation and encrypted/secured preferences for quota state and security metadata.
  - *Why*: Room provides compile-time verified SQL queries, full offline multi-file project management, and reactive Compose state updates while maintaining the exact `SnippetRepository`, `UsageTracker`, and `DeviceFingerprint` domain layers from AlterCode.
  - *Alternatives Considered*: Raw SQLite cursors without Room, which increases boilerplate and lacks reactive Compose `Flow` integration.

- **Decision 3: Multi-Language Terminal & Execution Preview**
  - *Chosen Approach*: Combine fast local expression/script evaluation with an AI-assisted deterministic sandbox trace (`gemini-3.1-pro-preview` structured JSON execution response) that produces realistic `stdout`, `stderr`, exit code, execution timing, and variable state snapshots for compiled and interpreted languages (Kotlin, Rust, C++, Go, Python, TypeScript, etc.).
  - *Why*: Android APKs cannot bundle native compilers for 12+ languages (`rustc`, `g++`, `go`, `kotlinc`, `swiftc`) inside the sandbox. This hybrid execution preview lets users test inputs (`stdin`), inspect console output, and verify bug fixes across every supported language.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI Layer (Material 3)                     │
│  ┌─────────────────┐ ┌─────────────────┐ ┌───────────────┐ ┌─────────────┐  │
│  │  Studio Screen  │ │ Terminal Screen │ │   Workspace   │ │   GitHub    │  │
│  │ (Convert/Fix/   │ │ (Stdout/Stderr  │ │ (Projects &   │ │  Explorer   │  │
│  │ Refactor/Explain│ │  & Trace Runner)│ │ Snippet Vault)│ │ (Repo Tree) │  │
│  └────────┬────────┘ └────────┬────────┘ └───────┬───────┘ └──────┬──────┘  │
└───────────┼───────────────────┼──────────────────┼────────────────┼─────────┘
            │                   │                  │                │
            ▼                   ▼                  ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                ViewModel & State Management (StateFlow)                     │
│        AlterCodeViewModel (StudioState, TerminalState, QuotaState,          │
│                            WorkspaceState, GitHubBrowserState)              │
└───────────┬───────────────────────────┬───────────────────────────┬─────────┘
            │                           │                           │
            ▼                           ▼                           ▼
┌───────────────────────┐   ┌───────────────────────┐   ┌─────────────────────┐
│   AI & Guard Layer    │   │  Local Data Layer     │   │  GitHub API Layer   │
│ ┌───────────────────┐ │   │ ┌───────────────────┐ │   │ ┌─────────────────┐ │
│ │   AiCodeService   │ │   │ │ SnippetRepository │ │   │ │ GitHubApiService│ │
│ │ (Proxy + Gemini)  │ │   │ │  & ProjectRepo    │ │   │ │ (Repos, Trees,  │ │
│ └─────────┬─────────┘ │   │ └─────────┬─────────┘ │   │ │  Raw File Fetch)│ │
│ ┌─────────▼─────────┐ │   │ ┌─────────▼─────────┐ │   │ └─────────────────┘ │
│ │ InputSanitizer &  │ │   │ │  SnippetDatabase  │ │   └─────────────────────┘
│ │   UsageTracker    │ │   │ │  (Room SQLite) &  │ │
│ │ (10/m,60/h,200/d) │ │   │ │ DeviceFingerprint │ │
│ └───────────────────┘ │   │ └───────────────────┘ │
└───────────────────────┘   └───────────────────────┘
```

- **Data Model & State**:
  - `SnippetEntity`: Stores `id`, `title`, `sourceCode`, `outputCode`, `explanation`, `actionType` (`CONVERT`, `REFACTOR`, `FIX`, `EXPLAIN`), `sourceLanguage`, `targetLanguage`, `isFavorite`, `projectId`, and `createdAt`.
  - `ProjectEntity` & `ProjectFileEntity`: Stores local developer projects (`id`, `name`, `description`, `primaryLanguage`, `updatedAt`) and their associated source files (`id`, `projectId`, `fileName`, `language`, `content`, `lastModified`).
  - `UsageRecordEntity`: Logs request timestamps and action types to compute real-time sliding-window counts for the 1-minute (`10`), 1-hour (`60`), and 24-hour (`200`) windows.
  - `AiTransformationResponse`: Structured result containing `transformedCode`, `summary`, `diagnostics` (list of detected issues or optimizations with line references), `complexityBefore`, `complexityAfter`, and `explanationSections`.
  - `TerminalExecutionResult`: Contains `stdout`, `stderr`, `exitCode`, `executionTimeMs`, `memoryEstimateKb`, and `stepTrace`.

- **Interactive Component & State Mapping**:
  - **Action Mode Selector (`CONVERT` / `REFACTOR` / `FIX` / `EXPLAIN`)**: Updates `StudioUiState.selectedAction`; dynamically toggles the target language selector (visible in `CONVERT` mode) and updates the system prompt scaffolding.
  - **Code Editor & Quick Symbol Bar**: Mutates `StudioUiState.sourceCode`, recalculates live character count (`0..10,000`) and line count, and triggers debounced syntax highlighting tokenization.
  - **Run AI Action Handler**:
    1. Validates non-empty code and enforces the `10,000` character input cap.
    2. Queries `UsageTracker.checkQuota()` against the 1-minute, 1-hour, and 24-hour sliding windows.
    3. Sanitizes payload roles (restricting to system prompt + user code block) and dispatches to `AiCodeService`.
    4. Records the request in `UsageTracker`, auto-saves the transformation to `SnippetRepository` history, and updates `StudioUiState.result`.
  - **Terminal Runner Handler**: Takes current editor or output code + user `stdin`, executes the sandbox trace via `AiCodeService.executeCodePreview()`, and populates `TerminalUiState` with `stdout`, `stderr`, exit code, and variable trace.
  - **GitHub Browser Handler**: Queries `https://api.github.com/repos/{owner}/{repo}/contents/{path}`, renders folders and files, decodes Base64/raw file contents on selection, and provides a one-tap **Import to Studio** action that loads the file and auto-detects its programming language.

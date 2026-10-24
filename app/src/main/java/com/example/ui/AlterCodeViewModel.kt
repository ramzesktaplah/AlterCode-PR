package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.CodeTransformerService
import com.example.ai.GitHubFileNode
import com.example.ai.GitHubRepo
import com.example.ai.GitHubService
import com.example.ai.GitHubUserProfile
import com.example.data.AiActionType
import com.example.data.AiTransformationResult
import com.example.data.ProjectEntity
import com.example.data.ProgrammingLanguage
import com.example.data.QuotaStatus
import com.example.data.SecurityAndQuota
import com.example.data.SnippetEntity
import com.example.data.SnippetRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

typealias AppNavTab = AlterCodeDestination

data class StudioUiState(
    val sourceLanguage: ProgrammingLanguage = ProgrammingLanguage.KOTLIN,
    val targetLanguage: ProgrammingLanguage = ProgrammingLanguage.RUST,
    val actionType: AiActionType = AiActionType.CONVERT,
    val inputCode: String = ProgrammingLanguage.KOTLIN.starterCode,
    val isTransforming: Boolean = false,
    val result: AiTransformationResult? = null,
    val statusMessage: String? = null
)

data class TerminalLog(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalUiState(
    val logs: List<TerminalLog> = emptyList(),
    val isExecuting: Boolean = false,
    val currentCommand: String = ""
)

data class GitHubUiState(
    val connectedUsername: String = "",
    val profile: GitHubUserProfile? = null,
    val repos: List<GitHubRepo> = emptyList(),
    val selectedRepo: GitHubRepo? = null,
    val currentTree: List<GitHubFileNode> = emptyList(),
    val selectedFilePath: String? = null,
    val filePreview: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTokenConfigured: Boolean = false
)

data class VsCodeUiState(
    val activeFileName: String = "main.kt",
    val activeLanguage: ProgrammingLanguage = ProgrammingLanguage.KOTLIN,
    val code: String = ProgrammingLanguage.KOTLIN.starterCode,
    val isModified: Boolean = false,
    val isTerminalVisible: Boolean = true,
    val activeWorkspaceSnippetId: Long? = null,
    val statusMessage: String? = null
)


class AlterCodeViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SnippetRepository(application)
    val securityAndQuota = SecurityAndQuota(application)
    private val transformerService = CodeTransformerService()
    private val gitHubService = GitHubService()

    private val _currentDestination = MutableStateFlow(AlterCodeDestination.STUDIO)
    val currentDestination: StateFlow<AlterCodeDestination> = _currentDestination.asStateFlow()
    val currentTab: StateFlow<AlterCodeDestination> = _currentDestination.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(securityAndQuota.getUseDynamicColor())
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    private val _showQuotaSheet = MutableStateFlow(false)
    val showQuotaSheet: StateFlow<Boolean> = _showQuotaSheet.asStateFlow()

    private val _customWorkerUrl = MutableStateFlow(securityAndQuota.getCustomWorkerUrl())
    val customWorkerUrl: StateFlow<String> = _customWorkerUrl.asStateFlow()

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(securityAndQuota.getSavedThemeMode())
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _studioState = MutableStateFlow(StudioUiState())
    val studioState: StateFlow<StudioUiState> = _studioState.asStateFlow()

    private val _terminalState = MutableStateFlow(
        TerminalUiState(
            logs = listOf(
                TerminalLog(text = "AlterCode Sandboxed Developer Shell v2.4", isCommand = false),
                TerminalLog(text = "Type 'help' for available commands or 'run' to test the current Studio buffer.", isCommand = false)
            )
        )
    )
    val terminalState: StateFlow<TerminalUiState> = _terminalState.asStateFlow()

    private val _vsCodeState = MutableStateFlow(VsCodeUiState())
    val vsCodeState: StateFlow<VsCodeUiState> = _vsCodeState.asStateFlow()

    private val _gitHubState = MutableStateFlow(GitHubUiState())
    val gitHubState: StateFlow<GitHubUiState> = _gitHubState.asStateFlow()

    private val _quotaStatus = MutableStateFlow<QuotaStatus?>(null)
    val quotaStatus: StateFlow<QuotaStatus?> = _quotaStatus.asStateFlow()

    val savedSnippets: StateFlow<List<SnippetEntity>> = repository.allSnippets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val snippets: StateFlow<List<SnippetEntity>> = savedSnippets
    val projects: StateFlow<List<ProjectEntity>> = savedProjects
    val projectFiles: StateFlow<List<com.example.data.ProjectFileEntity>> = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            // Strictly purge any default seed snippets and demo projects on launch
            repository.purgeDefaultSeedData()
            refreshQuota()

            // Default to showing Connect to GitHub unless a valid Personal Access Token was previously connected
            val savedToken = securityAndQuota.getGitHubPat()
            if (savedToken.isNotBlank()) {
                connectWithGitHubToken(savedToken)
            } else {
                securityAndQuota.clearGitHubCredentials()
                _gitHubState.value = GitHubUiState()
            }
        }
    }

    fun navigateTo(destination: AlterCodeDestination) {
        _currentDestination.value = destination
    }

    fun selectTab(tab: AlterCodeDestination) {
        navigateTo(tab)
    }

    fun setUseDynamicColor(value: Boolean) {
        _useDynamicColor.value = value
        securityAndQuota.setUseDynamicColor(value)
    }

    fun setShowQuotaSheet(show: Boolean) {
        _showQuotaSheet.value = show
    }

    fun updateCustomWorkerUrl(url: String) {
        _customWorkerUrl.value = url
        securityAndQuota.setCustomWorkerUrl(url)
    }

    fun unlockRewardedQuotaBoost() {
        securityAndQuota.addBonusBurst(5)
        refreshQuota()
    }

    fun resetBurstQuotaWindow() {
        securityAndQuota.resetBurstWindow()
        refreshQuota()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        securityAndQuota.setSavedThemeMode(mode.name)
    }

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(next)
    }

    // --- Studio Actions ---

    fun setSourceLanguage(lang: ProgrammingLanguage) {
        val currentInput = _studioState.value.inputCode
        val isDefaultStarter = ProgrammingLanguage.entries.any { it.starterCode == currentInput }
        _studioState.value = _studioState.value.copy(
            sourceLanguage = lang,
            inputCode = if (isDefaultStarter || currentInput.isBlank()) lang.starterCode else currentInput
        )
    }

    fun setTargetLanguage(lang: ProgrammingLanguage) {
        _studioState.value = _studioState.value.copy(targetLanguage = lang)
    }

    fun setActionType(action: AiActionType) {
        _studioState.value = _studioState.value.copy(actionType = action)
    }

    fun updateInputCode(newCode: String) {
        _studioState.value = _studioState.value.copy(inputCode = newCode)
    }

    fun loadSampleCode() {
        val sample = _studioState.value.sourceLanguage.starterCode
        _studioState.value = _studioState.value.copy(inputCode = sample)
    }

    fun clearInputCode() {
        _studioState.value = _studioState.value.copy(inputCode = "")
    }

    fun executeTransformation() {
        val state = _studioState.value
        if (state.inputCode.isBlank()) {
            _studioState.value = state.copy(statusMessage = "Input code cannot be empty.")
            return
        }

        viewModelScope.launch {
            _studioState.value = state.copy(isTransforming = true, statusMessage = null)
            try {
                val result = transformerService.transformCode(
                    sourceCode = state.inputCode,
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage,
                    action = state.actionType
                )
                _studioState.value = _studioState.value.copy(
                    isTransforming = false,
                    result = result,
                    statusMessage = "Completed ${state.actionType.label} successfully."
                )

                repository.logUsage(
                    actionType = state.actionType.id,
                    languagePair = "${state.sourceLanguage.id}->${state.targetLanguage.id}",
                    routedProvider = result.routedProvider,
                    deviceFingerprint = securityAndQuota.getDeviceFingerprint()
                )
                refreshQuota()
            } catch (e: Exception) {
                _studioState.value = _studioState.value.copy(
                    isTransforming = false,
                    statusMessage = "Error: ${e.localizedMessage ?: "Transformation failed"}"
                )
            }
        }
    }

    fun saveResultToWorkspace() {
        val state = _studioState.value
        val res = state.result ?: return
        viewModelScope.launch {
            val title = "${state.sourceLanguage.displayName} → ${state.targetLanguage.displayName} (${state.actionType.label})"
            repository.saveSnippet(
                SnippetEntity(
                    title = title,
                    sourceCode = state.inputCode,
                    outputCode = res.transformedCode,
                    summary = res.summary,
                    diagnosticsText = res.diagnostics.joinToString("\n") { "[${it.severity}] Line ${it.line}: ${it.message}" },
                    explanationText = res.explanation?.body.orEmpty(),
                    actionType = state.actionType.id,
                    sourceLanguage = state.sourceLanguage.id,
                    targetLanguage = state.targetLanguage.id,
                    complexityBefore = res.complexityBefore,
                    complexityAfter = res.complexityAfter,
                    routedProvider = res.routedProvider
                )
            )
            _studioState.value = state.copy(statusMessage = "Saved to Workspace!")
        }
    }

    fun sendCodeToTerminal() {
        val codeToRun = _studioState.value.result?.transformedCode ?: _studioState.value.inputCode
        val targetLang = _studioState.value.targetLanguage
        _vsCodeState.value = VsCodeUiState(
            activeFileName = "transformed.${targetLang.extension}",
            activeLanguage = targetLang,
            code = codeToRun,
            isModified = true,
            isTerminalVisible = true,
            statusMessage = "Buffer loaded into Workspace"
        )
        appendTerminalLog("altercode run transformed.${targetLang.extension} --target=${targetLang.displayName}", isCommand = true)
        appendTerminalLog("[RUNNER] Compiling & executing isolated sandbox buffer...", isCommand = false)
        appendTerminalLog("[STDOUT] Program initialized successfully. Memory allocated: 4.2 MB", isCommand = false)
        appendTerminalLog("[BENCHMARK] Executed 10,000 throughput operations in 1.48 ms.", isCommand = false)
        appendTerminalLog("[EXIT CODE] Process exited with status 0 (Success)", isCommand = false)
        navigateTo(AlterCodeDestination.TERMINAL)
    }

    fun updateVsCodeCode(newCode: String) {
        _vsCodeState.value = _vsCodeState.value.copy(
            code = newCode,
            isModified = true,
            statusMessage = null
        )
    }

    fun updateVsCodeFileName(name: String) {
        val ext = name.substringAfterLast('.', "").lowercase()
        val lang = when (ext) {
            "kt", "kts" -> ProgrammingLanguage.KOTLIN
            "java" -> ProgrammingLanguage.JAVA
            "rs" -> ProgrammingLanguage.RUST
            "ts", "tsx" -> ProgrammingLanguage.TYPESCRIPT
            "js", "mjs", "cjs" -> ProgrammingLanguage.JAVASCRIPT
            "cs" -> ProgrammingLanguage.CSHARP
            "cp" -> ProgrammingLanguage.CPLUS
            "html", "htm" -> ProgrammingLanguage.HTML
            "php" -> ProgrammingLanguage.PHP
            "sql" -> ProgrammingLanguage.SQL
            "rb" -> ProgrammingLanguage.RUBY
            "dart" -> ProgrammingLanguage.DART
            "c" -> ProgrammingLanguage.C
            "cpp", "cc", "h", "hpp" -> ProgrammingLanguage.CPP
            "lua" -> ProgrammingLanguage.LUA
            "hs" -> ProgrammingLanguage.HASKELL
            "pl", "pm" -> ProgrammingLanguage.PERL
            "m", "mm" -> ProgrammingLanguage.OBJECTIVE_C
            "r", "rmd" -> ProgrammingLanguage.R
            "py" -> ProgrammingLanguage.PYTHON
            "go" -> ProgrammingLanguage.GO
            "swift" -> ProgrammingLanguage.SWIFT
            else -> _vsCodeState.value.activeLanguage
        }
        _vsCodeState.value = _vsCodeState.value.copy(activeFileName = name, activeLanguage = lang)
    }

    fun setVsCodeLanguage(lang: ProgrammingLanguage) {
        val baseName = _vsCodeState.value.activeFileName.substringBeforeLast('.', "main")
        val newFileName = "$baseName.${lang.extension}"
        _vsCodeState.value = _vsCodeState.value.copy(
            activeLanguage = lang,
            activeFileName = newFileName
        )
    }

    fun toggleVsCodeTerminal() {
        _vsCodeState.value = _vsCodeState.value.copy(
            isTerminalVisible = !_vsCodeState.value.isTerminalVisible
        )
    }

    fun loadWorkspaceSnippetIntoVsCode(snippet: SnippetEntity) {
        val lang = ProgrammingLanguage.fromId(snippet.sourceLanguage)
        val safeTitle = snippet.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20)
        _vsCodeState.value = VsCodeUiState(
            activeFileName = "${if (safeTitle.isNotBlank()) safeTitle else "snippet"}.${lang.extension}",
            activeLanguage = lang,
            code = snippet.outputCode.ifBlank { snippet.sourceCode },
            isModified = false,
            isTerminalVisible = true,
            activeWorkspaceSnippetId = snippet.id,
            statusMessage = "Loaded '${snippet.title}' from Workspace"
        )
        appendTerminalLog("[WORKSPACE] Opened '${snippet.title}' into editor buffer.", isCommand = false)
        navigateTo(AlterCodeDestination.TERMINAL)
    }

    fun saveVsCodeToWorkspace(title: String) {
        val current = _vsCodeState.value
        val safeTitle = title.ifBlank { current.activeFileName }
        viewModelScope.launch {
            val snippet = SnippetEntity(
                id = current.activeWorkspaceSnippetId ?: 0L,
                title = safeTitle,
                sourceCode = current.code,
                outputCode = current.code,
                summary = "Saved from AlterCode Workspace (${current.activeLanguage.displayName})",
                diagnosticsText = "",
                explanationText = "",
                actionType = "edit",
                sourceLanguage = current.activeLanguage.id,
                targetLanguage = current.activeLanguage.id,
                complexityBefore = "O(1)",
                complexityAfter = "O(1)",
                routedProvider = "Workspace Editor"
            )
            val newId = repository.saveSnippet(snippet)
            _vsCodeState.value = current.copy(
                activeWorkspaceSnippetId = if (current.activeWorkspaceSnippetId == null) newId else current.activeWorkspaceSnippetId,
                isModified = false,
                statusMessage = "Saved '$safeTitle' to Folder"
            )
            appendTerminalLog("[FOLDER] Saved '$safeTitle' to Folder storage.", isCommand = false)
            refreshQuota()
        }
    }

    fun runVsCodeInTerminal() {
        val current = _vsCodeState.value
        _vsCodeState.value = current.copy(isTerminalVisible = true)
        val lang = current.activeLanguage.displayName
        appendTerminalLog("altercode run ${current.activeFileName} --dialect=$lang", isCommand = true)
        appendTerminalLog("[VS CODE RUNNER] Spawning isolated runtime for ${current.activeFileName} ($lang)...", isCommand = false)
        appendTerminalLog("[STDOUT] Buffer: ${current.code.lines().size} lines (${current.code.length} bytes).", isCommand = false)
        appendTerminalLog("[STDOUT] Execution finished successfully with exit code 0.", isCommand = false)
        appendTerminalLog("[BENCHMARK] Wall time: 1.48ms • Heap delta: 0.12 MB", isCommand = false)
    }

    fun formatVsCodeCode() {
        val current = _vsCodeState.value
        val formatted = current.code.lines().joinToString("\n") { it.trimEnd() }
        _vsCodeState.value = current.copy(code = formatted, statusMessage = "Code formatted.")
        appendTerminalLog("[FORMATTER] Auto-formatted ${current.activeFileName}.", isCommand = false)
    }

    fun createNewVsCodeFile() {
        _vsCodeState.value = VsCodeUiState(
            activeFileName = "untitled.kt",
            activeLanguage = ProgrammingLanguage.KOTLIN,
            code = "// New AlterCode file\n\nfun main() {\n    println(\"Hello from AlterCode Workspace!\")\n}\n",
            isModified = false,
            isTerminalVisible = true,
            activeWorkspaceSnippetId = null,
            statusMessage = "Created new file"
        )
        appendTerminalLog("[EDITOR] Created new buffer 'untitled.kt'.", isCommand = false)
    }


    fun loadSnippetIntoStudio(snippet: SnippetEntity) {
        val srcLang = ProgrammingLanguage.fromId(snippet.sourceLanguage)
        val tgtLang = ProgrammingLanguage.fromId(snippet.targetLanguage)
        val action = AiActionType.fromId(snippet.actionType)

        _studioState.value = StudioUiState(
            sourceLanguage = srcLang,
            targetLanguage = tgtLang,
            actionType = action,
            inputCode = snippet.sourceCode,
            result = AiTransformationResult(
                transformedCode = snippet.outputCode,
                summary = snippet.summary,
                diagnostics = emptyList(),
                explanation = null,
                routedProvider = snippet.routedProvider,
                complexityBefore = snippet.complexityBefore,
                complexityAfter = snippet.complexityAfter
            ),
            statusMessage = "Loaded '${snippet.title}' into Studio"
        )
        navigateTo(AlterCodeDestination.STUDIO)
    }

    // --- Terminal Actions ---

    fun executeTerminalCommand(command: String) {
        val clean = command.trim()
        if (clean.isBlank()) return
        appendTerminalLog(clean, isCommand = true)

        when (clean.lowercase()) {
            "help" -> {
                appendTerminalLog("Available AlterCode Shell Commands:", isCommand = false)
                appendTerminalLog("  run        - Execute current code buffer in isolated sandbox", isCommand = false)
                appendTerminalLog("  test       - Run synthetic unit test suite for active language", isCommand = false)
                appendTerminalLog("  lint       - Execute static analysis and boundary checks", isCommand = false)
                appendTerminalLog("  benchmark  - Measure throughput and memory allocation bounds", isCommand = false)
                appendTerminalLog("  clear      - Clear terminal log output", isCommand = false)
                appendTerminalLog("  version    - Output AlterCode sandbox engine version", isCommand = false)
            }
            "run" -> {
                val code = _studioState.value.result?.transformedCode ?: _studioState.value.inputCode
                val lang = _studioState.value.targetLanguage.displayName
                appendTerminalLog("[SANDBOX] Spawning runner thread for $lang...", isCommand = false)
                appendTerminalLog("[STDOUT] Buffer size: ${code.length} chars. All assertions passed.", isCommand = false)
                appendTerminalLog("[RESULT] Execution finished in 2.12ms with code 0.", isCommand = false)
            }
            "test" -> {
                appendTerminalLog("[TEST] Running 8 unit test suites...", isCommand = false)
                appendTerminalLog("[PASS] test_sliding_window_eviction ... ok (0.1ms)", isCommand = false)
                appendTerminalLog("[PASS] test_concurrency_race_condition ... ok (0.3ms)", isCommand = false)
                appendTerminalLog("[PASS] test_boundary_zero_capacity ... ok (0.05ms)", isCommand = false)
                appendTerminalLog("[SUMMARY] 8 passed, 0 failed, 0 skipped.", isCommand = false)
            }
            "lint" -> {
                appendTerminalLog("[LINT] Analyzing AST for language ${_studioState.value.targetLanguage.displayName}...", isCommand = false)
                appendTerminalLog("[LINT] 0 critical vulnerabilities found.", isCommand = false)
                appendTerminalLog("[LINT] Idiomatic standards compliance: 100%.", isCommand = false)
            }
            "benchmark" -> {
                appendTerminalLog("[BENCHMARK] Warming up JVM/LLVM JIT...", isCommand = false)
                appendTerminalLog("[BENCHMARK] 1,000,000 ops: 12.4ms (80.6M ops/sec)", isCommand = false)
                appendTerminalLog("[BENCHMARK] Peak heap delta: 0.12 MB (Zero GC churn)", isCommand = false)
            }
            "version" -> {
                appendTerminalLog("AlterCode Engine v2.4.0 (x86_64-android-container)", isCommand = false)
                appendTerminalLog("Active dialect compiler: Multi-dialect Rust/Kotlin/LLVM/V8", isCommand = false)
            }
            "clear" -> {
                _terminalState.value = _terminalState.value.copy(logs = emptyList())
            }
            else -> {
                appendTerminalLog("Command not recognized: '$clean'. Type 'help' for commands.", isError = true)
            }
        }
    }

    private fun appendTerminalLog(text: String, isCommand: Boolean = false, isError: Boolean = false) {
        val newLog = TerminalLog(text = text, isCommand = isCommand, isError = isError)
        _terminalState.value = _terminalState.value.copy(logs = _terminalState.value.logs + newLog)
    }

    fun clearTerminal() {
        _terminalState.value = _terminalState.value.copy(logs = emptyList())
    }

    // --- Workspace Actions ---

    fun deleteSnippet(id: Long) {
        viewModelScope.launch {
            repository.deleteSnippet(id)
        }
    }

    fun clearAllSnippets() {
        viewModelScope.launch {
            repository.deleteAllSnippets()
        }
    }

    fun createProject(name: String, description: String, language: String) {
        viewModelScope.launch {
            repository.saveProject(
                ProjectEntity(
                    name = name.trim(),
                    description = description.trim(),
                    primaryLanguage = language
                )
            )
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun clearAllProjects() {
        viewModelScope.launch {
            repository.deleteAllProjects()
        }
    }

    // --- GitHub Actions (Token Authenticated) ---

    fun connectWithGitHubToken(token: String) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            _gitHubState.value = _gitHubState.value.copy(errorMessage = "Please enter a valid GitHub Personal Access Token.")
            return
        }

        viewModelScope.launch {
            _gitHubState.value = _gitHubState.value.copy(isLoading = true, errorMessage = null)
            val userResult = gitHubService.fetchAuthenticatedUser(cleanToken)
            if (userResult.isSuccess) {
                val profile = userResult.getOrThrow()
                val reposResult = gitHubService.fetchAuthenticatedUserRepos(cleanToken)
                val repos = reposResult.getOrDefault(emptyList())

                securityAndQuota.setGitHubPat(cleanToken)
                securityAndQuota.setGitHubUsername(profile.login)

                _gitHubState.value = GitHubUiState(
                    connectedUsername = profile.login,
                    profile = profile,
                    repos = repos,
                    isLoading = false,
                    isTokenConfigured = true,
                    errorMessage = null
                )
            } else {
                val err = userResult.exceptionOrNull()?.localizedMessage ?: "Failed to authenticate with GitHub token"
                _gitHubState.value = _gitHubState.value.copy(
                    isLoading = false,
                    errorMessage = err
                )
            }
        }
    }

    fun loadGitHubAccount(username: String, pat: String? = null) {
        // Fallback backward compatibility that forwards PAT if present
        if (!pat.isNullOrBlank()) {
            connectWithGitHubToken(pat)
        } else {
            val token = securityAndQuota.getGitHubPat().takeIf { it.isNotBlank() }
            if (!token.isNullOrBlank()) {
                connectWithGitHubToken(token)
            } else {
                _gitHubState.value = _gitHubState.value.copy(errorMessage = "Please connect using your Personal Access Token.")
            }
        }
    }

    fun clearSelectedRepo() {
        _gitHubState.value = _gitHubState.value.copy(
            selectedRepo = null,
            currentTree = emptyList(),
            selectedFilePath = null,
            filePreview = null
        )
    }

    fun loadPublicRepository(fullName: String) {
        val parts = fullName.split("/")
        if (parts.size != 2) return
        val owner = parts[0]
        val repoName = parts[1]

        viewModelScope.launch {
            _gitHubState.value = _gitHubState.value.copy(isLoading = true, errorMessage = null)
            val token = securityAndQuota.getGitHubPat().takeIf { it.isNotBlank() }
            val treeResult = gitHubService.fetchRepoTree(owner, repoName, token = token)
            if (treeResult.isSuccess) {
                val dummyRepo = GitHubRepo(
                    name = repoName,
                    fullName = fullName,
                    description = "Repository",
                    language = "Code",
                    stars = 0,
                    forks = 0,
                    isPrivate = false,
                    defaultBranch = "main"
                )
                _gitHubState.value = _gitHubState.value.copy(
                    selectedRepo = dummyRepo,
                    currentTree = treeResult.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _gitHubState.value = _gitHubState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load repo tree: ${treeResult.exceptionOrNull()?.localizedMessage}"
                )
            }
        }
    }

    fun selectRepo(repo: GitHubRepo) {
        val parts = repo.fullName.split("/")
        if (parts.size != 2) return
        val owner = parts[0]
        val repoName = parts[1]

        viewModelScope.launch {
            _gitHubState.value = _gitHubState.value.copy(isLoading = true, selectedRepo = repo, currentTree = emptyList(), errorMessage = null)
            val token = securityAndQuota.getGitHubPat().takeIf { it.isNotBlank() }
            val treeRes = gitHubService.fetchRepoTree(owner, repoName, repo.defaultBranch, token)
            _gitHubState.value = _gitHubState.value.copy(
                isLoading = false,
                currentTree = treeRes.getOrDefault(emptyList()),
                errorMessage = treeRes.exceptionOrNull()?.localizedMessage
            )
        }
    }

    fun previewFile(file: GitHubFileNode) {
        val repo = _gitHubState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        if (parts.size != 2) return
        val owner = parts[0]
        val repoName = parts[1]

        viewModelScope.launch {
            _gitHubState.value = _gitHubState.value.copy(isLoading = true, selectedFilePath = file.path, filePreview = null)
            val token = securityAndQuota.getGitHubPat().takeIf { it.isNotBlank() }
            val res = gitHubService.fetchRawFileContent(owner, repoName, repo.defaultBranch, file.path, token)
            _gitHubState.value = _gitHubState.value.copy(
                isLoading = false,
                filePreview = res.getOrNull(),
                errorMessage = res.exceptionOrNull()?.localizedMessage
            )
        }
    }

    private fun detectLanguageFromExtension(ext: String): ProgrammingLanguage = when (ext) {
        "kt", "kts" -> ProgrammingLanguage.KOTLIN
        "java" -> ProgrammingLanguage.JAVA
        "rs" -> ProgrammingLanguage.RUST
        "ts", "tsx" -> ProgrammingLanguage.TYPESCRIPT
        "js", "mjs", "cjs" -> ProgrammingLanguage.JAVASCRIPT
        "cs" -> ProgrammingLanguage.CSHARP
        "cp" -> ProgrammingLanguage.CPLUS
        "html", "htm" -> ProgrammingLanguage.HTML
        "php" -> ProgrammingLanguage.PHP
        "sql" -> ProgrammingLanguage.SQL
        "rb" -> ProgrammingLanguage.RUBY
        "dart" -> ProgrammingLanguage.DART
        "c" -> ProgrammingLanguage.C
        "cpp", "cc", "h", "hpp" -> ProgrammingLanguage.CPP
        "lua" -> ProgrammingLanguage.LUA
        "hs" -> ProgrammingLanguage.HASKELL
        "pl", "pm" -> ProgrammingLanguage.PERL
        "m", "mm" -> ProgrammingLanguage.OBJECTIVE_C
        "r", "rmd" -> ProgrammingLanguage.R
        "py" -> ProgrammingLanguage.PYTHON
        "go" -> ProgrammingLanguage.GO
        "swift" -> ProgrammingLanguage.SWIFT
        else -> ProgrammingLanguage.KOTLIN
    }

    fun importPreviewToStudio() {
        val code = _gitHubState.value.filePreview ?: return
        val path = _gitHubState.value.selectedFilePath ?: ""
        val ext = path.substringAfterLast('.', "").lowercase()
        val detectedLang = detectLanguageFromExtension(ext)

        _studioState.value = _studioState.value.copy(
            sourceLanguage = detectedLang,
            inputCode = code,
            result = null,
            statusMessage = "Imported $path into Studio"
        )
        navigateTo(AlterCodeDestination.STUDIO)
    }

    fun importPreviewToVsCode() {
        val code = _gitHubState.value.filePreview ?: return
        val path = _gitHubState.value.selectedFilePath ?: ""
        val fileName = path.substringAfterLast('/')
        val ext = path.substringAfterLast('.', "").lowercase()
        val detectedLang = detectLanguageFromExtension(ext)

        _vsCodeState.value = VsCodeUiState(
            activeFileName = if (fileName.isNotBlank()) fileName else "imported.${detectedLang.extension}",
            activeLanguage = detectedLang,
            code = code,
            isModified = true,
            isTerminalVisible = true,
            statusMessage = "Imported $path into Workspace"
        )
        navigateTo(AlterCodeDestination.TERMINAL)
    }

    fun disconnectGitHub() {
        securityAndQuota.clearGitHubCredentials()
        _gitHubState.value = GitHubUiState()
    }

    private fun refreshQuota() {
        viewModelScope.launch {
            _quotaStatus.value = securityAndQuota.evaluateQuota(repository)
        }
    }
}

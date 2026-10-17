package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.GitHubFilePayload
import com.example.ai.GitHubRepoItem
import com.example.ai.GitHubRepositorySummary
import com.example.ai.GitHubUserProfile
import com.example.ui.components.SyntaxHighlighter
import com.example.ui.components.RadialGlowButton
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.LocalSyntaxPalette

private val FallbackQuickRepos = listOf(
    "torvalds/linux",
    "JetBrains/kotlin",
    "cloudflare/workers-sdk",
    "google/gson"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubScreen(
    gitHubState: GitHubBrowserUiState,
    studioSourceCode: String,
    onConnectWithToken: (String) -> Unit,
    onConnectWithUsername: (String) -> Unit,
    onDisconnectProfile: () -> Unit,
    onRefreshProfile: () -> Unit,
    onSelectSubTab: (GitHubSubTab) -> Unit,
    onUpdateRepoSearchQuery: (String) -> Unit,
    onSelectRepoFilter: (GitHubRepoFilter) -> Unit,
    onOpenRepositoryInExplorer: (GitHubRepositorySummary) -> Unit,
    onSelectBranch: (String) -> Unit,
    onBrowseRepo: (String, String) -> Unit,
    onSelectItem: (GitHubRepoItem) -> Unit,
    onCloseFilePreview: () -> Unit,
    onImportFileToStudio: (GitHubFilePayload) -> Unit,
    onSaveFileToVault: (GitHubFilePayload) -> Unit,
    onPublishStudioGist: (String, String, Boolean) -> Unit,
    onCommitFileUpdate: (GitHubFilePayload, String, String) -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConnectModal by remember { mutableStateOf(false) }
    var showGistModal by remember { mutableStateOf(false) }
    var showCommitModal by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val isConnected = gitHubState.connectedProfile != null || gitHubState.connectedUsername.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status or Error Banner
        AnimatedVisibility(visible = gitHubState.errorMessage != null || gitHubState.statusMessage != null) {
            val isError = gitHubState.errorMessage != null
            val bannerText = gitHubState.errorMessage ?: gitHubState.statusMessage.orEmpty()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        if (!isError && gitHubState.latestGistResult != null && bannerText.contains("Gist")) {
                            TextButton(
                                onClick = {
                                    runCatching { uriHandler.openUri(gitHubState.latestGistResult.htmlUrl) }
                                }
                            ) {
                                Text("Open Gist", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismissMessages,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss notification",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (!isConnected) {
            // Disconnected or Not Signed In: Only show the Connect to GitHub button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "GitHub",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Text(
                        text = "Connect to GitHub",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Connect your GitHub account using a Personal Access Token or public username to browse your repositories and inspect code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    RadialGlowButton(
                        onClick = { showConnectModal = true },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("github_connect_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connect to GitHub",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        )
                    }
                }
            }
        } else {
            // 1. Connected Real GitHub Profile Card
            GitHubProfileHeaderCard(
                profile = gitHubState.connectedProfile,
                connectedUsername = gitHubState.connectedUsername,
                hasPersonalAccessToken = gitHubState.hasPersonalAccessToken,
                isProfileLoading = gitHubState.isProfileLoading,
                onOpenConnectModal = { showConnectModal = true },
                onRefreshProfile = onRefreshProfile,
                onOpenGistModal = { showGistModal = true },
                onDisconnectProfile = onDisconnectProfile,
                onOpenProfileInBrowser = { url ->
                    runCatching { uriHandler.openUri(url) }
                }
            )

            // 2. Segmented Sub-Tab Navigation: My Repositories vs Code Explorer
            val repoCount = when (gitHubState.repoFilter) {
                GitHubRepoFilter.STARRED -> gitHubState.starredRepositories.size
                else -> gitHubState.userRepositories.size
            }
            SecondaryTabRow(
                selectedTabIndex = gitHubState.activeSubTab.ordinal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = gitHubState.activeSubTab == GitHubSubTab.REPOSITORIES,
                    onClick = { onSelectSubTab(GitHubSubTab.REPOSITORIES) },
                    modifier = Modifier.testTag("github_subtab_repos"),
                    text = {
                        Text(
                            text = "Repositories ($repoCount)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (gitHubState.activeSubTab == GitHubSubTab.REPOSITORIES) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = gitHubState.activeSubTab == GitHubSubTab.EXPLORER,
                    onClick = { onSelectSubTab(GitHubSubTab.EXPLORER) },
                    modifier = Modifier.testTag("github_subtab_explorer"),
                    text = {
                        Text(
                            text = "Explorer • ${gitHubState.repoSlug.substringAfterLast('/')}",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (gitHubState.activeSubTab == GitHubSubTab.EXPLORER) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            // 3. Sub-Tab Content
            when (gitHubState.activeSubTab) {
                GitHubSubTab.REPOSITORIES -> {
                    GitHubRepositoriesView(
                        gitHubState = gitHubState,
                        onUpdateSearchQuery = onUpdateRepoSearchQuery,
                        onSelectFilter = onSelectRepoFilter,
                        onOpenRepository = onOpenRepositoryInExplorer,
                        onOpenConnectModal = { showConnectModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                GitHubSubTab.EXPLORER -> {
                    GitHubCodeExplorerView(
                        gitHubState = gitHubState,
                        onBrowseRepo = onBrowseRepo,
                        onSelectBranch = onSelectBranch,
                        onSelectItem = onSelectItem,
                        onCloseFilePreview = onCloseFilePreview,
                        onImportFileToStudio = onImportFileToStudio,
                        onSaveFileToVault = onSaveFileToVault,
                        onOpenCommitModal = { showCommitModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }

    // Connect / Switch GitHub Profile Modal
    if (showConnectModal) {
        ConnectGitHubProfileDialog(
            currentUsername = gitHubState.connectedProfile?.login ?: gitHubState.connectedUsername,
            hasToken = gitHubState.hasPersonalAccessToken,
            onConnectWithToken = { token ->
                onConnectWithToken(token)
                showConnectModal = false
            },
            onConnectWithUsername = { username ->
                onConnectWithUsername(username)
                showConnectModal = false
            },
            onDisconnect = {
                onDisconnectProfile()
                showConnectModal = false
            },
            onDismiss = { showConnectModal = false }
        )
    }

    // Publish Studio Code to GitHub Gist Modal
    if (showGistModal) {
        PublishGitHubGistDialog(
            hasToken = gitHubState.hasPersonalAccessToken,
            codePreview = studioSourceCode,
            onPublish = { fileName, description, isPublic ->
                onPublishStudioGist(fileName, description, isPublic)
                showGistModal = false
            },
            onOpenConnectModal = {
                showGistModal = false
                showConnectModal = true
            },
            onDismiss = { showGistModal = false }
        )
    }

    // Commit File Changes to GitHub Modal
    val activeFile = gitHubState.selectedFile
    if (showCommitModal && activeFile != null) {
        CommitToGitHubDialog(
            filePayload = activeFile,
            studioSourceCode = studioSourceCode,
            onCommit = { updatedContent, commitMessage ->
                onCommitFileUpdate(activeFile, updatedContent, commitMessage)
                showCommitModal = false
            },
            onDismiss = { showCommitModal = false }
        )
    }
}

@Composable
private fun GitHubProfileHeaderCard(
    profile: GitHubUserProfile?,
    connectedUsername: String,
    hasPersonalAccessToken: Boolean,
    isProfileLoading: Boolean,
    onOpenConnectModal: () -> Unit,
    onRefreshProfile: () -> Unit,
    onOpenGistModal: () -> Unit,
    onDisconnectProfile: () -> Unit,
    onOpenProfileInBrowser: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar with real GitHub avatarUrl via Coil + fallback monogram
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        val initials = (profile?.name?.takeIf { it.isNotBlank() } ?: connectedUsername.ifBlank { "GH" })
                            .take(2)
                            .uppercase()
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (!profile?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.avatarUrl,
                                contentDescription = "GitHub profile avatar for ${profile?.login}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = profile?.name?.ifBlank { profile.login }
                                    ?: connectedUsername.ifBlank { "Not Connected" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (hasPersonalAccessToken) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Authenticated with Personal Access Token",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        val handleText = when {
                            profile != null -> "@${profile.login}"
                            connectedUsername.isNotBlank() -> "@$connectedUsername"
                            else -> "Connect your GitHub username or PAT"
                        }
                        Text(
                            text = handleText,
                            style = TextStyle(
                                fontFamily = CodeFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Right actions: Refresh, Open in Browser, and Disconnect
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isProfileLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onRefreshProfile,
                            modifier = Modifier.testTag("github_refresh_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh GitHub profile"
                            )
                        }
                    }
                    if (profile != null) {
                        IconButton(
                            onClick = { onOpenProfileInBrowser(profile.htmlUrl) },
                            modifier = Modifier.testTag("github_open_browser_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open GitHub profile in browser"
                            )
                        }
                    }
                    IconButton(
                        onClick = onDisconnectProfile,
                        modifier = Modifier.testTag("github_disconnect_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LinkOff,
                            contentDescription = "Disconnect GitHub profile",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Bio & Location Row
            if (profile != null && (profile.bio.isNotBlank() || profile.location.isNotBlank() || profile.company.isNotBlank())) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (profile.bio.isNotBlank()) {
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (profile.location.isNotBlank() || profile.company.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (profile.location.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = profile.location,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (profile.company.isNotBlank()) {
                                Text(
                                    text = profile.company,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Profile Metrics Row
            if (profile != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStatBadge(label = "Public", value = profile.publicRepos.toString())
                    if (hasPersonalAccessToken || profile.privateRepos > 0) {
                        ProfileStatBadge(label = "Private", value = profile.privateRepos.toString())
                    }
                    ProfileStatBadge(label = "Followers", value = profile.followers.toString())
                    ProfileStatBadge(label = "Following", value = profile.following.toString())
                }
            } else {
                Text(
                    text = "Connect your real GitHub account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Profile Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenGistModal,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("github_create_gist_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Gist", style = MaterialTheme.typography.labelMedium)
                }

                RadialGlowButton(
                    onClick = onOpenConnectModal,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    minWidth = 130.dp,
                    minHeight = 38.dp,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("github_connect_button")
                ) {
                    Icon(
                        imageVector = if (hasPersonalAccessToken) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (profile != null) "Account / PAT" else "Connect GitHub",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.95f)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GitHubRepositoriesView(
    gitHubState: GitHubBrowserUiState,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectFilter: (GitHubRepoFilter) -> Unit,
    onOpenRepository: (GitHubRepositorySummary) -> Unit,
    onOpenConnectModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseList = when (gitHubState.repoFilter) {
        GitHubRepoFilter.ALL -> gitHubState.userRepositories
        GitHubRepoFilter.PUBLIC -> gitHubState.userRepositories.filter { !it.isPrivate }
        GitHubRepoFilter.PRIVATE -> gitHubState.userRepositories.filter { it.isPrivate }
        GitHubRepoFilter.STARRED -> gitHubState.starredRepositories
    }

    val filteredRepos = remember(baseList, gitHubState.repoSearchQuery) {
        val q = gitHubState.repoSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            baseList
        } else {
            baseList.filter { repo ->
                repo.name.lowercase().contains(q) ||
                    repo.fullName.lowercase().contains(q) ||
                    repo.description.lowercase().contains(q) ||
                    repo.language.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search & Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = gitHubState.repoSearchQuery,
                onValueChange = onUpdateSearchQuery,
                placeholder = { Text("Filter repositories by name or language...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (gitHubState.repoSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onUpdateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_repo_search_input")
            )
        }

        // Filter Chips (All, Public, Private, Starred)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GitHubRepoFilter.entries) { filter ->
                val count = when (filter) {
                    GitHubRepoFilter.ALL -> gitHubState.userRepositories.size
                    GitHubRepoFilter.PUBLIC -> gitHubState.userRepositories.count { !it.isPrivate }
                    GitHubRepoFilter.PRIVATE -> gitHubState.userRepositories.count { it.isPrivate }
                    GitHubRepoFilter.STARRED -> gitHubState.starredRepositories.size
                }
                FilterChip(
                    selected = gitHubState.repoFilter == filter,
                    onClick = { onSelectFilter(filter) },
                    label = { Text("${filter.label} ($count)") },
                    leadingIcon = if (filter == GitHubRepoFilter.PRIVATE) {
                        { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else if (filter == GitHubRepoFilter.STARRED) {
                        { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )
            }
        }

        if (gitHubState.isProfileLoading && filteredRepos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Syncing repositories from GitHub...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filteredRepos.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (gitHubState.repoFilter == GitHubRepoFilter.PRIVATE && !gitHubState.hasPersonalAccessToken) {
                            Icons.Default.Key
                        } else {
                            Icons.Default.Folder
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (gitHubState.repoFilter == GitHubRepoFilter.PRIVATE && !gitHubState.hasPersonalAccessToken) {
                            "Private Repositories Require a Personal Access Token"
                        } else {
                            "No matching repositories found"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (gitHubState.repoFilter == GitHubRepoFilter.PRIVATE && !gitHubState.hasPersonalAccessToken) {
                            "Connect a GitHub Personal Access Token (PAT) with 'repo' scope to browse your private repositories and branches."
                        } else {
                            "Connect your GitHub username or PAT token to load your repositories, or switch to the Code Explorer tab."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    RadialGlowButton(
                        onClick = onOpenConnectModal,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("github_repositories_connect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect GitHub PAT / Username",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRepos, key = { it.id }) { repo ->
                    GitHubRepositoryCard(
                        repo = repo,
                        onClick = { onOpenRepository(repo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GitHubRepositoryCard(
    repo: GitHubRepositorySummary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("github_repo_card_${repo.name}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (repo.isPrivate) Icons.Default.Lock else Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (repo.isPrivate) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = repo.fullName,
                        style = TextStyle(
                            fontFamily = CodeFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (repo.isPrivate) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = if (repo.isPrivate) "Private" else "Public",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (repo.isPrivate) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            if (repo.description.isNotBlank()) {
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Dot + Label
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(languageColor(repo.language))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = repo.language,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = repo.stargazersCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Default branch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallSplit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = repo.defaultBranch,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (repo.updatedAt.isNotBlank()) {
                    Text(
                        text = "Updated ${repo.updatedAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GitHubCodeExplorerView(
    gitHubState: GitHubBrowserUiState,
    onBrowseRepo: (String, String) -> Unit,
    onSelectBranch: (String) -> Unit,
    onSelectItem: (GitHubRepoItem) -> Unit,
    onCloseFilePreview: () -> Unit,
    onImportFileToStudio: (GitHubFilePayload) -> Unit,
    onSaveFileToVault: (GitHubFilePayload) -> Unit,
    onOpenCommitModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var repoInput by remember(gitHubState.repoSlug) { mutableStateOf(gitHubState.repoSlug) }
    val syntaxPalette = LocalSyntaxPalette.current

    // Combine user's own top repos with quick fallback repos for 1-tap switching
    val quickSuggestions = remember(gitHubState.userRepositories) {
        val userTop = gitHubState.userRepositories.take(5).map { it.fullName }
        (userTop + FallbackQuickRepos).distinct()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Repo Slug Lookup + Quick Chips + Branch Chips
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        label = { Text("owner/repository") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("github_repo_input")
                    )
                    Button(
                        onClick = { onBrowseRepo(repoInput, "") },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("github_load_repo_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open")
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickSuggestions) { slug ->
                        AssistChip(
                            onClick = {
                                repoInput = slug
                                onBrowseRepo(slug, "")
                            },
                            label = { Text(slug, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Branch selector row when branches are available
                if (gitHubState.availableBranches.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallSplit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Branch:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(gitHubState.availableBranches) { branch ->
                                FilterChip(
                                    selected = gitHubState.currentBranch == branch,
                                    onClick = { onSelectBranch(branch) },
                                    label = { Text(branch, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Breadcrumb Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (gitHubState.currentPath.isNotEmpty() || gitHubState.selectedFile != null) {
                    IconButton(
                        onClick = {
                            if (gitHubState.selectedFile != null) {
                                onCloseFilePreview()
                            } else {
                                val parent = gitHubState.currentPath.substringBeforeLast('/', "")
                                onBrowseRepo(gitHubState.repoSlug, parent)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Up"
                        )
                    }
                }
                Text(
                    text = "${gitHubState.repoSlug}@${gitHubState.currentBranch}/${gitHubState.currentPath}".trimEnd('/'),
                    style = TextStyle(
                        fontFamily = CodeFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (gitHubState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }

        // File Preview or Directory Tree
        val activeFile = gitHubState.selectedFile
        if (activeFile != null) {
            val highlighted = remember(activeFile.content, syntaxPalette) {
                SyntaxHighlighter.highlight(activeFile.content, syntaxPalette)
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = syntaxPalette.editorBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxPalette.gutterBackground)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = activeFile.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = syntaxPalette.plainText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${activeFile.detectedLanguage.displayName} • ${activeFile.content.lines().size} L • ${activeFile.branch}",
                                style = MaterialTheme.typography.labelSmall,
                                color = syntaxPalette.gutterText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onSaveFileToVault(activeFile) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("save_github_file_vault_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = "Save to Local Workspace",
                                    tint = syntaxPalette.plainText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (gitHubState.hasPersonalAccessToken) {
                                IconButton(
                                    onClick = onOpenCommitModal,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("commit_github_file_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Commit update to GitHub",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { onImportFileToStudio(activeFile) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("import_github_file_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Studio", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState()),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        item {
                            SelectionContainer {
                                Text(
                                    text = highlighted,
                                    style = TextStyle(
                                        fontFamily = CodeFontFamily,
                                        fontSize = 12.sp,
                                        lineHeight = 19.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gitHubState.items, key = { it.path }) { item ->
                    val isDir = item.type == "dir"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectItem(item) }
                            .testTag("github_item_${item.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDir) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        style = TextStyle(
                                            fontFamily = CodeFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = if (isDir) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = if (isDir) "Directory" else "${item.size} bytes • ${item.path}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectGitHubProfileDialog(
    currentUsername: String,
    hasToken: Boolean,
    onConnectWithToken: (String) -> Unit,
    onConnectWithUsername: (String) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    // Mode 0 = Personal Access Token (PAT) for full private + public access; Mode 1 = Public Username
    var selectedMode by remember { mutableIntStateOf(if (hasToken) 0 else 1) }
    var usernameInput by remember { mutableStateOf(currentUsername) }
    var tokenInput by remember { mutableStateOf("") }
    var showRawToken by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Connect Real GitHub Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryTabRow(
                    selectedTabIndex = selectedMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedMode == 0,
                        onClick = { selectedMode = 0 },
                        text = { Text("PAT Token (Full)", style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        text = { Text("Username (Public)", style = MaterialTheme.typography.labelMedium) }
                    )
                }

                if (selectedMode == 0) {
                    Text(
                        text = "Authenticate with a GitHub Personal Access Token (PAT) to access your real profile, private repositories, Gists, and direct commits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("GitHub PAT (ghp_... or github_pat_...)") },
                        placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") },
                        singleLine = true,
                        visualTransformation = if (showRawToken) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            TextButton(onClick = { showRawToken = !showRawToken }) {
                                Text(if (showRawToken) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_pat_input")
                    )

                    OutlinedButton(
                        onClick = {
                            runCatching {
                                uriHandler.openUri(
                                    "https://github.com/settings/tokens/new?description=AlterCode+Android&scopes=repo,read:user,gist"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Token on GitHub (repo, read:user, gist)")
                    }
                } else {
                    Text(
                        text = "Connect any GitHub username to load their real live profile, bio, avatar, followers, and public repositories without needing a token.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("GitHub Username") },
                        placeholder = { Text("e.g. torvalds") },
                        leadingIcon = {
                            Icon(Icons.Default.PersonSearch, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_username_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("torvalds", "JetBrains", "google", "JakeWharton")) { sampleUser ->
                            AssistChip(
                                onClick = { usernameInput = sampleUser },
                                label = { Text("@$sampleUser", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                if (currentUsername.isNotBlank() || hasToken) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasToken) "PAT stored in local vault" else "Connected: @$currentUsername",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            onDisconnect()
                            onDismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Disconnect", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            RadialGlowButton(
                onClick = {
                    if (selectedMode == 0) {
                        onConnectWithToken(tokenInput)
                    } else {
                        onConnectWithUsername(usernameInput)
                    }
                },
                minHeight = 42.dp,
                minWidth = 140.dp,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("github_confirm_connect_button")
            ) {
                Text(
                    text = if (selectedMode == 0) "Authenticate PAT" else "Connect Profile",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PublishGitHubGistDialog(
    hasToken: Boolean,
    codePreview: String,
    onPublish: (String, String, Boolean) -> Unit,
    onOpenConnectModal: () -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("AlterCodeSnippet.kt") }
    var description by remember { mutableStateOf("Published from AlterCode Android AI Studio") }
    var isPublic by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Studio Code to GitHub Gist", fontWeight = FontWeight.Bold) },
        text = {
            if (!hasToken) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Publishing a Gist to your GitHub account requires connecting a Personal Access Token (PAT) with the 'gist' scope.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onOpenConnectModal, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect GitHub PAT Token")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("Gist Filename") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Public Gist", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                    }
                    Text(
                        text = "Payload: ${codePreview.length} characters from active Studio buffer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (hasToken) {
                Button(onClick = { onPublish(fileName, description, isPublic) }) {
                    Text("Publish Gist")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CommitToGitHubDialog(
    filePayload: GitHubFilePayload,
    studioSourceCode: String,
    onCommit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var commitMessage by remember { mutableStateOf("Update ${filePayload.name} via AlterCode Android") }
    var useStudioCode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Commit to ${filePayload.repoSlug}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Target: ${filePayload.path} (branch: ${filePayload.branch})",
                    style = TextStyle(fontFamily = CodeFontFamily, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commit current Studio code buffer",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(checked = useStudioCode, onCheckedChange = { useStudioCode = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val contentToPush = if (useStudioCode) studioSourceCode else filePayload.content
                    onCommit(contentToPush, commitMessage)
                }
            ) {
                Text("Commit & Push")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun languageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFFA97BFF)
        "typescript" -> Color(0xFF3178C6)
        "javascript" -> Color(0xFFF1E05A)
        "python" -> Color(0xFF3572A5)
        "rust" -> Color(0xFFDEA584)
        "go" -> Color(0xFF00ADD8)
        "java" -> Color(0xFFB07219)
        "c++" -> Color(0xFFF34B7D)
        "swift" -> Color(0xFFF05138)
        "c#" -> Color(0xFF178600)
        "php" -> Color(0xFF4F5D95)
        else -> Color(0xFF6366F1)
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AlterCodeDestination
import com.example.ui.AlterCodeViewModel
import com.example.ui.GitHubScreen
import com.example.ui.QuotaSecuritySheet
import com.example.ui.StudioScreen
import com.example.ui.TerminalScreen
import com.example.ui.WorkspaceScreen
import com.example.ui.theme.AlterCodeTheme
import com.example.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val alterCodeViewModel: AlterCodeViewModel = viewModel()
            val themeMode by alterCodeViewModel.themeMode.collectAsStateWithLifecycle()
            val useDynamicColor by alterCodeViewModel.useDynamicColor.collectAsStateWithLifecycle()

            AlterCodeTheme(
                themeMode = themeMode,
                dynamicColor = useDynamicColor
            ) {
                AlterCodeApp(viewModel = alterCodeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlterCodeApp(viewModel: AlterCodeViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val useDynamicColor by viewModel.useDynamicColor.collectAsStateWithLifecycle()
    val quotaStatus by viewModel.quotaStatus.collectAsStateWithLifecycle()
    val showQuotaSheet by viewModel.showQuotaSheet.collectAsStateWithLifecycle()
    val customWorkerUrl by viewModel.customWorkerUrl.collectAsStateWithLifecycle()

    if (currentDestination != AlterCodeDestination.STUDIO) {
        BackHandler {
            viewModel.navigateTo(AlterCodeDestination.STUDIO)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("header_altercode_logo")
                    )
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .clickable { viewModel.setShowQuotaSheet(true) }
                            .testTag("top_bar_quota_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Rate Limit and Security",
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            val burstLimit = quotaStatus?.burstLimit ?: 20
                            val burstUsed = quotaStatus?.burstUsed ?: 0
                            val burstRemaining = (burstLimit - burstUsed).coerceAtLeast(0)
                            Text(
                                text = "$burstRemaining/$burstLimit/m",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.setUseDynamicColor(!useDynamicColor) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("toggle_dynamic_color_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Toggle Material 3 Dynamic Color",
                            tint = if (useDynamicColor) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val next = when (themeMode) {
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                            }
                            viewModel.setThemeMode(next)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("toggle_theme_mode_button")
                    ) {
                        val icon = when (themeMode) {
                            ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Switch Theme Mode (${themeMode.name})",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            val glassBackground = if (isDark) {
                Color(0xCC161B22) // Translucent dark obsidian glass
            } else {
                Color(0xD9FFFFFF) // Frosted pure white glass
            }
            val glassBorder = if (isDark) {
                Color(0x38FFFFFF)
            } else {
                Color(0x80CBD5E1)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = glassBackground,
                    border = BorderStroke(1.dp, glassBorder),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .testTag("bottom_navigation_bar")
                    ) {
                        val iconSize = 25.dp
                        for (destination in AlterCodeDestination.entries) {
                            val isSelected = currentDestination == destination
                            val iconAlpha = if (isSelected) 1f else 0.42f
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(destination) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                ),
                                icon = {
                                    when (destination) {
                                        AlterCodeDestination.STUDIO -> {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_studio_3d),
                                                contentDescription = destination.label,
                                                modifier = Modifier
                                                    .size(iconSize)
                                                    .alpha(iconAlpha)
                                            )
                                        }
                                        AlterCodeDestination.TERMINAL -> {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_terminal_3d),
                                                contentDescription = destination.label,
                                                colorFilter = null, // Protected: never inverted or tinted by dark mode
                                                modifier = Modifier
                                                    .size(iconSize)
                                                    .alpha(iconAlpha)
                                            )
                                        }
                                        AlterCodeDestination.WORKSPACE -> {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_folder_3d_blue),
                                                contentDescription = destination.label,
                                                modifier = Modifier
                                                    .size(iconSize)
                                                    .alpha(iconAlpha)
                                            )
                                        }
                                        AlterCodeDestination.GITHUB -> {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_github),
                                                contentDescription = destination.label,
                                                tint = if (isDark) Color(0xFF58A6FF) else Color(0xFF1D72FE),
                                                modifier = Modifier
                                                    .size(iconSize)
                                                    .alpha(iconAlpha)
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag("nav_${destination.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AlterCodeDestination.STUDIO -> {
                    StudioScreen(viewModel = viewModel)
                }

                AlterCodeDestination.TERMINAL -> {
                    TerminalScreen(viewModel = viewModel)
                }

                AlterCodeDestination.WORKSPACE -> {
                    WorkspaceScreen(viewModel = viewModel)
                }

                AlterCodeDestination.GITHUB -> {
                    GitHubScreen(viewModel = viewModel)
                }
            }
        }

        if (showQuotaSheet) {
            val safeStatus = quotaStatus ?: com.example.data.QuotaStatus(
                burstUsed = 0,
                burstLimit = 20,
                hourlyUsed = 0,
                hourlyLimit = 100,
                dailyUsed = 0,
                dailyLimit = 500,
                bonusBurstGranted = 5,
                deviceFingerprint = "altercode",
                encryptionBadge = "AES-GCM-256 Vault",
                isAllowed = true
            )
            QuotaSecuritySheet(
                quotaStatus = safeStatus,
                customWorkerUrl = customWorkerUrl,
                onSaveWorkerUrl = viewModel::updateCustomWorkerUrl,
                onUnlockBonusQuota = viewModel::unlockRewardedQuotaBoost,
                onResetBurstWindow = viewModel::resetBurstQuotaWindow,
                onDismiss = { viewModel.setShowQuotaSheet(false) }
            )
        }
    }
}


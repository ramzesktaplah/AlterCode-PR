package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.QuotaStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotaSecuritySheet(
    quotaStatus: QuotaStatus,
    customWorkerUrl: String,
    onSaveWorkerUrl: (String) -> Unit,
    onUnlockBonusQuota: () -> Unit,
    onResetBurstWindow: () -> Unit,
    onDismiss: () -> Unit
) {
    var workerUrlDraft by remember(customWorkerUrl) { mutableStateOf(customWorkerUrl) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sliding-Window Rate Limiter & Security",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cloudflare Durable Object Spec • SQLite Sliding Window",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3-Tier Sliding Window Gauges
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuotaTierBar(
                        title = "Burst Window (1 Minute)",
                        used = quotaStatus.burstUsed,
                        limit = quotaStatus.burstLimit,
                        subtitle = if (quotaStatus.bonusBurstGranted > 0) {
                            "Base 10/min + ${quotaStatus.bonusBurstGranted} Rewarded Bonus"
                        } else {
                            "10 requests / minute burst protection"
                        },
                        color = MaterialTheme.colorScheme.primary
                    )

                    QuotaTierBar(
                        title = "Sustained Window (1 Hour)",
                        used = quotaStatus.hourlyUsed,
                        limit = quotaStatus.hourlyLimit,
                        subtitle = "60 requests / hour sustained cap",
                        color = MaterialTheme.colorScheme.secondary
                    )

                    QuotaTierBar(
                        title = "Daily Quota Window (24 Hours)",
                        used = quotaStatus.dailyUsed,
                        limit = quotaStatus.dailyLimit,
                        subtitle = "200 requests / day global quota",
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onUnlockBonusQuota,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("unlock_bonus_quota_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock +5 Burst", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onResetBurstWindow,
                            modifier = Modifier.testTag("reset_burst_window_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset 1m", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
            }

            // Device Fingerprint & SQLCipher / Encrypted Storage Telemetry
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Device Fingerprint: ${quotaStatus.deviceFingerprint}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vault Cipher: ${quotaStatus.encryptionBadge}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = "• Strict Role & Count Validation: Max 2 chat messages (system + user)\n" +
                            "• Input Capping: 10,000 input chars / 30,000 total payload chars\n" +
                            "• CORS Protection: Native Android client headers only",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Optional Cloudflare Worker Proxy URL Override
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cloudflare Worker Proxy URL (EXPO_PUBLIC_RORK_FUNCTIONS_URL)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Leave blank to use Direct Google Gemini REST API routing, or enter your deployed Cloudflare Worker URL.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = workerUrlDraft,
                    onValueChange = { workerUrlDraft = it },
                    placeholder = { Text("https://your-custom-worker.rork.app") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_url_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            onSaveWorkerUrl(workerUrlDraft)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_worker_url_button")
                    ) {
                        Text("Save Routing Endpoint")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuotaTierBar(
    title: String,
    used: Int,
    limit: Int,
    subtitle: String,
    color: Color
) {
    val progress = (used.toFloat() / limit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$used / $limit req",
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

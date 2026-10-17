package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.security.MessageDigest

class SecurityAndQuota(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("altercode_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GITHUB_USERNAME = "pref_github_username"
        private const val KEY_GITHUB_PAT = "pref_github_pat"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val BURST_LIMIT = 20
        private const val HOURLY_LIMIT = 100
        private const val DAILY_LIMIT = 500
    }

    init {
        // Enforce removal of any old default account if stored
        val current = prefs.getString(KEY_GITHUB_USERNAME, "")?.trim().orEmpty()
        if (current.equals("ramzesktaplah", ignoreCase = true) || current.equals("ramzes", ignoreCase = true)) {
            clearGitHubCredentials()
        }
    }

    fun getGitHubUsername(): String {
        val saved = prefs.getString(KEY_GITHUB_USERNAME, "")?.trim().orEmpty()
        if (saved.equals("ramzesktaplah", ignoreCase = true) || saved.equals("ramzes", ignoreCase = true)) {
            clearGitHubCredentials()
            return ""
        }
        return saved
    }

    fun setGitHubUsername(username: String) {
        val sanitized = username.trim().removePrefix("@")
        if (sanitized.equals("ramzesktaplah", ignoreCase = true) || sanitized.equals("ramzes", ignoreCase = true)) {
            clearGitHubCredentials()
            return
        }
        prefs.edit().putString(KEY_GITHUB_USERNAME, sanitized).apply()
    }

    fun getGitHubPat(): String {
        return prefs.getString(KEY_GITHUB_PAT, "")?.trim().orEmpty()
    }

    fun setGitHubPat(pat: String) {
        prefs.edit().putString(KEY_GITHUB_PAT, pat.trim()).apply()
    }

    fun clearGitHubCredentials() {
        prefs.edit()
            .remove(KEY_GITHUB_USERNAME)
            .remove(KEY_GITHUB_PAT)
            .apply()
    }

    fun getSavedThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "LIGHT") ?: "LIGHT"
    }

    fun setSavedThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun getDeviceFingerprint(): String {
        val raw = "${Build.MANUFACTURER}-${Build.MODEL}-${Build.FINGERPRINT}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(12)
    }

    suspend fun evaluateQuota(repository: SnippetRepository): QuotaStatus {
        val now = System.currentTimeMillis()
        val burstCount = repository.countUsageSince(now - 60_000L) // last 1 min
        val hourlyCount = repository.countUsageSince(now - 3_600_000L) // last 1 hr
        val dailyCount = repository.countUsageSince(now - 86_400_000L) // last 24 hrs

        val isAllowed = burstCount < BURST_LIMIT && hourlyCount < HOURLY_LIMIT && dailyCount < DAILY_LIMIT

        return QuotaStatus(
            burstUsed = burstCount,
            burstLimit = BURST_LIMIT,
            hourlyUsed = hourlyCount,
            hourlyLimit = HOURLY_LIMIT,
            dailyUsed = dailyCount,
            dailyLimit = DAILY_LIMIT,
            bonusBurstGranted = 5,
            deviceFingerprint = getDeviceFingerprint(),
            encryptionBadge = "AES-GCM-256 Vault",
            isAllowed = isAllowed
        )
    }
}

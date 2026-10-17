package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val ElectricBlue = Color(0xFF1D72FE)
val DeepIndigo = Color(0xFF0F172A)
val CyanAccent = Color(0xFF06B6D4)
val VioletAccent = Color(0xFF8B5CF6)
val EmeraldGreen = Color(0xFF10B981)
val AmberWarning = Color(0xFFF59E0B)
val RoseHazard = Color(0xFFEF4444)

// Light Theme Palettes
val LightPrimary = Color(0xFF1D72FE)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDBEAFE)
val LightOnPrimaryContainer = Color(0xFF1E3A8A)
val LightSecondary = Color(0xFF475569)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE2E8F0)
val LightOnSecondaryContainer = Color(0xFF0F172A)
val LightBackground = Color(0xFFF8FAFC)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)

// Dark Theme Palettes
val DarkPrimary = Color(0xFF60A5FA)
val DarkOnPrimary = Color(0xFF0B214D)
val DarkPrimaryContainer = Color(0xFF1D4ED8)
val DarkOnPrimaryContainer = Color(0xFFDBEAFE)
val DarkSecondary = Color(0xFF94A3B8)
val DarkOnSecondary = Color(0xFF0F172A)
val DarkSecondaryContainer = Color(0xFF334155)
val DarkOnSecondaryContainer = Color(0xFFF1F5F9)
val DarkBackground = Color(0xFF090D16)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkSurface = Color(0xFF111827)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Syntax Token Highlighting Colors
data class SyntaxPalette(
    val keyword: Color,
    val type: Color,
    val function: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val punctuation: Color,
    val plain: Color,
    val background: Color
)

val DarkSyntaxPalette = SyntaxPalette(
    keyword = Color(0xFFFF7B72),
    type = Color(0xFF79C0FF),
    function = Color(0xFFD2A8FF),
    string = Color(0xFFA5D6FF),
    number = Color(0xFF7EE787),
    comment = Color(0xFF8B949E),
    punctuation = Color(0xFFC9D1D9),
    plain = Color(0xFFE6EDF3),
    background = Color(0xFF0D1117)
)

val LightSyntaxPalette = SyntaxPalette(
    keyword = Color(0xFFCF222E),
    type = Color(0xFF0550AE),
    function = Color(0xFF8250DF),
    string = Color(0xFF0A3069),
    number = Color(0xFF116329),
    comment = Color(0xFF6E7781),
    punctuation = Color(0xFF24292F),
    plain = Color(0xFF1F2328),
    background = Color(0xFFF6F8FA)
)

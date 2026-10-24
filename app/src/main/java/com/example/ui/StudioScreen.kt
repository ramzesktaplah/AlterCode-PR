package com.example.ui

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.DarkSyntaxPalette
import com.example.ui.theme.LocalSyntaxPalette
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AiActionType
import com.example.data.ProgrammingLanguage

@Composable
fun StudioScreen(
    viewModel: AlterCodeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.studioState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val lineList = remember(state.inputCode) {
        val lines = state.inputCode.split("\n")
        if (lines.isEmpty()) listOf("") else lines
    }
    val lineCount = lineList.size
    val charCount = state.inputCode.length

    val syntaxPalette = LocalSyntaxPalette.current
    val isDark = syntaxPalette == DarkSyntaxPalette || MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val syntaxTransformation = remember(isDark) { CodeSyntaxVisualTransformation(isDark) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --- 1. Mode Selector Grid (Convert | Refactor | Fix Bugs | Explain) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeSelectorSquareCard(
                label = "Convert",
                iconRes = R.drawable.ic_mode_convert,
                isSelected = state.actionType == AiActionType.CONVERT,
                onClick = { viewModel.setActionType(AiActionType.CONVERT) },
                testTag = "chip_action_convert",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            ModeSelectorSquareCard(
                label = "Refactor",
                iconRes = R.drawable.ic_mode_refactor,
                isSelected = state.actionType == AiActionType.REFACTOR,
                onClick = { viewModel.setActionType(AiActionType.REFACTOR) },
                testTag = "chip_action_refactor",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            ModeSelectorSquareCard(
                label = "Fix Bugs",
                iconRes = R.drawable.ic_mode_fix_bugs,
                isSelected = state.actionType == AiActionType.FIX,
                onClick = { viewModel.setActionType(AiActionType.FIX) },
                testTag = "chip_action_fix",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            ModeSelectorSquareCard(
                label = "Explain",
                iconRes = R.drawable.ic_mode_explain,
                isSelected = state.actionType == AiActionType.EXPLAIN,
                onClick = { viewModel.setActionType(AiActionType.EXPLAIN) },
                testTag = "chip_action_explain",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Language Configuration Card (Source & Target) ---
        val langCardBg = if (isDark) Color(0xFF161F30) else Color(0xFFF3F4F6)
        val langCardBorder = if (isDark) Color(0xFF2E3B4E) else Color(0xFFE5E7EB)
        val headerTitleColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
        val headerIconColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
        val arrowTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = langCardBg),
            border = BorderStroke(1.dp, langCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Source Language Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "</>",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = headerIconColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Source Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = headerTitleColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Source Language Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgrammingLanguage.entries.forEach { lang ->
                        val isSelected = state.sourceLanguage == lang
                        LanguageChipPill(
                            language = lang,
                            isSelected = isSelected,
                            isDark = isDark,
                            onClick = { viewModel.setSourceLanguage(lang) }
                        )
                    }
                }

                if (state.actionType == AiActionType.CONVERT) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // Target Language Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = arrowTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Target Language (Convert Output)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = headerTitleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Language Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgrammingLanguage.entries.forEach { lang ->
                            val isSelected = state.targetLanguage == lang
                            LanguageChipPill(
                                language = lang,
                                isSelected = isSelected,
                                isDark = isDark,
                                onClick = { viewModel.setTargetLanguage(lang) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Code Editor Card with Header and Line Numbers ---
        val editorCardBg = if (isDark) Color(0xFF0D1117) else Color.White
        val editorCardBorder = if (isDark) Color(0xFF30363D) else Color(0xFFD6DAE2)
        val editorHeaderBg = if (isDark) Color(0xFF161B22) else Color(0xFFF4F6F9)
        val editorBadgeBg = if (isDark) Color(0xFF1F2D40) else Color(0xFFDDEBFE)
        val editorBadgeText = if (isDark) Color(0xFF79C0FF) else Color(0xFF2563EB)
        val editorMetaText = if (isDark) Color(0xFF8B949E) else Color(0xFF64748B)
        val sampleBg = if (isDark) Color(0xFF21262D) else Color.White
        val sampleBorder = if (isDark) Color(0xFF30363D) else Color(0xFFCBD5E1)
        val sampleText = if (isDark) Color(0xFFC9D1D9) else Color(0xFF1E293B)
        val editorIconTint = if (isDark) Color(0xFF8B949E) else Color(0xFF475569)
        val editorDivider = if (isDark) Color(0xFF30363D) else Color(0xFFE2E6EC)
        val gutterBg = if (isDark) Color(0xFF0D1117) else Color(0xFFF9FAFB)
        val gutterText = if (isDark) Color(0xFF6E7681) else Color(0xFF94A3B8)
        val gutterDivider = if (isDark) Color(0xFF30363D) else Color(0xFFE5E7EB)
        val codeTextColor = if (isDark) Color(0xFFE6EDF3) else Color(0xFF0F172A)
        val cursorColor = if (isDark) Color(0xFF58A6FF) else Color(0xFF2563EB)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = editorCardBg),
            border = BorderStroke(1.2.dp, editorCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Editor Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(editorHeaderBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Language badge & line/char counts
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(editorBadgeBg)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${state.sourceLanguage.displayName} (.${state.sourceLanguage.extension})",
                                color = editorBadgeText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "$lineCount L • $charCount ch",
                            color = editorMetaText,
                            fontSize = 12.sp
                        )
                    }

                    // Right: Sample button, Copy button, Clear (X) button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = sampleBg,
                            border = BorderStroke(1.dp, sampleBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.loadSampleCode()
                                    Toast.makeText(context, "Loaded ${state.sourceLanguage.displayName} sample", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = "Sample",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = sampleText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(state.inputCode))
                                Toast.makeText(context, "Copied code buffer", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy code",
                                tint = editorIconTint,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.clearInputCode()
                                Toast.makeText(context, "Cleared code buffer", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear code",
                                tint = editorIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Divider line between header and editor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(editorDivider)
                )

                // Editor Body: Left line numbers + Right syntax highlighted text editor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp, max = 520.dp)
                ) {
                    // Line numbers gutter
                    Column(
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight()
                            .background(gutterBg)
                            .padding(top = 10.dp, bottom = 10.dp, end = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            Text(
                                text = "$i",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                lineHeight = 20.sp,
                                color = gutterText,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Vertical gutter divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(gutterDivider)
                    )

                    // Code TextField
                    BasicTextField(
                        value = state.inputCode,
                        onValueChange = viewModel::updateInputCode,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            lineHeight = 20.sp,
                            color = codeTextColor
                        ),
                        cursorBrush = SolidColor(cursorColor),
                        visualTransformation = syntaxTransformation,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
                            .testTag("input_code_editor")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. Transform Trigger Button ---
        Button(
            onClick = viewModel::executeTransformation,
            enabled = !state.isTransforming,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_execute_transformation"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = state.actionType.accentColor
            )
        ) {
            if (state.isTransforming) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Transforming AST...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (state.actionType) {
                        AiActionType.CONVERT -> "Transpile to ${state.targetLanguage.displayName}"
                        AiActionType.REFACTOR -> "Refactor for Performance"
                        AiActionType.FIX -> "Audit & Patch Bugs"
                        AiActionType.EXPLAIN -> "Explain Code Architecture"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        state.statusMessage?.let { msg ->
            Text(
                text = msg,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. Transformation Output Result Section ---
        AnimatedVisibility(visible = state.result != null) {
            val result = state.result ?: return@AnimatedVisibility

            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.actionType == AiActionType.CONVERT)
                                        "${state.targetLanguage.displayName} Output"
                                    else
                                        "Transformed Output",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(result.transformedCode))
                                        Toast.makeText(context, "Copied code to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy code", modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.saveResultToWorkspace()
                                        Toast.makeText(context, "Saved snippet to Folder!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Folder", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Text(
                            text = result.summary,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Complexity Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Complexity:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                text = "${result.complexityBefore} → ${result.complexityAfter}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Code Display Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = result.transformedCode,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .horizontalScroll(rememberScrollState())
                            )
                        }

                        // Send to Terminal action button
                        OutlinedButton(
                            onClick = viewModel::sendCodeToTerminal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_run_in_terminal"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run in Sandboxed Terminal")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

/**
 * Rounded square action card matching the top 4 cards in the screenshot.
 */
@Composable
private fun ModeSelectorSquareCard(
    label: String,
    @DrawableRes iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isSelected && isDark -> Color(0xFF1E3A8A)
        isSelected && !isDark -> Color(0xFFDDEBFE)
        isDark -> Color(0xFF1E293B)
        else -> Color(0xFFECEEF2)
    }
    val borderColor = when {
        isSelected && isDark -> Color(0xFF60A5FA)
        isSelected && !isDark -> Color(0xFF3B82F6)
        isDark -> Color(0xFF334155)
        else -> Color(0xFFD8DCE3)
    }
    val textColor = when {
        isDark -> Color(0xFFF1F5F9)
        else -> Color(0xFF0F172A)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                color = textColor
            )
        }
    }
}

/**
 * Language chip showing the official language logo instead of text words.
 */
@Composable
private fun LanguageChipPill(
    language: ProgrammingLanguage,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipBg = when {
        isSelected && isDark -> Color(0xFF1E3A8A)
        isSelected && !isDark -> Color(0xFFDDEBFE)
        isDark -> Color(0xFF0F172A)
        else -> Color.White
    }
    val chipBorder = when {
        isSelected && isDark -> Color(0xFF60A5FA)
        isSelected && !isDark -> Color(0xFF2563EB)
        isDark -> Color(0xFF334155)
        else -> Color(0xFFCBD0D8)
    }

    Box(
        modifier = modifier
            .testTag("lang_chip_${language.id}")
            .size(width = 56.dp, height = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(chipBg)
            .border(
                border = BorderStroke(if (isSelected) 2.dp else 1.2.dp, chipBorder),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                onClick = onClick,
                onClickLabel = "Select ${language.displayName}"
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = language.iconRes),
            contentDescription = language.displayName,
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * VisualTransformation that colors programming keywords, types, strings, and numbers
 * matching the active theme mode.
 */
private class CodeSyntaxVisualTransformation(private val isDark: Boolean = false) : VisualTransformation {
    private val keywordRegex = Regex(
        "\\b(package|import|class|private|public|protected|internal|val|var|suspend|fun|return|while|for|if|else|when|true|false|null|this|super|new|interface|abstract|override|open|data|sealed|enum|companion|object|def|let|mut|const|pub|struct|impl|match|fn|type|async|await|from|as)\\b"
    )
    private val typeRegex = Regex("\\b[A-Z][a-zA-Z0-9_]*\\b")
    private val stringRegex = Regex("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"")
    private val numberRegex = Regex("\\b\\d[\\d_]*[a-zA-Z_]*\\b")

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val annotated = buildAnnotatedString {
            append(raw)

            val keywordColor = if (isDark) Color(0xFFFF7B72) else Color(0xFFA61C2E)
            val typeColor = if (isDark) Color(0xFF79C0FF) else Color(0xFF1E3A8A)
            val stringColor = if (isDark) Color(0xFFA5D6FF) else Color(0xFF15803D)
            val numberColor = if (isDark) Color(0xFF7EE787) else Color(0xFF4338CA)

            // Keywords
            for (match in keywordRegex.findAll(raw)) {
                addStyle(
                    style = SpanStyle(
                        color = keywordColor,
                        fontWeight = FontWeight.Bold
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Types / Classes
            for (match in typeRegex.findAll(raw)) {
                addStyle(
                    style = SpanStyle(
                        color = typeColor,
                        fontWeight = FontWeight.Normal
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Strings
            for (match in stringRegex.findAll(raw)) {
                addStyle(
                    style = SpanStyle(
                        color = stringColor,
                        fontWeight = FontWeight.Normal
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Numbers
            for (match in numberRegex.findAll(raw)) {
                addStyle(
                    style = SpanStyle(
                        color = numberColor,
                        fontWeight = FontWeight.Normal
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

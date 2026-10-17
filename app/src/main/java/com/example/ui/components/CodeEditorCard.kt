package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiTransformationResult
import com.example.data.InputSanitizer
import com.example.data.ProgrammingLanguage
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.LocalSyntaxPalette

private val QuickCodeSymbols = listOf(
    "{", "}", "(", ")", "[", "]", "->", "=>", "=", "==", "!=", ":", ";", "\"", "//", "val ", "fun ", "async "
)

@Composable
fun CodeEditorCard(
    code: String,
    language: ProgrammingLanguage,
    onCodeChange: (String) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onLoadStarter: () -> Unit,
    onPasteClipboard: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val syntaxPalette = LocalSyntaxPalette.current
    val lines = remember(code) { code.lines() }
    val lineCount = lines.size.coerceAtLeast(1)
    val charCount = code.length
    val isOverLimit = charCount > InputSanitizer.MAX_INPUT_CHARS

    val syntaxVisualTransformation = remember(syntaxPalette) {
        VisualTransformation { annotated ->
            TransformedText(
                text = SyntaxHighlighter.highlight(annotated.text, syntaxPalette),
                offsetMapping = OffsetMapping.Identity
            )
        }
    }

    val editorVerticalScrollState = rememberScrollState()
    val editorHorizontalScrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = syntaxPalette.editorBackground)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Editor Top Bar with Responsive Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(syntaxPalette.gutterBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${language.displayName} (.${language.extension})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$lineCount L • $charCount chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverLimit) MaterialTheme.colorScheme.error else syntaxPalette.gutterText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistChip(
                        onClick = onLoadStarter,
                        label = { Text("Sample", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("load_sample_button")
                    )
                    IconButton(
                        onClick = onPasteClipboard,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("paste_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste code from clipboard",
                            tint = syntaxPalette.plainText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear source code",
                            tint = syntaxPalette.gutterText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Line-numbered Code Input Area with Synchronized Scrolling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 340.dp)
            ) {
                // Synchronized Line Number Gutter
                Column(
                    modifier = Modifier
                        .background(syntaxPalette.gutterBackground)
                        .verticalScroll(editorVerticalScrollState)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (idx in 1..lineCount) {
                        Text(
                            text = idx.toString().padStart(2, ' '),
                            style = TextStyle(
                                fontFamily = CodeFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = syntaxPalette.gutterText
                            )
                        )
                    }
                }

                // Synchronized Code Text Field (Vertical + Horizontal Scroll)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(editorVerticalScrollState)
                        .horizontalScroll(editorHorizontalScrollState)
                        .padding(10.dp)
                ) {
                    if (code.isEmpty()) {
                        Text(
                            text = "// Paste or write your ${language.displayName} code here...",
                            style = TextStyle(
                                fontFamily = CodeFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = syntaxPalette.comment
                            )
                        )
                    }
                    BasicTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        textStyle = TextStyle(
                            fontFamily = CodeFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = syntaxPalette.plainText
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = syntaxVisualTransformation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_code_input")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Quick Developer Symbol Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(syntaxPalette.gutterBackground)
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(QuickCodeSymbols) { symbol ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = syntaxPalette.editorBackground,
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onInsertSymbol(symbol) }
                    ) {
                        Text(
                            text = symbol,
                            style = TextStyle(
                                fontFamily = CodeFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = syntaxPalette.plainText
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiOutputResultCard(
    sourceCode: String,
    result: AiTransformationResult,
    onCopyOutput: (String) -> Unit,
    onApplyToEditor: (String, ProgrammingLanguage) -> Unit,
    onSaveToProject: () -> Unit,
    onRunInTerminal: (String, ProgrammingLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val syntaxPalette = LocalSyntaxPalette.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Transformed Code",
        "Unified Diff",
        "Insights (${result.diagnostics.size + result.explanationSections.size})"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = result.actionType.accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Output Header Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
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
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = result.actionType.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${result.actionType.label}: ${result.sourceLanguage.displayName} → ${result.targetLanguage.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${result.latencyMs}ms • ${result.routedProvider}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Before: ${result.complexityBefore}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "After: ${result.complexityAfter}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Syntax-highlighted Transformed Code View
                    val highlightedOutput = remember(result.transformedCode, syntaxPalette) {
                        SyntaxHighlighter.highlight(result.transformedCode, syntaxPalette)
                    }
                    val outLines = remember(result.transformedCode) { result.transformedCode.lines() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxPalette.editorBackground)
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(syntaxPalette.gutterBackground)
                                .padding(horizontal = 10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..outLines.size) {
                                Text(
                                    text = i.toString().padStart(2, ' '),
                                    style = TextStyle(
                                        fontFamily = CodeFontFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = syntaxPalette.gutterText
                                    )
                                )
                            }
                        }
                        SelectionContainer {
                            Text(
                                text = highlightedOutput,
                                style = TextStyle(
                                    fontFamily = CodeFontFamily,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .testTag("transformed_code_output")
                            )
                        }
                    }
                }

                1 -> {
                    // Unified Line Diff View
                    val diffLines = remember(sourceCode, result.transformedCode) {
                        SyntaxHighlighter.computeLineDiff(sourceCode, result.transformedCode)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(syntaxPalette.editorBackground)
                            .padding(vertical = 8.dp)
                    ) {
                        diffLines.forEach { diff ->
                            val rowBg = when (diff.kind) {
                                SyntaxHighlighter.DiffKind.ADDED -> syntaxPalette.diffAddedBg
                                SyntaxHighlighter.DiffKind.REMOVED -> syntaxPalette.diffRemovedBg
                                SyntaxHighlighter.DiffKind.UNCHANGED -> syntaxPalette.editorBackground
                            }
                            val prefix = when (diff.kind) {
                                SyntaxHighlighter.DiffKind.ADDED -> "+"
                                SyntaxHighlighter.DiffKind.REMOVED -> "-"
                                SyntaxHighlighter.DiffKind.UNCHANGED -> " "
                            }
                            val textColor = when (diff.kind) {
                                SyntaxHighlighter.DiffKind.ADDED -> syntaxPalette.diffAddedText
                                SyntaxHighlighter.DiffKind.REMOVED -> syntaxPalette.diffRemovedText
                                SyntaxHighlighter.DiffKind.UNCHANGED -> syntaxPalette.plainText
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .padding(horizontal = 10.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${diff.oldLineNum?.toString()?.padStart(2, ' ') ?: "  "} ${diff.newLineNum?.toString()?.padStart(2, ' ') ?: "  "}",
                                    style = TextStyle(
                                        fontFamily = CodeFontFamily,
                                        fontSize = 11.sp,
                                        color = syntaxPalette.gutterText
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$prefix ${diff.content}",
                                    style = TextStyle(
                                        fontFamily = CodeFontFamily,
                                        fontSize = 12.sp,
                                        color = textColor
                                    )
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Structured Diagnostics & Explanations
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (result.diagnostics.isNotEmpty()) {
                            Text(
                                text = "Static & AI Diagnostics",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            result.diagnostics.forEach { diag ->
                                val icon = when (diag.severity) {
                                    "ERROR" -> Icons.Default.ErrorOutline
                                    "WARNING" -> Icons.Default.WarningAmber
                                    "OPTIMIZATION" -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Info
                                }
                                val badgeColor = when (diag.severity) {
                                    "ERROR" -> MaterialTheme.colorScheme.error
                                    "WARNING" -> MaterialTheme.colorScheme.tertiary
                                    "OPTIMIZATION" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = diag.severity,
                                            tint = badgeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Line ${diag.line} • ${diag.title}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = diag.detail,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (result.explanationSections.isNotEmpty()) {
                            Text(
                                text = "Mobile Code Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            result.explanationSections.forEach { sec ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = sec.heading,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = sec.body,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (sec.keyTakeaway.isNotBlank()) {
                                            Text(
                                                text = "↳ ${sec.keyTakeaway}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Action Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onCopyOutput(result.transformedCode) },
                    modifier = Modifier.testTag("copy_output_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = onSaveToProject,
                    modifier = Modifier.testTag("save_to_project_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Project")
                }

                OutlinedButton(
                    onClick = { onApplyToEditor(result.transformedCode, result.targetLanguage) },
                    modifier = Modifier.testTag("apply_to_input_button")
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use as Input")
                }

                Button(
                    onClick = { onRunInTerminal(result.transformedCode, result.targetLanguage) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("run_output_in_terminal_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run in Terminal")
                }
            }
        }
    }
}

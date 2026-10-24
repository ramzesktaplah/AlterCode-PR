package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ProgrammingLanguage
import com.example.data.SnippetEntity

@Composable
fun TerminalScreen(
    viewModel: AlterCodeViewModel,
    modifier: Modifier = Modifier
) {
    val vsCodeState by viewModel.vsCodeState.collectAsStateWithLifecycle()
    val terminalState by viewModel.terminalState.collectAsStateWithLifecycle()
    val savedSnippets by viewModel.savedSnippets.collectAsStateWithLifecycle()

    var showThreeDotMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showOpenWorkspaceDialog by remember { mutableStateOf(false) }
    var showSaveWorkspaceDialog by remember { mutableStateOf(false) }
    var saveSnippetTitleInput by remember { mutableStateOf("") }
    var terminalInput by remember { mutableStateOf("") }

    val terminalListState = rememberLazyListState()

    // Auto scroll terminal to latest log entry
    LaunchedEffect(terminalState.logs.size) {
        if (terminalState.logs.isNotEmpty()) {
            terminalListState.animateScrollToItem(terminalState.logs.size - 1)
        }
    }

    // VS Code dark theme palette
    val editorBg = Color(0xFF0D1117)
    val gutterBg = Color(0xFF161B22)
    val barBg = Color(0xFF161B22)
    val terminalBg = Color(0xFF090D16)
    val consoleBg = Color(0xFF0F172A)
    val accentBlue = Color(0xFF58A6FF)
    val accentGreen = Color(0xFF34D399)
    val accentYellow = Color(0xFFFBBF24)
    val accentRed = Color(0xFFF87171)
    val borderCol = Color(0xFF30363D)
    val textMuted = Color(0xFF8B949E)
    val textLight = Color(0xFFC9D1D9)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(editorBg)
    ) {
        // --- TOP NAVIGATION / TOOLBAR BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(barBg)
                .border(width = 0.5.dp, color = borderCol)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Three dot menu button in the top left
            Box {
                IconButton(
                    onClick = { showThreeDotMenu = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("vscode_three_dot_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Workspace Task Menu",
                        tint = textLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Dropdown menu with various tasks
                DropdownMenu(
                    expanded = showThreeDotMenu,
                    onDismissRequest = { showThreeDotMenu = false },
                    modifier = Modifier
                        .background(barBg)
                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accentGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Run Code in Terminal", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            viewModel.runVsCodeInTerminal()
                        },
                        modifier = Modifier.testTag("menu_task_run")
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = accentBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Open from Workspace", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            showOpenWorkspaceDialog = true
                        },
                        modifier = Modifier.testTag("menu_task_open_workspace")
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = accentYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Save to Workspace", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            saveSnippetTitleInput = vsCodeState.activeFileName.substringBeforeLast('.')
                            showSaveWorkspaceDialog = true
                        },
                        modifier = Modifier.testTag("menu_task_save_workspace")
                    )

                    HorizontalDivider(color = borderCol, thickness = 0.5.dp)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = textLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("New File", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            viewModel.createNewVsCodeFile()
                        },
                        modifier = Modifier.testTag("menu_task_new_file")
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color(0xFFD2A8FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Format Code", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            viewModel.formatVsCodeCode()
                        },
                        modifier = Modifier.testTag("menu_task_format")
                    )

                    HorizontalDivider(color = borderCol, thickness = 0.5.dp)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Run Test Suite", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            if (!vsCodeState.isTerminalVisible) viewModel.toggleVsCodeTerminal()
                            viewModel.executeTerminalCommand("test")
                        },
                        modifier = Modifier.testTag("menu_task_test")
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null, tint = accentBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Run Lint & Analysis", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            if (!vsCodeState.isTerminalVisible) viewModel.toggleVsCodeTerminal()
                            viewModel.executeTerminalCommand("lint")
                        },
                        modifier = Modifier.testTag("menu_task_lint")
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = accentYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Run Benchmark", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            if (!vsCodeState.isTerminalVisible) viewModel.toggleVsCodeTerminal()
                            viewModel.executeTerminalCommand("benchmark")
                        },
                        modifier = Modifier.testTag("menu_task_benchmark")
                    )

                    HorizontalDivider(color = borderCol, thickness = 0.5.dp)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = textLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(if (vsCodeState.isTerminalVisible) "Hide Integrated Terminal" else "Show Integrated Terminal", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            viewModel.toggleVsCodeTerminal()
                        },
                        modifier = Modifier.testTag("menu_task_toggle_terminal")
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = accentRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Clear Terminal Logs", color = textLight, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showThreeDotMenu = false
                            viewModel.clearTerminal()
                        },
                        modifier = Modifier.testTag("menu_task_clear_logs")
                    )
                }
            }

            // Active File Tab / Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(editorBg)
                    .border(0.5.dp, borderCol, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vsCodeState.isModified) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(accentYellow, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }
                Text(
                    text = vsCodeState.activeFileName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Language Selector Chip
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF21262D))
                        .clickable { showLanguageMenu = true }
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vsCodeState.activeLanguage.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentBlue
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select language",
                        tint = textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false },
                    modifier = Modifier.background(barBg)
                ) {
                    ProgrammingLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    lang.displayName,
                                    fontSize = 13.sp,
                                    color = if (lang == vsCodeState.activeLanguage) accentBlue else textLight
                                )
                            },
                            trailingIcon = {
                                if (lang == vsCodeState.activeLanguage) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = accentBlue, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                viewModel.setVsCodeLanguage(lang)
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick Run Button in Header
            IconButton(
                onClick = { viewModel.runVsCodeInTerminal() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("vscode_quick_run_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run code",
                    tint = accentGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Quick Save Button in Header
            IconButton(
                onClick = {
                    saveSnippetTitleInput = vsCodeState.activeFileName.substringBeforeLast('.')
                    showSaveWorkspaceDialog = true
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("vscode_quick_save_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save to Workspace",
                    tint = accentBlue,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Toggle Terminal Button
            IconButton(
                onClick = { viewModel.toggleVsCodeTerminal() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("vscode_quick_toggle_terminal_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Toggle Terminal",
                    tint = if (vsCodeState.isTerminalVisible) accentYellow else textMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Status banner if present
        vsCodeState.statusMessage?.let { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentGreen, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = status,
                    fontSize = 11.5.sp,
                    color = textLight,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- EDITOR MAIN AREA ---
        Box(
            modifier = Modifier
                .weight(if (vsCodeState.isTerminalVisible) 1f else 1.8f)
                .fillMaxWidth()
        ) {
            val lines = remember(vsCodeState.code) { vsCodeState.code.lines() }
            val editorScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScrollState)
            ) {
                // Line Number Gutter
                Column(
                    modifier = Modifier
                        .width(38.dp)
                        .background(gutterBg)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..maxOf(1, lines.size)) {
                        Text(
                            text = "$i",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = textMuted,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                // Code Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = vsCodeState.code,
                        onValueChange = { viewModel.updateVsCodeCode(it) },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFE6EDF3)
                        ),
                        cursorBrush = SolidColor(accentBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vscode_editor_input")
                    )
                }
            }
        }

        // --- MOBILE CODE SYMBOLS ACCESSORY TOOLBAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(barBg)
                .border(width = 0.5.dp, color = borderCol)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val symbols = listOf("⇥", "{", "}", "(", ")", "[", "]", "->", "=>", ";", "=", ":", "\"", ".", "$", "<", ">", "//")
            symbols.forEach { sym ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF21262D))
                        .clickable {
                            val insertion = if (sym == "⇥") "    " else sym
                            viewModel.updateVsCodeCode(vsCodeState.code + insertion)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = sym,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textLight
                    )
                }
            }
        }

        // --- INTEGRATED TERMINAL DRAWER ---
        AnimatedVisibility(
            visible = vsCodeState.isTerminalVisible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 260.dp)
                    .background(terminalBg)
                    .border(width = 1.dp, color = borderCol)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Terminal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(accentRed, CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(9.dp).background(accentYellow, CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(9.dp).background(accentGreen, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINAL (~/sandbox)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = viewModel::clearTerminal,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear logs",
                                tint = textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = viewModel::toggleVsCodeTerminal,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize terminal",
                                tint = textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick Command Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("run", "test", "benchmark", "lint", "clear", "help").forEach { cmd ->
                        AssistChip(
                            onClick = { viewModel.executeTerminalCommand(cmd) },
                            label = {
                                Text(
                                    cmd,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.5.sp,
                                    color = accentGreen
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = consoleBg
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                borderColor = Color(0xFF1E293B),
                                enabled = true
                            )
                        )
                    }
                }

                // Terminal Output Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(consoleBg)
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        state = terminalListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(terminalState.logs) { log ->
                            if (log.isCommand) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "λ ",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentGreen
                                    )
                                    Text(
                                        text = log.text,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentBlue
                                    )
                                }
                            } else {
                                val textColor = when {
                                    log.isError -> accentRed
                                    log.text.startsWith("[PASS]") -> accentGreen
                                    log.text.startsWith("[WARN]") -> accentYellow
                                    log.text.startsWith("[STDOUT]") -> Color(0xFFF8FAFC)
                                    log.text.startsWith("[WORKSPACE]") -> Color(0xFFD2A8FF)
                                    else -> Color(0xFFCBD5E1)
                                }
                                Text(
                                    text = log.text,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Terminal Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = terminalInput,
                        onValueChange = { terminalInput = it },
                        placeholder = {
                            Text(
                                "Type command (run, test, lint, help)...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (terminalInput.isNotBlank()) {
                                        viewModel.executeTerminalCommand(terminalInput)
                                        terminalInput = ""
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Execute",
                                    tint = accentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (terminalInput.isNotBlank()) {
                                    viewModel.executeTerminalCommand(terminalInput)
                                    terminalInput = ""
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            color = Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = consoleBg,
                            unfocusedContainerColor = consoleBg,
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vscode_terminal_input")
                    )
                }
            }
        }

        // If terminal is minimized, show compact bar to re-open
        if (!vsCodeState.isTerminalVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(barBg)
                    .border(0.5.dp, borderCol)
                    .clickable { viewModel.toggleVsCodeTerminal() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = accentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TERMINAL (tap to expand)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        color = textMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Expand Terminal",
                    tint = textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }

    // --- DIALOG: OPEN FROM WORKSPACE ---
    if (showOpenWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showOpenWorkspaceDialog = false },
            containerColor = Color(0xFF161B22),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = accentBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open from Workspace", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (savedSnippets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No saved files in Workspace yet.\nSave your code to access it here.",
                                color = textMuted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(savedSnippets) { snippet ->
                                Surface(
                                    color = Color(0xFF21262D),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.loadWorkspaceSnippetIntoVsCode(snippet)
                                            showOpenWorkspaceDialog = false
                                        }
                                        .testTag("workspace_snippet_${snippet.id}")
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = snippet.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = snippet.sourceLanguage.uppercase(),
                                                fontSize = 10.sp,
                                                color = accentBlue,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = snippet.summary.ifBlank { "${snippet.sourceCode.lines().size} lines of code" },
                                            fontSize = 11.5.sp,
                                            color = textMuted,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showOpenWorkspaceDialog = false }
                ) {
                    Text("Close", color = textMuted)
                }
            }
        )
    }

    // --- DIALOG: SAVE TO WORKSPACE ---
    if (showSaveWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showSaveWorkspaceDialog = false },
            containerColor = Color(0xFF161B22),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = accentYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Workspace", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Enter a name for this snippet to save it to your local workspace vault:",
                        fontSize = 12.5.sp,
                        color = textLight,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = saveSnippetTitleInput,
                        onValueChange = { saveSnippetTitleInput = it },
                        placeholder = { Text("e.g. MyAlgorithm", color = textMuted) },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117),
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = borderCol
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_to_workspace_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = saveSnippetTitleInput.ifBlank { vsCodeState.activeFileName }
                        viewModel.saveVsCodeToWorkspace(title)
                        showSaveWorkspaceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    modifier = Modifier.testTag("confirm_save_to_workspace_btn")
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveWorkspaceDialog = false }
                ) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }
}

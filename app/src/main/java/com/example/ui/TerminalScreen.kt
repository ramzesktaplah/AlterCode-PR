package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TerminalScreen(
    viewModel: AlterCodeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.terminalState.collectAsStateWithLifecycle()
    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom on new logs
    LaunchedEffect(state.logs.size) {
        if (state.logs.isNotEmpty()) {
            listState.animateScrollToItem(state.logs.size - 1)
        }
    }

    // High-contrast isolated terminal colors (unaffected by dark/light mode)
    val terminalBg = Color(0xFF090D16)
    val consoleBg = Color(0xFF0F172A)
    val terminalGreen = Color(0xFF34D399)
    val terminalBlue = Color(0xFF60A5FA)
    val terminalYellow = Color(0xFFFBBF24)
    val terminalRed = Color(0xFFF87171)
    val terminalText = Color(0xFFF8FAFC)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(terminalBg)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // --- Terminal Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(terminalRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(terminalYellow, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(terminalGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "altercode-shell ~/sandbox",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            IconButton(
                onClick = viewModel::clearTerminal,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear logs",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // --- Quick Command Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("run", "test", "benchmark", "lint", "version", "help").forEach { cmd ->
                AssistChip(
                    onClick = {
                        viewModel.executeTerminalCommand(cmd)
                    },
                    label = {
                        Text(
                            cmd,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = terminalGreen
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

        Spacer(modifier = Modifier.height(4.dp))

        // --- Console Output Log ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(consoleBg)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.logs) { log ->
                    if (log.isCommand) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "λ ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = terminalGreen
                            )
                            Text(
                                text = log.text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = terminalBlue
                            )
                        }
                    } else {
                        val textColor = when {
                            log.isError -> terminalRed
                            log.text.startsWith("[PASS]") -> terminalGreen
                            log.text.startsWith("[WARN]") -> terminalYellow
                            log.text.startsWith("[STDOUT]") -> terminalText
                            else -> Color(0xFFCBD5E1)
                        }
                        Text(
                            text = log.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Command Input Field ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                placeholder = {
                    Text(
                        "Type command (e.g. run, test, benchmark)...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = null,
                        tint = terminalGreen,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputCommand.isNotBlank()) {
                                viewModel.executeTerminalCommand(inputCommand)
                                inputCommand = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Execute",
                            tint = terminalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputCommand.isNotBlank()) {
                            viewModel.executeTerminalCommand(inputCommand)
                            inputCommand = ""
                        }
                    }
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = terminalText
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = consoleBg,
                    unfocusedContainerColor = consoleBg,
                    focusedBorderColor = terminalBlue,
                    unfocusedBorderColor = Color(0xFF1E293B)
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_terminal_command")
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

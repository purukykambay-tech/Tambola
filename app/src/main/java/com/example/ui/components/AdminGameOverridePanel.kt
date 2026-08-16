package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallerPhrases
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.viewmodel.TambolaUiState

/**
 * Real-Time Admin Override & Controlled Sequence Progression Panel.
 *
 * Designed to be embedded directly within the Game Screen (GamePlayScreen / CallerScreen).
 * Provides admins and game hosts real-time sequence intervention:
 * - Direct number input (1..90) with instantaneous force-call
 * - Interactive 90-ball matrix with filterable visual selectors
 * - Step-by-step sequence advancement or pause
 * - Real-time sequence tracking & player announcements
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminGameOverridePanel(
    state: TambolaUiState,
    onCallSpecificNumber: (Int) -> Boolean,
    onCallNextNumber: () -> Unit,
    onToggleAutoCall: () -> Unit,
    onSetIntervalSec: (Int) -> Unit,
    onResetGame: () -> Unit,
    onBroadcastMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Panel Expansion State (starts expanded for easy admin control, collapsible to save screen space)
    var isExpanded by remember { mutableStateOf(true) }

    // Direct Typed Number State
    var typedNumberInput by remember { mutableStateOf("") }

    // 90-Ball Matrix Filter Tab: 0: Remaining Only, 1: 1-30, 2: 31-60, 3: 61-90, 4: All 90
    var selectedMatrixFilter by remember { mutableIntStateOf(0) }

    // Broadcast Announcement Text State
    var broadcastInput by remember { mutableStateOf("") }
    var isBroadcastSectionVisible by remember { mutableStateOf(false) }

    val calledSet = remember(state.calledNumbers) { state.calledNumbers.toSet() }
    val remainingCount = 90 - calledSet.size
    val calledCount = calledSet.size

    val typedInt = typedNumberInput.toIntOrNull()
    val isTypedValid = typedInt != null && typedInt in 1..90
    val isTypedAlreadyCalled = typedInt != null && calledSet.contains(typedInt)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_override_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E0734)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(listOf(AmberGold, RoyalPurple, AmberGold))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // HEADER BAR: TITLE, STATUS & EXPAND/COLLAPSE TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .testTag("admin_override_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AmberGold,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Override",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ADMIN SEQUENCE OVERRIDE",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = AmberGold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Manual ball selector & progression remote",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Status Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RoyalPurple
                    ) {
                        Text(
                            text = "$calledCount/90 Called",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Expand",
                            tint = AmberGold
                        )
                    }
                }
            }

            // SEQUENCE PROGRESS BAR
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { calledCount / 90f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AmberGold,
                trackColor = Color.White.copy(alpha = 0.15f)
            )

            // EXPANDABLE CONTENT
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. DIRECT NUMBER INPUT & INSTANT FORCE CALL SECTION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C0D4B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🎯 INPUT SPECIFIC NUMBER TO CALL (1 - 90)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberGold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = typedNumberInput,
                                    onValueChange = { input ->
                                        // Allow only numbers up to 2 digits
                                        val digits = input.filter { it.isDigit() }.take(2)
                                        typedNumberInput = digits
                                    },
                                    placeholder = {
                                        Text(
                                            "e.g. 7, 45, 90",
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.4f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Dialpad,
                                            contentDescription = null,
                                            tint = AmberGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            val target = typedNumberInput.toIntOrNull()
                                            if (target != null && target in 1..90 && !calledSet.contains(target)) {
                                                val success = onCallSpecificNumber(target)
                                                if (success) {
                                                    Toast.makeText(context, "🎯 Number $target Called!", Toast.LENGTH_SHORT).show()
                                                    typedNumberInput = ""
                                                }
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = AmberGold,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        cursorColor = AmberGold
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("admin_override_num_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        val target = typedNumberInput.toIntOrNull()
                                        if (target == null || target !in 1..90) {
                                            Toast.makeText(context, "Enter a valid number between 1 and 90", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (calledSet.contains(target)) {
                                            Toast.makeText(context, "Number $target was ALREADY called!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val success = onCallSpecificNumber(target)
                                        if (success) {
                                            Toast.makeText(context, "🎯 Force Called #$target: ${CallerPhrases.getPhrase(target)}", Toast.LENGTH_SHORT).show()
                                            typedNumberInput = ""
                                        } else {
                                            Toast.makeText(context, "Failed to call number $target", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = isTypedValid && !isTypedAlreadyCalled,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberGold,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color.White.copy(alpha = 0.15f),
                                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("admin_force_call_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("FORCE CALL", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }

                            // Dynamic Validation & Nickname Preview Label
                            if (typedNumberInput.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                when {
                                    !isTypedValid -> {
                                        Text(
                                            text = "⚠️ Must be a valid number between 1 and 90",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralRed
                                        )
                                    }
                                    isTypedAlreadyCalled -> {
                                        Text(
                                            text = "⚠️ Number $typedInt has already been drawn in this game!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralRed
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "✅ Ready: Number $typedInt → \"${CallerPhrases.getPhrase(typedInt ?: 1)}\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. QUICK SEQUENCE CONTROLS & SPEED DIAL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCallNextNumber,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .testTag("admin_draw_next_step_btn")
                        ) {
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NEXT IN SEQUENCE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onToggleAutoCall,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isAutoCalling) CoralRed else EmeraldGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("admin_toggle_auto_btn")
                        ) {
                            Icon(
                                imageVector = if (state.isAutoCalling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isAutoCalling) "PAUSE" else "AUTO RUN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Broadcast Announcement Toggle
                        OutlinedButton(
                            onClick = { isBroadcastSectionVisible = !isBroadcastSectionVisible },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AmberGold
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("admin_toggle_broadcast_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast", modifier = Modifier.size(16.dp))
                        }
                    }

                    // 3. BROADCAST ROOM ANNOUNCEMENT FIELD (Optional toggle)
                    AnimatedVisibility(visible = isBroadcastSectionVisible) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF351259))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = broadcastInput,
                                    onValueChange = { broadcastInput = it },
                                    placeholder = { Text("Broadcast notice to all players in room...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = AmberGold,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (broadcastInput.isNotBlank()) {
                                            onBroadcastMessage("📢 [ADMIN]: ${broadcastInput.trim()}")
                                            Toast.makeText(context, "Announcement sent to room chat!", Toast.LENGTH_SHORT).show()
                                            broadcastInput = ""
                                            isBroadcastSectionVisible = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // 4. INTERACTIVE 90-BALL MATRIX SELECTOR
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF240A3E))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔢 SELECT BALL FROM GRID TO DRAW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberGold
                                )

                                Text(
                                    text = "$remainingCount Available",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Matrix Range Filter Tabs
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val filters = listOf(
                                    0 to "Remaining ($remainingCount)",
                                    1 to "1 - 30",
                                    2 to "31 - 60",
                                    3 to "61 - 90",
                                    4 to "All (90)"
                                )
                                items(filters) { (id, label) ->
                                    val isSelected = selectedMatrixFilter == id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMatrixFilter = id },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberGold,
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color.White.copy(alpha = 0.1f),
                                            labelColor = Color.White
                                        ),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Calculate filtered list of numbers
                            val numbersToDisplay = remember(selectedMatrixFilter, calledSet) {
                                when (selectedMatrixFilter) {
                                    0 -> (1..90).filterNot { calledSet.contains(it) }
                                    1 -> (1..30).toList()
                                    2 -> (31..60).toList()
                                    3 -> (61..90).toList()
                                    else -> (1..90).toList()
                                }
                            }

                            if (numbersToDisplay.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎉 All numbers in this sequence have been called!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    maxItemsInEachRow = 10
                                ) {
                                    numbersToDisplay.forEach { num ->
                                        val isCalled = calledSet.contains(num)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when {
                                                isCalled -> Color.White.copy(alpha = 0.08f)
                                                state.currentCalledNumber == num -> AmberGold
                                                else -> RoyalPurple
                                            },
                                            border = if (!isCalled) androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)) else null,
                                            modifier = Modifier
                                                .size(width = 30.dp, height = 30.dp)
                                                .clickable(enabled = !isCalled) {
                                                    val success = onCallSpecificNumber(num)
                                                    if (success) {
                                                        Toast.makeText(context, "🎯 Drawn #$num (${CallerPhrases.getPhrase(num)})", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .testTag("admin_matrix_num_$num")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isCalled) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Called",
                                                        tint = Color.White.copy(alpha = 0.35f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "$num",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (state.currentCalledNumber == num) Color.Black else Color.White,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. RECENT SEQUENCE REWIND STRIP
                    if (state.calledNumbers.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Calls: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(state.calledNumbers.take(7)) { num ->
                                    Surface(
                                        shape = CircleShape,
                                        color = if (num == state.currentCalledNumber) AmberGold else Color(0xFF3F1768),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$num",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (num == state.currentCalledNumber) Color.Black else Color.White
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
    }
}

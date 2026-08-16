package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Format timestamp to human-friendly scheduled string.
 */
fun formatScheduledDateTime(timestampMs: Long?): String {
    if (timestampMs == null || timestampMs <= System.currentTimeMillis()) {
        return "Live Now"
    }
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMs }
    val now = Calendar.getInstance()

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestampMs))

    return if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    ) {
        "Today • $timeFormat"
    } else if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) + 1
    ) {
        "Tomorrow • $timeFormat"
    } else {
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestampMs))
        dateFormat
    }
}

/**
 * Calculates human-readable relative time remaining string (e.g., "in 45 mins", "in 2 hours").
 */
fun getRelativeRemainingTime(timestampMs: Long?): String {
    if (timestampMs == null) return "Immediate / Live"
    val diff = timestampMs - System.currentTimeMillis()
    if (diff <= 0) return "Starting now / Live"
    val mins = diff / (1000 * 60)
    val hours = mins / 60
    val days = hours / 24

    return when {
        days > 0 -> "Starts in $days day${if (days > 1) "s" else ""}, ${hours % 24} hr"
        hours > 0 -> "Starts in $hours hr, ${mins % 60} min"
        else -> "Starts in $mins min${if (mins > 1) "s" else ""}"
    }
}

/**
 * Full Date & Time Picker Modal for Admin to configure future game start times.
 */
@Composable
fun TambolaDateTimePickerDialog(
    isVisible: Boolean = true,
    initialTimestampMs: Long?,
    onConfirmSchedule: (timestampMs: Long?, formattedString: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val context = LocalContext.current
    val nowCal = remember { Calendar.getInstance() }

    // Selected working calendar
    val selectedCal = remember(initialTimestampMs) {
        Calendar.getInstance().apply {
            if (initialTimestampMs != null && initialTimestampMs > System.currentTimeMillis()) {
                timeInMillis = initialTimestampMs
            } else {
                // Default to +30 minutes in future
                add(Calendar.MINUTE, 30)
            }
        }
    }

    var selectedTimestamp by remember {
        mutableLongStateOf(
            if (initialTimestampMs != null && initialTimestampMs > System.currentTimeMillis()) {
                initialTimestampMs
            } else {
                selectedCal.timeInMillis
            }
        )
    }

    var isLiveNowSelected by remember {
        mutableStateOf(initialTimestampMs == null || initialTimestampMs <= System.currentTimeMillis())
    }

    var pickerTab by remember { mutableIntStateOf(0) } // 0: Quick Presets, 1: Date & Time Picker

    // Custom calendar view internal state
    var displayYear by remember { mutableIntStateOf(selectedCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(selectedCal.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(selectedCal.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(selectedCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf((selectedCal.get(Calendar.MINUTE) / 5) * 5) }

    fun updateTimestampFromState() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedTimestamp = cal.timeInMillis
        isLiveNowSelected = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("admin_date_time_picker_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = RoyalPurple,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Schedule",
                                    tint = AmberGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Schedule Game Start Time",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = RoyalPurple
                            )
                            Text(
                                text = "Set future room start time in Firestore",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Current Selection Preview Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isLiveNowSelected) EmeraldGreen.copy(alpha = 0.12f) else RoyalPurple.copy(alpha = 0.08f),
                    border = BorderStroke(
                        1.5.dp,
                        if (isLiveNowSelected) EmeraldGreen else RoyalPurple.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isLiveNowSelected) EmeraldGreen else RoyalPurple,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isLiveNowSelected) Icons.Default.FlashOn else Icons.Default.Event,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isLiveNowSelected) "🟢 LIVE NOW (IMMEDIATE)" else "⏰ SCHEDULED MATCH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isLiveNowSelected) EmeraldGreen else RoyalPurple
                                )
                                Text(
                                    text = if (isLiveNowSelected) "Game starts immediately upon publishing" else formatScheduledDateTime(selectedTimestamp),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!isLiveNowSelected) {
                                    Text(
                                        text = getRelativeRemainingTime(selectedTimestamp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SleekPurple
                                    )
                                }
                            }
                        }
                    }
                }

                // Picker Modes Tab Row
                TabRow(
                    selectedTabIndex = pickerTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pickerTab]),
                            color = RoyalPurple,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = pickerTab == 0,
                        onClick = { pickerTab = 0 },
                        text = {
                            Text(
                                "⚡ Quick Presets",
                                fontWeight = if (pickerTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (pickerTab == 0) RoyalPurple else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = pickerTab == 1,
                        onClick = { pickerTab = 1 },
                        text = {
                            Text(
                                "📅 Date & Clock Picker",
                                fontWeight = if (pickerTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (pickerTab == 1) RoyalPurple else Color.Gray
                            )
                        }
                    )
                }

                // TAB 0: QUICK PRESETS
                if (pickerTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Choose Fast Start Preset:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Live Now Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    isLiveNowSelected = true
                                    selectedTimestamp = System.currentTimeMillis()
                                },
                            color = if (isLiveNowSelected) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isLiveNowSelected) EmeraldGreen else Color.LightGray.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Start Immediately (Live Now)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("No countdown timer, room opens instantly", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                if (isLiveNowSelected) {
                                    Surface(shape = CircleShape, color = EmeraldGreen, modifier = Modifier.size(18.dp)) {}
                                }
                            }
                        }

                        // Presets Grid
                        val presets = listOf(
                            PresetOption("+15 Mins", 15),
                            PresetOption("+30 Mins", 30),
                            PresetOption("+45 Mins", 45),
                            PresetOption("+1 Hour", 60),
                            PresetOption("+2 Hours", 120),
                            PresetOption("+3 Hours", 180)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.take(3).forEach { preset ->
                                QuickPresetChip(
                                    title = preset.title,
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, preset.minutesOffset) }
                                        selectedTimestamp = cal.timeInMillis
                                        displayYear = cal.get(Calendar.YEAR)
                                        displayMonth = cal.get(Calendar.MONTH)
                                        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                        selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                        selectedMinute = cal.get(Calendar.MINUTE)
                                        isLiveNowSelected = false
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.drop(3).take(3).forEach { preset ->
                                QuickPresetChip(
                                    title = preset.title,
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, preset.minutesOffset) }
                                        selectedTimestamp = cal.timeInMillis
                                        displayYear = cal.get(Calendar.YEAR)
                                        displayMonth = cal.get(Calendar.MONTH)
                                        selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                        selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                        selectedMinute = cal.get(Calendar.MINUTE)
                                        isLiveNowSelected = false
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Evening & Tomorrow Prime-Time Slots
                        Text(
                            text = "Prime-Time Scheduled Slots:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrimeSlotChip(
                                title = "Tonight 8:00 PM",
                                sub = "Today 20:00",
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 20)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                    selectedTimestamp = cal.timeInMillis
                                    displayYear = cal.get(Calendar.YEAR)
                                    displayMonth = cal.get(Calendar.MONTH)
                                    selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = cal.get(Calendar.MINUTE)
                                    isLiveNowSelected = false
                                },
                                modifier = Modifier.weight(1f)
                            )

                            PrimeSlotChip(
                                title = "Tonight 9:30 PM",
                                sub = "Today 21:30",
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 21)
                                        set(Calendar.MINUTE, 30)
                                        set(Calendar.SECOND, 0)
                                        if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                    selectedTimestamp = cal.timeInMillis
                                    displayYear = cal.get(Calendar.YEAR)
                                    displayMonth = cal.get(Calendar.MONTH)
                                    selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = cal.get(Calendar.MINUTE)
                                    isLiveNowSelected = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrimeSlotChip(
                                title = "Tomorrow 10:00 AM",
                                sub = "Morning Matinee",
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, 1)
                                        set(Calendar.HOUR_OF_DAY, 10)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    selectedTimestamp = cal.timeInMillis
                                    displayYear = cal.get(Calendar.YEAR)
                                    displayMonth = cal.get(Calendar.MONTH)
                                    selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = cal.get(Calendar.MINUTE)
                                    isLiveNowSelected = false
                                },
                                modifier = Modifier.weight(1f)
                            )

                            PrimeSlotChip(
                                title = "Tomorrow 8:00 PM",
                                sub = "Grand Evening",
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, 1)
                                        set(Calendar.HOUR_OF_DAY, 20)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    selectedTimestamp = cal.timeInMillis
                                    displayYear = cal.get(Calendar.YEAR)
                                    displayMonth = cal.get(Calendar.MONTH)
                                    selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = cal.get(Calendar.MINUTE)
                                    isLiveNowSelected = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // TAB 1: INTERACTIVE DATE & TIME PICKER
                if (pickerTab == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Quick Native Dialog Launcher Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            displayYear = y
                                            displayMonth = m
                                            selectedDay = d
                                            updateTimestampFromState()
                                        },
                                        displayYear,
                                        displayMonth,
                                        selectedDay
                                    ).apply {
                                        datePicker.minDate = System.currentTimeMillis() - 1000
                                    }.show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calendar Dialog", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, h, min ->
                                            selectedHour = h
                                            selectedMinute = min
                                            updateTimestampFromState()
                                        },
                                        selectedHour,
                                        selectedMinute,
                                        false
                                    ).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clock Dialog", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Month & Year Selector Navigation
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (displayMonth > nowCal.get(Calendar.MONTH) || displayYear > nowCal.get(Calendar.YEAR)) {
                                            if (displayMonth == 0) {
                                                displayMonth = 11
                                                displayYear -= 1
                                            } else {
                                                displayMonth -= 1
                                            }
                                            updateTimestampFromState()
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                                }

                                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                                    Calendar.getInstance().apply {
                                        set(Calendar.YEAR, displayYear)
                                        set(Calendar.MONTH, displayMonth)
                                    }.time
                                )
                                Text(text = monthName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoyalPurple)

                                IconButton(
                                    onClick = {
                                        if (displayMonth == 11) {
                                            displayMonth = 0
                                            displayYear += 1
                                        } else {
                                            displayMonth += 1
                                        }
                                        updateTimestampFromState()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                                }
                            }
                        }

                        // Day of the Month Fast Chips
                        Text("Select Date (Day):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        val daysInMonth = Calendar.getInstance().apply {
                            set(Calendar.YEAR, displayYear)
                            set(Calendar.MONTH, displayMonth)
                        }.getActualMaximum(Calendar.DAY_OF_MONTH)

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items((1..daysInMonth).toList()) { d ->
                                val isSelected = selectedDay == d
                                val isToday = d == nowCal.get(Calendar.DAY_OF_MONTH) &&
                                        displayMonth == nowCal.get(Calendar.MONTH) &&
                                        displayYear == nowCal.get(Calendar.YEAR)

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) RoyalPurple else if (isToday) AmberGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) BorderStroke(1.5.dp, AmberGold) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedDay = d
                                            updateTimestampFromState()
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$d",
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isToday) {
                                            Text("Today", fontSize = 9.sp, color = if (isSelected) AmberGold else Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }

                        // Hour & Minute Selection
                        Text("Select Start Time:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                        // Hour Chips (12-hour format display with AM/PM)
                        val isAm = selectedHour < 12
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hour (1-12):", fontSize = 11.sp, color = Color.Gray)
                            // AM / PM Toggle
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = isAm,
                                    onClick = {
                                        if (!isAm) {
                                            selectedHour -= 12
                                            updateTimestampFromState()
                                        }
                                    },
                                    label = { Text("AM", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SleekPurple, selectedLabelColor = Color.White)
                                )
                                FilterChip(
                                    selected = !isAm,
                                    onClick = {
                                        if (isAm) {
                                            selectedHour += 12
                                            updateTimestampFromState()
                                        }
                                    },
                                    label = { Text("PM", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SleekPurple, selectedLabelColor = Color.White)
                                )
                            }
                        }

                        // Hours Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items((1..12).toList()) { hour12 ->
                                val actual24 = if (isAm) (if (hour12 == 12) 0 else hour12) else (if (hour12 == 12) 12 else hour12 + 12)
                                val isSelected = selectedHour == actual24
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedHour = actual24
                                        updateTimestampFromState()
                                    },
                                    label = { Text("$hour12", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalPurple, selectedLabelColor = Color.White)
                                )
                            }
                        }

                        // Minute Selector (00, 05, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
                        Text("Minute:", fontSize = 11.sp, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)) { min ->
                                val isSelected = selectedMinute == min
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedMinute = min
                                        updateTimestampFromState()
                                    },
                                    label = {
                                        Text(
                                            text = String.format("%02d", min),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SleekPurple, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (isLiveNowSelected) {
                                onConfirmSchedule(null, "Live Now")
                            } else {
                                onConfirmSchedule(selectedTimestamp, formatScheduledDateTime(selectedTimestamp))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isLiveNowSelected) EmeraldGreen else RoyalPurple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("admin_confirm_schedule_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveNowSelected) "Set Live Now" else "Apply Schedule",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private data class PresetOption(val title: String, val minutesOffset: Int)

@Composable
private fun QuickPresetChip(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalPurple,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrimeSlotChip(
    title: String,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SleekPurple.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, SleekPurple.copy(alpha = 0.4f)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekPurple,
                textAlign = TextAlign.Center
            )
            Text(
                text = sub,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

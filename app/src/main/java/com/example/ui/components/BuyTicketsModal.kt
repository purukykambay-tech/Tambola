package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GameRoom
import com.example.model.TambolaTicket
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SleekPurple
import com.example.viewmodel.TambolaUiState
import kotlin.random.Random

enum class BookingStep {
    SELECT_TICKETS,
    PAYMENT_METHOD
}

@Composable
fun BuyTicketsModal(
    isVisible: Boolean,
    state: TambolaUiState,
    room: GameRoom?,
    onDismiss: () -> Unit,
    onBuyTicket: (ticketCount: Int, paymentMethod: String, room: GameRoom?, selectedTickets: List<TambolaTicket>?) -> Boolean,
    onBookSpecificSlot: ((GameRoom, Int, String) -> Boolean)? = null,
    onOpenRazorpayTopup: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val context = LocalContext.current
    val effectiveRoom = room ?: state.selectedRoomForDirectBuy ?: state.activeRooms.firstOrNull() ?: GameRoom(
        id = "room-live-1",
        title = "Grand Amber Live 90",
        hostName = "Housie Master",
        category = "Public",
        prizeAmount = 15000,
        entryFee = 30,
        currentPlayers = 35,
        maxPlayers = 100
    )

    // Current Step in the booking flow: First select tickets, then pay
    var currentStep by remember { mutableStateOf(BookingStep.SELECT_TICKETS) }

    val hasSlots = effectiveRoom.ticketSlots.isNotEmpty()
    var selectedSlotNumber by remember(effectiveRoom.id) {
        mutableStateOf<Int?>(effectiveRoom.ticketSlots.firstOrNull { !it.isBooked }?.slotNumber)
    }

    var ticketCount by remember { mutableIntStateOf(1) }
    var selectedPaymentMode by remember { mutableStateOf("GPay") } // "GPay", "OnlinePay", "Wallet"
    var isProcessing by remember { mutableStateOf(false) }

    // Generated preview tickets
    var previewTickets by remember(ticketCount, selectedSlotNumber) {
        val slotTicket = effectiveRoom.ticketSlots.find { it.slotNumber == selectedSlotNumber }?.ticket
        if (slotTicket != null) {
            mutableStateOf(listOf(slotTicket))
        } else {
            mutableStateOf((1..ticketCount).map { TambolaTicket.generate("TKT-${Random.nextInt(1000, 9999)}") })
        }
    }

    val totalAmount = if (hasSlots && selectedSlotNumber != null) {
        effectiveRoom.entryFee
    } else {
        effectiveRoom.entryFee * ticketCount
    }
    val canPayFromWallet = state.walletBalance >= totalAmount

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
                .testTag("buy_tickets_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentStep == BookingStep.PAYMENT_METHOD) {
                            IconButton(
                                onClick = { currentStep = BookingStep.SELECT_TICKETS },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.filled.ArrowBack,
                                    contentDescription = "Back to Selection",
                                    tint = RoyalPurple
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = RoyalPurple.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = "Tickets",
                                        tint = RoyalPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column {
                            Text(
                                text = if (currentStep == BookingStep.SELECT_TICKETS) "1. SELECT TICKETS" else "2. PAYMENT METHOD",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = RoyalPurple
                            )
                            Text(
                                text = if (currentStep == BookingStep.SELECT_TICKETS)
                                    "Pick slots or number of tickets to preview"
                                else
                                    "Choose how you want to pay ₹$totalAmount",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Animated step content
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState == BookingStep.PAYMENT_METHOD) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "BookingStepsAnimation"
                ) { step ->
                    when (step) {
                        BookingStep.SELECT_TICKETS -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Match Info Card & Prize Breakdown
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8FF)),
                                    border = BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = effectiveRoom.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E1E1E)
                                                )
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.EmojiEvents,
                                                        contentDescription = null,
                                                        tint = AmberGold,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Pool: ₹${effectiveRoom.prizeAmount}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4A4A4A)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("•", color = Color.Gray)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "₹${effectiveRoom.entryFee}/ticket",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = RoyalPurple
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (effectiveRoom.isLive) Color(0xFFE8F5E9) else AmberGold.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (effectiveRoom.isLive) "● LIVE NOW" else "SCHEDULED",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (effectiveRoom.isLive) EmeraldGreen else Color(0xFFE66700),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // Prize Breakdown Chips if available
                                        if (effectiveRoom.prizeBreakdown.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            HorizontalDivider(color = RoyalPurple.copy(alpha = 0.1f), thickness = 0.8.dp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "🏆 PRIZE BREAKDOWN (7 WAYS TO WIN)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = RoyalPurple
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(effectiveRoom.prizeBreakdown.toList()) { (prizeName, prizeVal) ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = AmberGold.copy(alpha = 0.15f),
                                                        border = BorderStroke(0.8.dp, AmberGold.copy(alpha = 0.5f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(text = "$prizeName: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                            Text(text = "₹$prizeVal", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFB76E00))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // LIMITED ROOM: INTERACTIVE SLOT SELECTION
                                if (hasSlots) {
                                    val bookedCount = effectiveRoom.ticketSlots.count { it.isBooked }
                                    val totalSlots = effectiveRoom.ticketSlots.size

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Select Reserved Slot ($bookedCount/$totalSlots Booked):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SleekPurple.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "${totalSlots - bookedCount} Available",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekPurple,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Slot Carousel / Selector Grid
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(effectiveRoom.ticketSlots) { slot ->
                                            val isSelected = selectedSlotNumber == slot.slotNumber
                                            val isBooked = slot.isBooked

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when {
                                                    isSelected -> RoyalPurple
                                                    isBooked -> Color(0xFFF0EFF2)
                                                    else -> Color(0xFFF3F0F7)
                                                },
                                                border = when {
                                                    isSelected -> BorderStroke(2.dp, AmberGold)
                                                    isBooked -> BorderStroke(1.dp, Color(0xFFDCD8E0))
                                                    else -> BorderStroke(1.dp, SleekPurple.copy(alpha = 0.3f))
                                                },
                                                modifier = Modifier
                                                    .width(105.dp)
                                                    .clickable(enabled = !isBooked) {
                                                        selectedSlotNumber = slot.slotNumber
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        if (isBooked) {
                                                            Icon(
                                                                imageVector = Icons.Default.Lock,
                                                                contentDescription = "Booked",
                                                                tint = Color.Gray,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                        }
                                                        Text(
                                                            text = "Slot #${slot.slotNumber}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = when {
                                                                isSelected -> Color.White
                                                                isBooked -> Color.Gray
                                                                else -> Color.Black
                                                            }
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Text(
                                                        text = if (isBooked) slot.bookedByName.ifBlank { "Booked" } else "Available",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            isSelected -> AmberGold
                                                            isBooked -> Color.Red.copy(alpha = 0.8f)
                                                            else -> EmeraldGreen
                                                        },
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                } else {
                                    // UNLIMITED ROOM: Ticket Quantity Selector
                                    Text("Select Ticket Quantity:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(1, 2, 3, 6).forEach { count ->
                                            val isSelected = ticketCount == count
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) RoyalPurple else Color(0xFFF3F0F7),
                                                border = if (isSelected) BorderStroke(1.5.dp, AmberGold) else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        ticketCount = count
                                                        previewTickets = (1..count).map {
                                                            TambolaTicket.generate("TKT-${Random.nextInt(1000, 9999)}")
                                                        }
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 10.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "$count",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isSelected) Color.White else Color.Black
                                                    )
                                                    Text(
                                                        text = if (count == 6) "Full Sheet" else if (count == 1) "Ticket" else "Tickets",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelected) AmberGold else Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // Real-time Ticket Number Preview & Refresh Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Check & Preview Numbers:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                    TextButton(
                                        onClick = {
                                            previewTickets = (1..ticketCount).map {
                                                TambolaTicket.generate("TKT-${Random.nextInt(1000, 9999)}")
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "New Numbers", modifier = Modifier.size(14.dp), tint = RoyalPurple)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Shuffle Tickets", fontSize = 11.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Ticket Mini-Grids
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(previewTickets) { index, tck ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
                                            modifier = Modifier.width(280.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Ticket #${index + 1} (${tck.id})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = RoyalPurple
                                                    )
                                                    Text(
                                                        text = "15 Numbers",
                                                        fontSize = 9.sp,
                                                        color = Color.Gray
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // 3x9 Grid Preview
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    tck.grid.forEach { row ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            row.forEach { num ->
                                                                Surface(
                                                                    shape = RoundedCornerShape(3.dp),
                                                                    color = if (num != null) RoyalPurple.copy(alpha = 0.12f) else Color(0xFFF2F2F2),
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(22.dp)
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        if (num != null) {
                                                                            Text(
                                                                                text = "$num",
                                                                                fontSize = 10.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                color = Color(0xFF1E1E1E)
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

                                Spacer(modifier = Modifier.height(20.dp))

                                // Bottom Action Row: Proceed to Pay
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Calculated Total", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            text = "₹$totalAmount",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = RoyalPurple
                                        )
                                    }

                                    Button(
                                        onClick = { currentStep = BookingStep.PAYMENT_METHOD },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("proceed_to_pay_btn")
                                    ) {
                                        Text(
                                            text = "Proceed to Pay (₹$totalAmount)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.filled.ArrowForward,
                                            contentDescription = null,
                                            tint = AmberGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        BookingStep.PAYMENT_METHOD -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Order Summary Pill
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF6F3FA),
                                    border = BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = effectiveRoom.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F1F1F)
                                            )
                                            Text(
                                                text = if (hasSlots && selectedSlotNumber != null) "Slot #$selectedSlotNumber • 1 Ticket" else "$ticketCount Ticket(s) Selected",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "₹$totalAmount",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = RoyalPurple
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Payment Method Selector
                                Text("Choose Payment Method:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                Spacer(modifier = Modifier.height(8.dp))

                                // 1. Google Pay (Direct UPI)
                                PaymentOptionRow(
                                    title = "Google Pay / UPI Fast Pay",
                                    subtitle = "Instant settlement to ${state.adminOrgInfo.adminUpiId}",
                                    icon = Icons.Default.PhoneAndroid,
                                    iconTint = Color(0xFF4285F4),
                                    isSelected = selectedPaymentMode == "GPay",
                                    badge = "INSTANT",
                                    onClick = { selectedPaymentMode = "GPay" }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. Online Payment Gateway (Razorpay / NetBanking / Cards)
                                PaymentOptionRow(
                                    title = "Online Pay (Cards / NetBanking / Paytm)",
                                    subtitle = "Secure multi-option payment gateway",
                                    icon = Icons.Default.CreditCard,
                                    iconTint = Color(0xFF0C2340),
                                    isSelected = selectedPaymentMode == "OnlinePay",
                                    badge = "GATEWAY",
                                    onClick = { selectedPaymentMode = "OnlinePay" }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3. Wallet Balance
                                PaymentOptionRow(
                                    title = "Pay from Wallet Balance",
                                    subtitle = "Available Balance: ₹${state.walletBalance} ${if (!canPayFromWallet) "(Insufficient)" else ""}",
                                    icon = Icons.Default.AccountBalanceWallet,
                                    iconTint = if (canPayFromWallet) EmeraldGreen else Color.Gray,
                                    isSelected = selectedPaymentMode == "Wallet",
                                    badge = "BALANCE",
                                    onClick = { selectedPaymentMode = "Wallet" }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Admin Account Destination Box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF9F7FA),
                                    border = BorderStroke(1.dp, Color(0xFFE2DCE8)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Official Admin Revenue Settlement",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF333333)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Recipient: ${state.adminOrgInfo.organizationName} • UPI: ${state.adminOrgInfo.adminUpiId}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Final Action Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { currentStep = BookingStep.SELECT_TICKETS },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        Text("← Back", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                    }

                                    if (selectedPaymentMode == "Wallet" && !canPayFromWallet) {
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onOpenRazorpayTopup()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66700)),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(46.dp)
                                        ) {
                                            Text("Top Up Wallet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                isProcessing = true
                                                val modeLabel = when (selectedPaymentMode) {
                                                    "GPay" -> "Google Pay (UPI)"
                                                    "OnlinePay" -> "Online Pay (Gateway)"
                                                    else -> "Wallet Balance"
                                                }
                                                val success = if (hasSlots && selectedSlotNumber != null && onBookSpecificSlot != null) {
                                                    onBookSpecificSlot(effectiveRoom, selectedSlotNumber!!, modeLabel)
                                                } else {
                                                    onBuyTicket(ticketCount, modeLabel, effectiveRoom, previewTickets)
                                                }
                                                isProcessing = false
                                                if (success) {
                                                    if (hasSlots && selectedSlotNumber != null) {
                                                        Toast.makeText(context, "₹$totalAmount paid! Slot #$selectedSlotNumber Booked!", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "₹$totalAmount paid! $ticketCount Ticket(s) Booked!", Toast.LENGTH_LONG).show()
                                                    }
                                                    onDismiss()
                                                } else {
                                                    Toast.makeText(context, "Transaction Failed. Please check balance.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            enabled = !isProcessing,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selectedPaymentMode == "GPay") Color(0xFF1A73E8) else EmeraldGreen
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .height(46.dp)
                                                .testTag("confirm_buy_ticket_btn")
                                        ) {
                                            if (isProcessing) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(
                                                    imageVector = if (selectedPaymentMode == "GPay") Icons.Default.PhoneAndroid else Icons.Default.ShoppingCart,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (selectedPaymentMode == "GPay") "PAY VIA GPAY (₹$totalAmount)" else "PAY ₹$totalAmount & BOOK",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 12.sp
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
}

@Composable
private fun PaymentOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    badge: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) RoyalPurple.copy(alpha = 0.08f) else Color(0xFFFAFAFA),
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, if (isSelected) RoyalPurple else Color(0xFFE5E5E5)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
                    }
                    Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
                }
            }

            Surface(
                shape = CircleShape,
                color = if (isSelected) RoyalPurple else Color.Transparent,
                border = if (!isSelected) BorderStroke(1.5.dp, Color.LightGray) else null,
                modifier = Modifier.size(20.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

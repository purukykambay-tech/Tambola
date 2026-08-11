package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.service.UpiApp
import com.example.service.UpiPaymentService
import com.example.service.UpiTransactionRequest
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple

@Composable
fun RazorpayModal(
    isVisible: Boolean,
    currentWalletBalance: Int,
    onPaymentSuccess: (amount: Int, method: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val context = LocalContext.current
    var selectedAmount by remember { mutableStateOf(500) }
    var selectedApp by remember { mutableStateOf(UpiApp.GPAY) }
    var customVpaInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val presetAmounts = listOf(100, 500, 1000, 2500)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("payment_modal_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Razorpay & UPI Gateway Brand Header
                Surface(
                    color = Color(0xFF0C2340), // Razorpay navy dark blue
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                color = Color(0xFF0288D1),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("R", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Razorpay UPI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = EmeraldGreen, shape = RoundedCornerShape(4.dp)) {
                                        Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("256-Bit NPCI Encrypted UPI Gateway", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                }
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet Balance Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9E6), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = RoyalPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Current Wallet Balance:", fontSize = 13.sp, color = Color.DarkGray)
                    }
                    Text("₹$currentWalletBalance", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RoyalPurple)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Amount Section
                Text("SELECT TOP-UP AMOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amt ->
                        val isSelected = selectedAmount == amt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AmberGold else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAmount = amt }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.Black else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 10.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "₹$amt",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Payment Application (UPI Deep Linking Options)
                Text("SELECT UPI DEEP-LINK APP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val appList = listOf(
                        UpiApp.GPAY to "Google Pay (GPay Instant)",
                        UpiApp.PHONEPE to "PhonePe UPI Direct",
                        UpiApp.PAYTM to "Paytm UPI / Wallet",
                        UpiApp.GENERIC_UPI to "Any Installed UPI App (BHIM / Cred)"
                    )

                    appList.forEach { (app, label) ->
                        val isSelected = selectedApp == app
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedApp = app }
                                .testTag("select_app_${app.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) RoyalPurple.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) RoyalPurple else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (app) {
                                            UpiApp.GPAY -> Icons.Default.PhoneAndroid
                                            UpiApp.PHONEPE -> Icons.Default.FlashOn
                                            UpiApp.PAYTM -> Icons.Default.QrCodeScanner
                                            else -> Icons.Default.QrCode
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) RoyalPurple else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                }

                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldGreen)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom VPA / UPI ID Option (Optional)
                OutlinedTextField(
                    value = customVpaInput,
                    onValueChange = { customVpaInput = it },
                    label = { Text("Enter Custom VPA / UPI ID (Optional)") },
                    placeholder = { Text("e.g. mobile@upi or username@okaxis") },
                    leadingIcon = { Icon(imageVector = Icons.Default.QrCode, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalPurple,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Main Deep Link Launch Button
                Button(
                    onClick = {
                        isProcessing = true
                        val request = UpiTransactionRequest(
                            payeeVpa = if (customVpaInput.isNotBlank()) customVpaInput.trim() else "housiesphere@upi",
                            payeeName = "HousieSphere Tambola",
                            amount = selectedAmount,
                            transactionNote = "HousieSphere Wallet Recharge"
                        )

                        UpiPaymentService.launchUpiPayment(
                            context = context,
                            request = request,
                            targetApp = selectedApp,
                            onSuccess = { refId, method ->
                                isProcessing = false
                                onPaymentSuccess(selectedAmount, method)
                            },
                            onError = { err ->
                                isProcessing = false
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0288D1), // Razorpay Blue button
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pay_upi_deeplink_btn"),
                    shape = RoundedCornerShape(26.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PAY ₹$selectedAmount VIA ${selectedApp.displayName.uppercase()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Auto-returns to HousieSphere & credits wallet instantly",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

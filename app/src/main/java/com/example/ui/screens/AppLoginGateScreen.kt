package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple

@Composable
fun AppLoginGateScreen(
    currentStep: Int,
    tempMobile: String,
    otpInput: String,
    statusMessage: String?,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String) -> Unit,
    onLoginStaff: (staffId: String, pass: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Local inputs for user OTP login
    var mobileText by remember { mutableStateOf("") }
    var otpText by remember { mutableStateOf("") }

    // State for Staff / Admin Login Dialog
    var showStaffDialog by remember { mutableStateOf(false) }
    var staffIdInput by remember { mutableStateOf("") }
    var staffPasswordInput by remember { mutableStateOf("") }
    var isStaffPassVisible by remember { mutableStateOf(false) }
    var staffErrorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RoyalPurple,
                        Color(0xFF2A0845),
                        Color(0xFF10002B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // TOP BAR with Staff / Admin Panel Corner Access Button (Outside Main App)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AmberGold,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Logo",
                                tint = RoyalPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "HousieSphere",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }

                // TOP RIGHT CORNER: STAFF / ADMIN / AGENT / MANAGER PORTAL BUTTON
                Button(
                    onClick = { showStaffDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("admin_login_corner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Staff Portal",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STAFF / ADMIN 🛡️",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // MAIN LOGIN CARD (USER OTP LOGIN)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_login_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RoyalPurple.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "User Auth",
                                tint = RoyalPurple,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PLAYER LOGIN",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = RoyalPurple
                    )

                    Text(
                        text = "Enter your mobile number to get OTP & join live matches",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (currentStep == 1) {
                        // Step 1: Mobile Number Input
                        OutlinedTextField(
                            value = mobileText,
                            onValueChange = { if (it.length <= 10) mobileText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Mobile Number") },
                            prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (mobileText.length == 10) {
                                    onSendOtp(mobileText)
                                    Toast.makeText(context, "OTP Sent to +91 $mobileText!", Toast.LENGTH_SHORT).show()
                                }
                            }),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_mobile_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalPurple,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (mobileText.length == 10) {
                                    onSendOtp(mobileText)
                                    Toast.makeText(context, "OTP Sent to +91 $mobileText!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalPurple,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_send_otp_btn"),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("GET OTP & CONTINUE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSendOtp("9876543210")
                                onVerifyOtp("123456")
                            },
                            modifier = Modifier.testTag("login_guest_preview_btn")
                        ) {
                            Text("Play as Demo Player", color = RoyalPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    } else {
                        // Step 2: OTP Input
                        Text(
                            text = "OTP sent to $tempMobile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = otpText,
                            onValueChange = { if (it.length <= 6) otpText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Enter 6-Digit OTP") },
                            placeholder = { Text("6-digit code") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onVerifyOtp(otpText)
                            }),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_otp_input"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSendOtp(mobileText.ifBlank { "9876543210" })
                            }) {
                                Text("Resend OTP", fontSize = 12.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = {
                                otpText = "123456"
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }) {
                                Text("Use Demo Code", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onVerifyOtp(otpText)
                                Toast.makeText(context, "Welcome to HousieSphere!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("login_verify_otp_btn"),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("VERIFY & ENTER GAME", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    statusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoyalPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // FOOTER SUPPORT & SECURITY DISCLAIMER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Secure Fair Play & Instant Wallet Top-ups",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        // STAFF & ADMIN LOGIN DIALOG (Top-Right Corner Triggered)
        if (showStaffDialog) {
            Dialog(onDismissRequest = { showStaffDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("admin_dialog_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RoyalPurple,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Staff Security",
                                    tint = AmberGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "STAFF & ADMIN PORTAL",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = RoyalPurple
                        )

                        Text(
                            text = "Sign in with your Admin, Manager, or Agent Login ID",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Fill Role Buttons for convenience
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    staffIdInput = "admin"
                                    staffPasswordInput = "admin123"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E8FF), contentColor = RoyalPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    staffIdInput = "manager"
                                    staffPasswordInput = "manager123"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE), contentColor = Color(0xFF0369A1)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Manager", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    staffIdInput = "agent1"
                                    staffPasswordInput = "agent123"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7), contentColor = EmeraldGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Agent", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // User ID Input
                        OutlinedTextField(
                            value = staffIdInput,
                            onValueChange = { staffIdInput = it },
                            label = { Text("Staff / Admin Login ID") },
                            placeholder = { Text("admin, manager, or agent1") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = RoyalPurple) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_dialog_userid"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Input
                        OutlinedTextField(
                            value = staffPasswordInput,
                            onValueChange = { staffPasswordInput = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = RoyalPurple) },
                            trailingIcon = {
                                IconButton(onClick = { isStaffPassVisible = !isStaffPassVisible }) {
                                    Icon(
                                        imageVector = if (isStaffPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (isStaffPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val success = onLoginStaff(staffIdInput, staffPasswordInput)
                                if (success) {
                                    showStaffDialog = false
                                    Toast.makeText(context, "Staff Authenticated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    staffErrorMsg = "Invalid Login ID or Password. Check with Admin."
                                }
                            }),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_dialog_pass"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        staffErrorMsg?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                showStaffDialog = false
                            }) {
                                Text("Cancel", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    val success = onLoginStaff(staffIdInput, staffPasswordInput)
                                    if (success) {
                                        showStaffDialog = false
                                        Toast.makeText(context, "Staff Authenticated!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        staffErrorMsg = "Invalid Login ID or Password. Check with Admin."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("admin_dialog_login_btn")
                            ) {
                                Text("SIGN IN TO PORTAL")
                            }
                        }
                    }
                }
            }
        }
    }
}

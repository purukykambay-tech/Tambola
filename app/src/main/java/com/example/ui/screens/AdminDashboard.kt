package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AdminOrganizationInfo
import com.example.model.GameConfiguration
import com.example.model.GameRoom
import com.example.model.StaffRole
import com.example.model.StaffUser
import com.example.ui.components.TambolaDateTimePickerDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPurple
import com.example.viewmodel.TambolaUiState
import java.util.UUID

/**
 * Enhanced Role-Based Staff & Admin Portal (Admin, Manager, Agent)
 *
 * Capabilities:
 * 1. ADMIN: Master control, Staff Login ID generation, Agent booking permission allow/disallow,
 *           manual next number selection (type or click from grid), full room & finance audits.
 * 2. MANAGER: Room Creation & Scheduling terminal only.
 * 3. AGENT: Player Ticket Booking terminal only (with admin permission enforcement).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    state: TambolaUiState,
    onLoginAdmin: (adminId: String, pass: String) -> Boolean,
    onLogoutAdmin: () -> Unit,
    onCreateRoom: (
        title: String,
        host: String,
        category: String,
        prize: Int,
        entryFee: Int,
        isJackpot: Boolean,
        scheduledStartTimeMs: Long?,
        scheduledTimeString: String,
        isUnlimitedPlayers: Boolean,
        maxPlayers: Int,
        prizeBreakdown: Map<String, Int>
    ) -> Unit,
    onUpdateRoom: (roomId: String, title: String, prize: Int, entryFee: Int, isLive: Boolean, scheduledStartTimeMs: Long?, scheduledTimeString: String) -> Unit,
    onDeleteRoom: (roomId: String) -> Unit,
    onForceCloseRoom: (roomId: String) -> Unit = onDeleteRoom,
    onUpdateGameConfig: (GameConfiguration) -> Unit,
    onBookTicketsForPlayer: (playerPhone: String, playerName: String, roomId: String, ticketCount: Int) -> Unit = { _, _, _, _ -> },
    onUpdateOrgInfo: (AdminOrganizationInfo) -> Unit = {},
    onCallNextNumber: () -> Unit,
    onCallSpecificNumber: (Int) -> Boolean = { false },
    onCreateStaffUser: (StaffUser) -> Unit = {},
    onToggleStaffBookingPermission: (staffId: String, allowed: Boolean) -> Unit = { _, _ -> },
    onToggleStaffCreationPermission: (staffId: String, allowed: Boolean) -> Unit = { _, _ -> },
    onDeleteStaffUser: (staffId: String) -> Unit = {},
    onAgentBookTicket: (agentId: String, roomId: String, slotNumber: Int?, ticketCount: Int, playerPhone: String, playerName: String, paymentMode: String) -> Boolean = { _, _, _, _, _, _, _ -> false },
    onBroadcastMessage: (String) -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentStaff = state.currentStaffUser ?: StaffUser(
        id = "staff_admin_master",
        loginId = "admin",
        password = "udoipurtambola@2026",
        name = "Chief Administrator",
        role = StaffRole.ADMIN
    )
    val role = currentStaff.role

    // Admin Credentials Input State for fallback
    var adminIdInput by remember { mutableStateOf("admin") }
    var passwordInput by remember { mutableStateOf("admin123") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Navigation Sub-tab inside Authenticated Dashboard (Admin only)
    var selectedDashboardTab by remember { mutableIntStateOf(0) }
    // 0: Live Room Monitor, 1: Rooms & Slots, 2: Manual Caller & Remote, 3: Staff & Agent Management, 4: Player Booking, 5: Org & Audit

    // Force Close Confirmation Dialog State
    var forceCloseTargetRoom by remember { mutableStateOf<GameRoom?>(null) }

    // Manual Next Number Selection / Type State
    var typedNumberInput by remember { mutableStateOf("") }

    // Staff User Generation Dialog State
    var isAddStaffDialogOpen by remember { mutableStateOf(false) }
    var newStaffLoginId by remember { mutableStateOf("") }
    var newStaffPassword by remember { mutableStateOf("") }
    var newStaffName by remember { mutableStateOf("") }
    var newStaffPhone by remember { mutableStateOf("") }
    var newStaffRole by remember { mutableStateOf(StaffRole.AGENT) }
    var newStaffAllowBooking by remember { mutableStateOf(true) }
    var newStaffAllowCreation by remember { mutableStateOf(false) }

    // Room Creation State
    var roomTitleInput by remember { mutableStateOf("Royal Amber 90 Special (Limited 10)") }
    var hostNameInput by remember { mutableStateOf(if (role == StaffRole.MANAGER) currentStaff.name else "Admin Master") }
    var selectedCategory by remember { mutableStateOf("Public") }
    var prizeAmountInput by remember { mutableStateOf("25000") }
    var entryFeeInput by remember { mutableStateOf("50") }
    var isJackpot by remember { mutableStateOf(true) }
    var isUnlimitedPlayers by remember { mutableStateOf(false) }
    var limitedCapacityInput by remember { mutableStateOf("10") }
    var createScheduledTimeMs by remember { mutableStateOf<Long?>(null) }
    var createScheduledTimeString by remember { mutableStateOf("Live Now") }
    var isCreateSchedulePickerOpen by remember { mutableStateOf(false) }

    // Custom Prize Breakdown State
    var prizeJaldi5 by remember { mutableStateOf("2500") }
    var prizeCorners by remember { mutableStateOf("2000") }
    var prizeTopLine by remember { mutableStateOf("3500") }
    var prizeMiddleLine by remember { mutableStateOf("3500") }
    var prizeBottomLine by remember { mutableStateOf("3500") }
    var prizeFullHouse1 by remember { mutableStateOf("7000") }
    var prizeFullHouse2 by remember { mutableStateOf("3000") }
    var showPrizeBreakdownCard by remember { mutableStateOf(true) }

    // Room Inline Editing State
    var editingRoomId by remember { mutableStateOf<String?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editPrize by remember { mutableStateOf("") }
    var editFee by remember { mutableStateOf("") }
    var editIsLive by remember { mutableStateOf(true) }
    var editScheduledTimeMs by remember { mutableStateOf<Long?>(null) }
    var editScheduledTimeString by remember { mutableStateOf("") }
    var isEditSchedulePickerOpen by remember { mutableStateOf(false) }

    // Ticket Booking for Players State (Admin / Agent)
    var bookPlayerPhoneInput by remember { mutableStateOf("") }
    var bookPlayerNameInput by remember { mutableStateOf("") }
    var bookSelectedRoomId by remember(state.activeRooms) { mutableStateOf(state.activeRooms.firstOrNull()?.id ?: "") }
    var bookTicketCount by remember { mutableIntStateOf(1) }

    // Admin Organization Info & Rules Editor State
    var orgNameInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.organizationName) }
    var orgPhoneInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.supportPhone) }
    var orgWhatsappInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.supportWhatsapp) }
    var orgEmailInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.supportEmail) }
    var orgAddressInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.address) }
    var orgUpiIdInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.adminUpiId) }
    var orgBankNameInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.adminBankName) }
    var orgAccountNumberInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.adminAccountNumber) }
    var orgIfscInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.adminIfscCode) }
    var orgRulesInput by remember(state.adminOrgInfo) { mutableStateOf(state.adminOrgInfo.rulesAndRegulations) }

    var broadcastText by remember { mutableStateOf("") }

    // 1. SECURE CREDENTIALS CHALLENGE GATE (If not authenticated at all)
    if (!state.isAdminAuthenticated && state.currentStaffUser == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF140326),
                            Color(0xFF22093D),
                            Color(0xFF0D021A)
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RoyalPurple,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Staff Gate",
                                tint = AmberGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "STAFF & ADMIN PORTAL 🛡️",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = RoyalPurple
                    )

                    Text(
                        text = "Secure Role-Based Access for Admin, Manager, and Agent accounts",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Quick-Fill Role Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (adminIdInput == "admin") AmberGold.copy(alpha = 0.25f) else Color(0xFFF3E8FF),
                            border = if (adminIdInput == "admin") androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    adminIdInput = "admin"
                                    passwordInput = "admin123"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("👑 Admin", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = RoyalPurple)
                                Text("Master", fontSize = 9.sp, color = Color.DarkGray)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (adminIdInput == "manager") Color(0xFF38BDF8).copy(alpha = 0.25f) else Color(0xFFE0F2FE),
                            border = if (adminIdInput == "manager") androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0369A1)) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    adminIdInput = "manager"
                                    passwordInput = "manager123"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💼 Manager", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0369A1))
                                Text("Rooms & Book", fontSize = 9.sp, color = Color.DarkGray)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (adminIdInput == "agent1") EmeraldGreen.copy(alpha = 0.25f) else Color(0xFFDCFCE7),
                            border = if (adminIdInput == "agent1") androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    adminIdInput = "agent1"
                                    passwordInput = "agent123"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎟️ Agent", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldGreen)
                                Text("Book Tickets", fontSize = 9.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = adminIdInput,
                        onValueChange = { adminIdInput = it },
                        label = { Text("Staff / Admin Login ID") },
                        placeholder = { Text("admin, manager, or agent1") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = RoyalPurple) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Role Restriction Notice
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8F6FA),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🔒 Access Control Policy:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = RoyalPurple
                            )
                            Text(
                                text = "• Agent: Ticket Booking Counter only (Restricted from Admin settings)\n• Manager: Room Scheduling & Ticket Booking (Restricted from Admin settings)\n• Admin: Master Controller (Caller overrides, Staff IDs & Banking)",
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val success = onLoginAdmin(adminIdInput, passwordInput)
                            if (success) {
                                Toast.makeText(context, "Welcome to Staff Portal!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid Login ID or Password!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_submit_btn"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AUTHENTICATE & ENTER", fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onLogoutAdmin) {
                        Text("Return to Player App Login", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
        return
    }

    // 2. AUTHENTICATED STAFF DASHBOARD
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F021B),
                        Color(0xFF1E0733),
                        Color(0xFF130122)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // TOP HEADER BAR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B0E4C)),
                elevation = CardDefaults.cardElevation(4.dp)
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
                            color = when (role) {
                                StaffRole.ADMIN -> AmberGold
                                StaffRole.MANAGER -> Color(0xFF38BDF8)
                                StaffRole.AGENT -> EmeraldGreen
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (role) {
                                        StaffRole.ADMIN -> Icons.Default.AdminPanelSettings
                                        StaffRole.MANAGER -> Icons.Default.SupervisorAccount
                                        StaffRole.AGENT -> Icons.Default.ConfirmationNumber
                                    },
                                    contentDescription = "Role",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (role) {
                                        StaffRole.ADMIN -> "CHIEF ADMIN PORTAL 👑"
                                        StaffRole.MANAGER -> "MANAGER OPERATIONS 💼"
                                        StaffRole.AGENT -> "AGENT BOOKING DESK 🎟️"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = AmberGold
                                )
                            }
                            Text(
                                text = "${currentStaff.name} (@${currentStaff.loginId})",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // EXIT / LOGOUT BUTTON
                    Button(
                        onClick = onLogoutAdmin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB3261E),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Exit to Login",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXIT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ROLE-BASED VIEW ROUTING
            when (role) {
                // ==========================================
                // AGENT TERMINAL: PLAYER TICKET BOOKINGS ONLY
                // ==========================================
                StaffRole.AGENT -> {
                    AgentTerminalView(
                        currentStaff = currentStaff,
                        state = state,
                        activeRooms = state.activeRooms,
                        onAgentBookTicket = onAgentBookTicket
                    )
                }

                // ==========================================
                // MANAGER TERMINAL: CREATE & MANAGE ROOMS ONLY
                // ==========================================
                StaffRole.MANAGER -> {
                    ManagerTerminalView(
                        currentStaff = currentStaff,
                        state = state,
                        onCreateRoom = onCreateRoom,
                        onDeleteRoom = onDeleteRoom,
                        onAgentBookTicket = onAgentBookTicket
                    )
                }

                // ==========================================
                // CHIEF ADMIN: FULL MASTER SUITE
                // ==========================================
                StaffRole.ADMIN -> {
                    ScrollableTabRow(
                        selectedTabIndex = selectedDashboardTab,
                        containerColor = Color(0xFF220A3E),
                        contentColor = AmberGold,
                        edgePadding = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedDashboardTab]),
                                color = AmberGold,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedDashboardTab == 0,
                            onClick = { selectedDashboardTab = 0 },
                            text = { Text("Room Monitor 📡", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("admin_tab_room_monitor")
                        )
                        Tab(
                            selected = selectedDashboardTab == 1,
                            onClick = { selectedDashboardTab = 1 },
                            text = { Text("Create & Slots ⚙️", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("admin_tab_rooms")
                        )
                        Tab(
                            selected = selectedDashboardTab == 2,
                            onClick = { selectedDashboardTab = 2 },
                            text = { Text("Manual Caller 🎯", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Dialpad, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("admin_tab_caller")
                        )
                        Tab(
                            selected = selectedDashboardTab == 3,
                            onClick = { selectedDashboardTab = 3 },
                            text = { Text("Staff & Agents 👥", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("admin_tab_staff")
                        )
                        Tab(
                            selected = selectedDashboardTab == 4,
                            onClick = { selectedDashboardTab = 4 },
                            text = { Text("Book Tickets 🎟️", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("admin_tab_booking")
                        )
                        Tab(
                            selected = selectedDashboardTab == 5,
                            onClick = { selectedDashboardTab = 5 },
                            text = { Text("Org & Audit 🏛️", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("admin_tab_org")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        when (selectedDashboardTab) {
                            // 0: Live Room Monitor View (Booked vs Available tickets & Force Close)
                            0 -> {
                                item {
                                    AdminLiveRoomMonitorSection(
                                        state = state,
                                        onForceCloseRequest = { room -> forceCloseTargetRoom = room },
                                        onNavigateToCreateRoom = { selectedDashboardTab = 1 },
                                        onNavigateToCaller = { selectedDashboardTab = 2 },
                                        onStartEditingRoom = { room ->
                                            editingRoomId = room.id
                                            editTitle = room.title
                                            editPrize = room.prizeAmount.toString()
                                            editFee = room.entryFee.toString()
                                            editIsLive = room.isLive
                                            editScheduledTimeMs = room.scheduledStartTimeMs
                                            editScheduledTimeString = room.scheduledTimeString
                                            selectedDashboardTab = 1
                                        }
                                    )
                                }
                            }

                            // 1: Room Creation & Management Tab
                            1 -> {
                                item {
                                    AdminRoomsManagementSection(
                                        state = state,
                                        roomTitleInput = roomTitleInput,
                                        onRoomTitleChange = { roomTitleInput = it },
                                        prizeAmountInput = prizeAmountInput,
                                        onPrizeAmountChange = { prizeAmountInput = it },
                                        entryFeeInput = entryFeeInput,
                                        onEntryFeeChange = { entryFeeInput = it },
                                        isUnlimitedPlayers = isUnlimitedPlayers,
                                        onIsUnlimitedPlayersChange = { isUnlimitedPlayers = it },
                                        limitedCapacityInput = limitedCapacityInput,
                                        onLimitedCapacityChange = { limitedCapacityInput = it },
                                        createScheduledTimeString = createScheduledTimeString,
                                        onOpenSchedulePicker = { isCreateSchedulePickerOpen = true },
                                        showPrizeBreakdownCard = showPrizeBreakdownCard,
                                        onTogglePrizeBreakdownCard = { showPrizeBreakdownCard = !showPrizeBreakdownCard },
                                        prizeJaldi5 = prizeJaldi5,
                                        onPrizeJaldi5Change = { prizeJaldi5 = it },
                                        prizeCorners = prizeCorners,
                                        onPrizeCornersChange = { prizeCorners = it },
                                        prizeTopLine = prizeTopLine,
                                        onPrizeTopLineChange = { prizeTopLine = it },
                                        prizeMiddleLine = prizeMiddleLine,
                                        onPrizeMiddleLineChange = { prizeMiddleLine = it },
                                        prizeBottomLine = prizeBottomLine,
                                        onPrizeBottomLineChange = { prizeBottomLine = it },
                                        prizeFullHouse1 = prizeFullHouse1,
                                        onPrizeFullHouse1Change = { prizeFullHouse1 = it },
                                        prizeFullHouse2 = prizeFullHouse2,
                                        onPrizeFullHouse2Change = { prizeFullHouse2 = it },
                                        onCreateRoomSubmit = {
                                            val prize = prizeAmountInput.toIntOrNull() ?: 10000
                                            val fee = entryFeeInput.toIntOrNull() ?: 20
                                            val capacity = if (isUnlimitedPlayers) 1000 else (limitedCapacityInput.toIntOrNull() ?: 10)
                                            val breakdown = mapOf(
                                                "Early 5 / Jaldi 5" to (prizeJaldi5.toIntOrNull() ?: (prize * 0.1).toInt()),
                                                "Four Corners" to (prizeCorners.toIntOrNull() ?: (prize * 0.08).toInt()),
                                                "Top Line" to (prizeTopLine.toIntOrNull() ?: (prize * 0.14).toInt()),
                                                "Middle Line" to (prizeMiddleLine.toIntOrNull() ?: (prize * 0.14).toInt()),
                                                "Bottom Line" to (prizeBottomLine.toIntOrNull() ?: (prize * 0.14).toInt()),
                                                "Full House 1st" to (prizeFullHouse1.toIntOrNull() ?: (prize * 0.28).toInt()),
                                                "Full House 2nd" to (prizeFullHouse2.toIntOrNull() ?: (prize * 0.12).toInt())
                                            )
                                            onCreateRoom(
                                                roomTitleInput,
                                                hostNameInput,
                                                selectedCategory,
                                                prize,
                                                fee,
                                                isJackpot,
                                                createScheduledTimeMs,
                                                createScheduledTimeString,
                                                isUnlimitedPlayers,
                                                capacity,
                                                breakdown
                                            )
                                            Toast.makeText(context, "Match Room Published to Firestore!", Toast.LENGTH_SHORT).show()
                                        },
                                        editingRoomId = editingRoomId,
                                        onStartEditingRoom = { room ->
                                            editingRoomId = room.id
                                            editTitle = room.title
                                            editPrize = room.prizeAmount.toString()
                                            editFee = room.entryFee.toString()
                                            editIsLive = room.isLive
                                            editScheduledTimeMs = room.scheduledStartTimeMs
                                            editScheduledTimeString = room.scheduledTimeString
                                        },
                                        onCancelEdit = { editingRoomId = null },
                                        editTitle = editTitle,
                                        onEditTitleChange = { editTitle = it },
                                        editPrize = editPrize,
                                        onEditPrizeChange = { editPrize = it },
                                        editFee = editFee,
                                        onEditFeeChange = { editFee = it },
                                        onSaveEditRoom = { room ->
                                            val prize = editPrize.toIntOrNull() ?: room.prizeAmount
                                            val fee = editFee.toIntOrNull() ?: room.entryFee
                                            onUpdateRoom(room.id, editTitle, prize, fee, editIsLive, editScheduledTimeMs, editScheduledTimeString)
                                            editingRoomId = null
                                            Toast.makeText(context, "Room updated in Firestore!", Toast.LENGTH_SHORT).show()
                                        },
                                        onDeleteRoom = onDeleteRoom
                                    )
                                }
                            }

                            // 2: Manual Caller & Remote Override Tab
                            2 -> {
                                item {
                                    AdminManualCallerSection(
                                        state = state,
                                        typedNumberInput = typedNumberInput,
                                        onTypedNumberChange = { typedNumberInput = it },
                                        onCallSpecificNumber = { num ->
                                            val success = onCallSpecificNumber(num)
                                            if (success) {
                                                Toast.makeText(context, "🎯 Number $num Called Successfully!", Toast.LENGTH_SHORT).show()
                                                typedNumberInput = ""
                                            } else {
                                                Toast.makeText(context, "⚠️ Number $num has already been called or is invalid!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onCallNextNumber = onCallNextNumber,
                                        onResetGame = onResetGame,
                                        broadcastText = broadcastText,
                                        onBroadcastTextChange = { broadcastText = it },
                                        onBroadcastMessage = onBroadcastMessage
                                    )
                                }
                            }

                            // 3: Staff & Agent Management Tab
                            3 -> {
                                item {
                                    AdminStaffManagementSection(
                                        state = state,
                                        onOpenAddStaffDialog = { isAddStaffDialogOpen = true },
                                        onToggleStaffBooking = onToggleStaffBookingPermission,
                                        onToggleStaffCreation = onToggleStaffCreationPermission,
                                        onDeleteStaff = onDeleteStaffUser
                                    )
                                }
                            }

                            // 4: Admin Player Ticket Booking
                            4 -> {
                                item {
                                    AdminPlayerBookingSection(
                                        state = state,
                                        bookPlayerPhoneInput = bookPlayerPhoneInput,
                                        onBookPhoneChange = { bookPlayerPhoneInput = it },
                                        bookPlayerNameInput = bookPlayerNameInput,
                                        onBookNameChange = { bookPlayerNameInput = it },
                                        bookSelectedRoomId = bookSelectedRoomId,
                                        onBookRoomChange = { bookSelectedRoomId = it },
                                        bookTicketCount = bookTicketCount,
                                        onBookCountChange = { bookTicketCount = it },
                                        onSubmitBooking = {
                                            if (bookPlayerPhoneInput.length < 10) {
                                                Toast.makeText(context, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                                                return@AdminPlayerBookingSection
                                            }
                                            if (bookPlayerNameInput.isBlank()) {
                                                Toast.makeText(context, "Please enter player name", Toast.LENGTH_SHORT).show()
                                                return@AdminPlayerBookingSection
                                            }
                                            onBookTicketsForPlayer(
                                                bookPlayerPhoneInput.trim(),
                                                bookPlayerNameInput.trim(),
                                                bookSelectedRoomId,
                                                bookTicketCount
                                            )
                                            Toast.makeText(context, "Booked $bookTicketCount tickets for $bookPlayerNameInput!", Toast.LENGTH_LONG).show()
                                            bookPlayerPhoneInput = ""
                                            bookPlayerNameInput = ""
                                        }
                                    )
                                }
                            }

                            // 5: Organization, UPI Details & Audit
                            5 -> {
                                item {
                                    AdminOrganizationAndAuditSection(
                                        state = state,
                                        orgNameInput = orgNameInput,
                                        onOrgNameChange = { orgNameInput = it },
                                        orgPhoneInput = orgPhoneInput,
                                        onOrgPhoneChange = { orgPhoneInput = it },
                                        orgWhatsappInput = orgWhatsappInput,
                                        onOrgWhatsappChange = { orgWhatsappInput = it },
                                        orgEmailInput = orgEmailInput,
                                        onOrgEmailChange = { orgEmailInput = it },
                                        orgAddressInput = orgAddressInput,
                                        onOrgAddressChange = { orgAddressInput = it },
                                        orgUpiIdInput = orgUpiIdInput,
                                        onOrgUpiIdChange = { orgUpiIdInput = it },
                                        orgBankNameInput = orgBankNameInput,
                                        onOrgBankNameChange = { orgBankNameInput = it },
                                        orgAccountNumberInput = orgAccountNumberInput,
                                        onOrgAccountNumberChange = { orgAccountNumberInput = it },
                                        orgIfscInput = orgIfscInput,
                                        onOrgIfscChange = { orgIfscInput = it },
                                        orgRulesInput = orgRulesInput,
                                        onOrgRulesChange = { orgRulesInput = it },
                                        onSaveOrgInfo = {
                                            val updated = AdminOrganizationInfo(
                                                organizationName = orgNameInput.trim().ifBlank { "Udaipur Tambola Club" },
                                                supportPhone = orgPhoneInput.trim(),
                                                supportWhatsapp = orgWhatsappInput.trim(),
                                                supportEmail = orgEmailInput.trim(),
                                                address = orgAddressInput.trim(),
                                                adminUpiId = orgUpiIdInput.trim(),
                                                adminBankName = orgBankNameInput.trim(),
                                                adminAccountNumber = orgAccountNumberInput.trim(),
                                                adminIfscCode = orgIfscInput.trim(),
                                                rulesAndRegulations = orgRulesInput.trim()
                                            )
                                            onUpdateOrgInfo(updated)
                                            Toast.makeText(context, "Organization details and payment UPI saved in Cloud!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: CONFIRM FORCE CLOSE ROOM
    if (forceCloseTargetRoom != null) {
        val roomToClose = forceCloseTargetRoom!!
        val isLimited = !roomToClose.isUnlimitedPlayers
        val bookedCount = if (isLimited) roomToClose.ticketSlots.count { it.isBooked } else roomToClose.currentPlayers
        val availableCount = if (isLimited) maxOf(0, roomToClose.maxPlayers - bookedCount) else 0

        Dialog(onDismissRequest = { forceCloseTargetRoom = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "FORCE CLOSE GAME ROOM? 🛑",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFFB71C1C)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Are you sure you want to terminate this live match immediately?",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F6FA),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = roomToClose.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = RoyalPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Room ID: ${roomToClose.id}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🎟️ Booked: $bookedCount tickets",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPurple
                                )
                                Text(
                                    text = "🟢 Available: ${if (isLimited) "$availableCount slots" else "Unlimited"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prize: ₹${roomToClose.prizeAmount} • Entry: ₹${roomToClose.entryFee}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "⚠️ WARNING: This will instantly remove the room from player lobbies, terminate any active caller sessions, and revoke all open slots.",
                        fontSize = 11.sp,
                        color = Color(0xFFC62828),
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { forceCloseTargetRoom = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CANCEL", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val targetId = roomToClose.id
                                val title = roomToClose.title
                                forceCloseTargetRoom = null
                                onForceCloseRoom(targetId)
                                Toast.makeText(context, "🛑 Force Closed '$title'!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CONFIRM CLOSE", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD / GENERATE STAFF LOGIN ID
    if (isAddStaffDialogOpen) {
        Dialog(onDismissRequest = { isAddStaffDialogOpen = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GENERATE STAFF LOGIN ID 🛡️",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = RoyalPurple
                    )
                    Text(
                        text = "Create role-based credentials for Agent or Manager",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newStaffRole == StaffRole.AGENT,
                            onClick = {
                                newStaffRole = StaffRole.AGENT
                                newStaffAllowBooking = true
                                newStaffAllowCreation = false
                            },
                            label = { Text("🎟️ AGENT (Bookings Only)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = newStaffRole == StaffRole.MANAGER,
                            onClick = {
                                newStaffRole = StaffRole.MANAGER
                                newStaffAllowBooking = false
                                newStaffAllowCreation = true
                            },
                            label = { Text("💼 MANAGER (Rooms Only)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newStaffName,
                        onValueChange = { newStaffName = it },
                        label = { Text("Staff Full Name *") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newStaffLoginId,
                        onValueChange = { newStaffLoginId = it.lowercase().trim() },
                        label = { Text("Login User ID * (Unique)") },
                        placeholder = { Text("e.g. agent_rahul") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newStaffPassword,
                        onValueChange = { newStaffPassword = it.trim() },
                        label = { Text("Password *") },
                        placeholder = { Text("e.g. pass1234") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = RoyalPurple) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newStaffPhone,
                        onValueChange = { newStaffPhone = it.trim() },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+91 98765 43210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalPurple) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (newStaffRole == StaffRole.AGENT) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Player Ticket Booking Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Switch(checked = newStaffAllowBooking, onCheckedChange = { newStaffAllowBooking = it })
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Room Creation Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Switch(checked = newStaffAllowCreation, onCheckedChange = { newStaffAllowCreation = it })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAddStaffDialogOpen = false }) {
                            Text("Cancel", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (newStaffLoginId.isBlank() || newStaffPassword.isBlank() || newStaffName.isBlank()) {
                                    Toast.makeText(context, "Please fill Name, Login ID, and Password", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val newStaff = StaffUser(
                                    id = "staff_${UUID.randomUUID().toString().take(6)}",
                                    loginId = newStaffLoginId,
                                    password = newStaffPassword,
                                    name = newStaffName,
                                    role = newStaffRole,
                                    phone = newStaffPhone,
                                    isBookingAllowed = newStaffAllowBooking,
                                    isCreationAllowed = newStaffAllowCreation
                                )
                                onCreateStaffUser(newStaff)
                                Toast.makeText(context, "Created ${newStaffRole.name} Login: $newStaffLoginId", Toast.LENGTH_LONG).show()
                                isAddStaffDialogOpen = false
                                newStaffLoginId = ""
                                newStaffPassword = ""
                                newStaffName = ""
                                newStaffPhone = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("GENERATE LOGIN ID")
                        }
                    }
                }
            }
        }
    }

    if (isCreateSchedulePickerOpen) {
        TambolaDateTimePickerDialog(
            initialTimestampMs = createScheduledTimeMs,
            onDismiss = { isCreateSchedulePickerOpen = false },
            onConfirmSchedule = { timestampMs, timeString ->
                createScheduledTimeMs = timestampMs
                createScheduledTimeString = timeString
                isCreateSchedulePickerOpen = false
            }
        )
    }

    if (isEditSchedulePickerOpen) {
        TambolaDateTimePickerDialog(
            initialTimestampMs = editScheduledTimeMs,
            onDismiss = { isEditSchedulePickerOpen = false },
            onConfirmSchedule = { timestampMs, timeString ->
                editScheduledTimeMs = timestampMs
                editScheduledTimeString = timeString
                isEditSchedulePickerOpen = false
            }
        )
    }
}

// ====================================================================
// AGENT TERMINAL VIEW (For Role = AGENT)
// ====================================================================
@Composable
fun AgentTerminalView(
    currentStaff: StaffUser,
    state: TambolaUiState,
    activeRooms: List<GameRoom>,
    onAgentBookTicket: (agentId: String, roomId: String, slotNumber: Int?, ticketCount: Int, playerPhone: String, playerName: String, paymentMode: String) -> Boolean
) {
    val context = LocalContext.current
    var playerPhone by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf("") }
    var selectedRoomId by remember(activeRooms) { mutableStateOf(activeRooms.firstOrNull()?.id ?: "") }
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var ticketCount by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("Cash Counter") }

    val targetRoom = activeRooms.find { it.id == selectedRoomId }
    val isLimited = targetRoom != null && !targetRoom.isUnlimitedPlayers

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentStaff.isBookingAllowed) Color(0xFFDCFCE7) else Color(0xFFFFE4E6)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentStaff.isBookingAllowed) "BOOKING STATUS: ACTIVE & ALLOWED ✅" else "BOOKING ACCESS: REVOKED 🚫",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (currentStaff.isBookingAllowed) EmeraldGreen else Color(0xFFB3261E)
                        )
                        Text(
                            text = if (currentStaff.isBookingAllowed)
                                "You are authorized to issue tickets for players at cash counter"
                            else
                                "Chief Admin has temporarily paused ticket issuance for this Agent ID",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                    Text(
                        text = "Tickets: ${currentStaff.totalTicketsBooked}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }

        if (currentStaff.isBookingAllowed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PLAYER TICKET COUNTER 🎟️",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = RoyalPurple
                        )
                        Text(
                            text = "Book tickets directly on behalf of players. Generates verified numbers.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = playerPhone,
                            onValueChange = { playerPhone = it },
                            label = { Text("Player Mobile *") },
                            placeholder = { Text("9876543210") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalPurple) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Player Name *") },
                            placeholder = { Text("e.g. Amit Verma") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Select Game Room:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (activeRooms.isEmpty()) {
                            Text("No active game rooms found. Ask Manager/Admin to create one.", fontSize = 12.sp, color = Color.Red)
                        } else {
                            activeRooms.forEach { room ->
                                val isSelected = room.id == selectedRoomId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AmberGold.copy(alpha = 0.15f) else Color(0xFFF8F6FA),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AmberGold) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedRoomId = room.id
                                            selectedSlot = null
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(room.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Entry: ₹${room.entryFee} | Prize: ₹${room.prizeAmount}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        if (!room.isUnlimitedPlayers) {
                                            Text("Limited (${room.maxPlayers} slots)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = RoyalPurple)
                                        } else {
                                            Text("Unlimited", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (isLimited && targetRoom != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Select Reserved Slot (1 to ${targetRoom.maxPlayers}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val capacity = targetRoom.maxPlayers
                                (1..minOf(capacity, 10)).forEach { slotNum ->
                                    val slotObj = targetRoom.ticketSlots.find { it.slotNumber == slotNum }
                                    val isSlotTaken = slotObj?.isBooked == true
                                    val isCurrentSelected = selectedSlot == slotNum

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isSlotTaken -> Color(0xFFFFCDD2)
                                            isCurrentSelected -> AmberGold
                                            else -> Color(0xFFE8EAF6)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(enabled = !isSlotTaken) {
                                                selectedSlot = slotNum
                                                ticketCount = 1
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isSlotTaken) "✕" else "#$slotNum",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isSlotTaken) Color.Red else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Select Number of Tickets (1 - 6):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..6).forEach { count ->
                                    FilterChip(
                                        selected = ticketCount == count,
                                        onClick = { ticketCount = count },
                                        label = { Text("$count Ticket${if (count > 1) "s" else ""}", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Payment Collection Mode:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = paymentMethod == "Cash Counter",
                                onClick = { paymentMethod = "Cash Counter" },
                                label = { Text("💵 Cash Received") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = paymentMethod == "Agent Direct UPI",
                                onClick = { paymentMethod = "Agent Direct UPI" },
                                label = { Text("📱 UPI Paid") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val totalCost = (targetRoom?.entryFee ?: 0) * ticketCount

                        Button(
                            onClick = {
                                if (playerPhone.length < 10) {
                                    Toast.makeText(context, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (playerName.isBlank()) {
                                    Toast.makeText(context, "Enter player name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (targetRoom == null) {
                                    Toast.makeText(context, "Select a game room", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val success = onAgentBookTicket(
                                    currentStaff.id,
                                    selectedRoomId,
                                    selectedSlot,
                                    ticketCount,
                                    playerPhone.trim(),
                                    playerName.trim(),
                                    paymentMethod
                                )

                                if (success) {
                                    Toast.makeText(context, "✅ Booked $ticketCount ticket(s) for $playerName! Total: ₹$totalCost", Toast.LENGTH_LONG).show()
                                    playerPhone = ""
                                    playerName = ""
                                    selectedSlot = null
                                } else {
                                    Toast.makeText(context, "Booking failed or permission revoked!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BOOK TICKET FOR PLAYER (₹$totalCost)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Recent Bookings Log by this Agent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "YOUR AGENT BOOKING LEDGER",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = RoyalPurple
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val myBookings = state.agentBookingRecords.filter { it.agentId == currentStaff.id || it.agentName == currentStaff.name }
                    if (myBookings.isEmpty()) {
                        Text("No tickets issued by you yet in this session.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        myBookings.forEach { record ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8F6FA),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(record.playerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("₹${record.amountPaid}", fontWeight = FontWeight.ExtraBold, color = EmeraldGreen, fontSize = 13.sp)
                                    }
                                    Text("Phone: ${record.playerPhone} | Mode: ${record.paymentMethod}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Room: ${record.roomTitle} (${record.ticketCount} tickets)", fontSize = 11.sp, color = RoyalPurple)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// MANAGER TERMINAL VIEW (For Role = MANAGER)
// ====================================================================
@Composable
fun ManagerTerminalView(
    currentStaff: StaffUser,
    state: TambolaUiState,
    onCreateRoom: (title: String, host: String, category: String, prize: Int, entryFee: Int, isJackpot: Boolean, scheduledStartTimeMs: Long?, scheduledTimeString: String, isUnlimitedPlayers: Boolean, maxPlayers: Int, prizeBreakdown: Map<String, Int>) -> Unit,
    onDeleteRoom: (roomId: String) -> Unit,
    onAgentBookTicket: (agentId: String, roomId: String, slotNumber: Int?, ticketCount: Int, playerPhone: String, playerName: String, paymentMode: String) -> Boolean
) {
    val context = LocalContext.current
    var selectedManagerTab by remember { mutableIntStateOf(0) } // 0: Ticket Booking, 1: Room Scheduling

    // Ticket Booking State for Manager
    var playerPhone by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf("") }
    var selectedRoomId by remember(state.activeRooms) { mutableStateOf(state.activeRooms.firstOrNull()?.id ?: "") }
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var ticketCount by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("Cash Counter") }

    val targetRoom = state.activeRooms.find { it.id == selectedRoomId }
    val isLimited = targetRoom != null && !targetRoom.isUnlimitedPlayers

    // Room Creation State
    var roomTitleInput by remember { mutableStateOf("Manager Match Special") }
    val selectedCategory by remember { mutableStateOf("Public") }
    var prizeAmountInput by remember { mutableStateOf("15000") }
    var entryFeeInput by remember { mutableStateOf("30") }
    val isJackpot by remember { mutableStateOf(false) }
    var isUnlimitedPlayers by remember { mutableStateOf(false) }
    var limitedCapacityInput by remember { mutableStateOf("10") }
    var createScheduledTimeMs by remember { mutableStateOf<Long?>(null) }
    var createScheduledTimeString by remember { mutableStateOf("Live Now") }
    var isSchedulePickerOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Manager Security & Status Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MANAGER TERMINAL 💼",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF0369A1)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0284C7)
                            ) {
                                Text(
                                    text = "OPERATIONS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Clearance: Authorized for Player Ticket Issuance & Room Scheduling (Restricted from Admin Master Settings)",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Segmented Tab Selector for Manager
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedManagerTab == 0) RoyalPurple else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedManagerTab = 0 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = if (selectedManagerTab == 0) AmberGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Book Player Tickets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (selectedManagerTab == 0) Color.White else Color.DarkGray
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedManagerTab == 1) RoyalPurple else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedManagerTab = 1 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = if (selectedManagerTab == 1) AmberGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Room Operations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (selectedManagerTab == 1) Color.White else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 0: TICKET BOOKING FOR PLAYERS (MANAGER)
        // ==========================================
        if (selectedManagerTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BOOK TICKETS FOR PLAYERS 🎟️",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = RoyalPurple
                        )
                        Text(
                            text = "Issue official tickets directly on behalf of players at the venue or via phone request.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = playerPhone,
                            onValueChange = { playerPhone = it },
                            label = { Text("Player Mobile Number *") },
                            placeholder = { Text("9876543210") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalPurple) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Player Full Name *") },
                            placeholder = { Text("e.g. Rahul Sharma") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Select Game Room:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (state.activeRooms.isEmpty()) {
                            Text("No active game rooms found. Use the Room Operations tab to create one.", fontSize = 12.sp, color = Color.Red)
                        } else {
                            state.activeRooms.forEach { room ->
                                val isSelected = room.id == selectedRoomId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) AmberGold.copy(alpha = 0.15f) else Color(0xFFF8F6FA),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AmberGold) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedRoomId = room.id
                                            selectedSlot = null
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(room.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Entry: ₹${room.entryFee} | Prize: ₹${room.prizeAmount}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        if (!room.isUnlimitedPlayers) {
                                            Text("Limited (${room.maxPlayers} slots)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = RoyalPurple)
                                        } else {
                                            Text("Unlimited", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (isLimited && targetRoom != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Select Reserved Slot (1 to ${targetRoom.maxPlayers}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val capacity = targetRoom.maxPlayers
                                (1..minOf(capacity, 10)).forEach { slotNum ->
                                    val slotObj = targetRoom.ticketSlots.find { it.slotNumber == slotNum }
                                    val isSlotTaken = slotObj?.isBooked == true
                                    val isCurrentSelected = selectedSlot == slotNum

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isSlotTaken -> Color(0xFFFFCDD2)
                                            isCurrentSelected -> AmberGold
                                            else -> Color(0xFFE8EAF6)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(enabled = !isSlotTaken) {
                                                selectedSlot = slotNum
                                                ticketCount = 1
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isSlotTaken) "✕" else "#$slotNum",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isSlotTaken) Color.Red else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Select Number of Tickets (1 - 6):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..6).forEach { count ->
                                    FilterChip(
                                        selected = ticketCount == count,
                                        onClick = { ticketCount = count },
                                        label = { Text("$count", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Payment Collection Mode:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cash Counter", "Manager UPI", "Admin Account").forEach { mode ->
                                FilterChip(
                                    selected = paymentMethod == mode,
                                    onClick = { paymentMethod = mode },
                                    label = { Text(mode, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val costPerTicket = targetRoom?.entryFee ?: 20
                        val totalAmount = if (isLimited) costPerTicket else (costPerTicket * ticketCount)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3E8FF),
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
                                    Text("Amount to Collect:", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("₹$totalAmount", fontWeight = FontWeight.Black, fontSize = 20.sp, color = RoyalPurple)
                                }
                                Text("Mode: $paymentMethod", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (playerName.isBlank()) {
                                    Toast.makeText(context, "Please enter player name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (playerPhone.length < 10) {
                                    Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isLimited && selectedSlot == null) {
                                    Toast.makeText(context, "Please select an available slot number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val success = onAgentBookTicket(
                                    currentStaff.id,
                                    selectedRoomId,
                                    selectedSlot,
                                    ticketCount,
                                    playerPhone,
                                    playerName,
                                    paymentMethod
                                )

                                if (success) {
                                    Toast.makeText(context, "Ticket Booked Successfully by Manager!", Toast.LENGTH_LONG).show()
                                    playerName = ""
                                    playerPhone = ""
                                    selectedSlot = null
                                } else {
                                    Toast.makeText(context, "Booking Failed. Slot may already be taken.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ISSUE & CONFIRM TICKET (₹$totalAmount)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Manager's Ticket Issuance History Ledger
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TICKETS ISSUED LEDGER (${state.agentBookingRecords.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = RoyalPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.agentBookingRecords.isEmpty()) {
                            Text("No tickets issued yet in this session.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            state.agentBookingRecords.forEach { record ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8F6FA),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${record.playerName} (${record.playerPhone})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("${record.roomTitle} | ${if (record.slotNumber != null) "Slot #${record.slotNumber}" else "${record.ticketCount} tkt(s)"}", fontSize = 10.sp, color = Color.Gray)
                                            Text("By: ${record.agentName} • ${record.paymentMethod}", fontSize = 9.sp, color = RoyalPurple)
                                        }
                                        Text("₹${record.amountPaid}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: ROOM OPERATIONS & SCHEDULING (MANAGER)
        // ==========================================
        if (selectedManagerTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CREATE & SCHEDULE GAME ROOM",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = RoyalPurple
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = roomTitleInput,
                            onValueChange = { roomTitleInput = it },
                            label = { Text("Match Room Title *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = prizeAmountInput,
                                onValueChange = { prizeAmountInput = it },
                                label = { Text("Prize Pool (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = entryFeeInput,
                                onValueChange = { entryFeeInput = it },
                                label = { Text("Entry Fee (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Room Player Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(if (isUnlimitedPlayers) "Unlimited player entries" else "Limited slot seats (e.g. 10)", fontSize = 11.sp, color = Color.Gray)
                            }
                            FilterChip(
                                selected = !isUnlimitedPlayers,
                                onClick = { isUnlimitedPlayers = !isUnlimitedPlayers },
                                label = { Text(if (!isUnlimitedPlayers) "Limited Slots" else "Unlimited") }
                            )
                        }

                        if (!isUnlimitedPlayers) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = limitedCapacityInput,
                                onValueChange = { limitedCapacityInput = it },
                                label = { Text("Max Player Seats (10, 20, 50)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Scheduled Match Timing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(createScheduledTimeString, fontSize = 11.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = { isSchedulePickerOpen = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Time")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val prize = prizeAmountInput.toIntOrNull() ?: 10000
                                val fee = entryFeeInput.toIntOrNull() ?: 20
                                val capacity = if (isUnlimitedPlayers) 1000 else (limitedCapacityInput.toIntOrNull() ?: 10)
                                val breakdown = mapOf(
                                    "Early 5" to (prize * 0.1).toInt(),
                                    "Top Line" to (prize * 0.2).toInt(),
                                    "Middle Line" to (prize * 0.2).toInt(),
                                    "Bottom Line" to (prize * 0.2).toInt(),
                                    "Full House" to (prize * 0.3).toInt()
                                )
                                onCreateRoom(
                                    roomTitleInput,
                                    currentStaff.name,
                                    selectedCategory,
                                    prize,
                                    fee,
                                    isJackpot,
                                    createScheduledTimeMs,
                                    createScheduledTimeString,
                                    isUnlimitedPlayers,
                                    capacity,
                                    breakdown
                                )
                                Toast.makeText(context, "Room Created & Published by Manager!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PUBLISH MATCH ROOM", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ACTIVE ROOMS IN CLOUD (${state.activeRooms.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = RoyalPurple
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        state.activeRooms.forEach { room ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8F6FA),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(room.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Prize: ₹${room.prizeAmount} | Entry: ₹${room.entryFee}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Players: ${room.currentPlayers}/${if (!room.isUnlimitedPlayers) room.maxPlayers else "∞"}", fontSize = 11.sp, color = RoyalPurple)
                                    }
                                    IconButton(onClick = { onDeleteRoom(room.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isSchedulePickerOpen) {
        TambolaDateTimePickerDialog(
            initialTimestampMs = createScheduledTimeMs,
            onDismiss = { isSchedulePickerOpen = false },
            onConfirmSchedule = { timestampMs, timeString ->
                createScheduledTimeMs = timestampMs
                createScheduledTimeString = timeString
                isSchedulePickerOpen = false
            }
        )
    }
}

// ====================================================================
// ADMIN SUB-SECTIONS (Rooms, Manual Caller, Staff Management, Org)
// ====================================================================

@Composable
fun AdminRoomsManagementSection(
    state: TambolaUiState,
    roomTitleInput: String,
    onRoomTitleChange: (String) -> Unit,
    prizeAmountInput: String,
    onPrizeAmountChange: (String) -> Unit,
    entryFeeInput: String,
    onEntryFeeChange: (String) -> Unit,
    isUnlimitedPlayers: Boolean,
    onIsUnlimitedPlayersChange: (Boolean) -> Unit,
    limitedCapacityInput: String,
    onLimitedCapacityChange: (String) -> Unit,
    createScheduledTimeString: String,
    onOpenSchedulePicker: () -> Unit,
    showPrizeBreakdownCard: Boolean,
    onTogglePrizeBreakdownCard: () -> Unit,
    prizeJaldi5: String,
    onPrizeJaldi5Change: (String) -> Unit,
    prizeCorners: String,
    onPrizeCornersChange: (String) -> Unit,
    prizeTopLine: String,
    onPrizeTopLineChange: (String) -> Unit,
    prizeMiddleLine: String,
    onPrizeMiddleLineChange: (String) -> Unit,
    prizeBottomLine: String,
    onPrizeBottomLineChange: (String) -> Unit,
    prizeFullHouse1: String,
    onPrizeFullHouse1Change: (String) -> Unit,
    prizeFullHouse2: String,
    onPrizeFullHouse2Change: (String) -> Unit,
    onCreateRoomSubmit: () -> Unit,
    editingRoomId: String?,
    onStartEditingRoom: (GameRoom) -> Unit,
    onCancelEdit: () -> Unit,
    editTitle: String,
    onEditTitleChange: (String) -> Unit,
    editPrize: String,
    onEditPrizeChange: (String) -> Unit,
    editFee: String,
    onEditFeeChange: (String) -> Unit,
    onSaveEditRoom: (GameRoom) -> Unit,
    onDeleteRoom: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CREATE NEW GAME ROOM (LIMITED / UNLIMITED SLOTS)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = RoyalPurple
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = roomTitleInput,
                    onValueChange = onRoomTitleChange,
                    label = { Text("Match Room Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prizeAmountInput,
                        onValueChange = onPrizeAmountChange,
                        label = { Text("Total Prize (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = entryFeeInput,
                        onValueChange = onEntryFeeChange,
                        label = { Text("Entry Fee (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Room Player Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(if (isUnlimitedPlayers) "Unlimited players" else "Limited slots (e.g. 10 players only)", fontSize = 11.sp, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = !isUnlimitedPlayers,
                            onClick = { onIsUnlimitedPlayersChange(false) },
                            label = { Text("Limited Slots") }
                        )
                        FilterChip(
                            selected = isUnlimitedPlayers,
                            onClick = { onIsUnlimitedPlayersChange(true) },
                            label = { Text("Unlimited") }
                        )
                    }
                }

                if (!isUnlimitedPlayers) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = limitedCapacityInput,
                        onValueChange = onLimitedCapacityChange,
                        label = { Text("Slot Seats Count (10, 20, 50)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Scheduled Match Timing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(createScheduledTimeString, fontSize = 11.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onOpenSchedulePicker) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Schedule")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTogglePrizeBreakdownCard() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("7-Tier Custom Prize Breakdown (₹)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberGold)
                    Text(if (showPrizeBreakdownCard) "▲ Hide" else "▼ Customize", fontSize = 11.sp, color = RoyalPurple)
                }

                AnimatedVisibility(visible = showPrizeBreakdownCard) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = prizeJaldi5,
                                onValueChange = onPrizeJaldi5Change,
                                label = { Text("Early 5 (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = prizeCorners,
                                onValueChange = onPrizeCornersChange,
                                label = { Text("Corners (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = prizeTopLine,
                                onValueChange = onPrizeTopLineChange,
                                label = { Text("Top Line (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = prizeMiddleLine,
                                onValueChange = onPrizeMiddleLineChange,
                                label = { Text("Mid Line (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = prizeBottomLine,
                                onValueChange = onPrizeBottomLineChange,
                                label = { Text("Bot Line (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = prizeFullHouse1,
                                onValueChange = onPrizeFullHouse1Change,
                                label = { Text("Full House 1st (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = prizeFullHouse2,
                                onValueChange = onPrizeFullHouse2Change,
                                label = { Text("Full House 2nd (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onCreateRoomSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PUBLISH MATCH ROOM TO FIRESTORE", fontWeight = FontWeight.Black)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIVE ROOMS & REAL-TIME SLOTS (${state.activeRooms.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = RoyalPurple
                )
                Spacer(modifier = Modifier.height(10.dp))

                state.activeRooms.forEach { room ->
                    val isEditing = editingRoomId == room.id
                    val isLimited = !room.isUnlimitedPlayers
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F6FA),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(room.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Prize: ₹${room.prizeAmount} | Entry: ₹${room.entryFee}", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = if (isLimited)
                                            "Slots Taken: ${room.currentPlayers} / ${room.maxPlayers} players"
                                        else
                                            "Unlimited Room (${room.currentPlayers} joined)",
                                        fontSize = 11.sp,
                                        color = RoyalPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onStartEditingRoom(room) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalPurple)
                                    }
                                    IconButton(onClick = { onDeleteRoom(room.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }

                            if (isEditing) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editTitle,
                                    onValueChange = onEditTitleChange,
                                    label = { Text("Edit Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = editPrize, onValueChange = onEditPrizeChange, label = { Text("Prize") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = editFee, onValueChange = onEditFeeChange, label = { Text("Fee") }, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = onCancelEdit) { Text("Cancel") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { onSaveEditRoom(room) }) { Text("Save Changes") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// MANUAL CALLER & REMOTE SECTION
// ====================================================================
@Composable
fun AdminManualCallerSection(
    state: TambolaUiState,
    typedNumberInput: String,
    onTypedNumberChange: (String) -> Unit,
    onCallSpecificNumber: (Int) -> Unit,
    onCallNextNumber: () -> Unit,
    onResetGame: () -> Unit,
    broadcastText: String,
    onBroadcastTextChange: (String) -> Unit,
    onBroadcastMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MANUAL NEXT NUMBER OVERRIDE 🎯",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = RoyalPurple
                        )
                        Text(
                            text = "Admin can manually type or select any number (1-90) to draw next",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Default.Dialpad, contentDescription = null, tint = AmberGold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedNumberInput,
                        onValueChange = {
                            if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                onTypedNumberChange(it)
                            }
                        },
                        label = { Text("Type Number (1-90)") },
                        placeholder = { Text("e.g. 42") },
                        leadingIcon = { Icon(Icons.Default.Casino, contentDescription = null, tint = RoyalPurple) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            val num = typedNumberInput.toIntOrNull()
                            if (num != null && num in 1..90) {
                                onCallSpecificNumber(num)
                            }
                        }),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val num = typedNumberInput.toIntOrNull()
                            if (num != null && num in 1..90) {
                                onCallSpecificNumber(num)
                            } else {
                                Toast.makeText(context, "Please enter a valid number between 1 and 90", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CALL TYPED 🎯", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCallNextNumber,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RANDOM DRAW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onResetGame,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET BOARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1-90 NUMBER GRID SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIRECT 1-90 NUMBERS MATRIX (TAP TO DRAW)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = RoyalPurple
                    )
                    Text(
                        text = "Called: ${state.calledNumbers.size}/90",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val calledSet = state.calledNumbers.toSet()

                for (row in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 1..10) {
                            val number = row * 10 + col
                            val isCalled = calledSet.contains(number)
                            val isLatest = state.currentCalledNumber == number

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isLatest -> AmberGold
                                    isCalled -> RoyalPurple.copy(alpha = 0.6f)
                                    else -> Color(0xFFF3E8FF)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isCalled) {
                                        onCallSpecificNumber(number)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$number",
                                        fontWeight = if (isLatest || !isCalled) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = when {
                                            isLatest -> Color.Black
                                            isCalled -> Color.White
                                            else -> RoyalPurple
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Chat Broadcast Alert
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BROADCAST TO LIVE PLAYERS 📢",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = RoyalPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = broadcastText,
                    onValueChange = onBroadcastTextChange,
                    label = { Text("Broadcast System Announcement") },
                    placeholder = { Text("e.g. Next match starting in 5 minutes!") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (broadcastText.isNotBlank()) {
                            onBroadcastMessage("📢 ADMIN: $broadcastText")
                            Toast.makeText(context, "Broadcast sent to room chat!", Toast.LENGTH_SHORT).show()
                            onBroadcastTextChange("")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SEND BROADCAST ALERT")
                }
            }
        }
    }
}

// ====================================================================
// STAFF & AGENT MANAGEMENT
// ====================================================================
@Composable
fun AdminStaffManagementSection(
    state: TambolaUiState,
    onOpenAddStaffDialog: () -> Unit,
    onToggleStaffBooking: (String, Boolean) -> Unit,
    onToggleStaffCreation: (String, Boolean) -> Unit,
    onDeleteStaff: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STAFF & AGENT ACCESS CONTROL 👥",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = RoyalPurple
                )
                Text(
                    text = "Generate Login IDs for Agents & Managers, and toggle booking access",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onOpenAddStaffDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ GENERATE LOGIN ID / ADD STAFF", fontWeight = FontWeight.Black)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REGISTERED STAFF ACCOUNTS (${state.staffUsers.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = RoyalPurple
                )

                Spacer(modifier = Modifier.height(10.dp))

                state.staffUsers.forEach { staff ->
                    var isPasswordShown by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F6FA),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (staff.role) {
                                            StaffRole.ADMIN -> AmberGold
                                            StaffRole.MANAGER -> Color(0xFF38BDF8)
                                            StaffRole.AGENT -> EmeraldGreen
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (staff.role) {
                                                    StaffRole.ADMIN -> Icons.Default.AdminPanelSettings
                                                    StaffRole.MANAGER -> Icons.Default.SupervisorAccount
                                                    StaffRole.AGENT -> Icons.Default.ConfirmationNumber
                                                },
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Role: ${staff.role.name} | Phone: ${staff.phone.ifBlank { "N/A" }}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                if (staff.role != StaffRole.ADMIN) {
                                    IconButton(onClick = { onDeleteStaff(staff.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Login ID: @${staff.loginId}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalPurple)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isPasswordShown) "Pass: ${staff.password}" else "Pass: ••••••••",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                        IconButton(
                                            onClick = { isPasswordShown = !isPasswordShown },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPasswordShown) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (staff.role == StaffRole.AGENT) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Ticket Booking Access", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(if (staff.isBookingAllowed) "Allowed (Active)" else "Revoked (Disabled)", fontSize = 10.sp, color = if (staff.isBookingAllowed) EmeraldGreen else Color.Red)
                                    }
                                    Switch(
                                        checked = staff.isBookingAllowed,
                                        onCheckedChange = { onToggleStaffBooking(staff.id, it) }
                                    )
                                }
                            } else if (staff.role == StaffRole.MANAGER) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Room Creation Access", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(if (staff.isCreationAllowed) "Allowed (Active)" else "Revoked (Disabled)", fontSize = 10.sp, color = if (staff.isCreationAllowed) EmeraldGreen else Color.Red)
                                    }
                                    Switch(
                                        checked = staff.isCreationAllowed,
                                        onCheckedChange = { onToggleStaffCreation(staff.id, it) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Stats: ${staff.totalTicketsBooked} tickets booked | Revenue: ₹${staff.totalAmountHandled}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// ADMIN PLAYER BOOKING SECTION
// ====================================================================
@Composable
fun AdminPlayerBookingSection(
    state: TambolaUiState,
    bookPlayerPhoneInput: String,
    onBookPhoneChange: (String) -> Unit,
    bookPlayerNameInput: String,
    onBookNameChange: (String) -> Unit,
    bookSelectedRoomId: String,
    onBookRoomChange: (String) -> Unit,
    bookTicketCount: Int,
    onBookCountChange: (Int) -> Unit,
    onSubmitBooking: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADMIN PLAYER TICKET BOOKING 🎟️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = RoyalPurple
                )
                Text(
                    text = "Book tickets directly on behalf of any player. Generates valid numbers & records to admin ledger.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = bookPlayerPhoneInput,
                    onValueChange = onBookPhoneChange,
                    label = { Text("Player Mobile *") },
                    placeholder = { Text("9876543210") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalPurple) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bookPlayerNameInput,
                    onValueChange = onBookNameChange,
                    label = { Text("Player Name *") },
                    placeholder = { Text("e.g. Ramesh Singh") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalPurple) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Game Room:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                state.activeRooms.forEach { room ->
                    val isSelected = room.id == bookSelectedRoomId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AmberGold.copy(alpha = 0.15f) else Color(0xFFF8F6FA),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AmberGold) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onBookRoomChange(room.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(room.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Fee: ₹${room.entryFee} | Prize: ₹${room.prizeAmount}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Ticket Count:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..6).forEach { count ->
                        FilterChip(
                            selected = bookTicketCount == count,
                            onClick = { onBookCountChange(count) },
                            label = { Text("$count", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSubmitBooking,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ISSUE & BOOK TICKETS", fontWeight = FontWeight.Black)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADMIN BOOKED TICKETS AUDIT LEDGER (${state.adminBookingsList.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = RoyalPurple
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (state.adminBookingsList.isEmpty()) {
                    Text("No direct admin bookings recorded yet.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    state.adminBookingsList.forEach { booking ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8F6FA),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(booking.playerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("₹${booking.amountPaid}", fontWeight = FontWeight.ExtraBold, color = EmeraldGreen, fontSize = 13.sp)
                                }
                                Text("Phone: ${booking.playerPhone} | Room: ${booking.roomTitle}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// ADMIN ORGANIZATION, UPI & AUDIT SECTION
// ====================================================================
@Composable
fun AdminOrganizationAndAuditSection(
    state: TambolaUiState,
    orgNameInput: String,
    onOrgNameChange: (String) -> Unit,
    orgPhoneInput: String,
    onOrgPhoneChange: (String) -> Unit,
    orgWhatsappInput: String,
    onOrgWhatsappChange: (String) -> Unit,
    orgEmailInput: String,
    onOrgEmailChange: (String) -> Unit,
    orgAddressInput: String,
    onOrgAddressChange: (String) -> Unit,
    orgUpiIdInput: String,
    onOrgUpiIdChange: (String) -> Unit,
    orgBankNameInput: String,
    onOrgBankNameChange: (String) -> Unit,
    orgAccountNumberInput: String,
    onOrgAccountNumberChange: (String) -> Unit,
    orgIfscInput: String,
    onOrgIfscChange: (String) -> Unit,
    orgRulesInput: String,
    onOrgRulesChange: (String) -> Unit,
    onSaveOrgInfo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ORGANIZATION, UPI & RULES 🏛️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = RoyalPurple
                )
                Text(
                    text = "Configure official contact, UPI payment receiver, and player regulations",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = orgNameInput,
                    onValueChange = onOrgNameChange,
                    label = { Text("Club / Organization Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = orgPhoneInput,
                        onValueChange = onOrgPhoneChange,
                        label = { Text("Support Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = orgWhatsappInput,
                        onValueChange = onOrgWhatsappChange,
                        label = { Text("WhatsApp Helpline") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = orgEmailInput,
                    onValueChange = onOrgEmailChange,
                    label = { Text("Support Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("ADMIN REVENUE UPI ID", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberGold)
                OutlinedTextField(
                    value = orgUpiIdInput,
                    onValueChange = onOrgUpiIdChange,
                    label = { Text("Admin UPI ID (GPay / PhonePe)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = orgBankNameInput, onValueChange = onOrgBankNameChange, label = { Text("Bank Name") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = orgIfscInput, onValueChange = onOrgIfscChange, label = { Text("IFSC") }, modifier = Modifier.weight(1f), singleLine = true)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = orgAccountNumberInput,
                    onValueChange = onOrgAccountNumberChange,
                    label = { Text("Bank Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("GAME RULES & REGULATIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalPurple)
                OutlinedTextField(
                    value = orgRulesInput,
                    onValueChange = onOrgRulesChange,
                    label = { Text("Official Rules") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSaveOrgInfo,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE & PUBLISH DETAILS", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// =========================================================================
// LIVE ROOM MONITOR COMPOSABLE (Booked vs Available & Force Close)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveRoomMonitorSection(
    state: TambolaUiState,
    onForceCloseRequest: (GameRoom) -> Unit,
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToCaller: () -> Unit,
    onStartEditingRoom: (GameRoom) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var expandedRoomId by remember { mutableStateOf<String?>(null) }

    val allRooms = state.activeRooms

    // Real-time telemetry computations
    val totalRooms = allRooms.size
    val liveRoomsCount = allRooms.count { it.isLive }
    val scheduledRoomsCount = allRooms.count { !it.isLive || it.scheduledStartTimeMs != null }

    val totalBookedTickets = allRooms.sumOf { room ->
        if (!room.isUnlimitedPlayers) room.ticketSlots.count { it.isBooked } else room.currentPlayers
    }
    val totalAvailableTickets = allRooms.sumOf { room ->
        if (!room.isUnlimitedPlayers) maxOf(0, room.maxPlayers - room.ticketSlots.count { it.isBooked }) else 0
    }
    val hasUnlimitedRooms = allRooms.any { it.isUnlimitedPlayers }
    val totalPrizeInPlay = allRooms.sumOf { it.prizeAmount }
    val totalEstimatedRevenue = allRooms.sumOf { room ->
        val booked = if (!room.isUnlimitedPlayers) room.ticketSlots.count { it.isBooked } else room.currentPlayers
        booked * room.entryFee
    }

    val filteredRooms = allRooms.filter { room ->
        val matchesSearch = searchQuery.isBlank() ||
                room.title.contains(searchQuery, ignoreCase = true) ||
                room.id.contains(searchQuery, ignoreCase = true) ||
                room.hostName.contains(searchQuery, ignoreCase = true) ||
                room.category.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Live" -> room.isLive
            "Scheduled" -> !room.isLive || room.scheduledStartTimeMs != null
            "Limited" -> !room.isUnlimitedPlayers
            "Unlimited" -> room.isUnlimitedPlayers
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // TOP TELEMETRY SUMMARY BOARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0A33)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreen,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE ROOM MONITOR 📡",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = AmberGold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                    ) {
                        Text(
                            text = "$totalRooms ACTIVE MATCHES",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Metric Telemetry Cards Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 1: Booked Tickets
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E1354)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎟️ BOOKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalBookedTickets",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberGold
                            )
                            Text("Tickets Sold", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    // Card 2: Available Tickets
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E1354)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🟢 AVAILABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasUnlimitedRooms) "$totalAvailableTickets+∞" else "$totalAvailableTickets",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                            Text("Open Slots", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    // Card 3: Total Prize Pools
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E1354)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💰 PRIZES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹$totalPrizeInPlay",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8)
                            )
                            Text("In Pool", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    // Card 4: Estimated Collections
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E1354)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💵 REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹$totalEstimatedRevenue",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFA7F3D0)
                            )
                            Text("Collected", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // SEARCH & FILTER BAR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search room title, ID, or host...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalPurple) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Live", "Scheduled", "Limited", "Unlimited").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val labelText = when (filter) {
                            "All" -> "All ($totalRooms)"
                            "Live" -> "🔴 Live ($liveRoomsCount)"
                            "Scheduled" -> "⏳ Sched ($scheduledRoomsCount)"
                            "Limited" -> "Limited"
                            "Unlimited" -> "Unlimited"
                            else -> filter
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(labelText, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // LIST OF ACTIVE GAMES
        if (filteredRooms.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No Matching Game Rooms Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoyalPurple)
                    Text("Create a new room or change your search filter.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToCreateRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Match Room", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            filteredRooms.forEach { room ->
                val isLimited = !room.isUnlimitedPlayers
                val bookedCount = if (isLimited) room.ticketSlots.count { it.isBooked } else room.currentPlayers
                val capacity = if (isLimited) room.maxPlayers else 0
                val availableCount = if (isLimited) maxOf(0, capacity - bookedCount) else 0
                val fillRatio = if (isLimited && capacity > 0) (bookedCount.toFloat() / capacity).coerceIn(0f, 1f) else 0.5f
                val isExpanded = expandedRoomId == room.id

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header Row: Status, Category, Host
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (room.isLive) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (room.isLive) Color(0xFFD32F2F) else AmberGold,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (room.isLive) "LIVE MATCH" else "SCHEDULED",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = if (room.isLive) Color(0xFFD32F2F) else Color(0xFFB45309)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = RoyalPurple.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "${room.iconEmoji} ${room.category}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = RoyalPurple,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Host: ${room.hostName}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title & ID
                        Text(
                            text = room.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "Room ID: ${room.id} ${if (room.scheduledTimeString.isNotBlank()) "• ⏰ ${room.scheduledTimeString}" else ""}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // TICKET TELEMETRY BOARD (BOOKED VS AVAILABLE)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF6F3FB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Booked count
                                    Column {
                                        Text("BOOKED TICKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalPurple)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🎟️ ", fontSize = 14.sp)
                                            Text(
                                                text = "$bookedCount",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp,
                                                color = RoyalPurple
                                            )
                                            Text(
                                                text = if (isLimited) " / $capacity" else " tickets",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }

                                    // Available count
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("AVAILABLE TICKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🟢 ", fontSize = 12.sp)
                                            Text(
                                                text = if (isLimited) "$availableCount" else "Unlimited",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp,
                                                color = if (isLimited && availableCount == 0) Color.Red else EmeraldGreen
                                            )
                                            if (isLimited) {
                                                Text(" left", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                }

                                if (isLimited) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { fillRatio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = when {
                                            fillRatio >= 1f -> Color.Red
                                            fillRatio >= 0.7f -> AmberGold
                                            else -> EmeraldGreen
                                        },
                                        trackColor = Color(0xFFE5E7EB)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${(fillRatio * 100).toInt()}% Capacity Filled",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = if (availableCount == 0) "⚠️ HOUSE FULL (Sold Out)" else "$availableCount seats remaining",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (availableCount == 0) Color.Red else EmeraldGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Financial Telemetry
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entry: ₹${room.entryFee} • Prize: ₹${room.prizeAmount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Collected: ₹${bookedCount * room.entryFee}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }

                        // EXPANDABLE SLOT / PLAYER BREAKDOWN
                        if (isLimited && room.ticketSlots.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { expandedRoomId = if (isExpanded) null else room.id },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = RoyalPurple
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isExpanded) "Hide Slot Roster" else "View Slot Roster & Booked Players ($bookedCount/$capacity)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPurple
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF9FAFB),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "RESERVED TICKET SLOTS MAP:",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = RoyalPurple
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        room.ticketSlots.forEach { slot ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (slot.isBooked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = if (slot.isBooked) Color(0xFFD32F2F) else EmeraldGreen,
                                                            modifier = Modifier.size(18.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text(
                                                                    text = "${slot.slotNumber}",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = if (slot.isBooked) slot.bookedByName.ifBlank { "Player (${slot.bookedByPhone})" } else "Slot #${slot.slotNumber} Available",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                color = if (slot.isBooked) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                                                            )
                                                            if (slot.isBooked && slot.bookedByPhone.isNotBlank()) {
                                                                Text(
                                                                    text = "📱 ${slot.bookedByPhone} • ${slot.paymentMethod}",
                                                                    fontSize = 9.sp,
                                                                    color = Color.DarkGray
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (slot.isBooked) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                                                    ) {
                                                        Text(
                                                            text = if (slot.isBooked) "BOOKED" else "FREE",
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 9.sp,
                                                            color = if (slot.isBooked) Color(0xFFB71C1C) else Color(0xFF2E7D32),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ACTION BUTTONS ROW (FORCE CLOSE & MANAGEMENT)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Prominent FORCE CLOSE Button
                            Button(
                                onClick = { onForceCloseRequest(room) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                                    .testTag("admin_force_close_btn_${room.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Force Close",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FORCE CLOSE 🛑", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            // 2. Caller Action
                            OutlinedButton(
                                onClick = onNavigateToCaller,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dialpad,
                                    contentDescription = "Call Numbers",
                                    tint = RoyalPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CALLER 🎯", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = RoyalPurple)
                            }

                            // 3. Edit Action
                            OutlinedButton(
                                onClick = { onStartEditingRoom(room) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("EDIT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

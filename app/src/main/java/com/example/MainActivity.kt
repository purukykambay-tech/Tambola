package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ClaimVerificationModal
import com.example.ui.components.MobileAuthModal
import com.example.ui.components.RazorpayModal
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AppLoginGateScreen
import com.example.ui.screens.CallerScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.TicketGeneratorScreen
import com.example.ui.screens.VerifierScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TambolaTheme
import com.example.viewmodel.ActiveTab
import com.example.viewmodel.TambolaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TambolaViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TambolaTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                if (!state.isLoggedIn && !state.isAdminAuthenticated) {
                    // GATE SCREEN: APP DOES NOT OPEN DIRECTLY
                    AppLoginGateScreen(
                        currentStep = state.authStep,
                        tempMobile = state.tempMobileInput,
                        otpInput = state.otpInput,
                        statusMessage = state.authStatusMessage,
                        onSendOtp = viewModel::sendMobileOtp,
                        onVerifyOtp = viewModel::verifyMobileOtp,
                        onLoginAdmin = viewModel::loginAdmin
                    )
                } else {
                    // AUTHENTICATED APP INTERFACE
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = state.activeTab == ActiveTab.LOBBY,
                                    onClick = { viewModel.selectTab(ActiveTab.LOBBY) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = "Lobby"
                                        )
                                    },
                                    label = { Text("Lobby") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE66700),
                                        indicatorColor = AmberGold.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.testTag("nav_lobby")
                                )

                                NavigationBarItem(
                                    selected = state.activeTab == ActiveTab.TICKET_GENERATOR,
                                    onClick = { viewModel.selectTab(ActiveTab.TICKET_GENERATOR) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.ConfirmationNumber,
                                            contentDescription = "Tickets"
                                        )
                                    },
                                    label = { Text("Tickets") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE66700),
                                        indicatorColor = AmberGold.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.testTag("nav_tickets")
                                )

                                NavigationBarItem(
                                    selected = state.activeTab == ActiveTab.CALLER_BOARD || state.activeTab == ActiveTab.SOLO_BOT_ROOM,
                                    onClick = { viewModel.selectTab(ActiveTab.CALLER_BOARD) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.CellTower,
                                            contentDescription = "Live Board"
                                        )
                                    },
                                    label = { Text("Live Board") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE66700),
                                        indicatorColor = AmberGold.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.testTag("nav_live_board")
                                )

                                NavigationBarItem(
                                    selected = state.activeTab == ActiveTab.CLAIM_VERIFIER,
                                    onClick = { viewModel.selectTab(ActiveTab.CLAIM_VERIFIER) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Forum,
                                            contentDescription = "Chat & Claims"
                                        )
                                    },
                                    label = { Text("Chat & Claims") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE66700),
                                        indicatorColor = AmberGold.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.testTag("nav_chat_claims")
                                )

                                NavigationBarItem(
                                    selected = state.activeTab == ActiveTab.ADMIN_PANEL,
                                    onClick = { viewModel.selectTab(ActiveTab.ADMIN_PANEL) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "Admin"
                                        )
                                    },
                                    label = { Text("Admin") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE66700),
                                        indicatorColor = AmberGold.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.testTag("nav_admin")
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (state.activeTab) {
                                ActiveTab.LOBBY -> {
                                    LobbyScreen(
                                        state = state,
                                        onOpenAuthModal = viewModel::openAuthModal,
                                        onOpenRazorpayModal = viewModel::openRazorpayModal,
                                        onSelectCategory = viewModel::setSelectedCategory,
                                        onJoinRoom = viewModel::joinRoom,
                                        onNavigateToTab = viewModel::selectTab
                                    )
                                }
                                ActiveTab.SOLO_BOT_ROOM -> {
                                    GamePlayScreen(
                                        state = state,
                                        onCallNextNumber = viewModel::callNextNumber,
                                        onToggleAutoCall = viewModel::toggleAutoCall,
                                        onSetTicketCount = viewModel::setTicketCount,
                                        onToggleMark = viewModel::toggleMarkNumber,
                                        onAutoMark = viewModel::autoMarkAllCalledNumbers,
                                        onClaimPrize = viewModel::claimPrize,
                                        onSendMessage = { msg -> viewModel.addChatMessage("You", msg) },
                                        onResetGame = viewModel::resetGame
                                    )
                                }
                                ActiveTab.CALLER_BOARD -> {
                                    CallerScreen(
                                        state = state,
                                        onCallNext = viewModel::callNextNumber,
                                        onToggleAutoCall = viewModel::toggleAutoCall,
                                        onSetIntervalSec = viewModel::setAutoCallInterval,
                                        onToggleSound = viewModel::toggleSound,
                                        onResetGame = viewModel::resetGame
                                    )
                                }
                                ActiveTab.CLAIM_VERIFIER -> {
                                    VerifierScreen(
                                        state = state,
                                        onEvaluateTicket = { ticket, claim ->
                                            viewModel.evaluateClaim(
                                                ticket = ticket,
                                                claimType = claim,
                                                calledSet = state.calledNumbers.toSet(),
                                                markedSet = state.markedNumbersMap[ticket.id] ?: emptySet()
                                            )
                                        }
                                    )
                                }
                                ActiveTab.TICKET_GENERATOR -> {
                                    TicketGeneratorScreen(
                                        state = state,
                                        onGenerateSheetOfSix = viewModel::generateSheetOfSix,
                                        onSaveTicket = viewModel::saveGeneratedTicket
                                    )
                                }
                                ActiveTab.GAME_HISTORY -> {
                                    HistoryScreen(
                                        state = state,
                                        onDeleteTicket = viewModel::deleteSavedTicket,
                                        onClearHistory = viewModel::clearHistory
                                    )
                                }
                                ActiveTab.ADMIN_PANEL -> {
                                    AdminPanelScreen(
                                        state = state,
                                        onLoginAdmin = viewModel::loginAdmin,
                                        onLogoutAdmin = viewModel::logoutAdmin,
                                        onCreateRoom = viewModel::createRoomByAdmin,
                                        onUpdateRoom = viewModel::updateAdminRoom,
                                        onDeleteRoom = viewModel::deleteAdminRoom,
                                        onCallNextNumber = viewModel::callNextNumber,
                                        onBroadcastMessage = { msg -> viewModel.addChatMessage("Admin", msg, isSystem = true) },
                                        onResetGame = viewModel::resetGame
                                    )
                                }
                            }

                            // Mobile OTP Auth Modal
                            MobileAuthModal(
                                isVisible = state.isAuthModalVisible,
                                authStep = state.authStep,
                                currentMobile = state.userMobileNumber,
                                statusMessage = state.authStatusMessage,
                                onSendOtp = viewModel::sendMobileOtp,
                                onVerifyOtp = viewModel::verifyMobileOtp,
                                onDismiss = viewModel::closeAuthModal
                            )

                            // Razorpay Payment Deposit Modal
                            RazorpayModal(
                                isVisible = state.isRazorpayModalVisible,
                                currentWalletBalance = state.walletBalance,
                                onPaymentSuccess = viewModel::processRazorpayPayment,
                                onDismiss = viewModel::closeRazorpayModal
                            )

                            // Claim Verification Result Modal Dialog
                            state.lastClaimResult?.let { result ->
                                ClaimVerificationModal(
                                    result = result,
                                    onDismiss = viewModel::dismissClaimResult
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

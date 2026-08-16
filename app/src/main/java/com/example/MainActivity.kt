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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
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
import com.example.ui.components.BuyTicketsModal
import com.example.ui.components.ClaimVerificationModal
import com.example.ui.components.GameSettingsModal
import com.example.ui.components.MobileAuthModal
import com.example.ui.components.RazorpayModal
import com.example.ui.components.WinnerNotificationToastBanner
import com.example.ui.screens.AdminDashboard
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AppLoginGateScreen
import com.example.ui.screens.CallerScreen
import com.example.ui.screens.GameHistoryScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.TicketGeneratorScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.VerifierScreen
import com.example.ui.screens.WalletTransactionHistoryScreen
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

                if (state.isStaffPortalOpen || state.isAdminAuthenticated || state.currentStaffUser != null) {
                    // STAFF / ADMIN / MANAGER / AGENT PORTAL (Outside normal player app)
                    AdminDashboard(
                        state = state,
                        onLoginAdmin = viewModel::loginAdmin,
                        onLogoutAdmin = viewModel::logoutStaff,
                        onCreateRoom = viewModel::createRoomByAdmin,
                        onUpdateRoom = viewModel::updateAdminRoom,
                        onDeleteRoom = viewModel::deleteAdminRoom,
                        onUpdateGameConfig = viewModel::updateGameConfiguration,
                        onBookTicketsForPlayer = viewModel::bookTicketsForPlayerByAdmin,
                        onUpdateOrgInfo = viewModel::updateAdminOrganizationDetails,
                        onCallNextNumber = viewModel::callNextNumber,
                        onCallSpecificNumber = viewModel::callSpecificNumber,
                        onCreateStaffUser = viewModel::createOrUpdateStaffUser,
                        onToggleStaffBookingPermission = viewModel::toggleStaffBookingPermission,
                        onToggleStaffCreationPermission = viewModel::toggleStaffCreationPermission,
                        onDeleteStaffUser = viewModel::deleteStaffUser,
                        onAgentBookTicket = viewModel::agentBookPlayerTicket,
                        onBroadcastMessage = { msg -> viewModel.addChatMessage("Admin", msg, isSystem = true) },
                        onResetGame = viewModel::resetGame
                    )
                } else if (!state.isLoggedIn) {
                    // GATE SCREEN: APP DOES NOT OPEN DIRECTLY (Staff portal button in top right corner)
                    AppLoginGateScreen(
                        currentStep = state.authStep,
                        tempMobile = state.tempMobileInput,
                        otpInput = state.otpInput,
                        statusMessage = state.authStatusMessage,
                        onSendOtp = viewModel::sendMobileOtp,
                        onVerifyOtp = viewModel::verifyMobileOtp,
                        onLoginStaff = { id, pass -> viewModel.loginStaff(id, pass) != null }
                    )
                } else {
                    // AUTHENTICATED PLAYER APP INTERFACE
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
                                    selected = state.activeTab == ActiveTab.PROFILE,
                                    onClick = { viewModel.selectTab(ActiveTab.PROFILE) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Profile"
                                        )
                                    },
                                    label = { Text("Profile") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFE66700),
                                        indicatorColor = AmberGold.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.testTag("nav_profile")
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
                                        onNavigateToTab = viewModel::selectTab,
                                        onOpenSettings = viewModel::openSettingsModal,
                                        onOpenBuyTicketsModal = { room -> viewModel.openBuyTicketsModal(room) }
                                    )
                                }
                                ActiveTab.PROFILE -> {
                                    UserProfileScreen(
                                        state = state,
                                        onUpdateNickname = viewModel::updateUserNickname,
                                        onUpdateAvatarUri = viewModel::updateUserAvatarUri,
                                        onUpdateAvatarPreset = viewModel::updateUserAvatarPreset,
                                        onUpdateBio = viewModel::updateUserBio,
                                        onOpenAuthModal = viewModel::openAuthModal,
                                        onOpenRazorpayModal = viewModel::openRazorpayModal,
                                        onNavigateBack = { viewModel.selectTab(ActiveTab.LOBBY) },
                                        onLogoutUser = viewModel::logoutUser
                                    )
                                }
                                ActiveTab.WALLET_HISTORY -> {
                                    WalletTransactionHistoryScreen(
                                        state = state,
                                        onOpenDepositModal = viewModel::openRazorpayModal,
                                        onWithdrawFunds = viewModel::withdrawFunds,
                                        onNavigateBack = { viewModel.selectTab(ActiveTab.LOBBY) }
                                    )
                                }
                                ActiveTab.SOLO_BOT_ROOM -> {
                                    GamePlayScreen(
                                        state = state,
                                        onCallNextNumber = viewModel::callNextNumber,
                                        onCallSpecificNumber = viewModel::callSpecificNumber,
                                        onToggleAutoCall = viewModel::toggleAutoCall,
                                        onSetIntervalSec = viewModel::setAutoCallInterval,
                                        onSetTicketCount = viewModel::setTicketCount,
                                        onToggleMark = viewModel::toggleMarkNumber,
                                        onAutoMark = viewModel::autoMarkAllCalledNumbers,
                                        onToggleAutoDab = viewModel::toggleAutoDab,
                                        onOpenSettings = viewModel::openSettingsModal,
                                        onClaimPrize = viewModel::claimPrize,
                                        onSendMessage = { msg -> viewModel.addChatMessage("You", msg) },
                                        onResetGame = viewModel::resetGame,
                                        onBroadcastMessage = { msg -> viewModel.addChatMessage("Admin 📢", msg, isSystem = true) }
                                    )
                                }
                                ActiveTab.CALLER_BOARD -> {
                                    CallerScreen(
                                        state = state,
                                        onCallNext = viewModel::callNextNumber,
                                        onCallSpecificNumber = viewModel::callSpecificNumber,
                                        onToggleAutoCall = viewModel::toggleAutoCall,
                                        onSetIntervalSec = viewModel::setAutoCallInterval,
                                        onToggleSound = viewModel::toggleSound,
                                        onResetGame = viewModel::resetGame,
                                        onToggleBackgroundService = viewModel::toggleFirestoreBackgroundCaller,
                                        onDrawViaFirestore = viewModel::drawNextFirestoreGameNumber,
                                        onResetFirestoreGame = viewModel::resetFirestoreGame,
                                        onBroadcastMessage = { msg -> viewModel.addChatMessage("Admin 📢", msg, isSystem = true) }
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
                                        onGenerateSingleTicket = viewModel::generateSingleTicket,
                                        onSaveTicket = viewModel::saveGeneratedTicket
                                    )
                                }
                                ActiveTab.GAME_HISTORY -> {
                                    GameHistoryScreen(
                                        state = state,
                                        onDeleteTicket = viewModel::deleteSavedTicket,
                                        onClearHistory = viewModel::clearHistory
                                    )
                                }
                                ActiveTab.ADMIN_PANEL -> {
                                    AdminDashboard(
                                        state = state,
                                        onLoginAdmin = viewModel::loginAdmin,
                                        onLogoutAdmin = viewModel::logoutStaff,
                                        onCreateRoom = viewModel::createRoomByAdmin,
                                        onUpdateRoom = viewModel::updateAdminRoom,
                                        onDeleteRoom = viewModel::deleteAdminRoom,
                                        onForceCloseRoom = viewModel::forceCloseRoom,
                                        onUpdateGameConfig = viewModel::updateGameConfiguration,
                                        onBookTicketsForPlayer = viewModel::bookTicketsForPlayerByAdmin,
                                        onUpdateOrgInfo = viewModel::updateAdminOrganizationDetails,
                                        onCallNextNumber = viewModel::callNextNumber,
                                        onCallSpecificNumber = viewModel::callSpecificNumber,
                                        onCreateStaffUser = viewModel::createOrUpdateStaffUser,
                                        onToggleStaffBookingPermission = viewModel::toggleStaffBookingPermission,
                                        onToggleStaffCreationPermission = viewModel::toggleStaffCreationPermission,
                                        onDeleteStaffUser = viewModel::deleteStaffUser,
                                        onAgentBookTicket = viewModel::agentBookPlayerTicket,
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

                            // Buy Tickets Modal (Direct Google Pay / Online Pay / Wallet with Admin Settlement)
                            BuyTicketsModal(
                                isVisible = state.isBuyTicketsModalVisible,
                                state = state,
                                room = state.selectedRoomForDirectBuy,
                                onDismiss = viewModel::closeBuyTicketsModal,
                                onBuyTicket = viewModel::buyTicketDirectly,
                                onBookSpecificSlot = viewModel::bookSpecificRoomSlot,
                                onOpenRazorpayTopup = viewModel::openRazorpayModal
                            )

                            // Claim Verification Result Modal Dialog
                            state.lastClaimResult?.let { result ->
                                ClaimVerificationModal(
                                    result = result,
                                    onDismiss = viewModel::dismissClaimResult
                                )
                            }

                            // Game Settings Modal Dialog
                            GameSettingsModal(
                                isVisible = state.isSettingsModalVisible,
                                isAutoDabEnabled = state.isAutoDabEnabled,
                                isAutoCalling = state.isAutoCalling,
                                isSoundEnabled = state.isSoundEnabled,
                                autoCallIntervalSec = state.autoCallIntervalSec,
                                onToggleAutoDab = viewModel::toggleAutoDab,
                                onToggleAutoCalling = viewModel::toggleAutoCall,
                                onToggleSound = viewModel::toggleSound,
                                onSetIntervalSec = viewModel::setAutoCallInterval,
                                onDismiss = viewModel::closeSettingsModal,
                                isLoggedIn = state.isLoggedIn,
                                userPhone = state.userMobileNumber,
                                onLogoutUser = viewModel::logoutUser
                            )

                            // Real-time FCM Winner Alert Toast Banner Overlay
                            WinnerNotificationToastBanner(
                                payload = state.activeWinnerNotification,
                                onDismiss = viewModel::dismissWinnerNotification
                            )
                        }
                    }
                }
            }
        }
    }
}

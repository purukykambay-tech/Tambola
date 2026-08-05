package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.FactCheck
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
import com.example.ui.screens.CallerScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HistoryScreen
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

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Tambola Game Studio",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            },
                            actions = {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = AmberGold,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = "${state.calledNumbers.size} Called",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = RoyalPurple
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = state.activeTab == ActiveTab.SOLO_BOT_ROOM,
                                onClick = { viewModel.selectTab(ActiveTab.SOLO_BOT_ROOM) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = "Play"
                                    )
                                },
                                label = { Text("Play Room") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    indicatorColor = AmberGold
                                ),
                                modifier = Modifier.testTag("nav_play_room")
                            )

                            NavigationBarItem(
                                selected = state.activeTab == ActiveTab.CALLER_BOARD,
                                onClick = { viewModel.selectTab(ActiveTab.CALLER_BOARD) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = "Caller"
                                    )
                                },
                                label = { Text("Caller Board") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    indicatorColor = AmberGold
                                ),
                                modifier = Modifier.testTag("nav_caller_board")
                            )

                            NavigationBarItem(
                                selected = state.activeTab == ActiveTab.CLAIM_VERIFIER,
                                onClick = { viewModel.selectTab(ActiveTab.CLAIM_VERIFIER) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FactCheck,
                                        contentDescription = "Verifier"
                                    )
                                },
                                label = { Text("Verifier") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    indicatorColor = AmberGold
                                ),
                                modifier = Modifier.testTag("nav_verifier")
                            )

                            NavigationBarItem(
                                selected = state.activeTab == ActiveTab.TICKET_GENERATOR,
                                onClick = { viewModel.selectTab(ActiveTab.TICKET_GENERATOR) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = "Generator"
                                    )
                                },
                                label = { Text("Generator") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    indicatorColor = AmberGold
                                ),
                                modifier = Modifier.testTag("nav_generator")
                            )

                            NavigationBarItem(
                                selected = state.activeTab == ActiveTab.GAME_HISTORY,
                                onClick = { viewModel.selectTab(ActiveTab.GAME_HISTORY) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History"
                                    )
                                },
                                label = { Text("History") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    indicatorColor = AmberGold
                                ),
                                modifier = Modifier.testTag("nav_history")
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
                        }

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

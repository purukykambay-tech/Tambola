package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.AdminOrganizationInfo
import com.example.model.GameConfiguration
import com.example.viewmodel.TambolaUiState

/**
 * AdminPanelScreen - Wrapper around secure AdminDashboard
 */
@Composable
fun AdminPanelScreen(
    state: TambolaUiState,
    onLoginAdmin: (adminId: String, pass: String) -> Boolean,
    onLogoutAdmin: () -> Unit,
    onCreateRoom: (title: String, host: String, category: String, prize: Int, entryFee: Int, isJackpot: Boolean, scheduledStartTimeMs: Long?, scheduledTimeString: String, isUnlimitedPlayers: Boolean, maxPlayers: Int, prizeBreakdown: Map<String, Int>) -> Unit,
    onUpdateRoom: (roomId: String, title: String, prize: Int, entryFee: Int, isLive: Boolean, scheduledStartTimeMs: Long?, scheduledTimeString: String) -> Unit,
    onDeleteRoom: (roomId: String) -> Unit,
    onCallNextNumber: () -> Unit,
    onBroadcastMessage: (String) -> Unit,
    onResetGame: () -> Unit,
    onUpdateGameConfig: (GameConfiguration) -> Unit = {},
    onBookTicketsForPlayer: (playerPhone: String, playerName: String, roomId: String, ticketCount: Int) -> Unit = { _, _, _, _ -> },
    onUpdateOrgInfo: (AdminOrganizationInfo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AdminDashboard(
        state = state,
        onLoginAdmin = onLoginAdmin,
        onLogoutAdmin = onLogoutAdmin,
        onCreateRoom = onCreateRoom,
        onUpdateRoom = onUpdateRoom,
        onDeleteRoom = onDeleteRoom,
        onUpdateGameConfig = onUpdateGameConfig,
        onBookTicketsForPlayer = onBookTicketsForPlayer,
        onUpdateOrgInfo = onUpdateOrgInfo,
        onCallNextNumber = onCallNextNumber,
        onBroadcastMessage = onBroadcastMessage,
        onResetGame = onResetGame,
        modifier = modifier
    )
}

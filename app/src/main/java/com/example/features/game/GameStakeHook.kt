package com.example.features.game

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.features.lobby.LobbyViewModel
import kotlinx.coroutines.launch

data class GameStakeState(
    val stakeDeducted: Boolean,
    val stakePending: Boolean,
    val entryError: String?
)

@Composable
fun useGameStake(
    stake: Double,
    gameName: String,
    viewModel: LobbyViewModel,
    feeAlreadyPaid: Boolean = false
): Triple<GameStakeState, (() -> Unit) -> Unit, kotlinx.coroutines.CoroutineScope> {
    var stakeDeducted by rememberSaveable { mutableStateOf(feeAlreadyPaid) }
    var stakePending by rememberSaveable { mutableStateOf(false) }
    var entryError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun requireStake(action: () -> Unit) {
        if (stakeDeducted) {
            action()
            return
        }
        if (stakePending) return
        stakePending = true
        scope.launch {
            val result = viewModel.deductStake(stake, "Entry: $gameName")
            stakePending = false
            if (result.isSuccess) {
                stakeDeducted = true
                entryError = null
                action()
            } else {
                entryError = result.exceptionOrNull()?.message ?: "Insufficient balance to play."
            }
        }
    }

    val state = GameStakeState(
        stakeDeducted = stakeDeducted,
        stakePending = stakePending,
        entryError = entryError
    )

    return Triple(state, ::requireStake, scope)
}
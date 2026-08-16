package com.example.features.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.lobby.LobbyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SnakesAndLaddersView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var pos by remember { mutableStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }
    var win by remember { mutableStateOf(false) }
    val stake = 10.0
    val target = 30
    val maxRolls = 10
    var rollsLeft by remember { mutableStateOf(maxRolls) }
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
            val result = viewModel.deductStake(stake, "Entry: Snakes & Ladders Rush")
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

    fun roll() {
        if (gameOver) return
        requireStake {
            triggerVibration()
            scope.launch {
                delay(300)
                val roll = Random.nextInt(1, 7)
                var newPos = (pos + roll).coerceAtMost(target + 5)

                // Snakes and ladders (hardcoded events)
                when (newPos) {
                    7 -> newPos = 15   // ladder
                    12 -> newPos = 5   // snake
                    18 -> newPos = 26  // ladder
                    22 -> newPos = 9   // snake
                    28 -> newPos = 30  // near win ladder
                }
                pos = newPos.coerceAtMost(target)
                rollsLeft--
                if (pos >= target) {
                    gameOver = true
                    win = true
                    val winnings = stake * 3.2
                    viewModel.awardWinnings(winnings) { }
                    viewModel.recordGamePlayed("Snakes & Ladders Rush", true, winnings)
                } else if (rollsLeft <= 0) {
                    gameOver = true
                    win = false
                    viewModel.recordGamePlayed("Snakes & Ladders Rush", false, 0.0)
                }
            }
        }
    }

    GameShell(
        title = "Snakes & Ladders Rush",
        subtitle = "Climb ladders, dodge snakes, reach 30",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text("Position: $pos / $target   Rolls left: $rollsLeft", color = GamingGoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Black)

        // Visual track
        LinearProgressIndicator(progress = { pos / target.toFloat() }, modifier = Modifier.fillMaxWidth().height(10.dp), color = GamingBrightGreen)

        Spacer(Modifier.height(24.dp))

        if (!gameOver) {
            Button(onClick = { roll() }, modifier = Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(GamingGoldAccent)) {
                Text("ROLL DICE & MOVE", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 18.sp)
            }
            Text("Watch out for snakes!", color = GamingTextMuted, fontSize = 13.sp)
        }

        if (gameOver) {
            GameResultOverlay(
                won = win,
                title = if (win) "You Reached 30!" else "Slid Too Far",
                subtitle = if (win) "You raced to the finish line" else "A snake dragged you back before the finish",
                amount = if (win) stake * 3.2 else 0.0,
                onPlayAgain = { pos = 1; gameOver = false; win = false; rollsLeft = maxRolls },
                onExit = onExit
            )
        }
    }
    }
}
}

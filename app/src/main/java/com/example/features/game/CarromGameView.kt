package com.example.features.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.lobby.LobbyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CarromGameView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var shots by remember { mutableStateOf(8) }
    var gameOver by remember { mutableStateOf(false) }
    var win by remember { mutableStateOf(false) }
    val stake = 30.0
    var strikerPos by remember { mutableStateOf(Offset(200f, 620f)) }
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
            val result = viewModel.deductStake(stake, "Entry: Carrom Pro League")
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

    fun shoot(strength: Float) {
        if (gameOver || shots <= 0) return
        requireStake {
            triggerVibration()
            shots--
            // Simple "physics": random pocket chance based on strength
            val pocketed = Random.nextFloat() < (0.35f + strength * 0.08f)
            if (pocketed) {
                score += 15 + Random.nextInt(0, 20)
            }
            if (shots <= 0) {
                gameOver = true
                win = score >= 70
                if (win) {
                    val winnings = stake * 1.8 + (score / 10)
                    viewModel.awardWinnings(winnings) { }
                    viewModel.recordGamePlayed("Carrom Pro League", true, winnings)
                } else {
                    viewModel.recordGamePlayed("Carrom Pro League", false, 0.0)
                }
            }
        }
    }

    GameShell(
        title = "Carrom Pro League",
        subtitle = "Pocket carrom men and the queen",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text("Score: $score   Shots left: $shots", color = GamingNeonCyan, fontSize = 14.sp)

        // Carrom board (simplified)
        Surface(
            color = Color(0xFF3E2A1F),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { _, dragAmount ->
                                strikerPos = (strikerPos + dragAmount * 0.6f).run {
                                    Offset(x.coerceIn(80f, 320f), y.coerceIn(500f, 680f))
                                }
                            },
                            onDragEnd = {
                                val strength = (strikerPos.x / 300f).coerceIn(0.3f, 1.8f)
                                shoot(strength)
                            }
                        )
                    }
            ) {
                // Pocket indicators
                listOf(Offset(60f,60f), Offset(340f,60f), Offset(60f,460f), Offset(340f,460f)).forEach { p ->
                    Canvas(Modifier.size(20.dp).offset { IntOffset(p.x.toInt(), p.y.toInt()) }) {
                        drawCircle(GamingGoldAccent, radius = 12f)
                    }
                }
                // Striker
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White, radius = 18f, center = strikerPos)
                }
                Text("Drag striker down & release to shoot", color = Color.White.copy(0.6f), modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
            }
        }

        if (!gameOver) {
            Button(onClick = { shoot(1.0f) }, colors = ButtonDefaults.buttonColors(GamingGoldAccent), modifier = Modifier.fillMaxWidth()) {
                Text("QUICK SHOOT (Center)", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        if (gameOver) {
            GameResultOverlay(
                won = win,
                title = if (win) "Pockets Cleared!" else "Tough Board",
                subtitle = if (win) "You pocketed the queen and sealed the win" else "The board got the better of you this round",
                amount = if (win) (stake * 1.8 + score / 8) else 0.0,
                onPlayAgain = { score = 0; shots = 8; gameOver = false; win = false; strikerPos = Offset(200f, 620f) },
                onExit = onExit
            )
        }
    }
    }
}
}

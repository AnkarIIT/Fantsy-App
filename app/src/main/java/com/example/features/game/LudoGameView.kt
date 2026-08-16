package com.example.features.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

private val redColor = Color(0xFFEF5350)
private val greenColor = Color(0xFF66BB6A)
private const val BASE = -1
private const val DONE = 58

private fun trackPos(g: Int): Pair<Int, Int> {
    val row = g / 13
    val col = g % 13
    return if (row % 2 == 0) col to row else (12 - col) to row
}

private fun canMove(p: Int, dice: Int): Boolean =
    when (p) {
        BASE -> dice == 6
        DONE -> false
        else -> p + dice <= DONE
    }

private fun applyLudoMove(tokens: IntArray, idx: Int, dice: Int, start: Int, opponentTokens: IntArray, opponentStart: Int): IntArray {
    val next = tokens.copyOf()
    val cur = next[idx]
    next[idx] = if (cur == BASE) 0 else cur + dice
    val newPos = next[idx]
    if (newPos in 0..51) {
        val global = (newPos + start) % 52
        for (i in opponentTokens.indices) {
            val opp = opponentTokens[i]
            if (opp in 0..51 && (opp + opponentStart) % 52 == global) {
                opponentTokens[i] = BASE
            }
        }
    }
    return next
}

@Composable
fun LudoGameView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var redTokens by remember { mutableStateOf(intArrayOf(BASE, BASE, BASE, BASE)) }
    var greenTokens by remember { mutableStateOf(intArrayOf(BASE, BASE, BASE, BASE)) }
    var turn by remember { mutableStateOf(0) } // 0 = human red, 1 = AI green
    var dice by remember { mutableStateOf(0) }
    var rolling by remember { mutableStateOf(false) }
    var awaitingChoice by remember { mutableStateOf(false) }
    var movableIndices by remember { mutableStateOf(emptyList<Int>()) }
    var message by remember { mutableStateOf("Your turn — roll the dice") }
    var gameOver by remember { mutableStateOf(false) }
    var win by remember { mutableStateOf(false) }
    var stakeDeducted by rememberSaveable { mutableStateOf(feeAlreadyPaid) }
    var stakePending by rememberSaveable { mutableStateOf(false) }
    var entryError by remember { mutableStateOf<String?>(null) }
    val stake = 20.0
    val scope = rememberCoroutineScope()

    fun requireStake(action: () -> Unit) {
        if (stakeDeducted) {
            action()
            return
        }
        if (stakePending) return
        stakePending = true
        scope.launch {
            val result = viewModel.deductStake(stake, "Entry: Ludo Empire")
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

    fun isHome(tokens: IntArray) = tokens.all { it == DONE }

    fun endGame(redWon: Boolean) {
        gameOver = true
        win = redWon
        val winnings = if (redWon) stake * 2.8 else 0.0
        if (redWon) viewModel.awardWinnings(winnings) { }
        viewModel.recordGamePlayed("Ludo Empire", redWon, winnings)
        message = if (redWon) "All four tokens home — YOU WIN!" else "AI brought all tokens home."
    }

    fun afterHumanMove(movedTokens: IntArray) {
        redTokens = movedTokens
        if (isHome(movedTokens)) { endGame(true); return }
        if (dice == 6) {
            message = "You rolled a 6 — roll again!"
            return
        }
        turn = 1
        message = "AI is rolling..."
    }

    fun humanRoll() {
        if (rolling || gameOver || turn != 0 || awaitingChoice) return
        requireStake {
            rolling = true
            triggerVibration()
            scope.launch {
                val roll = Random.nextInt(1, 7)
                dice = roll
                delay(500)
                val movable = (0 until 4).filter { canMove(redTokens[it], roll) }
                rolling = false
                if (movable.isEmpty()) {
                    message = "No token can move with a $roll. AI's turn."
                    turn = 1
                } else if (movable.size == 1) {
                    afterHumanMove(applyLudoMove(redTokens, movable[0], roll, start = 0, opponentTokens = greenTokens, opponentStart = 13))
                } else {
                    movableIndices = movable
                    awaitingChoice = true
                    message = "Rolled $roll — choose a token to move."
                }
            }
        }
    }

    fun humanChoose(idx: Int) {
        if (!awaitingChoice || idx !in movableIndices) return
        awaitingChoice = false
        movableIndices = emptyList()
        afterHumanMove(applyLudoMove(redTokens, idx, dice, start = 0, opponentTokens = greenTokens, opponentStart = 13))
    }

    fun runAi() {
        scope.launch {
            delay(900)
            var aiTokens = greenTokens.copyOf()
            var aiTurn = true
            var rolls = 0
            while (aiTurn && rolls < 3) {
                val roll = Random.nextInt(1, 7)
                dice = roll
                delay(450)
                val movable = (0 until 4).filter { canMove(aiTokens[it], roll) }
                if (movable.isNotEmpty()) {
                    val choice = movable.random()
                    aiTokens = applyLudoMove(aiTokens, choice, roll, start = 13, opponentTokens = redTokens, opponentStart = 0)
                    redTokens = redTokens.copyOf()
                }
                rolls++
                if (isHome(aiTokens)) { greenTokens = aiTokens; endGame(false); return@launch }
                aiTurn = roll == 6
            }
            greenTokens = aiTokens
            if (!gameOver) {
                turn = 0
                awaitingChoice = false
                message = "Your turn — roll the dice"
            }
        }
    }

    LaunchedEffect(turn) {
        if (turn == 1 && !gameOver) runAi()
    }

    GameShell(
        title = "Ludo Empire",
        subtitle = "First to bring all four tokens home wins",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text(message, color = GamingGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)

        Surface(color = GamingDeepSurface, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, GamingBorderSlate)) {
            Canvas(Modifier.fillMaxWidth().padding(12.dp).height(150.dp)) {
                val cell = size.width / 13f
                val pad = 2f
                for (g in 0..51) {
                    val (c, r) = trackPos(g)
                    val x = c * cell + pad
                    val y = r * cell + pad
                    val isStart = g == 0 || g == 13
                    drawRoundRect(
                        color = if (isStart) Color(0xFF37474F) else Color(0xFF263238),
                        topLeft = Offset(x, y),
                        size = Size(cell - pad * 2, cell - pad * 2),
                        cornerRadius = CornerRadius(4f)
                    )
                    if (isStart) {
                        val col = if (g == 0) redColor else greenColor
                        drawCircle(col, radius = (cell - pad * 2) / 2, center = Offset(x + (cell - pad * 2) / 2, y + (cell - pad * 2) / 2))
                    }
                }
                // tokens (red then green), stacked with small offset when shared
                fun drawTokens(tokens: IntArray, color: Color, start: Int) {
                    for (idx in 0 until 4) {
                        val p = tokens[idx]
                        if (p == BASE || p == DONE) continue
                        if (p in 0..51) {
                            val global = (p + start) % 52
                            val (c, r) = trackPos(global)
                            val cx = c * cell + cell / 2 + idx * 2f - 3f
                            val cy = r * cell + cell / 2 + idx * 2f - 3f
                            drawCircle(color, radius = cell * 0.3f, center = Offset(cx, cy))
                        } else {
                            val laneCol = if (start == 0) 0.5f else 12.5f
                            val laneRow = (p - 52) * (cell / 6f)
                            drawCircle(color, radius = cell * 0.25f, center = Offset(laneCol * cell, 3 * cell + 12f - laneRow))
                        }
                    }
                }
                drawTokens(redTokens, redColor, 0)
                drawTokens(greenTokens, greenColor, 13)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("YOU (${redTokens.count { it == DONE }}/4)", color = redColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${redTokens.count { it == BASE }} in base", color = GamingTextMuted, fontSize = 10.sp)
            }
            Surface(color = GamingDeepSurface, shape = RoundedCornerShape(12.dp), border = BorderStroke(2.dp, GamingNeonCyan), modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (rolling || turn == 1) "🎲" else dice.toString(), fontSize = 34.sp, fontWeight = FontWeight.Black, color = GamingGoldAccent)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AI (${greenTokens.count { it == DONE }}/4)", color = greenColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${greenTokens.count { it == BASE }} in base", color = GamingTextMuted, fontSize = 10.sp)
            }
        }

        if (!gameOver) {
            if (awaitingChoice) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    movableIndices.forEach { idx ->
                        Button(onClick = { humanChoose(idx) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(GamingNeonCyan)) {
                            Text("TOKEN ${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            } else {
                Button(
                    onClick = { humanRoll() },
                    enabled = turn == 0 && !rolling,
                    colors = ButtonDefaults.buttonColors(containerColor = GamingNeonCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(if (turn == 1) "AI IS PLAYING..." else "ROLL DICE", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }

        if (gameOver) {
            GameResultOverlay(
                won = win,
                title = if (win) "All Home — You Win!" else "AI Wins",
                subtitle = if (win) "All four tokens made it home safely" else "The AI brought all four tokens home first",
                amount = if (win) stake * 2.8 else 0.0,
                onPlayAgain = {
                    redTokens = intArrayOf(BASE, BASE, BASE, BASE)
                    greenTokens = intArrayOf(BASE, BASE, BASE, BASE)
                    turn = 0; dice = 0; rolling = false; awaitingChoice = false
                    movableIndices = emptyList(); gameOver = false; win = false
                    message = "Your turn — roll the dice"
                },
                onExit = onExit
            )
        }
    }
    }
}
}

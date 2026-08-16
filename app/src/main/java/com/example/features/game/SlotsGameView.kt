package com.example.features.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.lobby.LobbyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@Composable
fun SlotsGameView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    val symbols = listOf("🍒", "🍋", "🔔", "💎", "7️⃣", "🍇", "⭐", "🎰")
    val reelAnims = remember {
        listOf(
            Animatable(symbols.indexOf("🍒").toFloat()),
            Animatable(symbols.indexOf("🍋").toFloat()),
            Animatable(symbols.indexOf("🔔").toFloat())
        )
    }
    var spinning by remember { mutableStateOf(false) }
    var lastWin by remember { mutableStateOf(0.0) }
    var winningReels by remember { mutableStateOf<List<Boolean>?>(null) }
    var gameOver by remember { mutableStateOf(false) }
    val stake = 10.0
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
            val result = viewModel.deductStake(stake, "Entry: Golden Pharaoh Spins")
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

    fun spin() {
        if (spinning || gameOver) return
        requireStake {
            val finals = listOf(symbols.random(), symbols.random(), symbols.random())
            spinning = true
            winningReels = null
            triggerVibration()
            scope.launch {
                val jobs = finals.mapIndexed { i, s ->
                    launch {
                        if (i > 0) delay(i * 180L)
                        val targetIndex = symbols.indexOf(s)
                        val base = reelAnims[i].value + 130 + i * 55
                        var target = base
                        while (target.toInt() % symbols.size != targetIndex) target += 1
                        reelAnims[i].animateTo(target, tween(1050, easing = FastOutSlowInEasing))
                    }
                }
                jobs.joinAll()
                spinning = false

                val win = if (finals[0] == finals[1] && finals[1] == finals[2]) stake * 8 else if (finals[0] == finals[1] || finals[1] == finals[2]) stake * 2 else 0.0
                lastWin = win
                winningReels = listOf(finals[0] == finals[1], finals[1] == finals[2], finals[0] == finals[2])
                if (win > 0) {
                    triggerVibration()
                    viewModel.awardWinnings(win) { }
                    viewModel.recordGamePlayed("Golden Pharaoh Spins", true, win)
                } else {
                    viewModel.recordGamePlayed("Golden Pharaoh Spins", false, 0.0)
                }
                delay(1100)
                gameOver = true
            }
        }
    }

    GameShell(
        title = "Golden Pharaoh Spins",
        subtitle = "Match 3 symbols to unlock the jackpot",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                entryError?.let {
                    Surface(
                        color = Color(0xFFFF5252).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            it,
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    "Stake per spin: ₹$stake",
                    color = GamingTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = GamingDeepSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, GamingGoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 22.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "GOLDEN REELS",
                            color = GamingGoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            reelAnims.indices.forEach { i ->
                                val idx = ((reelAnims[i].value.toInt() % symbols.size) + symbols.size) % symbols.size
                                val sym = symbols[idx]
                                val isWin = winningReels?.get(i) == true
                                Surface(
                                    color = if (isWin) GamingGoldAccent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(2.dp, if (isWin) GamingGoldAccent else GamingBorderSlate),
                                    modifier = Modifier
                                        .size(72.dp)
                                        .graphicsLayer {
                                            scaleX = if (isWin) 1.08f else 1f
                                            scaleY = if (isWin) 1.08f else 1f
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            sym,
                                            fontSize = 40.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (!gameOver) {
                    Button(
                        onClick = { spin() },
                        enabled = !spinning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GamingNeonCyan,
                            disabledContainerColor = GamingBorderSlate
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (spinning) "SPINNING..." else "SPIN THE REELS  •  ₹$stake",
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "3 of a kind = 8x  •  2 matching = 2x",
                        color = GamingTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            if (gameOver) {
                GameResultOverlay(
                    won = lastWin > 0,
                    title = if (lastWin > 0) "Jackpot!" else "No Match",
                    subtitle = if (lastWin > 0) "The golden reels aligned in your favor" else "The pharaoh was not in your favor this round",
                    amount = lastWin,
                    onPlayAgain = {
                        scope.launch {
                            reelAnims[0].snapTo(symbols.indexOf("🍒").toFloat())
                            reelAnims[1].snapTo(symbols.indexOf("🍋").toFloat())
                            reelAnims[2].snapTo(symbols.indexOf("🔔").toFloat())
                        }
                        winningReels = null
                        lastWin = 0.0
                        gameOver = false
                    },
                    onExit = onExit
                )
            }
        }
    }
}

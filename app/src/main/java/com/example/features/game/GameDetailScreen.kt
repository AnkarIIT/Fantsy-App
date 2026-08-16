package com.example.features.game

import android.os.Build
import android.os.Vibrator
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.lobby.LobbyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface MatchmakingState {
    object Searching : MatchmakingState
    data class OpponentFound(val opponentName: String, val winStreak: Int, val avatarUrl: String) : MatchmakingState
    object GameActive : MatchmakingState
    data class Completed(val multiplierSecured: Double, val profitEarned: Double, val status: String) : MatchmakingState
    object InsufficientFunds : MatchmakingState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    matchId: String,
    viewModel: LobbyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val scope = rememberCoroutineScope()

    var matchState by remember { mutableStateOf<MatchmakingState>(MatchmakingState.Searching) }
    var opponentNameState by remember { mutableStateOf("Player_Delta") }
    var opponentStreakState by remember { mutableStateOf(4) }
    var sessionCount by remember { mutableStateOf(0) }

    // Tournament ids (m_*) already deduct their entry fee via registerForTournament,
    // so those entries are pre-paid. Featured grid ids (g_*) must pay the game stake.
    val feeAlreadyPaid = matchId.startsWith("m_")
    var stakeDeducted by rememberSaveable { mutableStateOf(false) }

    // Dedicated games (ludo, carrom, bubble, snake, andar, rummy, teenpatti, slots, chess)
    // manage their own stake via requireStake. Only the rocket multiplier crash game is
    // driven by the matchmaking flow below, so the entry stake is only deducted for it.
    val isCrashGame = matchId.lowercase().let { lower ->
        !(listOf("ludo", "carrom", "bubble", "snake", "andar", "rummy", "teenpatti", "slots", "chess")
            .any { lower.contains(it) })
    }

    // Multiplier crash simulator values
    var multiplier by remember { mutableStateOf(1.0) }
    var rocketCrashed by remember { mutableStateOf(false) }
    var cashOutSuccess by remember { mutableStateOf(false) }
    var payoutWon by remember { mutableStateOf(0.0) }
    var initialStakeFee by remember { mutableStateOf(10.0) }
    var crashThreshold by remember { mutableStateOf(1.0) }

    fun triggerVibration(pattern: LongArray? = null, strength: Int = android.os.VibrationEffect.DEFAULT_AMPLITUDE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (pattern != null) {
                vibrator?.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
            } else {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(40, strength))
            }
        } else {
            @Suppress("DEPRECATION")
            if (pattern != null) vibrator?.vibrate(pattern, -1) else vibrator?.vibrate(40)
        }
    }

    // Matchmaking simulator (crash game only — dedicated games handle their own stake)
    LaunchedEffect(key1 = matchId, key2 = sessionCount) {
        if (!isCrashGame) return@LaunchedEffect

        // Step 0: Pay entry stake once per arena entry (pre-paid when entered via tournament)
        if (!feeAlreadyPaid && !stakeDeducted) {
            stakeDeducted = true
            val paid = viewModel.deductStake(initialStakeFee, "Entry: Rocket Multiplier X")
            if (paid.isFailure) {
                matchState = MatchmakingState.InsufficientFunds
                return@LaunchedEffect
            }
        }

        // Step 1: Search Matchmaking for 2.5 seconds
        delay(2500)
        val names = listOf("Stalker_09", "Esports_God", "CashViper", "CardMasterPro", "AviatorQueen", "LudoChampX")
        opponentNameState = names[Random.nextInt(names.size)]
        opponentStreakState = Random.nextInt(2, 8)
        matchState = MatchmakingState.OpponentFound(opponentNameState, opponentStreakState, "")
        triggerVibration(longArrayOf(0, 100, 80, 150))

        // Step 2: Show Found Opponent for 2.0 seconds
        delay(2000)
        multiplier = 1.0
        rocketCrashed = false
        cashOutSuccess = false
        payoutWon = 0.0
        // Set a random threshold where the game will crash (e.g. between 1.5 and 7.5)
        crashThreshold = Random.nextDouble(1.3, 8.5)
        matchState = MatchmakingState.GameActive
        triggerVibration()
    }

    // Live Game Multiplier Loop
    LaunchedEffect(key1 = matchState, key2 = rocketCrashed, key3 = cashOutSuccess) {
        if (matchState is MatchmakingState.GameActive && !rocketCrashed && !cashOutSuccess) {
            while (true) {
                delay(120) // Multiplier speed tick
                val increment = when {
                    multiplier < 2.0 -> 0.05
                    multiplier < 4.0 -> 0.12
                    else -> 0.28
                }
                val nextMultiplier = multiplier + increment
                if (nextMultiplier >= crashThreshold) {
                    // CRASH!
                    rocketCrashed = true
                    triggerVibration(longArrayOf(0, 500), strength = 255)
                    matchState = MatchmakingState.Completed(0.0, 0.0, "CRASHED")
                    break
                } else {
                    multiplier = nextMultiplier
                    // Mild pulse feedback at major multipliers
                    if (multiplier.toInt() > (multiplier - increment).toInt()) {
                        triggerVibration()
                    }
                }
            }
        }
    }

    val lowerId = matchId.lowercase()

    when {
        // Dedicated game experiences render full-screen with their own GameShell chrome
        lowerId.contains("ludo") || lowerId.contains("g_ludo") -> {
            LudoGameView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("carrom") || lowerId.contains("g_carrom") -> {
            CarromGameView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("bubble") || lowerId.contains("g_bubble") -> {
            BubbleShooterView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("snake") || lowerId.contains("g_snakes") -> {
            SnakesAndLaddersView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("andar") || lowerId.contains("g_andar") -> {
            AndarBaharView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("rummy") || lowerId.contains("g_rummy") || lowerId.contains("m_rummy") -> {
            RummyGameView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("teenpatti") || lowerId.contains("g_teenpatti") || lowerId.contains("m_teenpatti") -> {
            TeenPattiGameView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("slots") || lowerId.contains("g_slots") -> {
            SlotsGameView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }
        lowerId.contains("chess") || lowerId.contains("g_chess") || lowerId.contains("m_chess") -> {
            ChessGameView(
                viewModel = viewModel,
                onExit = onNavigateBack,
                feeAlreadyPaid = feeAlreadyPaid,
                triggerVibration = { triggerVibration() }
            )
        }

        else -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "ROYALE LIVE ARENA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                triggerVibration()
                                onNavigateBack()
                            }) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Go Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    when {
                        // Original rocket crash / multiplier game (Aviator)
                        lowerId.contains("aviator") || lowerId.contains("multiplier") || lowerId == "g_aviator" -> {
                    when (val state = matchState) {
                        is MatchmakingState.Searching -> {
                            SearchingLobbyView()
                        }
                        is MatchmakingState.InsufficientFunds -> {
                            InsufficientFundsView(onReturn = onNavigateBack, onAddCash = onNavigateToWallet)
                        }
                        is MatchmakingState.OpponentFound -> {
                            OpponentMatchedView(playerName = state.opponentName, streak = state.winStreak)
                        }
                        is MatchmakingState.GameActive -> {
                            ActiveGamePlayView(
                                multiplier = multiplier,
                                stake = initialStakeFee,
                                isCrashed = rocketCrashed,
                                isSecured = cashOutSuccess,
                                onCashOut = {
                                    cashOutSuccess = true
                                    payoutWon = initialStakeFee * multiplier
                                    val profit = payoutWon - initialStakeFee
                                    matchState = MatchmakingState.Completed(multiplier, profit, "SUCCESS")
                                    triggerVibration(longArrayOf(0, 100, 50, 100))

                                    // Credit only the net profit to winnings (the stake was
                                    // already deducted from the deposit balance on entry).
                                    viewModel.awardWinnings(profit) { /* fire-and-forget for demo */ }
                                    viewModel.recordGamePlayed("Rocket Multiplier X", true, profit)
                                }
                            )
                        }
                        is MatchmakingState.Completed -> {
                            GameFinishedView(
                                opponentName = opponentNameState,
                                multiplier = multiplier,
                                payout = payoutWon,
                                stake = initialStakeFee,
                                status = state.status,
                                onPlayAgain = {
                                    triggerVibration()
                                    stakeDeducted = false; sessionCount++
                                    matchState = MatchmakingState.Searching
                                },
                                onNavigateHome = onNavigateBack
                            )
                        }
                    }
                }

                else -> {
                    // Unknown game id - graceful fallback to the classic rocket experience
                    when (val state = matchState) {
                        is MatchmakingState.Searching -> SearchingLobbyView()
                        is MatchmakingState.InsufficientFunds -> InsufficientFundsView(onReturn = onNavigateBack, onAddCash = onNavigateToWallet)
                        is MatchmakingState.OpponentFound -> OpponentMatchedView(playerName = state.opponentName, streak = state.winStreak)
                        is MatchmakingState.GameActive -> ActiveGamePlayView(
                            multiplier = multiplier,
                            stake = initialStakeFee,
                            isCrashed = rocketCrashed,
                            isSecured = cashOutSuccess,
                                onCashOut = {
                                    cashOutSuccess = true
                                    payoutWon = initialStakeFee * multiplier
                                    val profit = payoutWon - initialStakeFee
                                    matchState = MatchmakingState.Completed(multiplier, profit, "SUCCESS")
                                    triggerVibration(longArrayOf(0, 100, 50, 100))
                                    viewModel.awardWinnings(profit) { }
                                    viewModel.recordGamePlayed("Rocket Multiplier X", true, profit)
                                }
                        )
                        is MatchmakingState.Completed -> GameFinishedView(
                            opponentName = opponentNameState,
                            multiplier = multiplier,
                            payout = payoutWon,
                            stake = initialStakeFee,
                            status = state.status,
                            onPlayAgain = {
                                triggerVibration()
                                stakeDeducted = false; sessionCount++
                                matchState = MatchmakingState.Searching
                            },
                            onNavigateHome = onNavigateBack
                        )
                    }
                }
                }
            }
        }
    }
}
}

@Composable
fun SearchingLobbyView() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    
    // Spinning ripple ring layout
    val sizeAnim by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    val opacityAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Ripple layer
            Box(
                modifier = Modifier
                    .size(sizeAnim.dp)
                    .drawBehind {
                        drawCircle(
                            color = GamingNeonCyan.copy(alpha = opacityAnim),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            )

            // Inner circle radar
            Surface(
                modifier = Modifier
                    .size(130.dp)
                    .padding(10.dp),
                shape = CircleShape,
                color = GamingDeepSurface,
                border = BorderStroke(2.dp, GamingNeonCyan)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.WifiFind,
                        contentDescription = "Radar",
                        tint = GamingNeonCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "MATCHING LIVE DRIFTERS...",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Establishing secure game session, please wait",
            color = GamingTextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OpponentMatchedView(playerName: String, streak: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MATCH CONFLICT FOUND",
            color = GamingGoldAccent,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Box (You)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = GamingDeepSurface,
                    modifier = Modifier.size(80.dp),
                    border = BorderStroke(2.dp, GamingNeonCyan)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = "Me", tint = GamingNeonCyan, modifier = Modifier.size(48.dp))
                    }
                }
                Text("YOU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Level 28 Gold", color = GamingTextMuted, fontSize = 10.sp)
            }

            // VS Visual representation
            Surface(
                shape = CircleShape,
                color = Color.Red,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("VS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Enemy Box (Opponent)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = GamingDeepSurface,
                    modifier = Modifier.size(80.dp),
                    border = BorderStroke(2.dp, Color(0xFFFF3D00))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Face, contentDescription = "Me", tint = Color(0xFFFF3D00), modifier = Modifier.size(48.dp))
                    }
                }
                Text(playerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = "$streak Win Streak",
                        color = Color.Red,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Starting battle table in 1 second...", color = Color.White, fontSize = 13.sp)
            LinearProgressIndicator(color = GamingNeonCyan, trackColor = GamingBorderSlate, modifier = Modifier.width(180.dp).clip(CircleShape))
        }
    }
}

@Composable
fun InsufficientFundsView(onReturn: () -> Unit, onAddCash: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.AccountBalanceWallet,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "INSUFFICIENT BALANCE",
            color = Color.Red,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add cash to your wallet to enter the arena.",
            color = GamingTextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddCash,
            colors = ButtonDefaults.buttonColors(containerColor = GamingGoldAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("ADD CASH NOW", fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onReturn,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, GamingBorderSlate),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("RETURN TO LOBBY", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveGamePlayView(
    multiplier: Double,
    stake: Double,
    isCrashed: Boolean,
    isSecured: Boolean,
    onCashOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stats header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = GamingDeepSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = GamingGoldAccent, modifier = Modifier.size(14.dp))
                    Text("Stake Value: ₹10.00", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                color = GamingDeepSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = GamingBrightGreen, modifier = Modifier.size(14.dp))
                    Text(
                        text = String.format("Winnings Pot: ₹%.2f", multiplier * stake),
                        color = GamingBrightGreen,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Multiplier Rocket Graph Canvas Board representation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(GamingDeepSurface, shape = RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, GamingBorderSlate), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Live Curve Background Drawing
            Canvas(modifier = Modifier.matchParentSize()) {
                val gridBrush = Brush.linearGradient(colors = listOf(GamingBorderSlate, Color.Transparent))
                // Draw grid guides
                for (i in 1..4) {
                    val y = size.height * (i / 5f)
                    val x = size.width * (i / 5f)
                    drawLine(color = GamingBorderSlate.copy(alpha = 0.3f), start = Offset(0f, y), end = Offset(size.width, y))
                    drawLine(color = GamingBorderSlate.copy(alpha = 0.3f), start = Offset(x, 0f), end = Offset(x, size.height))
                }

                // Draw curve path
                val curvePath = Path().apply {
                    moveTo(30f, size.height - 30f)
                    // Quadratic curve relative to multiplier value
                    val progressRatio = minOf((multiplier - 1.0) / 5.0, 1.0).toFloat()
                    val targetX = 30f + (size.width - 90f) * progressRatio
                    val targetY = (size.height - 30f) - (size.height - 90f) * progressRatio * progressRatio
                    
                    quadraticTo(
                        size.width * 0.4f, size.height - 30f,
                        targetX, targetY
                    )
                }
                drawPath(
                    path = curvePath,
                    brush = Brush.linearGradient(colors = listOf(GamingVibrantIndigo, GamingNeonCyan)),
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = String.format("%.2fx", multiplier),
                    color = when {
                        isCrashed -> Color.Red
                        isSecured -> GamingBrightGreen
                        else -> GamingNeonCyan
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 52.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = when {
                        isCrashed -> "CRASHED!"
                        isSecured -> "MULTIPLIER SECURED!"
                        else -> "ELEVATING ROCKET GRAPH..."
                    },
                    color = if (isCrashed) Color.Red else if (isSecured) GamingBrightGreen else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Action Trigger Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCashOut,
                enabled = !isCrashed && !isSecured,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamingGoldAccent,
                    disabledContainerColor = GamingBorderSlate,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("cashout_button")
            ) {
                Text(
                    text = if (isSecured) "MULTIPLIER CASHED OUT" else "CASH OUT SECURELY",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            Text(
                "Risk warning: Multiplier crash occurs at random thresholds. Cash out quickly to lock profits.",
                color = GamingTextMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun GameFinishedView(
    opponentName: String,
    multiplier: Double,
    payout: Double,
    stake: Double,
    status: String,
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = if (status == "SUCCESS") Icons.Rounded.Celebration else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (status == "SUCCESS") GamingGoldAccent else Color.Red,
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = if (status == "SUCCESS") "CONGRATULATIONS!" else "CATASTROPHIC CRASH!",
                color = if (status == "SUCCESS") GamingGoldAccent else Color.Red,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = if (status == "SUCCESS") "You beat $opponentName and locked winnings!" else "You failed to cashout before impact.",
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // Stats card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GamingDeepSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GamingBorderSlate)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Initial Ticket Stake", color = GamingTextMuted)
                    Text(String.format("₹%.2f", stake), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Secured Multiplier", color = GamingTextMuted)
                    Text(
                        String.format("%.2fx", if (status == "SUCCESS") multiplier else 0.0),
                        color = if (status == "SUCCESS") GamingNeonCyan else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Divider(color = GamingBorderSlate)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Net Winnings", color = GamingTextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (status == "SUCCESS") String.format("+₹%.2f", payout - stake) else String.format("-₹%.2f", stake),
                        color = if (status == "SUCCESS") GamingBrightGreen else Color.Red,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = GamingNeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("play_again_button")
            ) {
                Text("BATTLE NEXT MATCH", fontWeight = FontWeight.Black)
            }

            OutlinedButton(
                onClick = onNavigateHome,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, GamingBorderSlate),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("RETURN TO LOBBY", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun ResultCard(
    title: String,
    amount: Double,
    onClaim: () -> Unit,
    onPlayAgain: () -> Unit
) {
    Surface(color = GamingDeepSurface, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, GamingBorderSlate)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (amount > 0) GamingBrightGreen else Color.Red)
            if (amount > 0) {
                Text("+₹${String.format("%.0f", amount)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = GamingGoldAccent, fontFamily = FontFamily.Monospace)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPlayAgain, modifier = Modifier.weight(1f)) { Text("PLAY AGAIN") }
                Button(onClick = onClaim, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(GamingNeonCyan)) {
                    Text("CLAIM & EXIT", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

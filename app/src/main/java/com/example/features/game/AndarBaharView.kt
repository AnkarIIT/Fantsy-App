package com.example.features.game

import androidx.compose.foundation.BorderStroke
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

private val andarRanks = listOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
private val andarSuits = listOf("♠","♥","♦","♣")

private data class AndarBaharRound(
    val joker: String,
    val andarCards: List<String>,
    val baharCards: List<String>,
    val winningSide: String
)

private fun dealAndarBahar(): AndarBaharRound {
    val deck = andarSuits.flatMap { s -> andarRanks.map { r -> "$r$s" } }.shuffled()
    val joker = deck[0]
    val andar = mutableListOf<String>()
    val bahar = mutableListOf<String>()
    var turnAndar = true
    for (i in 1 until deck.size) {
        val card = deck[i]
        if (turnAndar) andar.add(card) else bahar.add(card)
        if (card.dropLast(1) == joker.dropLast(1)) {
            return AndarBaharRound(joker, andar, bahar, if (turnAndar) "ANDAR" else "BAHAR")
        }
        turnAndar = !turnAndar
    }
    return AndarBaharRound(joker, andar, bahar, "ANDAR")
}

private fun isRedCard(card: String): Boolean = card.last() == '♥' || card.last() == '♦'

private fun cardFaceColor(card: String): Color = if (isRedCard(card)) Color(0xFFE53935) else Color(0xFF1E293B)

@Composable
private fun MiniCard(card: String, modifier: Modifier = Modifier, faceUp: Boolean = true) {
    Surface(
        color = if (faceUp) Color(0xFFF8FAFC) else Color(0xFF2A2F45),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, GamingBorderSlate),
        modifier = modifier.size(width = 42.dp, height = 56.dp)
    ) {
        if (faceUp) {
            Box(modifier = Modifier.padding(4.dp)) {
                Column {
                    Text(card.dropLast(1), color = cardFaceColor(card), fontSize = 12.sp, fontWeight = FontWeight.Black, lineHeight = 12.sp)
                    Text(card.last().toString(), color = cardFaceColor(card), fontSize = 10.sp, lineHeight = 10.sp)
                }
                Text(card.last().toString(), color = cardFaceColor(card), fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

@Composable
private fun SideFan(label: String, cards: List<String>, accent: Color, alignEnd: Boolean) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(color = accent.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
            Text(
                "$label  •  ${cards.size}",
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
            cards.takeLast(7).reversed().forEach { card ->
                MiniCard(card)
            }
        }
    }
}

@Composable
fun AndarBaharView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var betOn by remember { mutableStateOf<String?>(null) }
    var round by remember { mutableStateOf<AndarBaharRound?>(null) }
    var dealing by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var stakeDeducted by rememberSaveable { mutableStateOf(feeAlreadyPaid) }
    var stakePending by rememberSaveable { mutableStateOf(false) }
    var entryError by remember { mutableStateOf<String?>(null) }
    val stake = 25.0
    val scope = rememberCoroutineScope()

    fun requireStake(action: () -> Unit) {
        if (stakeDeducted) {
            action()
            return
        }
        if (stakePending) return
        stakePending = true
        scope.launch {
            val result = viewModel.deductStake(stake, "Entry: Andar Bahar Elite")
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

    fun placeBet(side: String) {
        requireStake {
            betOn = side
            dealing = true
            triggerVibration()
            scope.launch {
                delay(700)
                val dealt = dealAndarBahar()
                round = dealt
                dealing = false
                won = dealt.winningSide == side
                val winnings = if (won) stake * 2.0 else 0.0
                if (won) viewModel.awardWinnings(winnings) { }
                viewModel.recordGamePlayed("Andar Bahar Elite", won, winnings)
            }
        }
    }

    // Let the dealt cards sink in, then reveal the result overlay
    LaunchedEffect(round, dealing) {
        if (round != null && !dealing) {
            showResult = false
            delay(1600)
            showResult = true
            triggerVibration()
        }
    }

    GameShell(
        title = "Andar Bahar Elite",
        subtitle = "Bet on which side receives the joker card",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                entryError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                when {
                    round == null && !dealing -> {
                        Text(
                            "A joker card is drawn, then cards are dealt alternately to ANDAR and BAHAR. The side receiving a card matching the joker's rank wins. Pays 1:1.",
                            color = GamingTextMuted,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                    dealing -> {
                        Text(
                            "DEALING CARDS...",
                            color = GamingNeonCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                round?.let { r ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("JOKER CARD", color = GamingTextMuted, fontSize = 11.sp)
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GamingBorderSlate)
                        ) {
                            Column(
                                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(r.joker.dropLast(1), color = cardFaceColor(r.joker), fontSize = 24.sp, fontWeight = FontWeight.Black, lineHeight = 24.sp)
                                Text(r.joker.last().toString(), color = cardFaceColor(r.joker), fontSize = 20.sp)
                            }
                        }
                        Surface(
                            color = if (r.winningSide == "ANDAR") GamingNeonCyan.copy(alpha = 0.18f) else GamingGoldAccent.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "WINNER: ${r.winningSide}",
                                color = if (r.winningSide == "ANDAR") GamingNeonCyan else GamingGoldAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (round != null && !dealing) {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SideFan("ANDAR", round!!.andarCards, GamingNeonCyan, alignEnd = false)
                        SideFan("BAHAR", round!!.baharCards, GamingGoldAccent, alignEnd = true)
                    }
                }

                if (betOn == null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { placeBet("ANDAR") },
                            enabled = !dealing,
                            modifier = Modifier.weight(1f).height(70.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GamingNeonCyan)
                        ) { Text("BET ANDAR", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 13.sp) }

                        Button(
                            onClick = { placeBet("BAHAR") },
                            enabled = !dealing,
                            modifier = Modifier.weight(1f).height(70.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GamingGoldAccent)
                        ) { Text("BET BAHAR", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 13.sp) }
                    }
                } else if (dealing) {
                    Text("Stake ₹$stake", color = GamingTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showResult) {
                GameResultOverlay(
                    won = won,
                    title = if (won) "You Win the Pot!" else "House Wins",
                    subtitle = if (won) "Your bet on ${betOn ?: "your side"} paid off" else "The ${round?.winningSide ?: "opposing"} side received the joker card",
                    amount = if (won) stake * 2.0 else 0.0,
                    onPlayAgain = {
                        betOn = null
                        round = null
                        won = false
                        showResult = false
                    },
                    onExit = onExit
                )
            }
        }
    }
}

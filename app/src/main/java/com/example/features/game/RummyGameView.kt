package com.example.features.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val rummyRanks = listOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
private val rummySuits = listOf('♠','♥','♦','♣')

private data class RummyCard(val rank: String, val suit: Char) {
    override fun toString(): String = "$rank$suit"
}

private fun rummyDeck(): List<RummyCard> =
    rummySuits.flatMap { s -> rummyRanks.map { r -> RummyCard(r, s) } }

private fun cardKey(c: RummyCard): Int = rummyRanks.indexOf(c.rank) * 4 + rummySuits.indexOf(c.suit)

private fun isSequence(g: List<RummyCard>): Boolean =
    g.map { rummyRanks.indexOf(it.rank) }.sorted().let { r ->
        r.size >= 3 && r.last() - r.first() == r.size - 1 && g.map { it.suit }.distinct().size == 1
    }

private fun findGroups(hand: List<RummyCard>): List<List<RummyCard>>? {
    if (hand.isEmpty()) return emptyList()
    val first = hand.minBy { cardKey(it) }
    val sameRank = hand.filter { it.rank == first.rank }
    if (sameRank.size in 3..4) {
        val rest = findGroups(hand - sameRank.toSet())
        if (rest != null) return rest + listOf(sameRank)
    }
    val base = rummyRanks.indexOf(first.rank)
    for (len in listOf(4, 3)) {
        if (base + len - 1 < rummyRanks.size) {
            val seq = (0 until len).map { RummyCard(rummyRanks[base + it], first.suit) }
            if (hand.toSet().containsAll(seq)) {
                val rest = findGroups(hand - seq.toSet())
                if (rest != null) return rest + listOf(seq)
            }
        }
    }
    return null
}

private fun validDeclaration(hand: List<RummyCard>): Boolean {
    val groups = findGroups(hand) ?: return false
    return groups.count { isSequence(it) } >= 2
}

@Composable
fun RummyGameView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var hand by remember { mutableStateOf<List<RummyCard>>(emptyList()) }
    var stock by remember { mutableStateOf<List<RummyCard>>(emptyList()) }
    var opponentCount by remember { mutableStateOf(13) }
    var selected by remember { mutableStateOf<RummyCard?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var dealt by remember { mutableStateOf(false) }
    val stake = 15.0
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
            val result = viewModel.deductStake(stake, "Entry: Rummy Royale 13")
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

    fun deal() {
        val deck = rummyDeck().shuffled()
        hand = deck.take(13).sortedBy { cardKey(it) }
        stock = deck.drop(26) // 13 discarded to a placeholder opponent table
        opponentCount = 13
        selected = null
        message = null
        dealt = true
    }

    fun swapCard(card: RummyCard) {
        if (gameOver) return
        if (stock.isEmpty()) return
        val replacement = stock.first()
        stock = stock.drop(1)
        hand = (hand - card + replacement).sortedBy { cardKey(it) }
        selected = null
        triggerVibration()
        opponentCount = if (opponentCount <= 7) 7 else opponentCount - 1
        message = "Discarded ${card}. Drew $replacement. Form 2 sequences + sets."
    }

    fun declare() {
        if (gameOver || !dealt) return
        triggerVibration()
        val ok = validDeclaration(hand)
        won = ok
        gameOver = true
        val winnings = if (ok) stake * 2.2 else 0.0
        if (ok) viewModel.awardWinnings(winnings) { }
        viewModel.recordGamePlayed("Rummy Royale 13", ok, winnings)
    }

    GameShell(
        title = "Rummy Royale 13",
        subtitle = "Form 2 sequences and valid sets to declare",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Surface(color = GamingDeepSurface, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, GamingBorderSlate), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Opponent: $opponentCount cards", color = GamingTextMuted, fontSize = 13.sp)
                Text("Stock: ${stock.size}", color = GamingNeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (!dealt && !gameOver) {
            Text("Tap DECALRE below once your hand forms 2 sequences (3+ consecutive, same suit) and valid sets.", color = GamingTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
        } else if (!gameOver) {
            Text("Your 13 cards — tap a card to discard & draw. At least 2 sequences needed to declare.", color = GamingTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            message?.let { Text(it, color = GamingGoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(hand) { card ->
                    Surface(
                        onClick = { selected = if (selected == card) null else card },
                        color = if (selected == card) GamingNeonCyan else GamingDeepSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (selected == card) GamingNeonCyan else GamingBorderSlate)
                    ) {
                        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(card.rank, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(card.suit.toString(), color = if (card.suit == '♥' || card.suit == '♦') Color(0xFFFF5252) else Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (!dealt && !gameOver) {
            Button(onClick = { requireStake { deal() } }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(GamingGoldAccent)) {
                Text("DEAL HAND (₹$stake)", fontWeight = FontWeight.Black, color = Color.Black)
            }
        } else if (!gameOver) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { selected?.let { swapCard(it) } },
                    enabled = selected != null && stock.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(GamingNeonCyan)
                ) { Text("SWAP SELECTED", fontWeight = FontWeight.Black, color = Color.Black) }
                Button(
                    onClick = { declare() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(GamingBrightGreen)
                ) { Text("DECLARE", fontWeight = FontWeight.Black, color = Color.Black) }
            }
        }

        if (gameOver) {
            GameResultOverlay(
                won = won,
                title = if (won) "Valid Declare!" else "Invalid Declaration",
                subtitle = if (won) "Your hand formed valid sequences and sets" else "Your hand does not meet the declaration rules",
                amount = if (won) stake * 2.2 else 0.0,
                onPlayAgain = { gameOver = false; won = false; dealt = false; deal() },
                onExit = onExit
            )
        }
    }
    }
}
}

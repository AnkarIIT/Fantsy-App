package com.example.features.game

import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch

private val teenPattiRanks = listOf("2","3","4","5","6","7","8","9","10","J","Q","K","A")
private val teenPattiSuits = listOf("♠","♥","♦","♣")

private fun teenPattiDeck(): List<String> {
    val deck = mutableListOf<String>()
    teenPattiSuits.forEach { suit -> teenPattiRanks.forEach { rank -> deck.add("$rank$suit") } }
    return deck
}

private fun randomPattiHand(): List<String> = teenPattiDeck().shuffled().take(3)

private fun teenPattiHandScore(cards: List<String>): Int {
    val sorted = cards.map { teenPattiRanks.indexOf(it.dropLast(1)) }.sorted()
    val counts = sorted.groupingBy { it }.eachCount()
    val isTrail = counts.size == 1
    val isSequence = sorted[2] - sorted[1] == 1 && sorted[1] - sorted[0] == 1
    val sameSuit = cards.map { it.last() }.distinct().size == 1
    val isPair = counts.size == 2
    val pairRank = counts.entries.firstOrNull { it.value == 2 }?.key ?: -1
    return when {
        isTrail -> 100000 + sorted[0] * 100
        isSequence && sameSuit -> 90000 + sorted[2] * 100
        isSequence -> 80000 + sorted[2] * 100
        sameSuit -> 70000 + sorted.sum()
        isPair -> 60000 + pairRank * 100 + sorted.filter { it != pairRank }.sum()
        else -> sorted.sum()
    }
}

@Composable
fun TeenPattiGameView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var playerCards by remember { mutableStateOf(randomPattiHand()) }
    var opponentCards by remember { mutableStateOf(randomPattiHand()) }
    var revealed by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    val stake = 25.0
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
            val result = viewModel.deductStake(stake, "Entry: Teen Patti Clash")
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

    fun playHand() {
        if (revealed) return
        triggerVibration()
        requireStake {
            revealed = true
            playerCards = randomPattiHand()
            opponentCards = randomPattiHand()
            won = teenPattiHandScore(playerCards) > teenPattiHandScore(opponentCards)
            val winnings = if (won) stake * 2.0 else 0.0
            if (won) viewModel.awardWinnings(winnings) { }
            viewModel.recordGamePlayed("Teen Patti Clash", won, winnings)
        }
    }

    GameShell(
        title = "Teen Patti Clash",
        subtitle = "Trails and high sequences win the pot",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text("Compare hands. Trails and high sequences win.", color = GamingTextMuted)

        Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("YOU", color = GamingNeonCyan)
                Surface(color = GamingDeepSurface, shape = RoundedCornerShape(8.dp)) {
                    Text(playerCards.joinToString("\n"), color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(12.dp), fontFamily = FontFamily.Monospace)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("OPPONENT", color = Color.Red)
                Surface(color = GamingDeepSurface, shape = RoundedCornerShape(8.dp)) {
                    Text(if (revealed) opponentCards.joinToString("\n") else "● ● ●", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(12.dp), fontFamily = FontFamily.Monospace)
                }
            }
        }

        if (!revealed) {
            Button(onClick = { playHand() }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(GamingGoldAccent)) {
                Text("SHOW / PLAY HAND", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        if (revealed) {
            GameResultOverlay(
                won = won,
                title = if (won) "You Win The Pot!" else "Better Luck Next Round",
                subtitle = if (won) "Your hand beat the opponent's" else "The opponent's hand was stronger",
                amount = if (won) stake * 2.0 else 0.0,
                onPlayAgain = { revealed = false; won = false; playerCards = randomPattiHand(); opponentCards = randomPattiHand() },
                onExit = onExit
            )
        }
    }
    }
}
}

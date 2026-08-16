package com.example.features.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.lobby.LobbyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private val bubbleColors = listOf(
    Color(0xFF26C6DA), GamingGoldAccent, GamingBrightGreen, Color(0xFF7C4DFF), Color(0xFFFF5252)
)

@Composable
fun BubbleShooterView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    val rows = 10
    val cols = 8
    var grid by remember { mutableStateOf(Array(rows) { arrayOfNulls<Int>(cols) }) }
    var angle by remember { mutableStateOf(0) } // -60..60 degrees
    var currentColor by remember { mutableStateOf(Random.nextInt(bubbleColors.size)) }
    var nextColor by remember { mutableStateOf(Random.nextInt(bubbleColors.size)) }
    var score by remember { mutableStateOf(0) }
    var firing by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var win by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
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
            val result = viewModel.deductStake(stake, "Entry: Bubble Blast Royale")
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

    fun neighborCells(r: Int, c: Int): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        val above = r - 1; val below = r + 1
        if (c - 1 >= 0) list.add(r to c - 1)
        if (c + 1 < cols) list.add(r to c + 1)
        if (r % 2 == 0) { // even row: children align left
            if (above >= 0) { if (c - 1 >= 0) list.add(above to c - 1); list.add(above to c) }
            if (below < rows) { if (c - 1 >= 0) list.add(below to c - 1); list.add(below to c) }
        } else { // odd row: children align right
            if (above >= 0) { list.add(above to c); if (c + 1 < cols) list.add(above to c + 1) }
            if (below < rows) { list.add(below to c); if (c + 1 < cols) list.add(below to c + 1) }
        }
        return list.filter { it.first in 0 until rows && it.second in 0 until cols }
    }

    fun popAndDrop() {
        var changed = true
        while (changed) {
            changed = false
            // find groups of >=3 same color and remove the one containing placed bubble first,
            // then keep clearing any remaining groups.
            for (r in 0 until rows) for (c in 0 until cols) {
                val color = grid[r][c] ?: continue
                val group = mutableListOf<Pair<Int, Int>>()
                val queue = ArrayDeque<Pair<Int, Int>>()
                queue.add(r to c)
                group.add(r to c)
                while (queue.isNotEmpty()) {
                    val (cr, cc) = queue.removeFirst()
                    for (n in neighborCells(cr, cc)) {
                        if (grid[n.first][n.second] == color && !group.contains(n)) {
                            group.add(n); queue.add(n)
                        }
                    }
                }
                if (group.size >= 3) {
                    group.forEach { grid[it.first][it.second] = null }
                    score += group.size * 10
                    changed = true
                }
            }
            // drop floating bubbles (disconnected from row 0)
            val connected = Array(rows) { BooleanArray(cols) }
            val q = ArrayDeque<Pair<Int, Int>>()
            for (c in 0 until cols) if (grid[0][c] != null) { connected[0][c] = true; q.add(0 to c) }
            while (q.isNotEmpty()) {
                val (cr, cc) = q.removeFirst()
                for (n in neighborCells(cr, cc)) {
                    if (grid[n.first][n.second] != null && !connected[n.first][n.second]) {
                        connected[n.first][n.second] = true; q.add(n)
                    }
                }
            }
            for (r in 0 until rows) for (c in 0 until cols) {
                if (grid[r][c] != null && !connected[r][c]) { grid[r][c] = null; changed = true }
            }
        }
    }

    fun fire() {
        if (firing || gameOver) return
        requireStake {
            firing = true
            triggerVibration()
            scope.launch {
                val lane = ((angle + 60.0) / 120.0 * (cols - 1)).roundToInt().coerceIn(0, cols - 1)
                var landed = false
                for (r in 0 until rows) {
                    if (grid[r][lane] == null) {
                        grid = grid.mapIndexed { rr, row -> if (rr == r) row.copyOf().also { it[lane] = currentColor } else row.copyOf() }.toTypedArray()
                        landed = true
                        break
                    }
                }
                delay(500)
                popAndDrop()
                currentColor = nextColor
                nextColor = Random.nextInt(bubbleColors.size)
                firing = false

                val anyInBottom = grid[rows - 1].any { it != null }
                val anyLeft = grid.any { row -> row.any { it != null } }
                win = !anyLeft
                if (win || anyInBottom || !landed) {
                    gameOver = true
                    val winnings = if (win) stake * 2 + score / 12 else 0.0
                    if (win) viewModel.awardWinnings(winnings) { }
                    viewModel.recordGamePlayed("Bubble Blast Royale", win, winnings)
                    message = if (win) "Board cleared!" else if (anyInBottom) "Bubbles reached the bottom!" else "Column full — miss!"
                }
            }
        }
    }

    GameShell(
        title = "Bubble Blast Royale",
        subtitle = "Aim and pop groups of 3+ bubbles",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text("Score: $score   Aim the angle and pop groups of 3+ of the same color.", color = GamingTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)

        Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp)) {
            Canvas(Modifier.fillMaxSize().background(GamingDeepSurface, RoundedCornerShape(12.dp))) {
                val width = size.width / cols
                val height = size.height / rows
                for (r in 0 until rows) for (c in 0 until cols) {
                    val color = grid[r][c]
                    if (color != null) {
                        val x = c * width + (if (r % 2 == 1) width / 2 else 0f) + width / 2
                        val y = r * height + height / 2
                        drawCircle(bubbleColors[color], radius = width / 2 - 2f, center = Offset(x, y))
                    }
                }
                // shooter
                val cx = size.width / 2
                val cy = size.height - 28f
                drawCircle(bubbleColors[currentColor], radius = 14f, center = Offset(cx, cy))
                // aim line
                val rad = Math.toRadians(angle.toDouble())
                val endX = cx + Math.sin(rad) * 140
                val endY = cy - Math.cos(rad) * 140
                drawLine(bubbleColors[currentColor], Offset(cx, cy), Offset(endX.toFloat(), endY.toFloat()), strokeWidth = 3f)
                drawCircle(bubbleColors[currentColor], radius = 6f, center = Offset(endX.toFloat(), endY.toFloat()), style = Stroke(width = 2f))
            }
        }

        if (!gameOver) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { angle = (angle - 10).coerceAtLeast(-60) }, colors = ButtonDefaults.buttonColors(GamingNeonCyan)) {
                    Text("◀", fontSize = 20.sp, color = Color.Black)
                }
                Text("$angle°", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
                Button(onClick = { angle = (angle + 10).coerceAtMost(60) }, colors = ButtonDefaults.buttonColors(GamingNeonCyan)) {
                    Text("▶", fontSize = 20.sp, color = Color.Black)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { fire() }, modifier = Modifier.weight(1f).height(54.dp), enabled = !firing, colors = ButtonDefaults.buttonColors(GamingGoldAccent)) {
                    Text(if (firing) "FIRING..." else "FIRE!", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NEXT", color = GamingTextMuted, fontSize = 9.sp)
                    Canvas(Modifier.size(20.dp)) { drawCircle(bubbleColors[nextColor], radius = 9f) }
                }
            }
        }

        if (gameOver) {
            GameResultOverlay(
                won = win,
                title = if (win) "Board Cleared!" else (message ?: "Game Over"),
                subtitle = if (win) "You popped every bubble on the board" else (message ?: "Out of luck this round"),
                amount = if (win) stake * 2 + score / 12 else 0.0,
                onPlayAgain = {
                    grid = Array(rows) { arrayOfNulls<Int>(cols) }
                    score = 0; gameOver = false; win = false; angle = 0
                    currentColor = Random.nextInt(bubbleColors.size)
                    nextColor = Random.nextInt(bubbleColors.size)
                    message = null
                },
                onExit = onExit
            )
        }
    }
    }
}
}

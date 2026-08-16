package com.example.features.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

private data class Move(val fr: Int, val fc: Int, val tr: Int, val tc: Int)
private const val WP = 'P'; const val WN = 'N'; const val WB = 'B'; const val WR = 'R'; const val WQ = 'Q'; const val WK = 'K'
private const val BP = 'p'; const val BN = 'n'; const val BB = 'b'; const val BR = 'r'; const val BQ = 'q'; const val BK = 'k'

private typealias Board = Array<Array<Char?>>

private fun initialBoard(): Board = arrayOf(
    arrayOf(BR, BN, BB, BQ, BK, BB, BN, BR),
    arrayOf(BP, BP, BP, BP, BP, BP, BP, BP),
    arrayOf(null, null, null, null, null, null, null, null),
    arrayOf(null, null, null, null, null, null, null, null),
    arrayOf(null, null, null, null, null, null, null, null),
    arrayOf(null, null, null, null, null, null, null, null),
    arrayOf(WP, WP, WP, WP, WP, WP, WP, WP),
    arrayOf(WR, WN, WB, WQ, WK, WB, WN, WR)
)

private fun copyBoard(b: Board): Board = Array(8) { r -> b[r].copyOf() }

private fun isWhitePiece(p: Char?) = p != null && p.isUpperCase()
private fun isBlackPiece(p: Char?) = p != null && p.isLowerCase()

private fun inside(r: Int, c: Int) = r in 0..7 && c in 0..7

private fun pseudoMoves(board: Board, r: Int, c: Int, withCastling: Boolean): List<Move> {
    val p = board[r][c] ?: return emptyList()
    val moves = mutableListOf<Move>()
    val white = isWhitePiece(p)

    fun addIfValid(nr: Int, nc: Int) {
        if (!inside(nr, nc)) return
        val t = board[nr][nc]
        if (t == null || (white && isBlackPiece(t)) || (!white && isWhitePiece(t))) moves.add(Move(r, c, nr, nc))
    }

    fun addSlide(dr: Int, dc: Int) {
        var nr = r + dr; var nc = c + dc
        while (inside(nr, nc)) {
            val t = board[nr][nc]
            if (t == null) { moves.add(Move(r, c, nr, nc)) }
            else { addIfValid(nr, nc); break }
            nr += dr; nc += dc
        }
    }

    when (p.lowercaseChar()) {
        'p' -> {
            val dir = if (white) -1 else 1
            val startRow = if (white) 6 else 1
            val fwd = r + dir
            if (inside(fwd, c) && board[fwd][c] == null) {
                moves.add(Move(r, c, fwd, c))
                if (r == startRow && inside(r + 2 * dir, c) && board[r + 2 * dir][c] == null) {
                    moves.add(Move(r, c, r + 2 * dir, c))
                }
            }
            addIfValid(fwd, c - 1)
            addIfValid(fwd, c + 1)
        }
        'n' -> for ((dr, dc) in listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)) addIfValid(r + dr, c + dc)
        'b' -> for ((dr, dc) in listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)) addSlide(dr, dc)
        'r' -> for ((dr, dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) addSlide(dr, dc)
        'q' -> for ((dr, dc) in listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)) addSlide(dr, dc)
        'k' -> for (dr in -1..1) for (dc in -1..1) if (!(dr == 0 && dc == 0)) addIfValid(r + dr, c + dc)
    }

    // Castling
    if (withCastling && p == WK && r == 7 && c == 4) {
        if (board[7][7] == WR && board[7][6] == null && board[7][5] == null) {
            if (!isAttacked(board, 7, 4, false) && !isAttacked(board, 7, 5, false) && !isAttacked(board, 7, 6, false)) {
                moves.add(Move(7, 4, 7, 6))
            }
        }
        if (board[7][0] == WR && board[7][1] == null && board[7][2] == null && board[7][3] == null) {
            if (!isAttacked(board, 7, 4, false) && !isAttacked(board, 7, 3, false) && !isAttacked(board, 7, 2, false)) {
                moves.add(Move(7, 4, 7, 2))
            }
        }
    }
    if (withCastling && p == BK && r == 0 && c == 4) {
        if (board[0][7] == BR && board[0][6] == null && board[0][5] == null) {
            if (!isAttacked(board, 0, 4, true) && !isAttacked(board, 0, 5, true) && !isAttacked(board, 0, 6, true)) {
                moves.add(Move(0, 4, 0, 6))
            }
        }
        if (board[0][0] == BR && board[0][1] == null && board[0][2] == null && board[0][3] == null) {
            if (!isAttacked(board, 0, 4, true) && !isAttacked(board, 0, 3, true) && !isAttacked(board, 0, 2, true)) {
                moves.add(Move(0, 4, 0, 2))
            }
        }
    }
    return moves
}

private fun findKing(board: Board, white: Boolean): Pair<Int, Int>? {
    for (r in 0..7) for (c in 0..7) {
        val p = board[r][c]
        if (p == (if (white) WK else BK)) return r to c
    }
    return null
}

private fun isAttacked(board: Board, r: Int, c: Int, byWhite: Boolean): Boolean {
    for (ar in 0..7) for (ac in 0..7) {
        val p = board[ar][ac] ?: continue
        if ((byWhite && isWhitePiece(p)) || (!byWhite && isBlackPiece(p))) {
            if (pseudoMoves(board, ar, ac, withCastling = false).any { it.tr == r && it.tc == c }) return true
        }
    }
    return false
}

private fun kingInCheck(board: Board, white: Boolean): Boolean {
    val k = findKing(board, white) ?: return false
    return isAttacked(board, k.first, k.second, !white)
}

private fun applyMove(board: Board, m: Move, captureEnPassantAllowed: Boolean = false): Board {
    val b = copyBoard(board)
    val piece = b[m.fr][m.fc]!!
    b[m.tr][m.tc] = piece
    b[m.fr][m.fc] = null
    // Castling rook move
    if (piece == WK && m.fr == 7 && m.fc == 4) {
        if (m.tc == 6) { b[7][5] = WR; b[7][7] = null }
        if (m.tc == 2) { b[7][3] = WR; b[7][0] = null }
    }
    if (piece == BK && m.fr == 0 && m.fc == 4) {
        if (m.tc == 6) { b[0][5] = BR; b[0][7] = null }
        if (m.tc == 2) { b[0][3] = BR; b[0][0] = null }
    }
    // Promotion (auto-queen)
    val promoRow = if (isWhitePiece(piece)) 0 else 7
    if (piece.lowercaseChar() == 'p' && m.tr == promoRow) {
        b[m.tr][m.tc] = if (isWhitePiece(piece)) WQ else BQ
    }
    return b
}

private fun legalMoves(board: Board, white: Boolean): List<Move> {
    val result = mutableListOf<Move>()
    for (r in 0..7) for (c in 0..7) {
        val p = board[r][c] ?: continue
        if (isWhitePiece(p) == white) {
            for (m in pseudoMoves(board, r, c, withCastling = true)) {
                val next = applyMove(board, m)
                if (!kingInCheck(next, white)) result.add(m)
            }
        }
    }
    return result
}

@Composable
fun ChessGameView(
    viewModel: LobbyViewModel,
    onExit: () -> Unit,
    feeAlreadyPaid: Boolean = false,
    triggerVibration: () -> Unit
) {
    var board by remember { mutableStateOf(initialBoard()) }
    var whiteTurn by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var aiThinking by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("White to move") }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
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
            val result = viewModel.deductStake(stake, "Entry: Bullet Chess League")
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

    fun finish(playerWon: Boolean) {
        gameOver = true
        won = playerWon
        val winnings = if (playerWon) stake * 1.5 else 0.0
        if (playerWon) viewModel.awardWinnings(winnings) { }
        viewModel.recordGamePlayed("Bullet Chess League", playerWon, winnings)
    }

    fun checkEnd(): Boolean {
        val moves = legalMoves(board, whiteTurn)
        val inCheck = kingInCheck(board, whiteTurn)
        if (moves.isEmpty()) {
            if (inCheck) finish(!whiteTurn)
            else { gameOver = true; won = false; viewModel.recordGamePlayed("Bullet Chess League", false, 0.0); status = "Stalemate — Draw" }
            return true
        }
        status = if (inCheck) "${if (whiteTurn) "White" else "Black"} to move — CHECK!" else "${if (whiteTurn) "White" else "Black"} to move"
        return false
    }

    fun runAi() {
        if (gameOver || whiteTurn) return
        aiThinking = true
        scope.launch {
            delay(500)
            val moves = legalMoves(board, white = false)
            if (moves.isNotEmpty()) {
                val kingPos = findKing(board, true)
                val checks = if (kingPos != null)
                    moves.filter { isAttacked(applyMove(board, it), kingPos.first, kingPos.second, byWhite = false) }
                else emptyList()
                val captures = moves.filter { board[it.tr][it.tc] != null }
                val pool = when {
                    checks.isNotEmpty() -> checks
                    captures.isNotEmpty() -> captures
                    else -> moves
                }
                board = applyMove(board, pool.random())
                whiteTurn = true
            }
            aiThinking = false
            if (!checkEnd() && !gameOver && whiteTurn) status = "White to move"
        }
    }

    fun onSquareTap(r: Int, c: Int) {
        if (gameOver || !whiteTurn || aiThinking) return
        requireStake {
            if (!started) { started = true; status = "White to move" }
            val piece = board[r][c]
            val sel = selected
            if (sel != null) {
                val moves = legalMoves(board, true).filter { it.fr == sel.first && it.fc == sel.second }
                if (moves.any { it.tr == r && it.tc == c }) {
                    board = applyMove(board, moves.first { it.tr == r && it.tc == c })
                    selected = null
                    whiteTurn = false
                    triggerVibration()
                    if (!checkEnd()) runAi()
                    return@requireStake
                }
            }
            selected = if (piece != null && isWhitePiece(piece)) (r to c) else null
        }
    }

    GameShell(
        title = "Bullet Chess League",
        subtitle = "You play White — checkmate to win",
        viewModel = viewModel,
        onExit = onExit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entryError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text(status, color = if (status.contains("CHECK")) Color(0xFFFF6B6B) else GamingBrightGreen, fontWeight = FontWeight.Bold)
        Text("You are WHITE. Tap a piece, then tap a highlighted square. Basic rules (no en passant).", color = GamingTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)

        val moves = selected?.let { legalMoves(board, true).filter { m -> m.fr == it.first && m.fc == it.second } } ?: emptyList()

        Surface(color = GamingDeepSurface, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, GamingBorderSlate)) {
            Column(Modifier.padding(6.dp)) {
                for (r in 0..7) {
                    Row {
                        for (c in 0..7) {
                            val dark = (r + c) % 2 == 0
                            val isSel = selected?.let { it.first == r && it.second == c } == true
                            val isTarget = moves.any { it.tr == r && it.tc == c }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (isSel) GamingNeonCyan.copy(alpha = 0.7f) else if (dark) Color(0xFF37474F) else Color(0xFFECEFF1))
                                    .clickable { onSquareTap(r, c) },
                                contentAlignment = Alignment.Center
                            ) {
                                val p = board[r][c]
                                if (p != null) {
                                    Text(pieceSymbol(p), fontSize = 24.sp, color = if (isWhitePiece(p)) Color.White else Color(0xFF212121))
                                } else if (isTarget) {
                                    Box(Modifier.size(10.dp).background(Color(0xFFFFC107), CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!gameOver) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { selected = null }, modifier = Modifier.weight(1f), enabled = selected != null) { Text("CLEAR") }
                Button(
                    onClick = {
                        won = false; gameOver = true
                        viewModel.recordGamePlayed("Bullet Chess League", false, 0.0)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(GamingGoldAccent)
                ) { Text("RESIGN", fontWeight = FontWeight.Black, color = Color.Black) }
            }
        }

        if (gameOver) {
            GameResultOverlay(
                won = won,
                title = when {
                    status.contains("Stalemate") -> "Draw"
                    won -> "Checkmate — You Win!"
                    else -> "Checkmate — AI Wins"
                },
                subtitle = if (won) "You trapped the black king" else if (status.contains("Stalemate")) "No legal moves remain" else "Your king was checkmated",
                amount = if (won) stake * 1.5 else 0.0,
                onPlayAgain = { board = initialBoard(); whiteTurn = true; selected = null; gameOver = false; won = false; started = false; status = "White to move" },
                onExit = onExit
            )
        }
    }
    }
}
}

private fun pieceSymbol(p: Char): String = when (p) {
    WP -> "♙"; WN -> "♘"; WB -> "♗"; WR -> "♖"; WQ -> "♕"; WK -> "♔"
    BP -> "♟"; BN -> "♞"; BB -> "♝"; BR -> "♜"; BQ -> "♛"; BK -> "♚"
    else -> ""
}

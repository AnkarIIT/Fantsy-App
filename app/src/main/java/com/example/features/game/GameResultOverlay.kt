package com.example.features.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val drift: Float,
    val swaySeed: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun GameResultOverlay(
    won: Boolean,
    title: String,
    amount: Double,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    playAgainLabel: String = "PLAY AGAIN",
    exitLabel: String = "RETURN TO LOBBY"
) {
    val progress = remember { Animatable(0f) }
    val countUp = remember { Animatable(0f) }
    var particles by remember { mutableStateOf(emptyList<ConfettiParticle>()) }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(450, easing = EaseOutBack))
        if (won) {
            countUp.snapTo(0f)
            countUp.animateTo(amount.toFloat(), tween(1100, easing = EaseOutCubic))
        }
        if (won) {
            val random = Random(System.currentTimeMillis())
            val colors = listOf(
                GamingNeonCyan, GamingGoldAccent, GamingBrightGreen,
                GamingVibrantIndigo, Color(0xFFFF5252), Color(0xFF38BDF8)
            )
            particles = List(52) {
                ConfettiParticle(
                    x = random.nextFloat(),
                    y = -0.1f - random.nextFloat() * 0.4f,
                    speed = 0.18f + random.nextFloat() * 0.3f,
                    drift = (random.nextFloat() - 0.5f) * 0.5f,
                    swaySeed = random.nextFloat() * 10f,
                    color = colors[random.nextInt(colors.size)],
                    width = 6f + random.nextFloat() * 7f,
                    height = 4f + random.nextFloat() * 6f,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 10f
                )
            }
            while (true) {
                withFrameNanos { }
                particles = particles.map { p ->
                    val sway = sin(p.y * 22f + p.swaySeed) * 0.012f
                    val nextY = p.y + p.speed / 70f
                    val nextX = p.x + p.drift / 70f + sway
                    if (nextY > 1.15f) {
                        p.copy(x = random.nextFloat(), y = -0.1f, drift = (random.nextFloat() - 0.5f) * 0.5f)
                    } else {
                        p.copy(x = nextX, y = nextY, rotation = p.rotation + p.rotationSpeed / 70f)
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        if (won) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    rotate(p.rotation) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(p.x * size.width, p.y * size.height),
                            size = Size(p.width * 2f, p.height * 2f)
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .graphicsLayer {
                    scaleX = progress.value
                    scaleY = progress.value
                    alpha = progress.value
                }
                .testTag("game_result_overlay"),
            shape = RoundedCornerShape(24.dp),
            color = GamingDeepSurface,
            border = BorderStroke(1.5.dp, if (won) GamingGoldAccent else Color(0xFFFF5252))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            if (won) GamingGoldAccent.copy(alpha = 0.12f) else Color(0xFFFF5252).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(48.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (won) Icons.Rounded.EmojiEvents else Icons.Rounded.Cancel,
                        contentDescription = if (won) "Victory" else "Loss",
                        tint = if (won) GamingGoldAccent else Color(0xFFFF5252),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Text(
                    text = title.uppercase(),
                    color = if (won) GamingGoldAccent else Color(0xFFFF5252),
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (amount > 0) {
                    Text(
                        text = String.format("+₹%.2f", countUp.value),
                        color = GamingBrightGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else if (!won) {
                    Text(
                        text = "Entry stake lost",
                        color = GamingTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = GamingGoldAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("result_play_again")
                ) {
                    Text(playAgainLabel, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onExit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, GamingBorderSlate),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("result_exit")
                ) {
                    Text(exitLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

package com.example.features.lobby

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    size: Int = 28
) {
    val logoColors = listOf(GamingNeonCyan, GamingGoldAccent)
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Logo mark - stylized "R" with crown
        Box(
            modifier = Modifier
                .size(size.dp)
                .drawBehind {
                    val centerX = size.dp.toPx() / 2
                    val centerY = size.dp.toPx() / 2
                    val radius = size.dp.toPx() * 0.45f
                    
                    // Outer glow ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GamingNeonCyan.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(centerX, centerY),
                            radius = radius * 1.3f
                        )
                    )
                    
                    // Main logo circle with gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GamingNeonCyan, GamingGoldAccent),
                            center = Offset(centerX, centerY),
                            radius = radius
                        )
                    )
                    
                    // Inner highlight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f),
                            radius = radius * 0.5f
                        )
                    )
                    
                    // "R" letter
                    // Draw a simple crown shape as logo mark
                    drawCrown(centerX, centerY, radius * 0.7f)
                }
        )
        
        if (showText) {
            Text(
                text = "ROYALE",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = "GRAND",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = GamingGoldAccent,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrown(cx: Float, cy: Float, radius: Float) {
    val path = androidx.compose.ui.graphics.Path().apply {
        val spikeWidth = radius / 2.5f
        val spikeHeight = radius * 0.6f
        val baseY = cy + radius * 0.2f
        
        // Crown base
        moveTo(cx - radius, baseY)
        lineTo(cx - radius, cy + radius * 0.5f)
        lineTo(cx + radius, cy + radius * 0.5f)
        lineTo(cx + radius, baseY)
        
        // 5 spikes
        repeat(5) { i ->
            val x = cx - radius + (i + 0.5f) * (radius * 2f / 5)
            val peakX = cx - radius + (i + 1f) * (radius * 2f / 5)
            val nextX = cx - radius + (i + 1.5f) * (radius * 2f / 5)
            
            lineTo(peakX, cy - spikeHeight)
            lineTo(nextX, baseY)
        }
        close()
    }
    
    drawPath(
        path,
        color = Color.White,
        style = Stroke(width = 1.5f)
    )
    
    // Fill
    val fillPath = androidx.compose.ui.graphics.Path().apply {
        val spikeWidth = radius / 2.5f
        val spikeHeight = radius * 0.6f
        val baseY = cy + radius * 0.2f
        
        moveTo(cx - radius, baseY)
        lineTo(cx - radius, cy + radius * 0.5f)
        lineTo(cx + radius, cy + radius * 0.5f)
        lineTo(cx + radius, baseY)
        
        repeat(5) { i ->
            val x = cx - radius + (i + 0.5f) * (radius * 2f / 5)
            val peakX = cx - radius + (i + 1f) * (radius * 2f / 5)
            val nextX = cx - radius + (i + 1.5f) * (radius * 2f / 5)
            
            lineTo(peakX, cy - spikeHeight)
            lineTo(nextX, baseY)
        }
        close()
    }
    
    drawPath(
        fillPath,
        brush = Brush.linearGradient(
            colors = listOf(GamingGoldAccent, Color(0xFFFFD700)),
            start = Offset(cx, cy - radius),
            end = Offset(cx, cy + radius)
        )
    )
}

@Composable
fun AppLogoMini(
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Box(
        modifier = modifier.size(size.dp)
            .drawBehind {
                val centerX = size.dp.toPx() / 2
                val centerY = size.dp.toPx() / 2
                val radius = size.dp.toPx() * 0.45f
                
                // Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GamingNeonCyan.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = radius * 1.4f
                    )
                )
                
                // Main circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GamingNeonCyan, GamingGoldAccent),
                        center = Offset(centerX, centerY),
                        radius = radius
                    )
                )
                
                // Crown icon
                drawCrown(centerX, centerY, radius * 0.7f)
            }
    )
}
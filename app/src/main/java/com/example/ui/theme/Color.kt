package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Frosted Glass Premium Theme Palette
val GamingObsidian = Color(0xFF0B0E14)       // Deep slate-black background
val GamingDeepSurface = Color(0x12FFFFFF)     // Translucent frosted white card surface (7% opacity)
val GamingBorderSlate = Color(0x1BFFFFFF)     // Translucent crisp frosted border (10% opacity)
val GamingGoldAccent = Color(0xFFFBBF24)      // Vibrant royal amber gold for wallets & VIP elements
val GamingNeonCyan = Color(0xFF818CF8)        // Glowing indigo-blue accent (classic frosted glass highlight icon/tabs)
val GamingVibrantIndigo = Color(0xFFC084FC)   // Glowing purple accent (complementary glow gradient)
val GamingBrightGreen = Color(0xFF34D399)     // Gorgeous emerald mint success / players indicator
val GamingTextMuted = Color(0xFF94A3B8)       // Elegant muted text
val GamingGoldSecondary = Color(0x15FBBF24)   // Translucent gold backing

val DarkPrimary = GamingNeonCyan // Primary is now Indigo-blue for the frosted glass theme
val DarkSecondary = GamingBrightGreen // Secondary is Emerald Mint
val DarkTertiary = GamingVibrantIndigo // Tertiary is Purple
val DarkBackground = GamingObsidian
val DarkSurface = GamingDeepSurface
val DarkOnPrimary = Color(0xFF0B0E14)
val DarkOnSecondary = Color(0xFF0B0E14)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFF8FAFC)

// Elegant frosted background gradient
val FrostedBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF131726), // Rich, deep slate-blue shade for glowing background depth
        Color(0xFF0B0E14)  // Base elegant bottom (#0B0E14)
    )
)


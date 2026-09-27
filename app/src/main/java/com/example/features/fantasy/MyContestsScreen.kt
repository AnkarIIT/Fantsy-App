package com.example.features.fantasy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.SportsCricket
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow as ComposeTextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CricketMatch
import com.example.data.models.FantasyContest
import com.example.data.models.LeaderboardEntry
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyContestsScreen(
    match: CricketMatch,
    contests: List<FantasyContest>,
    myTeams: List<MyContestEntry>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "My Contests",
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Rounded.Leaderboard, contentDescription = "Leaderboard", tint = GamingGoldAccent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myTeams) { entry ->
                MyContestCard(
                    entry = entry,
                    contest = contests.find { it.id == entry.contestId },
                    onClick = { /* navigate to leaderboard detail */ }
                )
            }
            
            item {
                if (myTeams.isEmpty()) {
                    Surface(
                        color = GamingDeepSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GamingBorderSlate),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.SportsCricket, contentDescription = null, tint = GamingTextMuted, modifier = Modifier.size(48.dp))
                            Text("No Active Contests", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Join a contest from the match screen to track your team here", color = GamingTextMuted, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

data class MyContestEntry(
    val teamId: String,
    val contestId: String,
    val teamName: String,
    val rank: Int,
    val points: Int,
    val winnings: Double,
    val captainName: String,
    val viceCaptainName: String
)

@Composable
fun MyContestCard(
    entry: MyContestEntry,
    contest: FantasyContest?,
    onClick: () -> Unit
) {
    val prizePool = contest?.prizePool ?: 0.0
    val entryFee = contest?.entryFee ?: 0.0
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = GamingDeepSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GamingBorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Rank and Contest Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        entry.teamName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        contest?.name ?: "Unknown Contest",
                        color = GamingTextMuted,
                        fontSize = 11.sp
                    )
                }
                
                // Rank Badge
                Surface(
                    color = when {
                        entry.rank == 1 -> GamingGoldAccent.copy(alpha = 0.2f)
                        entry.rank <= 3 -> GamingNeonCyan.copy(alpha = 0.2f)
                        else -> GamingBorderSlate.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(
                            imageVector = when {
                                entry.rank == 1 -> Icons.Rounded.EmojiEvents
                                entry.rank <= 3 -> Icons.Rounded.MilitaryTech
                                else -> Icons.Rounded.Leaderboard
                            },
                            contentDescription = "Rank",
                            tint = when {
                                entry.rank == 1 -> GamingGoldAccent
                                entry.rank <= 3 -> GamingNeonCyan
                                else -> GamingTextMuted
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "#${entry.rank}",
                            color = when {
                                entry.rank == 1 -> GamingGoldAccent
                                entry.rank <= 3 -> GamingNeonCyan
                                else -> GamingTextMuted
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
            
            // Points & Winnings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBox(
                    label = "Points",
                    value = entry.points.toString(),
                    valueColor = GamingNeonCyan,
                    icon = Icons.Rounded.EmojiEvents
                )
                StatBox(
                    label = "Winnings",
                    value = if (entry.winnings > 0) "₹${String.format("%,.0f", entry.winnings)}" else "—",
                    valueColor = GamingBrightGreen,
                    icon = Icons.Rounded.AccountBalanceWallet
                )
                StatBox(
                    label = "Entry",
                    value = if (entryFee == 0.0) "FREE" else "₹${entryFee.toInt()}",
                    valueColor = GamingGoldAccent,
                    icon = Icons.Rounded.CheckCircle
                )
            }
            
            // Captain / Vice-Captain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CaptionBox(
                    label = "C",
                    name = entry.captainName,
                    color = GamingGoldAccent
                )
                CaptionBox(
                    label = "VC",
                    name = entry.viceCaptainName,
                    color = GamingBrightGreen
                )
            }
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        color = GamingBorderSlate.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(16.dp))
            Text(label, color = GamingTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text(value, color = valueColor, fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
    }
}

@Composable
fun CaptionBox(
    label: String,
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Surface(
                color = color,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.padding(bottom = 4.dp).size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 8.sp)
                }
            }
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = ComposeTextOverflow.Ellipsis)
        }
    }
}
package com.example.features.fantasy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CricketMatch
import com.example.data.models.FantasyContest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FantasyContestsScreen(
    match: CricketMatch,
    contests: List<FantasyContest>,
    joinedContestIds: Set<String> = emptySet(),
    onNavigateBack: () -> Unit,
    onSelectContest: (FantasyContest) -> Unit,
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
                            "${match.team1.shortName} vs ${match.team2.shortName}",
                            fontWeight = FontWeight.Bold
                        )
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
            item {
                Text(
                    "All Contests",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(contests) { contest ->
                val isJoined = joinedContestIds.contains(contest.id)
                ContestCard(
                    contest = contest,
                    isJoined = isJoined,
                    onClick = { if (!isJoined) onSelectContest(contest) }
                )
            }
        }
    }
}

@Composable
fun ContestCard(
    contest: FantasyContest,
    isJoined: Boolean = false,
    onClick: () -> Unit
) {
    val progress = (contest.joinedTeams.toFloat() / contest.maxTeams.toFloat())

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
            // Contest Name and Prize Pool
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        contest.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Prize Pool: ₹${String.format("%,.0f", contest.prizePool)}",
                        color = GamingBrightGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                
                if (isJoined) {
                    Surface(
                        color = GamingBrightGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "JOINED",
                            color = GamingBrightGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Surface(
                        color = if (contest.entryFee == 0.0) GamingBrightGreen.copy(alpha = 0.15f) else GamingNeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (contest.entryFee == 0.0) "FREE" else "₹${contest.entryFee.toInt()}",
                            color = if (contest.entryFee == 0.0) GamingBrightGreen else GamingNeonCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            // Progress Bar
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = GamingNeonCyan,
                    trackColor = GamingBorderSlate
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${contest.joinedTeams} joined",
                        color = GamingNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${contest.maxTeams - contest.joinedTeams} spots left",
                        color = GamingBorderSlate,
                        fontSize = 11.sp
                    )
                }
            }
            
            // Prize Breakdown Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                contest.prizeBreakdown.take(2).forEach { tier ->
                    Surface(
                        color = GamingBorderSlate.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${tier.rankRange.first}: ₹${String.format("%,.0f", tier.amount)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                if (contest.prizeBreakdown.size > 2) {
                    Surface(
                        color = GamingBorderSlate.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "+${contest.prizeBreakdown.size - 2} more",
                            color = GamingTextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

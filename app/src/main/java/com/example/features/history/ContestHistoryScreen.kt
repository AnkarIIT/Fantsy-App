package com.example.features.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MatchStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestHistoryScreen(
    contests: List<ContestHistoryItem>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Upcoming", "Live", "Completed")

    val filteredContests = when (selectedTab) {
        1 -> contests.filter { it.status == MatchStatus.UPCOMING }
        2 -> contests.filter { it.status == MatchStatus.LIVE }
        3 -> contests.filter { it.status == MatchStatus.COMPLETED }
        else -> contests
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MY CONTESTS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Go Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = GamingNeonCyan,
                indicator = { tabPositions ->
                    val tab = tabPositions[selectedTab]
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .width(tab.right - tab.left)
                            .offset(x = tab.left),
                        color = GamingNeonCyan,
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = GamingNeonCyan,
                        unselectedContentColor = GamingTextMuted
                    )
                }
            }

            // Contest List
            if (filteredContests.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = GamingTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No contests found",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Join contests to see them here",
                        color = GamingTextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredContests) { contest ->
                        ContestHistoryCard(contest = contest)
                    }
                }
            }
        }
    }
}

@Composable
fun ContestHistoryCard(contest: ContestHistoryItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GamingDeepSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GamingBorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Match Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamLogo(abbreviation = contest.team1Abbreviation)
                    Text("VS", color = GamingTextMuted, fontWeight = FontWeight.Bold)
                    TeamLogo(abbreviation = contest.team2Abbreviation)
                }
                Surface(
                    color = when (contest.status) {
                        MatchStatus.LIVE -> GamingBrightGreen.copy(alpha = 0.15f)
                        MatchStatus.UPCOMING -> GamingGoldAccent.copy(alpha = 0.15f)
                        MatchStatus.COMPLETED -> GamingBorderSlate
                        MatchStatus.CANCELLED -> Color.Red.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when (contest.status) {
                            MatchStatus.LIVE -> "LIVE"
                            MatchStatus.UPCOMING -> "UPCOMING"
                            MatchStatus.COMPLETED -> "COMPLETED"
                            MatchStatus.CANCELLED -> "CANCELLED"
                        },
                        color = when (contest.status) {
                            MatchStatus.LIVE -> GamingBrightGreen
                            MatchStatus.UPCOMING -> GamingGoldAccent
                            MatchStatus.COMPLETED -> GamingTextMuted
                            MatchStatus.CANCELLED -> Color.Red
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Contest Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = contest.contestName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = contest.matchName,
                        color = GamingTextMuted,
                        fontSize = 12.sp
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "₹${contest.entryFee}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Divider(color = GamingBorderSlate, thickness = 0.5.dp)

            // Result/Rank Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Your Rank",
                        color = GamingTextMuted,
                        fontSize = 11.sp
                    )
                    contest.rank?.let {
                        Text(
                            text = "#$it",
                            color = if (it <= 10) GamingGoldAccent else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } ?: Text(
                        text = "--",
                        color = GamingTextMuted,
                        fontSize = 16.sp
                    )
                }
                contest.prizeWon?.let { prize ->
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Prize Won",
                            color = GamingTextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+₹${prize.toInt()}",
                            color = GamingBrightGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } ?: Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Prize Pool",
                        color = GamingTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "₹${contest.prizePool.toInt()}",
                        color = GamingGoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Action Button
            var showDetails by remember { mutableStateOf(false) }
            if (contest.status == MatchStatus.LIVE || contest.status == MatchStatus.COMPLETED) {
                Button(
                    onClick = { showDetails = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingNeonCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (contest.status == MatchStatus.LIVE) "View Leaderboard" else "View Results",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (showDetails) {
                AlertDialog(
                    onDismissRequest = { showDetails = false },
                    title = { Text(contest.contestName, color = Color.White) },
                    text = {
                        Column {
                            Text("Match: ${contest.matchName}")
                            Text("Your rank: ${contest.rank?.let { "#$it" } ?: "--"}")
                            Text("Prize: ₹${contest.prizeWon ?: contest.prizePool}")
                            Text("Status: ${contest.status}")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showDetails = false }, colors = ButtonDefaults.buttonColors(containerColor = GamingNeonCyan)) {
                            Text("Close", color = Color.Black)
                        }
                    },
                    containerColor = GamingDeepSurface
                )
            }
        }
    }
}

@Composable
fun TeamLogo(abbreviation: String) {
    Surface(
        shape = CircleShape,
        color = GamingVibrantIndigo.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, GamingVibrantIndigo)
    ) {
        Text(
            text = abbreviation,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}

data class ContestHistoryItem(
    val id: String,
    val contestName: String,
    val matchName: String,
    val team1Abbreviation: String,
    val team2Abbreviation: String,
    val entryFee: Double,
    val prizePool: Double,
    val status: MatchStatus,
    val rank: Int? = null,
    val prizeWon: Double? = null
)

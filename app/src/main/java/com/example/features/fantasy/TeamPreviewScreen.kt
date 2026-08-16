package com.example.features.fantasy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CricketMatch
import com.example.data.models.CricketPlayer
import com.example.data.models.FantasyContest
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamPreviewScreen(
    match: CricketMatch,
    contest: FantasyContest,
    selectedPlayers: List<CricketPlayer>,
    captainId: String,
    viceCaptainId: String,
    onConfirm: (onError: (String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Confirm Team", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GamingDeepSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Entry Fee",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "₹${contest.entryFee}",
                            color = GamingNeonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Prize Pool",
                            color = GamingTextMuted,
                            fontSize = 14.sp
                        )
                        Text(
                            "₹${String.format("%,.0f", contest.prizePool)}",
                            color = GamingBrightGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            onConfirm { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error, withDismissAction = true)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(GamingNeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Join Contest",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Match info + selected count
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = GamingDeepSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GamingBorderSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GamingNeonCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        match.team1.shortName,
                                        color = GamingNeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(8.dp, 4.dp)
                                    )
                                }
                                Text(
                                    match.team1.name,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                "VS",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GamingNeonCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        match.team2.shortName,
                                        color = GamingNeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(8.dp, 4.dp)
                                    )
                                }
                                Text(
                                    match.team2.name,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Divider(color = GamingBorderSlate)
                        Text(
                            "11 Players Selected",
                            color = GamingBrightGreen,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Grouped players by role
            val playersByRole = selectedPlayers.groupBy { it.role }
            val orderedRoles = listOf(
                com.example.data.models.PlayerRole.WICKETKEEPER,
                com.example.data.models.PlayerRole.BATTER,
                com.example.data.models.PlayerRole.ALLROUNDER,
                com.example.data.models.PlayerRole.BOWLER
            )
            orderedRoles.forEach { role ->
                val playersInRole = playersByRole[role]
                if (playersInRole != null && playersInRole.isNotEmpty()) {
                    item {
                        Text(
                            role.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = GamingTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(playersInRole) { player ->
                        PlayerPreviewItem(
                            player = player,
                            isCaptain = player.id == captainId,
                            isViceCaptain = player.id == viceCaptainId
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerPreviewItem(
    player: CricketPlayer,
    isCaptain: Boolean = false,
    isViceCaptain: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDeepSurface,
        border = BorderStroke(
            1.dp,
            when {
                isCaptain -> GamingGoldAccent
                isViceCaptain -> GamingBrightGreen
                else -> GamingBorderSlate
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        isCaptain -> GamingGoldAccent.copy(alpha = 0.2f)
                        isViceCaptain -> GamingBrightGreen.copy(alpha = 0.2f)
                        else -> GamingBorderSlate.copy(alpha = 0.2f)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            player.name.first().toString().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        player.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (isCaptain) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = GamingGoldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Captain (2x Points)",
                                color = GamingGoldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isViceCaptain) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = GamingBrightGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Vice Captain (1.5x Points)",
                                color = GamingBrightGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Surface(
                color = GamingBorderSlate.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    "₹${player.credit}",
                    color = GamingNeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp, 4.dp)
                )
            }
        }
    }
}

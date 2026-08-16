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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CricketMatch
import com.example.data.models.MatchStatus
import com.example.ui.theme.GamingBorderSlate
import com.example.ui.theme.GamingDeepSurface
import com.example.ui.theme.GamingNeonCyan
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FantasyMatchesScreen(
    matches: List<CricketMatch>,
    onNavigateBack: () -> Unit,
    onSelectMatch: (CricketMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FANTASY CRICKET",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
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
                    "Upcoming Matches",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(matches) { match ->
                MatchCard(
                    match = match,
                    onClick = { onSelectMatch(match) }
                )
            }
        }
    }
}

@Composable
fun MatchCard(match: CricketMatch, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val matchTime = dateFormat.format(Date(match.startTime))

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
            // Teams vs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamLogoAndName(
                    shortName = match.team1.shortName,
                    fullName = match.team1.name
                )
                
                Text("VS", color = GamingNeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                TeamLogoAndName(
                    shortName = match.team2.shortName,
                    fullName = match.team2.name
                )
            }
            
            Divider(color = GamingBorderSlate)
            
            // Match details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        match.venue,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Text(
                        matchTime,
                        color = GamingNeonCyan,
                        fontSize = 12.sp
                    )
                }
                
                Surface(
                    color = GamingNeonCyan,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${match.contestCount} Contests",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TeamLogoAndName(shortName: String, fullName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingNeonCyan.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, GamingNeonCyan)
        ) {
            Text(
                shortName,
                color = GamingNeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        Text(
            fullName,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

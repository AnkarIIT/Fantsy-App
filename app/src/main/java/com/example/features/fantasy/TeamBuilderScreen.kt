package com.example.features.fantasy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.models.CricketPlayer
import com.example.data.models.FantasyContest
import com.example.data.models.FantasyTeamDraft
import com.example.data.models.PlayerRole
import com.example.ui.theme.*
import java.util.UUID

// Define team validation rules
private data class TeamValidationRules(
    val minWk: Int = 1, val maxWk: Int = 4,
    val minBat: Int = 3, val maxBat: Int = 6,
    val minAr: Int = 1, val maxAr: Int = 4,
    val minBowl: Int = 3, val maxBowl: Int = 6,
    val maxPerTeam: Int = 7
)

private fun validateTeam(
    players: List<CricketPlayer>,
    match: CricketMatch,
    rules: TeamValidationRules = TeamValidationRules()
): Pair<Boolean, List<String>> {
    val errors = mutableListOf<String>()
    // Count per role
    val wkCount = players.count { it.role == PlayerRole.WICKETKEEPER }
    val batCount = players.count { it.role == PlayerRole.BATTER }
    val arCount = players.count { it.role == PlayerRole.ALLROUNDER }
    val bowlCount = players.count { it.role == PlayerRole.BOWLER }

    // Validate role counts
    if (wkCount < rules.minWk) errors.add("Need at least ${rules.minWk} wicket keeper!")
    if (wkCount > rules.maxWk) errors.add("Max ${rules.maxWk} wicket keepers allowed!")

    if (batCount < rules.minBat) errors.add("Need at least ${rules.minBat} batters!")
    if (batCount > rules.maxBat) errors.add("Max ${rules.maxBat} batters allowed!")

    if (arCount < rules.minAr) errors.add("Need at least ${rules.minAr} all-rounders!")
    if (arCount > rules.maxAr) errors.add("Max ${rules.maxAr} all-rounders allowed!")

    if (bowlCount < rules.minBowl) errors.add("Need at least ${rules.minBowl} bowlers!")
    if (bowlCount > rules.maxBowl) errors.add("Max ${rules.maxBowl} bowlers allowed!")

    // Max per team count
    val team1Count = players.count { it.teamId == match.team1.id }
    val team2Count = players.count { it.teamId == match.team2.id }
    if (team1Count > rules.maxPerTeam) errors.add("Max ${rules.maxPerTeam} players from ${match.team1.name}!")
    if (team2Count > rules.maxPerTeam) errors.add("Max ${rules.maxPerTeam} players from ${match.team2.name}!")

    // Credit budget (100 max)
    val totalCredits = players.sumOf { it.credit }
    if (totalCredits > 100.0) {
        errors.add("Credit budget exceeded! Total credits ${String.format("%.1f", totalCredits)} > 100.")
    }

    return (errors.isEmpty() && players.size == 11) to errors
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBuilderScreen(
    match: CricketMatch,
    contest: FantasyContest,
    allPlayers: List<CricketPlayer>,
    onNavigateBack: () -> Unit,
    onPreviewTeam: (FantasyTeamDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlayers by remember { mutableStateOf<List<CricketPlayer>>(emptyList()) }
    var captainId by remember { mutableStateOf<String?>(null) }
    var viceCaptainId by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf<PlayerRole?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var creditError by remember { mutableStateOf<String?>(null) }
    
    val remainingCredits = 100.0 - selectedPlayers.sumOf { it.credit }

    val (isTeamValid, validationErrors) = remember(selectedPlayers, match) {
        validateTeam(selectedPlayers, match)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Your Team", fontWeight = FontWeight.Bold)
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
        },
        bottomBar = {
            BottomBar(
                selectedCount = selectedPlayers.size,
                remainingCredits = remainingCredits,
                captainId = captainId,
                viceCaptainId = viceCaptainId,
                isTeamValid = isTeamValid,
                validationErrors = validationErrors,
                onShowValidationErrors = { showValidationErrors = it },
                onSaveTeam = {
                    if (selectedPlayers.size == 11 && captainId != null && viceCaptainId != null && isTeamValid) {
                        onPreviewTeam(
                            FantasyTeamDraft(
                                teamId = UUID.randomUUID().toString(),
                                matchId = match.id,
                                contestId = contest.id,
                                players = selectedPlayers,
                                captainId = captainId!!,
                                viceCaptainId = viceCaptainId!!
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Role Tabs
            RoleTabs(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )

            // Selected Players Summary (Role Count Cards)
            SelectedPlayersSummary(
                selectedPlayers = selectedPlayers,
                match = match
            )

            // Players List
            val playersByRole = if (selectedRole == null) {
                allPlayers
            } else {
                allPlayers.filter { it.role == selectedRole }
            }
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playersByRole) { player ->
                    PlayerCard(
                        player = player,
                        isSelected = selectedPlayers.contains(player),
                        isCaptain = captainId == player.id,
                        isViceCaptain = viceCaptainId == player.id,
                        onToggle = {
                            if (selectedPlayers.contains(player)) {
                                selectedPlayers = selectedPlayers - player
                                creditError = null
                                if (captainId == player.id) captainId = null
                                if (viceCaptainId == player.id) viceCaptainId = null
                            } else {
                                if (selectedPlayers.size >= 11) {
                                    creditError = "Team is full. Max 11 players allowed."
                                } else if (player.credit > remainingCredits) {
                                    creditError = "Insufficient credits! You need ${String.format("%.1f", player.credit)} credits but only ${String.format("%.1f", remainingCredits)} remain."
                                } else {
                                    selectedPlayers = selectedPlayers + player
                                    creditError = null
                                }
                            }
                        },
                        onSetCaptain = {
                            if (selectedPlayers.contains(player)) {
                                if (viceCaptainId == player.id) viceCaptainId = null
                                captainId = player.id
                            }
                        },
                        onSetViceCaptain = {
                            if (selectedPlayers.contains(player)) {
                                if (captainId == player.id) captainId = null
                                viceCaptainId = player.id
                            }
                        }
                    )
                }
            }

            creditError?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = Color(0xFF3A0D0D),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        it,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Validation Errors Dialog
            if (showValidationErrors) {
                AlertDialog(
                    onDismissRequest = { showValidationErrors = false },
                    title = { Text("Team Validation Errors", color = Color.White) },
                    text = {
                        Column {
                            validationErrors.forEach { err ->
                                Text("• $err", color = GamingTextMuted, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showValidationErrors = false }, colors = ButtonDefaults.buttonColors(GamingNeonCyan)) {
                            Text("Okay", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = GamingDeepSurface
                )
            }
        }
    }
}

@Composable
fun SelectedPlayersSummary(
    selectedPlayers: List<CricketPlayer>,
    match: CricketMatch
) {
    val wkCount = selectedPlayers.count { it.role == PlayerRole.WICKETKEEPER }
    val batCount = selectedPlayers.count { it.role == PlayerRole.BATTER }
    val arCount = selectedPlayers.count { it.role == PlayerRole.ALLROUNDER }
    val bowlCount = selectedPlayers.count { it.role == PlayerRole.BOWLER }
    val team1Count = selectedPlayers.count { it.teamId == match.team1.id }
    val team2Count = selectedPlayers.count { it.teamId == match.team2.id }

    Surface(
        color = GamingDeepSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GamingBorderSlate),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Selected Players Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoleCountChip("WK", wkCount, 1, 4)
                RoleCountChip("BAT", batCount, 3, 6)
                RoleCountChip("AR", arCount, 1,4)
                RoleCountChip("BOWL", bowlCount, 3,6)
            }
            Divider(color = GamingBorderSlate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoleCountChip(match.team1.shortName, team1Count, 0, 7)
                RoleCountChip(match.team2.shortName, team2Count, 0,7)
            }
        }
    }
}

@Composable
fun RoleCountChip(
    label: String,
    count: Int,
    min: Int,
    max: Int
) {
    val isValid = count >= min && count <= max
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isValid) GamingNeonCyan.copy(0.15f) else Color.Red.copy(0.15f),
        border = BorderStroke(1.dp, if (isValid) GamingNeonCyan else Color.Red)
    ) {
        Text(
            "$label: $count",
            color = if (isValid) GamingNeonCyan else Color.Red,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun RoleTabs(
    selectedRole: PlayerRole?,
    onRoleSelected: (PlayerRole?) -> Unit
) {
    val roles = listOf(
        null to "All",
        PlayerRole.WICKETKEEPER to "WK",
        PlayerRole.BATTER to "BAT",
        PlayerRole.ALLROUNDER to "AR",
        PlayerRole.BOWLER to "BOWL"
    )
    
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(roles) { (role, label) ->
            Surface(
                onClick = { onRoleSelected(role) },
                shape = RoundedCornerShape(20.dp),
                color = if (selectedRole == role) GamingNeonCyan else GamingDeepSurface,
                border = if (selectedRole != role) BorderStroke(1.dp, GamingBorderSlate) else null
            ) {
                Text(
                    label,
                    color = if (selectedRole == role) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PlayerCard(
    player: CricketPlayer,
    isSelected: Boolean,
    isCaptain: Boolean,
    isViceCaptain: Boolean,
    onToggle: () -> Unit,
    onSetCaptain: () -> Unit,
    onSetViceCaptain: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isSelected) GamingNeonCyan.copy(alpha = 0.1f) else GamingDeepSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isCaptain || isViceCaptain) 2.dp else 1.dp,
            color = when {
                isCaptain -> GamingGoldAccent
                isViceCaptain -> GamingBrightGreen
                else -> GamingBorderSlate
            }
        ),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) GamingNeonCyan else GamingBorderSlate
                ) {
                    Text(
                        player.name.take(2).uppercase(),
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
                
                // Name & Role
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        player.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        player.role.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = GamingTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Credits & Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Credits
                Surface(
                    color = GamingBorderSlate.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "₹${player.credit}",
                        color = GamingGoldAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
                
                // Captain / VC buttons
                if (isSelected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CaptionToggleButton(
                            label = "C",
                            isActive = isCaptain,
                            onClick = onSetCaptain
                        )
                        CaptionToggleButton(
                            label = "VC",
                            isActive = isViceCaptain,
                            onClick = onSetViceCaptain
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaptionToggleButton(label: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) GamingGoldAccent else GamingBorderSlate.copy(alpha = 0.3f)
    ) {
        Text(
            label,
            color = if (isActive) Color.Black else GamingTextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BottomBar(
    selectedCount: Int,
    remainingCredits: Double,
    captainId: String?,
    viceCaptainId: String?,
    isTeamValid: Boolean,
    validationErrors: List<String>,
    onShowValidationErrors: (Boolean) -> Unit,
    onSaveTeam: () -> Unit
) {
    val canSave = selectedCount == 11 && captainId != null && viceCaptainId != null && isTeamValid
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GamingDeepSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Players", value = "$selectedCount/11")
                StatItem(label = "Credits Left", value = "₹${String.format("%.1f", remainingCredits)}", color = GamingNeonCyan)
            }
            
            // Save Button
            Button(
                onClick = {
                    if (isTeamValid) {
                        onSaveTeam()
                    } else {
                        onShowValidationErrors(true)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCount ==11 && captainId != null && viceCaptainId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamingNeonCyan,
                    disabledContainerColor = GamingBorderSlate
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (canSave) "Save & Join Contest" else if (validationErrors.isNotEmpty()) "Fix Team Errors" else "Select 11 Players (C & VC)",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = Color.White) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = GamingTextMuted, fontSize = 12.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

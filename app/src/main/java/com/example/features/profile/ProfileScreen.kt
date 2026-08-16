package com.example.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.KYCStatus
import com.example.data.models.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToKYC: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToContestHistory: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onUpdateName: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MY PROFILE",
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = GamingDeepSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GamingVibrantIndigo, GamingNeonCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Text(
                        text = userProfile.name,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        text = userProfile.phone,
                        color = GamingTextMuted,
                        fontSize = 12.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (userProfile.kycStatus) {
                                KYCStatus.VERIFIED -> GamingBrightGreen.copy(alpha = 0.15f)
                                KYCStatus.PENDING -> GamingGoldAccent.copy(alpha = 0.15f)
                                KYCStatus.REJECTED -> Color.Red.copy(alpha = 0.15f)
                                KYCStatus.NOT_SUBMITTED -> GamingBorderSlate
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (userProfile.kycStatus) {
                                    KYCStatus.VERIFIED -> "KYC Verified"
                                    KYCStatus.PENDING -> "KYC Pending"
                                    KYCStatus.REJECTED -> "KYC Rejected"
                                    KYCStatus.NOT_SUBMITTED -> "KYC Not Submitted"
                                },
                                color = when (userProfile.kycStatus) {
                                    KYCStatus.VERIFIED -> GamingBrightGreen
                                    KYCStatus.PENDING -> GamingGoldAccent
                                    KYCStatus.REJECTED -> Color.Red
                                    KYCStatus.NOT_SUBMITTED -> GamingTextMuted
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            color = GamingVibrantIndigo.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Level ${userProfile.level}",
                                color = GamingVibrantIndigo,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Stats Grid
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = GamingDeepSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total Winnings",
                        value = "₹${userProfile.totalWinnings.toInt()}",
                        color = GamingBrightGreen
                    )
                    StatItem(
                        label = "Matches",
                        value = userProfile.totalMatches.toString(),
                        color = GamingNeonCyan
                    )
                    StatItem(
                        label = "Win Rate",
                        value = "${userProfile.winRate.toInt()}%",
                        color = GamingGoldAccent
                    )
                }
            }

            // Menu Options
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = GamingDeepSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    var showEditDialog by remember { mutableStateOf(false) }
                    var editedName by remember { mutableStateOf(userProfile.name) }

                    MenuItem(
                        icon = Icons.Rounded.PersonOutline,
                        title = "Edit Profile",
                        onClick = { showEditDialog = true },
                        showDivider = true
                    )

                    if (showEditDialog) {
                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            title = { Text("Edit Profile", color = Color.White) },
                            text = {
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GamingNeonCyan, unfocusedTextColor = Color.White)
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    onUpdateName(editedName.trim().ifBlank { userProfile.name })
                                    showEditDialog = false
                                }, colors = ButtonDefaults.buttonColors(containerColor = GamingGoldAccent)) {
                                    Text("Save", color = Color.Black)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = Color.White) }
                            },
                            containerColor = GamingDeepSurface
                        )
                    }
                    MenuItem(
                        icon = Icons.Rounded.VerifiedUser,
                        title = "KYC Verification",
                        onClick = onNavigateToKYC,
                        showDivider = true,
                        badge = if (userProfile.kycStatus != KYCStatus.VERIFIED) "Pending" else null
                    )
                    MenuItem(
                        icon = Icons.Rounded.Share,
                        title = "Refer & Earn",
                        onClick = onNavigateToReferral,
                        showDivider = true
                    )
                    MenuItem(
                        icon = Icons.Rounded.History,
                        title = "My Contests",
                        onClick = onNavigateToContestHistory,
                        showDivider = true
                    )
                    MenuItem(
                        icon = Icons.Rounded.HelpOutline,
                        title = "Help & Support",
                        onClick = onNavigateToHelp,
                        showDivider = true
                    )
                    MenuItem(
                        icon = Icons.Rounded.Settings,
                        title = "Settings",
                        onClick = onNavigateToSettings,
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = GamingTextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean,
    badge: String? = null
) {
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GamingNeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    badge?.let {
                        Surface(
                            color = GamingGoldAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = it,
                                color = GamingGoldAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = GamingTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (showDivider) {
            Divider(color = GamingBorderSlate, thickness = 0.5.dp)
        }
    }
}

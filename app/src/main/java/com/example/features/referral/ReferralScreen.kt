package com.example.features.referral

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Referral
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    referralCode: String,
    referrals: List<Referral>,
    totalEarnings: Double,
    onNavigateBack: () -> Unit,
    onShareReferral: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "REFER & EARN",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GamingVibrantIndigo, GamingNeonCyan)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Invite Friends & Earn",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Get ₹100 when your friend joins and ₹50 more when they deposit",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Stats Card
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
                        label = "Total Referrals",
                        value = referrals.size.toString(),
                        color = GamingNeonCyan
                    )
                    StatItem(
                        label = "Total Earnings",
                        value = "₹${totalEarnings.toInt()}",
                        color = GamingBrightGreen
                    )
                    StatItem(
                        label = "Pending",
                        value = referrals.count { it.status == com.example.data.models.ReferralStatus.PENDING }.toString(),
                        color = GamingGoldAccent
                    )
                }
            }

            // Referral Code Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = GamingDeepSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Your Referral Code",
                        color = GamingTextMuted,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = GamingBorderSlate.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GamingNeonCyan)
                    ) {
                        Text(
                            text = referralCode,
                            color = GamingNeonCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            letterSpacing = 4.sp
                        )
                    }
                    Button(
                        onClick = onShareReferral,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GamingGoldAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Referral Code", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Referral History
            if (referrals.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Referral History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = GamingDeepSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GamingBorderSlate)
                    ) {
                        Column {
                            referrals.forEach { referral ->
                                ReferralHistoryItem(referral = referral)
                                if (referral != referrals.last()) {
                                    Divider(color = GamingBorderSlate, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
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
            fontSize = 20.sp,
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
fun ReferralHistoryItem(referral: Referral) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = when (referral.status) {
                    com.example.data.models.ReferralStatus.COMPLETED -> GamingBrightGreen.copy(alpha = 0.15f)
                    com.example.data.models.ReferralStatus.PENDING -> GamingGoldAccent.copy(alpha = 0.15f)
                    com.example.data.models.ReferralStatus.FAILED -> Color.Red.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonAdd,
                    contentDescription = null,
                    tint = when (referral.status) {
                        com.example.data.models.ReferralStatus.COMPLETED -> GamingBrightGreen
                        com.example.data.models.ReferralStatus.PENDING -> GamingGoldAccent
                        com.example.data.models.ReferralStatus.FAILED -> Color.Red
                    },
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = referral.referredUserName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = when (referral.status) {
                        com.example.data.models.ReferralStatus.COMPLETED -> "Completed"
                        com.example.data.models.ReferralStatus.PENDING -> "Pending"
                        com.example.data.models.ReferralStatus.FAILED -> "Failed"
                    },
                    color = when (referral.status) {
                        com.example.data.models.ReferralStatus.COMPLETED -> GamingBrightGreen
                        com.example.data.models.ReferralStatus.PENDING -> GamingGoldAccent
                        com.example.data.models.ReferralStatus.FAILED -> Color.Red
                    },
                    fontSize = 12.sp
                )
            }
        }
        if (referral.bonusEarned > 0) {
            Text(
                text = "+₹${referral.bonusEarned.toInt()}",
                color = GamingBrightGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

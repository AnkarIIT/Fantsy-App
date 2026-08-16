package com.example

import android.os.Bundle
import android.os.Vibrator
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.models.*
import com.example.features.game.GameDetailScreen
import com.example.features.lobby.LobbyScreen
import com.example.features.lobby.LobbyViewModel
import com.example.features.wallet.WalletScreen
import com.example.features.profile.ProfileScreen
import com.example.features.settings.SettingsScreen
import com.example.features.help.HelpScreen
import com.example.features.kyc.KYCScreen
import com.example.features.referral.ReferralScreen
import com.example.features.history.ContestHistoryScreen
import com.example.features.history.ContestHistoryItem
import com.example.features.fantasy.FantasyMatchesScreen
import com.example.features.fantasy.FantasyContestsScreen
import com.example.features.fantasy.TeamBuilderScreen
import com.example.features.fantasy.TeamPreviewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.GamingBorderSlate
import com.example.ui.theme.GamingDeepSurface
import com.example.ui.theme.GamingNeonCyan

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GamingAppContainer()
            }
        }
    }
}

@Composable
fun GamingAppContainer() {
    val navController = rememberNavController()
    val lobbyViewModel: LobbyViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // Sample data for new features
    val sampleUserProfile = UserProfile(
        id = "user123",
        name = "John Doe",
        email = "john@example.com",
        phone = "+91 98765 43210",
        referralCode = "JOHN123",
        kycStatus = KYCStatus.NOT_SUBMITTED,
        totalWinnings = 5000.0,
        totalMatches = 42,
        winRate = 65.0f
    )

    val liveKycStatus by lobbyViewModel.kycStatus.collectAsStateWithLifecycle()
    val liveDisplayName by lobbyViewModel.userDisplayName.collectAsStateWithLifecycle()
    val liveUserProfile = sampleUserProfile.copy(
        name = liveDisplayName,
        kycStatus = liveKycStatus
    )

    val sampleAppSettings = AppSettings(
        notificationsEnabled = true,
        soundEnabled = true,
        vibrationEnabled = true,
        darkMode = true
    )

    val liveAppSettings by lobbyViewModel.appSettings.collectAsStateWithLifecycle()
    val playedGames by lobbyViewModel.playedGames.collectAsStateWithLifecycle()
    val fantasyTeams by lobbyViewModel.fantasyTeams.collectAsStateWithLifecycle()
    val teamPreviewDraft by lobbyViewModel.teamPreviewDraft.collectAsStateWithLifecycle()

    val sampleFAQs = listOf(
        FAQItem("faq1", "How do I deposit money?", "You can deposit money using UPI, debit/credit cards, or net banking from the wallet screen.", FAQCategory.DEPOSIT),
        FAQItem("faq2", "How do I withdraw my winnings?", "To withdraw, you need to complete KYC first. Then you can withdraw to your bank account.", FAQCategory.WITHDRAWAL),
        FAQItem("faq3", "How to create a fantasy team?", "Go to any upcoming match, select players within your credit limit, choose captain and vice-captain.", FAQCategory.GAMEPLAY),
        FAQItem("faq4", "Is KYC mandatory?", "Yes, KYC is mandatory for withdrawals to comply with legal regulations.", FAQCategory.KYC),
        FAQItem("faq5", "How does refer and earn work?", "Share your referral code. When your friend joins and deposits, you both get bonuses.", FAQCategory.REFERRAL)
    )

    val sampleReferrals = listOf(
        Referral("ref1", "user123", "user456", "Jane Smith", ReferralStatus.COMPLETED, 100.0),
        Referral("ref2", "user123", "user789", "Bob Wilson", ReferralStatus.PENDING, 0.0)
    )

    val sampleContestHistory = listOf(
        ContestHistoryItem("c1", "Mega League", "IND vs AUS T20", "IND", "AUS", 49.0, 100000.0, MatchStatus.COMPLETED, 23, 500.0),
        ContestHistoryItem("c2", "Head to Head", "MI vs CSK", "MI", "CSK", 20.0, 1000.0, MatchStatus.LIVE),
        ContestHistoryItem("c3", "Practice League", "RCB vs KKR", "RCB", "KKR", 0.0, 500.0, MatchStatus.UPCOMING)
    )

    fun triggerVibration() {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(25)
    }

    // Determine if bottom bar should be visible
    val showBottomBar = currentRoute in listOf("lobby", "wallet", "profile")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.FrostedBackgroundBrush)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = GamingDeepSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("app_bottom_navigation")
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == "lobby",
                            onClick = {
                                if (currentRoute != "lobby") {
                                    triggerVibration()
                                    navController.navigate("lobby") {
                                        popUpTo("lobby") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.SportsEsports,
                                    contentDescription = "Esports Arena"
                                )
                            },
                            label = { Text("Arena", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GamingNeonCyan,
                                selectedTextColor = GamingNeonCyan,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = GamingBorderSlate
                            ),
                            modifier = Modifier.testTag("nav_lobby_item")
                        )

                        NavigationBarItem(
                            selected = currentRoute == "wallet",
                            onClick = {
                                if (currentRoute != "wallet") {
                                    triggerVibration()
                                    navController.navigate("wallet") {
                                        popUpTo("lobby") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = "My Wallet Ledger"
                                )
                            },
                            label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GamingNeonCyan,
                                selectedTextColor = GamingNeonCyan,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = GamingBorderSlate
                            ),
                            modifier = Modifier.testTag("nav_wallet_item")
                        )

                        NavigationBarItem(
                            selected = currentRoute == "profile",
                            onClick = {
                                if (currentRoute != "profile") {
                                    triggerVibration()
                                    navController.navigate("profile") {
                                        popUpTo("lobby") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "Profile"
                                )
                            },
                            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GamingNeonCyan,
                                selectedTextColor = GamingNeonCyan,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = GamingBorderSlate
                            ),
                            modifier = Modifier.testTag("nav_profile_item")
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "lobby",
                modifier = Modifier.padding(
                    bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                composable("lobby") {
                    LobbyScreen(
                        viewModel = lobbyViewModel,
                        onNavigateToWallet = {
                            navController.navigate("wallet") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToGame = { matchId ->
                            navController.navigate("game/$matchId")
                        },
                        onNavigateToFantasyCricket = {
                            navController.navigate("fantasy/matches")
                        }
                    )
                }
                
                // Fantasy Cricket Routes
                composable("fantasy/matches") {
                    FantasyMatchesScreen(
                        matches = lobbyViewModel.fantasyMatches,
                        onNavigateBack = { navController.popBackStack() },
                        onSelectMatch = { match ->
                            navController.navigate("fantasy/contests/${match.id}")
                        }
                    )
                }
                
                composable(
                    route = "fantasy/contests/{matchId}",
                    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                    val match = lobbyViewModel.fantasyMatches.find { it.id == matchId }
                    val joined by lobbyViewModel.joinedFantasyContests.collectAsStateWithLifecycle()
                    match?.let {
                        FantasyContestsScreen(
                            match = it,
                            contests = lobbyViewModel.getFantasyContests(matchId),
                            joinedContestIds = joined,
                            onNavigateBack = { navController.popBackStack() },
                            onSelectContest = { contest ->
                                if (!joined.contains(contest.id)) {
                                    navController.navigate("fantasy/team-builder/$matchId/${contest.id}")
                                }
                            }
                        )
                    }
                }
                
                composable(
                    route = "fantasy/team-builder/{matchId}/{contestId}",
                    arguments = listOf(
                        navArgument("matchId") { type = NavType.StringType },
                        navArgument("contestId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                    val contestId = backStackEntry.arguments?.getString("contestId") ?: ""
                    val match = lobbyViewModel.fantasyMatches.find { it.id == matchId }
                    val contest = lobbyViewModel.getFantasyContests(matchId).find { it.id == contestId }
                    if (match != null && contest != null) {
                        TeamBuilderScreen(
                            match = match,
                            contest = contest,
                            allPlayers = lobbyViewModel.getFantasyPlayers(matchId),
                            onNavigateBack = { navController.popBackStack() },
                            onPreviewTeam = { draft ->
                                lobbyViewModel.setTeamPreviewDraft(draft)
                                navController.navigate("fantasy/team-preview/$matchId/${contest.id}")
                            }
                        )
                    }
                }

                composable(
                    route = "fantasy/team-preview/{matchId}/{contestId}",
                    arguments = listOf(
                        navArgument("matchId") { type = NavType.StringType },
                        navArgument("contestId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                    val contestId = backStackEntry.arguments?.getString("contestId") ?: ""
                    val match = lobbyViewModel.fantasyMatches.find { it.id == matchId }
                    val contest = lobbyViewModel.getFantasyContests(matchId).find { it.id == contestId }
                    val draft = teamPreviewDraft

                    if (match != null && contest != null && draft != null) {
                        TeamPreviewScreen(
                            match = match,
                            contest = contest,
                            selectedPlayers = draft.players,
                            captainId = draft.captainId,
                            viceCaptainId = draft.viceCaptainId,
                            onBack = { navController.popBackStack() },
                            onConfirm = { onError ->
                                lobbyViewModel.joinFantasyContest(
                                    matchId = draft.matchId,
                                    contest = contest,
                                    teamId = draft.teamId,
                                    captainId = draft.captainId,
                                    viceCaptainId = draft.viceCaptainId,
                                    players = draft.players
                                ) { result ->
                                    if (result.isSuccess) {
                                        lobbyViewModel.clearTeamPreviewDraft()
                                        navController.navigate("history") {
                                            popUpTo("fantasy/matches")
                                        }
                                    } else {
                                        onError(result.exceptionOrNull()?.message ?: "Failed to join contest. Please check your balance and try again.")
                                    }
                                }
                            }
                        )
                    }
                }

                composable("wallet") {
                    WalletScreen(
                        viewModel = lobbyViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        userProfile = liveUserProfile,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToKYC = { navController.navigate("kyc") },
                        onNavigateToReferral = { navController.navigate("referral") },
                        onNavigateToContestHistory = { navController.navigate("history") },
                        onNavigateToHelp = { navController.navigate("help") },
                        onUpdateName = { lobbyViewModel.updateDisplayName(it) }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        appSettings = liveAppSettings,
                        onSettingsChanged = { lobbyViewModel.updateSettings(it) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("help") {
                    HelpScreen(
                        faqItems = sampleFAQs,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("kyc") {
                    KYCScreen(
                        kycStatus = liveKycStatus,
                        onNavigateBack = { navController.popBackStack() },
                        onSubmitKyc = { lobbyViewModel.submitKyc() }
                    )
                }

                composable("referral") {
                    val context = LocalContext.current
                    ReferralScreen(
                        referralCode = sampleUserProfile.referralCode,
                        referrals = sampleReferrals,
                        totalEarnings = 100.0,
                        onNavigateBack = { navController.popBackStack() },
                        onShareReferral = {
                            val shareText = "Join me on Royale Gaming! Use my referral code: ${sampleUserProfile.referralCode}\n\nDownload and play to earn bonuses together."
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share referral via"))
                        }
                    )
                }

                composable("history") {
                    // Mix sample data with live played games for a rich dynamic history
                    val dynamicHistory = playedGames.map { pg ->
                        ContestHistoryItem(
                            id = pg.id,
                            contestName = pg.title,
                            matchName = "Quick Match",
                            team1Abbreviation = "ME",
                            team2Abbreviation = "OPP",
                            entryFee = 0.0,
                            prizePool = pg.amount,
                            status = if (pg.result == "WIN") MatchStatus.COMPLETED else MatchStatus.CANCELLED,
                            rank = if (pg.result == "WIN") 1 else null,
                            prizeWon = if (pg.result == "WIN") pg.amount else null
                        )
                    }
                    val fantasyHistory = fantasyTeams.map { fantasyTeam ->
                        val fantasyMatch = lobbyViewModel.fantasyMatches.find { it.id == fantasyTeam.team.matchId }
                        val fantasyContest = lobbyViewModel
                            .getFantasyContests(fantasyTeam.team.matchId)
                            .find { it.id == fantasyTeam.team.contestId }
                        ContestHistoryItem(
                            id = fantasyTeam.team.id,
                            contestName = fantasyContest?.name ?: "My Fantasy Team",
                            matchName = fantasyMatch?.let { "${it.team1.shortName} vs ${it.team2.shortName}" } ?: "Fantasy Match",
                            team1Abbreviation = fantasyMatch?.team1?.shortName ?: "T1",
                            team2Abbreviation = fantasyMatch?.team2?.shortName ?: "T2",
                            entryFee = fantasyContest?.entryFee ?: 0.0,
                            prizePool = fantasyContest?.prizePool ?: 0.0,
                            status = fantasyMatch?.status ?: MatchStatus.UPCOMING,
                            rank = fantasyTeam.team.rank,
                            prizeWon = fantasyTeam.team.totalPoints.takeIf { it > 0 }?.toDouble()
                        )
                    }
                    ContestHistoryScreen(
                        contests = fantasyHistory + dynamicHistory + sampleContestHistory,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "game/{matchId}",
                    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: "default"
                    GameDetailScreen(
                        matchId = matchId,
                        viewModel = lobbyViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToWallet = {
                            navController.navigate("wallet") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

package com.example.features.lobby

import android.os.Build
import android.os.Vibrator
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GameCategory
import com.example.data.models.GameItem
import com.example.data.models.TournamentMatch
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToGame: (String) -> Unit,
    onNavigateToFantasyCricket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val scope = rememberCoroutineScope()

    val uiState by viewModel.lobbyUiState.collectAsStateWithLifecycle()
    val wallet by viewModel.walletBalance.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showDepositDialog by remember { mutableStateOf(false) }
    var showJoinConfirmDialog by remember { mutableStateOf<TournamentMatch?>(null) }
    var snackbarHostState = remember { SnackbarHostState() }

    fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(40)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(color = GamingNeonCyan)
                            drawCircle(color = Color.White, radius = 2.dp.toPx())
                        }
                        Text(
                            text = "ROYALE GRAND",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Quick Wallet Wallet Chips
                    Surface(
                        onClick = {
                            triggerVibration()
                            onNavigateToWallet()
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("lobby_wallet_chip"),
                        shape = RoundedCornerShape(24.dp),
                        color = GamingDeepSurface,
                        border = BorderStroke(1.dp, GamingBorderSlate)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = "Wallet Balance",
                                tint = GamingGoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            val totalAmount = wallet?.let { it.depositBalance + it.winningsBalance } ?: 0.0
                            Text(
                                text = String.format("₹%.2f", totalAmount),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.Rounded.AddCircle,
                                contentDescription = "Add Balance",
                                tint = GamingNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Live Status Ticker Banner (Marquee representation)
            LiveTickerGrid()

            // Main List Grid
            when (val state = uiState) {
                is LobbyUiState.Loading -> {
                    LobbyShimmerState()
                }
                is LobbyUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header Search & Filters
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Dynamic Promos Slider
                                LobbyPromoSlider(
                                    onDepositClick = {
                                        triggerVibration()
                                        showDepositDialog = true
                                    }
                                )

                                // Search Field
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.searchGames(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("lobby_search_input"),
                                    placeholder = { Text("Search 100+ Esports Games...", color = GamingTextMuted) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = "Search",
                                            tint = GamingTextMuted
                                        )
                                    },
                                    trailingIcon = if (searchQuery.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { viewModel.searchGames("") }) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Clear Search",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = GamingDeepSurface,
                                        unfocusedContainerColor = GamingDeepSurface,
                                        focusedBorderColor = GamingNeonCyan,
                                        unfocusedBorderColor = GamingBorderSlate,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true
                                )

                                // Category Row
                                Text(
                                    text = "Game Categories",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Horizontal Scroll View for Categories
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    CategoryPill(
                                        title = "All Arena",
                                        isSelected = selectedCategory == null,
                                        onClick = {
                                            triggerVibration()
                                            viewModel.selectCategory(null)
                                        },
                                        icon = Icons.Rounded.SportsEsports
                                    )
                                }
                                items(GameCategory.values()) { category ->
                                    val icon = when (category) {
                                        GameCategory.FANTASY -> Icons.Rounded.SportsCricket
                                        GameCategory.CARDS -> Icons.Rounded.Casino
                                        GameCategory.CASINO -> Icons.Rounded.Style
                                        GameCategory.CASUAL -> Icons.Rounded.SportsEsports
                                        GameCategory.MULTIPLIER -> Icons.AutoMirrored.Rounded.TrendingUp
                                    }
                                    CategoryPill(
                                        title = category.displayName,
                                        isSelected = selectedCategory == category,
                                        onClick = {
                                            triggerVibration()
                                            viewModel.selectCategory(category)
                                        },
                                        icon = icon
                                    )
                                }
                            }
                        }

                        // Hot & Featured Games Section
                        if (state.filteredGames.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Rounded.Whatshot, contentDescription = "Trending", tint = Color.Red)
                                        Text(
                                            text = "Featured Battles",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "${state.filteredGames.size} Games Available",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GamingTextMuted
                                    )
                                }
                            }

                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(state.filteredGames, key = { it.id }) { game ->
                                        FeaturedGameCard(
                                            game = game,
                                            onPlayClick = {
                                                triggerVibration()
                                                if (game.id == "g_cricket") {
                                                    onNavigateToFantasyCricket()
                                                } else {
                                                    onNavigateToGame(game.id)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp, horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SentimentDissatisfied,
                                        contentDescription = "Not Found",
                                        tint = GamingTextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "No Games matched your filter.",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Try switching game category or modifying search keyword.",
                                        color = GamingTextMuted,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Live Tournaments Section
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.Stream, contentDescription = "Live", tint = GamingNeonCyan)
                                    Text(
                                        text = "Live Contests & Tournaments",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Join active rooms. Win pools instantaneously.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GamingTextMuted
                                )
                            }
                        }

                        items(state.liveTournaments, key = { it.id }) { contest ->
                            TournamentRowCard(
                                contest = contest,
                                onRegisterClick = {
                                    triggerVibration()
                                    showJoinConfirmDialog = contest
                                }
                            )
                        }
                    }
                }
                is LobbyUiState.Error -> {
                    LobbyErrorBox(
                        message = (uiState as LobbyUiState.Error).message,
                        onRetry = { viewModel.loadLobbyData() }
                    )
                }
            }
        }
    }

    // Modal Add Cash Dialog
    if (showDepositDialog) {
        QuickDepositDialog(
            onDismiss = { showDepositDialog = false },
            onDepositAction = { amount ->
                showDepositDialog = false
                viewModel.addCash(amount) { result ->
                    scope.launch {
                        if (result.isSuccess) {
                            triggerVibration()
                            snackbarHostState.showSnackbar(
                                message = "Successfully added ₹$amount cash instantly!",
                                withDismissAction = true
                            )
                        } else {
                            snackbarHostState.showSnackbar(
                                message = result.exceptionOrNull()?.message ?: "Deposit failed.",
                                withDismissAction = true
                            )
                        }
                    }
                }
            }
        )
    }

    // Join Tournament Confirmation Dialog
    showJoinConfirmDialog?.let { contest ->
        Dialog(onDismissRequest = { showJoinConfirmDialog = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("confirm_join_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = GamingDeepSurface,
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = "Secure Join",
                        tint = GamingNeonCyan,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Confirm Entry Ticket",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Are you sure you want to register for ${contest.title}? The entry fee of ₹${contest.entryFee} will be deducted from your cash wallet.",
                        color = GamingTextMuted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Details Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GamingBorderSlate)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Entry Ticket Fee:", color = GamingTextMuted, fontSize = 13.sp)
                                Text(String.format("₹%.2f", contest.entryFee), color = GamingGoldAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Guaranteed Pool:", color = GamingTextMuted, fontSize = 13.sp)
                                Text(String.format("₹%.2f", contest.prizePool), color = GamingBrightGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showJoinConfirmDialog = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val matchToJoin = showJoinConfirmDialog
                                showJoinConfirmDialog = null
                                if (matchToJoin != null) {
                                    viewModel.joinMatch(matchToJoin) { result ->
                                        scope.launch {
                                            if (result.isSuccess) {
                                                triggerVibration()
                                                snackbarHostState.showSnackbar(
                                                    message = "Ticket confirmed! Registration complete.",
                                                    withDismissAction = true
                                                )
                                                // Take to simulated matchmaking directly!
                                                onNavigateToGame(matchToJoin.id)
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    message = result.exceptionOrNull()?.message ?: "Join failed.",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("dialog_confirm_join_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamingGoldAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Join Battle", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveTickerGrid() {
    Surface(
        color = GamingDeepSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(color = GamingBrightGreen)
                }
                Text(
                    text = "Live Arenas",
                    color = GamingBrightGreen,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "⚡ Rooms fill fast • Join before slots close",
                color = GamingTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GamingGoldAccent else GamingDeepSurface,
        border = if (isSelected) null else BorderStroke(1.dp, GamingBorderSlate),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else GamingNeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LobbyPromoSlider(onDepositClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scaleAnimate by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        color = GamingDeepSurface,
        border = BorderStroke(1.dp, GamingBorderSlate)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val brush = Brush.radialGradient(
                        colors = listOf(GamingVibrantIndigo.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(size.width, size.height / 2),
                        radius = size.width * 0.8f
                    )
                    drawRect(brush)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Surface(
                        color = Color(0xFFFF4081),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "QUICK TOP-UP",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Instant Cash Top-Up",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "Add ₹50+ and your balance is available instantly to play any arena.",
                        color = GamingTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(0.7f)
                ) {
                    Button(
                        onClick = onDepositClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GamingNeonCyan),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("promo_deposit_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Add Cash",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "100% Instant & Safe",
                        color = GamingGoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedGameCard(game: GameItem, onPlayClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable(onClick = onPlayClick),
        color = GamingDeepSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GamingBorderSlate)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient Canvas graphic representing background slots/graphics
            Canvas(modifier = Modifier.matchParentSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.4f)
                    quadraticTo(size.width * 0.5f, size.height * 0.3f, size.width, size.height * 0.5f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = GamingBorderSlate.copy(alpha = 0.4f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GamingNeonCyan.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = GamingNeonCyan,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = String.format("%.1fK", game.playersCount / 1000f),
                                color = GamingNeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (game.isHot) {
                        Surface(
                            color = Color(0xFFFF5252),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "HOT",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Center visual slot representation
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(GamingVibrantIndigo, GamingNeonCyan)
                                ),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(game.category) {
                            GameCategory.FANTASY -> Icons.Rounded.SportsCricket
                            GameCategory.CARDS -> Icons.Rounded.Casino
                            GameCategory.CASINO -> Icons.Rounded.Style
                            GameCategory.CASUAL -> Icons.Rounded.SportsEsports
                            GameCategory.MULTIPLIER -> Icons.AutoMirrored.Rounded.TrendingUp
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = GamingGoldAccent
                    )
                }

                // Titles & Actions
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = game.maxMultiplier,
                        color = GamingNeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TournamentRowCard(contest: TournamentMatch, onRegisterClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = GamingDeepSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GamingBorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = GamingBorderSlate,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                contest.gameTitle.uppercase(),
                                color = GamingNeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = contest.startTime,
                            color = GamingVibrantIndigo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contest.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(0.8f)
                ) {
                    Text("Guaranteed Pool", color = GamingTextMuted, fontSize = 11.sp)
                    Text(
                        text = String.format("₹%.2f", contest.prizePool),
                        color = GamingBrightGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Fill meter progress slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { contest.fillPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = GamingGoldAccent,
                    trackColor = GamingBorderSlate,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${contest.slotsLeft} seats left",
                        color = GamingTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Total slots: ${contest.totalSlots}",
                        color = GamingTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Divider(color = GamingBorderSlate.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = "Trusted Platform",
                        tint = GamingBrightGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("Instant Matchmaking", color = GamingBrightGreen, fontSize = 10.sp)
                }

                Button(
                    onClick = onRegisterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingGoldAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("register_button_${contest.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "JOIN FOR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("₹%.2f", contest.entryFee),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickDepositDialog(
    onDismiss: () -> Unit,
    onDepositAction: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("50") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = GamingDeepSurface,
            border = BorderStroke(1.dp, GamingBorderSlate)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Cash Instantly",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Select preset or insert custom amount. Funds are added to your secure wallet.",
                    color = GamingTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(20.0, 50.0, 100.0, 250.0).forEach { amt ->
                        Surface(
                            onClick = {
                                amountText = amt.toInt().toString()
                                errorMsg = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (amountText == amt.toInt().toString()) GamingGoldAccent else MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, GamingBorderSlate)
                        ) {
                            Text(
                                text = String.format("₹%.0f", amt),
                                color = if (amountText == amt.toInt().toString()) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMsg = null
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_cash_input"),
                    prefix = { Text("₹", color = Color.White) },
                    isError = errorMsg != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GamingNeonCyan,
                        unfocusedBorderColor = GamingBorderSlate
                    ),
                    singleLine = true
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val valDouble = amountText.toDoubleOrNull()
                            if (valDouble == null || valDouble <= 0) {
                                errorMsg = "Please insert a positive cash amount."
                            } else {
                                onDepositAction(valDouble)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("dialog_add_cash_submit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GamingGoldAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Pay Now", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// Full screen loaders
@Composable
fun LobbyShimmerState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Shimmer slider
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            color = GamingDeepSurface,
            shape = RoundedCornerShape(16.dp)
        ) { Box(Modifier.fillMaxSize()) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    color = GamingDeepSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {}
            }
        }

        repeat(2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = GamingDeepSurface,
                shape = RoundedCornerShape(12.dp)
            ) {}
        }
    }
}

@Composable
fun LobbyErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.WifiOff,
            contentDescription = "Sync Error",
            tint = Color.Red,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = "Lobby Sync Timeout",
            color = Color.White,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = message,
            color = GamingTextMuted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = GamingNeonCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Retry Sync Now", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

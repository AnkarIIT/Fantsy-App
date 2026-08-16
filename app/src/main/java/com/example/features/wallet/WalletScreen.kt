package com.example.features.wallet

import android.os.Build
import android.os.Vibrator
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.KYCStatus
import com.example.data.models.TransactionType
import com.example.data.models.WalletTransaction
import com.example.features.lobby.LobbyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: LobbyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val scope = rememberCoroutineScope()

    val wallet by viewModel.walletBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val kycStatus by viewModel.kycStatus.collectAsStateWithLifecycle()

    var showActionSheet by remember { mutableStateOf<String?>(null) } // "DEPOSIT" or "WITHDRAW"
    val snackbarHostState = remember { SnackbarHostState() }

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
                    Text(
                        "ROYALE SECURED WALLET",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        triggerVibration()
                        onNavigateBack()
                    }) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Secure Indicator Banner
            Surface(
                color = GamingBrightGreen.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "Secured",
                        tint = GamingBrightGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "All transactions are processed over an encrypted channel.",
                        color = GamingBrightGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Total Balance Card Overview
            val dep = wallet?.depositBalance ?: 0.0
            val win = wallet?.winningsBalance ?: 0.0
            val bon = wallet?.bonusBalance ?: 0.0
            val total = dep + win + bon

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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TOTAL PLAYABLE BALANCE", color = GamingTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format("₹%.2f", total),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("INR", color = GamingGoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        }
                    }

                    Divider(color = GamingBorderSlate)

                    // Wallet Splits Table
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WalletCategoryItem(
                            title = "Deposited Cash",
                            amount = dep,
                            description = "Used to join any game room",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(56.dp)
                                .background(GamingBorderSlate)
                        )
                        WalletCategoryItem(
                            title = "Withdrawable Winnings",
                            amount = win,
                            description = "Redeemable to your Bank",
                            amountColor = GamingBrightGreen,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(56.dp)
                                .background(GamingBorderSlate)
                        )
                        WalletCategoryItem(
                            title = "Promo Bonuses",
                            amount = bon,
                            description = "10% playable discounts",
                            amountColor = GamingVibrantIndigo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Action buttons (Withdraw, Deposit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                triggerVibration()
                                showActionSheet = "WITHDRAW"
                            },
                            modifier = Modifier.weight(1f).testTag("withdraw_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GamingBorderSlate),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Withdraw", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                triggerVibration()
                                showActionSheet = "DEPOSIT"
                            },
                            modifier = Modifier.weight(1f).testTag("deposit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamingGoldAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Add Cash", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // Trust badging
            WalletSecurityBadging()

            // Transactions Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Transaction History Ledger",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Comprehensive logs of stakes, tournament payouts & deposits",
                    style = MaterialTheme.typography.bodySmall,
                    color = GamingTextMuted
                )
            }

            // Ledger Cards (Replacing lazy column inside column scroll with explicit mapping to avoid nest scrolls)
            if (transactions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    transactions.forEach { tx ->
                        TransactionRowItem(tx = tx)
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = GamingDeepSurface,
                    border = BorderStroke(1.dp, GamingBorderSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Payments, contentDescription = null, tint = GamingTextMuted, modifier = Modifier.size(36.dp))
                        Text("No transaction history detected.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Complete your first UPI deposit or register for an arena to see data here.", color = GamingTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    // Modal forms
    showActionSheet?.let { action ->
        if (action == "DEPOSIT") {
            AddCashSheet(
                onDismiss = { showActionSheet = null },
                onAddConfirm = { amount ->
                    showActionSheet = null
                    viewModel.addCash(amount) { res ->
                        scope.launch {
                            if (res.isSuccess) {
                                triggerVibration()
                                snackbarHostState.showSnackbar("Deposited ₹$amount cash successfully!", withDismissAction = true)
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Deposit error", withDismissAction = true)
                            }
                        }
                    }
                }
            )
        } else if (action == "WITHDRAW") {
            WithdrawCashSheet(
                withdrawableLimit = wallet?.winningsBalance ?: 0.0,
                onDismiss = { showActionSheet = null },
                onWithdrawConfirm = { amount ->
                    if (kycStatus != KYCStatus.VERIFIED) {
                        showActionSheet = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (kycStatus == KYCStatus.PENDING) {
                                    "KYC verification is under review. Withdrawals unlock once verified."
                                } else {
                                    "Complete KYC verification before withdrawing (Profile > KYC)."
                                },
                                withDismissAction = true
                            )
                        }
                    } else {
                        showActionSheet = null
                        viewModel.withdrawCash(amount) { res ->
                            scope.launch {
                                if (res.isSuccess) {
                                    triggerVibration()
                                    snackbarHostState.showSnackbar("Withdrawal of ₹$amount approved & initiated to bank!", withDismissAction = true)
                                } else {
                                    snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Withdrawal error", withDismissAction = true)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun WalletCategoryItem(
    title: String,
    amount: Double,
    description: String,
    modifier: Modifier = Modifier,
    amountColor: Color = Color.White
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = GamingTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Text(
            text = String.format("₹%.2f", amount),
            color = amountColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(description, color = GamingTextMuted, fontSize = 8.sp, lineHeight = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun WalletSecurityBadging() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) { item ->
            val icon = when (item) {
                0 -> Icons.Rounded.TrendingFlat
                1 -> Icons.Rounded.VerifiedUser
                else -> Icons.Rounded.ElectricBolt
            }
            val title = when (item) {
                0 -> "Instant Withdrawals"
                1 -> "Anti-Fraud Vault"
                else -> "SSL Secured"
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = GamingDeepSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GamingBorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (item == 0) Icons.Rounded.CheckCircle else icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GamingGoldAccent
                    )
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(tx: WalletTransaction) {
    val fmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateStr = fmt.format(Date(tx.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth().testTag("transaction_item_${tx.id}"),
        color = GamingDeepSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GamingBorderSlate)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.2f)
            ) {
                // Circle type icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = when (tx.type) {
                                TransactionType.DEPOSIT -> GamingBrightGreen.copy(alpha = 0.12f)
                                TransactionType.WITHDRAW -> Color(0xFFFF4D4D).copy(alpha = 0.12f)
                                TransactionType.JOIN_FEE -> GamingVibrantIndigo.copy(alpha = 0.12f)
                                TransactionType.WINNING_PAYOUT -> GamingGoldAccent.copy(alpha = 0.12f)
                                TransactionType.REFERRAL_BONUS -> GamingBrightGreen.copy(alpha = 0.12f)
                                TransactionType.CASHBACK -> GamingNeonCyan.copy(alpha = 0.12f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (tx.type) {
                            TransactionType.DEPOSIT -> Icons.Rounded.ArrowDownward
                            TransactionType.WITHDRAW -> Icons.Rounded.ArrowUpward
                            TransactionType.JOIN_FEE -> Icons.Rounded.SportsEsports
                            TransactionType.WINNING_PAYOUT -> Icons.Rounded.EmojiEvents
                            TransactionType.REFERRAL_BONUS -> Icons.Rounded.GroupAdd
                            TransactionType.CASHBACK -> Icons.Rounded.CurrencyExchange
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when (tx.type) {
                            TransactionType.DEPOSIT -> GamingBrightGreen
                            TransactionType.WITHDRAW -> Color(0xFFFF4D4D)
                            TransactionType.JOIN_FEE -> GamingVibrantIndigo
                            TransactionType.WINNING_PAYOUT -> GamingGoldAccent
                            TransactionType.REFERRAL_BONUS -> GamingBrightGreen
                            TransactionType.CASHBACK -> GamingNeonCyan
                        }
                    )
                }

                Column {
                    Text(tx.description, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (tx.type == TransactionType.DEPOSIT || tx.type == TransactionType.WINNING_PAYOUT) GamingBrightGreen.copy(alpha = 0.1f) else GamingBorderSlate
                        ) {
                            Text(
                                text = tx.type.name.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == TransactionType.DEPOSIT || tx.type == TransactionType.WINNING_PAYOUT) GamingBrightGreen else GamingTextMuted
                            )
                        }
                        Text(dateStr, color = GamingTextMuted, fontSize = 10.sp)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.8f)
            ) {
                Text(
                    text = when (tx.type) {
                        TransactionType.DEPOSIT, TransactionType.WINNING_PAYOUT -> String.format("+₹%.2f", tx.amount)
                        else -> String.format("-₹%.2f", tx.amount)
                    },
                    color = when (tx.type) {
                        TransactionType.DEPOSIT, TransactionType.WINNING_PAYOUT -> GamingBrightGreen
                        else -> Color.White
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = tx.status,
                    color = if (tx.status == "SUCCESS") GamingBrightGreen else Color.DarkGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddCashSheet(
    onDismiss: () -> Unit,
    onAddConfirm: (Double) -> Unit
) {
    var amountVal by remember { mutableStateOf("100") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Secured Cash Deposit", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Insert balance payload to deposit. Funds will match instantly in your playable wallet.", color = GamingTextMuted, fontSize = 12.sp)

                OutlinedTextField(
                    value = amountVal,
                    onValueChange = {
                        amountVal = it
                        errorMsg = null
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_cash_dialog_input"),
                    prefix = { Text("₹", color = Color.White) },
                    isError = errorMsg != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GamingNeonCyan,
                        unfocusedBorderColor = GamingBorderSlate
                    )
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doubleVal = amountVal.toDoubleOrNull()
                    if (doubleVal == null || doubleVal <= 0) {
                        errorMsg = "Insert a valid balance positive sum."
                    } else {
                        onAddConfirm(doubleVal)
                    }
                },
                modifier = Modifier.testTag("dialog_add_cash_confirm"),
                colors = ButtonDefaults.buttonColors(containerColor = GamingGoldAccent, contentColor = Color.Black)
            ) {
                Text("Confirm Deposit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = GamingDeepSurface
    )
}

@Composable
fun WithdrawCashSheet(
    withdrawableLimit: Double,
    onDismiss: () -> Unit,
    onWithdrawConfirm: (Double) -> Unit
) {
    var amountVal by remember { mutableStateOf("50") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Secured Redemptions", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = String.format("Maximum Withdraw Limit: ₹%.2f Winnings Cash", withdrawableLimit),
                    color = GamingBrightGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Withdrawal requests are processed to your registered bank account details.",
                    color = GamingTextMuted,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = amountVal,
                    onValueChange = {
                        amountVal = it
                        errorMsg = null
                    },
                    modifier = Modifier.fillMaxWidth().testTag("withdraw_dialog_input"),
                    prefix = { Text("₹", color = Color.White) },
                    isError = errorMsg != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GamingNeonCyan,
                        unfocusedBorderColor = GamingBorderSlate
                    )
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doubleVal = amountVal.toDoubleOrNull()
                    if (doubleVal == null || doubleVal <= 0) {
                        errorMsg = "Insert a valid redemption positive sum."
                    } else if (doubleVal > withdrawableLimit) {
                        errorMsg = String.format("Withdraw lock! Current winnings limit is ₹%.2f.", withdrawableLimit)
                    } else {
                        onWithdrawConfirm(doubleVal)
                    }
                },
                modifier = Modifier.testTag("dialog_withdraw_confirm"),
                colors = ButtonDefaults.buttonColors(containerColor = GamingGoldAccent, contentColor = Color.Black)
            ) {
                Text("Initiate Bank Transfer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = GamingDeepSurface
    )
}

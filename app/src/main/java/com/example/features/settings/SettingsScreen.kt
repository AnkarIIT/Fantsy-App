package com.example.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.models.AppSettings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
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
            // Notifications Settings
            SettingsSection(
                title = "Preferences"
            ) {
                SettingsToggleItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive match and contest updates",
                    checked = appSettings.notificationsEnabled,
                    onCheckedChange = {
                        onSettingsChanged(appSettings.copy(notificationsEnabled = it))
                    }
                )
                SettingsToggleItem(
                    icon = Icons.Rounded.VolumeUp,
                    title = "Sound Effects",
                    subtitle = "Play sounds during gameplay",
                    checked = appSettings.soundEnabled,
                    onCheckedChange = {
                        onSettingsChanged(appSettings.copy(soundEnabled = it))
                    }
                )
                SettingsToggleItem(
                    icon = Icons.Rounded.Vibration,
                    title = "Vibration",
                    subtitle = "Haptic feedback on actions",
                    checked = appSettings.vibrationEnabled,
                    onCheckedChange = {
                        onSettingsChanged(appSettings.copy(vibrationEnabled = it))
                    }
                )
            }

            // App Settings
            SettingsSection(
                title = "App Settings"
            ) {
                SettingsToggleItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = appSettings.darkMode,
                    onCheckedChange = {
                        onSettingsChanged(appSettings.copy(darkMode = it))
                    }
                )
                var langMessage by remember { mutableStateOf<String?>(null) }
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = "Language",
                    subtitle = "English (US)",
                    onClick = { langMessage = "Language changed to English (demo)" }
                )
                langMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(1400)
                        langMessage = null
                    }
                    Text(msg, color = GamingBrightGreen, fontSize = 11.sp, modifier = Modifier.padding(start = 16.dp))
                }
            }

            // Account Settings
            SettingsSection(
                title = "Account"
            ) {
                SettingsItem(
                    icon = Icons.Rounded.Lock,
                    title = "Change Password",
                    onClick = { /* demo: password change dialog would appear here */ }
                )
                var logoutMessage by remember { mutableStateOf<String?>(null) }
                SettingsItem(
                    icon = Icons.Rounded.Logout,
                    title = "Logout",
                    onClick = { logoutMessage = "Logged out (demo). Returning to lobby..." },
                    isDanger = true
                )
                logoutMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(1600)
                        logoutMessage = null
                    }
                    Text(msg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                }
            }

            // About Section
            SettingsSection(
                title = "About"
            ) {
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = "Terms & Conditions",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = GamingTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GamingDeepSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GamingBorderSlate)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
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
                    tint = if (isDanger) Color.Red else GamingNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isDanger) Color.Red else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            color = GamingTextMuted,
                            fontSize = 12.sp
                        )
                    }
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
    Divider(color = GamingBorderSlate, thickness = 0.5.dp)
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        onClick = { onCheckedChange(!checked) }
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GamingNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = subtitle,
                        color = GamingTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = GamingNeonCyan,
                    uncheckedThumbColor = GamingTextMuted,
                    uncheckedTrackColor = GamingBorderSlate
                )
            )
        }
    }
    Divider(color = GamingBorderSlate, thickness = 0.5.dp)
}

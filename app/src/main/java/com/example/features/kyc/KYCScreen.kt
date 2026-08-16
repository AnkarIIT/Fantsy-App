package com.example.features.kyc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.KYCStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCScreen(
    kycStatus: KYCStatus,
    onNavigateBack: () -> Unit,
    onSubmitKyc: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Simulated document upload state (for demo)
    var panUploaded by remember { mutableStateOf(false) }
    var aadhaarFrontUploaded by remember { mutableStateOf(false) }
    var aadhaarBackUploaded by remember { mutableStateOf(false) }
    var bankDocUploaded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "KYC VERIFICATION",
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
            // KYC Status Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = when (kycStatus) {
                    KYCStatus.VERIFIED -> GamingBrightGreen.copy(alpha = 0.1f)
                    KYCStatus.PENDING -> GamingGoldAccent.copy(alpha = 0.1f)
                    KYCStatus.REJECTED -> Color.Red.copy(alpha = 0.1f)
                    KYCStatus.NOT_SUBMITTED -> GamingDeepSurface
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    when (kycStatus) {
                        KYCStatus.VERIFIED -> GamingBrightGreen
                        KYCStatus.PENDING -> GamingGoldAccent
                        KYCStatus.REJECTED -> Color.Red
                        KYCStatus.NOT_SUBMITTED -> GamingBorderSlate
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (kycStatus) {
                            KYCStatus.VERIFIED -> Icons.Rounded.Verified
                            KYCStatus.PENDING -> Icons.Rounded.HourglassEmpty
                            KYCStatus.REJECTED -> Icons.Rounded.Cancel
                            KYCStatus.NOT_SUBMITTED -> Icons.Rounded.Info
                        },
                        contentDescription = null,
                        tint = when (kycStatus) {
                            KYCStatus.VERIFIED -> GamingBrightGreen
                            KYCStatus.PENDING -> GamingGoldAccent
                            KYCStatus.REJECTED -> Color.Red
                            KYCStatus.NOT_SUBMITTED -> GamingTextMuted
                        },
                        modifier = Modifier.size(32.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (kycStatus) {
                                KYCStatus.VERIFIED -> "KYC Verified!"
                                KYCStatus.PENDING -> "KYC Under Review"
                                KYCStatus.REJECTED -> "KYC Rejected"
                                KYCStatus.NOT_SUBMITTED -> "Complete Your KYC"
                            },
                            color = when (kycStatus) {
                                KYCStatus.VERIFIED -> GamingBrightGreen
                                KYCStatus.PENDING -> GamingGoldAccent
                                KYCStatus.REJECTED -> Color.Red
                                KYCStatus.NOT_SUBMITTED -> Color.White
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = when (kycStatus) {
                                KYCStatus.VERIFIED -> "You can now withdraw your winnings"
                                KYCStatus.PENDING -> "We are verifying your documents"
                                KYCStatus.REJECTED -> "Please resubmit your documents"
                                KYCStatus.NOT_SUBMITTED -> "Complete KYC to withdraw winnings"
                            },
                            color = GamingTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // KYC Form (Only show if not verified or pending)
            if (kycStatus != KYCStatus.VERIFIED) {
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Personal Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        // Full Name
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Full Name", color = GamingTextMuted, fontSize = 12.sp)
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter your full name", color = GamingTextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = GamingTextMuted)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = GamingNeonCyan,
                                    unfocusedBorderColor = GamingBorderSlate,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )
                        }

                        // PAN Number
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("PAN Number", color = GamingTextMuted, fontSize = 12.sp)
                            OutlinedTextField(
                                value = panNumber,
                                onValueChange = { panNumber = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("ABCDE1234F", color = GamingTextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Badge, contentDescription = null, tint = GamingTextMuted)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = GamingNeonCyan,
                                    unfocusedBorderColor = GamingBorderSlate,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )
                        }

                        // Aadhaar Number
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Aadhaar Number", color = GamingTextMuted, fontSize = 12.sp)
                            OutlinedTextField(
                                value = aadhaarNumber,
                                onValueChange = { aadhaarNumber = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("1234 5678 9012", color = GamingTextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = GamingTextMuted)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = GamingNeonCyan,
                                    unfocusedBorderColor = GamingBorderSlate,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )
                        }

                        // Address
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Address", color = GamingTextMuted, fontSize = 12.sp)
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter your complete address", color = GamingTextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = GamingTextMuted)
                                },
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = GamingNeonCyan,
                                    unfocusedBorderColor = GamingBorderSlate,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // Document Upload
                        Text(
                            text = "Document Upload",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        DocumentUploadItem(
                            title = "PAN Card",
                            subtitle = "Upload front side of PAN card",
                            isUploaded = panUploaded,
                            onUpload = { panUploaded = !panUploaded }
                        )
                        DocumentUploadItem(
                            title = "Aadhaar Card (Front)",
                            subtitle = "Upload front side of Aadhaar",
                            isUploaded = aadhaarFrontUploaded,
                            onUpload = { aadhaarFrontUploaded = !aadhaarFrontUploaded }
                        )
                        DocumentUploadItem(
                            title = "Aadhaar Card (Back)",
                            subtitle = "Upload back side of Aadhaar",
                            isUploaded = aadhaarBackUploaded,
                            onUpload = { aadhaarBackUploaded = !aadhaarBackUploaded }
                        )
                        DocumentUploadItem(
                            title = "Bank Passbook / Cancel Cheque",
                            subtitle = "For withdrawal verification",
                            isUploaded = bankDocUploaded,
                            onUpload = { bankDocUploaded = !bankDocUploaded }
                        )

                        val canSubmit = panUploaded && aadhaarFrontUploaded && aadhaarBackUploaded
                        Button(
                            onClick = {
                                onSubmitKyc()
                                // User can navigate back; status will update live in profile
                                onNavigateBack()
                            },
                            enabled = canSubmit,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamingGoldAccent,
                                contentColor = Color.Black,
                                disabledContainerColor = GamingBorderSlate
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (canSubmit) "Submit for Verification" else "Upload required documents", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DocumentUploadItem(
    title: String,
    subtitle: String,
    isUploaded: Boolean = false,
    onUpload: () -> Unit
) {
    Surface(
        onClick = onUpload,
        modifier = Modifier.fillMaxWidth(),
        color = if (isUploaded) GamingBrightGreen.copy(alpha = 0.1f) else GamingBorderSlate.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isUploaded) GamingBrightGreen else GamingBorderSlate)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isUploaded) GamingBrightGreen.copy(alpha = 0.2f) else GamingNeonCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (isUploaded) Icons.Rounded.CheckCircle else Icons.Rounded.CloudUpload,
                    contentDescription = null,
                    tint = if (isUploaded) GamingBrightGreen else GamingNeonCyan,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (isUploaded) "Uploaded ✓" else subtitle,
                    color = if (isUploaded) GamingBrightGreen else GamingTextMuted,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = if (isUploaded) Icons.Rounded.Check else Icons.Rounded.AddCircle,
                contentDescription = null,
                tint = if (isUploaded) GamingBrightGreen else GamingNeonCyan,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

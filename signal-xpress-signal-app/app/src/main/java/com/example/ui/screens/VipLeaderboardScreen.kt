package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.VipMemberEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardHeaderBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun VipLeaderboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vipMembers by viewModel.filteredVipMembers.collectAsState()
    val selectedPeriod by viewModel.vipPeriodFilter.collectAsState()
    val searchQuery by viewModel.vipSearchQuery.collectAsState()
    val vipWebUrl by viewModel.vipWebUrl.collectAsState()
    val isSyncingVip by viewModel.isSyncingVip.collectAsState()
    val lastVipSyncTime by viewModel.lastVipSyncTime.collectAsState()

    var showAdminConfigDialog by remember { mutableStateOf(false) }
    var selectedMemberForDetail by remember { mutableStateOf<VipMemberEntity?>(null) }
    var isSearchOpen by remember { mutableStateOf(false) }

    // Auto-sync on screen load if a Web URL is configured
    LaunchedEffect(vipWebUrl) {
        if (vipWebUrl.isNotBlank()) {
            viewModel.syncVipLeaderboardFromWeb()
        }
    }

    val totalLots = remember(vipMembers) {
        vipMembers.sumOf { it.lots }
    }
    val df = remember { DecimalFormat("#,##0.00") }

    // Rotation animation for sync icon when syncing
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spin"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Main Trophy Header exact to screenshot: "🏆 Top 10 — Lots අනුව"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Top 10 — Lots අනුව",
                        color = Color(0xFF94A3B8),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search toggle
                    IconButton(
                        onClick = { isSearchOpen = !isSearchOpen },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchOpen) PrimarySky else Color(0xFF64748B),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Live Web Sync Button
                    IconButton(
                        onClick = {
                            if (vipWebUrl.isBlank()) {
                                showAdminConfigDialog = true
                            } else {
                                viewModel.syncVipLeaderboardFromWeb { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync from Web",
                            tint = if (isSyncingVip) PrimarySky else Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(20.dp)
                                .then(if (isSyncingVip) Modifier.rotate(spinAngle) else Modifier)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Admin Web Link Configuration Gate
                    IconButton(
                        onClick = { showAdminConfigDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Web Link Settings",
                            tint = if (vipWebUrl.isNotBlank()) PrimarySky else AccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Search Field
            AnimatedVisibility(visible = isSearchOpen) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setVipSearchQuery(it) },
                    placeholder = { Text("Search by name, ID (e.g. SX1043)...", fontSize = 12.sp, color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimarySky,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = CardHeaderBackground,
                        unfocusedContainerColor = CardHeaderBackground,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Live Web Sync Status Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardHeaderBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSyncingVip) {
                            CircularProgressIndicator(
                                color = PrimarySky,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (vipWebUrl.isNotBlank()) Icons.Default.CloudDone else Icons.Default.Link,
                                contentDescription = null,
                                tint = if (vipWebUrl.isNotBlank()) AccentEmerald else AccentAmber,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (vipWebUrl.isNotBlank()) "Live Web Admin Sync • Read Only" else "Web Admin Link Not Set",
                                color = if (vipWebUrl.isNotBlank()) Color(0xFFE2E8F0) else AccentAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (vipWebUrl.isNotBlank()) lastVipSyncTime else "Tap ⚙️ to link your web backend",
                                color = Color(0xFF64748B),
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    if (vipWebUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimarySky.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                viewModel.syncVipLeaderboardFromWeb { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(
                                text = if (isSyncingVip) "SYNCING..." else "SYNC NOW",
                                color = PrimarySky,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentAmber.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { showAdminConfigDialog = true }
                        ) {
                            Text(
                                text = "LINK WEB",
                                color = AccentAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Period Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val periods = listOf(
                    "MONTHLY" to "🔥 This Month",
                    "ALL_TIME" to "🌐 All Time",
                    "WEEKLY" to "⚡ This Week"
                )

                periods.forEach { (key, label) ->
                    val isSelected = selectedPeriod == key
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PrimarySky.copy(alpha = 0.2f) else CardHeaderBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimarySky else BorderColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setVipPeriodFilter(key) }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) PrimarySky else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Top Volume Banner Strip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardHeaderBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL VIP VOLUME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${df.format(totalLots)} Lots",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimarySky,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22F59E0B),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentAmber)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VIP Lounge",
                                    color = AccentAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Main Top 10 List exactly styled to screenshot (100% Read Only for normal users)
            if (vipMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No VIP members recorded yet.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        if (vipWebUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimarySky,
                                modifier = Modifier.clickable {
                                    viewModel.syncVipLeaderboardFromWeb { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(
                                    text = "Fetch from Web",
                                    color = DarkBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(vipMembers.take(10)) { index, member ->
                        val displayRank = index + 1
                        VipLeaderboardRow(
                            member = member,
                            displayRank = displayRank,
                            onClick = { selectedMemberForDetail = member }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }

    // Admin Web Link & API Source Configurator Dialog
    if (showAdminConfigDialog) {
        AdminWebLinkConfigDialog(
            currentUrl = vipWebUrl,
            viewModel = viewModel,
            onDismiss = { showAdminConfigDialog = false }
        )
    }

    // Detail Modal for VIP Trader (Read Only for regular users)
    selectedMemberForDetail?.let { member ->
        VipTraderDetailDialog(
            member = member,
            onDismiss = { selectedMemberForDetail = null }
        )
    }
}

@Composable
fun VipLeaderboardRow(
    member: VipMemberEntity,
    displayRank: Int,
    onClick: () -> Unit
) {
    val df = remember { DecimalFormat("#,##0.00") }
    val animatedProgress by animateFloatAsState(
        targetValue = member.progressFraction.coerceIn(0.04f, 0.95f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    val rowAccentColor = Color(member.accentHex)

    // Specific card styling per rank position to match user screenshot
    val (cardBg, borderColor) = when (displayRank) {
        1 -> Color(0xFF14110A) to Color(0xFF4A3814) // Rank 1: Gold themed dark container
        2 -> Color(0xFF0F1521) to Color(0xFF26354D) // Rank 2: Silver/Slate themed container
        3 -> Color(0xFF17110C) to Color(0xFF3F2716) // Rank 3: Bronze themed container
        else -> Color(0xFF0A111F) to Color(0xFF162338) // Ranks 4-10: Dark navy container
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Icon / Number
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                when (displayRank) {
                    1 -> {
                        // Gold Medal 🥇
                        Text(text = "🥇", fontSize = 20.sp)
                    }
                    2 -> {
                        // Silver Medal 🥈
                        Text(text = "🥈", fontSize = 20.sp)
                    }
                    3 -> {
                        // Bronze Medal 🥉
                        Text(text = "🥉", fontSize = 20.sp)
                    }
                    else -> {
                        // #4 to #10
                        Text(
                            text = "#$displayRank",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Name + Progress Underline Bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = member.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Underline progress bar matching screenshot style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                ) {
                    // Active accent bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(rowAccentColor)
                    )
                }
            }

            // Member ID Pill Badge (e.g. SX1043 or —)
            Box(
                modifier = Modifier.padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1D4ED8), // Vivid Royal Blue pill matching screenshot
                    modifier = Modifier.height(26.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(
                            horizontal = if (member.memberId == "—") 10.dp else 9.dp,
                            vertical = 4.dp
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.memberId,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            // Lots Value & "lots" subtitle on the right
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(62.dp)
            ) {
                Text(
                    text = df.format(member.lots),
                    color = rowAccentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "lots",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AdminWebLinkConfigDialog(
    currentUrl: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isUnlocked by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var webUrlInput by remember { mutableStateOf(currentUrl) }
    var syncResultMsg by remember { mutableStateOf<String?>(null) }
    var isTestingSync by remember { mutableStateOf(false) }
    var showFormatGuide by remember { mutableStateOf(false) }

    val sampleJson = """
[
  {
    "name": "Prabath manjula",
    "memberId": "SX1043",
    "lots": 81.15,
    "broker": "Exness Raw Spread",
    "favoritePair": "XAU/USD",
    "winRate": 86.4,
    "totalTrades": 128
  },
  {
    "name": "Unknown",
    "memberId": "—",
    "lots": 36.66,
    "broker": "XM Ultra Low",
    "favoritePair": "EUR/USD",
    "winRate": 79.2,
    "totalTrades": 64
  }
]
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimarySky.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = PrimarySky,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Admin Web Link Config",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isUnlocked) {
                    // PIN Authentication Gate
                    Text(
                        text = "Enter Admin PIN to configure Web API / Sheet source link.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("Admin PIN (e.g. 7788 or 1234)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimarySky,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            errorBorderColor = AccentRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError) {
                        Text(
                            text = "Invalid Admin PIN. Use 7788 or 1234",
                            color = AccentRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimarySky,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (enteredPin == "7788" || enteredPin == "1234" || enteredPin == "9988" || enteredPin == "admin") {
                                    isUnlocked = true
                                } else {
                                    pinError = true
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Authorize & Open Settings", color = DarkBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Unlocked Web Link Settings
                    Text(
                        text = "Set the Web JSON URL where your admin updates the Top 10 rankings. When you update it on the web, the app will automatically display the latest rankings.",
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = webUrlInput,
                        onValueChange = {
                            webUrlInput = it
                            syncResultMsg = null
                        },
                        label = { Text("Web API / JSON / Sheet URL") },
                        placeholder = { Text("https://example.com/api/vip-members.json", fontSize = 12.sp) },
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimarySky,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Sync Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CardHeaderBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimarySky.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTestingSync && webUrlInput.isNotBlank()) {
                                isTestingSync = true
                                syncResultMsg = "Connecting and fetching from Web..."
                                viewModel.syncVipLeaderboardFromWeb(webUrlInput) { success, msg ->
                                    isTestingSync = false
                                    syncResultMsg = if (success) "✓ $msg" else "✕ $msg"
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isTestingSync) {
                                CircularProgressIndicator(color = PrimarySky, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = PrimarySky, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isTestingSync) "Testing Web Connection..." else "Test & Sync from Web Now",
                                color = PrimarySky,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    syncResultMsg?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.startsWith("✓")) AccentEmerald else AccentRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Expandable JSON Format Guide
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x1538BDF8),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, PrimarySky.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFormatGuide = !showFormatGuide }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PrimarySky, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showFormatGuide) "Hide JSON Guide" else "View Web JSON Format Guide",
                                    color = PrimarySky,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (showFormatGuide) "▲" else "▼",
                                color = PrimarySky,
                                fontSize = 10.sp
                            )
                        }
                    }

                    AnimatedVisibility(visible = showFormatGuide) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF030712),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sample JSON payload:", color = TextSecondary, fontSize = 10.sp)
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sampleJson))
                                            Toast.makeText(context, "Sample JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimarySky, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(
                                    text = sampleJson,
                                    color = AccentEmerald,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save URL Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimarySky,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setVipWebUrl(webUrlInput)
                                if (webUrlInput.isNotBlank()) {
                                    viewModel.syncVipLeaderboardFromWeb { success, msg ->
                                        Toast.makeText(context, "Web Link Saved! $msg", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Web Link Cleared", Toast.LENGTH_SHORT).show()
                                }
                                onDismiss()
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save Web Link",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VipTraderDetailDialog(
    member: VipMemberEntity,
    onDismiss: () -> Unit
) {
    val df = remember { DecimalFormat("#,##0.00") }
    val accentColor = Color(member.accentHex)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CardBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = member.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "VIP Member ID: ${member.memberId}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardHeaderBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("VOLUME TRADED", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${df.format(member.lots)} Lots",
                                    color = accentColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("WIN RATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format("%.1f", member.winRate)}%",
                                    color = AccentEmerald,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("BROKER", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(member.broker, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("TOP ASSET", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(member.favoritePair, color = PrimarySky, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verified VIP Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x1A38BDF8),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, PrimarySky)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = PrimarySky,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified Top 10 Volume Trader • Synced with Web Server",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

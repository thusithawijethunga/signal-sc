package com.widhura.signalxp.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.widhura.signalxp.data.VipMemberEntity
import com.widhura.signalxp.ui.MainViewModel
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.CardHeaderBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun VipLeaderboardScreen(
    viewModel: MainViewModel,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val cardHeaderBg = if (isDarkMode) CardHeaderBackground else LightTheme.CardHeaderBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val accentAmber = if (isDarkMode) AccentAmber else LightTheme.AccentAmber
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed

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

    LaunchedEffect(vipWebUrl) {
        if (vipWebUrl.isNotBlank()) {
            viewModel.syncVipLeaderboardFromWeb()
        }
    }

    val totalLots = remember(vipMembers) { vipMembers.sumOf { it.lots } }
    val df = remember { DecimalFormat("#,##0.00") }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "spin"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = bg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top 10 — Lots අනුව",
                            color = textSec,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSearchOpen = !isSearchOpen }, modifier = Modifier.size(34.dp)) {
                            Icon(
                                if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                                "Search", tint = if (isSearchOpen) primary else textSec,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.syncVipLeaderboardFromWeb { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                        }, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Default.Refresh, "Sync",
                                tint = if (isSyncingVip) primary else textSec,
                                modifier = Modifier.size(20.dp).then(if (isSyncingVip) Modifier.rotate(spinAngle) else Modifier)
                            )
                        }
                    }
                }
            }

            // Search
            if (isSearchOpen) {
                item {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { viewModel.setVipSearchQuery(it) },
                        placeholder = { Text("Search by name, ID...", fontSize = 12.sp, color = textSec) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary, unfocusedBorderColor = border,
                            focusedContainerColor = cardHeaderBg, unfocusedContainerColor = cardHeaderBg,
                            focusedTextColor = textOnBg, unfocusedTextColor = textOnBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
            }

            // ═══════════════════════════════════════════
            // PODIUM - TOP 3 MEMBERS (01, 02, 03)
            // ═══════════════════════════════════════════
            if (vipMembers.size >= 3) {
                item {
                    PodiumTop3(
                        first = vipMembers[0],
                        second = vipMembers[1],
                        third = vipMembers[2],
                        onClick = { selectedMemberForDetail = it }
                    )
                }
            }

            // Period Filter Tabs
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MONTHLY" to "🔥 This Month", "ALL_TIME" to "🌐 All Time", "WEEKLY" to "⚡ This Week").forEach { (key, label) ->
                        val isSelected = selectedPeriod == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) primary.copy(alpha = 0.2f) else cardHeaderBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) primary else border.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f).clickable { viewModel.setVipPeriodFilter(key) }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = if (isSelected) primary else textSec, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Total Volume Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp), color = cardHeaderBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, border.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL VIP VOLUME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textSec, letterSpacing = 1.sp)
                            Text("${df.format(totalLots)} Lots", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = primary, fontFamily = FontFamily.Monospace)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = accentAmber.copy(alpha = 0.15f), border = androidx.compose.foundation.BorderStroke(0.5.dp, accentAmber)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, null, tint = accentAmber, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("VIP Lounge", color = accentAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Remaining Members (#4 onwards)
            if (vipMembers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No VIP members recorded yet.", color = textSec, fontSize = 13.sp)
                            if (vipWebUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp), color = primary,
                                    modifier = Modifier.clickable { viewModel.syncVipLeaderboardFromWeb { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() } }
                                ) {
                                    Text("Fetch from Web", color = bg, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                val remainingMembers = vipMembers.drop(3)
                itemsIndexed(remainingMembers) { index, member ->
                    VipLeaderboardRow(member = member, displayRank = index + 4, onClick = { selectedMemberForDetail = member })
                }
                item { Spacer(modifier = Modifier.height(70.dp)) }
            }
        }
    }

    if (showAdminConfigDialog) {
        AdminWebLinkConfigDialog(currentUrl = vipWebUrl, viewModel = viewModel, onDismiss = { showAdminConfigDialog = false })
    }

    selectedMemberForDetail?.let { member ->
        VipTraderDetailDialog(member = member, onDismiss = { selectedMemberForDetail = null })
    }
}

// ═══════════════════════════════════════════════════════
// PODIUM COMPOSABLE - Top 3 with photos & rank numbers
// ═══════════════════════════════════════════════════════
@Composable
fun PodiumTop3(
    first: VipMemberEntity,
    second: VipMemberEntity,
    third: VipMemberEntity,
    onClick: (VipMemberEntity) -> Unit
) {
    val df = remember { DecimalFormat("#,##0.00") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // ── #2 (Left - Silver) ──
        PodiumCard(
            member = second,
            rank = 2,
            rankLabel = "02",
            cardHeight = 160.dp,
            borderColor = Color(0xFFC0C0C0),
            rankColor = Color(0xFFC0C0C0),
            modifier = Modifier.weight(1f),
            onClick = { onClick(second) },
            df = df
        )

        // ── #1 (Center - Gold, tallest) ──
        PodiumCard(
            member = first,
            rank = 1,
            rankLabel = "01",
            cardHeight = 200.dp,
            borderColor = AccentAmber,
            rankColor = AccentAmber,
            modifier = Modifier.weight(1.15f),
            onClick = { onClick(first) },
            df = df
        )

        // ── #3 (Right - Bronze) ──
        PodiumCard(
            member = third,
            rank = 3,
            rankLabel = "03",
            cardHeight = 160.dp,
            borderColor = Color(0xFFCD7F32),
            rankColor = Color(0xFFCD7F32),
            modifier = Modifier.weight(1f),
            onClick = { onClick(third) },
            df = df
        )
    }
}

@Composable
fun PodiumCard(
    member: VipMemberEntity,
    rank: Int,
    rankLabel: String,
    cardHeight: androidx.compose.ui.unit.Dp,
    borderColor: Color,
    rankColor: Color,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    df: DecimalFormat
) {
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    Card(
        modifier = modifier
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar circle with rank number inside
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                borderColor.copy(alpha = 0.3f),
                                borderColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(2.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rankLabel,
                    color = rankColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Name
            Text(
                text = member.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Lots
            Text(
                text = "${df.format(member.lots)} Lots",
                color = rankColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// REGULAR ROW (#4 onwards)
// ═══════════════════════════════════════════════════════
@Composable
fun VipLeaderboardRow(
    member: VipMemberEntity,
    displayRank: Int,
    isDarkMode: Boolean = true,
    onClick: () -> Unit
) {
    val df = remember { DecimalFormat("#,##0.00") }
    val animatedProgress by animateFloatAsState(
        targetValue = member.progressFraction.coerceIn(0.04f, 0.95f),
        animationSpec = tween(durationMillis = 600), label = "progress"
    )
    val rowAccentColor = Color(member.accentHex)
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky

    val (cardBg, cardBorder) = when {
        !isDarkMode -> LightTheme.CardBackground to LightTheme.BorderColor
        displayRank == 1 -> Color(0xFF14110A) to Color(0xFF4A3814)
        displayRank == 2 -> Color(0xFF0F1521) to Color(0xFF26354D)
        displayRank == 3 -> Color(0xFF17110C) to Color(0xFF3F2716)
        else -> Color(0xFF0A111F) to Color(0xFF162338)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.CenterStart) {
                when (displayRank) {
                    1 -> Text("🥇", fontSize = 20.sp)
                    2 -> Text("🥈", fontSize = 20.sp)
                    3 -> Text("🥉", fontSize = 20.sp)
                    else -> Text("#$displayRank", color = textSec, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Name + Progress bar
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(member.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(animatedProgress).height(3.dp).clip(RoundedCornerShape(2.dp)).background(rowAccentColor))
                }
            }

            // Member ID pill
            Box(modifier = Modifier.padding(end = 12.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(14.dp), color = primary, modifier = Modifier.height(26.dp)) {
                    Box(modifier = Modifier.padding(horizontal = if (member.memberId == "—") 10.dp else 9.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                        Text(member.memberId, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                    }
                }
            }

            // Lots
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(62.dp)) {
                Text(df.format(member.lots), color = rowAccentColor, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                Text("lots", color = textSec, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// ADMIN WEB LINK CONFIG DIALOG
// ═══════════════════════════════════════════════════════
@Composable
fun AdminWebLinkConfigDialog(currentUrl: String, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isUnlocked by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var webUrlInput by remember { mutableStateOf(currentUrl) }
    var syncResultMsg by remember { mutableStateOf<String?>(null) }
    var isTestingSync by remember { mutableStateOf(false) }
    var showFormatGuide by remember { mutableStateOf(false) }

    val sampleJson = """[{"name":"Prabath manjula","memberId":"SX1043","lots":81.15,"broker":"Exness","favoritePair":"XAU/USD","winRate":86.4,"totalTrades":128}]"""

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(18.dp), color = CardBackground, border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = PrimarySky.copy(alpha = 0.15f), modifier = Modifier.size(34.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CloudSync, null, tint = PrimarySky, modifier = Modifier.size(18.dp)) }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Admin Web Link Config", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Close", tint = TextSecondary) }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isUnlocked) {
                    Text("Enter Admin PIN to configure Web API source link.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = enteredPin, onValueChange = { enteredPin = it; pinError = false },
                        label = { Text("Admin PIN (7788, 1234, 9988)") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimarySky, unfocusedBorderColor = BorderColor, focusedTextColor = TextLight, unfocusedTextColor = TextLight, errorBorderColor = AccentRed),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) Text("Invalid PIN", color = AccentRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp), color = PrimarySky, modifier = Modifier.fillMaxWidth().clickable {
                            if (enteredPin in listOf("7788", "1234", "9988", "admin")) isUnlocked = true else pinError = true
                        }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("Authorize & Open Settings", color = DarkBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text("Set the Web JSON URL where admin updates Top 10 rankings.", color = TextSecondary, fontSize = 11.5.sp, lineHeight = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = webUrlInput, onValueChange = { webUrlInput = it; syncResultMsg = null },
                        label = { Text("Web API / JSON URL") }, singleLine = false, maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimarySky, unfocusedBorderColor = BorderColor, focusedTextColor = TextLight, unfocusedTextColor = TextLight),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp), color = CardHeaderBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimarySky.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !isTestingSync && webUrlInput.isNotBlank()) {
                            isTestingSync = true; syncResultMsg = "Connecting..."
                            viewModel.syncVipLeaderboardFromWeb(webUrlInput) { success, msg -> isTestingSync = false; syncResultMsg = if (success) "✓ $msg" else "✕ $msg" }
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            if (isTestingSync) { CircularProgressIndicator(color = PrimarySky, strokeWidth = 2.dp, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(8.dp)) }
                            else { Icon(Icons.Default.CloudSync, null, tint = PrimarySky, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(6.dp)) }
                            Text(if (isTestingSync) "Testing..." else "Test & Sync Now", color = PrimarySky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    syncResultMsg?.let { Text(it, color = if (it.startsWith("✓")) AccentEmerald else AccentRed, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0x1538BDF8), border = androidx.compose.foundation.BorderStroke(0.5.dp, PrimarySky.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth().clickable { showFormatGuide = !showFormatGuide }) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = PrimarySky, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(6.dp)); Text(if (showFormatGuide) "Hide Guide" else "View JSON Format Guide", color = PrimarySky, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            Text(if (showFormatGuide) "▲" else "▼", color = PrimarySky, fontSize = 10.sp)
                        }
                    }
                    AnimatedVisibility(visible = showFormatGuide) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF030712), border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Sample JSON:", color = TextSecondary, fontSize = 10.sp)
                                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(sampleJson)); Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show() }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.ContentCopy, "Copy", tint = PrimarySky, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(sampleJson, color = AccentEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp), color = PrimarySky, modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.setVipWebUrl(webUrlInput)
                            if (webUrlInput.isNotBlank()) viewModel.syncVipLeaderboardFromWeb { _, msg -> Toast.makeText(context, "Saved! $msg", Toast.LENGTH_SHORT).show() }
                            else Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text("Save Web Link", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// VIP TRADER DETAIL DIALOG
// ═══════════════════════════════════════════════════════
@Composable
fun VipTraderDetailDialog(member: VipMemberEntity, onDismiss: () -> Unit) {
    val df = remember { DecimalFormat("#,##0.00") }
    val accentColor = Color(member.accentHex)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp), color = CardBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.2f), modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Star, null, tint = accentColor, modifier = Modifier.size(18.dp)) }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(member.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("VIP ID: ${member.memberId}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close, "Close", tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(shape = RoundedCornerShape(12.dp), color = CardHeaderBackground, border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("VOLUME", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("${df.format(member.lots)} Lots", color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace) }
                            Column(horizontalAlignment = Alignment.End) { Text("WIN RATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text("${String.format("%.1f", member.winRate)}%", color = AccentEmerald, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace) }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("BROKER", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(member.broker, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                            Column(horizontalAlignment = Alignment.End) { Text("TOP ASSET", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(member.favoritePair, color = PrimarySky, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(shape = RoundedCornerShape(10.dp), color = Color(0x1A38BDF8), border = androidx.compose.foundation.BorderStroke(0.5.dp, PrimarySky)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = PrimarySky, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verified Top 10 Volume Trader", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

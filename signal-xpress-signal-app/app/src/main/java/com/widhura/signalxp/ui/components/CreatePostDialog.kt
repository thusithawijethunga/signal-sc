package com.widhura.signalxp.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.widhura.signalxp.R
import com.widhura.signalxp.data.CommunityPostEntity
import com.widhura.signalxp.ui.MainViewModel
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.BuyBadgeBg
import com.widhura.signalxp.ui.theme.BuyBadgeText
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.CardHeaderBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.SellBadgeBg
import com.widhura.signalxp.ui.theme.SellBadgeText
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 📸 Screenshot & Idea Post, 1 = 💎 Custom P&L Profit Card

    // Trader Info
    var authorName by remember { mutableStateOf("Kasun Trader LK") }
    var authorBadge by remember { mutableStateOf("VIP Trader") }
    val avatarColors = listOf(0xFF10B981, 0xFF38BDF8, 0xFFF59E0B, 0xFF8B5CF6, 0xFFEC4899)
    var selectedAvatarHex by remember { mutableStateOf(avatarColors[0]) }

    // TAB 0: Screenshot & Idea fields
    var typedThought by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<String?>("res://drawable/img_gold_profit_shot") }
    var selectedPair by remember { mutableStateOf("XAU/USD") }
    var tradeType by remember { mutableStateOf("BUY") }
    var optionalProfitText by remember { mutableStateOf("320.00") }
    var optionalPipsText by remember { mutableStateOf("65") }
    var customTags by remember { mutableStateOf("#XAUUSD #ForexSL #SniperEntry") }

    val popularPairs = listOf("XAU/USD", "EUR/USD", "GBP/JPY", "BTC/USD", "USD/JPY", "GBP/USD", "GENERAL")

    // Image Picker Launcher for device gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri.toString()
            Toast.makeText(context, "Screenshot attached successfully! 📸", Toast.LENGTH_SHORT).show()
        }
    }

    // Fallback file picker if photo picker not available
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri.toString()
            Toast.makeText(context, "Screenshot selected! 📸", Toast.LENGTH_SHORT).show()
        }
    }

    // TAB 1: Profit Card fields
    var pnlProfitAmountText by remember { mutableStateOf("450.00") }
    var pnlPipsGainText by remember { mutableStateOf("90") }
    var pnlRoiText by remember { mutableStateOf("180.0") }
    var pnlLotSizeText by remember { mutableStateOf("0.50") }
    var pnlEntryPriceText by remember { mutableStateOf("4122.00") }
    var pnlExitPriceText by remember { mutableStateOf("4131.00") }
    var pnlBroker by remember { mutableStateOf("Exness Pro") }
    var pnlTheme by remember { mutableStateOf("EMERALD_NEON") }
    var pnlCaption by remember { mutableStateOf("TP2 hit with sniper precision! 🚀") }

    val themes = listOf(
        Pair("EMERALD_NEON", "🟢 Neon Emerald"),
        Pair("GOLD_LUXURY", "🟡 Luxury Gold"),
        Pair("CYBER_SKY", "🔵 Cyber Sky"),
        Pair("DEEP_VIOLET", "🟣 Deep Violet")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardHeaderBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Post to Traders Community",
                            color = TextLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Share your trade screenshots, thoughts & profits",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Tabs: 📸 Screenshot & Idea vs 💎 Synthetic Profit Card
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBackground,
                    contentColor = PrimarySky,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimarySky
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) PrimarySky else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Screenshot & Idea",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) TextLight else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) AccentEmerald else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "P&L Profit Card",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) TextLight else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }

                // Form body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Trader Identity Row
                    Text(
                        text = "YOUR TRADER IDENTITY",
                        fontSize = 11.sp,
                        color = PrimarySky,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = authorName,
                            onValueChange = { authorName = it },
                            label = { Text("Name / Nickname") },
                            modifier = Modifier.weight(0.6f),
                            textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimarySky,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = authorBadge,
                            onValueChange = { authorBadge = it },
                            label = { Text("Badge / Title") },
                            modifier = Modifier.weight(0.4f),
                            textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimarySky,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedTab == 0) {
                        // ==========================================
                        // TAB 0: SCREENSHOT & TYPED THOUGHT POST
                        // ==========================================

                        // Typed Thought / Idea Input
                        Text(
                            text = "TYPE YOUR THOUGHTS / TRADE IDEA / COMMENTS",
                            fontSize = 11.sp,
                            color = PrimarySky,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = typedThought,
                            onValueChange = { typedThought = it },
                            placeholder = { Text("Type your thoughts, trade idea, market setup analysis, or question here...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6,
                            textStyle = TextStyle(color = TextLight, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimarySky,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Screenshot Attachment Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ATTACH SCREENSHOT (MT5 / CHART / TRADE)",
                                fontSize = 11.sp,
                                color = PrimarySky,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            if (attachedImageUri != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentRed.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { attachedImageUri = null }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = AccentRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Remove", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Attached image preview or upload box
                        if (attachedImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkBackground)
                                    .border(1.dp, PrimarySky, RoundedCornerShape(12.dp))
                            ) {
                                when {
                                    attachedImageUri?.contains("img_gold_profit_shot") == true -> {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_gold_profit_shot),
                                            contentDescription = "Selected screenshot",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    attachedImageUri?.contains("img_chart_analysis_shot") == true -> {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_chart_analysis_shot),
                                            contentDescription = "Selected screenshot",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        SubcomposeAsyncImage(
                                            model = attachedImageUri,
                                            contentDescription = "Picked screenshot",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = AccentEmerald,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Screenshot Attached",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Empty upload box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkBackground)
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .clickable {
                                        try {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        } catch (e: Exception) {
                                            getContentLauncher.launch("image/*")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Pick Screenshot",
                                        tint = PrimarySky,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap to pick screenshot from phone gallery",
                                        color = TextLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "PNG, JPG, MT4/MT5 trade screenshot",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick screenshot preset buttons (Phone Gallery vs Sample Presets)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } catch (e: Exception) {
                                        getContentLauncher.launch("image/*")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimarySky),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = PrimarySky,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery Photo", color = PrimarySky, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { attachedImageUri = "res://drawable/img_gold_profit_shot" },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🟡 Gold Profit Shot", color = TextLight, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { attachedImageUri = "res://drawable/img_chart_analysis_shot" },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📊 Chart Setup", color = TextLight, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pair & Direction Options
                        Text(
                            text = "PAIR & TRADE DETAILS (OPTIONAL)",
                            fontSize = 11.sp,
                            color = PrimarySky,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            popularPairs.forEach { pair ->
                                val isSel = selectedPair == pair
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) PrimarySky else CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSel) PrimarySky else BorderColor
                                    ),
                                    modifier = Modifier.clickable { selectedPair = pair }
                                ) {
                                    Text(
                                        text = pair,
                                        color = if (isSel) DarkBackground else TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // BUY / SELL / IDEA Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (tradeType == "BUY") BuyBadgeBg else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tradeType = "BUY" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = if (tradeType == "BUY") BuyBadgeText else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "BUY / LONG",
                                        color = if (tradeType == "BUY") BuyBadgeText else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (tradeType == "SELL") SellBadgeBg else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tradeType = "SELL" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (tradeType == "SELL") SellBadgeText else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "SELL / SHORT",
                                        color = if (tradeType == "SELL") SellBadgeText else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (tradeType == "IDEA") PrimarySky.copy(alpha = 0.2f) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tradeType = "IDEA" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (tradeType == "IDEA") PrimarySky else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "ANALYSIS",
                                        color = if (tradeType == "IDEA") PrimarySky else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Optional Profit Amount and Pips
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = optionalProfitText,
                                onValueChange = { optionalProfitText = it },
                                label = { Text("Profit Amount $ (Optional)") },
                                modifier = Modifier.weight(0.5f),
                                textStyle = TextStyle(color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentEmerald,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = optionalPipsText,
                                onValueChange = { optionalPipsText = it },
                                label = { Text("Pips (Optional)") },
                                modifier = Modifier.weight(0.5f),
                                textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimarySky,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customTags,
                            onValueChange = { customTags = it },
                            label = { Text("Hashtags / Tags (e.g. #XAUUSD #Profit #SLST)") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = PrimarySky, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimarySky,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            singleLine = true
                        )

                    } else {
                        // ==========================================
                        // TAB 1: P&L PROFIT CARD BUILDER
                        // ==========================================
                        val previewProfit = pnlProfitAmountText.toDoubleOrNull() ?: 0.0
                        val previewPips = pnlPipsGainText.toIntOrNull() ?: 0
                        val previewRoi = pnlRoiText.toDoubleOrNull() ?: 0.0

                        val previewPost = remember(
                            authorName, authorBadge, selectedAvatarHex, selectedPair, tradeType,
                            pnlProfitAmountText, pnlPipsGainText, pnlRoiText, pnlLotSizeText,
                            pnlEntryPriceText, pnlExitPriceText, pnlBroker, pnlTheme, pnlCaption
                        ) {
                            CommunityPostEntity(
                                authorName = if (authorName.isBlank()) "Trader LK" else authorName,
                                authorBadge = authorBadge,
                                authorAvatarHex = selectedAvatarHex,
                                postType = "PROFIT_CARD",
                                pair = selectedPair,
                                tradeType = tradeType,
                                entryPrice = pnlEntryPriceText,
                                exitPrice = pnlExitPriceText,
                                lotSize = pnlLotSizeText,
                                profitAmount = previewProfit,
                                pipsGain = previewPips,
                                roiPercentage = previewRoi,
                                brokerName = pnlBroker,
                                cardTheme = pnlTheme,
                                content = pnlCaption
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pnlProfitAmountText,
                                onValueChange = { pnlProfitAmountText = it },
                                label = { Text("Profit Amount ($)") },
                                modifier = Modifier.weight(0.5f),
                                textStyle = TextStyle(color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentEmerald,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = pnlPipsGainText,
                                onValueChange = { pnlPipsGainText = it },
                                label = { Text("Pips") },
                                modifier = Modifier.weight(0.5f),
                                textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimarySky,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pnlEntryPriceText,
                                onValueChange = { pnlEntryPriceText = it },
                                label = { Text("Entry Price") },
                                modifier = Modifier.weight(0.5f),
                                textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimarySky,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = pnlExitPriceText,
                                onValueChange = { pnlExitPriceText = it },
                                label = { Text("Exit Price") },
                                modifier = Modifier.weight(0.5f),
                                textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimarySky,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Theme Selection
                        Text(
                            text = "CARD THEME",
                            fontSize = 11.sp,
                            color = PrimarySky,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            themes.forEach { (key, label) ->
                                val isSel = pnlTheme == key
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) PrimarySky.copy(alpha = 0.2f) else CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSel) PrimarySky else BorderColor
                                    ),
                                    modifier = Modifier.clickable { pnlTheme = key }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSel) TextLight else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pnlCaption,
                            onValueChange = { pnlCaption = it },
                            label = { Text("Caption") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimarySky,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        ProfitCardWidget(post = previewPost, showCopyButton = false)
                    }
                }

                Divider(color = BorderColor, thickness = 1.dp)

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                val profitVal = optionalProfitText.toDoubleOrNull() ?: 0.0
                                val pipsVal = optionalPipsText.toIntOrNull() ?: 0

                                viewModel.createCommunityPost(
                                    authorName = authorName,
                                    authorBadge = authorBadge,
                                    authorAvatarHex = selectedAvatarHex,
                                    content = typedThought,
                                    imageUri = attachedImageUri,
                                    pair = selectedPair,
                                    tradeType = tradeType,
                                    profitAmount = profitVal,
                                    pipsGain = pipsVal,
                                    hashtags = customTags
                                )
                            } else {
                                val profitVal = pnlProfitAmountText.toDoubleOrNull() ?: 0.0
                                val pipsVal = pnlPipsGainText.toIntOrNull() ?: 0
                                val roiVal = pnlRoiText.toDoubleOrNull() ?: 0.0

                                viewModel.createProfitCardPost(
                                    authorName = authorName,
                                    authorBadge = authorBadge,
                                    authorAvatarHex = selectedAvatarHex,
                                    pair = selectedPair,
                                    tradeType = tradeType,
                                    entryPrice = pnlEntryPriceText,
                                    exitPrice = pnlExitPriceText,
                                    lotSize = pnlLotSizeText,
                                    profitAmount = profitVal,
                                    pipsGain = pipsVal,
                                    roiPercentage = roiVal,
                                    brokerName = pnlBroker,
                                    cardTheme = pnlTheme,
                                    caption = pnlCaption,
                                    hashtags = "#${selectedPair.replace("/", "")} #ProfitCard"
                                )
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySky),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedTab == 0) "Publish Post & Screenshot 🚀" else "Publish Profit Card 💎",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

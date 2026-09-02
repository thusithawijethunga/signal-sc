package com.widhura.signalxp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.widhura.signalxp.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widhura.signalxp.data.CommunityPostEntity
import com.widhura.signalxp.ui.CommunityFilter
import com.widhura.signalxp.ui.MainViewModel
import com.widhura.signalxp.ui.components.CommunityPostCard
import com.widhura.signalxp.ui.components.CreatePostDialog
import com.widhura.signalxp.ui.theme.AccentAmber
import com.widhura.signalxp.ui.theme.AccentEmerald
import com.widhura.signalxp.ui.theme.AccentRed
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.CardHeaderBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.SecondaryBlue
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun CommunityScreen(
    viewModel: MainViewModel,
    isDarkMode: Boolean = true
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

    val posts by viewModel.filteredCommunityPosts.collectAsState()
    val topHighlights by viewModel.topProfitHighlights.collectAsState()
    val selectedFilter by viewModel.communityFilter.collectAsState()
    val searchQuery by viewModel.communitySearchQuery.collectAsState()
    val selectedPair by viewModel.communityPairFilter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val moderationMessage by viewModel.moderationMessage.collectAsState()

    LaunchedEffect(moderationMessage) {
        moderationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearModerationMessage()
        }
    }

    val pairs = listOf("ALL", "XAU/USD", "EUR/USD", "GBP/JPY", "BTC/USD")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Screen Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.signal_xpress_icon_1786298386233)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Traders Community",
                        color = textOnBg,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Share Profit Cards, Ideas & Market Thoughts",
                        color = textSec,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lazy Column for entire feed content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Top Community Profit Highlights Banner
                if (topHighlights.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = accentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TODAY'S TOP PROFIT LEADERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentAmber,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Text(
                                    text = "${topHighlights.size} Top Winners",
                                    fontSize = 11.sp,
                                    color = textSec
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(topHighlights) { item ->
                                    LeaderHighlightChip(item = item, isDarkMode = isDarkMode)
                                }
                            }
                        }
                    }
                }

                // Search Box
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setCommunitySearchQuery(it) },
                        placeholder = { Text("Search...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = textSec,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setCommunitySearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = textSec,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textStyle = TextStyle(color = textOnBg, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            unfocusedBorderColor = border,
                            focusedContainerColor = cardHeaderBg,
                            unfocusedContainerColor = cardHeaderBg
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Filter Tabs: All, 📸 Screenshots, 💎 Profit Cards, 💡 Discussions, 🏆 Top Gainers
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        item {
                            CommunityFilterChip(
                                label = "🌐 All Feed",
                                isSelected = selectedFilter == CommunityFilter.ALL,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.ALL) },
                                isDarkMode = isDarkMode
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "📸 Screenshots",
                                isSelected = selectedFilter == CommunityFilter.SCREENSHOTS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.SCREENSHOTS) },
                                isDarkMode = isDarkMode
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "💎 Profit Cards",
                                isSelected = selectedFilter == CommunityFilter.PROFIT_CARDS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.PROFIT_CARDS) },
                                isDarkMode = isDarkMode
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "💡 Discussions",
                                isSelected = selectedFilter == CommunityFilter.DISCUSSIONS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.DISCUSSIONS) },
                                isDarkMode = isDarkMode
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "🏆 Top Gainers",
                                isSelected = selectedFilter == CommunityFilter.TOP_GAINERS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.TOP_GAINERS) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // Pair Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pairs.forEach { pair ->
                            val isSelected = selectedPair == pair
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) primary.copy(alpha = 0.2f) else cardHeaderBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) primary else border
                                ),
                                modifier = Modifier.clickable { viewModel.setCommunityPairFilter(pair) }
                            ) {
                                Text(
                                    text = pair,
                                    color = if (isSelected) primary else textSec,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Feed List Items
                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💎",
                                    fontSize = 40.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No posts found matching filter",
                                    color = textOnBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Be the first to share your profit card or market thought!",
                                    color = textSec,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                                ) {
                                    Text("Post to Community 🚀", color = bg, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Show pending posts banner if user has pending posts
                    val pendingPosts = posts.filter { it.status == "pending" }
                    if (pendingPosts.isNotEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = accentAmber.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentAmber.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "⏳", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${pendingPosts.size} post${if (pendingPosts.size > 1) "s" else ""} pending review",
                                            color = accentAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Your post will appear after admin approval",
                                            color = textSec,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Show rejected posts
                    val rejectedPosts = posts.filter { it.status == "rejected" }
                    if (rejectedPosts.isNotEmpty()) {
                        items(rejectedPosts, key = { "rejected_${it.id}" }) { post ->
                            RejectedPostCard(post = post, isDarkMode = isDarkMode)
                        }
                    }

                    items(posts.filter { it.status == "approved" }, key = { it.id }) { post ->
                        CommunityPostCard(
                            post = post,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Profit Card / Discussion
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = primary,
            contentColor = bg,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Share Profit / Thought"
            )
        }
    }

    if (showCreateDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun CommunityFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean = true
) {
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary
    val bg = if (isDarkMode) DarkBackground else LightTheme.Background

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) primary else cardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) primary else border
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) bg else textOnBg,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun LeaderHighlightChip(item: CommunityPostEntity, isDarkMode: Boolean = true) {
    val cardBg = if (isDarkMode) CardBackground else LightTheme.CardBackground
    val accentAmber = if (isDarkMode) AccentAmber else LightTheme.AccentAmber
    val accentEmerald = if (isDarkMode) AccentEmerald else LightTheme.AccentEmerald
    val textOnBg = if (isDarkMode) TextLight else LightTheme.TextPrimary

    val df = remember { DecimalFormat("#,##0.00") }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentAmber.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(item.authorAvatarHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.authorName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = item.authorName,
                    color = textOnBg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+$${df.format(item.profitAmount)} (${item.pair})",
                    color = accentEmerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RejectedPostCard(post: CommunityPostEntity, isDarkMode: Boolean = true) {
    val accentRed = if (isDarkMode) AccentRed else LightTheme.AccentRed
    val textSec = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = accentRed.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentRed.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "❌", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Post Not Approved",
                    color = accentRed.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            if (post.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.content.take(100) + if (post.content.length > 100) "..." else "",
                    color = textSec,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
            if (!post.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: ${post.rejectionReason}",
                    color = textSec,
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

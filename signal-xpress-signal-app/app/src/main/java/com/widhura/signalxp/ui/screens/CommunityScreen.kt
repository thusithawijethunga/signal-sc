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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
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
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardBackground
import com.widhura.signalxp.ui.theme.CardHeaderBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.SecondaryBlue
import com.widhura.signalxp.ui.theme.TextLight
import com.widhura.signalxp.ui.theme.TextPrimary
import com.widhura.signalxp.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun CommunityScreen(
    viewModel: MainViewModel
) {
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
            .background(DarkBackground)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Traders Community",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentEmerald.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3.4K Online",
                                    color = AccentEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Share Profit Cards, Ideas & Market Thoughts",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Header Action Button
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySky),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = "Share",
                        tint = DarkBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
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
                                        tint = AccentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TODAY'S TOP PROFIT LEADERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentAmber,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Text(
                                    text = "${topHighlights.size} Top Winners",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(topHighlights) { item ->
                                    LeaderHighlightChip(item = item)
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
                        placeholder = { Text("Search traders, pairs, brokers, #hashtags...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setCommunitySearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textStyle = TextStyle(color = TextLight, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimarySky,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = CardHeaderBackground,
                            unfocusedContainerColor = CardHeaderBackground
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
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.ALL) }
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "📸 Screenshots",
                                isSelected = selectedFilter == CommunityFilter.SCREENSHOTS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.SCREENSHOTS) }
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "💎 Profit Cards",
                                isSelected = selectedFilter == CommunityFilter.PROFIT_CARDS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.PROFIT_CARDS) }
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "💡 Discussions",
                                isSelected = selectedFilter == CommunityFilter.DISCUSSIONS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.DISCUSSIONS) }
                            )
                        }
                        item {
                            CommunityFilterChip(
                                label = "🏆 Top Gainers",
                                isSelected = selectedFilter == CommunityFilter.TOP_GAINERS,
                                onClick = { viewModel.setCommunityFilter(CommunityFilter.TOP_GAINERS) }
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
                                color = if (isSelected) PrimarySky.copy(alpha = 0.2f) else CardHeaderBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimarySky else BorderColor
                                ),
                                modifier = Modifier.clickable { viewModel.setCommunityPairFilter(pair) }
                            ) {
                                Text(
                                    text = pair,
                                    color = if (isSelected) PrimarySky else TextSecondary,
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
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Be the first to share your profit card or market thought!",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySky)
                                ) {
                                    Text("Post to Community 🚀", color = DarkBackground, fontWeight = FontWeight.Bold)
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
                                color = AccentAmber.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
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
                                            color = AccentAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Your post will appear after admin approval",
                                            color = TextSecondary,
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
                            RejectedPostCard(post = post)
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
        ExtendedFloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = PrimarySky,
            contentColor = DarkBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Share Profit / Thought"
                )
            },
            text = {
                Text(
                    text = "Share Profit / Idea",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        )
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
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimarySky else CardBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimarySky else BorderColor
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) DarkBackground else TextLight,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun LeaderHighlightChip(item: CommunityPostEntity) {
    val df = remember { DecimalFormat("#,##0.00") }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
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
                    color = TextLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+$${df.format(item.profitAmount)} (${item.pair})",
                    color = Color(0xFF4ADE80),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RejectedPostCard(post: CommunityPostEntity) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF991B1B).copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "❌", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Post Not Approved",
                    color = Color(0xFFFCA5A5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            if (post.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.content.take(100) + if (post.content.length > 100) "..." else "",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
            if (!post.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: ${post.rejectionReason}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

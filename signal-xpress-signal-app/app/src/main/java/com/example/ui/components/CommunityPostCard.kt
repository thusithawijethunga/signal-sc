package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.CommunityCommentEntity
import com.example.data.CommunityPostEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.BuyBadgeBg
import com.example.ui.theme.BuyBadgeText
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardHeaderBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.SellBadgeBg
import com.example.ui.theme.SellBadgeText
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityPostCard(
    post: CommunityPostEntity,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCommentsExpanded by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var commenterName by remember { mutableStateOf("") }
    var showFullImageViewer by remember { mutableStateOf(false) }

    val commentsList by viewModel.getCommentsForPost(post.id).collectAsState(initial = emptyList())
    val df = remember { DecimalFormat("#,##0.00") }

    val timeAgoStr = remember(post.timestamp) {
        val diff = System.currentTimeMillis() - post.timestamp
        val mins = diff / (1000 * 60)
        val hours = mins / 60
        val days = hours / 24
        when {
            mins < 1 -> "Just now"
            mins < 60 -> "${mins}m ago"
            hours < 24 -> "${hours}h ago"
            else -> "${days}d ago"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (post.isPinned) AccentAmber.copy(alpha = 0.6f) else BorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Pinned Banner if active
            if (post.isPinned) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = AccentAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PINNED HIGHLIGHT",
                        color = AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Author Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Circle
                    val avatarColor = Color(post.authorAvatarHex)
                    val initials = post.authorName.take(2).uppercase()

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PrimarySky.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, PrimarySky)
                            ) {
                                Text(
                                    text = post.authorBadge,
                                    color = PrimarySky,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = timeAgoStr,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            if (post.pair.isNotBlank() && post.pair != "GENERAL") {
                                Text(
                                    text = " • ${post.pair}",
                                    color = PrimarySky,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Trade / Sentiment Tag
                if (post.tradeType == "BUY") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BuyBadgeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = BuyBadgeText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "BUY",
                                color = BuyBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else if (post.tradeType == "SELL") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SellBadgeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = SellBadgeText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SELL",
                                color = SellBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Typed Idea / Thought Content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    color = TextLight,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Attached Screenshot View
            if (!post.imageUri.isNullOrBlank()) {
                PostScreenshotWidget(
                    imageUri = post.imageUri,
                    profitAmount = post.profitAmount,
                    pipsGain = post.pipsGain,
                    onImageClick = { showFullImageViewer = true }
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else if (post.postType == "PROFIT_CARD") {
                // If purely Profit Card without screenshot image, render widget
                ProfitCardWidget(post = post)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Profit numbers tag if not inside screenshot and available
            if (post.imageUri.isNullOrBlank() && post.postType != "PROFIT_CARD" && post.profitAmount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROFIT: +$${df.format(post.profitAmount)}",
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        if (post.pipsGain > 0) {
                            Text(
                                text = " (+${post.pipsGain} Pips)",
                                color = TextLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Hashtags
            if (post.hashtags.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    post.hashtags.split(" ").filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x2238BDF8)
                        ) {
                            Text(
                                text = tag,
                                color = PrimarySky,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = BorderColor.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Reaction & Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like button
                    ReactionButton(
                        icon = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        count = post.likesCount,
                        isSelected = post.isLikedByMe,
                        activeColor = AccentRed,
                        onClick = { viewModel.toggleCommunityLike(post) }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Fire button
                    ReactionButton(
                        icon = if (post.isFiredByMe) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                        count = post.fireCount,
                        isSelected = post.isFiredByMe,
                        activeColor = AccentAmber,
                        onClick = { viewModel.toggleCommunityFire(post) }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Rocket button
                    ReactionButton(
                        icon = if (post.isRocketByMe) Icons.Filled.RocketLaunch else Icons.Outlined.RocketLaunch,
                        count = post.rocketCount,
                        isSelected = post.isRocketByMe,
                        activeColor = PrimarySky,
                        onClick = { viewModel.toggleCommunityRocket(post) }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Comments Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isCommentsExpanded = !isCommentsExpanded }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = if (isCommentsExpanded) PrimarySky else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.commentsCount + commentsList.size.coerceAtLeast(0)}",
                            color = if (isCommentsExpanded) PrimarySky else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareBody = if (post.imageUri != null) {
                                "📸 Trader Post by ${post.authorName}:\n\"${post.content}\"\nPair: ${post.pair} (${post.tradeType})\nShared from Signal Xpress"
                            } else if (post.postType == "PROFIT_CARD") {
                                "🔥 Profit Card by ${post.authorName}:\nPair: ${post.pair} (${post.tradeType})\nProfit: +$${post.profitAmount} (+${post.pipsGain} Pips)\nCaption: ${post.content}\nShared from Signal Xpress"
                            } else {
                                "💡 Trader Discussion by ${post.authorName}:\n\"${post.content}\"\nShared from Signal Xpress"
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Community Post", shareBody))
                            Toast.makeText(context, "Post copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expandable Comments Section
            AnimatedVisibility(
                visible = isCommentsExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(CardHeaderBackground, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Replies & Trader Comments (${commentsList.size})",
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (commentsList.isEmpty()) {
                        Text(
                            text = "No replies yet. Be the first to share your thoughts!",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        commentsList.forEach { comment ->
                            CommentRow(comment = comment)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add reply form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commenterName,
                                onValueChange = { commenterName = it },
                                placeholder = { Text("Your Name", fontSize = 11.sp) },
                                modifier = Modifier.weight(0.4f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextLight),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimarySky,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                placeholder = { Text("Write a reply...", fontSize = 11.sp) },
                                modifier = Modifier.weight(0.6f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextLight),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimarySky,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        viewModel.addComment(post.id, commenterName, newCommentText)
                                        newCommentText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimarySky, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Screen Image Viewer Modal
    if (showFullImageViewer && !post.imageUri.isNullOrBlank()) {
        FullScreenImageDialog(
            imageUri = post.imageUri,
            authorName = post.authorName,
            caption = post.content,
            onDismiss = { showFullImageViewer = false }
        )
    }
}

@Composable
fun PostScreenshotWidget(
    imageUri: String,
    profitAmount: Double = 0.0,
    pipsGain: Int = 0,
    onImageClick: () -> Unit
) {
    val df = remember { DecimalFormat("#,##0.00") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { onImageClick() }
    ) {
        when {
            imageUri.contains("img_gold_profit_shot") -> {
                Image(
                    painter = painterResource(id = R.drawable.img_gold_profit_shot),
                    contentDescription = "Gold Profit Screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }
            imageUri.contains("img_chart_analysis_shot") -> {
                Image(
                    painter = painterResource(id = R.drawable.img_chart_analysis_shot),
                    contentDescription = "Chart Analysis Screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                SubcomposeAsyncImage(
                    model = imageUri,
                    contentDescription = "Trader Screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimarySky,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CardHeaderBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Screenshot Attached",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                )
            }
        }

        // Tap to expand badge overlay
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Tap to expand",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Optional Profit Badge overlay on top corner
        if (profitAmount > 0) {
            Surface(
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                color = AccentEmerald.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+$${df.format(profitAmount)}",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (pipsGain > 0) {
                        Text(
                            text = " (${pipsGain} Pips)",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImageDialog(
    imageUri: String,
    authorName: String,
    caption: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Top Bar with Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Screenshot by $authorName",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (caption.isNotBlank()) {
                        Text(
                            text = caption.take(45) + if (caption.length > 45) "..." else "",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Central Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 70.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageUri.contains("img_gold_profit_shot") -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_gold_profit_shot),
                            contentDescription = "Gold Profit Screenshot Full",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    imageUri.contains("img_chart_analysis_shot") -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_chart_analysis_shot),
                            contentDescription = "Chart Analysis Full",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        SubcomposeAsyncImage(
                            model = imageUri,
                            contentDescription = "Full Screenshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimarySky)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentRow(comment: CommunityCommentEntity) {
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val timeFormatted = remember(comment.timestamp) { sdf.format(Date(comment.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x33000000), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(PrimarySky.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorName.take(1).uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorName,
                    color = PrimarySky,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = timeFormatted,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.content,
                color = TextLight,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ReactionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count",
            color = if (isSelected) activeColor else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

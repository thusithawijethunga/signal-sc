package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorBadge: String = "VIP Trader", // "VIP Trader", "Pro Scalper", "Gold Master", "Forex Member"
    val authorAvatarHex: Long = 0xFF10B981,
    val postType: String = "PROFIT_CARD", // "PROFIT_CARD", "IDEA_DISCUSSION"
    val timestamp: Long = System.currentTimeMillis(),
    val content: String = "",
    val hashtags: String = "",
    val imageUri: String? = null, // URI from gallery or demo drawable name "res://drawable/img_gold_profit_shot"
    
    // Trade & Profit details (optional)
    val pair: String = "XAU/USD",
    val tradeType: String = "BUY", // "BUY", "SELL", "ANALYSIS"
    val entryPrice: String = "",
    val exitPrice: String = "",
    val lotSize: String = "",
    val profitAmount: Double = 0.0,
    val pipsGain: Int = 0,
    val roiPercentage: Double = 0.0,
    val brokerName: String = "Exness",
    val cardTheme: String = "EMERALD_NEON",
    val isVerifiedTrade: Boolean = true,
    
    // Engagement
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val fireCount: Int = 0,
    val isFiredByMe: Boolean = false,
    val rocketCount: Int = 0,
    val isRocketByMe: Boolean = false,
    val commentsCount: Int = 0,
    val isPinned: Boolean = false
)

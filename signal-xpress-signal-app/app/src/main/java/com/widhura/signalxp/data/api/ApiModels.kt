package com.widhura.signalxp.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)

@JsonClass(generateAdapter = true)
data class PaginatedResponse<T>(
    @Json(name = "current_page") val currentPage: Int = 1,
    val data: List<T> = emptyList(),
    val from: Int? = null,
    @Json(name = "last_page") val lastPage: Int = 1,
    val links: List<Link>? = null,
    @Json(name = "next_page_url") val nextPageUrl: String? = null,
    val path: String? = null,
    @Json(name = "per_page") val perPage: Int = 20,
    @Json(name = "prev_page_url") val prevPageUrl: String? = null,
    val to: Int? = null,
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class Link(
    val url: String? = null,
    val label: String? = null,
    val active: Boolean = false
)

// Auth
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String,
    val broker: String? = null,
    @Json(name = "account_id") val accountId: String? = null,
    @Json(name = "account_type") val accountType: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val user: UserResponse,
    val token: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String = "viewer",
    @Json(name = "sx_id") val sxId: String? = null,
    val broker: String? = null,
    @Json(name = "account_id") val accountId: String? = null,
    @Json(name = "account_type") val accountType: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

// Signal
@JsonClass(generateAdapter = true)
data class SignalResponse(
    val id: Long,
    val no: Int,
    val date: String,
    val pair: String,
    val direction: String,
    @Json(name = "entry1") val entry1: Double? = null,
    @Json(name = "entry2") val entry2: Double? = null,
    val sl: Double? = null,
    @Json(name = "tp1") val tp1: Double? = null,
    @Json(name = "tp2") val tp2: Double? = null,
    @Json(name = "tp3") val tp3: Double? = null,
    @Json(name = "tp4") val tp4: Double? = null,
    val pips: Double? = 0.0,
    val profit: Double? = 0.0,
    val result: String = "RUNNING",
    val channel: String? = "VIP",
    @Json(name = "hit_level") val hitLevel: String? = null,
    val status: String = "active",
    @Json(name = "thumbs_count") val thumbsCount: Int = 0,
    @Json(name = "fire_count") val fireCount: Int = 0,
    @Json(name = "rocket_count") val rocketCount: Int = 0,
    @Json(name = "broken_heart_count") val brokenHeartCount: Int = 0,
    @Json(name = "user_id") val userId: Long? = null,
    val user: UserResponse? = null
)

@JsonClass(generateAdapter = true)
data class SignalStoreRequest(
    val pair: String,
    val direction: String,
    @Json(name = "entry1") val entry1: Double,
    val sl: Double,
    @Json(name = "tp1") val tp1: Double,
    @Json(name = "tp2") val tp2: Double? = null,
    @Json(name = "tp3") val tp3: Double? = null,
    @Json(name = "tp4") val tp4: Double? = null,
    val date: String,
    val result: String? = "RUNNING",
    val channel: String? = "VIP"
)

@JsonClass(generateAdapter = true)
data class SignalReactRequest(
    val emoji: String
)

// Trade
@JsonClass(generateAdapter = true)
data class TradeResponse(
    val id: Long,
    val no: Int,
    val date: String,
    val pair: String,
    val direction: String,
    @Json(name = "entry1") val entry1: Double? = null,
    @Json(name = "entry2") val entry2: Double? = null,
    val sl: Double? = null,
    @Json(name = "tp1") val tp1: Double? = null,
    @Json(name = "tp2") val tp2: Double? = null,
    @Json(name = "tp3") val tp3: Double? = null,
    @Json(name = "tp4") val tp4: Double? = null,
    val pips: Double? = 0.0,
    val profit: Double? = 0.0,
    val result: String = "RUNNING",
    val channel: String? = "VIP",
    @Json(name = "hit_level") val hitLevel: String? = null,
    @Json(name = "user_id") val userId: Long? = null
)

@JsonClass(generateAdapter = true)
data class TradeStoreRequest(
    val pair: String,
    val direction: String,
    val date: String,
    @Json(name = "entry1") val entry1: Double? = null,
    val sl: Double? = null,
    @Json(name = "tp1") val tp1: Double? = null,
    @Json(name = "tp2") val tp2: Double? = null,
    @Json(name = "tp3") val tp3: Double? = null,
    @Json(name = "tp4") val tp4: Double? = null,
    val pips: Double? = 0.0,
    val profit: Double? = 0.0,
    val result: String = "RUNNING",
    val channel: String? = "VIP"
)

@JsonClass(generateAdapter = true)
data class TradeSummaryResponse(
    @Json(name = "total_trades") val totalTrades: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val bes: Int = 0,
    @Json(name = "total_pips") val totalPips: Double = 0.0,
    @Json(name = "total_profit") val totalProfit: Double = 0.0,
    @Json(name = "win_rate") val winRate: Double = 0.0,
    @Json(name = "current_balance") val currentBalance: Double = 0.0
)

// Community
@JsonClass(generateAdapter = true)
data class CommunityPostResponse(
    val id: Long,
    @Json(name = "user_id") val userId: Long? = null,
    @Json(name = "author_name") val authorName: String,
    @Json(name = "author_badge") val authorBadge: String? = null,
    @Json(name = "author_avatar_hex") val authorAvatarHex: Long? = null,
    @Json(name = "post_type") val postType: String = "text",
    val content: String? = null,
    val hashtags: String? = null,
    @Json(name = "image_uri") val imageUri: String? = null,
    val pair: String? = null,
    @Json(name = "trade_type") val tradeType: String? = null,
    @Json(name = "entry_price") val entryPrice: String? = null,
    @Json(name = "exit_price") val exitPrice: String? = null,
    @Json(name = "lot_size") val lotSize: String? = null,
    @Json(name = "profit_amount") val profitAmount: Double? = 0.0,
    @Json(name = "pips_gain") val pipsGain: Int? = 0,
    @Json(name = "roi_percentage") val roiPercentage: Double? = 0.0,
    @Json(name = "broker_name") val brokerName: String? = null,
    @Json(name = "card_theme") val cardTheme: String? = null,
    @Json(name = "is_verified_trade") val isVerifiedTrade: Boolean = false,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "fire_count") val fireCount: Int = 0,
    @Json(name = "rocket_count") val rocketCount: Int = 0,
    @Json(name = "comments_count") val commentsCount: Int = 0,
    @Json(name = "is_pinned") val isPinned: Boolean = false,
    val status: String = "approved",
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "comments_need_review") val commentsNeedReview: Boolean = false,
    val user: UserResponse? = null
)

@JsonClass(generateAdapter = true)
data class CommunityPostStoreRequest(
    val content: String,
    @Json(name = "post_type") val postType: String,
    val hashtags: String? = null,
    @Json(name = "image_uri") val imageUri: String? = null,
    val pair: String? = null,
    @Json(name = "trade_type") val tradeType: String? = null,
    @Json(name = "entry_price") val entryPrice: String? = null,
    @Json(name = "exit_price") val exitPrice: String? = null,
    @Json(name = "lot_size") val lotSize: String? = null,
    @Json(name = "profit_amount") val profitAmount: Double? = null,
    @Json(name = "pips_gain") val pipsGain: Int? = null,
    @Json(name = "roi_percentage") val roiPercentage: Double? = null,
    @Json(name = "broker_name") val brokerName: String? = null,
    @Json(name = "card_theme") val cardTheme: String? = null,
    @Json(name = "author_badge") val authorBadge: String? = null,
    @Json(name = "author_avatar_hex") val authorAvatarHex: Long? = null
)

@JsonClass(generateAdapter = true)
data class CommentResponse(
    val id: Long,
    @Json(name = "post_id") val postId: Long,
    @Json(name = "user_id") val userId: Long? = null,
    @Json(name = "author_name") val authorName: String,
    val content: String,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "likes_count") val likesCount: Int = 0,
    val status: String = "approved",
    val user: UserResponse? = null
)

@JsonClass(generateAdapter = true)
data class CommunitySettingsResponse(
    @Json(name = "require_post_approval") val requirePostApproval: Boolean = true,
    @Json(name = "require_comment_approval") val requireCommentApproval: Boolean = false,
    @Json(name = "max_post_length") val maxPostLength: String = "5000"
)

@JsonClass(generateAdapter = true)
data class CommunityStatsResponse(
    @Json(name = "pending_posts") val pendingPosts: Int = 0,
    @Json(name = "approved_posts") val approvedPosts: Int = 0,
    @Json(name = "rejected_posts") val rejectedPosts: Int = 0,
    @Json(name = "pending_comments") val pendingComments: Int = 0,
    @Json(name = "approved_comments") val approvedComments: Int = 0,
    @Json(name = "total_posts") val totalPosts: Int = 0,
    @Json(name = "total_comments") val totalComments: Int = 0
)

@JsonClass(generateAdapter = true)
data class PostStoreResponse(
    val post: CommunityPostResponse,
    val message: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class CommentStoreApiResponse(
    val comment: CommentResponse,
    val message: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class CommentStoreRequest(
    val content: String
)

@JsonClass(generateAdapter = true)
data class CommunityReactRequest(
    val emoji: String
)

// News
@JsonClass(generateAdapter = true)
data class NewsResponse(
    val id: Long,
    @Json(name = "event_time") val eventTime: String? = null,
    val currency: String,
    val title: String,
    val impact: String? = null,
    val forecast: String? = null,
    val previous: String? = null,
    val actual: String? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class NewsStoreRequest(
    val title: String,
    @Json(name = "event_time") val eventTime: String,
    val currency: String,
    val impact: String? = null,
    val forecast: String? = null,
    val previous: String? = null,
    val actual: String? = null,
    val description: String? = null
)

// VIP
@JsonClass(generateAdapter = true)
data class VipMemberResponse(
    val id: Long,
    val rank: Int? = null,
    val name: String,
    @Json(name = "member_id") val memberId: String,
    val lots: Double? = 0.0,
    @Json(name = "progress_fraction") val progressFraction: Double? = 0.0,
    @Json(name = "accent_hex") val accentHex: String? = null,
    val period: String? = null,
    @Json(name = "win_rate") val winRate: Double? = null,
    @Json(name = "total_trades") val totalTrades: Int? = null,
    val broker: String? = null,
    @Json(name = "favorite_pair") val favoritePair: String? = null
)

@JsonClass(generateAdapter = true)
data class VipMemberStoreRequest(
    @Json(name = "member_id") val memberId: String,
    val name: String,
    val rank: Int? = null,
    val lots: Double? = null,
    @Json(name = "progress_fraction") val progressFraction: Double? = null,
    @Json(name = "accent_hex") val accentHex: String? = null,
    val period: String? = null,
    @Json(name = "win_rate") val winRate: Double? = null,
    @Json(name = "total_trades") val totalTrades: Int? = null,
    val broker: String? = null,
    @Json(name = "favorite_pair") val favoritePair: String? = null
)

// IB Partner
@JsonClass(generateAdapter = true)
data class IbMemberResponse(
    val id: Long,
    @Json(name = "sx_id") val sxId: String,
    val name: String,
    val broker: String? = "XM",
    @Json(name = "account_id") val accountId: String? = null,
    val nic: String? = null,
    val whatsapp: String? = null,
    val telegram: String? = null,
    @Json(name = "partner_id") val partnerId: Long? = null,
    val partner: IbPartnerResponse? = null
)

@JsonClass(generateAdapter = true)
data class IbMemberStoreRequest(
    val name: String,
    val broker: String? = "XM",
    @Json(name = "account_id") val accountId: String? = null,
    val nic: String? = null,
    val whatsapp: String? = null,
    val telegram: String? = null,
    @Json(name = "partner_id") val partnerId: Long? = null
)

@JsonClass(generateAdapter = true)
data class IbPartnerResponse(
    val id: Long,
    val name: String,
    @Json(name = "members_count") val membersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class IbPartnerStoreRequest(
    val name: String
)

// Sync
@JsonClass(generateAdapter = true)
data class SyncPullResponse(
    val trades: List<TradeResponse> = emptyList(),
    val signals: List<SignalResponse> = emptyList(),
    @Json(name = "account_balances") val accountBalances: List<AccountBalanceResponse> = emptyList(),
    @Json(name = "pulled_at") val pulledAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AccountBalanceResponse(
    val id: Long,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "start_balance") val startBalance: Double = 1000.0,
    @Json(name = "deposit_balance") val depositBalance: Double = 0.0,
    @Json(name = "withdraw_balance") val withdrawBalance: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class SyncOperation(
    @Json(name = "table_name") val tableName: String,
    val action: String,
    @Json(name = "record_id") val recordId: Long? = null,
    val payload: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class SyncPushRequest(
    val operations: List<SyncOperation>,
    @Json(name = "device_id") val deviceId: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncPushResponse(
    val message: String,
    val results: Map<String, SyncResult>? = null
)

@JsonClass(generateAdapter = true)
data class SyncResult(
    val status: String,
    val result: Map<String, Any>? = null,
    val message: String? = null
)

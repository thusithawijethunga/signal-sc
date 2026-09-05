package com.widhura.signalxp.data.api

import android.content.Context
import com.widhura.signalxp.data.CommunityCommentEntity
import com.widhura.signalxp.data.CommunityPostEntity
import com.widhura.signalxp.data.NewsEntity
import com.widhura.signalxp.data.SignalDao
import com.widhura.signalxp.data.SignalEntity
import com.widhura.signalxp.data.CommunityDao
import com.widhura.signalxp.data.NewsDao
import com.widhura.signalxp.data.VipMemberDao
import com.widhura.signalxp.data.VipMemberEntity

class ApiRepository(
    private val context: Context,
    private val signalDao: SignalDao,
    private val newsDao: NewsDao,
    private val communityDao: CommunityDao,
    private val vipMemberDao: VipMemberDao
) {
    private val api = ApiClient.getApiService(context)

    // ── Signals ───────────────────────────────────────

    suspend fun fetchSignals(
        pair: String? = null,
        result: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<List<SignalEntity>> {
        return try {
            val response = api.getSignals(pair = pair, result = result, dateFrom = dateFrom, dateTo = dateTo)
            if (response.isSuccessful) {
                val signals = response.body()?.data?.map { it.toEntity() } ?: emptyList()
                Result.success(signals)
            } else {
                Result.failure(Exception("Failed to fetch signals"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed: ${e.localizedMessage}"))
        }
    }

    suspend fun syncSignals(
        pair: String? = null,
        result: String? = null
    ): Result<Unit> {
        return try {
            val response = api.getSignals(pair = pair, result = result, perPage = 100)
            if (response.isSuccessful) {
                val signals = response.body()?.data?.map { it.toEntity() } ?: emptyList()
                if (signals.isNotEmpty()) {
                    signalDao.replaceAllSignals(signals)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync signals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSignal(request: SignalStoreRequest): Result<SignalEntity> {
        return try {
            val response = api.createSignal(request)
            if (response.isSuccessful) {
                val entity = response.body()!!.toEntity()
                signalDao.insertSignal(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("Failed to create signal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSignal(id: Long, data: Map<String, Any>): Result<SignalEntity> {
        return try {
            val response = api.updateSignal(id, data)
            if (response.isSuccessful) {
                val entity = response.body()!!.toEntity()
                signalDao.updateSignal(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("Failed to update signal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSignal(id: Long): Result<Unit> {
        return try {
            val response = api.deleteSignal(id)
            if (response.isSuccessful) {
                signalDao.deleteSignalById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete signal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactSignal(id: Long, emoji: String): Result<Unit> {
        return try {
            val response = api.reactSignal(id, SignalReactRequest(emoji))
            if (response.isSuccessful) {
                val signalData = response.body()?.get("signal") as? Map<*, *>
                if (signalData != null) {
                    val entity = parseSignalFromMap(signalData)
                    if (entity != null) {
                        val existing = signalDao.getSignalById(id)
                        signalDao.updateSignal(entity.copy(
                            userReactedEmoji = existing?.userReactedEmoji
                        ))
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to react"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── News ──────────────────────────────────────────

    suspend fun syncNews(): Result<Unit> {
        return try {
            val response = api.getNews(perPage = 100)
            if (response.isSuccessful) {
                val news = response.body()?.data?.map { it.toEntity() } ?: emptyList()
                if (news.isNotEmpty()) {
                    newsDao.deleteAllNews()
                    newsDao.insertAll(news)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync news"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncNewsFromExternal(): Result<Int> {
        return try {
            val response = api.syncNews()
            if (response.isSuccessful) {
                val count = response.body()?.get("synced_count") as? Int ?: 0
                Result.success(count)
            } else {
                Result.failure(Exception("Failed to sync news from external"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Community ─────────────────────────────────────

    suspend fun syncCommunityPosts(
        postType: String? = null,
        pair: String? = null,
        search: String? = null
    ): Result<Unit> {
        return try {
            val response = api.getCommunityPosts(postType = postType, pair = pair, search = search, perPage = 100)
            if (response.isSuccessful) {
                val posts = response.body()?.data?.map { it.toEntity() } ?: emptyList()
                if (posts.isNotEmpty()) {
                    communityDao.deleteAllPosts()
                    communityDao.insertPosts(posts)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync community posts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCommunityPost(request: CommunityPostStoreRequest): Result<Pair<CommunityPostEntity, String>> {
        return try {
            val response = api.createCommunityPost(request)
            if (response.isSuccessful) {
                val body = response.body()!!
                val entity = body.post.toEntity()
                communityDao.insertPost(entity)
                Result.success(Pair(entity, body.message))
            } else {
                Result.failure(Exception("Failed to create post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCommunityPost(id: Long): Result<Unit> {
        return try {
            val response = api.deleteCommunityPost(id)
            if (response.isSuccessful) {
                communityDao.deletePostById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostComments(postId: Long): Result<List<CommunityCommentEntity>> {
        return try {
            val response = api.getPostComments(postId)
            if (response.isSuccessful) {
                val comments = response.body()?.data?.map { it.toEntity(postId) } ?: emptyList()
                Result.success(comments)
            } else {
                Result.failure(Exception("Failed to fetch comments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createComment(postId: Long, content: String): Result<Pair<CommunityCommentEntity, String>> {
        return try {
            val response = api.createComment(postId, CommentStoreRequest(content))
            if (response.isSuccessful) {
                val body = response.body()!!
                val entity = body.comment.toEntity(postId)
                communityDao.insertComment(entity)
                if (body.status == "approved") {
                    communityDao.incrementCommentCount(postId)
                }
                Result.success(Pair(entity, body.message))
            } else {
                Result.failure(Exception("Failed to create comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactPost(postId: Long, emoji: String): Result<Unit> {
        return try {
            val response = api.reactPost(postId, CommunityReactRequest(emoji))
            if (response.isSuccessful) {
                val postData = response.body()?.get("post") as? Map<*, *>
                if (postData != null) {
                    val entity = parsePostFromMap(postData)
                    if (entity != null) {
                        communityDao.updatePost(entity)
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to react"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── VIP ───────────────────────────────────────────

    suspend fun syncVipLeaderboard(period: String? = null): Result<Unit> {
        return try {
            val response = api.getVipLeaderboard(period = period, perPage = 50)
            if (response.isSuccessful) {
                val members = response.body()?.data?.map { it.toEntity() } ?: emptyList()
                if (members.isNotEmpty()) {
                    vipMemberDao.deleteAllVipMembers()
                    vipMemberDao.insertAll(members)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync VIP leaderboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Trade Summary ─────────────────────────────────

    suspend fun getTradeSummary(): Result<TradeSummaryResponse> {
        return try {
            val response = api.getTradeSummary()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch trade summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sync Push/Pull ────────────────────────────────

    suspend fun syncPull(since: String? = null): Result<SyncPullResponse> {
        return try {
            val response = api.syncPull(since)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to pull sync data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPush(request: SyncPushRequest): Result<SyncPushResponse> {
        return try {
            val response = api.syncPush(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to push sync data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Community Settings ────────────────────────────

    suspend fun getCommunitySettings(): Result<CommunitySettingsResponse> {
        return try {
            val response = api.getCommunitySettings()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Full Sync ─────────────────────────────────────

    suspend fun fullSync(): Result<Unit> {
        val signalResult = syncSignals()
        val newsResult = syncNews()
        val communityResult = syncCommunityPosts()
        val vipResult = syncVipLeaderboard()

        return if (signalResult.isSuccess && newsResult.isSuccess && communityResult.isSuccess && vipResult.isSuccess) {
            Result.success(Unit)
        } else {
            val errors = mutableListOf<String>()
            signalResult.exceptionOrNull()?.let { errors.add("Signals: ${it.message}") }
            newsResult.exceptionOrNull()?.let { errors.add("News: ${it.message}") }
            communityResult.exceptionOrNull()?.let { errors.add("Community: ${it.message}") }
            vipResult.exceptionOrNull()?.let { errors.add("VIP: ${it.message}") }
            Result.failure(Exception(errors.joinToString("; ")))
        }
    }

    // ── Entity Mappers ────────────────────────────────

    private fun SignalResponse.toEntity(): SignalEntity {
        return SignalEntity(
            id = id,
            no = no,
            date = date,
            pair = pair,
            type = direction,
            entry = formatEntry(entry1, entry2),
            tp1 = formatPrice(tp1),
            tp2 = formatPrice(tp2),
            tp3 = formatPrice(tp3),
            tp4 = formatPrice(tp4),
            sl = formatPrice(sl),
            pips = (pips ?: 0.0).toInt(),
            profit = profit ?: 0.0,
            hitLevel = hitLevel ?: "NONE",
            status = status,
            result = result,
            thumbsCount = thumbsCount,
            fireCount = fireCount,
            rocketCount = rocketCount,
            brokenHeartCount = brokenHeartCount
        )
    }

    private fun NewsResponse.toEntity(): NewsEntity {
        val tsMs = parseDateTime(eventTime)
        return NewsEntity(
            id = id,
            time = formatNewsTime(eventTime, tsMs),
            currency = currency,
            title = title,
            impact = impact ?: "LOW",
            forecast = forecast ?: "-",
            previous = previous ?: "-",
            actual = actual ?: "-",
            description = description ?: "",
            timestamp = tsMs
        )
    }

    private fun CommunityPostResponse.toEntity(): CommunityPostEntity {
        return CommunityPostEntity(
            id = id,
            authorName = authorName,
            authorBadge = authorBadge ?: "Trader",
            authorAvatarHex = authorAvatarHex ?: 0xFF10B981,
            postType = postType,
            timestamp = System.currentTimeMillis(),
            content = content ?: "",
            hashtags = hashtags ?: "",
            imageUri = imageUri,
            pair = pair ?: "XAU/USD",
            tradeType = tradeType ?: "BUY",
            entryPrice = entryPrice ?: "",
            exitPrice = exitPrice ?: "",
            lotSize = lotSize ?: "",
            profitAmount = profitAmount ?: 0.0,
            pipsGain = pipsGain ?: 0,
            roiPercentage = roiPercentage ?: 0.0,
            brokerName = brokerName ?: "Exness",
            cardTheme = cardTheme ?: "EMERALD_NEON",
            isVerifiedTrade = isVerifiedTrade,
            likesCount = likesCount,
            fireCount = fireCount,
            rocketCount = rocketCount,
            commentsCount = commentsCount,
            isPinned = isPinned,
            status = status,
            rejectionReason = rejectionReason,
            commentsNeedReview = commentsNeedReview
        )
    }

    private fun CommentResponse.toEntity(postId: Long): CommunityCommentEntity {
        return CommunityCommentEntity(
            id = id,
            postId = postId,
            authorName = authorName,
            content = content,
            timestamp = System.currentTimeMillis(),
            likesCount = likesCount,
            status = status
        )
    }

    private fun VipMemberResponse.toEntity(): VipMemberEntity {
        val accentColor = when (rank) {
            1 -> 0xFFF59E0B
            2 -> 0xFFE2E8F0
            3 -> 0xFFF97316
            4 -> 0xFF6366F1
            5 -> 0xFFD946EF
            6 -> 0xFF10B981
            7 -> 0xFFEF4444
            8 -> 0xFF06B6D4
            9 -> 0xFFEAB308
            else -> 0xFF8B5CF6
        }

        return VipMemberEntity(
            id = id,
            rank = rank ?: 1,
            name = name,
            memberId = memberId,
            lots = lots ?: 0.0,
            progressFraction = (progressFraction ?: 0.0).toFloat(),
            accentHex = accentColor,
            period = period ?: "MONTHLY",
            winRate = winRate ?: 75.0,
            totalTrades = totalTrades ?: 0,
            broker = broker ?: "Exness",
            favoritePair = favoritePair ?: "XAU/USD"
        )
    }

    private fun formatEntry(entry1: Double?, entry2: Double?): String {
        return if (entry2 != null && entry2 > 0) {
            "$entry1 / $entry2"
        } else {
            entry1?.toString() ?: ""
        }
    }

    private fun formatPrice(price: Double?): String {
        return price?.toString() ?: ""
    }

    private fun parseDateTime(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val formats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        )
        for (format in formats) {
            try {
                val parsed = format.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) { }
        }
        return System.currentTimeMillis()
    }

    private fun formatNewsTime(dateStr: String?, timestampMs: Long): String {
        if (dateStr.isNullOrBlank()) return ""
        val slTimeZone = java.util.TimeZone.getTimeZone("Asia/Colombo")
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).apply { timeZone = slTimeZone }
        val dayFormat = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.US).apply { timeZone = slTimeZone }
        val nowCal = java.util.Calendar.getInstance(slTimeZone)
        val eventCal = java.util.Calendar.getInstance(slTimeZone).apply { timeInMillis = timestampMs }

        val timeStr = timeFormat.format(java.util.Date(timestampMs))

        val isToday = eventCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                eventCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)

        val tomorrowCal = java.util.Calendar.getInstance(slTimeZone).apply {
            timeInMillis = nowCal.timeInMillis
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val isTomorrow = eventCal.get(java.util.Calendar.YEAR) == tomorrowCal.get(java.util.Calendar.YEAR) &&
                eventCal.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCal.get(java.util.Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today \u2022 $timeStr (SLST)"
            isTomorrow -> "Tomorrow \u2022 $timeStr (SLST)"
            else -> "${dayFormat.format(java.util.Date(timestampMs))} \u2022 $timeStr (SLST)"
        }
    }

    private fun parseSignalFromMap(data: Map<*, *>): SignalEntity? {
        return try {
            SignalEntity(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                no = (data["no"] as? Number)?.toInt() ?: 0,
                date = data["date"] as? String ?: "",
                pair = data["pair"] as? String ?: "",
                type = data["direction"] as? String ?: "BUY",
                entry = formatEntry(
                    (data["entry1"] as? Number)?.toDouble(),
                    (data["entry2"] as? Number)?.toDouble()
                ),
                tp1 = formatPrice((data["tp1"] as? Number)?.toDouble()),
                tp2 = formatPrice((data["tp2"] as? Number)?.toDouble()),
                tp3 = formatPrice((data["tp3"] as? Number)?.toDouble()),
                tp4 = formatPrice((data["tp4"] as? Number)?.toDouble()),
                sl = formatPrice((data["sl"] as? Number)?.toDouble()),
                pips = ((data["pips"] as? Number)?.toDouble() ?: 0.0).toInt(),
                profit = (data["profit"] as? Number)?.toDouble() ?: 0.0,
                hitLevel = data["hit_level"] as? String ?: "NONE",
                status = data["status"] as? String ?: "active",
                result = data["result"] as? String ?: "RUNNING",
                thumbsCount = (data["thumbs_count"] as? Number)?.toInt() ?: 0,
                fireCount = (data["fire_count"] as? Number)?.toInt() ?: 0,
                rocketCount = (data["rocket_count"] as? Number)?.toInt() ?: 0,
                brokenHeartCount = (data["broken_heart_count"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePostFromMap(data: Map<*, *>): CommunityPostEntity? {
        return try {
            CommunityPostEntity(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                authorName = data["author_name"] as? String ?: "",
                authorBadge = data["author_badge"] as? String ?: "Trader",
                authorAvatarHex = (data["author_avatar_hex"] as? Number)?.toLong() ?: 0xFF10B981,
                postType = data["post_type"] as? String ?: "text",
                content = data["content"] as? String ?: "",
                hashtags = data["hashtags"] as? String ?: "",
                imageUri = data["image_uri"] as? String,
                pair = data["pair"] as? String ?: "XAU/USD",
                tradeType = data["trade_type"] as? String ?: "BUY",
                profitAmount = (data["profit_amount"] as? Number)?.toDouble() ?: 0.0,
                pipsGain = (data["pips_gain"] as? Number)?.toInt() ?: 0,
                likesCount = (data["likes_count"] as? Number)?.toInt() ?: 0,
                fireCount = (data["fire_count"] as? Number)?.toInt() ?: 0,
                rocketCount = (data["rocket_count"] as? Number)?.toInt() ?: 0,
                commentsCount = (data["comments_count"] as? Number)?.toInt() ?: 0,
                isPinned = data["is_pinned"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            null
        }
    }
}

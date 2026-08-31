package com.widhura.signalxp.data

import kotlinx.coroutines.flow.Flow

class SignalRepository(
    private val signalDao: SignalDao,
    private val newsDao: NewsDao,
    private val communityDao: CommunityDao,
    private val vipMemberDao: VipMemberDao
) {
    val allSignals: Flow<List<SignalEntity>> = signalDao.getAllSignals()
    val allNews: Flow<List<NewsEntity>> = newsDao.getAllNews()
    val allCommunityPosts: Flow<List<CommunityPostEntity>> = communityDao.getAllPosts()
    val allVipMembers: Flow<List<VipMemberEntity>> = vipMemberDao.getAllVipMembers()

    suspend fun insertSignal(signal: SignalEntity): Long {
        return signalDao.insertSignal(signal)
    }

    suspend fun updateSignal(signal: SignalEntity) {
        signalDao.updateSignal(signal)
    }

    suspend fun deleteSignal(id: Long) {
        signalDao.deleteSignalById(id)
    }

    suspend fun insertNews(news: NewsEntity): Long {
        return newsDao.insertNews(news)
    }

    suspend fun replaceAllNews(newsList: List<NewsEntity>) {
        newsDao.deleteAllNews()
        newsDao.insertAll(newsList)
    }

    // Community methods
    fun getCommentsForPost(postId: Long): Flow<List<CommunityCommentEntity>> {
        return communityDao.getCommentsForPost(postId)
    }

    suspend fun insertCommunityPost(post: CommunityPostEntity): Long {
        return communityDao.insertPost(post)
    }

    suspend fun updateCommunityPost(post: CommunityPostEntity) {
        communityDao.updatePost(post)
    }

    suspend fun deleteCommunityPost(id: Long) {
        communityDao.deletePostById(id)
    }

    suspend fun insertComment(comment: CommunityCommentEntity): Long {
        val id = communityDao.insertComment(comment)
        communityDao.incrementCommentCount(comment.postId)
        return id
    }

    suspend fun seedInitialDataIfEmpty() {
        if (signalDao.getCount() == 0) {
            val initialSignals = listOf(
                SignalEntity(
                    no = 1,
                    date = "2026-08-09",
                    pair = "EUR/USD",
                    type = "SELL",
                    entry = "1.0850",
                    tp1 = "1.0830", tp2 = "1.0810", tp3 = "1.0790", tp4 = "1.0770",
                    sl = "1.0880",
                    pips = 80,
                    profit = 80.00,
                    hitLevel = "4",
                    status = "ALL TARGETS HIT 🔥",
                    result = "WIN",
                    thumbsCount = 45,
                    fireCount = 89,
                    rocketCount = 34,
                    brokenHeartCount = 0
                ),
                SignalEntity(
                    no = 2,
                    date = "2026-08-09",
                    pair = "XAU/USD",
                    type = "BUY",
                    entry = "4122 / 4120",
                    tp1 = "4125", tp2 = "4130", tp3 = "4135", tp4 = "4140",
                    sl = "4115",
                    pips = 100,
                    profit = 100.00,
                    hitLevel = "2",
                    status = "RUNNING (TP2 HIT)",
                    result = "WIN",
                    thumbsCount = 15,
                    fireCount = 28,
                    rocketCount = 12,
                    brokenHeartCount = 0
                ),
                SignalEntity(
                    no = 3,
                    date = "2026-08-05",
                    pair = "GBP/JPY",
                    type = "SELL",
                    entry = "188.50",
                    tp1 = "188.20", tp2 = "187.80", tp3 = "187.40", tp4 = "187.00",
                    sl = "189.00",
                    pips = -50,
                    profit = -50.00,
                    hitLevel = "SL",
                    status = "STOP LOSS HIT 🛑",
                    result = "LOSS",
                    thumbsCount = 0,
                    fireCount = 0,
                    rocketCount = 0,
                    brokenHeartCount = 14
                )
            )
            signalDao.insertAll(initialSignals)
        }

        if (newsDao.getCount() == 0) {
            val nowMs = System.currentTimeMillis()
            val dayMs = 86400000L
            val initialNews = listOf(
                NewsEntity(
                    time = "Today • 06:00 PM (SLST)",
                    currency = "USD",
                    title = "Core CPI m/m (Consumer Price Index)",
                    impact = "HIGH",
                    forecast = "0.3%",
                    previous = "0.2%",
                    actual = "0.3%",
                    description = "Key inflation measure for USD. High volatility expected on XAU/USD and major USD pairs.",
                    timestamp = nowMs + 1000
                ),
                NewsEntity(
                    time = "Today • 06:00 PM (SLST)",
                    currency = "USD",
                    title = "CPI y/y (Annual Inflation)",
                    impact = "HIGH",
                    forecast = "3.1%",
                    previous = "3.0%",
                    actual = "3.1%",
                    description = "Measures price change of goods and services purchased by consumers.",
                    timestamp = nowMs + 2000
                ),
                NewsEntity(
                    time = "Today • 07:30 PM (SLST)",
                    currency = "USD",
                    title = "Federal Funds Rate & FOMC Statement",
                    impact = "HIGH",
                    forecast = "5.25%",
                    previous = "5.25%",
                    actual = "5.25%",
                    description = "Major central bank interest rate decision. Heavy market impact across all pairs!",
                    timestamp = nowMs + 3000
                ),
                NewsEntity(
                    time = "Tomorrow • 06:00 PM (SLST)",
                    currency = "USD",
                    title = "Non-Farm Employment Change (NFP)",
                    impact = "HIGH",
                    forecast = "165K",
                    previous = "142K",
                    actual = "-",
                    description = "Monthly non-farm job creation figure. Expected to cause high momentum spikes.",
                    timestamp = nowMs + dayMs
                ),
                NewsEntity(
                    time = "Tomorrow • 06:00 PM (SLST)",
                    currency = "USD",
                    title = "Unemployment Rate",
                    impact = "HIGH",
                    forecast = "4.2%",
                    previous = "4.3%",
                    actual = "-",
                    description = "Percentage of total work force that is unemployed and actively seeking employment.",
                    timestamp = nowMs + dayMs + 1000
                ),
                NewsEntity(
                    time = "Thu • 05:45 PM (SLST)",
                    currency = "EUR",
                    title = "ECB Main Refinancing Rate",
                    impact = "HIGH",
                    forecast = "3.65%",
                    previous = "3.90%",
                    actual = "-",
                    description = "European Central Bank interest rate announcement for the Eurozone.",
                    timestamp = nowMs + (dayMs * 2)
                ),
                NewsEntity(
                    time = "Thu • 07:30 PM (SLST)",
                    currency = "GBP",
                    title = "Official Bank Rate & MPC Summary",
                    impact = "HIGH",
                    forecast = "5.00%",
                    previous = "5.00%",
                    actual = "-",
                    description = "Bank of England interest rate decision affecting all GBP pairs.",
                    timestamp = nowMs + (dayMs * 2) + 1000
                ),
                NewsEntity(
                    time = "Fri • 06:00 PM (SLST)",
                    currency = "CAD",
                    title = "Employment Change & Unemployment Rate",
                    impact = "HIGH",
                    forecast = "25.0K",
                    previous = "-2.8K",
                    actual = "-",
                    description = "Canadian labor market data release. Strong impact on USD/CAD and CAD/JPY.",
                    timestamp = nowMs + (dayMs * 3)
                ),
                NewsEntity(
                    time = "Fri • 07:00 AM (SLST)",
                    currency = "AUD",
                    title = "RBA Cash Rate Statement",
                    impact = "HIGH",
                    forecast = "4.35%",
                    previous = "4.35%",
                    actual = "-",
                    description = "Reserve Bank of Australia interest rate release and monetary policy commentary.",
                    timestamp = nowMs + (dayMs * 3) + 1000
                ),
                NewsEntity(
                    time = "Mon • 10:00 AM (SLST)",
                    currency = "EUR",
                    title = "German Flash Manufacturing PMI",
                    impact = "MEDIUM",
                    forecast = "45.8",
                    previous = "45.5",
                    actual = "-",
                    description = "Leading economic indicator for German manufacturing health.",
                    timestamp = nowMs + (dayMs * 4)
                )
            )
            newsDao.insertAll(initialNews)
        }

        if (communityDao.getPostCount() == 0) {
            val now = System.currentTimeMillis()
            val hourMs = 3600000L
            val post1 = CommunityPostEntity(
                id = 1,
                authorName = "Kasun Perera",
                authorBadge = "VIP Master Trader",
                authorAvatarHex = 0xFFF59E0B,
                postType = "SCREENSHOT_POST",
                timestamp = now - (hourMs * 2),
                content = "Gold signal #2 was pure sniper entry! Hit TP2 effortlessly and locked +$1,650 in profit! Huge gratitude to Signal Xpress team! 🚀🔥",
                hashtags = "#XAUUSD #TP2Hit #ForexProfit #Screenshot",
                imageUri = "res://drawable/img_gold_profit_shot",
                pair = "XAU/USD",
                tradeType = "BUY",
                entryPrice = "4122.00",
                exitPrice = "4138.50",
                lotSize = "1.00",
                profitAmount = 1650.00,
                pipsGain = 165,
                roiPercentage = 165.0,
                brokerName = "Exness Pro",
                cardTheme = "GOLD_LUXURY",
                isVerifiedTrade = true,
                likesCount = 124,
                fireCount = 88,
                rocketCount = 52,
                commentsCount = 3,
                isPinned = true
            )

            val post2 = CommunityPostEntity(
                id = 2,
                authorName = "Dinuka Silva",
                authorBadge = "Pro Scalper",
                authorAvatarHex = 0xFF10B981,
                postType = "SCREENSHOT_POST",
                timestamp = now - (hourMs * 4),
                content = "EUR/USD Technical breakdown on 15M chart! Clean support retest and sell continuation as predicted.",
                hashtags = "#EURUSD #PriceAction #ChartAnalysis",
                imageUri = "res://drawable/img_chart_analysis_shot",
                pair = "EUR/USD",
                tradeType = "SELL",
                entryPrice = "1.0850",
                exitPrice = "1.0770",
                lotSize = "0.50",
                profitAmount = 400.00,
                pipsGain = 80,
                roiPercentage = 80.0,
                brokerName = "IC Markets Raw",
                cardTheme = "EMERALD_NEON",
                isVerifiedTrade = true,
                likesCount = 76,
                fireCount = 45,
                rocketCount = 19,
                commentsCount = 2,
                isPinned = false
            )

            val post3 = CommunityPostEntity(
                id = 3,
                authorName = "Ruwan Chamara",
                authorBadge = "Senior Analyst",
                authorAvatarHex = 0xFF38BDF8,
                postType = "IDEA_DISCUSSION",
                timestamp = now - (hourMs * 6),
                content = "US CPI High Impact News today at 6:00 PM SLST. Gold (XAU/USD) is respecting the 4,120 support zone strongly. Expect heavy volatility! Always lock TP1 profits and shift stop loss to breakeven before news release.",
                hashtags = "#CPI #GoldAnalysis #RiskManagement #SLST",
                imageUri = null,
                pair = "XAU/USD",
                tradeType = "BUY",
                likesCount = 98,
                fireCount = 41,
                rocketCount = 18,
                commentsCount = 3,
                isPinned = false
            )

            val post4 = CommunityPostEntity(
                id = 4,
                authorName = "Ashan Wijesinghe",
                authorBadge = "Gold Scalper",
                authorAvatarHex = 0xFF8B5CF6,
                postType = "SCREENSHOT_POST",
                timestamp = now - (hourMs * 8),
                content = "Quick scalping profit on today's Gold London open bounce! +$285.00 booked into wallet. Discipline is key 🎯",
                hashtags = "#XAUUSD #DailyTarget #Scalping",
                imageUri = "res://drawable/img_gold_profit_shot",
                pair = "XAU/USD",
                tradeType = "BUY",
                entryPrice = "4120.50",
                exitPrice = "4130.00",
                lotSize = "0.30",
                profitAmount = 285.00,
                pipsGain = 95,
                roiPercentage = 95.0,
                brokerName = "XM Ultra Low",
                cardTheme = "CYBER_SKY",
                isVerifiedTrade = true,
                likesCount = 52,
                fireCount = 31,
                rocketCount = 12,
                commentsCount = 1,
                isPinned = false
            )

            communityDao.insertPosts(listOf(post1, post2, post3, post4))

            // Seed some comments
            val comments = listOf(
                CommunityCommentEntity(
                    postId = 1,
                    authorName = "Nalaka FX",
                    content = "Super trade bro! I caught 100 pips on this one too! 🔥",
                    timestamp = now - (hourMs * 1)
                ),
                CommunityCommentEntity(
                    postId = 1,
                    authorName = "Sameera L.",
                    content = "Congratulations Kasun! Exness execution was blazing fast today.",
                    timestamp = now - (hourMs / 2)
                ),
                CommunityCommentEntity(
                    postId = 1,
                    authorName = "Admin (Signal Xpress)",
                    content = "Great profit lock! Remember to maintain 1-2% risk per position team.",
                    timestamp = now - 600000L
                ),
                CommunityCommentEntity(
                    postId = 2,
                    authorName = "Tharindu M.",
                    content = "London breakout strategy worked perfectly on EUR/USD! 👏",
                    timestamp = now - (hourMs * 3)
                ),
                CommunityCommentEntity(
                    postId = 3,
                    authorName = "Mahesh",
                    content = "Thanks for the reminder about CPI timings. Will wait for news candles to settle.",
                    timestamp = now - (hourMs * 5)
                )
            )
            communityDao.insertComments(comments)
        }

        if (vipMemberDao.getCount() == 0) {
            val top10Members = listOf(
                VipMemberEntity(
                    rank = 1,
                    name = "Prabath manjula",
                    memberId = "SX1043",
                    lots = 81.15,
                    progressFraction = 0.88f,
                    accentHex = 0xFFF59E0B, // Gold / Yellow
                    period = "MONTHLY",
                    winRate = 86.4,
                    totalTrades = 128,
                    broker = "Exness Raw Spread",
                    favoritePair = "XAU/USD"
                ),
                VipMemberEntity(
                    rank = 2,
                    name = "Unknown",
                    memberId = "—",
                    lots = 36.66,
                    progressFraction = 0.44f,
                    accentHex = 0xFFE2E8F0, // Silver / Light Slate
                    period = "MONTHLY",
                    winRate = 79.2,
                    totalTrades = 64,
                    broker = "XM Ultra Low",
                    favoritePair = "EUR/USD"
                ),
                VipMemberEntity(
                    rank = 3,
                    name = "Unknown",
                    memberId = "—",
                    lots = 21.13,
                    progressFraction = 0.26f,
                    accentHex = 0xFFF97316, // Bronze / Orange
                    period = "MONTHLY",
                    winRate = 75.0,
                    totalTrades = 48,
                    broker = "IC Markets Pro",
                    favoritePair = "GBP/USD"
                ),
                VipMemberEntity(
                    rank = 4,
                    name = "Asitha lakmal",
                    memberId = "SX1029",
                    lots = 17.62,
                    progressFraction = 0.21f,
                    accentHex = 0xFF6366F1, // Indigo / Royal Blue
                    period = "MONTHLY",
                    winRate = 81.5,
                    totalTrades = 39,
                    broker = "Exness Pro",
                    favoritePair = "XAU/USD"
                ),
                VipMemberEntity(
                    rank = 5,
                    name = "Tharindu manoj",
                    memberId = "SX1036",
                    lots = 13.86,
                    progressFraction = 0.17f,
                    accentHex = 0xFFD946EF, // Magenta / Purple
                    period = "MONTHLY",
                    winRate = 72.8,
                    totalTrades = 31,
                    broker = "Pepperstone Razor",
                    favoritePair = "USD/JPY"
                ),
                VipMemberEntity(
                    rank = 6,
                    name = "Unknown",
                    memberId = "—",
                    lots = 11.07,
                    progressFraction = 0.13f,
                    accentHex = 0xFF10B981, // Mint / Emerald
                    period = "MONTHLY",
                    winRate = 68.4,
                    totalTrades = 25,
                    broker = "Exness Standard",
                    favoritePair = "XAU/USD"
                ),
                VipMemberEntity(
                    rank = 7,
                    name = "Unknown",
                    memberId = "—",
                    lots = 10.10,
                    progressFraction = 0.12f,
                    accentHex = 0xFFEF4444, // Coral / Red
                    period = "MONTHLY",
                    winRate = 70.0,
                    totalTrades = 22,
                    broker = "XM Standard",
                    favoritePair = "GBP/JPY"
                ),
                VipMemberEntity(
                    rank = 8,
                    name = "Roshan",
                    memberId = "SX1081",
                    lots = 9.63,
                    progressFraction = 0.11f,
                    accentHex = 0xFF06B6D4, // Cyan / Teal
                    period = "MONTHLY",
                    winRate = 76.5,
                    totalTrades = 19,
                    broker = "Exness Pro",
                    favoritePair = "EUR/USD"
                ),
                VipMemberEntity(
                    rank = 9,
                    name = "Vidya Karunaratne",
                    memberId = "SX1003",
                    lots = 7.17,
                    progressFraction = 0.09f,
                    accentHex = 0xFFEAB308, // Gold / Yellow
                    period = "MONTHLY",
                    winRate = 83.3,
                    totalTrades = 16,
                    broker = "IC Markets",
                    favoritePair = "XAU/USD"
                ),
                VipMemberEntity(
                    rank = 10,
                    name = "Unknown",
                    memberId = "—",
                    lots = 6.06,
                    progressFraction = 0.07f,
                    accentHex = 0xFF8B5CF6, // Lavender / Violet
                    period = "MONTHLY",
                    winRate = 66.7,
                    totalTrades = 14,
                    broker = "Exness Standard",
                    favoritePair = "AUD/USD"
                )
            )
            vipMemberDao.insertAll(top10Members)
        }
    }

    suspend fun insertVipMember(member: VipMemberEntity): Long {
        return vipMemberDao.insertVipMember(member)
    }

    suspend fun replaceAllVipMembers(members: List<VipMemberEntity>) {
        vipMemberDao.deleteAllVipMembers()
        vipMemberDao.insertAll(members)
    }

    suspend fun updateVipMember(member: VipMemberEntity) {
        vipMemberDao.updateVipMember(member)
    }

    suspend fun deleteVipMember(id: Long) {
        vipMemberDao.deleteVipMemberById(id)
    }
}

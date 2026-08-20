package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.CommunityCommentEntity
import com.example.data.CommunityPostEntity
import com.example.data.NewsEntity
import com.example.data.SignalEntity
import com.example.data.SignalRepository
import com.example.data.VipMemberEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class TimeFilter { ALL, DAILY, WEEKLY, MONTHLY, CUSTOM }
enum class ResultFilter { ALL, WIN, LOSS }
enum class CommunityFilter { ALL, SCREENSHOTS, PROFIT_CARDS, DISCUSSIONS, TOP_GAINERS, TOP_PROFITS }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SignalRepository
    val okHttpClient = OkHttpClient()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SignalRepository(db.signalDao(), db.newsDao(), db.communityDao(), db.vipMemberDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Raw signals flow from Room
    val rawSignals: StateFlow<List<SignalEntity>> = repository.allSignals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Raw news flow from Room
    val rawNews: StateFlow<List<NewsEntity>> = repository.allNews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Forex Factory News Filters & Syncing
    private val _newsCurrencyFilter = MutableStateFlow("ALL")
    val newsCurrencyFilter: StateFlow<String> = _newsCurrencyFilter.asStateFlow()

    private val _newsImpactFilter = MutableStateFlow("ALL")
    val newsImpactFilter: StateFlow<String> = _newsImpactFilter.asStateFlow()

    private val _isSyncingNews = MutableStateFlow(false)
    val isSyncingNews: StateFlow<Boolean> = _isSyncingNews.asStateFlow()

    val filteredNews: StateFlow<List<NewsEntity>> = combine(
        rawNews,
        _newsCurrencyFilter,
        _newsImpactFilter
    ) { newsList, currencyFilter, impactFilter ->
        var list = newsList
        if (currencyFilter != "ALL") {
            list = list.filter { it.currency.equals(currencyFilter, ignoreCase = true) }
        }
        if (impactFilter != "ALL") {
            list = list.filter { it.impact.equals(impactFilter, ignoreCase = true) }
        }

        val slTimeZone = TimeZone.getTimeZone("Asia/Colombo")
        val todayStartMs = Calendar.getInstance(slTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Exclude past news (events before today in SLST)
        val todayAndUpcoming = list.filter { it.timestamp >= todayStartMs }

        // Sort chronologically: Today's events first (earliest to latest), then tomorrow, then rest of the week
        todayAndUpcoming.sortedBy { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setNewsCurrencyFilter(currency: String) {
        _newsCurrencyFilter.value = currency
    }

    fun setNewsImpactFilter(impact: String) {
        _newsImpactFilter.value = impact
    }

    fun syncForexFactoryNews() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncingNews.value = true
            try {
                val url = "https://nfp.ourforecast.com/calendar.json"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept", "application/json")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val jsonStr = response.body?.string() ?: ""

                if (response.isSuccessful && jsonStr.isNotBlank() && jsonStr.trim().startsWith("[")) {
                    val jsonArray = JSONArray(jsonStr)
                    val fetchedList = mutableListOf<NewsEntity>()

                    val slTimeZone = TimeZone.getTimeZone("Asia/Colombo")
                    val sdfInput1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                    val sdfInput2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)

                    val timeFormatSL = SimpleDateFormat("hh:mm a", Locale.US).apply { timeZone = slTimeZone }
                    val dayFormatSL = SimpleDateFormat("EEE, MMM d", Locale.US).apply { timeZone = slTimeZone }

                    val nowCalSL = Calendar.getInstance(slTimeZone)

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val title = obj.optString("title", "Economic Event")
                        val country = obj.optString("country", "USD").uppercase()
                        val impactRaw = obj.optString("impact", "Low").uppercase()
                        val forecast = obj.optString("forecast", "-")
                        val previous = obj.optString("previous", "-")
                        val actual = obj.optString("actual", "-")
                        val dateStr = obj.optString("date", "")

                        var parsedDate: Date? = null
                        if (dateStr.isNotEmpty()) {
                            try {
                                parsedDate = sdfInput1.parse(dateStr)
                            } catch (e: Exception) {
                                try {
                                    parsedDate = sdfInput2.parse(dateStr)
                                } catch (e2: Exception) {
                                    parsedDate = null
                                }
                            }
                        }

                        val eventTimeMs = parsedDate?.time ?: System.currentTimeMillis()

                        var formattedTimeStr = "Today"
                        if (parsedDate != null) {
                            val eventCal = Calendar.getInstance(slTimeZone).apply { time = parsedDate }
                            val isToday = eventCal.get(Calendar.YEAR) == nowCalSL.get(Calendar.YEAR) &&
                                    eventCal.get(Calendar.DAY_OF_YEAR) == nowCalSL.get(Calendar.DAY_OF_YEAR)

                            val tomorrowCal = Calendar.getInstance(slTimeZone).apply {
                                timeInMillis = nowCalSL.timeInMillis
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            val isTomorrow = eventCal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
                                    eventCal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)

                            val timeFormatted = timeFormatSL.format(parsedDate)
                            formattedTimeStr = when {
                                isToday -> "Today • $timeFormatted (SLST)"
                                isTomorrow -> "Tomorrow • $timeFormatted (SLST)"
                                else -> "${dayFormatSL.format(parsedDate)} • $timeFormatted (SLST)"
                            }
                        } else {
                            formattedTimeStr = "$dateStr (SLST)"
                        }

                        val impact = when {
                            impactRaw.contains("HIGH") -> "HIGH"
                            impactRaw.contains("MED") -> "MEDIUM"
                            else -> "LOW"
                        }

                        fetchedList.add(
                            NewsEntity(
                                time = formattedTimeStr,
                                currency = country,
                                title = title,
                                impact = impact,
                                forecast = if (forecast.isBlank()) "-" else forecast,
                                previous = if (previous.isBlank()) "-" else previous,
                                actual = if (actual.isBlank()) "-" else actual,
                                description = "Forex Factory Calendar ($country). Potential market volatility during release.",
                                timestamp = eventTimeMs
                            )
                        )
                    }

                    if (fetchedList.isNotEmpty()) {
                        repository.replaceAllNews(fetchedList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncingNews.value = false
            }
        }
    }

    // UI Filters
    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.ALL)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _customDate = MutableStateFlow("") // YYYY-MM-DD
    val customDate: StateFlow<String> = _customDate.asStateFlow()

    private val _selectedResultFilter = MutableStateFlow(ResultFilter.ALL)
    val selectedResultFilter: StateFlow<ResultFilter> = _selectedResultFilter.asStateFlow()

    private val _selectedPairFilter = MutableStateFlow("ALL")
    val selectedPairFilter: StateFlow<String> = _selectedPairFilter.asStateFlow()

    // AI Analysis State
    private val _aiAnalysisResult = MutableStateFlow<String?>(null)
    val aiAnalysisResult: StateFlow<String?> = _aiAnalysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Filtered signals based on selected time filter & pair
    val filteredSignals: StateFlow<List<SignalEntity>> = combine(
        rawSignals,
        _selectedTimeFilter,
        _customDate,
        _selectedPairFilter
    ) { signals, timeFilter, customDateStr, pairFilter ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())

        var result = signals

        if (pairFilter != "ALL") {
            result = result.filter { it.pair.equals(pairFilter, ignoreCase = true) }
        }

        when (timeFilter) {
            TimeFilter.ALL -> result
            TimeFilter.DAILY -> result.filter { it.date == todayStr }
            TimeFilter.WEEKLY -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = cal.time
                result.filter {
                    try {
                        val d = sdf.parse(it.date)
                        d != null && (d.after(sevenDaysAgo) || d == sevenDaysAgo)
                    } catch (e: Exception) {
                        true
                    }
                }
            }
            TimeFilter.MONTHLY -> {
                val cal = Calendar.getInstance()
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear = cal.get(Calendar.YEAR)
                result.filter {
                    try {
                        val d = sdf.parse(it.date)
                        if (d != null) {
                            val signalCal = Calendar.getInstance()
                            signalCal.time = d
                            signalCal.get(Calendar.MONTH) == currentMonth && signalCal.get(Calendar.YEAR) == currentYear
                        } else false
                    } catch (e: Exception) {
                        true
                    }
                }
            }
            TimeFilter.CUSTOM -> {
                if (customDateStr.isNotBlank()) {
                    result.filter { it.date == customDateStr }
                } else {
                    result
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered signals for the Analytics Ledger Table
    val ledgerSignals: StateFlow<List<SignalEntity>> = combine(
        filteredSignals,
        _selectedResultFilter
    ) { signals, resultFilter ->
        when (resultFilter) {
            ResultFilter.ALL -> signals
            ResultFilter.WIN -> signals.filter { it.result.uppercase() == "WIN" }
            ResultFilter.LOSS -> signals.filter { it.result.uppercase() == "LOSS" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User actions
    fun setTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
        if (filter != TimeFilter.CUSTOM) {
            _customDate.value = ""
        }
    }

    fun setCustomDate(dateStr: String) {
        _customDate.value = dateStr
        _selectedTimeFilter.value = TimeFilter.CUSTOM
    }

    fun setResultFilter(filter: ResultFilter) {
        _selectedResultFilter.value = filter
    }

    fun setPairFilter(pair: String) {
        _selectedPairFilter.value = pair
    }

    fun toggleReaction(signal: SignalEntity, emoji: String) {
        viewModelScope.launch {
            val isCurrentSame = signal.userReactedEmoji == emoji
            var newThumbs = signal.thumbsCount
            var newFire = signal.fireCount
            var newRocket = signal.rocketCount
            var newHeart = signal.brokenHeartCount

            // Remove previous reaction if any
            signal.userReactedEmoji?.let { prev ->
                when (prev) {
                    "👍" -> if (newThumbs > 0) newThumbs--
                    "🔥" -> if (newFire > 0) newFire--
                    "🚀" -> if (newRocket > 0) newRocket--
                    "💔" -> if (newHeart > 0) newHeart--
                }
            }

            val nextEmoji = if (isCurrentSame) null else emoji

            if (nextEmoji != null) {
                when (nextEmoji) {
                    "👍" -> newThumbs++
                    "🔥" -> newFire++
                    "🚀" -> newRocket++
                    "💔" -> newHeart++
                }
            }

            val updated = signal.copy(
                thumbsCount = newThumbs,
                fireCount = newFire,
                rocketCount = newRocket,
                brokenHeartCount = newHeart,
                userReactedEmoji = nextEmoji
            )
            repository.updateSignal(updated)
        }
    }

    fun updateSignalHitLevel(signal: SignalEntity, newHitLevel: String) {
        viewModelScope.launch {
            val (statusText, resultText, pipsVal, profitVal) = when (newHitLevel) {
                "1" -> Tuple4("TP1 HIT 🎯", "WIN", 20, 20.0)
                "2" -> Tuple4("RUNNING (TP2 HIT)", "WIN", 50, 50.0)
                "3" -> Tuple4("TP3 HIT 🔥", "WIN", 80, 80.0)
                "4" -> Tuple4("ALL TARGETS HIT 🔥", "WIN", 120, 120.0)
                "BE" -> Tuple4("BREAKEVEN HIT 🟡", "WIN", 0, 0.0)
                "SL" -> Tuple4("STOP LOSS HIT 🛑", "LOSS", -40, -40.0)
                "CLOSE" -> Tuple4("CLOSED MANUALLY ⏱️", "WIN", 10, 10.0)
                else -> Tuple4("RUNNING 📡", "RUNNING", 0, 0.0)
            }

            val updated = signal.copy(
                hitLevel = newHitLevel,
                status = statusText,
                result = resultText,
                pips = pipsVal,
                profit = profitVal
            )
            repository.updateSignal(updated)
        }
    }

    fun addNewSignal(
        pair: String,
        type: String,
        entry: String,
        tp1: String,
        tp2: String,
        tp3: String,
        tp4: String,
        sl: String
    ) {
        viewModelScope.launch {
            val count = rawSignals.value.size + 1
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val newSignal = SignalEntity(
                no = count,
                date = sdf.format(Date()),
                pair = pair.uppercase(),
                type = type.uppercase(),
                entry = entry,
                tp1 = tp1,
                tp2 = tp2,
                tp3 = tp3,
                tp4 = tp4,
                sl = sl,
                pips = 0,
                profit = 0.0,
                hitLevel = "NONE",
                status = "NEW SIGNAL BROADCAST 📡",
                result = "RUNNING"
            )
            repository.insertSignal(newSignal)
        }
    }

    fun addNewNews(
        time: String,
        currency: String,
        title: String,
        impact: String,
        forecast: String,
        previous: String,
        description: String
    ) {
        viewModelScope.launch {
            val news = NewsEntity(
                time = time,
                currency = currency.uppercase(),
                title = title,
                impact = impact.uppercase(),
                forecast = forecast,
                previous = previous,
                description = description
            )
            repository.insertNews(news)
        }
    }

    fun analyzeSignalWithGemini(signal: SignalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            _aiAnalysisResult.value = null

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                _aiAnalysisResult.value = "Gemini API key is not configured in Secrets. Here is an offline analysis preview:\n\n" +
                        "• Pair: ${signal.pair} (${signal.type})\n" +
                        "• Risk/Reward Ratio: High conviction setup based on standard ATR market volatility.\n" +
                        "• Strategy Recommendation: Use strictly 1-2% lot size risk management. Move Stop Loss to Breakeven once TP1 is hit!"
                _isAnalyzing.value = false
                return@launch
            }

            try {
                val promptText = "You are an expert Forex & Gold trading analyst. Analyze the following trading signal and provide brief, professional advice in simple English and Sinhala:\n" +
                        "Pair: ${signal.pair}\nType: ${signal.type}\nEntry: ${signal.entry}\nTPs: TP1 ${signal.tp1}, TP2 ${signal.tp2}, TP3 ${signal.tp3}, TP4 ${signal.tp4}\nSL: ${signal.sl}\nStatus: ${signal.status}\n" +
                        "Provide 3 bullet points on market sentiment, risk-to-reward advice, and money management guidelines."

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", promptText)
                                })
                            })
                        })
                    })
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseStr = response.body?.string()

                if (response.isSuccessful && responseStr != null) {
                    val jsonObj = JSONObject(responseStr)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "No response text.")
                            _aiAnalysisResult.value = text
                        } else {
                            _aiAnalysisResult.value = "Failed to parse AI response."
                        }
                    } else {
                        _aiAnalysisResult.value = "No response generated by AI."
                    }
                } else {
                    _aiAnalysisResult.value = "Gemini API error (${response.code})."
                }
            } catch (e: Exception) {
                _aiAnalysisResult.value = "Analysis failed: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearAiAnalysis() {
        _aiAnalysisResult.value = null
    }

    // ==========================================
    // COMMUNITY & PROFIT CARD SHARING HUB
    // ==========================================

    val rawCommunityPosts: StateFlow<List<CommunityPostEntity>> = repository.allCommunityPosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _communityFilter = MutableStateFlow(CommunityFilter.ALL)
    val communityFilter: StateFlow<CommunityFilter> = _communityFilter.asStateFlow()

    private val _communitySearchQuery = MutableStateFlow("")
    val communitySearchQuery: StateFlow<String> = _communitySearchQuery.asStateFlow()

    private val _communityPairFilter = MutableStateFlow("ALL")
    val communityPairFilter: StateFlow<String> = _communityPairFilter.asStateFlow()

    // Filtered Community Posts
    val filteredCommunityPosts: StateFlow<List<CommunityPostEntity>> = combine(
        rawCommunityPosts,
        _communityFilter,
        _communitySearchQuery,
        _communityPairFilter
    ) { posts, filter, query, pair ->
        var result = posts

        if (pair != "ALL") {
            result = result.filter { it.pair.equals(pair, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.authorName.lowercase().contains(q) ||
                        it.content.lowercase().contains(q) ||
                        it.hashtags.lowercase().contains(q) ||
                        it.pair.lowercase().contains(q) ||
                        it.brokerName.lowercase().contains(q)
            }
        }

        when (filter) {
            CommunityFilter.ALL -> result
            CommunityFilter.SCREENSHOTS -> result.filter { !it.imageUri.isNullOrBlank() || it.postType == "SCREENSHOT_POST" }
            CommunityFilter.PROFIT_CARDS -> result.filter { it.postType == "PROFIT_CARD" || it.profitAmount > 0 }
            CommunityFilter.DISCUSSIONS -> result.filter { it.postType == "IDEA_DISCUSSION" || (it.imageUri.isNullOrBlank() && it.postType != "PROFIT_CARD") }
            CommunityFilter.TOP_GAINERS, CommunityFilter.TOP_PROFITS -> result.filter { it.profitAmount > 0 }
                .sortedByDescending { it.profitAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Top Profit Highlights for community banner
    val topProfitHighlights: StateFlow<List<CommunityPostEntity>> = rawCommunityPosts.combine(_communityFilter) { posts, _ ->
        posts.filter { it.profitAmount > 0 }
            .sortedByDescending { it.profitAmount }
            .take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setCommunityFilter(filter: CommunityFilter) {
        _communityFilter.value = filter
    }

    fun setCommunitySearchQuery(query: String) {
        _communitySearchQuery.value = query
    }

    fun setCommunityPairFilter(pair: String) {
        _communityPairFilter.value = pair
    }

    fun toggleCommunityLike(post: CommunityPostEntity) {
        viewModelScope.launch {
            val newIsLiked = !post.isLikedByMe
            val newCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
            repository.updateCommunityPost(post.copy(isLikedByMe = newIsLiked, likesCount = newCount))
        }
    }

    fun toggleCommunityFire(post: CommunityPostEntity) {
        viewModelScope.launch {
            val newIsFired = !post.isFiredByMe
            val newCount = if (newIsFired) post.fireCount + 1 else (post.fireCount - 1).coerceAtLeast(0)
            repository.updateCommunityPost(post.copy(isFiredByMe = newIsFired, fireCount = newCount))
        }
    }

    fun toggleCommunityRocket(post: CommunityPostEntity) {
        viewModelScope.launch {
            val newIsRocket = !post.isRocketByMe
            val newCount = if (newIsRocket) post.rocketCount + 1 else (post.rocketCount - 1).coerceAtLeast(0)
            repository.updateCommunityPost(post.copy(isRocketByMe = newIsRocket, rocketCount = newCount))
        }
    }

    fun getCommentsForPost(postId: Long): Flow<List<CommunityCommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    fun addComment(postId: Long, authorName: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val comment = CommunityCommentEntity(
                postId = postId,
                authorName = if (authorName.isBlank()) "Trader LK" else authorName.trim(),
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.insertComment(comment)
        }
    }

    fun createCommunityPost(
        authorName: String,
        authorBadge: String,
        authorAvatarHex: Long,
        content: String,
        imageUri: String?,
        pair: String = "XAU/USD",
        tradeType: String = "BUY",
        profitAmount: Double = 0.0,
        pipsGain: Int = 0,
        brokerName: String = "",
        hashtags: String = ""
    ) {
        if (content.isBlank() && imageUri.isNullOrBlank()) return
        viewModelScope.launch {
            val post = CommunityPostEntity(
                authorName = if (authorName.isBlank()) "Trader LK" else authorName.trim(),
                authorBadge = if (authorBadge.isBlank()) "Forex Trader" else authorBadge.trim(),
                authorAvatarHex = authorAvatarHex,
                postType = if (!imageUri.isNullOrBlank()) "SCREENSHOT_POST" else "IDEA_DISCUSSION",
                timestamp = System.currentTimeMillis(),
                content = content.trim(),
                imageUri = imageUri,
                hashtags = if (hashtags.isBlank()) {
                    if (pair.isNotBlank() && pair != "GENERAL") "#${pair.replace("/", "")} #ForexLK" else "#TradingCommunity"
                } else hashtags.trim(),
                pair = if (pair.isBlank()) "XAU/USD" else pair.uppercase(),
                tradeType = tradeType.uppercase(),
                profitAmount = profitAmount,
                pipsGain = pipsGain,
                brokerName = if (brokerName.isBlank()) "Exness" else brokerName.trim(),
                likesCount = 1,
                isLikedByMe = true,
                fireCount = 1,
                isFiredByMe = true
            )
            repository.insertCommunityPost(post)
        }
    }

    fun createProfitCardPost(
        authorName: String,
        authorBadge: String,
        authorAvatarHex: Long,
        pair: String,
        tradeType: String,
        entryPrice: String,
        exitPrice: String,
        lotSize: String,
        profitAmount: Double,
        pipsGain: Int,
        roiPercentage: Double,
        brokerName: String,
        cardTheme: String,
        caption: String,
        hashtags: String
    ) {
        viewModelScope.launch {
            val post = CommunityPostEntity(
                authorName = if (authorName.isBlank()) "Trader LK" else authorName.trim(),
                authorBadge = if (authorBadge.isBlank()) "VIP Member" else authorBadge.trim(),
                authorAvatarHex = authorAvatarHex,
                postType = "PROFIT_CARD",
                timestamp = System.currentTimeMillis(),
                content = caption.trim(),
                hashtags = if (hashtags.isBlank()) "#${pair.replace("/", "")} #Profit" else hashtags.trim(),
                pair = pair.uppercase(),
                tradeType = tradeType.uppercase(),
                entryPrice = entryPrice.trim(),
                exitPrice = exitPrice.trim(),
                lotSize = if (lotSize.isBlank()) "0.50" else lotSize.trim(),
                profitAmount = profitAmount,
                pipsGain = pipsGain,
                roiPercentage = roiPercentage,
                brokerName = if (brokerName.isBlank()) "Exness Pro" else brokerName.trim(),
                cardTheme = cardTheme,
                isVerifiedTrade = true,
                likesCount = 1,
                isLikedByMe = true,
                fireCount = 1,
                isFiredByMe = true
            )
            repository.insertCommunityPost(post)
        }
    }

    fun createDiscussionPost(
        authorName: String,
        authorBadge: String,
        authorAvatarHex: Long,
        content: String,
        pair: String,
        hashtags: String
    ) {
        if (content.isBlank()) return
        createCommunityPost(
            authorName = authorName,
            authorBadge = authorBadge,
            authorAvatarHex = authorAvatarHex,
            content = content,
            imageUri = null,
            pair = pair,
            hashtags = hashtags
        )
    }

    fun deleteCommunityPost(postId: Long) {
        viewModelScope.launch {
            repository.deleteCommunityPost(postId)
        }
    }

    // VIP Members Top 10 StateFlows & Web Sync
    private val prefs = application.getSharedPreferences("signal_xpress_vip_prefs", android.content.Context.MODE_PRIVATE)

    private val _vipWebUrl = MutableStateFlow(
        prefs.getString("vip_admin_web_url", "") ?: ""
    )
    val vipWebUrl: StateFlow<String> = _vipWebUrl.asStateFlow()

    private val _isSyncingVip = MutableStateFlow(false)
    val isSyncingVip: StateFlow<Boolean> = _isSyncingVip.asStateFlow()

    private val _lastVipSyncTime = MutableStateFlow(
        prefs.getString("vip_last_sync_time", "Live Auto-Sync Active") ?: "Live Auto-Sync Active"
    )
    val lastVipSyncTime: StateFlow<String> = _lastVipSyncTime.asStateFlow()

    val rawVipMembers: StateFlow<List<VipMemberEntity>> = repository.allVipMembers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _vipSearchQuery = MutableStateFlow("")
    val vipSearchQuery: StateFlow<String> = _vipSearchQuery.asStateFlow()

    private val _vipPeriodFilter = MutableStateFlow("MONTHLY") // "MONTHLY", "ALL_TIME", "WEEKLY"
    val vipPeriodFilter: StateFlow<String> = _vipPeriodFilter.asStateFlow()

    val filteredVipMembers: StateFlow<List<VipMemberEntity>> = combine(
        rawVipMembers,
        _vipSearchQuery,
        _vipPeriodFilter
    ) { members, query, period ->
        var list = if (period == "ALL_TIME") {
            members
        } else {
            members.filter { it.period.equals(period, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.memberId.lowercase().contains(q) ||
                        it.broker.lowercase().contains(q)
            }
        }
        list.sortedByDescending { it.lots }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setVipSearchQuery(query: String) {
        _vipSearchQuery.value = query
    }

    fun setVipPeriodFilter(period: String) {
        _vipPeriodFilter.value = period
    }

    fun setVipWebUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        _vipWebUrl.value = trimmed
        prefs.edit().putString("vip_admin_web_url", trimmed).apply()
    }

    fun syncVipLeaderboardFromWeb(
        overrideUrl: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        val urlToUse = overrideUrl?.trim() ?: _vipWebUrl.value.trim()
        if (urlToUse.isBlank()) {
            onComplete?.invoke(false, "Web URL is not set. Please set Admin Web URL in Admin Settings.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isSyncingVip.value = true
            try {
                val request = Request.Builder()
                    .url(urlToUse)
                    .header("User-Agent", "Mozilla/5.0 (Android; SignalXpress/1.0)")
                    .header("Accept", "application/json")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseCode = response.code
                val responseBodyStr = response.body?.string() ?: ""

                if (!response.isSuccessful || responseBodyStr.isBlank()) {
                    withContext(Dispatchers.Main) {
                        _isSyncingVip.value = false
                        onComplete?.invoke(false, "Server returned HTTP $responseCode: Invalid response.")
                    }
                    return@launch
                }

                val trimmed = responseBodyStr.trim()
                val jsonArray: JSONArray = when {
                    trimmed.startsWith("[") -> JSONArray(trimmed)
                    trimmed.startsWith("{") -> {
                        val rootObj = JSONObject(trimmed)
                        when {
                            rootObj.has("members") -> rootObj.getJSONArray("members")
                            rootObj.has("data") -> rootObj.getJSONArray("data")
                            rootObj.has("leaderboard") -> rootObj.getJSONArray("leaderboard")
                            rootObj.has("top10") -> rootObj.getJSONArray("top10")
                            rootObj.has("vips") -> rootObj.getJSONArray("vips")
                            rootObj.has("record") -> {
                                val rec = rootObj.get("record")
                                if (rec is JSONArray) rec
                                else if (rec is JSONObject && rec.has("members")) rec.getJSONArray("members")
                                else JSONArray()
                            }
                            else -> JSONArray()
                        }
                    }
                    else -> JSONArray()
                }

                if (jsonArray.length() == 0) {
                    withContext(Dispatchers.Main) {
                        _isSyncingVip.value = false
                        onComplete?.invoke(false, "No valid VIP member entries found in web JSON.")
                    }
                    return@launch
                }

                val parsedList = mutableListOf<VipMemberEntity>()
                var maxLots = 1.0

                // First pass to find max lots for progress bar scaling
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val lots = obj.optDouble("lots", obj.optDouble("volume", obj.optDouble("total_lots", 0.0)))
                    if (lots > maxLots) {
                        maxLots = lots
                    }
                }

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val name = obj.optString("name", obj.optString("trader", obj.optString("user", "Unknown")))
                    val memberId = obj.optString("memberId", obj.optString("member_id", obj.optString("id", obj.optString("code", "—"))))
                    val lots = obj.optDouble("lots", obj.optDouble("volume", obj.optDouble("total_lots", 0.0)))
                    val broker = obj.optString("broker", obj.optString("broker_name", "Exness VIP"))
                    val favoritePair = obj.optString("favoritePair", obj.optString("pair", obj.optString("top_asset", "XAU/USD")))
                    val winRate = obj.optDouble("winRate", obj.optDouble("win_rate", obj.optDouble("winrate", 75.0)))
                    val totalTrades = obj.optInt("totalTrades", obj.optInt("trades", obj.optInt("total_trades", 20)))
                    val period = obj.optString("period", obj.optString("timeframe", "MONTHLY")).uppercase()

                    val fraction = (lots / (maxLots * 1.12)).toFloat().coerceIn(0.05f, 0.95f)

                    val rankIndex = i + 1
                    val accentColor = when (rankIndex) {
                        1 -> 0xFFF59E0B // Gold
                        2 -> 0xFFE2E8F0 // Silver
                        3 -> 0xFFF97316 // Bronze
                        4 -> 0xFF6366F1 // Blue
                        5 -> 0xFFD946EF // Purple
                        6 -> 0xFF10B981 // Emerald
                        7 -> 0xFFEF4444 // Red
                        8 -> 0xFF06B6D4 // Cyan
                        9 -> 0xFFEAB308 // Amber
                        else -> 0xFF8B5CF6 // Lavender
                    }

                    parsedList.add(
                        VipMemberEntity(
                            rank = rankIndex,
                            name = if (name.isBlank()) "Unknown" else name.trim(),
                            memberId = if (memberId.isBlank()) "—" else memberId.trim(),
                            lots = lots,
                            progressFraction = fraction,
                            accentHex = accentColor,
                            period = period,
                            winRate = winRate,
                            totalTrades = totalTrades,
                            broker = broker,
                            favoritePair = favoritePair
                        )
                    )
                }

                if (parsedList.isNotEmpty()) {
                    repository.replaceAllVipMembers(parsedList)
                    val sdf = SimpleDateFormat("hh:mm a", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Colombo")
                    }
                    val syncTimeStr = "Synced at ${sdf.format(Date())} (SLST)"
                    _lastVipSyncTime.value = syncTimeStr
                    prefs.edit().putString("vip_last_sync_time", syncTimeStr).apply()

                    withContext(Dispatchers.Main) {
                        _isSyncingVip.value = false
                        onComplete?.invoke(true, "Successfully fetched ${parsedList.size} Top VIP Traders from Web!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _isSyncingVip.value = false
                        onComplete?.invoke(false, "Unable to parse VIP traders from JSON.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isSyncingVip.value = false
                    onComplete?.invoke(false, "Connection error: ${e.localizedMessage ?: "Failed to reach web source"}")
                }
            }
        }
    }

    fun addOrUpdateVipMember(
        name: String,
        memberId: String,
        lots: Double,
        broker: String = "Exness VIP",
        favoritePair: String = "XAU/USD",
        winRate: Double = 75.0,
        totalTrades: Int = 30
    ) {
        viewModelScope.launch {
            val currentList = rawVipMembers.value
            val maxLots = currentList.maxOfOrNull { it.lots }?.coerceAtLeast(1.0) ?: 100.0
            val fraction = (lots / (maxLots * 1.12)).toFloat().coerceIn(0.05f, 0.95f)

            val accentColor = when {
                lots >= 50.0 -> 0xFFF59E0B // Gold
                lots >= 30.0 -> 0xFFE2E8F0 // Silver
                lots >= 20.0 -> 0xFFF97316 // Bronze
                lots >= 15.0 -> 0xFF6366F1 // Blue
                lots >= 12.0 -> 0xFFD946EF // Purple
                lots >= 10.0 -> 0xFF10B981 // Green
                lots >= 8.0 -> 0xFFEF4444 // Red
                lots >= 6.0 -> 0xFF06B6D4 // Cyan
                lots >= 4.0 -> 0xFFEAB308 // Yellow
                else -> 0xFF8B5CF6 // Lavender
            }

            val member = VipMemberEntity(
                name = name.ifBlank { "Unknown" },
                memberId = memberId.ifBlank { "—" },
                lots = lots,
                progressFraction = fraction,
                accentHex = accentColor,
                period = _vipPeriodFilter.value,
                winRate = winRate,
                totalTrades = totalTrades,
                broker = broker,
                favoritePair = favoritePair
            )
            repository.insertVipMember(member)
        }
    }

    fun deleteVipMember(id: Long) {
        viewModelScope.launch {
            repository.deleteVipMember(id)
        }
    }
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

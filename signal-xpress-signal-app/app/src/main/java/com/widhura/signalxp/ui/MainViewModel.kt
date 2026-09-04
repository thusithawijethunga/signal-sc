package com.widhura.signalxp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.widhura.signalxp.BuildConfig
import com.widhura.signalxp.data.AppDatabase
import com.widhura.signalxp.data.CommunityCommentEntity
import com.widhura.signalxp.data.CommunityPostEntity
import com.widhura.signalxp.data.NewsEntity
import com.widhura.signalxp.data.SignalEntity
import com.widhura.signalxp.data.SignalRepository
import com.widhura.signalxp.data.VipMemberEntity
import com.widhura.signalxp.data.api.ApiClient
import com.widhura.signalxp.data.api.ApiRepository
import com.widhura.signalxp.data.api.AuthRepository
import com.widhura.signalxp.data.api.CentrifugoWebSocketService
import com.widhura.signalxp.data.api.CommunityPostStoreRequest
import com.widhura.signalxp.data.api.SignalStoreRequest
import com.widhura.signalxp.data.api.CommunityRealtimeEvent
import com.widhura.signalxp.data.api.NewsRealtimeEvent
import com.widhura.signalxp.data.api.SignalRealtimeEvent
import com.widhura.signalxp.data.api.TradeRealtimeEvent
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

data class HighlightedSignal(
    val signalId: Long,
    val signalNo: Int,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationMessage(
    val title: String,
    val body: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SignalRepository
    private val apiRepository: ApiRepository
    private val authRepository: AuthRepository
    val okHttpClient = OkHttpClient()

    // ── WebSocket State ───────────────────────────────
    private val _isWebSocketConnected = MutableStateFlow(false)
    val isWebSocketConnected: StateFlow<Boolean> = _isWebSocketConnected.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    // ── Highlight State ───────────────────────────────
    private val _highlightedSignal = MutableStateFlow<HighlightedSignal?>(null)
    val highlightedSignal: StateFlow<HighlightedSignal?> = _highlightedSignal.asStateFlow()

    // ── Notification State ────────────────────────────
    private val _activeNotification = MutableStateFlow<NotificationMessage?>(null)
    val activeNotification: StateFlow<NotificationMessage?> = _activeNotification.asStateFlow()

    private var centrifugoService: CentrifugoWebSocketService? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SignalRepository(db.signalDao(), db.newsDao(), db.communityDao(), db.vipMemberDao())
        apiRepository = ApiRepository(
            application,
            db.signalDao(),
            db.newsDao(),
            db.communityDao(),
            db.vipMemberDao()
        )
        authRepository = AuthRepository(application)

        viewModelScope.launch {
            // Auto-sync from API on startup
            syncAllFromApi()
            // Connect WebSocket after sync
            connectWebSocket()
        }
    }

    // ── WebSocket Connection ──────────────────────────

    fun connectWebSocket() {
        val context = getApplication<Application>()
        val token = ApiClient.getToken(context) ?: return

        centrifugoService?.destroy()

        centrifugoService = CentrifugoWebSocketService(
            onSignalUpdate = { event ->
                viewModelScope.launch(Dispatchers.IO) {
                    handleRealtimeSignalUpdate(event)
                }
            },
            onTradeUpdate = { event ->
                viewModelScope.launch(Dispatchers.IO) {
                    handleRealtimeTradeUpdate(event)
                }
            },
            onNewsUpdate = { event ->
                viewModelScope.launch(Dispatchers.IO) {
                    handleRealtimeNewsUpdate(event)
                }
            },
            onCommunityUpdate = { event ->
                viewModelScope.launch(Dispatchers.IO) {
                    handleRealtimeCommunityUpdate(event)
                }
            },
            onNotification = { event ->
                Log.d("Centrifugo", "Notification: ${event.title} - ${event.body}")
            },
            onConnectionChange = { connected ->
                _isWebSocketConnected.value = connected
            }
        )

        // Fetch WebSocket token from backend then connect
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wsTokenRequest = Request.Builder()
                    .url("https://backend.signalxpress.com/api/websocket/token")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .build()

                val response = okHttpClient.newCall(wsTokenRequest).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@launch
                    val json = JSONObject(body)
                    val wsToken = json.getString("token")
                    val wsUrl = json.optString("ws_url", "wss://socket.signalxpress.com/connection/websocket")
                    centrifugoService?.connect(wsUrl, wsToken)
                }
            } catch (e: Exception) {
                Log.e("Centrifugo", "Failed to get WS token: ${e.message}")
            }
        }
    }

    fun disconnectWebSocket() {
        centrifugoService?.disconnect()
    }

    fun clearHighlight() {
        _highlightedSignal.value = null
    }

    fun clearNotification() {
        _activeNotification.value = null
    }

    // ── Real-time Handlers ────────────────────────────

    private suspend fun handleRealtimeSignalUpdate(event: SignalRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())

        if (event.action == "deleted") {
            db.signalDao().deleteSignalById(event.id)
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • Signal #${event.no} deleted"
                _activeNotification.value = NotificationMessage(
                    title = "Signal Deleted",
                    body = "Signal #${event.no} ${event.pair} removed",
                    type = "signal_deleted"
                )
            }
            return
        }

        if (event.action == "reaction") {
            val existing = db.signalDao().getSignalById(event.id)
            if (existing != null) {
                val updated = existing.copy(
                    thumbsCount = event.thumbsCount,
                    fireCount = event.fireCount,
                    rocketCount = event.rocketCount,
                    brokenHeartCount = event.brokenHeartCount
                )
                db.signalDao().updateSignal(updated)
            }
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • Reaction updated"
            }
            return
        }

        val existing = db.signalDao().getSignalById(event.id)

        val entry = if (event.entry2 > 0) "${event.entry1} / ${event.entry2}"
                    else if (event.entry1 > 0) event.entry1.toString()
                    else existing?.entry ?: ""

        val tp1 = if (event.tp1 > 0) event.tp1.toString() else existing?.tp1 ?: ""
        val tp2 = if (event.tp2 > 0) event.tp2.toString() else existing?.tp2 ?: ""
        val tp3 = if (event.tp3 > 0) event.tp3.toString() else existing?.tp3 ?: ""
        val tp4 = if (event.tp4 > 0) event.tp4.toString() else existing?.tp4 ?: ""
        val sl = if (event.sl > 0) event.sl.toString() else existing?.sl ?: ""

        val entity = SignalEntity(
            id = event.id,
            no = event.no,
            date = event.date.ifBlank { existing?.date ?: "" },
            pair = event.pair.ifBlank { existing?.pair ?: "" },
            type = event.direction.ifBlank { existing?.type ?: "BUY" },
            entry = entry,
            tp1 = tp1, tp2 = tp2, tp3 = tp3, tp4 = tp4, sl = sl,
            pips = event.pips.toInt().let { if (it != 0) it else existing?.pips ?: 0 },
            profit = if (event.profit != 0.0) event.profit else existing?.profit ?: 0.0,
            hitLevel = event.hitLevel.ifBlank { existing?.hitLevel ?: "NONE" },
            status = event.status.ifBlank { existing?.status ?: "active" },
            result = event.result.ifBlank { existing?.result ?: "RUNNING" },
            thumbsCount = event.thumbsCount.let { if (it > 0) it else existing?.thumbsCount ?: 0 },
            fireCount = event.fireCount.let { if (it > 0) it else existing?.fireCount ?: 0 },
            rocketCount = event.rocketCount.let { if (it > 0) it else existing?.rocketCount ?: 0 },
            brokenHeartCount = event.brokenHeartCount.let { if (it > 0) it else existing?.brokenHeartCount ?: 0 }
        )

        if (existing == null) {
            db.signalDao().insertSignal(entity)
        } else {
            db.signalDao().updateSignal(entity)
        }

        val sdf = SimpleDateFormat("hh:mm a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Colombo")
        }
        withContext(Dispatchers.Main) {
            _lastSyncTime.value = "Live • ${sdf.format(Date())} (SLST)"
            _highlightedSignal.value = HighlightedSignal(
                signalId = event.id,
                signalNo = event.no,
                action = event.action.ifBlank { "updated" }
            )
            _activeNotification.value = NotificationMessage(
                title = "Signal #${event.no} ${event.pair}",
                body = "${event.direction} • ${event.result}",
                type = "signal"
            )
        }
    }

    private suspend fun handleRealtimeTradeUpdate(event: TradeRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        Log.d("Centrifugo", "Trade update: ${event.pair} ${event.result} action=${event.action}")

        if (event.eventType == "trade" && event.action == "created") {
            val existing = db.signalDao().getSignalById(event.id)
            val entity = SignalEntity(
                id = event.id,
                no = event.no,
                date = existing?.date ?: "",
                pair = event.pair.ifBlank { existing?.pair ?: "" },
                type = event.direction.ifBlank { existing?.type ?: "BUY" },
                entry = existing?.entry ?: "",
                tp1 = existing?.tp1 ?: "", tp2 = existing?.tp2 ?: "",
                tp3 = existing?.tp3 ?: "", tp4 = existing?.tp4 ?: "",
                sl = existing?.sl ?: "",
                pips = event.pips.toInt().let { if (it != 0) it else existing?.pips ?: 0 },
                profit = if (event.profit != 0.0) event.profit else existing?.profit ?: 0.0,
                hitLevel = event.hitLevel.ifBlank { existing?.hitLevel ?: "NONE" },
                status = existing?.status ?: "active",
                result = event.result.ifBlank { existing?.result ?: "RUNNING" }
            )
            if (existing == null) {
                db.signalDao().insertSignal(entity)
            } else {
                db.signalDao().updateSignal(entity)
            }
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • New trade #${event.no} ${event.pair}"
                _activeNotification.value = NotificationMessage(
                    title = "New Trade: ${event.pair}",
                    body = "${event.direction} • ${event.result}",
                    type = "trade"
                )
            }
            return
        }

        if (event.eventType == "trade_hit" || event.action == "updated") {
            withContext(Dispatchers.Main) {
                _highlightedSignal.value = HighlightedSignal(
                    signalId = event.id,
                    signalNo = event.no,
                    action = event.action.ifBlank { "updated" }
                )
                _activeNotification.value = NotificationMessage(
                    title = "Signal #${event.no} Updated",
                    body = "${event.pair} • ${event.result}",
                    type = "trade_hit"
                )
            }
        }
    }

    private suspend fun handleRealtimeNewsUpdate(event: NewsRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        val slTimeZone = java.util.TimeZone.getTimeZone("Asia/Colombo")
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).apply { timeZone = slTimeZone }
        val nowStr = timeFormat.format(java.util.Date())
        val entity = NewsEntity(
            id = event.id,
            time = "Just now \u2022 $nowStr (SLST)",
            currency = event.currency,
            title = event.title,
            impact = event.impact,
            forecast = event.forecast,
            previous = event.previous,
            actual = event.actual,
            description = "",
            timestamp = System.currentTimeMillis()
        )
        db.newsDao().insertNews(entity)
    }

    private suspend fun handleRealtimeCommunityUpdate(event: CommunityRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        val entity = CommunityPostEntity(
            id = event.id,
            authorName = event.authorName,
            postType = event.postType,
            content = event.content,
            pair = event.pair,
            profitAmount = event.profitAmount,
            pipsGain = event.pipsGain
        )
        db.communityDao().insertPost(entity)
    }

    // ── Sync from API ─────────────────────────────────

    fun syncAllFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                apiRepository.fullSync()
                withContext(Dispatchers.Main) {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Colombo")
                    }
                    _lastSyncTime.value = "Synced • ${sdf.format(Date())} (SLST)"
                }
            } catch (e: Exception) {
                Log.e("Sync", "Full sync failed: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // ── Raw signals flow from Room ────────────────────
    val rawSignals: StateFlow<List<SignalEntity>> = repository.allSignals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ── Raw news flow from Room ───────────────────────
    val rawNews: StateFlow<List<NewsEntity>> = repository.allNews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ── Forex Factory News Filters & Syncing ──────────
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

        val todayAndUpcoming = list.filter { it.timestamp >= todayStartMs }
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
                            try { parsedDate = sdfInput1.parse(dateStr) } catch (e: Exception) {
                                try { parsedDate = sdfInput2.parse(dateStr) } catch (e2: Exception) { parsedDate = null }
                            }
                        }

                        val eventTimeMs = parsedDate?.time ?: System.currentTimeMillis()

                        var formattedTimeStr = "Today"
                        if (parsedDate != null) {
                            val eventCal = Calendar.getInstance(slTimeZone).apply { time = parsedDate }
                            val isToday = eventCal.get(Calendar.YEAR) == nowCalSL.get(Calendar.YEAR) &&
                                    eventCal.get(Calendar.DAY_OF_YEAR) == nowCalSL.get(Calendar.DAY_OF_YEAR)
                            val tomorrowCal = Calendar.getInstance(slTimeZone).apply {
                                timeInMillis = nowCalSL.timeInMillis; add(Calendar.DAY_OF_YEAR, 1)
                            }
                            val isTomorrow = eventCal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
                                    eventCal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)
                            val timeFormatted = timeFormatSL.format(parsedDate)
                            formattedTimeStr = when {
                                isToday -> "Today • $timeFormatted (SLST)"
                                isTomorrow -> "Tomorrow • $timeFormatted (SLST)"
                                else -> "${dayFormatSL.format(parsedDate)} • $timeFormatted (SLST)"
                            }
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

    // ── UI Filters ────────────────────────────────────
    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.ALL)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _customDate = MutableStateFlow("")
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

    // Filtered signals
    val filteredSignals: StateFlow<List<SignalEntity>> = combine(
        rawSignals, _selectedTimeFilter, _customDate, _selectedPairFilter
    ) { signals, timeFilter, customDateStr, pairFilter ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        var result = signals
        if (pairFilter != "ALL") result = result.filter { it.pair.equals(pairFilter, ignoreCase = true) }
        when (timeFilter) {
            TimeFilter.ALL -> result
            TimeFilter.DAILY -> result.filter { it.date == todayStr }
            TimeFilter.WEEKLY -> {
                val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -7); val sevenDaysAgo = cal.time
                result.filter { try { val d = sdf.parse(it.date); d != null && (d.after(sevenDaysAgo) || d == sevenDaysAgo) } catch (e: Exception) { true } }
            }
            TimeFilter.MONTHLY -> {
                val cal = Calendar.getInstance(); val cm = cal.get(Calendar.MONTH); val cy = cal.get(Calendar.YEAR)
                result.filter { try { val d = sdf.parse(it.date); if (d != null) { val sc = Calendar.getInstance(); sc.time = d; sc.get(Calendar.MONTH) == cm && sc.get(Calendar.YEAR) == cy } else false } catch (e: Exception) { true } }
            }
            TimeFilter.CUSTOM -> if (customDateStr.isNotBlank()) result.filter { it.date == customDateStr } else result
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Ledger signals
    val ledgerSignals: StateFlow<List<SignalEntity>> = combine(filteredSignals, _selectedResultFilter) { signals, resultFilter ->
        when (resultFilter) {
            ResultFilter.ALL -> signals
            ResultFilter.WIN -> signals.filter { it.result.uppercase() == "WIN" }
            ResultFilter.LOSS -> signals.filter { it.result.uppercase() == "LOSS" }
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setTimeFilter(filter: TimeFilter) { _selectedTimeFilter.value = filter; if (filter != TimeFilter.CUSTOM) _customDate.value = "" }
    fun setCustomDate(dateStr: String) { _customDate.value = dateStr; _selectedTimeFilter.value = TimeFilter.CUSTOM }
    fun setResultFilter(filter: ResultFilter) { _selectedResultFilter.value = filter }
    fun setPairFilter(pair: String) { _selectedPairFilter.value = pair }

    // ── Reactions (API call + local update) ───────────
    fun toggleReaction(signal: SignalEntity, emoji: String) {
        viewModelScope.launch {
            // Map emoji to API emoji name
            val apiEmoji = when (emoji) {
                "👍" -> "thumbs"
                "🔥" -> "fire"
                "🚀" -> "rocket"
                "💔" -> "broken_heart"
                else -> return@launch
            }

            // Optimistic local update
            val isCurrentSame = signal.userReactedEmoji == emoji
            var newThumbs = signal.thumbsCount
            var newFire = signal.fireCount
            var newRocket = signal.rocketCount
            var newHeart = signal.brokenHeartCount

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
                thumbsCount = newThumbs, fireCount = newFire,
                rocketCount = newRocket, brokenHeartCount = newHeart,
                userReactedEmoji = nextEmoji
            )
            repository.updateSignal(updated)

            // Send to API
            apiRepository.reactSignal(signal.id, apiEmoji)
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
                hitLevel = newHitLevel, status = statusText,
                result = resultText, pips = pipsVal, profit = profitVal
            )
            repository.updateSignal(updated)

            // Send to API
            apiRepository.updateSignal(signal.id, mapOf(
                "hit_level" to newHitLevel,
                "status" to statusText,
                "result" to resultText,
                "pips" to pipsVal,
                "profit" to profitVal
            ))
        }
    }

    fun addNewSignal(pair: String, type: String, entry: String, tp1: String, tp2: String, tp3: String, tp4: String, sl: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val request = com.widhura.signalxp.data.api.SignalStoreRequest(
                pair = pair.uppercase(), direction = type.uppercase(),
                entry1 = entry.toDoubleOrNull() ?: 0.0,
                sl = sl.toDoubleOrNull() ?: 0.0,
                tp1 = tp1.toDoubleOrNull() ?: 0.0,
                tp2 = tp2.toDoubleOrNull(),
                tp3 = tp3.toDoubleOrNull(),
                tp4 = tp4.toDoubleOrNull(),
                date = sdf.format(Date())
            )
            apiRepository.createSignal(request)
        }
    }

    fun addNewNews(time: String, currency: String, title: String, impact: String, forecast: String, previous: String, description: String) {
        viewModelScope.launch {
            val news = NewsEntity(
                time = time, currency = currency.uppercase(), title = title,
                impact = impact.uppercase(), forecast = forecast, previous = previous, description = description
            )
            repository.insertNews(news)
        }
    }

    fun analyzeSignalWithGemini(signal: SignalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            _aiAnalysisResult.value = null
            val apiKey = com.widhura.signalxp.BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                _aiAnalysisResult.value = "Gemini API key not configured.\n\n• Pair: ${signal.pair} (${signal.type})\n• Risk/Reward: High conviction setup\n• Strategy: Use 1-2% lot size risk management"
                _isAnalyzing.value = false
                return@launch
            }
            try {
                val promptText = "Analyze this trading signal:\nPair: ${signal.pair}\nType: ${signal.type}\nEntry: ${signal.entry}\nTPs: ${signal.tp1}, ${signal.tp2}, ${signal.tp3}, ${signal.tp4}\nSL: ${signal.sl}\nStatus: ${signal.status}\nProvide 3 bullet points on market sentiment, risk-to-reward, and money management."
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", promptText) })
                            })
                        })
                    })
                }
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                val request = Request.Builder().url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType())).build()
                val response = okHttpClient.newCall(request).execute()
                val responseStr = response.body?.string()
                if (response.isSuccessful && responseStr != null) {
                    val jsonObj = JSONObject(responseStr)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            _aiAnalysisResult.value = parts.getJSONObject(0).optString("text", "No response.")
                        }
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

    fun clearAiAnalysis() { _aiAnalysisResult.value = null }

    // ── Community ─────────────────────────────────────
    val rawCommunityPosts: StateFlow<List<CommunityPostEntity>> = repository.allCommunityPosts.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList()
    )

    private val _communityFilter = MutableStateFlow(CommunityFilter.ALL)
    val communityFilter: StateFlow<CommunityFilter> = _communityFilter.asStateFlow()
    private val _communitySearchQuery = MutableStateFlow("")
    val communitySearchQuery: StateFlow<String> = _communitySearchQuery.asStateFlow()
    private val _communityPairFilter = MutableStateFlow("ALL")
    val communityPairFilter: StateFlow<String> = _communityPairFilter.asStateFlow()

    val filteredCommunityPosts: StateFlow<List<CommunityPostEntity>> = combine(
        rawCommunityPosts, _communityFilter, _communitySearchQuery, _communityPairFilter
    ) { posts, filter, query, pair ->
        var result = posts
        if (pair != "ALL") result = result.filter { it.pair.equals(pair, ignoreCase = true) }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { it.authorName.lowercase().contains(q) || it.content.lowercase().contains(q) || it.hashtags.lowercase().contains(q) || it.pair.lowercase().contains(q) || it.brokerName.lowercase().contains(q) }
        }
        when (filter) {
            CommunityFilter.ALL -> result
            CommunityFilter.SCREENSHOTS -> result.filter { !it.imageUri.isNullOrBlank() || it.postType == "SCREENSHOT_POST" }
            CommunityFilter.PROFIT_CARDS -> result.filter { it.postType == "PROFIT_CARD" || it.profitAmount > 0 }
            CommunityFilter.DISCUSSIONS -> result.filter { it.postType == "IDEA_DISCUSSION" || (it.imageUri.isNullOrBlank() && it.postType != "PROFIT_CARD") }
            CommunityFilter.TOP_GAINERS, CommunityFilter.TOP_PROFITS -> result.filter { it.profitAmount > 0 }.sortedByDescending { it.profitAmount }
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val topProfitHighlights: StateFlow<List<CommunityPostEntity>> = rawCommunityPosts.combine(_communityFilter) { posts, _ ->
        posts.filter { it.profitAmount > 0 }.sortedByDescending { it.profitAmount }.take(5)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setCommunityFilter(filter: CommunityFilter) { _communityFilter.value = filter }
    fun setCommunitySearchQuery(query: String) { _communitySearchQuery.value = query }
    fun setCommunityPairFilter(pair: String) { _communityPairFilter.value = pair }

    fun toggleCommunityLike(post: CommunityPostEntity) {
        viewModelScope.launch {
            val newIsLiked = !post.isLikedByMe
            val newCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
            repository.updateCommunityPost(post.copy(isLikedByMe = newIsLiked, likesCount = newCount))
            apiRepository.reactPost(post.id, "thumbs")
        }
    }

    fun toggleCommunityFire(post: CommunityPostEntity) {
        viewModelScope.launch {
            val newIsFired = !post.isFiredByMe
            val newCount = if (newIsFired) post.fireCount + 1 else (post.fireCount - 1).coerceAtLeast(0)
            repository.updateCommunityPost(post.copy(isFiredByMe = newIsFired, fireCount = newCount))
            apiRepository.reactPost(post.id, "fire")
        }
    }

    fun toggleCommunityRocket(post: CommunityPostEntity) {
        viewModelScope.launch {
            val newIsRocket = !post.isRocketByMe
            val newCount = if (newIsRocket) post.rocketCount + 1 else (post.rocketCount - 1).coerceAtLeast(0)
            repository.updateCommunityPost(post.copy(isRocketByMe = newIsRocket, rocketCount = newCount))
            apiRepository.reactPost(post.id, "rocket")
        }
    }

    fun getCommentsForPost(postId: Long): Flow<List<CommunityCommentEntity>> = repository.getCommentsForPost(postId)

    fun addComment(postId: Long, authorName: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val comment = CommunityCommentEntity(
                postId = postId, authorName = if (authorName.isBlank()) "Trader LK" else authorName.trim(),
                content = content.trim(), timestamp = System.currentTimeMillis()
            )
            repository.insertComment(comment)
            val result = apiRepository.createComment(postId, content.trim())
            result.fold(
                onSuccess = { (entity, message) ->
                    _moderationMessage.value = message
                },
                onFailure = { }
            )
        }
    }

    // Community moderation message
    private val _moderationMessage = MutableStateFlow<String?>(null)
    val moderationMessage: StateFlow<String?> = _moderationMessage.asStateFlow()

    fun clearModerationMessage() {
        _moderationMessage.value = null
    }

    fun createCommunityPost(authorName: String, authorBadge: String, authorAvatarHex: Long, content: String, imageUri: String?, pair: String, tradeType: String, profitAmount: Double, pipsGain: Int, brokerName: String, hashtags: String) {
        if (content.isBlank() && imageUri.isNullOrBlank()) return
        viewModelScope.launch {
            val post = CommunityPostEntity(
                authorName = if (authorName.isBlank()) "Trader LK" else authorName.trim(),
                authorBadge = if (authorBadge.isBlank()) "Forex Trader" else authorBadge.trim(),
                authorAvatarHex = authorAvatarHex,
                postType = if (!imageUri.isNullOrBlank()) "SCREENSHOT_POST" else "IDEA_DISCUSSION",
                timestamp = System.currentTimeMillis(), content = content.trim(),
                imageUri = imageUri, hashtags = hashtags.trim(),
                pair = pair.ifBlank { "XAU/USD" }.uppercase(),
                tradeType = tradeType.uppercase(), profitAmount = profitAmount,
                pipsGain = pipsGain, brokerName = brokerName.ifBlank { "Exness" }.trim(),
                likesCount = 1, isLikedByMe = true, fireCount = 1, isFiredByMe = true
            )
            repository.insertCommunityPost(post)
            val result = apiRepository.createCommunityPost(
                com.widhura.signalxp.data.api.CommunityPostStoreRequest(
                    content = content.trim(), postType = if (!imageUri.isNullOrBlank()) "screenshot" else "text",
                    hashtags = hashtags.trim(), imageUri = imageUri, pair = pair,
                    tradeType = tradeType, profitAmount = profitAmount, pipsGain = pipsGain,
                    brokerName = brokerName, authorBadge = authorBadge, authorAvatarHex = authorAvatarHex
                )
            )
            result.fold(
                onSuccess = { (entity, message) ->
                    // Update local post with server response (including status)
                    repository.updateCommunityPost(entity.copy(
                        status = entity.status,
                        timestamp = System.currentTimeMillis()
                    ))
                    _moderationMessage.value = message
                },
                onFailure = {
                    _moderationMessage.value = "Failed to submit post"
                }
            )
        }
    }

    fun createProfitCardPost(authorName: String, authorBadge: String, authorAvatarHex: Long, pair: String, tradeType: String, entryPrice: String, exitPrice: String, lotSize: String, profitAmount: Double, pipsGain: Int, roiPercentage: Double, brokerName: String, cardTheme: String, caption: String, hashtags: String) {
        viewModelScope.launch {
            val post = CommunityPostEntity(
                authorName = if (authorName.isBlank()) "Trader LK" else authorName.trim(),
                authorBadge = if (authorBadge.isBlank()) "VIP Member" else authorBadge.trim(),
                authorAvatarHex = authorAvatarHex, postType = "PROFIT_CARD",
                timestamp = System.currentTimeMillis(), content = caption.trim(),
                hashtags = hashtags.trim(), pair = pair.uppercase(), tradeType = tradeType.uppercase(),
                entryPrice = entryPrice.trim(), exitPrice = exitPrice.trim(),
                lotSize = lotSize.ifBlank { "0.50" }.trim(), profitAmount = profitAmount,
                pipsGain = pipsGain, roiPercentage = roiPercentage,
                brokerName = brokerName.ifBlank { "Exness Pro" }.trim(),
                cardTheme = cardTheme, isVerifiedTrade = true,
                likesCount = 1, isLikedByMe = true, fireCount = 1, isFiredByMe = true
            )
            repository.insertCommunityPost(post)
            apiRepository.createCommunityPost(
                com.widhura.signalxp.data.api.CommunityPostStoreRequest(
                    content = caption.trim(), postType = "signal_card",
                    hashtags = hashtags.trim(), pair = pair, tradeType = tradeType,
                    entryPrice = entryPrice, exitPrice = exitPrice, lotSize = lotSize,
                    profitAmount = profitAmount, pipsGain = pipsGain, roiPercentage = roiPercentage,
                    brokerName = brokerName, cardTheme = cardTheme,
                    authorBadge = authorBadge, authorAvatarHex = authorAvatarHex
                )
            )
        }
    }

    fun createDiscussionPost(authorName: String, authorBadge: String, authorAvatarHex: Long, content: String, pair: String, hashtags: String) {
        if (content.isBlank()) return
        createCommunityPost(authorName, authorBadge, authorAvatarHex, content, null, pair, "BUY", 0.0, 0, "", hashtags)
    }

    fun deleteCommunityPost(postId: Long) {
        viewModelScope.launch {
            repository.deleteCommunityPost(postId)
            apiRepository.deleteCommunityPost(postId)
        }
    }

    // ── VIP ───────────────────────────────────────────
    private val prefs = getApplication<Application>().getSharedPreferences("signal_xpress_vip_prefs", android.content.Context.MODE_PRIVATE)

    val isDeveloperMode: StateFlow<Boolean> = MutableStateFlow(com.widhura.signalxp.BuildConfig.DEVELOPER_MODE).asStateFlow()
    val isScreenshotDisabled: StateFlow<Boolean> = MutableStateFlow(com.widhura.signalxp.BuildConfig.SCREENSHOT_DISABLED).asStateFlow()

    private val _isSyncingVip = MutableStateFlow(false)
    val isSyncingVip: StateFlow<Boolean> = _isSyncingVip.asStateFlow()

    val rawVipMembers: StateFlow<List<VipMemberEntity>> = repository.allVipMembers.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList()
    )

    private val _vipSearchQuery = MutableStateFlow("")
    val vipSearchQuery: StateFlow<String> = _vipSearchQuery.asStateFlow()
    private val _vipPeriodFilter = MutableStateFlow("MONTHLY")
    val vipPeriodFilter: StateFlow<String> = _vipPeriodFilter.asStateFlow()

    val filteredVipMembers: StateFlow<List<VipMemberEntity>> = combine(rawVipMembers, _vipSearchQuery, _vipPeriodFilter) { members, query, period ->
        var list = if (period == "ALL_TIME") members else members.filter { it.period.equals(period, ignoreCase = true) }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(q) || it.memberId.lowercase().contains(q) || it.broker.lowercase().contains(q) }
        }
        list.sortedByDescending { it.lots }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setVipSearchQuery(query: String) { _vipSearchQuery.value = query }
    fun setVipPeriodFilter(period: String) { _vipPeriodFilter.value = period }

    fun loadVipFromApi(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncingVip.value = true
            try {
                val result = apiRepository.syncVipLeaderboard()
                _isSyncingVip.value = false
                if (result.isSuccess) {
                    onComplete?.invoke(true, "VIP data loaded")
                } else {
                    onComplete?.invoke(false, result.exceptionOrNull()?.message ?: "Failed to load VIP data")
                }
            } catch (e: Exception) {
                _isSyncingVip.value = false
                onComplete?.invoke(false, e.message ?: "Failed to load VIP data")
            }
        }
    }

    fun addOrUpdateVipMember(name: String, memberId: String, lots: Double, broker: String, favoritePair: String, winRate: Double, totalTrades: Int) {
        viewModelScope.launch {
            val currentList = rawVipMembers.value
            val maxLots = currentList.maxOfOrNull { it.lots }?.coerceAtLeast(1.0) ?: 100.0
            val fraction = (lots / (maxLots * 1.12)).toFloat().coerceIn(0.05f, 0.95f)
            val accentColor = when { lots >= 50.0 -> 0xFFF59E0B; lots >= 30.0 -> 0xFFE2E8F0; lots >= 20.0 -> 0xFFF97316; lots >= 15.0 -> 0xFF6366F1; lots >= 12.0 -> 0xFFD946EF; lots >= 10.0 -> 0xFF10B981; lots >= 8.0 -> 0xFFEF4444; lots >= 6.0 -> 0xFF06B6D4; lots >= 4.0 -> 0xFFEAB308; else -> 0xFF8B5CF6 }
            val member = VipMemberEntity(name = name.ifBlank { "Unknown" }, memberId = memberId.ifBlank { "—" }, lots = lots, progressFraction = fraction, accentHex = accentColor, period = _vipPeriodFilter.value, winRate = winRate, totalTrades = totalTrades, broker = broker, favoritePair = favoritePair)
            repository.insertVipMember(member)
        }
    }

    fun deleteVipMember(id: Long) { viewModelScope.launch { repository.deleteVipMember(id) } }

    override fun onCleared() {
        super.onCleared()
        centrifugoService?.destroy()
    }
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

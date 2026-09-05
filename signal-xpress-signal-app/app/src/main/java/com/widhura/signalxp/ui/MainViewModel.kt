package com.widhura.signalxp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.widhura.signalxp.BuildConfig
import com.widhura.signalxp.NotificationForegroundService
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
import com.widhura.signalxp.data.api.CentrifugoEventBus
import com.widhura.signalxp.data.api.CommunityPostStoreRequest
import com.widhura.signalxp.data.api.SignalStoreRequest
import com.widhura.signalxp.data.api.CommunityRealtimeEvent
import com.widhura.signalxp.data.api.NewsRealtimeEvent
import com.widhura.signalxp.data.api.NotificationEvent
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
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

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

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val signalRepository: SignalRepository,
    private val apiRepository: ApiRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val repository: SignalRepository = signalRepository
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

    init {
        viewModelScope.launch {
            syncAllFromApi()
            connectWebSocket()
        }

        viewModelScope.launch {
            CentrifugoEventBus.connectionState.collect { connected ->
                _isWebSocketConnected.value = connected
            }
        }
        viewModelScope.launch {
            CentrifugoEventBus.signalEvents.collect { event ->
                withContext(Dispatchers.IO) {
                    handleRealtimeSignalUpdate(event)
                }
            }
        }
        viewModelScope.launch {
            CentrifugoEventBus.tradeEvents.collect { event ->
                withContext(Dispatchers.IO) {
                    handleRealtimeTradeUpdate(event)
                }
            }
        }
        viewModelScope.launch {
            CentrifugoEventBus.newsEvents.collect { event ->
                withContext(Dispatchers.IO) {
                    handleRealtimeNewsUpdate(event)
                }
            }
        }
        viewModelScope.launch {
            CentrifugoEventBus.communityEvents.collect { event ->
                withContext(Dispatchers.IO) {
                    handleRealtimeCommunityUpdate(event)
                }
            }
        }
        viewModelScope.launch {
            CentrifugoEventBus.notificationEvents.collect { event ->
                Log.d("Centrifugo", "Notification: ${event.title} - ${event.body} type=${event.type}")
                withContext(Dispatchers.IO) {
                    var signalNo = event.signalNo
                    // Fallback: look up from DB if signal_no not included in the broadcast
                    if (signalNo == 0 && event.signalId != 0L) {
                        try {
                            signalNo = AppDatabase.getDatabase(getApplication())
                                .signalDao().getSignalById(event.signalId)?.no ?: 0
                        } catch (e: Exception) {
                            Log.d("Centrifugo", "signalNo lookup failed: ${e.message}")
                        }
                    }
                    try {
                        com.widhura.signalxp.util.SignalNotifications.showIfImportant(
                            getApplication(), event, signalNo
                        )
                    } catch (e: Exception) {
                        Log.d("Centrifugo", "system notification failed: ${e.message}")
                    }
                    withContext(Dispatchers.Main) {
                        _activeNotification.value = NotificationMessage(
                            title = event.title ?: "",
                            body = event.body ?: "",
                            type = event.type ?: "info"
                        )
                    }
                }
            }
        }
    }

    // ── WebSocket Connection ──────────────────────────

    fun connectWebSocket() {
        val context = getApplication<Application>()
        val userId = ApiClient.getCurrentUserId(context).toString()

        NotificationForegroundService.start(context, userId)
    }

    fun disconnectWebSocket() {
        val context = getApplication<Application>()
        NotificationForegroundService.stop(context)
    }

    fun clearHighlight() {
        _highlightedSignal.value = null
    }

    /** Jump to a signal (used by system-notification tap). */
    fun focusSignal(signalId: Long, signalNo: Int) {
        if (signalId == 0L && signalNo == 0) return
        _highlightedSignal.value = HighlightedSignal(
            signalId = signalId,
            signalNo = signalNo,
            action = "updated"
        )
    }

    fun clearNotification() {
        _activeNotification.value = null
    }

    // ── Real-time Handlers ────────────────────────────

    private suspend fun handleRealtimeSignalUpdate(event: SignalRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        val eventNo = event.no ?: 0
        val eventId = event.id ?: 0L
        val eventAction = event.action ?: ""
        Log.d("Centrifugo", "Signal update: id=$eventId no=$eventNo action=$eventAction hitLevel=${event.hitLevel} result=${event.result} pair=${event.pair}")

        if (eventAction == "deleted") {
            if (eventId != 0L) db.signalDao().deleteSignalById(eventId)
            if (eventNo != 0) {
                db.signalDao().getSignalByNo(eventNo)?.let {
                    if (it.id != eventId) db.signalDao().deleteSignalById(it.id)
                }
            }
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • Signal #$eventNo deleted"
                _activeNotification.value = NotificationMessage(
                    title = "Signal Deleted",
                    body = "Signal #$eventNo ${event.pair ?: ""} removed",
                    type = "signal_deleted"
                )
            }
            return
        }

        val byId = if (eventId != 0L) db.signalDao().getSignalById(eventId) else null
        val byNo = if (eventNo != 0) db.signalDao().getSignalByNo(eventNo) else null
        val existing = byId ?: byNo

        if (eventAction == "reaction") {
            if (existing != null) {
                val updated = existing.copy(
                    thumbsCount = event.thumbsCount ?: existing.thumbsCount,
                    fireCount = event.fireCount ?: existing.fireCount,
                    rocketCount = event.rocketCount ?: existing.rocketCount,
                    brokenHeartCount = event.brokenHeartCount ?: existing.brokenHeartCount
                )
                db.signalDao().updateSignal(updated)
            }
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • Reaction updated"
            }
            return
        }

        val e1 = event.entry1 ?: 0.0
        val e2 = event.entry2 ?: 0.0
        val entry = if (e2 > 0) "$e1 / $e2"
                    else if (e1 > 0) e1.toString()
                    else existing?.entry ?: ""

        val tp1 = if ((event.tp1 ?: 0.0) > 0) event.tp1.toString() else existing?.tp1 ?: ""
        val tp2 = if ((event.tp2 ?: 0.0) > 0) event.tp2.toString() else existing?.tp2 ?: ""
        val tp3 = if ((event.tp3 ?: 0.0) > 0) event.tp3.toString() else existing?.tp3 ?: ""
        val tp4 = if ((event.tp4 ?: 0.0) > 0) event.tp4.toString() else existing?.tp4 ?: ""
        val sl = if ((event.sl ?: 0.0) > 0) event.sl.toString() else existing?.sl ?: ""

        val resolvedId = when {
            byId != null -> eventId
            byNo != null && eventId != 0L -> eventId
            byNo != null -> byNo.id
            else -> eventId
        }
        val entity = SignalEntity(
            id = resolvedId,
            no = eventNo,
            date = event.date ?: existing?.date ?: "",
            pair = event.pair ?: existing?.pair ?: "",
            type = event.direction ?: existing?.type ?: "BUY",
            entry = entry,
            tp1 = tp1, tp2 = tp2, tp3 = tp3, tp4 = tp4, sl = sl,
            pips = if (event.pips != null) event.pips.toInt() else existing?.pips ?: 0,
            profit = event.profit ?: existing?.profit ?: 0.0,
            hitLevel = event.hitLevel ?: existing?.hitLevel ?: "NONE",
            status = event.status ?: existing?.status ?: "active",
            result = event.result ?: existing?.result ?: "RUNNING",
            thumbsCount = event.thumbsCount ?: existing?.thumbsCount ?: 0,
            fireCount = event.fireCount ?: existing?.fireCount ?: 0,
            rocketCount = event.rocketCount ?: existing?.rocketCount ?: 0,
            brokenHeartCount = event.brokenHeartCount ?: existing?.brokenHeartCount ?: 0,
            userReactedEmoji = existing?.userReactedEmoji
        )

        if (byId == null && byNo != null && eventId != 0L && byNo.id != eventId) {
            db.signalDao().deleteSignalById(byNo.id)
            db.signalDao().insertSignal(entity)
        } else if (existing == null) {
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
                signalId = resolvedId,
                signalNo = eventNo,
                action = eventAction.ifBlank { "updated" }
            )
            _activeNotification.value = NotificationMessage(
                title = "Signal #$eventNo ${event.pair ?: ""}",
                body = "${event.direction ?: ""} • ${event.result ?: ""}",
                type = "signal"
            )
        }

        if (eventNo != 0 && eventAction != "created") {
            refreshSignalByNo(eventNo)
        }
    }

    private suspend fun handleRealtimeTradeUpdate(event: TradeRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        val eventNo = event.no ?: 0
        val eventAction = event.action ?: ""
        val eventEventType = event.eventType
        Log.d("Centrifugo", "Trade update: ${event.pair} ${event.result} action=$eventAction")

        if (eventAction == "deleted") {
            val existing = db.signalDao().getSignalByNo(eventNo)
            if (existing != null) {
                db.signalDao().deleteSignalById(existing.id)
                withContext(Dispatchers.Main) {
                    _lastSyncTime.value = "Live • Trade #$eventNo deleted"
                    _activeNotification.value = NotificationMessage(
                        title = "Trade Deleted",
                        body = "Signal #$eventNo ${event.pair ?: ""} removed",
                        type = "signal_deleted"
                    )
                }
            }
            return
        }

        val existing = db.signalDao().getSignalByNo(eventNo)
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

        if (eventEventType == "trade" && eventAction == "created") {
            val entity = SignalEntity(
                id = existing?.id ?: 0,
                no = eventNo,
                date = existing?.date ?: todayStr,
                pair = event.pair ?: existing?.pair ?: "",
                type = event.direction ?: existing?.type ?: "BUY",
                entry = existing?.entry ?: "",
                tp1 = existing?.tp1 ?: "", tp2 = existing?.tp2 ?: "",
                tp3 = existing?.tp3 ?: "", tp4 = existing?.tp4 ?: "",
                sl = existing?.sl ?: "",
                pips = if (event.pips != null) event.pips.toInt() else existing?.pips ?: 0,
                profit = event.profit ?: existing?.profit ?: 0.0,
                hitLevel = event.hitLevel ?: existing?.hitLevel ?: "NONE",
                status = existing?.status ?: "active",
                result = event.result ?: existing?.result ?: "RUNNING"
            )
            if (existing == null) {
                db.signalDao().insertSignal(entity)
            } else {
                db.signalDao().updateSignal(entity)
            }
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • New trade #$eventNo ${event.pair ?: ""}"
                _activeNotification.value = NotificationMessage(
                    title = "New Trade: ${event.pair ?: ""}",
                    body = "${event.direction ?: ""} • ${event.result ?: ""}",
                    type = "trade"
                )
            }
            if (eventNo != 0) {
                refreshSignalByNo(eventNo)
            }
            return
        }

        if (eventEventType == "trade_hit" || eventAction == "updated" || eventEventType == "trade") {
            if (existing != null) {
                val updated = existing.copy(
                    result = event.result ?: existing.result,
                    pips = if (event.pips != null) event.pips.toInt() else existing.pips,
                    profit = event.profit ?: existing.profit,
                    hitLevel = event.hitLevel ?: existing.hitLevel
                )
                db.signalDao().updateSignal(updated)
            }
            withContext(Dispatchers.Main) {
                _lastSyncTime.value = "Live • Trade #$eventNo ${event.result ?: ""}"
                _highlightedSignal.value = HighlightedSignal(
                    signalId = existing?.id ?: (event.id ?: 0L),
                    signalNo = eventNo,
                    action = eventAction.ifBlank { "updated" }
                )
                _activeNotification.value = NotificationMessage(
                    title = "Trade #$eventNo ${event.pair ?: ""}",
                    body = "${event.direction ?: ""} • ${event.result ?: ""} • ${event.pips ?: 0} pips",
                    type = "trade_hit"
                )
            }
            if (eventNo != 0) {
                refreshSignalByNo(eventNo)
            }
        }
    }

    /**
     * REST confirm for a single signal number. WebSocket payloads can be
     * partial or arrive out of order — this guarantees the local row
     * converges to the backend state after any hit/update event.
     */
    private var lastRefreshByNo = 0 to 0L
    private fun refreshSignalByNo(no: Int) {
        // Coalesce bursts (signal + trade + broadcast events for one hit)
        val now = System.currentTimeMillis()
        if (lastRefreshByNo.first == no && now - lastRefreshByNo.second < 3000) return
        lastRefreshByNo = no to now
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = apiRepository.refreshSignalByNo(no)
                if (result.isFailure) {
                    Log.d("Centrifugo", "refreshSignalByNo #$no failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.d("Centrifugo", "refreshSignalByNo #$no error: ${e.message}")
            }
        }
    }

    private suspend fun handleRealtimeNewsUpdate(event: NewsRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        val newsId = event.id ?: 0L
        val newsTitle = event.title ?: ""
        if (newsId == 0L && newsTitle.isBlank()) return
        val slTimeZone = java.util.TimeZone.getTimeZone("Asia/Colombo")
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).apply { timeZone = slTimeZone }
        val nowStr = timeFormat.format(java.util.Date())
        val impactRaw = event.impact ?: ""
        val impactNorm = when {
            impactRaw.contains("high", ignoreCase = true) -> "HIGH"
            impactRaw.contains("med", ignoreCase = true) -> "MEDIUM"
            impactRaw.contains("low", ignoreCase = true) -> "LOW"
            else -> impactRaw.ifBlank { "MEDIUM" }
        }
        val entity = NewsEntity(
            id = if (newsId != 0L) newsId else 0L,
            time = "Just now \u2022 $nowStr (SLST)",
            currency = event.currency ?: "",
            title = newsTitle,
            impact = impactNorm,
            forecast = event.forecast ?: "",
            previous = event.previous ?: "",
            actual = event.actual ?: "",
            description = "",
            timestamp = System.currentTimeMillis()
        )
        db.newsDao().insertNews(entity)
    }

    private suspend fun handleRealtimeCommunityUpdate(event: CommunityRealtimeEvent) {
        val db = AppDatabase.getDatabase(getApplication())
        val communityId = event.id ?: 0L
        val communityContent = event.content ?: ""
        if (communityId == 0L && communityContent.isBlank()) return
        val postTypeRaw = event.postType ?: ""
        val postTypeNorm = when (postTypeRaw.lowercase()) {
            "screenshot", "screenshot_post" -> "SCREENSHOT_POST"
            "profit_card", "profit", "trade" -> "PROFIT_CARD"
            "idea", "discussion", "idea_discussion", "text", "signal_card" -> "IDEA_DISCUSSION"
            else -> postTypeRaw.ifBlank { "PROFIT_CARD" }
        }
        val existing = try { db.communityDao().getPostById(communityId) } catch (e: Exception) { null }
        if (existing != null) {
            val updated = existing.copy(
                authorName = event.authorName.ifBlank { existing.authorName },
                postType = postTypeNorm,
                content = communityContent.ifBlank { existing.content },
                pair = (event.pair ?: "").ifBlank { existing.pair },
                profitAmount = if (event.profitAmount != 0.0) event.profitAmount else existing.profitAmount,
                pipsGain = if (event.pipsGain != 0) event.pipsGain else existing.pipsGain
            )
            db.communityDao().updatePost(updated)
        } else {
            val entity = CommunityPostEntity(
                id = communityId,
                authorName = event.authorName,
                postType = postTypeNorm,
                content = communityContent,
                pair = (event.pair ?: "").ifBlank { "XAU/USD" },
                profitAmount = event.profitAmount,
                pipsGain = event.pipsGain
            )
            db.communityDao().insertPost(entity)
        }
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
        // Foreground service manages its own lifecycle
    }
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

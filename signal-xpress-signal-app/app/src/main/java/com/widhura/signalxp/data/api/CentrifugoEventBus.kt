package com.widhura.signalxp.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object CentrifugoEventBus {
    private val _signalEvents = MutableSharedFlow<SignalRealtimeEvent>(extraBufferCapacity = 16)
    val signalEvents = _signalEvents.asSharedFlow()

    private val _tradeEvents = MutableSharedFlow<TradeRealtimeEvent>(extraBufferCapacity = 16)
    val tradeEvents = _tradeEvents.asSharedFlow()

    private val _newsEvents = MutableSharedFlow<NewsRealtimeEvent>(extraBufferCapacity = 16)
    val newsEvents = _newsEvents.asSharedFlow()

    private val _communityEvents = MutableSharedFlow<CommunityRealtimeEvent>(extraBufferCapacity = 16)
    val communityEvents = _communityEvents.asSharedFlow()

    private val _notificationEvents = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 16)
    val notificationEvents = _notificationEvents.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>(extraBufferCapacity = 4)
    val connectionState = _connectionState.asSharedFlow()

    fun emitSignal(event: SignalRealtimeEvent) { _signalEvents.tryEmit(event) }
    fun emitTrade(event: TradeRealtimeEvent) { _tradeEvents.tryEmit(event) }
    fun emitNews(event: NewsRealtimeEvent) { _newsEvents.tryEmit(event) }
    fun emitCommunity(event: CommunityRealtimeEvent) { _communityEvents.tryEmit(event) }
    fun emitNotification(event: NotificationEvent) { _notificationEvents.tryEmit(event) }
    fun emitConnectionState(connected: Boolean) { _connectionState.tryEmit(connected) }
}

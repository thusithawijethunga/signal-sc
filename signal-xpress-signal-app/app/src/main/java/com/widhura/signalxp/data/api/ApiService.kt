package com.widhura.signalxp.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth ──────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun me(): Response<UserResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // ── Signals ───────────────────────────────────────
    @GET("signals")
    suspend fun getSignals(
        @Query("pair") pair: String? = null,
        @Query("result") result: String? = null,
        @Query("direction") direction: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("no") no: Int? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<SignalResponse>>

    @POST("signals")
    suspend fun createSignal(@Body request: SignalStoreRequest): Response<SignalResponse>

    @PUT("signals/{id}")
    suspend fun updateSignal(
        @Path("id") id: Long,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<SignalResponse>

    @DELETE("signals/{id}")
    suspend fun deleteSignal(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @POST("signals/{id}/react")
    suspend fun reactSignal(
        @Path("id") id: Long,
        @Body request: SignalReactRequest
    ): Response<Map<String, Any>>

    // ── Trades ────────────────────────────────────────
    @GET("trades")
    suspend fun getTrades(
        @Query("result") result: String? = null,
        @Query("pair") pair: String? = null,
        @Query("period") period: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<TradeResponse>>

    @GET("trades/summary")
    suspend fun getTradeSummary(): Response<TradeSummaryResponse>

    @POST("trades")
    suspend fun createTrade(@Body request: TradeStoreRequest): Response<TradeResponse>

    @PUT("trades/{id}")
    suspend fun updateTrade(
        @Path("id") id: Long,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<TradeResponse>

    @DELETE("trades/{id}")
    suspend fun deleteTrade(@Path("id") id: Long): Response<ApiResponse<Unit>>

    // ── Community ─────────────────────────────────────
    @GET("community/posts")
    suspend fun getCommunityPosts(
        @Query("post_type") postType: String? = null,
        @Query("pair") pair: String? = null,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<CommunityPostResponse>>

    @POST("community/posts")
    suspend fun createCommunityPost(@Body request: CommunityPostStoreRequest): Response<PostStoreResponse>

    @DELETE("community/posts/{id}")
    suspend fun deleteCommunityPost(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @GET("community/posts/{id}/comments")
    suspend fun getPostComments(
        @Path("id") postId: Long,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<CommentResponse>>

    @POST("community/posts/{id}/comments")
    suspend fun createComment(
        @Path("id") postId: Long,
        @Body request: CommentStoreRequest
    ): Response<CommentStoreApiResponse>

    @POST("community/posts/{id}/react")
    suspend fun reactPost(
        @Path("id") postId: Long,
        @Body request: CommunityReactRequest
    ): Response<Map<String, Any>>

    @GET("community/settings")
    suspend fun getCommunitySettings(): Response<CommunitySettingsResponse>

    // ── Admin Community Moderation ────────────────────
    @GET("admin/community/stats")
    suspend fun getAdminCommunityStats(): Response<CommunityStatsResponse>

    @GET("admin/community/pending-posts")
    suspend fun getPendingPosts(
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<CommunityPostResponse>>

    @GET("admin/community/posts")
    suspend fun getAdminAllPosts(
        @Query("status") status: String? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<CommunityPostResponse>>

    @POST("admin/community/posts/{id}/approve")
    suspend fun approvePost(@Path("id") id: Long): Response<Map<String, Any>>

    @POST("admin/community/posts/approve-all")
    suspend fun approveAllPosts(): Response<Map<String, Any>>

    @POST("admin/community/posts/{id}/reject")
    suspend fun rejectPost(
        @Path("id") id: Long,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>

    @DELETE("admin/community/posts/{id}")
    suspend fun adminDeletePost(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @GET("admin/community/pending-comments")
    suspend fun getPendingComments(
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<CommentResponse>>

    @POST("admin/community/comments/{id}/approve")
    suspend fun approveComment(@Path("id") id: Long): Response<Map<String, Any>>

    @POST("admin/community/comments/approve-all")
    suspend fun approveAllComments(): Response<Map<String, Any>>

    @POST("admin/community/comments/{id}/reject")
    suspend fun rejectComment(@Path("id") id: Long): Response<Map<String, Any>>

    // ── News ──────────────────────────────────────────
    @GET("news")
    suspend fun getNews(
        @Query("currency") currency: String? = null,
        @Query("impact") impact: String? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<NewsResponse>>

    @POST("news")
    suspend fun createNews(@Body request: NewsStoreRequest): Response<NewsResponse>

    @POST("news/sync")
    suspend fun syncNews(): Response<Map<String, Any>>

    // ── VIP ───────────────────────────────────────────
    @GET("vip/leaderboard")
    suspend fun getVipLeaderboard(
        @Query("period") period: String? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<VipMemberResponse>>

    @POST("vip")
    suspend fun createVipMember(@Body request: VipMemberStoreRequest): Response<VipMemberResponse>

    @POST("vip/sync")
    suspend fun syncVip(@Body request: Map<String, String>): Response<Map<String, Any>>

    // ── IB Partners ───────────────────────────────────
    @GET("partners/members")
    suspend fun getIbMembers(
        @Query("search") search: String? = null,
        @Query("partner_id") partnerId: Long? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<IbMemberResponse>>

    @POST("partners/members")
    suspend fun createIbMember(@Body request: IbMemberStoreRequest): Response<IbMemberResponse>

    @GET("partners/list")
    suspend fun getIbPartners(
        @Query("per_page") perPage: Int = 50
    ): Response<PaginatedResponse<IbPartnerResponse>>

    @POST("partners")
    suspend fun createIbPartner(@Body request: IbPartnerStoreRequest): Response<IbPartnerResponse>

    @POST("partners/search")
    suspend fun searchIbMember(@Body request: Map<String, String>): Response<List<IbMemberResponse>>

    // ── Sync ──────────────────────────────────────────
    @POST("sync/push")
    suspend fun syncPush(@Body request: SyncPushRequest): Response<SyncPushResponse>

    @GET("sync/pull")
    suspend fun syncPull(
        @Query("since") since: String? = null
    ): Response<SyncPullResponse>
}

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

    suspend fun insertVipMember(member: VipMemberEntity): Long {
        return vipMemberDao.insertVipMember(member)
    }

    suspend fun replaceAllVipMembers(members: List<VipMemberEntity>) {
        vipMemberDao.deleteAllVipMembers()
        vipMemberDao.insertAll(members)
    }

    suspend fun insertAll(signals: List<SignalEntity>) {
        signalDao.insertAll(signals)
    }

    suspend fun updateVipMember(member: VipMemberEntity) {
        vipMemberDao.updateVipMember(member)
    }

    suspend fun deleteVipMember(id: Long) {
        vipMemberDao.deleteVipMemberById(id)
    }
}

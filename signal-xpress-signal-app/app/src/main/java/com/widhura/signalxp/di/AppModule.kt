package com.widhura.signalxp.di

import android.content.Context
import com.widhura.signalxp.data.AppDatabase
import com.widhura.signalxp.data.CommunityDao
import com.widhura.signalxp.data.NewsDao
import com.widhura.signalxp.data.SignalDao
import com.widhura.signalxp.data.SignalRepository
import com.widhura.signalxp.data.VipMemberDao
import com.widhura.signalxp.data.api.ApiRepository
import com.widhura.signalxp.data.api.AuthRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideSignalDao(db: AppDatabase): SignalDao = db.signalDao()

    @Provides
    fun provideNewsDao(db: AppDatabase): NewsDao = db.newsDao()

    @Provides
    fun provideCommunityDao(db: AppDatabase): CommunityDao = db.communityDao()

    @Provides
    fun provideVipMemberDao(db: AppDatabase): VipMemberDao = db.vipMemberDao()

    @Provides
    @Singleton
    fun provideSignalRepository(
        signalDao: SignalDao,
        newsDao: NewsDao,
        communityDao: CommunityDao,
        vipMemberDao: VipMemberDao
    ): SignalRepository {
        return SignalRepository(signalDao, newsDao, communityDao, vipMemberDao)
    }

    @Provides
    @Singleton
    fun provideApiRepository(
        @ApplicationContext context: Context,
        signalDao: SignalDao,
        newsDao: NewsDao,
        communityDao: CommunityDao,
        vipMemberDao: VipMemberDao
    ): ApiRepository {
        return ApiRepository(context, signalDao, newsDao, communityDao, vipMemberDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepository(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }
}

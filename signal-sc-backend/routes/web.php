<?php

use App\Http\Controllers\DashboardController;
use App\Http\Controllers\AdminPanelController;
use App\Http\Controllers\IbPartnerPageController;
use App\Http\Controllers\CsvAnalyticsController;
use App\Http\Controllers\IbPartnerManageController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\TradeManageController;
use App\Http\Controllers\ProfileController;
use App\Http\Controllers\AdminCommunityController;
use App\Http\Controllers\AdminNewsController;
use App\Http\Controllers\AdminChatController;
use App\Http\Controllers\AdminFeedController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn() => redirect()->route('login'));

Route::middleware('auth')->group(function () {
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');
});

Route::middleware('auth')->group(function () {
    // Dashboard & Admin Panel
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard');
    Route::get('/admin/panel', [AdminPanelController::class, 'index'])->name('admin.panel');

    // Trade CRUD
    Route::post('/admin/trades', [TradeManageController::class, 'store'])->name('admin.trades.store');
    Route::post('/admin/trades/import', [TradeManageController::class, 'import'])->name('admin.trades.import');
    Route::put('/admin/trades/{trade}', [TradeManageController::class, 'update'])->name('admin.trades.update');
    Route::delete('/admin/trades/{trade}', [TradeManageController::class, 'destroy'])->name('admin.trades.destroy');

    // IB Partners
    Route::get('/admin/ib-partners', [IbPartnerPageController::class, 'index'])->name('admin.ib-partners');
    Route::post('/admin/ib/member', [IbPartnerManageController::class, 'storeMember'])->name('admin.ib.member.store');
    Route::post('/admin/ib/partner', [IbPartnerManageController::class, 'storePartner'])->name('admin.ib.partner.store');
    Route::get('/admin/ib/search', [IbPartnerManageController::class, 'search'])->name('admin.ib.search');
    Route::get('/admin/ib/sync', [IbPartnerManageController::class, 'syncMembers'])->name('admin.ib.sync');

    // CSV Analytics
    Route::get('/admin/csv-analytics', [CsvAnalyticsController::class, 'index'])->name('admin.csv-analytics');

    // Community Management
    Route::get('/admin/community', [AdminCommunityController::class, 'index'])->name('admin.community');
    Route::post('/admin/community/post', [AdminCommunityController::class, 'postAsAdmin'])->name('admin.community.post');
    Route::post('/admin/community/posts/{post}/approve', [AdminCommunityController::class, 'approvePost'])->name('admin.community.approve');
    Route::post('/admin/community/posts/approve-all', [AdminCommunityController::class, 'approveAllPosts'])->name('admin.community.approve-all');
    Route::post('/admin/community/posts/{post}/reject', [AdminCommunityController::class, 'rejectPost'])->name('admin.community.reject');
    Route::delete('/admin/community/posts/{post}', [AdminCommunityController::class, 'deletePost'])->name('admin.community.delete');
    Route::post('/admin/community/comments/{comment}/approve', [AdminCommunityController::class, 'approveComment'])->name('admin.community.comment.approve');
    Route::post('/admin/community/comments/approve-all', [AdminCommunityController::class, 'approveAllComments'])->name('admin.community.comments.approve-all');
    Route::post('/admin/community/comments/{comment}/reject', [AdminCommunityController::class, 'rejectComment'])->name('admin.community.comment.reject');
    Route::post('/admin/community/settings', [AdminCommunityController::class, 'updateSettings'])->name('admin.community.settings');

    // Market News Management
    Route::get('/admin/news', [AdminNewsController::class, 'index'])->name('admin.news');
    Route::post('/admin/news', [AdminNewsController::class, 'store'])->name('admin.news.store');
    Route::put('/admin/news/{newsItem}', [AdminNewsController::class, 'update'])->name('admin.news.update');
    Route::delete('/admin/news/{newsItem}', [AdminNewsController::class, 'destroy'])->name('admin.news.destroy');
    Route::post('/admin/news/sync', [AdminNewsController::class, 'syncFromApi'])->name('admin.news.sync');

    // Real-time Chat
    Route::get('/admin/chat', [AdminChatController::class, 'index'])->name('admin.chat');
    Route::get('/admin/chat/token', [AdminChatController::class, 'getToken'])->name('admin.chat.token');
    Route::post('/admin/chat/send', [AdminChatController::class, 'sendMessage'])->name('admin.chat.send');

    // Unified Feed (Telegram-like)
    Route::get('/admin/feed', [AdminFeedController::class, 'index'])->name('admin.feed');
    Route::post('/admin/feed/post', [AdminFeedController::class, 'postAsAdmin'])->name('admin.feed.post');
    Route::post('/admin/feed/posts/{post}/approve', [AdminFeedController::class, 'approvePost'])->name('admin.feed.approve');
    Route::post('/admin/feed/posts/{post}/reject', [AdminFeedController::class, 'rejectPost'])->name('admin.feed.reject');
    Route::delete('/admin/feed/posts/{post}', [AdminFeedController::class, 'deletePost'])->name('admin.feed.delete');
    Route::post('/admin/feed/posts/{post}/pin', [AdminFeedController::class, 'togglePin'])->name('admin.feed.pin');
    Route::post('/admin/feed/comments/{comment}/approve', [AdminFeedController::class, 'approveComment'])->name('admin.feed.comment.approve');
    Route::post('/admin/feed/comments/{comment}/reject', [AdminFeedController::class, 'rejectComment'])->name('admin.feed.comment.reject');
    Route::get('/admin/feed/pending-count', [AdminFeedController::class, 'getPendingCount'])->name('admin.feed.pending-count');

    // Settings
    Route::post('/admin/settings', [SettingsController::class, 'update'])->name('admin.settings.update');
});

require __DIR__.'/auth.php';

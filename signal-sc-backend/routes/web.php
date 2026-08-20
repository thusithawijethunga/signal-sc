<?php

use App\Http\Controllers\DashboardController;
use App\Http\Controllers\AdminPanelController;
use App\Http\Controllers\IbPartnerPageController;
use App\Http\Controllers\CsvAnalyticsController;
use App\Http\Controllers\GoogleSheetsController;
use App\Http\Controllers\IbPartnerManageController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\TradeManageController;
use App\Http\Controllers\ProfileController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn() => redirect()->route('login'));

Route::middleware('auth')->group(function () {
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');
});

Route::middleware('auth')->group(function () {
    // 4 main pages
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard');
    Route::get('/admin/panel', [AdminPanelController::class, 'index'])->name('admin.panel');
    Route::get('/admin/ib-partners', [IbPartnerPageController::class, 'index'])->name('admin.ib-partners');
    Route::get('/admin/csv-analytics', [CsvAnalyticsController::class, 'index'])->name('admin.csv-analytics');

    // Trade CRUD (used by admin panel)
    Route::post('/admin/trades', [TradeManageController::class, 'store'])->name('admin.trades.store');
    Route::post('/admin/trades/import', [TradeManageController::class, 'import'])->name('admin.trades.import');
    Route::put('/admin/trades/{trade}', [TradeManageController::class, 'update'])->name('admin.trades.update');
    Route::delete('/admin/trades/{trade}', [TradeManageController::class, 'destroy'])->name('admin.trades.destroy');

    // IB Partner actions (used by ib-partners page)
    Route::post('/admin/ib/member', [IbPartnerManageController::class, 'storeMember'])->name('admin.ib.member.store');
    Route::post('/admin/ib/partner', [IbPartnerManageController::class, 'storePartner'])->name('admin.ib.partner.store');
    Route::get('/admin/ib/search', [IbPartnerManageController::class, 'search'])->name('admin.ib.search');

    // Settings
    Route::post('/admin/settings', [SettingsController::class, 'update'])->name('admin.settings.update');

    // Google Sheets sync (server-side)
    Route::get('/admin/google-sheets/test', [GoogleSheetsController::class, 'test'])->name('admin.gs.test');
    Route::post('/admin/google-sheets/sync', [GoogleSheetsController::class, 'sync'])->name('admin.gs.sync');
});

require __DIR__.'/auth.php';

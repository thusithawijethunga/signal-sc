<?php

use App\Http\Controllers\DashboardController;
use App\Http\Controllers\SignalManageController;
use App\Http\Controllers\TradeManageController;
use App\Http\Controllers\IbPartnerManageController;
use App\Http\Controllers\CsvManageController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\ProfileController;
use Illuminate\Support\Facades\Route;

Route::get('/', fn() => redirect()->route('login'));

Route::middleware('auth')->group(function () {
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');
});

Route::middleware('auth')->group(function () {
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard');

    Route::prefix('admin/signals')->name('admin.signals.')->group(function () {
        Route::get('/', [SignalManageController::class, 'index'])->name('index');
        Route::get('/create', [SignalManageController::class, 'create'])->name('create');
        Route::post('/', [SignalManageController::class, 'store'])->name('store');
        Route::get('/{signal}/edit', [SignalManageController::class, 'edit'])->name('edit');
        Route::put('/{signal}', [SignalManageController::class, 'update'])->name('update');
        Route::delete('/{signal}', [SignalManageController::class, 'destroy'])->name('destroy');
    });

    Route::prefix('admin/trades')->name('admin.trades.')->group(function () {
        Route::get('/', [TradeManageController::class, 'index'])->name('index');
        Route::post('/', [TradeManageController::class, 'store'])->name('store');
        Route::put('/{trade}', [TradeManageController::class, 'update'])->name('update');
        Route::delete('/{trade}', [TradeManageController::class, 'destroy'])->name('destroy');
    });

    Route::prefix('admin/ib-partners')->name('admin.ib.')->group(function () {
        Route::get('/', [IbPartnerManageController::class, 'index'])->name('index');
        Route::post('/member', [IbPartnerManageController::class, 'storeMember'])->name('member.store');
        Route::post('/partner', [IbPartnerManageController::class, 'storePartner'])->name('partner.store');
        Route::get('/search', [IbPartnerManageController::class, 'search'])->name('search');
    });

    Route::prefix('admin/csv')->name('admin.csv.')->group(function () {
        Route::get('/', [CsvManageController::class, 'index'])->name('index');
        Route::post('/upload', [CsvManageController::class, 'upload'])->name('upload');
    });

    Route::prefix('admin/settings')->name('admin.settings.')->group(function () {
        Route::get('/', [SettingsController::class, 'index'])->name('index');
        Route::post('/', [SettingsController::class, 'update'])->name('update');
    });
});

require __DIR__.'/auth.php';

<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\SignalController;
use App\Http\Controllers\Api\TradeController;
use App\Http\Controllers\Api\CommunityController;
use App\Http\Controllers\Api\NewsController;
use App\Http\Controllers\Api\VipController;
use App\Http\Controllers\Api\IbPartnerController;
use App\Http\Controllers\Api\CsvController;
use App\Http\Controllers\Api\SyncController;
use Illuminate\Support\Facades\Route;

Route::post('/auth/login', [AuthController::class, 'login']);
Route::post('/auth/register', [AuthController::class, 'register']);

Route::middleware('api.auth')->group(function () {
    Route::get('/auth/me', [AuthController::class, 'me']);
    Route::post('/auth/logout', [AuthController::class, 'logout']);

    Route::get('/signals', [SignalController::class, 'index']);
    Route::post('/signals', [SignalController::class, 'store']);
    Route::put('/signals/{signal}', [SignalController::class, 'update']);
    Route::delete('/signals/{signal}', [SignalController::class, 'destroy']);
    Route::post('/signals/{signal}/react', [SignalController::class, 'react']);

    Route::get('/trades', [TradeController::class, 'index']);
    Route::get('/trades/summary', [TradeController::class, 'summary']);
    Route::post('/trades', [TradeController::class, 'store']);
    Route::put('/trades/{trade}', [TradeController::class, 'update']);
    Route::delete('/trades/{trade}', [TradeController::class, 'destroy']);

    Route::get('/community/posts', [CommunityController::class, 'posts']);
    Route::post('/community/posts', [CommunityController::class, 'storePost']);
    Route::delete('/community/posts/{post}', [CommunityController::class, 'destroyPost']);
    Route::get('/community/posts/{post}/comments', [CommunityController::class, 'comments']);
    Route::post('/community/posts/{post}/comments', [CommunityController::class, 'storeComment']);
    Route::post('/community/posts/{post}/react', [CommunityController::class, 'reactPost']);

    Route::get('/news', [NewsController::class, 'index']);
    Route::post('/news/sync', [NewsController::class, 'sync']);
    Route::post('/news', [NewsController::class, 'store']);

    Route::get('/vip/leaderboard', [VipController::class, 'index']);
    Route::post('/vip/sync', [VipController::class, 'sync']);
    Route::post('/vip', [VipController::class, 'store']);

    Route::get('/partners/members', [IbPartnerController::class, 'members']);
    Route::post('/partners/members', [IbPartnerController::class, 'storeMember']);
    Route::get('/partners/list', [IbPartnerController::class, 'partners']);
    Route::post('/partners', [IbPartnerController::class, 'storePartner']);
    Route::post('/partners/search', [IbPartnerController::class, 'searchMember']);

    Route::post('/csv/upload', [CsvController::class, 'upload']);
    Route::get('/csv/uploads', [CsvController::class, 'uploads']);
    Route::get('/csv/{csvUpload}/analysis', [CsvController::class, 'analysis']);

    Route::post('/sync/push', [SyncController::class, 'push']);
    Route::get('/sync/pull', [SyncController::class, 'pull']);
});

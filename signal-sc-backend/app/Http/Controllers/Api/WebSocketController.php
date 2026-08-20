<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\CentrifugoService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class WebSocketController extends Controller
{
    public function token(Request $request, CentrifugoService $centrifugo): JsonResponse
    {
        $user = $request->user();
        $userId = $user ? (string) $user->id : 'web_' . Str::random(8);

        $token = $centrifugo->generateConnectionToken($userId);

        return response()->json([
            'token' => $token,
            'channels' => [
                'trading' => CentrifugoService::CHANNEL_TRADING,
                'trades' => CentrifugoService::CHANNEL_TRADES,
                'news' => CentrifugoService::CHANNEL_NEWS,
                'community' => CentrifugoService::CHANNEL_COMMUNITY,
                'broadcast' => CentrifugoService::CHANNEL_BROADCAST,
            ],
            'expires_in' => config('centrifugo.token_ttl'),
            'user_id' => $userId,
        ]);
    }
}

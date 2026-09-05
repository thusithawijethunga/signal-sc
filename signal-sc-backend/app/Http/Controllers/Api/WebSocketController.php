<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\CentrifugoService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class WebSocketController extends Controller
{
    public function token(Request $request, CentrifugoService $centrifugo): JsonResponse
    {
        // Support both auth methods:
        // 1. Bearer token auth (mobile app via api.auth middleware)
        // 2. Session/cookie auth (admin panel — Laravel web session)
        $user = $request->user() ?? Auth::user();

        if (! $user) {
            return response()->json(['message' => 'Unauthorized'], 401);
        }

        $token = $centrifugo->generateConnectionToken((string) $user->id, [
            'name' => $user->name,
            'role' => $user->role,
        ]);

        $apiUrl = config('centrifugo.api_url', '');
        $wsUrl = str_replace('/api', '/connection/websocket', $apiUrl);
        $wsUrl = str_replace('https://', 'wss://', $wsUrl);
        $wsUrl = str_replace('http://', 'ws://', $wsUrl);

        return response()->json([
            'token' => $token,
            'ws_url' => $wsUrl,
            'channels' => [
                'trading' => CentrifugoService::CHANNEL_TRADING,
                'trades' => CentrifugoService::CHANNEL_TRADES,
                'news' => CentrifugoService::CHANNEL_NEWS,
                'community' => CentrifugoService::CHANNEL_COMMUNITY,
                'broadcast' => CentrifugoService::CHANNEL_BROADCAST,
            ],
            'expires_in' => config('centrifugo.token_ttl'),
            'user_id' => $user->id,
        ]);
    }
}

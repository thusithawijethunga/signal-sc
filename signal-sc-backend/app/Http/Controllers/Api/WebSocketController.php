<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\CentrifugoService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class WebSocketController extends Controller
{
    public function token(Request $request, CentrifugoService $centrifugo): JsonResponse
    {
        // Route is now behind the api.auth middleware, so $request->user() is
        // guaranteed to be the real authenticated user - no more anonymous
        // 'web_xxxxxxxx' fallback id.
        $user = $request->user();

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

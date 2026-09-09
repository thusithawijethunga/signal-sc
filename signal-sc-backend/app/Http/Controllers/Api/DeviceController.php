<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\DeviceToken;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DeviceController extends Controller
{
    /**
     * Register (or refresh) this install's FCM token.
     * Same token re-registered by another user is moved to that user —
     * one device never keeps another user's push traffic.
     */
    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'token' => 'required|string|max:512',
            'platform' => 'sometimes|string|max:16',
        ]);

        $user = $request->user();
        if (! $user) {
            return response()->json(['message' => 'Unauthorized'], 401);
        }

        DeviceToken::updateOrCreate(
            ['token' => $validated['token']],
            [
                'user_id' => $user->id,
                'platform' => $validated['platform'] ?? 'android',
            ]
        );

        return response()->json(['message' => 'Device registered']);
    }

    /**
     * Remove an FCM token (logout / uninstall cleanup, best-effort).
     */
    public function destroy(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'token' => 'required|string|max:512',
        ]);

        DeviceToken::where('token', $validated['token'])->delete();

        return response()->json(['message' => 'Device unregistered']);
    }
}

<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\PresenceService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class PresenceController extends Controller
{
    /**
     * Device heartbeat — call on login, on foreground/background switch and
     * every ~60s while the app's service is alive. No special permission
     * needed client-side: only basic Build info is collected.
     */
    public function heartbeat(Request $request, PresenceService $presence): JsonResponse
    {
        $validated = $request->validate([
            'device_key' => 'required|string|max:128',
            'state' => 'sometimes|string|in:online,background',
            'device' => 'sometimes|array',
            'device.brand' => 'sometimes|string|max:64',
            'device.model' => 'sometimes|string|max:64',
            'device.android' => 'sometimes|string|max:16',
            'device.sdk' => 'sometimes|integer',
            'device.app_version' => 'sometimes|string|max:32',
        ]);

        $user = $request->user();
        if (! $user) {
            return response()->json(['message' => 'Unauthorized'], 401);
        }

        $row = $presence->heartbeat(
            $user,
            $validated['device_key'],
            $validated['state'] ?? 'online',
            $validated['device'] ?? []
        );

        return response()->json([
            'message' => 'ok',
            'state' => $row->state,
            'ttl_seconds' => PresenceService::TTL_SECONDS,
        ]);
    }

    /** Explicit goodbye — call on logout before the api_token is cleared. */
    public function offline(Request $request, PresenceService $presence): JsonResponse
    {
        $validated = $request->validate([
            'device_key' => 'required|string|max:128',
        ]);

        $user = $request->user();
        if (! $user) {
            return response()->json(['message' => 'Unauthorized'], 401);
        }

        $presence->markOffline($user, $validated['device_key']);

        return response()->json(['message' => 'ok']);
    }
}

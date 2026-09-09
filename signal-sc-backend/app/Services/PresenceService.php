<?php

namespace App\Services;

use App\Models\DevicePresence;
use App\Models\User;
use Illuminate\Support\Facades\Log;

/**
 * Live presence board: which users/devices are online right now.
 *
 * The app heartbeats every ~60s while its foreground service is alive
 * (state = online when UI visible, background otherwise). Rows older than
 * the TTL are considered gone (killed app, dead battery, uninstall).
 * Explicit logout deletes the row immediately.
 *
 * Every first-seen / state-change / goodbye is also published on the
 * `presence:devices` Centrifugo channel so the admin page updates live.
 */
class PresenceService
{
    public const CHANNEL = 'presence:devices';

    /** Seconds without heartbeat after which a device counts as gone. */
    public const TTL_SECONDS = 150;

    public function __construct(private CentrifugoService $centrifugo) {}

    /**
     * @param array{brand?:string,model?:string,android?:string,sdk?:int,app_version?:string} $device
     */
    public function heartbeat(User $user, string $deviceKey, string $state, array $device = []): DevicePresence
    {
        $state = $state === 'background' ? 'background' : 'online';

        $row = DevicePresence::firstOrNew([
            'user_id' => $user->id,
            'device_key' => $deviceKey,
        ]);

        $isNew = ! $row->exists;
        $stateChanged = ! $isNew && $row->state !== $state;

        $row->fill([
            'device' => $device,
            'state' => $state,
            'last_seen_at' => now(),
        ])->save();

        if ($isNew || $stateChanged) {
            $this->broadcast($row, $isNew ? 'online' : 'state');
        }

        return $row;
    }

    public function markOffline(User $user, string $deviceKey): void
    {
        $row = DevicePresence::where('user_id', $user->id)
            ->where('device_key', $deviceKey)
            ->first();

        if ($row) {
            $row->delete();
            $this->broadcast($row, 'offline');
        }
    }

    /** Drop rows whose heartbeat expired (killed apps that never said goodbye). */
    public function prune(): int
    {
        return DevicePresence::where('last_seen_at', '<', now()->subSeconds(self::TTL_SECONDS))->delete();
    }

    /**
     * Current live board: every device seen within the TTL.
     *
     * @return array{devices: array, users_online: int, devices_online: int, background: int}
     */
    public function snapshot(): array
    {
        $this->prune();

        $rows = DevicePresence::with('user:id,name,email')
            ->where('last_seen_at', '>=', now()->subSeconds(self::TTL_SECONDS))
            ->orderByDesc('last_seen_at')
            ->get();

        $devices = $rows->map(fn (DevicePresence $row) => [
            'user_id' => $row->user_id,
            'name' => $row->user?->name ?? ('User #' . $row->user_id),
            'email' => $row->user?->email ?? '',
            'device_key' => $row->device_key,
            'device' => $row->device ?? [],
            'state' => $row->state,
            'last_seen_at' => $row->last_seen_at?->toISOString(),
            'last_seen_human' => $row->last_seen_at?->diffForHumans(),
        ])->values()->all();

        return [
            'devices' => $devices,
            'users_online' => collect($devices)->pluck('user_id')->unique()->count(),
            'devices_online' => count($devices),
            'background' => collect($devices)->where('state', 'background')->count(),
            'server_time' => now()->toISOString(),
        ];
    }

    private function broadcast(DevicePresence $row, string $event): void
    {
        try {
            $user = $row->user ?? User::find($row->user_id);
            $this->centrifugo->publish(self::CHANNEL, [
                'event' => $event, // online | state | offline
                'user_id' => $row->user_id,
                'name' => $user?->name ?? ('User #' . $row->user_id),
                'email' => $user?->email ?? '',
                'device_key' => $row->device_key,
                'device' => $row->device ?? [],
                'state' => $event === 'offline' ? 'offline' : $row->state,
                'timestamp' => now()->toISOString(),
            ]);
        } catch (\Throwable $e) {
            Log::warning('Presence: broadcast failed: ' . $e->getMessage());
        }
    }
}

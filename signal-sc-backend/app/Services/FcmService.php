<?php

namespace App\Services;

use App\Models\DeviceToken;
use Illuminate\Support\Facades\Log;
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\MulticastSendReport;

/**
 * Sends FCM data messages to registered devices.
 *
 * Data-only + high priority: Play services wakes the app in ANY state
 * (foreground / background / killed) and SignalFirebaseMessagingService
 * renders the notification. Dedupe with live WebSocket events happens
 * client-side via the shared signal_no key.
 */
class FcmService
{
    private ?\Kreait\Firebase\Messaging $messaging = null;
    private bool $configured = false;

    public function __construct()
    {
        $credentials = (string) config('services.fcm.credentials_file', '');

        // Allow relative paths (resolved from the project root).
        if ($credentials !== '' && ! str_starts_with($credentials, '/') && ! preg_match('/^[A-Z]:\\\\/i', $credentials)) {
            $credentials = base_path($credentials);
        }

        if ($credentials !== '' && is_readable($credentials)) {
            try {
                $this->messaging = (new Factory)
                    ->withServiceAccount($credentials)
                    ->createMessaging();
                $this->configured = true;
            } catch (\Throwable $e) {
                Log::error('FCM: failed to initialize: ' . $e->getMessage());
            }
        }
    }

    public function isConfigured(): bool
    {
        return $this->configured;
    }

    /**
     * Push a signal broadcast to every registered device.
     * Skips reaction chatter (client filters those anyway).
     */
    public function pushBroadcast(string $title, string $body, string $type = 'info', array $extra = []): int
    {
        if (! $this->configured) {
            return 0;
        }

        if ($type === 'signal_reaction') {
            return 0;
        }

        $tokens = DeviceToken::query()->pluck('token')->all();
        if (empty($tokens)) {
            return 0;
        }

        $data = [
            'id' => (string) ($extra['id'] ?? \Illuminate\Support\Str::uuid()->toString()),
            'title' => mb_substr($title, 0, 200),
            'body' => mb_substr($body, 0, 500),
            'type' => $type,
            'signal_id' => (string) ($extra['signal_id'] ?? 0),
            'signal_no' => (string) ($extra['signal_no'] ?? 0),
            'trade_id' => (string) ($extra['trade_id'] ?? 0),
            'action' => (string) ($extra['action'] ?? ''),
            'result' => (string) ($extra['result'] ?? ''),
        ];

        try {
            // Data-only message: no 'notification' block, so the app (not the
            // system tray) always handles display — consistent in every state.
            $message = CloudMessage::fromArray([
                'data' => $data,
                'android' => [
                    'priority' => 'high',
                    // Collapse rapid updates for the same signal into one slot
                    // while the device is offline/dozing.
                    'collapse_key' => 'sig_' . $data['signal_no'],
                ],
            ]);

            /** @var MulticastSendReport $report */
            $report = $this->messaging->sendMulticast($message, $tokens);

            $this->pruneDeadTokens($report);

            $sent = count($report->successes()->getItems());
            $failed = count($report->failures()->getItems());
            Log::info("FCM: broadcast [{$type}] sent={$sent} failed={$failed}");

            return $sent;
        } catch (\Throwable $e) {
            Log::error('FCM: broadcast exception: ' . $e->getMessage());
            return 0;
        }
    }

    /**
     * Drop tokens the provider reports as dead (uninstalls, revoked
     * permissions) so the table stays healthy automatically.
     */
    private function pruneDeadTokens(MulticastSendReport $report): void
    {
        $dead = array_merge($report->unknownTokens(), $report->invalidTokens());
        if (empty($dead)) {
            return;
        }

        $values = array_map(fn ($t) => (string) $t, $dead);
        $deleted = DeviceToken::whereIn('token', $values)->delete();
        if ($deleted > 0) {
            Log::info("FCM: pruned {$deleted} dead device token(s)");
        }
    }
}

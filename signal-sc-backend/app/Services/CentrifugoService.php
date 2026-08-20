<?php

namespace App\Services;

use Firebase\JWT\JWT;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;

class CentrifugoService
{
    public const CHANNEL_TRADING   = 'trading:signals';
    public const CHANNEL_BROADCAST = 'notifications:broadcast';
    public const CHANNEL_TRADES    = 'trading:trades';
    public const CHANNEL_NEWS      = 'trading:news';
    public const CHANNEL_COMMUNITY = 'trading:community';

    private string $apiUrl;
    private string $apiKey;
    private string $hmacSecretKey;
    private int    $tokenTtl;

    public function __construct()
    {
        $this->apiUrl        = config('centrifugo.api_url');
        $this->apiKey        = config('centrifugo.api_key');
        $this->hmacSecretKey = config('centrifugo.token_hmac_secret_key');
        $this->tokenTtl      = config('centrifugo.token_ttl');
    }

    public function generateConnectionToken(string|int $userId, array $info = []): string
    {
        $now     = time();
        $payload = [
            'sub' => (string) $userId,
            'iat' => $now,
            'exp' => $now + $this->tokenTtl,
        ];
        if (! empty($info)) {
            $payload['info'] = $info;
        }
        return JWT::encode($payload, $this->hmacSecretKey, 'HS256');
    }

    public function generateSubscriptionToken(string|int $userId, string $channel): string
    {
        $now = time();
        return JWT::encode([
            'sub'     => (string) $userId,
            'channel' => $channel,
            'iat'     => $now,
            'exp'     => $now + $this->tokenTtl,
        ], $this->hmacSecretKey, 'HS256');
    }

    public function publish(string $channel, array $data): bool
    {
        try {
            $response = Http::withHeaders([
                'Content-Type' => 'application/json',
                'X-API-Key'    => $this->apiKey,
            ])->post("{$this->apiUrl}/publish", [
                'channel' => $channel,
                'data'    => $data,
            ]);

            if ($response->successful()) {
                Log::info("Centrifugo: published to [{$channel}]", [
                    'id' => $data['id'] ?? 'n/a',
                ]);
                return true;
            }

            Log::error("Centrifugo: publish failed", [
                'channel'  => $channel,
                'status'   => $response->status(),
                'response' => $response->body(),
            ]);
            return false;

        } catch (\Throwable $e) {
            Log::error("Centrifugo: publish exception", [
                'channel' => $channel,
                'error'   => $e->getMessage(),
            ]);
            return false;
        }
    }

    public function broadcastNotification(string $title, string $body, string $type = 'info', array $extra = []): bool
    {
        $data = array_merge([
            'id'        => (string) Str::uuid(),
            'title'     => $title,
            'body'      => $body,
            'type'      => $type,
            'timestamp' => now()->toISOString(),
        ], $extra);

        return $this->publish(self::CHANNEL_BROADCAST, $data);
    }

    public function getChannelInfo(string $channel): array
    {
        try {
            $response = Http::withHeaders([
                'Content-Type' => 'application/json',
                'X-API-Key'    => $this->apiKey,
            ])->post("{$this->apiUrl}/presence", ['channel' => $channel]);

            return $response->json() ?? [];
        } catch (\Throwable $e) {
            Log::error("Centrifugo: presence error", ['error' => $e->getMessage()]);
            return [];
        }
    }
}

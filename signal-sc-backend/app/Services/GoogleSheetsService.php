<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class GoogleSheetsService
{
    protected string $url;

    public function __construct()
    {
        $this->url = config('services.google_sheets.url', '');
    }

    public function isConfigured(): bool
    {
        return ! empty($this->url);
    }

    public function getUrl(): string
    {
        return $this->url;
    }

    public function testConnection(): array
    {
        if (! $this->isConfigured()) {
            return ['ok' => false, 'message' => 'Google Sheets URL not configured'];
        }

        try {
            $response = Http::timeout(20)->get($this->url);
            return [
                'ok' => $response->successful(),
                'status' => $response->status(),
                'count' => is_array($response->json()) ? count($response->json()) : 0,
            ];
        } catch (\Throwable $e) {
            Log::error('Google Sheets test connection error', ['error' => $e->getMessage()]);
            return ['ok' => false, 'error' => $e->getMessage()];
        }
    }

    public function fetchTrades(): array
    {
        if (! $this->isConfigured()) {
            return [];
        }

        try {
            $response = Http::timeout(20)->get($this->url);
            $data = $response->json();
            return is_array($data) ? $data : [];
        } catch (\Throwable $e) {
            Log::error('Google Sheets fetch error', ['error' => $e->getMessage()]);
            return [];
        }
    }

    public function syncTrade($trade, ?string $hitType = null): bool
    {
        if (! $this->isConfigured()) {
            return false;
        }

        $payload = [
            'no' => $trade->no,
            'date' => $trade->date?->format('Y-m-d'),
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'entry1' => $trade->entry1,
            'entry2' => $trade->entry2,
            'sl' => $trade->sl,
            'tp1' => $trade->tp1,
            'tp2' => $trade->tp2,
            'tp3' => $trade->tp3,
            'tp4' => $trade->tp4,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'channel' => $trade->channel,
        ];

        if ($hitType) {
            $payload['hit_type'] = $hitType;
        }

        try {
            $response = Http::withOptions([
                'allow_redirects' => [
                    'max' => 5,
                    'strict' => true, // preserve POST method across Google's 302 redirect
                    'protocols' => ['https', 'http'],
                ],
            ])
                ->asForm()
                ->timeout(20)
                ->post($this->url, ['data' => json_encode($payload)]);

            if ($response->successful()) {
                Log::info('Google Sheets: trade synced', ['no' => $trade->no, 'hit_type' => $hitType]);
                return true;
            }

            Log::error('Google Sheets: sync failed', [
                'no' => $trade->no,
                'status' => $response->status(),
                'body' => $response->body(),
            ]);
            return false;
        } catch (\Throwable $e) {
            Log::error('Google Sheets sync error', ['error' => $e->getMessage()]);
            return false;
        }
    }
}

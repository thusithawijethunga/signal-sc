<?php

namespace App\Http\Controllers;

use App\Models\Trade;
use App\Services\GoogleSheetsService;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GoogleSheetsController extends Controller
{
    public function test(GoogleSheetsService $sheets): JsonResponse
    {
        return response()->json($sheets->testConnection());
    }

    public function sync(GoogleSheetsService $sheets, Request $request): JsonResponse
    {
        if (! $sheets->isConfigured()) {
            return response()->json(['ok' => false, 'message' => 'Google Sheets URL not configured'], 400);
        }

        $rows = $sheets->fetchTrades();
        if (empty($rows)) {
            return response()->json(['ok' => false, 'message' => 'No data returned from Google Sheets'], 400);
        }

        $count = 0;
        foreach ($rows as $row) {
            $no = $row['no'] ?? null;
            if (! $no) {
                continue;
            }

            $data = [
                'date' => $this->parseDate($row['date'] ?? null),
                'pair' => $row['pair'] ?? 'XAU/USD',
                'direction' => strtoupper($row['direction'] ?? ''),
                'entry1' => $this->num($row['entry1'] ?? null),
                'entry2' => $this->num($row['entry2'] ?? null),
                'sl' => $this->num($row['sl'] ?? null),
                'tp1' => $this->num($row['tp1'] ?? null),
                'tp2' => $this->num($row['tp2'] ?? null),
                'tp3' => $this->num($row['tp3'] ?? null),
                'tp4' => $this->num($row['tp4'] ?? null),
                'pips' => (float) ($row['pips'] ?? 0),
                'profit' => (float) ($row['profit'] ?? 0),
                'result' => strtoupper($row['result'] ?? 'RUNNING'),
                'channel' => ($row['channel'] ?? '') ?: 'VIP',
                'user_id' => $request->user()->id,
            ];
            $data['no'] = (int) $no;

            Trade::updateOrCreate(['no' => (int) $no], $data);
            $count++;
        }

        return response()->json(['ok' => true, 'count' => $count]);
    }

    private function num($v)
    {
        if ($v === '' || $v === null) {
            return null;
        }
        return (float) $v;
    }

    private function parseDate($v)
    {
        if (! $v) {
            return now()->format('Y-m-d');
        }
        try {
            return Carbon::parse($v)->format('Y-m-d');
        } catch (\Exception $e) {
            return now()->format('Y-m-d');
        }
    }
}

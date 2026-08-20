<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\MarketNews;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class NewsController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        try {
            $query = MarketNews::query();

            if ($request->filled('currency')) {
                $query->where('currency', $request->currency);
            }
            if ($request->filled('impact')) {
                $query->where('impact', $request->impact);
            }

            $news = $query->orderBy('event_time', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($news);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch news', 'error' => $e->getMessage()], 500);
        }
    }

    public function sync(Request $request): JsonResponse
    {
        try {
            $response = Http::timeout(30)->get('https://nfp.ourforecast.com/calendar.json');

            if (!$response->successful()) {
                return response()->json(['message' => 'Failed to fetch from external API'], 502);
            }

            $data = $response->json();
            $events = $data['data'] ?? $data ?? [];
            $synced = 0;

            foreach ($events as $event) {
                $eventTime = $event['date'] ?? $event['event_time'] ?? null;
                $currency = $event['currency'] ?? $event['cur'] ?? '';
                $title = $event['title'] ?? $event['event'] ?? $event['name'] ?? '';
                $impact = $event['impact'] ?? $event['impact_level'] ?? '';
                $forecast = $event['forecast'] ?? null;
                $previous = $event['previous'] ?? null;
                $actual = $event['actual'] ?? null;

                if (empty($title)) {
                    continue;
                }

                MarketNews::updateOrCreate(
                    [
                        'event_time' => $eventTime,
                        'currency' => $currency,
                        'title' => $title,
                    ],
                    [
                        'impact' => $impact,
                        'forecast' => $forecast,
                        'previous' => $previous,
                        'actual' => $actual,
                    ]
                );
                $synced++;
            }

            return response()->json(['message' => 'Sync completed', 'synced_count' => $synced]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Sync failed', 'error' => $e->getMessage()], 500);
        }
    }

    public function store(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'title' => 'required|string',
                'event_time' => 'required|date',
                'currency' => 'required|string',
                'impact' => 'nullable|string',
            ]);

            $news = MarketNews::create($request->only([
                'title', 'event_time', 'currency', 'impact',
                'forecast', 'previous', 'actual', 'description',
            ]));

            return response()->json($news, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create news event', 'error' => $e->getMessage()], 500);
        }
    }
}

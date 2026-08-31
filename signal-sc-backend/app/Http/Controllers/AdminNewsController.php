<?php

namespace App\Http\Controllers;

use App\Models\MarketNews;
use App\Events\MarketNewsCreated;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class AdminNewsController extends Controller
{
    public function index(Request $request)
    {
        $query = MarketNews::query();

        if ($request->filled('currency')) {
            $query->where('currency', $request->currency);
        }
        if ($request->filled('impact')) {
            $query->where('impact', $request->impact);
        }

        $sort = $request->get('sort', 'event_time');
        if ($sort === 'newest') {
            $news = $query->orderBy('created_at', 'desc')->paginate(20);
        } else {
            $news = $query->orderBy('event_time', 'desc')->paginate(20);
        }
        $stats = [
            'total' => MarketNews::count(),
            'high' => MarketNews::where('impact', 'HIGH')->count(),
            'medium' => MarketNews::where('impact', 'MEDIUM')->count(),
            'low' => MarketNews::where('impact', 'LOW')->count(),
        ];

        return view('admin.news', compact('news', 'stats'));
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'event_time' => 'required|date',
            'currency' => 'required|string|max:10',
            'impact' => 'nullable|string|max:10',
            'forecast' => 'nullable|string|max:50',
            'previous' => 'nullable|string|max:50',
            'actual' => 'nullable|string|max:50',
            'description' => 'nullable|string',
        ]);

        $validated['impact'] = strtoupper($validated['impact'] ?? 'LOW');

        $news = MarketNews::create($validated);
        MarketNewsCreated::dispatch($news);

        return back()->with('success', 'News event created');
    }

    public function update(Request $request, MarketNews $newsItem)
    {
        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'event_time' => 'required|date',
            'currency' => 'required|string|max:10',
            'impact' => 'nullable|string|max:10',
            'forecast' => 'nullable|string|max:50',
            'previous' => 'nullable|string|max:50',
            'actual' => 'nullable|string|max:50',
            'description' => 'nullable|string',
        ]);

        $validated['impact'] = strtoupper($validated['impact'] ?? 'LOW');

        $newsItem->update($validated);

        return back()->with('success', 'News event updated');
    }

    public function destroy(MarketNews $newsItem)
    {
        $newsItem->delete();
        return back()->with('success', 'News event deleted');
    }

    public function syncFromApi()
    {
        $synced = 0;
        $errors = [];

        // ── Source 1: ForexFactory free JSON feed ──────
        try {
            $response = Http::timeout(20)->get('https://nfp.ourforecast.com/calendar.json', [
                'User-Agent' => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                'Accept' => 'application/json',
            ]);

            if ($response->successful()) {
                $data = $response->json();
                $events = is_array($data) ? $data : [];

                foreach ($events as $event) {
                    $title = $event['title'] ?? $event['event'] ?? $event['name'] ?? null;
                    if (empty($title)) continue;

                    $currency = strtoupper($event['country'] ?? $event['currency'] ?? '');
                    $impact = strtoupper($event['impact'] ?? 'Low');
                    if (!in_array($impact, ['HIGH', 'MEDIUM', 'LOW'])) $impact = 'LOW';

                    $eventTime = $event['date'] ?? $event['event_time'] ?? null;

                    MarketNews::updateOrCreate(
                        ['title' => $title, 'event_time' => $eventTime],
                        [
                            'currency' => $currency,
                            'impact' => $impact,
                            'forecast' => $event['forecast'] ?? null,
                            'previous' => $event['previous'] ?? null,
                            'actual' => $event['actual'] ?? null,
                            'description' => "ForexFactory Calendar ({$currency})",
                        ]
                    );
                    $synced++;
                }
            }
        } catch (\Throwable $e) {
            $errors[] = 'ForexFactory: ' . $e->getMessage();
        }

        // ── Source 2: Xoomar Pulse free calendar (US events, no key) ──
        try {
            $from = now()->subDays(3)->format('Y-m-d');
            $to = now()->addDays(30)->format('Y-m-d');
            $response = Http::timeout(15)->get("https://xoomar.com/api/markets/calendar", [
                'from' => $from,
                'to' => $to,
            ]);

            if ($response->successful()) {
                $data = $response->json();
                $events = $data['data'] ?? $data ?? [];

                foreach ($events as $event) {
                    $title = $event['eventName'] ?? $event['event'] ?? null;
                    if (empty($title)) continue;

                    $impact = ucfirst($event['importance'] ?? 'low');
                    $eventTime = $event['scheduledAt'] ?? $event['date'] ?? null;

                    MarketNews::updateOrCreate(
                        ['title' => $title, 'event_time' => $eventTime],
                        [
                            'currency' => 'USD',
                            'impact' => strtoupper($impact),
                            'forecast' => $event['estimate'] ?? $event['forecast'] ?? null,
                            'previous' => $event['previous'] ?? null,
                            'actual' => $event['actual'] ?? null,
                            'description' => $event['source'] ? "Source: " . strtoupper($event['source']) : 'US Economic Calendar',
                        ]
                    );
                    $synced++;
                }
            }
        } catch (\Throwable $e) {
            $errors[] = 'Xoomar: ' . $e->getMessage();
        }

        // ── Source 3: FMP (if API key is configured) ──
        $apiKey = config('services.fmp.api_key', '');
        if (!empty($apiKey)) {
            try {
                $from = now()->subDays(3)->format('Y-m-d');
                $to = now()->addDays(30)->format('Y-m-d');
                $response = Http::timeout(20)->get("https://financialmodelingprep.com/stable/economic-calendar", [
                    'from' => $from,
                    'to' => $to,
                    'apikey' => $apiKey,
                ]);

                if ($response->successful()) {
                    $events = $response->json();
                    if (is_array($events)) {
                        $currencyMap = ['US'=>'USD','EU'=>'EUR','GB'=>'GBP','JP'=>'JPY','AU'=>'AUD','CA'=>'CAD','CH'=>'CHF','DE'=>'EUR','FR'=>'EUR'];
                        foreach ($events as $event) {
                            $title = $event['event'] ?? $event['name'] ?? null;
                            if (empty($title)) continue;
                            $country = strtoupper($event['country'] ?? '');
                            $currency = strtoupper($event['currency'] ?? $currencyMap[$country] ?? $country);
                            $impact = strtoupper($event['impact'] ?? 'Low');
                            if (!in_array($impact, ['HIGH','MEDIUM','LOW'])) $impact = 'LOW';

                            MarketNews::updateOrCreate(
                                ['title' => $title, 'event_time' => $event['date'] ?? null],
                                [
                                    'currency' => $currency,
                                    'impact' => $impact,
                                    'forecast' => $event['estimate'] ?? null,
                                    'previous' => $event['previous'] ?? null,
                                    'actual' => $event['actual'] ?? null,
                                    'description' => "FMP Economic Calendar",
                                ]
                            );
                            $synced++;
                        }
                    }
                }
            } catch (\Throwable $e) {
                $errors[] = 'FMP: ' . $e->getMessage();
            }
        }

        $msg = "Synced {$synced} economic events";
        if (!empty($errors)) {
            $msg .= " (warnings: " . implode('; ', $errors) . ")";
        }

        return back()->with('success', $msg);
    }
}

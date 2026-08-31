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

        $news = $query->orderBy('event_time', 'desc')->paginate(20);
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
        try {
            $apiKey = config('services.fmp.api_key', '');

            if (empty($apiKey)) {
                return back()->with('error', 'FMP API key not configured. Set FMP_API_KEY in .env');
            }

            // Fetch next 30 days of economic events
            $from = now()->subDays(3)->format('Y-m-d');
            $to = now()->addDays(30)->format('Y-m-d');

            $url = "https://financialmodelingprep.com/stable/economic-calendar?from={$from}&to={$to}&apikey={$apiKey}";

            $response = Http::timeout(30)->get($url);

            if (!$response->successful()) {
                $status = $response->status();
                if ($status === 402) {
                    return back()->with('error', 'FMP API requires a paid subscription. Using fallback API.');
                }
                if ($status === 401) {
                    return back()->with('error', 'Invalid FMP API key. Check FMP_API_KEY in .env');
                }
                return back()->with('error', 'FMP API returned HTTP ' . $status);
            }

            $events = $response->json();

            if (!is_array($events)) {
                return back()->with('error', 'Invalid response from FMP API');
            }

            $synced = 0;
            $currencyMap = [
                'US' => 'USD', 'EU' => 'EUR', 'GB' => 'GBP', 'JP' => 'JPY',
                'AU' => 'AUD', 'CA' => 'CAD', 'CH' => 'CHF', 'CN' => 'CNY',
                'NZ' => 'NZD', 'DE' => 'EUR', 'FR' => 'EUR', 'IT' => 'EUR',
                'ES' => 'EUR', 'KR' => 'KRW', 'IN' => 'INR', 'BR' => 'BRL',
                'MX' => 'MXN', 'RU' => 'RUB', 'SA' => 'SAR', 'SE' => 'SEK',
            ];

            foreach ($events as $event) {
                $title = $event['event'] ?? $event['name'] ?? null;
                if (empty($title)) continue;

                $country = strtoupper($event['country'] ?? '');
                $currency = strtoupper($event['currency'] ?? $currencyMap[$country] ?? $country);
                $impact = strtoupper($event['impact'] ?? 'Low');

                // Map impact levels
                if (!in_array($impact, ['HIGH', 'MEDIUM', 'LOW'])) {
                    $impact = 'LOW';
                }

                $eventTime = $event['date'] ?? null;

                MarketNews::updateOrCreate(
                    ['title' => $title, 'event_time' => $eventTime],
                    [
                        'currency' => $currency,
                        'impact' => $impact,
                        'forecast' => $event['estimate'] ?? $event['forecast'] ?? null,
                        'previous' => $event['previous'] ?? null,
                        'actual' => $event['actual'] ?? null,
                        'description' => "Economic event: {$title} ({$currency}) - Impact: {$impact}",
                    ]
                );
                $synced++;
            }

            return back()->with('success', "Synced {$synced} economic events from Financial Modeling Prep API ({$from} to {$to})");
        } catch (\Illuminate\Http\Client\ConnectionException $e) {
            return back()->with('error', 'Connection timeout. Please try again.');
        } catch (\Throwable $e) {
            return back()->with('error', 'Sync failed: ' . $e->getMessage());
        }
    }
}

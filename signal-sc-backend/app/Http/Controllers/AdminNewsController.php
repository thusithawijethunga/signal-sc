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
            $response = Http::timeout(30)->get('https://nfp.ourforecast.com/calendar.json');

            if (!$response->successful()) {
                return back()->with('error', 'Failed to fetch from external API');
            }

            $data = $response->json();
            $events = is_array($data) ? ($data['data'] ?? $data) : [];
            $synced = 0;

            foreach ($events as $event) {
                $eventTime = $event['date'] ?? $event['event_time'] ?? null;
                $currency = $event['currency'] ?? $event['cur'] ?? '';
                $title = $event['title'] ?? $event['event'] ?? $event['name'] ?? '';
                $impact = strtoupper($event['impact'] ?? $event['impact_level'] ?? 'LOW');
                $forecast = $event['forecast'] ?? null;
                $previous = $event['previous'] ?? null;
                $actual = $event['actual'] ?? null;

                if (empty($title)) continue;

                MarketNews::updateOrCreate(
                    ['event_time' => $eventTime, 'currency' => $currency, 'title' => $title],
                    [
                        'impact' => $impact,
                        'forecast' => $forecast,
                        'previous' => $previous,
                        'actual' => $actual,
                    ]
                );
                $synced++;
            }

            return back()->with('success', "Synced {$synced} news events from external API");
        } catch (\Throwable $e) {
            return back()->with('error', 'Sync failed: ' . $e->getMessage());
        }
    }
}

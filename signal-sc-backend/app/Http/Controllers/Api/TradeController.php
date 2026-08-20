<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Events\TradeCreated;
use App\Events\TradeUpdated;
use App\Events\TradeHit;
use App\Models\AccountBalance;
use App\Models\Trade;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class TradeController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        try {
            $query = Trade::query();

            if ($request->filled('result')) {
                $query->where('result', $request->result);
            }

            if ($request->filled('pair')) {
                $query->where('pair', $request->pair);
            }

            $period = $request->get('period', 'ALL');

            match ($period) {
                'THIS_WEEK' => $query->whereBetween('date', [now()->startOfWeek(), now()->endOfWeek()]),
                'LAST_WEEK' => $query->whereBetween('date', [now()->subWeek()->startOfWeek(), now()->subWeek()->endOfWeek()]),
                'LAST_MONTH' => $query->whereBetween('date', [now()->subMonth()->startOfMonth(), now()->subMonth()->endOfMonth()]),
                'CUSTOM_DATE' => $query->when($request->filled('date_from'), fn ($q) => $q->whereDate('date', '>=', $request->date_from))
                    ->when($request->filled('date_to'), fn ($q) => $q->whereDate('date', '<=', $request->date_to)),
                default => null,
            };

            $trades = $query->orderBy('date', 'desc')->orderBy('no', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($trades);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch trades', 'error' => $e->getMessage()], 500);
        }
    }

    public function summary(Request $request): JsonResponse
    {
        try {
            $userId = $request->user()->id;

            $totalTrades = Trade::where('user_id', $userId)->count();
            $wins = Trade::where('user_id', $userId)->where('result', 'WIN')->count();
            $losses = Trade::where('user_id', $userId)->where('result', 'LOSS')->count();
            $bes = Trade::where('user_id', $userId)->where('result', 'BREAKEVEN')->count();
            $totalPips = (float) Trade::where('user_id', $userId)->sum('pips');
            $totalProfit = (float) Trade::where('user_id', $userId)->sum('profit');
            $winRate = $totalTrades > 0 ? round(($wins / $totalTrades) * 100, 2) : 0;

            $balance = AccountBalance::where('user_id', $userId)->first();
            $currentBalance = $balance
                ? $balance->start_balance + $balance->deposit_balance - $balance->withdraw_balance + $totalProfit
                : 0;

            return response()->json([
                'total_trades' => $totalTrades,
                'wins' => $wins,
                'losses' => $losses,
                'bes' => $bes,
                'total_pips' => round($totalPips, 1),
                'total_profit' => round($totalProfit, 2),
                'win_rate' => $winRate,
                'current_balance' => round($currentBalance, 2),
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch summary', 'error' => $e->getMessage()], 500);
        }
    }

    public function store(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'pair' => 'required|string',
                'direction' => 'required|string',
                'date' => 'required|date',
            ]);

            $trade = Trade::create([
                'no' => Trade::max('no') + 1,
                'date' => $request->date,
                'pair' => $request->pair,
                'direction' => $request->direction,
                'entry1' => $request->get('entry1'),
                'entry2' => $request->get('entry2'),
                'sl' => $request->get('sl'),
                'tp1' => $request->get('tp1'),
                'tp2' => $request->get('tp2'),
                'tp3' => $request->get('tp3'),
                'tp4' => $request->get('tp4'),
                'pips' => $request->get('pips', 0),
                'profit' => $request->get('profit', 0),
                'result' => $request->get('result', 'RUNNING'),
                'channel' => $request->get('channel', 'VIP'),
                'user_id' => $request->user()->id,
            ]);

            TradeCreated::dispatch($trade);

            return response()->json($trade, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create trade', 'error' => $e->getMessage()], 500);
        }
    }

    public function update(Request $request, Trade $trade): JsonResponse
    {
        try {
            $oldResult = $trade->result;
            $trade->update($request->only([
                'pair', 'direction', 'entry1', 'entry2', 'sl',
                'tp1', 'tp2', 'tp3', 'tp4', 'pips', 'profit',
                'result', 'channel', 'date',
            ]));

            TradeUpdated::dispatch($trade, $oldResult);

            if ($oldResult !== $trade->result && $trade->result !== 'RUNNING') {
                $hitType = match($trade->result) {
                    'WIN' => 'TP', 'LOSS' => 'SL', 'BE' => 'BE', default => $trade->result,
                };
                TradeHit::dispatch($trade, $hitType);
            }

            return response()->json($trade);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to update trade', 'error' => $e->getMessage()], 500);
        }
    }

    public function destroy(Request $request, Trade $trade): JsonResponse
    {
        try {
            $trade->delete();
            return response()->json(['message' => 'Trade deleted']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to delete trade', 'error' => $e->getMessage()], 500);
        }
    }
}

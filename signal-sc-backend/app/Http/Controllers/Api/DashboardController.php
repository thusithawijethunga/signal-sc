<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\AccountBalance;
use App\Models\Trade;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    public function index(Request $request): JsonResponse
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

            $recentTrades = Trade::where('user_id', $userId)
                ->orderBy('date', 'desc')
                ->orderBy('no', 'desc')
                ->limit(10)
                ->get();

            $monthlyData = Trade::where('user_id', $userId)
                ->selectRaw('strftime("%Y-%m", date) as month, COUNT(*) as trades, SUM(pips) as total_pips, SUM(profit) as total_profit')
                ->groupBy('month')
                ->orderBy('month', 'desc')
                ->limit(12)
                ->get();

            $dailyPips = Trade::where('user_id', $userId)
                ->selectRaw('date, SUM(pips) as daily_pips')
                ->groupBy('date')
                ->orderBy('date', 'desc')
                ->limit(30)
                ->get();

            return response()->json([
                'total_trades' => $totalTrades,
                'wins' => $wins,
                'losses' => $losses,
                'bes' => $bes,
                'total_pips' => round($totalPips, 1),
                'total_profit' => round($totalProfit, 2),
                'win_rate' => $winRate,
                'current_balance' => round($currentBalance, 2),
                'recent_trades' => $recentTrades,
                'monthly_data' => $monthlyData,
                'daily_pips' => $dailyPips,
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch dashboard', 'error' => $e->getMessage()], 500);
        }
    }
}

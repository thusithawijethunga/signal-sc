<?php

namespace App\Http\Controllers;

use App\Models\Trade;
use App\Models\Signal;
use App\Models\AccountBalance;
use App\Models\IbMember;
use App\Models\CommunityPost;
use App\Models\VipMember;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();

        $totalTrades = Trade::where('user_id', $user->id)->count();
        $wins = Trade::where('user_id', $user->id)->where('result', 'win')->count();
        $losses = Trade::where('user_id', $user->id)->where('result', 'loss')->count();
        $bes = Trade::where('user_id', $user->id)->where('result', 'BE')->count();
        $totalProfit = (float) Trade::where('user_id', $user->id)->sum('profit');
        $winRate = $totalTrades > 0 ? round(($wins / $totalTrades) * 100, 2) : 0;

        $balance = AccountBalance::where('user_id', $user->id)->first();
        $currentBalance = $balance
            ? (float) $balance->start_balance + (float) $balance->deposit_balance - (float) $balance->withdraw_balance + $totalProfit
            : 0;

        $recentTrades = Trade::where('user_id', $user->id)
            ->latest('date')
            ->limit(10)
            ->get();

        $chartData = Trade::where('user_id', $user->id)
            ->selectRaw('date, SUM(profit) as daily_profit')
            ->groupBy('date')
            ->orderBy('date')
            ->get()
            ->map(fn ($row) => [
                'date' => $row->date->format('Y-m-d'),
                'profit' => (float) $row->daily_profit,
            ]);

        return view('dashboard', compact(
            'totalTrades',
            'wins',
            'losses',
            'bes',
            'totalProfit',
            'currentBalance',
            'winRate',
            'recentTrades',
            'chartData'
        ));
    }
}

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
        $isAdmin = $user->role === 'admin';

        $query = $isAdmin ? Trade::query() : Trade::where('user_id', $user->id);

        $totalTrades = (clone $query)->count();
        $wins = (clone $query)->where('result', 'WIN')->count();
        $losses = (clone $query)->where('result', 'LOSS')->count();
        $bes = (clone $query)->where('result', 'BE')->count();
        $totalProfit = (float) (clone $query)->sum('profit');
        $netPips = (float) (clone $query)->sum('pips');
        $lossPips = (float) (clone $query)->where('pips', '<', 0)->sum('pips');
        $winRate = $totalTrades > 0 ? round(($wins / $totalTrades) * 100, 1) : 0;

        $balanceQuery = $isAdmin ? AccountBalance::query() : AccountBalance::where('user_id', $user->id);
        $balance = (clone $balanceQuery)->first();
        $startBalance = $balance ? (float) $balance->start_balance : 1000;
        $depositBalance = $balance ? (float) $balance->deposit_balance : 0;
        $withdrawBalance = $balance ? (float) $balance->withdraw_balance : 0;
        $currentBalance = $startBalance + $depositBalance - $withdrawBalance + $totalProfit;

        $recentTrades = (clone $query)
            ->latest('date')
            ->latest('id')
            ->limit(20)
            ->get();

        return view('dashboard', compact(
            'totalTrades',
            'wins',
            'losses',
            'bes',
            'totalProfit',
            'currentBalance',
            'winRate',
            'recentTrades',
            'netPips',
            'lossPips',
            'startBalance',
            'depositBalance',
            'withdrawBalance'
        ));
    }
}

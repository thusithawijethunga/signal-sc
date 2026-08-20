<?php

namespace App\Http\Controllers;

use App\Models\Trade;
use App\Models\Signal;
use App\Models\AccountBalance;
use App\Models\IbMember;
use App\Models\IbPartner;
use App\Models\CommunityPost;
use App\Models\VipMember;
use App\Models\CsvUpload;
use App\Models\CsvTradeRecord;
use Illuminate\Http\Request;

class DashboardController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();

        // All trades (admin sees all)
        $trades = Trade::orderBy('date', 'desc')->orderBy('id', 'desc')->get();
        $totalTrades = $trades->count();
        $wins = $trades->where('result', 'WIN')->count();
        $losses = $trades->where('result', 'LOSS')->count();
        $bes = $trades->where('result', 'BE')->count();
        $totalProfit = (float) $trades->sum('profit');
        $netPips = (float) $trades->sum('pips');
        $lossPips = (float) $trades->where('pips', '<', 0)->sum('pips');
        $winRate = $totalTrades > 0 ? round(($wins / $totalTrades) * 100, 1) : 0;

        $balance = AccountBalance::first();
        $startBalance = $balance ? (float) $balance->start_balance : 1000;
        $depositBalance = $balance ? (float) $balance->deposit_balance : 0;
        $withdrawBalance = $balance ? (float) $balance->withdraw_balance : 0;
        $currentBalance = $startBalance + $depositBalance - $withdrawBalance + $totalProfit;

        // IB Partners
        $ibPartners = IbPartner::withCount('members')->get();
        $ibMembers = IbMember::with('partner')->latest()->get();

        // CSV uploads
        $csvUploads = CsvUpload::latest()->get();

        // JSON for Alpine.js
        $tradesJson = $trades->map(fn ($t) => [
            'no' => $t->no ?? $t->id,
            'date' => $t->date,
            'pair' => $t->pair,
            'direction' => $t->direction,
            'entry1' => $t->entry1,
            'entry2' => $t->entry2,
            'sl' => $t->sl,
            'tp1' => $t->tp1,
            'tp2' => $t->tp2,
            'tp3' => $t->tp3,
            'tp4' => $t->tp4,
            'pips' => (float) $t->pips,
            'profit' => (float) $t->profit,
            'result' => strtoupper($t->result),
            'channel' => $t->channel,
        ])->toArray();

        $ibMembersJson = $ibMembers->map(fn ($m) => [
            'sx_id' => $m->sx_id,
            'name' => $m->name,
            'broker' => $m->broker,
            'account_id' => $m->account_id,
            'partner' => $m->partner->name ?? '',
        ])->toArray();

        return view('dashboard', compact(
            'trades', 'tradesJson', 'ibMembersJson',
            'totalTrades', 'wins', 'losses', 'bes',
            'totalProfit', 'netPips', 'lossPips', 'winRate',
            'startBalance', 'depositBalance', 'withdrawBalance', 'currentBalance',
            'ibPartners', 'ibMembers', 'csvUploads'
        ));
    }
}

<?php

namespace App\Http\Controllers;

use App\Models\Trade;
use App\Models\Signal;
use App\Models\AccountBalance;
use Illuminate\Http\Request;

class AdminPanelController extends Controller
{
    public function index(Request $request)
    {
        $trades = Trade::orderBy('date', 'desc')->orderBy('id', 'desc')->get();

        $balance = AccountBalance::first();
        $startBalance = $balance ? (float) $balance->start_balance : 1000;
        $depositBalance = $balance ? (float) $balance->deposit_balance : 0;
        $withdrawBalance = $balance ? (float) $balance->withdraw_balance : 0;

        $tradeNos = $trades->pluck('no')->filter()->values();
        $signalsByNo = Signal::whereIn('no', $tradeNos)
            ->with('reactions.user:id,name')
            ->get()
            ->keyBy('no');

        $tradesJson = $trades->map(function ($t) use ($signalsByNo) {
            $signal = $signalsByNo->get($t->no);
            return [
                'no' => $t->no ?? $t->id,
                'id' => $t->id,
                'date' => $t->date->format('Y-m-d'),
                'pair' => $t->pair,
                'direction' => strtoupper($t->direction ?? ''),
                'entry1' => $t->entry1,
                'entry2' => $t->entry2,
                'sl' => $t->sl,
                'tp1' => $t->tp1,
                'tp2' => $t->tp2,
                'tp3' => $t->tp3,
                'tp4' => $t->tp4,
                'pips' => (float) $t->pips,
                'profit' => (float) $t->profit,
                'result' => strtoupper($t->result ?? ''),
                'hit_level' => $t->hit_level,
                'channel' => $t->channel,
                'signal_id' => $signal?->id,
                'thumbs_count' => $signal->thumbs_count ?? 0,
                'fire_count' => $signal->fire_count ?? 0,
                'rocket_count' => $signal->rocket_count ?? 0,
                'broken_heart_count' => $signal->broken_heart_count ?? 0,
                'reactions' => $signal ? $signal->reactions->map(fn($r) => [
                    'emoji' => $r->emoji,
                    'user_name' => $r->user->name ?? 'Unknown',
                    'created_at' => $r->created_at->diffForHumans(),
                ])->values() : [],
            ];
        })->toArray();

        return view('admin.admin', compact(
            'trades', 'tradesJson', 'signalsByNo',
            'startBalance', 'depositBalance', 'withdrawBalance'
        ))->with([
            'gsUrl' => config('services.google_sheets.url', ''),
            'tgToken' => config('services.telegram.bot_token', ''),
            'tgChatId' => config('services.telegram.chat_id', ''),
        ]);
    }
}

<?php

namespace App\Http\Controllers;

use App\Events\TradeCreated;
use App\Events\TradeUpdated;
use App\Events\TradeHit;
use App\Models\Trade;
use Illuminate\Http\Request;

class TradeManageController extends Controller
{
    public function store(Request $request)
    {
        $validated = $request->validate([
            'no' => 'nullable|integer',
            'date' => 'required|date',
            'pair' => 'nullable|string|max:20',
            'direction' => 'nullable|string|max:20',
            'entry1' => 'nullable|numeric',
            'entry2' => 'nullable|numeric',
            'sl' => 'nullable|numeric',
            'tp1' => 'nullable|numeric',
            'tp2' => 'nullable|numeric',
            'tp3' => 'nullable|numeric',
            'tp4' => 'nullable|numeric',
            'pips' => 'nullable|numeric',
            'profit' => 'nullable|numeric',
            'result' => 'nullable|string|max:10',
            'channel' => 'nullable|string|max:10',
        ]);

        $validated['no'] = $validated['no'] ?? (Trade::max('no') + 1);
        $validated['user_id'] = $request->user()->id;

        $trade = Trade::create($validated);

        TradeCreated::dispatch($trade);

        return response()->json(['message' => 'Trade saved', 'trade' => $trade]);
    }

    public function update(Request $request, Trade $trade)
    {
        $oldResult = $trade->result;

        $validated = $request->validate([
            'date' => 'required|date',
            'pair' => 'nullable|string|max:20',
            'direction' => 'nullable|string|max:20',
            'entry1' => 'nullable|numeric',
            'entry2' => 'nullable|numeric',
            'sl' => 'nullable|numeric',
            'tp1' => 'nullable|numeric',
            'tp2' => 'nullable|numeric',
            'tp3' => 'nullable|numeric',
            'tp4' => 'nullable|numeric',
            'pips' => 'nullable|numeric',
            'profit' => 'nullable|numeric',
            'result' => 'nullable|string|max:10',
            'channel' => 'nullable|string|max:10',
        ]);

        $trade->update($validated);

        TradeUpdated::dispatch($trade, $oldResult);

        if ($oldResult !== $trade->result && $trade->result !== 'RUNNING') {
            $hitType = match($trade->result) {
                'WIN' => 'TP',
                'LOSS' => 'SL',
                'BE' => 'BE',
                default => $trade->result,
            };
            TradeHit::dispatch($trade, $hitType);
        }

        return response()->json(['message' => 'Trade updated', 'trade' => $trade]);
    }

    public function destroy(Trade $trade)
    {
        $trade->delete();
        return response()->json(['message' => 'Trade deleted']);
    }
}

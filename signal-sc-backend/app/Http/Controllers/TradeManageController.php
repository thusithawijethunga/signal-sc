<?php

namespace App\Http\Controllers;

use App\Events\TradeCreated;
use App\Events\TradeUpdated;
use App\Events\SignalCreated;
use App\Events\SignalUpdated;
use App\Events\SignalDeleted;
use App\Models\Trade;
use App\Models\Signal;
use Carbon\Carbon;
use Illuminate\Http\Request;

class TradeManageController extends Controller
{
    public function import(Request $request)
    {
        $request->validate([
            'trades' => 'required|array',
        ]);

        $count = 0;
        foreach ($request->trades as $row) {
            $no = $row['no'] ?? null;
            $data = [
                'date' => $this->parseDate($row['date'] ?? null),
                'pair' => $row['pair'] ?? 'XAU/USD',
                'direction' => strtoupper($row['direction'] ?? ''),
                'entry1' => $this->num($row['entry1'] ?? null),
                'entry2' => $this->num($row['entry2'] ?? null),
                'sl' => $this->num($row['sl'] ?? null),
                'tp1' => $this->num($row['tp1'] ?? null),
                'tp2' => $this->num($row['tp2'] ?? null),
                'tp3' => $this->num($row['tp3'] ?? null),
                'tp4' => $this->num($row['tp4'] ?? null),
                'pips' => (float) ($row['pips'] ?? 0),
                'profit' => (float) ($row['profit'] ?? 0),
                'result' => strtoupper($row['result'] ?? 'RUNNING'),
                'channel' => ($row['channel'] ?? '') ?: 'VIP',
                'user_id' => $request->user()->id,
            ];

            if ($no) {
                $data['no'] = (int) $no;
                $trade = Trade::updateOrCreate(['no' => (int) $no], $data);
            } else {
                $data['no'] = (int) (Trade::max('no') + 1);
                $trade = Trade::create($data);
                TradeCreated::dispatch($trade);
            }

            $this->syncTradeToSignal($trade);

            $count++;
        }

        return response()->json(['message' => "$count trades imported successfully", 'count' => $count]);
    }

    private function num($v)
    {
        if ($v === '' || $v === null) {
            return null;
        }
        return (float) $v;
    }

    private function parseDate($v)
    {
        if (! $v) {
            return now()->format('Y-m-d');
        }
        try {
            return Carbon::parse($v)->format('Y-m-d');
        } catch (\Exception $e) {
            return now()->format('Y-m-d');
        }
    }

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
        $this->syncTradeToSignal($trade);

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
            'hit_type' => 'nullable|string|max:10',
            'hit_level' => 'nullable|string|max:10',
        ]);

        $hitType = $request->input('hit_type');

        if ($hitType) {
            $validated['hit_level'] = $hitType;
        }

        $trade->update($validated);
        TradeUpdated::dispatch($trade, $oldResult, $hitType);
        $this->syncTradeToSignal($trade, $hitType);

        return response()->json(['message' => 'Trade updated', 'trade' => $trade]);
    }

    public function destroy(Trade $trade)
    {
        $signal = Signal::where('no', $trade->no)->first();
        $trade->delete();

        if ($signal) {
            SignalDeleted::dispatch($signal->id, $signal->no);
            $signal->delete();
        }

        return response()->json(['message' => 'Trade deleted']);
    }

    private function syncTradeToSignal(Trade $trade, ?string $action = null): void
    {
        $signalData = [
            'no' => $trade->no,
            'date' => $trade->date,
            'pair' => $trade->pair,
            'direction' => $trade->direction,
            'entry1' => $trade->entry1,
            'entry2' => $trade->entry2,
            'sl' => $trade->sl,
            'tp1' => $trade->tp1,
            'tp2' => $trade->tp2,
            'tp3' => $trade->tp3,
            'tp4' => $trade->tp4,
            'pips' => $trade->pips,
            'profit' => $trade->profit,
            'result' => $trade->result,
            'channel' => $trade->channel,
            'hit_level' => $trade->hit_level,
            'user_id' => $trade->user_id,
        ];

        $existing = Signal::where('no', $trade->no)->first();

        if ($existing) {
            $existing->update($signalData);
            SignalUpdated::dispatch($existing, $action);
        } else {
            $signal = Signal::create($signalData);
            SignalCreated::dispatch($signal);
        }
    }
}

<?php

namespace App\Http\Controllers;

use App\Models\Trade;
use Illuminate\Http\Request;

class TradeManageController extends Controller
{
    public function index(Request $request)
    {
        $query = Trade::query();

        if ($request->filled('result')) {
            $query->where('result', $request->input('result'));
        }
        if ($request->filled('pair')) {
            $query->where('pair', $request->input('pair'));
        }
        if ($request->filled('date_from')) {
            $query->where('date', '>=', $request->input('date_from'));
        }
        if ($request->filled('date_to')) {
            $query->where('date', '<=', $request->input('date_to'));
        }
        if ($request->filled('search')) {
            $search = $request->input('search');
            $query->where(function ($q) use ($search) {
                $q->where('pair', 'like', "%{$search}%")
                  ->orWhere('channel', 'like', "%{$search}%");
            });
        }

        $trades = $query->latest('date')->paginate(20)->withQueryString();

        return view('admin.trades.index', compact('trades'));
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'no' => 'required|integer',
            'date' => 'required|date',
            'pair' => 'required|string|max:20',
            'direction' => 'required|in,buy,sell',
            'entry1' => 'required|numeric',
            'entry2' => 'nullable|numeric',
            'sl' => 'required|numeric',
            'tp1' => 'required|numeric',
            'tp2' => 'nullable|numeric',
            'tp3' => 'nullable|numeric',
            'tp4' => 'nullable|numeric',
            'pips' => 'nullable|numeric',
            'profit' => 'nullable|numeric',
            'result' => 'required|in,win,loss,BE,pending',
            'channel' => 'nullable|string|max:100',
            'user_id' => 'nullable|exists:users,id',
        ]);

        Trade::create($validated);

        return redirect()->route('admin.trades.index')->with('success', 'Trade created successfully.');
    }

    public function update(Request $request, Trade $trade)
    {
        $validated = $request->validate([
            'no' => 'required|integer',
            'date' => 'required|date',
            'pair' => 'required|string|max:20',
            'direction' => 'required|in,buy,sell',
            'entry1' => 'required|numeric',
            'entry2' => 'nullable|numeric',
            'sl' => 'required|numeric',
            'tp1' => 'required|numeric',
            'tp2' => 'nullable|numeric',
            'tp3' => 'nullable|numeric',
            'tp4' => 'nullable|numeric',
            'pips' => 'nullable|numeric',
            'profit' => 'nullable|numeric',
            'result' => 'required|in,win,loss,BE,pending',
            'channel' => 'nullable|string|max:100',
            'user_id' => 'nullable|exists:users,id',
        ]);

        $trade->update($validated);

        return redirect()->route('admin.trades.index')->with('success', 'Trade updated successfully.');
    }

    public function destroy(Trade $trade)
    {
        $trade->delete();

        return redirect()->route('admin.trades.index')->with('success', 'Trade deleted successfully.');
    }
}

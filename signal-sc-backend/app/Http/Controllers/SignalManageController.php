<?php

namespace App\Http\Controllers;

use App\Models\Signal;
use Illuminate\Http\Request;

class SignalManageController extends Controller
{
    public function index()
    {
        $signals = Signal::latest('date')->paginate(20);

        return view('admin.signals.index', compact('signals'));
    }

    public function create()
    {
        return view('admin.signals.create');
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
            'hit_level' => 'nullable|string|max:50',
            'status' => 'nullable|string|max:50',
        ]);

        $validated['user_id'] = $request->user()->id;

        Signal::create($validated);

        return redirect()->route('admin.signals.index')->with('success', 'Signal created successfully.');
    }

    public function edit(Signal $signal)
    {
        return view('admin.signals.edit', compact('signal'));
    }

    public function update(Request $request, Signal $signal)
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
            'hit_level' => 'nullable|string|max:50',
            'status' => 'nullable|string|max:50',
        ]);

        $signal->update($validated);

        return redirect()->route('admin.signals.index')->with('success', 'Signal updated successfully.');
    }

    public function destroy(Signal $signal)
    {
        $signal->delete();

        return redirect()->route('admin.signals.index')->with('success', 'Signal deleted successfully.');
    }
}

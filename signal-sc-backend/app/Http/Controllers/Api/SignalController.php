<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Events\SignalCreated;
use App\Events\SignalUpdated;
use App\Events\SignalDeleted;
use App\Events\SignalReacted;
use App\Models\Signal;
use App\Models\SignalReaction;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SignalController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        try {
            $query = Signal::with('user');

            if ($request->filled('pair')) {
                $query->where('pair', $request->pair);
            }
            if ($request->filled('result')) {
                $query->where('result', $request->result);
            }
            if ($request->filled('direction')) {
                $query->where('direction', $request->direction);
            }
            if ($request->filled('no')) {
                $query->where('no', $request->no);
            }
            if ($request->filled('date_from')) {
                $query->whereDate('date', '>=', $request->date_from);
            }
            if ($request->filled('date_to')) {
                $query->whereDate('date', '<=', $request->date_to);
            }

            $signals = $query->orderBy('date', 'desc')->orderBy('no', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($signals);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch signals', 'error' => $e->getMessage()], 500);
        }
    }

    public function store(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'pair' => 'required|string',
                'direction' => 'required|string|in:BUY,SELL',
                'entry1' => 'required|numeric',
                'sl' => 'required|numeric',
                'tp1' => 'required|numeric',
                'date' => 'required|date',
                'result' => 'nullable|string|in:WIN,LOSS,BREAKEVEN,PENDING',
            ]);

            $signal = Signal::create([
                'no' => Signal::max('no') + 1,
                'date' => $request->date,
                'pair' => $request->pair,
                'direction' => $request->direction,
                'entry1' => $request->entry1,
                'entry2' => $request->get('entry2'),
                'sl' => $request->sl,
                'tp1' => $request->tp1,
                'tp2' => $request->get('tp2'),
                'tp3' => $request->get('tp3'),
                'tp4' => $request->get('tp4'),
                'pips' => $request->get('pips'),
                'profit' => $request->get('profit'),
                'result' => $request->get('result', 'RUNNING'),
                'channel' => $request->get('channel'),
                'hit_level' => $request->get('hit_level'),
                'status' => $request->get('status', 'active'),
                'user_id' => $request->user()->id,
            ]);

            SignalCreated::dispatch($signal);

            return response()->json($signal, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create signal', 'error' => $e->getMessage()], 500);
        }
    }

    public function update(Request $request, Signal $signal): JsonResponse
    {
        try {
            $signal->update($request->only([
                'pair', 'direction', 'entry1', 'entry2', 'sl',
                'tp1', 'tp2', 'tp3', 'tp4', 'pips', 'profit',
                'result', 'channel', 'hit_level', 'status', 'date',
            ]));

            SignalUpdated::dispatch($signal->fresh(), $request->input('hit_type', 'updated'));

            return response()->json($signal);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to update signal', 'error' => $e->getMessage()], 500);
        }
    }

    public function destroy(Request $request, Signal $signal): JsonResponse
    {
        try {
            $signalId = $signal->id;
            $signalNo = $signal->no;
            $signal->delete();
            SignalDeleted::dispatch($signalId, $signalNo);
            return response()->json(['message' => 'Signal deleted']);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to delete signal', 'error' => $e->getMessage()], 500);
        }
    }

    public function react(Request $request, Signal $signal): JsonResponse
    {
        try {
            $request->validate([
                'emoji' => 'required|string|in:thumbs,fire,rocket,broken_heart',
            ]);

            $userId = $request->user()->id;
            $userName = $request->user()->name ?? 'User';
            $emoji = $request->emoji;

            $existing = SignalReaction::where('signal_id', $signal->id)
                ->where('user_id', $userId)
                ->first();

            if ($existing) {
                if ($existing->emoji === $emoji) {
                    $existing->delete();
                    $this->decrementCount($signal, $emoji);
                    $signal = $signal->fresh();
                    SignalReacted::dispatch($signal, 'removed', $emoji, $userName);
                    return response()->json(['message' => 'Reaction removed', 'signal' => $signal]);
                }

                $this->decrementCount($signal, $existing->emoji);
                $existing->update(['emoji' => $emoji]);
                $this->incrementCount($signal, $emoji);
                $signal = $signal->fresh();
                SignalReacted::dispatch($signal, 'changed', $emoji, $userName);
                return response()->json(['message' => 'Reaction changed', 'signal' => $signal]);
            }

            SignalReaction::create([
                'signal_id' => $signal->id,
                'user_id' => $userId,
                'emoji' => $emoji,
            ]);
            $this->incrementCount($signal, $emoji);
            $signal = $signal->fresh();
            SignalReacted::dispatch($signal, 'added', $emoji, $userName);
            return response()->json(['message' => 'Reaction added', 'signal' => $signal]);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to react', 'error' => $e->getMessage()], 500);
        }
    }

    private function incrementCount(Signal $signal, string $emoji): void
    {
        $column = $emoji . '_count';
        if (in_array($column, ['thumbs_count', 'fire_count', 'rocket_count', 'broken_heart_count'])) {
            $signal->increment($column);
        }
    }

    private function decrementCount(Signal $signal, string $emoji): void
    {
        $column = $emoji . '_count';
        if (in_array($column, ['thumbs_count', 'fire_count', 'rocket_count', 'broken_heart_count'])) {
            $signal->decrement($column);
        }
    }
}

<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\SyncQueue;
use App\Models\Trade;
use App\Models\Signal;
use App\Models\AccountBalance;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class SyncController extends Controller
{
    public function push(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'operations' => 'required|array|min:1',
                'operations.*.table_name' => 'required|string|in:trades,signals,account_balances',
                'operations.*.action' => 'required|string|in:create,update,delete',
                'operations.*.record_id' => 'nullable|integer',
                'operations.*.payload' => 'nullable|array',
                'device_id' => 'nullable|string',
            ]);

            $userId = $request->user()->id;
            $deviceId = $request->get('device_id', 'unknown');
            $results = [];

            DB::beginTransaction();

            foreach ($request->operations as $index => $op) {
                try {
                    $result = $this->processOperation($userId, $op);
                    $results[$index] = ['status' => 'success', 'result' => $result];

                    SyncQueue::create([
                        'user_id' => $userId,
                        'device_id' => $deviceId,
                        'table_name' => $op['table_name'],
                        'record_id' => $result['id'] ?? null,
                        'action' => $op['action'],
                        'payload' => $op['payload'] ?? null,
                        'synced_at' => now(),
                    ]);
                } catch (\Exception $e) {
                    $results[$index] = ['status' => 'error', 'message' => $e->getMessage()];
                }
            }

            DB::commit();

            return response()->json(['message' => 'Push completed', 'results' => $results]);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['message' => 'Push failed', 'error' => $e->getMessage()], 500);
        }
    }

    public function pull(Request $request): JsonResponse
    {
        try {
            $since = $request->get('since');
            $user = $request->user();
            $isAdmin = $user->role === 'admin';

            $trades = Trade::query()
                ->when(!$isAdmin, fn ($q) => $q->where('user_id', $user->id))
                ->when($since, fn ($q) => $q->where('updated_at', '>', $since))
                ->get();

            $signals = Signal::query()
                ->when(!$isAdmin, fn ($q) => $q->where('user_id', $user->id))
                ->when($since, fn ($q) => $q->where('updated_at', '>', $since))
                ->get();

            $balances = AccountBalance::query()
                ->when(!$isAdmin, fn ($q) => $q->where('user_id', $user->id))
                ->when($since, fn ($q) => $q->where('updated_at', '>', $since))
                ->get();

            return response()->json([
                'trades' => $trades,
                'signals' => $signals,
                'account_balances' => $balances,
                'pulled_at' => now()->toIso8601String(),
            ]);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Pull failed', 'error' => $e->getMessage()], 500);
        }
    }

    private function processOperation(int $userId, array $op): array
    {
        $model = match ($op['table_name']) {
            'trades' => Trade::class,
            'signals' => Signal::class,
            'account_balances' => AccountBalance::class,
            default => throw new \Exception("Unknown table: {$op['table_name']}"),
        };

        return match ($op['action']) {
            'create' => $model::create(array_merge($op['payload'] ?? [], ['user_id' => $userId]))->toArray(),
            'update' => tap($model::where('id', $op['record_id'])->where('user_id', $userId)->firstOrFail(), function ($record) use ($op) {
                $record->update($op['payload'] ?? []);
            })->toArray(),
            'delete' => $model::where('id', $op['record_id'])->where('user_id', $userId)->firstOrFail()->delete(),
            default => throw new \Exception("Unknown action: {$op['action']}"),
        };
    }
}

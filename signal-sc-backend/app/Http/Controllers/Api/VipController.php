<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\VipMember;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class VipController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        try {
            $query = VipMember::query();

            if ($request->filled('period')) {
                $query->where('period', $request->period);
            }

            $members = $query->orderBy('rank', 'asc')
                ->paginate($request->get('per_page', 20));

            return response()->json($members);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch VIP members', 'error' => $e->getMessage()], 500);
        }
    }

    public function sync(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'url' => 'required|url',
            ]);

            $response = Http::timeout(30)->get($request->url);

            if (!$response->successful()) {
                return response()->json(['message' => 'Failed to fetch from external URL'], 502);
            }

            $data = $response->json();
            $members = is_array($data) && isset($data['data']) ? $data['data'] : ($data ?? []);
            $synced = 0;

            foreach ($members as $member) {
                $memberId = $member['member_id'] ?? $member['id'] ?? null;

                if (!$memberId) {
                    continue;
                }

                VipMember::updateOrCreate(
                    ['member_id' => $memberId],
                    [
                        'rank' => $member['rank'] ?? null,
                        'name' => $member['name'] ?? '',
                        'lots' => $member['lots'] ?? 0,
                        'progress_fraction' => $member['progress_fraction'] ?? 0,
                        'accent_hex' => $member['accent_hex'] ?? null,
                        'period' => $member['period'] ?? null,
                        'win_rate' => $member['win_rate'] ?? null,
                        'total_trades' => $member['total_trades'] ?? null,
                        'broker' => $member['broker'] ?? null,
                        'favorite_pair' => $member['favorite_pair'] ?? null,
                    ]
                );
                $synced++;
            }

            return response()->json(['message' => 'Sync completed', 'synced_count' => $synced]);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Sync failed', 'error' => $e->getMessage()], 500);
        }
    }

    public function store(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'member_id' => 'required|string',
                'name' => 'required|string',
                'rank' => 'nullable|integer',
            ]);

            $member = VipMember::updateOrCreate(
                ['member_id' => $request->member_id],
                $request->only([
                    'rank', 'name', 'lots', 'progress_fraction',
                    'accent_hex', 'period', 'win_rate', 'total_trades',
                    'broker', 'favorite_pair',
                ])
            );

            return response()->json($member, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create VIP member', 'error' => $e->getMessage()], 500);
        }
    }
}

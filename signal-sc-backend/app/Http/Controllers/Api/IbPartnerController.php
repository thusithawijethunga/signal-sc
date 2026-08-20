<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\IbMember;
use App\Models\IbPartner;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class IbPartnerController extends Controller
{
    public function members(Request $request): JsonResponse
    {
        try {
            $query = IbMember::with('partner');

            if ($request->filled('search')) {
                $search = $request->search;
                $query->where(function ($q) use ($search) {
                    $q->where('sx_id', 'like', "%{$search}%")
                      ->orWhere('name', 'like', "%{$search}%")
                      ->orWhere('account_id', 'like', "%{$search}%")
                      ->orWhere('nic', 'like', "%{$search}%");
                });
            }

            if ($request->filled('partner_id')) {
                $query->where('partner_id', $request->partner_id);
            }

            $members = $query->orderBy('created_at', 'desc')
                ->paginate($request->get('per_page', 20));

            return response()->json($members);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch members', 'error' => $e->getMessage()], 500);
        }
    }

    public function storeMember(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'name' => 'required|string',
                'broker' => 'nullable|string',
                'account_id' => 'nullable|string',
                'nic' => 'nullable|string',
                'whatsapp' => 'nullable|string',
                'telegram' => 'nullable|string',
                'partner_id' => 'nullable|exists:ib_partners,id',
            ]);

            $member = IbMember::create($request->only([
                'name', 'broker', 'account_id', 'nic',
                'whatsapp', 'telegram', 'partner_id',
            ]));

            return response()->json($member, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create member', 'error' => $e->getMessage()], 500);
        }
    }

    public function partners(Request $request): JsonResponse
    {
        try {
            $partners = IbPartner::withCount('members')
                ->orderBy('name', 'asc')
                ->paginate($request->get('per_page', 20));

            return response()->json($partners);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to fetch partners', 'error' => $e->getMessage()], 500);
        }
    }

    public function storePartner(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'name' => 'required|string',
            ]);

            $partner = IbPartner::create($request->only(['name']));

            return response()->json($partner, 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Failed to create partner', 'error' => $e->getMessage()], 500);
        }
    }

    public function searchMember(Request $request): JsonResponse
    {
        try {
            $request->validate([
                'query' => 'required|string|min:1',
            ]);

            $search = $request->query('query');
            $members = IbMember::with('partner')
                ->where('sx_id', 'like', "%{$search}%")
                ->orWhere('name', 'like', "%{$search}%")
                ->orWhere('account_id', 'like', "%{$search}%")
                ->orWhere('nic', 'like', "%{$search}%")
                ->limit(20)
                ->get();

            return response()->json($members);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json(['message' => 'Validation failed', 'errors' => $e->errors()], 422);
        } catch (\Exception $e) {
            return response()->json(['message' => 'Search failed', 'error' => $e->getMessage()], 500);
        }
    }
}

<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class CsvAnalyticsController extends Controller
{
    public function index(Request $request)
    {
        $ibMembers = User::ibMembers()->with('partner')->latest()->get();

        $ibMembersJson = $ibMembers->map(fn ($m) => [
            'sx_id' => $m->sx_id,
            'name' => $m->name,
            'broker' => $m->broker,
            'account_id' => $m->account_id,
            'partner' => $m->partner->name ?? '',
        ])->toArray();

        return view('admin.csv-analytics', compact('ibMembersJson'));
    }

    public function autoCreateMembers(Request $request)
    {
        $request->validate([
            'accounts' => 'required|array',
            'accounts.*.account_id' => 'required|string',
            'accounts.*.broker' => 'nullable|string',
        ]);

        $defaultPassword = 'SignalXp';
        $created = [];
        $existing = [];
        $errors = [];

        foreach ($request->accounts as $item) {
            $accountId = trim($item['account_id']);
            $broker = trim($item['broker'] ?? 'XM');

            if (empty($accountId)) continue;

            $existingMember = User::where('account_id', $accountId)->where('role', 'ib_member')->first();

            if ($existingMember) {
                $existing[] = [
                    'sx_id' => $existingMember->sx_id,
                    'account_id' => $existingMember->account_id,
                    'name' => $existingMember->name,
                    'status' => 'existing',
                ];
                continue;
            }

            try {
                $email = 'ib' . $accountId . '@signalxpress.local';

                $lastMember = User::ibMembers()->orderByDesc('id')->first();
                $nextNumber = $lastMember ? intval(substr($lastMember->sx_id, 2)) + 1 : 1;
                $sxId = 'SX' . str_pad($nextNumber, 5, '0', STR_PAD_LEFT);

                $user = User::create([
                    'name' => 'Trader ' . $accountId,
                    'email' => $email,
                    'password' => $defaultPassword,
                    'role' => 'ib_member',
                    'sx_id' => $sxId,
                    'broker' => $broker,
                    'account_id' => $accountId,
                ]);

                $created[] = [
                    'sx_id' => $sxId,
                    'account_id' => $accountId,
                    'name' => $user->name,
                    'email' => $email,
                    'password' => $defaultPassword,
                    'status' => 'created',
                ];
            } catch (\Exception $e) {
                $errors[] = ['account_id' => $accountId, 'error' => $e->getMessage()];
            }
        }

        $allMembers = User::ibMembers()->with('partner')->latest()->get()->map(fn ($m) => [
            'sx_id' => $m->sx_id,
            'name' => $m->name,
            'broker' => $m->broker,
            'account_id' => $m->account_id,
            'partner' => $m->partner->name ?? '',
        ])->toArray();

        return response()->json([
            'ok' => true,
            'created' => $created,
            'existing' => $existing,
            'errors' => $errors,
            'created_count' => count($created),
            'existing_count' => count($existing),
            'default_password' => $defaultPassword,
            'ib_members' => $allMembers,
        ]);
    }
}

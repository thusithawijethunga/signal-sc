<?php

namespace App\Http\Controllers;

use App\Models\IbMember;
use App\Models\CsvUpload;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class CsvAnalyticsController extends Controller
{
    public function index(Request $request)
    {
        $ibMembers = IbMember::with('partner')->latest()->get();

        $ibMembersJson = $ibMembers->map(fn ($m) => [
            'sx_id' => $m->sx_id,
            'name' => $m->name,
            'broker' => $m->broker,
            'account_id' => $m->account_id,
            'partner' => $m->partner->name ?? '',
        ])->toArray();

        $csvUploads = CsvUpload::latest()->get();

        return view('admin.csv-analytics', compact('ibMembersJson', 'csvUploads'));
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

        foreach ($request->accounts as $item) {
            $accountId = trim($item['account_id']);
            $broker = $item['broker'] ?? 'XM';

            if (empty($accountId)) continue;

            $existingMember = IbMember::where('account_id', $accountId)->first();

            if ($existingMember) {
                $existing[] = [
                    'sx_id' => $existingMember->sx_id,
                    'account_id' => $existingMember->account_id,
                    'name' => $existingMember->name,
                    'status' => 'existing',
                ];
                continue;
            }

            DB::beginTransaction();

            try {
                $email = strtolower('trader' . $accountId) . '@signalxpress.local';

                $user = User::create([
                    'name' => 'Trader ' . $accountId,
                    'email' => $email,
                    'password' => $defaultPassword,
                    'role' => 'member',
                ]);

                $member = IbMember::create([
                    'user_id' => $user->id,
                    'name' => 'Trader ' . $accountId,
                    'broker' => $broker,
                    'account_id' => $accountId,
                ]);

                DB::commit();

                $created[] = [
                    'sx_id' => $member->sx_id,
                    'account_id' => $accountId,
                    'name' => $member->name,
                    'email' => $email,
                    'password' => $defaultPassword,
                    'status' => 'created',
                ];
            } catch (\Exception $e) {
                DB::rollBack();
            }
        }

        $allMembers = IbMember::with('partner')->latest()->get()->map(fn ($m) => [
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
            'created_count' => count($created),
            'existing_count' => count($existing),
            'default_password' => $defaultPassword,
            'ib_members' => $allMembers,
        ]);
    }
}

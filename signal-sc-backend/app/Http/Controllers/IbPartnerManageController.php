<?php

namespace App\Http\Controllers;

use App\Models\IbMember;
use App\Models\IbPartner;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class IbPartnerManageController extends Controller
{
    public function index()
    {
        $partners = IbPartner::withCount('members')->get();
        $members = IbMember::with('partner')->latest()->get();

        return view('dashboard');
    }

    public function syncMembers(Request $request)
    {
        $url = $request->input('url') ?: config('services.ib_google_sheets.url', '');

        if (! $url) {
            return response()->json(['ok' => false, 'message' => 'IB Google Sheets URL not configured'], 400);
        }

        try {
            $response = Http::timeout(30)->get($url);
            $data = $response->json();

            if (! is_array($data)) {
                return response()->json(['ok' => false, 'message' => 'Invalid response from Google Sheets'], 400);
            }

            $synced = 0;
            $created = 0;
            $updated = 0;

            foreach ($data as $row) {
                $name = $row['name'] ?? $row['Name'] ?? $row['trader_name'] ?? null;
                if (empty($name)) {
                    continue;
                }

                $broker = $row['broker'] ?? $row['Broker'] ?? $row['broker_name'] ?? 'XM';
                $accountId = $row['account_id'] ?? $row['Account ID'] ?? $row['accountId'] ?? $row['account'] ?? null;
                $nic = $row['nic'] ?? $row['NIC'] ?? null;
                $whatsapp = $row['whatsapp'] ?? $row['WhatsApp'] ?? $row['phone'] ?? null;
                $telegram = $row['telegram'] ?? $row['Telegram'] ?? $row['tg'] ?? null;
                $partnerName = $row['partner'] ?? $row['Partner'] ?? $row['partner_name'] ?? $row['referral'] ?? null;
                $sxId = $row['sx_id'] ?? $row['SX ID'] ?? $row['sxId'] ?? $row['id'] ?? null;

                // Resolve partner_id
                $partnerId = null;
                if (! empty($partnerName)) {
                    $partner = IbPartner::firstOrCreate(['name' => $partnerName]);
                    $partnerId = $partner->id;
                }

                // Try to find existing member by sx_id or account_id
                $existing = null;
                if (! empty($sxId)) {
                    $existing = IbMember::where('sx_id', $sxId)->first();
                }
                if (! $existing && ! empty($accountId)) {
                    $existing = IbMember::where('account_id', $accountId)->first();
                }

                if ($existing) {
                    $existing->update([
                        'name' => $name,
                        'broker' => $broker,
                        'account_id' => $accountId,
                        'nic' => $nic,
                        'whatsapp' => $whatsapp,
                        'telegram' => $telegram,
                        'partner_id' => $partnerId,
                    ]);
                    $updated++;
                } else {
                    IbMember::create([
                        'name' => $name,
                        'broker' => $broker,
                        'account_id' => $accountId,
                        'nic' => $nic,
                        'whatsapp' => $whatsapp,
                        'telegram' => $telegram,
                        'partner_id' => $partnerId,
                    ]);
                    $created++;
                }

                $synced++;
            }

            return response()->json([
                'ok' => true,
                'message' => "Synced {$synced} members ({$created} created, {$updated} updated)",
                'synced' => $synced,
                'created' => $created,
                'updated' => $updated,
                'members' => IbMember::with('partner')->latest()->get()->map(fn ($m) => [
                    'sx_id' => $m->sx_id,
                    'name' => $m->name,
                    'broker' => $m->broker,
                    'account_id' => $m->account_id,
                    'nic' => $m->nic,
                    'whatsapp' => $m->whatsapp,
                    'telegram' => $m->telegram,
                    'partner' => $m->partner->name ?? '',
                ])->toArray(),
            ]);
        } catch (\Throwable $e) {
            return response()->json(['ok' => false, 'message' => 'Sync failed: ' . $e->getMessage()], 500);
        }
    }

    public function storeMember(Request $request)
    {
        $data = $request->validate([
            'name' => 'required|string|max:255',
            'broker' => 'nullable|string|max:255',
            'account_id' => 'nullable|string|max:255',
            'nic' => 'nullable|string|max:255',
            'whatsapp' => 'nullable|string|max:50',
            'telegram' => 'nullable|string|max:100',
            'partner' => 'nullable|string|max:255',
            'partner_id' => 'nullable|exists:ib_partners,id',
        ]);

        if (empty($data['partner_id']) && !empty($data['partner'])) {
            $partner = IbPartner::where('name', $data['partner'])->first();
            if ($partner) {
                $data['partner_id'] = $partner->id;
            }
        }
        unset($data['partner']);

        $accountId = $data['account_id'] ?? null;
        $userId = null;

        if ($accountId) {
            $email = 'trader' . $accountId . '@signalxpress.local';
            $user = User::firstOrCreate(
                ['email' => $email],
                [
                    'name' => $data['name'],
                    'password' => 'SignalXp',
                    'role' => 'member',
                ]
            );
            $userId = $user->id;
        }

        $data['user_id'] = $userId;
        IbMember::create($data);

        return response()->json(['message' => 'Member saved successfully']);
    }

    public function storePartner(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
        ]);

        IbPartner::create($validated);

        return response()->json(['message' => 'Partner added', 'partner' => IbPartner::latest()->first()]);
    }

    public function search(Request $request)
    {
        $query = $request->input('q', '');

        $members = IbMember::with('partner')
            ->where(function ($q) use ($query) {
                $q->where('name', 'like', "%{$query}%")
                  ->orWhere('sx_id', 'like', "%{$query}%")
                  ->orWhere('account_id', 'like', "%{$query}%")
                  ->orWhere('nic', 'like', "%{$query}%")
                  ->orWhere('whatsapp', 'like', "%{$query}%")
                  ->orWhere('telegram', 'like', "%{$query}%");
            })
            ->latest()
            ->get();

        return response()->json($members);
    }
}

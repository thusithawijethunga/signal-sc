<?php

namespace App\Http\Controllers;

use App\Models\IbMember;
use App\Models\IbPartner;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class IbPartnerManageController extends Controller
{
    public function index()
    {
        $partners = IbPartner::withCount('members')->get();
        $members = IbMember::with('partner')->latest()->get();

        return view('dashboard');
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

<?php

namespace App\Http\Controllers;

use App\Models\IbMember;
use App\Models\IbPartner;
use Illuminate\Http\Request;

class IbPartnerManageController extends Controller
{
    public function index()
    {
        $partners = IbPartner::with('members')->get();
        $members = IbMember::with('partner')->latest()->paginate(20);

        return view('admin.ib-partners.index', compact('partners', 'members'));
    }

    public function storeMember(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'broker' => 'nullable|string|max:255',
            'account_id' => 'nullable|string|max:255',
            'nic' => 'nullable|string|max:255',
            'whatsapp' => 'nullable|string|max:50',
            'telegram' => 'nullable|string|max:100',
            'partner_id' => 'nullable|exists:ib_partners,id',
        ]);

        IbMember::create($validated);

        return redirect()->route('admin.ib.index')->with('success', 'Member added successfully.');
    }

    public function storePartner(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
        ]);

        IbPartner::create($validated);

        return redirect()->route('admin.ib.index')->with('success', 'Partner added successfully.');
    }

    public function search(Request $request)
    {
        $query = $request->input('q', '');

        $members = IbMember::with('partner')
            ->where(function ($q) use ($query) {
                $q->where('name', 'like', "%{$query}%")
                  ->orWhere('sx_id', 'like', "%{$query}%")
                  ->orWhere('account_id', 'like', "%{$query}%")
                  ->orWhere('whatsapp', 'like', "%{$query}%")
                  ->orWhere('telegram', 'like', "%{$query}%");
            })
            ->latest()
            ->paginate(20);

        $partners = IbPartner::with('members')->get();

        return view('admin.ib-partners.index', compact('members', 'partners', 'query'));
    }
}

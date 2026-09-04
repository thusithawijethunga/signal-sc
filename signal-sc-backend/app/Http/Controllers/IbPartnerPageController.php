<?php

namespace App\Http\Controllers;

use App\Models\IbPartner;
use App\Models\User;
use Illuminate\Http\Request;

class IbPartnerPageController extends Controller
{
    public function index(Request $request)
    {
        $ibPartners = IbPartner::withCount('members')->get();
        $ibMembers = User::ibMembers()->with('partner')->latest()->get();

        $ibMembersJson = $ibMembers->map(fn ($m) => [
            'sx_id' => $m->sx_id,
            'name' => $m->name,
            'broker' => $m->broker,
            'account_id' => $m->account_id,
            'nic' => $m->nic,
            'whatsapp' => $m->whatsapp,
            'telegram' => $m->telegram,
            'partner' => $m->partner->name ?? '',
        ])->toArray();

        $ibPartnersJson = $ibPartners->map(fn ($p) => [
            'id' => $p->id,
            'name' => $p->name,
            'members_count' => $p->members_count,
        ])->toArray();

        return view('admin.ib-partners', compact('ibPartners', 'ibMembers', 'ibMembersJson', 'ibPartnersJson'))
            ->with('ibGsUrl', config('services.ib_google_sheets.url', ''));
    }
}

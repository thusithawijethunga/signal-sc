<?php

namespace App\Http\Controllers;

use App\Models\IbMember;
use App\Models\CsvUpload;
use Illuminate\Http\Request;

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
}

<?php

namespace App\Http\Controllers;

use App\Services\PresenceService;
use Illuminate\Http\JsonResponse;
use Illuminate\View\View;

class PresenceBoardController extends Controller
{
    /** Live "who is online" board for admins (session auth, like other admin pages). */
    public function index(): View
    {
        return view('admin.presence');
    }

    /** Snapshot polled by the board every few seconds. */
    public function data(PresenceService $presence): JsonResponse
    {
        return response()->json($presence->snapshot());
    }
}

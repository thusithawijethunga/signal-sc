<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Services\CentrifugoService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class AdminChatController extends Controller
{
    public function index()
    {
        $users = User::where('id', '!=', auth()->id())->get();
        return view('admin.chat', compact('users'));
    }

    public function getToken(Request $request, CentrifugoService $centrifugo)
    {
        $user = $request->user();
        $token = $centrifugo->generateConnectionToken((string) $user->id, [
            'name' => $user->name,
            'role' => $user->role,
        ]);

        return response()->json([
            'token' => $token,
            'user_id' => $user->id,
            'channels' => [
                'chat' => 'chat:admin',
                'trading' => CentrifugoService::CHANNEL_TRADING,
                'trades' => CentrifugoService::CHANNEL_TRADES,
                'news' => CentrifugoService::CHANNEL_NEWS,
                'community' => CentrifugoService::CHANNEL_COMMUNITY,
                'broadcast' => CentrifugoService::CHANNEL_BROADCAST,
            ],
        ]);
    }

    public function sendMessage(Request $request, CentrifugoService $centrifugo)
    {
        $request->validate([
            'message' => 'required|string|max:2000',
            'channel' => 'nullable|string|max:100',
        ]);

        $user = $request->user();
        $channel = $request->input('channel', 'chat:admin');

        $data = [
            'id' => (string) now()->timestamp,
            'type' => 'chat_message',
            'user_id' => $user->id,
            'user_name' => $user->name,
            'user_role' => $user->role,
            'message' => $request->message,
            'timestamp' => now()->toISOString(),
        ];

        $centrifugo->publish($channel, $data);

        return response()->json(['ok' => true, 'message' => 'Sent']);
    }
}

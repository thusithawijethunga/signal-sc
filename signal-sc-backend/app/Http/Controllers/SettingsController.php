<?php

namespace App\Http\Controllers;

use App\Models\AccountBalance;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Config;

class SettingsController extends Controller
{
    public function index()
    {
        $settings = [
            'telegram_bot_token' => Config::get('services.telegram.bot_token', ''),
            'telegram_chat_id' => Config::get('services.telegram.chat_id', ''),
            'google_sheets_url' => Config::get('services.google_sheets.url', ''),
        ];

        $balances = AccountBalance::all();

        return view('admin.settings.index', compact('settings', 'balances'));
    }

    public function update(Request $request)
    {
        $validated = $request->validate([
            'telegram_bot_token' => 'nullable|string|max:255',
            'telegram_chat_id' => 'nullable|string|max:255',
            'google_sheets_url' => 'nullable|url|max:500',
            'start_balance' => 'nullable|numeric',
            'deposit_balance' => 'nullable|numeric',
            'withdraw_balance' => 'nullable|numeric',
        ]);

        // Save balances to DB
        if ($request->hasAny(['start_balance', 'deposit_balance', 'withdraw_balance'])) {
            $balance = AccountBalance::firstOrCreate(['user_id' => $request->user()->id]);
            $balance->update([
                'start_balance' => $request->input('start_balance', $balance->start_balance),
                'deposit_balance' => $request->input('deposit_balance', $balance->deposit_balance),
                'withdraw_balance' => $request->input('withdraw_balance', $balance->withdraw_balance),
            ]);
        }

        $envPath = base_path('.env');

        $replacements = [
            'TELEGRAM_BOT_TOKEN' => $validated['telegram_bot_token'] ?? '',
            'TELEGRAM_CHAT_ID' => $validated['telegram_chat_id'] ?? '',
            'GOOGLE_SHEETS_URL' => $validated['google_sheets_url'] ?? '',
        ];

        foreach ($replacements as $key => $value) {
            if ($value !== '') {
                $envContent = file_get_contents($envPath);
                $pattern = "/^{$key}=.*/m";
                if (preg_match($pattern, $envContent)) {
                    $envContent = preg_replace($pattern, "{$key}={$value}", $envContent);
                } else {
                    $envContent .= "\n{$key}={$value}";
                }
                file_put_contents($envPath, $envContent);
            }
        }

        if ($request->wantsJson()) {
            return response()->json(['message' => 'Settings updated successfully']);
        }

        return redirect()->route('admin.settings.index')->with('success', 'Settings updated successfully.');
    }
}

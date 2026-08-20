<x-app-layout>
    <x-slot name="header">
        <h2 class="text-xl font-bold" style="color: var(--gold);"><i class="fas fa-cog me-2"></i>Settings</h2>
    </x-slot>

    <div class="space-y-4">
        {{-- Telegram Bot Settings --}}
        <div class="sx-card p-4" style="border-left:3px solid #3b82f6;">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color:#3b82f6;"><i class="fab fa-telegram me-1"></i> Telegram Bot Settings</h3>
            <form action="{{ route('admin.settings.update') }}" method="POST" class="space-y-3">
                @csrf
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs mb-1" style="color:var(--text-muted);">Bot Token</label>
                        <input type="text" name="tg_token" value="{{ config('settings.tg_token', '') }}" class="sx-input" placeholder="123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ">
                    </div>
                    <div>
                        <label class="block text-xs mb-1" style="color:var(--text-muted);">Chat ID / Channel</label>
                        <input type="text" name="tg_chatid" value="{{ config('settings.tg_chatid', '') }}" class="sx-input" placeholder="@signalxpress_official or -100xxxxxxxxxx">
                    </div>
                </div>
                <button type="submit" name="section" value="telegram" class="sx-btn sx-btn-gold"><i class="fas fa-save me-1"></i> Save Telegram Config</button>
            </form>
        </div>

        {{-- Google Sheets Sync --}}
        <div class="sx-card p-4" style="border-left:3px solid #10b981;">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color:#10b981;"><i class="fas fa-sync me-1"></i> Google Sheets Sync</h3>
            <form action="{{ route('admin.settings.update') }}" method="POST" class="space-y-3">
                @csrf
                <div>
                    <label class="block text-xs mb-1" style="color:var(--text-muted);">Google Apps Script Web App URL</label>
                    <input type="text" name="gs_url" value="{{ config('settings.gs_url', '') }}" class="sx-input" placeholder="https://script.google.com/macros/s/.../exec">
                </div>
                <button type="submit" name="section" value="google" class="sx-btn" style="background:#10b981;color:#000;border-color:#10b981;"><i class="fas fa-save me-1"></i> Save Google Sheets URL</button>
            </form>
        </div>

        {{-- Account Balances --}}
        <div class="sx-card p-4" style="border-left:3px solid var(--gold);">
            <h3 class="text-sm font-semibold uppercase mb-3" style="color:var(--gold);"><i class="fas fa-wallet me-1"></i> Account Balance Configuration</h3>
            <form action="{{ route('admin.settings.update') }}" method="POST" class="space-y-3">
                @csrf
                <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
                    <div>
                        <label class="block text-xs mb-1" style="color:var(--text-muted);">Start Balance ($)</label>
                        <input type="number" name="start_balance" step="0.01" value="{{ config('settings.start_balance', 1000) }}" class="sx-input">
                    </div>
                    <div>
                        <label class="block text-xs mb-1" style="color:var(--text-muted);">Deposit Balance ($)</label>
                        <input type="number" name="deposit_balance" step="0.01" value="{{ config('settings.deposit_balance', 0) }}" class="sx-input">
                    </div>
                    <div>
                        <label class="block text-xs mb-1" style="color:var(--text-muted);">Withdraw Balance ($)</label>
                        <input type="number" name="withdraw_balance" step="0.01" value="{{ config('settings.withdraw_balance', 0) }}" class="sx-input">
                    </div>
                </div>
                <button type="submit" name="section" value="balance" class="sx-btn sx-btn-gold"><i class="fas fa-save me-1"></i> Save Balances</button>
            </form>
        </div>

        @if(session('status'))
            <div class="sx-card p-3 text-sm" style="border-left:3px solid var(--green);color:var(--green);">{{ session('status') }}</div>
        @endif
    </div>
</x-app-layout>
